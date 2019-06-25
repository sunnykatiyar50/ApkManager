package com.sunnykatiyar.AppManager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.nononsenseapps.filepicker.FilePickerActivity;
import com.nononsenseapps.filepicker.Utils;
import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static android.content.Intent.EXTRA_PACKAGE_NAME;
import static com.sunnykatiyar.AppManager.AppListActivity.prefEditor;

public class ApkListFragment extends Fragment {

    private static final String TAG = "MAIN ACTIVITY : ";
    Button btn_search_apks;
    public static Menu option_menu;
    public static TextView text_msgs ;

    Context context = getContext();

    EditText value_local_path;
    List<File> list_files ;
    CustomApkListAdapter cla;
    List<ApkListDataItem> apkFilesList ;
    RecyclerView recyclerView;
    public static PackageManager pm ;
    public static List<ApkListDataItem> selected_files_list = new ArrayList<>();
    private boolean rootSelected ;
    private boolean rootAccess ;
    Snackbar snackbar;
    String value_global_path;
    final static String key_search_subfolders = "SEARCH_SUBFOLDERS";
    String value_search_subfolders;

    String moveToFolderName;
    File moveToFolder;

    DividerItemDecoration mDividerItemDecoration;
    public static NavigationView navigationView;
    AlertDialog.Builder builder;

    final String key_global_path = "GLOBAL_PATH";

    final String name_part_1 = RenameApkFragment.name_part_1;
    final String name_part_2 = RenameApkFragment.name_part_2;
    final String name_part_3 = RenameApkFragment.name_part_3;
    final String name_part_4 = RenameApkFragment.name_part_4;
    final String name_part_5 = RenameApkFragment.name_part_5;
    final String name_part_6 = RenameApkFragment.name_part_6;
    final String name_part_7 = RenameApkFragment.name_part_7;
    final String name_part_8 = RenameApkFragment.name_part_8;

    final String key_sorting = "SORT BY";
    String value_sorting;
    final String key_order_by = "INVERSE SORTING";
    String value_order_by;

    final String sort_by_name = "SORT_BY_NAME";
    final String sort_by_date = "SORT_BY_DATE";
    final String sort_by_size = "SORT_BY_SIZE";

    final String order_decreasing = "ORDER_INCREASING";
    final String order_increasing = "ORDER_DECREASING";


    public String sort_by ;
    public String order_by;
    final String name_format_data_saved = RenameApkFragment.name_format_data_saved;
    
    static {
        Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR);
        Shell.Config.verboseLogging(BuildConfig.DEBUG);
        //Shell.Config.setTimeout(10);
    }

    public ApkListFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_apk_list, container, false);

        recyclerView = v.findViewById(R.id.r_view);
        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);

        mDividerItemDecoration = new DividerItemDecoration(getContext(),llm.getOrientation());
        recyclerView.addItemDecoration(mDividerItemDecoration);

        pm = getContext().getPackageManager();

        btn_search_apks = v.findViewById(R.id.btn_browse_local_path);
        value_local_path = v.findViewById(R.id.edit_search_folder);

        value_global_path = AppListActivity.sharedPref.getString(key_global_path,"Path Not Set");
        value_local_path.setText(value_global_path);

        File dir_path = new File(value_local_path.getText().toString());

        if(dir_path.exists() & dir_path.isDirectory()){
            sort_by = AppListActivity.sharedPref.getString(value_sorting,sort_by_name);
            new LongTask().execute("search", dir_path.toString());
        }else{
            Toast.makeText(context,"Set a Valid Folder To Load Files.",Toast.LENGTH_SHORT);
        }

        text_msgs = v.findViewById(R.id.text_msgs);
        cla = new CustomApkListAdapter(apkFilesList,context);
        recyclerView.setAdapter(cla);
        cla.notifyDataSetChanged();

        btn_search_apks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                Intent i = new Intent(getContext(), FilePickerActivity.class);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);

                File f = new File(value_local_path.toString());

                if(f.exists() & f.isFile()){
                    i.putExtra(FilePickerActivity.EXTRA_START_PATH, f.getParent());

                }else if(f.exists() & f.isDirectory()){
                    i.putExtra(FilePickerActivity.EXTRA_START_PATH, f.getAbsoluteFile());
                }else{
                    i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());
                }

                startActivityForResult(i, 2);
            }
        });

        setHasOptionsMenu(true);

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        switch(requestCode)
        {
            case 2 : if(resultCode == RESULT_OK)
            {
                //Use the provided utility method to parse the result
                List<Uri> files = Utils.getSelectedFilesFromResult(data);
                File file = Utils.getFileForUri(files.get(0));
                value_local_path.setText(file.getPath());
                btn_search_apks.setEnabled(true);

            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu1, MenuInflater menuInflater) {
        Log.i(TAG," onCreateOptionsMenu : ");

        menuInflater.inflate(R.menu.menu_apk_list, menu1);
        this.option_menu = menu1;
        super.onCreateOptionsMenu(menu1, menuInflater);

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        Log.i(TAG," onPrepareOptionsMenu : ");

//-----------------------LOADING "SORT BY" FROM SHARED PREFERENCES---------------------------------------
        value_sorting = AppListActivity.sharedPref.getString(key_sorting,sort_by_name);
        Log.i(TAG,"Sorting Setting in Shared Preferences: "+value_sorting);

        if(value_sorting.equals(sort_by_name)){
            // Log.i(TAG," inside equating sort_by_name : ");
            sort_by = sort_by_name;
            menu.findItem(R.id.menuitem_sortbyname).setChecked(true);
            //sort_by = "Hello name";
        }
        else if(value_sorting.equals(sort_by_date)){
            //Log.i(TAG," inside equating sort_by_date : ");
            sort_by = sort_by_date;
            menu.findItem(R.id.menuitem_sortbydate).setChecked(true);
            //sort_by = "Hello date";

        }
        else if(value_sorting.equals(sort_by_size)){
            //Log.i(TAG," inside equating sort_by_size : ");
            sort_by = sort_by_size;
            menu.findItem(R.id.menuitem_sortbysize).setChecked(true);
            //sort_by = "Hello size";
        }
        Log.i(TAG," Value of sort_by : " + sort_by);

//--------------------------------LOADING "ORDER BY" FROM SHARED PREFERENCES---------------------------------------

        value_order_by = AppListActivity.sharedPref.getString(key_order_by,order_increasing);
        Log.i(TAG," Found Ordering Settings in SHARED PREFERENCES: "+ value_order_by);

        if(value_order_by.equals(order_decreasing)){
            option_menu.findItem(R.id.menuitem_decreasing).setChecked(true);
            order_by = order_decreasing;
        }
        else if(value_order_by.equals(order_increasing)){
            option_menu.findItem(R.id.menuitem_increasing).setChecked(true);
            order_by = order_increasing;
        }
        Log.i(TAG," Value of order by : " + order_by);

//----------------------------------------------------------------------------------------------------------------------
        option_menu.findItem(R.id.menuitem_root).setChecked(AppListActivity.sharedPref.getBoolean("ROOT",true));
        rootSelected = option_menu.findItem(R.id.menuitem_root).isChecked();
//--------------------------------------------------------------------------------------------------------------------
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        Log.i(TAG," onOptionsItemSelected : "+item.getTitle()+" AND  isItemChecked()="+item.isChecked());

        //--------------------------------SEARCH-----------------------------------------
        if(id == R.id.menuitem_search) {

            File dir_path = new File(value_local_path.getText().toString());
            new LongTask().execute("search",dir_path.toString());
            cla = new CustomApkListAdapter(apkFilesList,context);
            recyclerView.setAdapter(cla);
            cla.notifyDataSetChanged();
        }

        //-----------------------RELOAD------------------------------------------------
        if(id == R.id.menuitem_reset_path){

            if(AppListActivity.sharedPref.contains(key_global_path)){
                value_global_path = AppListActivity.sharedPref.getString(key_global_path,"Path Not Set");
                value_local_path.setText(value_global_path);
            }
        }

        if(id==R.id.menuitem_subdir){
            item.setChecked(!item.isChecked());
            prefEditor.putBoolean(key_search_subfolders,item.isChecked()).commit();
        }

        //---------------SELECT ROOT----------------------------------------------
        if (id == R.id.menuitem_root) {
            if(item.isChecked()) {
                item.setChecked(false);
                //  rootAccess = Shell.rootAccess();
                Toast.makeText(context, " ROOT Actions Disabled.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "ROOT SELECTED :" + rootSelected);
                Log.e(TAG, "ROOT ACCESS :" + rootAccess);
            }else if(!item.isChecked()) {
                item.setChecked(true);
                rootSelected = true;
//              rootAccess = Shell.rootAccess();
                Toast.makeText(context, " ROOT Actions Enabled.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "ROOT SELECTED :" + rootSelected);
                Log.e(TAG, "ROOT ACCESS :" + rootAccess);
            }
            prefEditor.putBoolean("ROOT",item.isChecked());
            prefEditor.commit();
        }

        //------------------INSTALL/UPDATE APPS--------------------------------
        if(id == R.id.menuitem_install) {

            if (cla != null) {
                this.selected_files_list = cla.getSelectedItemsList();
                Toast.makeText(context, " No. of selecetd files : " + cla.getSelectedItemsList().size(), Toast.LENGTH_LONG);
            }

            builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Confirm Installing "+selected_files_list.size()+" Files");
            builder.setMessage("Click Yes to Continue...");
            builder.setCancelable(false);
            boolean yes;
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(getContext(), "You've choosen to install.", Toast.LENGTH_SHORT).show();
                    if (selected_files_list != null & selected_files_list.size() > 0) {
                        if (rootSelected) {
                            rootAccess = Shell.rootAccess();
                            Log.i(TAG, "Root Access " + rootAccess);

                            if (rootAccess) {
                                new LongTask().execute("rootinstall", selected_files_list);
                            } else {
                                Log.i(TAG, " No ROOT ACCESS. Grant Root Access.\n Or Try after Unselecting ROOT From Menu");
                                Toast.makeText(context, "No ROOT ACCESS. ", Toast.LENGTH_SHORT).show();
                            }
                        } else if (!rootSelected) {
                            new LongTask().execute("norootinstall", selected_files_list);
                        }

                    } else{
                        Log.i(TAG, " No Items Selected.");
                        Toast.makeText(context, "No Items Selected. ", Toast.LENGTH_SHORT).show();
                    }                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(getContext(), "Installation Cancelled.", Toast.LENGTH_SHORT).show();
                }
            });

            builder.show();
        }

        //-------------------MOVE FILES----------------------
        if(id == R.id.menuitem_move_files){
            item.setChecked(!item.isChecked());
            new LongTask().execute("move",apkFilesList);
        }

        //---------------------SELECT ALL----------------------
        if(id == R.id.menuitem_select_all){

            item.setChecked(!item.isChecked());

            if(apkFilesList.size()>0){
                //-------------------Selecting each file--------------------
                for(ApkListDataItem l : apkFilesList){
                    l.select_box_state = item.isChecked();
                }
            }else{
                Toast.makeText(context,"First Load Files to Select",Toast.LENGTH_SHORT);
            }

            File dir_path = new File(value_local_path.getText().toString());
            if (dir_path.exists() & dir_path.isDirectory()) {
                new LongTask().execute("search",dir_path.toString());
                cla = new CustomApkListAdapter(apkFilesList,context);
                recyclerView.setAdapter(cla);
                cla.notifyDataSetChanged();

            }else{
                Toast.makeText(context,"Set Correct Path",Toast.LENGTH_SHORT);
            }
        }

        //----------------------UNINSTALL APPS----------------------------------
        if(id == R.id.menuitem_uninstall) {

            if (cla != null) {
                selected_files_list = cla.getSelectedItemsList();
                Toast.makeText(getContext(), " No. of selecetd files : " + cla.getSelectedItemsList().size(), Toast.LENGTH_LONG);
            }
            builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Confirm Batch Uninstall "+selected_files_list.size() +" Apps");
            builder.setMessage("Are You Sure ");
            builder.setCancelable(false);

            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(getContext(), "You've choosen to Uninstall.", Toast.LENGTH_SHORT).show();

                    if (selected_files_list != null & selected_files_list.size() > 0) {
                        if (rootSelected) {
                            rootAccess = Shell.rootAccess();
                            Log.i(TAG, "Root Access " + rootAccess);

                            if (rootAccess) {
                                new LongTask().execute("rootuninstall", selected_files_list);
                            } else {
                                Log.i(TAG, " No ROOT ACCESS. Grant Root Access.\n Or Try after Unselecting ROOT From Menu");
                                Toast.makeText(context, "No ROOT ACCESS. ", Toast.LENGTH_SHORT).show();
                            }
                        }else if (!rootSelected) {
                            new LongTask().execute("norootuninstall", selected_files_list);
                        }

                    }else{
                        Log.i(TAG, " No Items Selected.");
                        Toast.makeText(context, "No Items Selected. ", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(getContext(), "Uninstallation Cancelled.", Toast.LENGTH_SHORT).show();
                }
            });

            builder.show();

        }

        //----------------------DELETE FILES--------------------------------------
        if (id == R.id.menuitem_delete) {

            if (cla != null) {
                this.selected_files_list = cla.getSelectedItemsList();
                Toast.makeText(context, " No. of selecetd files : " + cla.getSelectedItemsList().size(), Toast.LENGTH_LONG);
            }

            builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Confirm Deleting "+selected_files_list.size()+" Files");
            builder.setMessage("Are You Sure");
            builder.setCancelable(false);
            boolean yes;
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(getContext(), "You've choosen to delete files.", Toast.LENGTH_SHORT).show();
                    new LongTask().execute("delete", selected_files_list);
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(getContext(), "Deletion Cancelled.", Toast.LENGTH_SHORT).show();
                }
            });

            builder.show();
            // Toast.makeText(context, "Its good to delete redundant stuffs ...\n \n... Just Be Sure", Toast.LENGTH_LONG).show();
        }

        //----------------------RENAME APKS-----------------------------------------------
        if (id == R.id.menuitem_rename) {

            if (cla != null) {
                this.selected_files_list = cla.getSelectedItemsList();
                //    Toast.makeText(context, " No. of selecetd files : " + cla.getSelectedItemCount(), Toast.LENGTH_LONG);
            }

          //showSnackBar("Renaming Files Count :"+cla.getSelectedItemsList().size());

            Toast.makeText(context, " ", Toast.LENGTH_LONG).show();

            builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Confirm Renaming "+selected_files_list.size()+" Files");
            builder.setMessage("Are You Sure");
            builder.setCancelable(false);
            boolean yes;
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(getContext(), "You've choosen to Rename.", Toast.LENGTH_SHORT).show();
                    new LongTask().execute("rename", selected_files_list);
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(getContext(), "Renaming Cancelled.", Toast.LENGTH_SHORT).show();
                }
            });

            builder.show();
        }

        //------------------------------------SORTING----------------------------------------
        if(id == R.id.menuitem_sortbyname){
            item.setChecked(true);
            sort_by = sort_by_name;
            prefEditor.putString(key_sorting, sort_by).commit();
            SortApkList();
        }

        if(id == R.id.menuitem_sortbydate){
            item.setChecked(true);
            sort_by = sort_by_date;
            prefEditor.putString(key_sorting, sort_by).commit();
            SortApkList();
        }

        if(id == R.id.menuitem_sortbysize){
            item.setChecked(true);
            //    Log.i(TAG,"Clicked sort by size");
            sort_by = sort_by_size;
            prefEditor.putString(key_sorting, sort_by).commit();
            SortApkList();
        }

        //-----------------------------------INVERSE SORTING-------------------------------------
        if(id == R.id.menuitem_decreasing){
            item.setChecked(true);
            order_by = order_decreasing;
            prefEditor.putString(key_order_by, order_by).commit();
            SortApkList();
        }

        if(id == R.id.menuitem_increasing){
            item.setChecked(true);
            order_by = order_increasing;
            prefEditor.putString(key_order_by, order_by).commit();
            SortApkList();
        }

        //-----------------------------------SELECT UPDATED APKS-------------------------------------
        if(id == R.id.menuitem_select_update){
            if(cla!=null & apkFilesList!=null){
                cla.SelectUpdatable();
                cla.notifyDataSetChanged();
            }
            ShowMsgInTextView();

        }


        return true;
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        mListener = null;
    }

    public class LongTask extends AsyncTask<Object,Object,String> {
        public LongTask() {
            super();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i(TAG,"in OnPreExecute :");

        }

        @Override
        protected void onProgressUpdate(Object... objects) {
            if(objects[0] instanceof String){
                Toast.makeText(context, (String) objects[0], Toast.LENGTH_LONG).show();
            }

            if(objects[0] instanceof File){
                Log.i(TAG,objects[0].getClass().toString());
                Log.i(TAG,"Loading : "+((File)objects[0]).getName());
                text_msgs.setText("Loading : "+((File)objects[0]).getName());
            }
            super.onProgressUpdate(objects);
        }

        @Override
        protected String doInBackground(Object... objects) {
            Log.i(TAG,"in DoInBackground :");

            if(objects[0].equals("search")){
                searchApks(new File((String)objects[1]));
                return "Searching completed." ;
            }
            else if(objects[0].equals("rootinstall")){
                RootInstall((List) objects[1]);
                return "Root Installation Completed." ;
            }
            else if(objects[0].equals("norootinstall")){
                NoRootInstall((List) objects[1]);
                return "Installation Completed." ;
            }else if(objects[0].equals("rootuninstall")){
                RootUninstall((List) objects[1]);
                return "Root Uninstall Operation completed." ;
            }else if(objects[0].equals("delete")){
                DeleteApks((List)objects[1] );
                return "Apk Deletion Operation completed." ;
            }else if(objects[0].equals("rename")) {
                RenameApks((List) objects[1]);
                return "Renaming completed." ;
            }else if(objects[0].equals("move")) {
                MoveApks((List) objects[1]);
                return "Renaming completed." ;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.i(TAG,"in OnPostExecute :");
            SortApkList();
            // showSnackBar(s);
            //  ShowMsgInTextView();
            super.onPostExecute(s);
        }

        public void searchApks(File dir_path){

            list_files = new ArrayList<>();
            apkFilesList = new ArrayList<>();

            //CHECK FOR CORRECT PATH
            if (dir_path.exists() & dir_path.isDirectory()) {
                // CHECK IF TO SEARCH IN SUBDIRECTORY
                if(AppListActivity.sharedPref.getBoolean(key_search_subfolders,true)) {
                    Log.i(TAG,"Search in Subfolder : True");
                    getAllSubDirFiles(dir_path);
                }

                // CHECK IF TO NOT SEARCH IN SUBDIRECTORY
                else if(!AppListActivity.sharedPref.getBoolean(key_search_subfolders,true)) {
                    Log.i(TAG,"Search in Subfolder : False");
                    getDirFiles(dir_path);
                    Log.i(TAG,list_files.size()+" files found.");
                    publishProgress("Path is set to : " + dir_path.getAbsolutePath());
                }
            }
            // IF INVALID FOLDER PATH
            else{
                //Toast.makeText(context,"Set Correct Path",Toast.LENGTH_SHORT);
                publishProgress("Set Correct Path");
            }

        }

        public void getAllSubDirFiles(File file1){
            //  Log.i(TAG,"getAllSubDirFiles: "+f1.getName());
            ApkListDataItem ldm ;
            for(File f1 : file1.listFiles()){
              //  Log.i(TAG,"File : "+f1.getName());
                if(f1.isFile() & f1.getName().endsWith(".apk")){
                    ldm = new ApkListDataItem(f1,context);
                    if(ldm.apk_pkg_info!=null){
                        apkFilesList.add(ldm);
                    }
                //    Log.i(TAG,"File : "+f1.getName());
                    publishProgress(f1);
                }
                else if(f1.isDirectory())
                {
                    getAllSubDirFiles(f1);
                }
            }
        }

        public void getDirFiles(File file1){
            for(File f1 : file1.listFiles()){
                if(f1.isFile() & f1.getName().endsWith(".apk")){
                    apkFilesList.add(new ApkListDataItem(f1,context));
                    publishProgress(f1);
                }
            }
        }

        public void RootInstall(List<ApkListDataItem> files_list){
            long apk_size;
            String command;
            List<String> std_err = new ArrayList<>();
            List<String> std_out = new ArrayList<>();

            Log.i(TAG, "Root Access " + rootAccess);
            Log.i(TAG, " No. of files to UPDATE/INSALL  " + selected_files_list.size());
            //   Toast.makeText(context, "No. of files to install : " + selected_files_list.size(), Toast.LENGTH_LONG).show();
            publishProgress("No. of files to install : " + selected_files_list.size());

            for (ApkListDataItem file : files_list) {
                try{
                    apk_size = file.file.length();
                    command = "cat \"" + file.file.getPath() + "\"|pm install -S " + apk_size;
                    Shell.su(command).to(std_out, std_err).exec();
                    //  Log.i(TAG, " EXECUTING :"+ command);
                    Log.i(TAG, " ROOT installing " + file.file_name + " : " + std_out.get(0));
                    publishProgress("Installation Output : "+std_out.get(0));
                    // Toast.makeText(ApkListActivity.this, "Installation Successfull" + ps.toString(), Toast.LENGTH_SHORT).show();
                }catch (Exception ex) {
                    if(!std_err.isEmpty()){
                        //  Log.e(TAG, " ROOT INSTALL ERROR of " + file.file_name + " \nError : " + std_err.get(0));
                        //   Toast.makeText(context, " Error installing \"" + file.file_name + "\"", Toast.LENGTH_SHORT).show();
                        publishProgress(" Error installing \"" + file.file_name + "\"");
                    }
                    Log.e(TAG, " ROOT INSTALL ERROR of " + file.file_name + " \nError : " + ex);
                }
            }
        }

        public void RootUninstall(List<ApkListDataItem> files_list){
            String command;
            List<String> std_err = new ArrayList<>();
            List<String> std_out = new ArrayList<>();

            Log.i(TAG, "Root Access " + rootAccess);
            Log.i(TAG, " No. of files to UNINSTALL  " + selected_files_list.size());
            publishProgress("No. of files to uninstall : " + selected_files_list.size());

            for (ApkListDataItem file : files_list) {
                if(file.isInstalled){
                    try{
                        command = "pm uninstall " + file.pkg_name;
                        Shell.su(command).to(std_out, std_err).exec();
                        Log.i(TAG, " EXECUTING :"+ command);
                        Log.i(TAG, " ROOT Uninstalling " + file.file_name + " : " + std_out.get(0));
                        publishProgress("UnInstallation Output : "+std_out.get(0));
                    }catch (Exception ex) {
                        if(!std_err.isEmpty()){
                            Log.e(TAG, " ROOT UNINSTALL ERROR : " + file.file_name + " \nError : " + std_err.get(0));
                            //Toast.makeText(context, " Error installing \"" + file.file_name + "\"", Toast.LENGTH_SHORT).show();
                            //publishProgress(" Error Uninstalling \"" + file.file_name + "\"");
                        }
                        //publishProgress(" Error Uninstalling \"" + file.file_name + "ex");
                        Log.e(TAG, " ROOT INSTALL ERROR of " + file.file_name + " \nError : " + ex);
                    }
                }
            }
        }

        public void Uninstall(List<ApkListDataItem> files_list){
            String command;
            List<String> std_err = new ArrayList<>();
            List<String> std_out = new ArrayList<>();
            Intent i;
            Log.i(TAG, "Root Access " + rootAccess);
            Log.i(TAG, " No. of files to UNINSTALL  " + selected_files_list.size());
            publishProgress("No. of files to uninstall : " + selected_files_list.size());

            for (ApkListDataItem file : files_list) {
                if(file.isInstalled){
                    Toast.makeText(context, "Confirm to uninstall " + file.app_name, Toast.LENGTH_SHORT).show();
                    i = new Intent(Intent.ACTION_DELETE);
                    //i.setData(Uri.parse(p.packageName));
                    i.putExtra(EXTRA_PACKAGE_NAME, file.pkg_name);
                    context.startActivity(i);
                    Log.i(TAG, " Uninstalling " + file.file_name + " : " + std_out.get(0));
                }
            }
        }

        public void MoveApks(List list){

        };


        public void NoRootInstall(List<ApkListDataItem> files_list){

            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setType("application/vnd.android.package-archive");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Log.i(TAG, "Root Access " + rootAccess);

            for (ApkListDataItem file : files_list)
            {
//                i.setData(file.file_uri);
//                context.startActivity(i);
            }
        }

        public void DeleteApks(List<ApkListDataItem> files_list){

            boolean isDeleted;
            for (ApkListDataItem file : files_list)
            {
                isDeleted = file.file.delete();
                Log.i(TAG,"Deletion of "+file.file_name+" : "+isDeleted);
            }
        }

        public void RenameApks(List<ApkListDataItem> files_list){
            Log.i(TAG,"Renaming Files Count :"+files_list.size());

            if(AppListActivity.sharedPref.contains(name_format_data_saved)){

                int part1 = AppListActivity.sharedPref.getInt(name_part_1,1);
                Log.i(TAG,"Part 1 :"+ part1);

                String part2 = AppListActivity.sharedPref.getString(name_part_2,"_v");
                Log.i(TAG,"Part 2 :"+part2);

                int part3 =AppListActivity.sharedPref.getInt(name_part_3,2);
                Log.i(TAG,"Part 3 :"+ part3);

                String part4 = AppListActivity.sharedPref.getString(name_part_4,"_");
                Log.i(TAG,"Part 4 :"+part4);

                int part5 =AppListActivity.sharedPref.getInt(name_part_5,3);
                Log.i(TAG,"Part 5 :"+ part5);

                String part6 = AppListActivity.sharedPref.getString(name_part_6,"");
                Log.i(TAG,"Part 6 :"+part6);

                int part7 =AppListActivity.sharedPref.getInt(name_part_7,0);
                Log.i(TAG,"Part 7 :"+ part7);

                String part8 = AppListActivity.sharedPref.getString(name_part_8,"");
                Log.i(TAG,"Part 8 :"+part8);

                File f1;
                File f2;
                String parent;
                boolean result ;
                for (ApkListDataItem f : files_list)
                {
                    f1 = new File(f.file.getAbsolutePath());
                    Log.i(TAG," f1 path : "+f.file.getAbsolutePath());
                    parent = f.file.getParent();
                    Log.i(TAG," f1 parent : "+parent);
                    f2 = new File(parent+"/"+getName(f,part1)+part2+getName(f,part3)+part4+getName(f,part5)+part6+getName(f,part7)+part8+".apk");
                    result = (f.file).renameTo(f2);
                    publishProgress("Renaming : \""+f.file_name+"\" - "+result);
                    Log.i(TAG,"Renaming "+f.file_name+" : \""+f2+"\" - "+result);
                }
            }else
            {
                Log.i(TAG,"Set a proper Name Format First");
                publishProgress("Set a proper Name Format First");
            }
        }

        protected String getName(ApkListDataItem ld, int i){
            switch(i){
                case 0:{  return "";              }
                case 1:{  return ld.app_name;     }
                case 2:{  return ld.apk_version_name;    }
                case 3:{  return ld.apk_version_code;  }
                case 4:{  return ld.file_size;  }
                case 5:{  return ld.pkg_name; }
                default : { return "";             }
            }
        }
    }

    public void ShowMsgInTextView(String str){
        String msg_text = "Total : "+apkFilesList.size()+"\t Selected : "+cla.getSelectedItemsList().size()+"\n"+str;
        text_msgs.setText(msg_text);
    }

    public void ShowMsgInTextView(){
        String msg_text = "Total : "+apkFilesList.size()+"\t Selected : "+cla.getSelectedItemsList().size();
        text_msgs.setText(msg_text);
    }

    public void SortApkList(){
        Comparator<ApkListDataItem> file_name_comparator = (ApkListDataItem l1, ApkListDataItem l2)-> l1.file_name.compareTo(l2.file_name);
        Comparator<ApkListDataItem> file_size_comparator = (ApkListDataItem l1, ApkListDataItem l2)-> Long.compare(l1.file.length(),l2.file.length());
        Comparator<ApkListDataItem> modified_date_comparator = (ApkListDataItem l1, ApkListDataItem l2)-> Long.compare(l1.file.lastModified(),l2.file.lastModified());
        Comparator<ApkListDataItem> creation_date_comparator = (ApkListDataItem l1, ApkListDataItem l2)-> Long.compare(l1.file_creation_time,l2.file_creation_time);

        Log.i(TAG," In SortApkList : sort by = "+ sort_by);

        switch(sort_by){
            case sort_by_name :{ Collections.sort(apkFilesList,file_name_comparator);
                if(order_by == order_decreasing){
                    Collections.reverse(apkFilesList);  }
                Log.i(TAG,"in sort_by_name");
                break;}

            case sort_by_date : {   Collections.sort(apkFilesList,creation_date_comparator);
                if(order_by == order_decreasing){
                    Collections.reverse(apkFilesList);
                }
                Log.i(TAG,"in sort_by_date");   break;}

            case sort_by_size : {  Collections.sort(apkFilesList,file_size_comparator);
                if(order_by == order_decreasing){
                    Collections.reverse(apkFilesList);}
                Log.i(TAG,"in sort_by_size");  break;}

            default : { Collections.sort(apkFilesList,file_name_comparator);    break;}
        }

        recyclerView.setAdapter(cla);
        cla.notifyDataSetChanged();
        ShowMsgInTextView();

    }

}

package com.sunnykatiyar.appmanager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.sunnykatiyar.appmanager.ActivityMain.prefEditApkManager;
import static com.sunnykatiyar.appmanager.ActivityMain.prefEditRepository;
import static com.sunnykatiyar.appmanager.ActivityMain.sharedPrefSettings;
import static com.sunnykatiyar.appmanager.ActivityMain.sharedPrefRepository;

class FragmentApkFiles extends Fragment {


    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int REQUEST_CODE_BROWSE_LOCAL_PATH = 29;
    private static final int REQUEST_CODE_DESTINATION_MOVE_FOLDER = 31;
    private static final int REQUEST_CODE_DESTINATION_COPY_FOLDER = 37;

    private static final String TAG = "APKLIST FRAGMENT ACTIVITY : ";
    private static final String key_root_access = FragmentSettings.key_root_access;
    private boolean rootAccess;

    private static Menu option_menu;
    public static TextView msg_textview;

    Context context ;

    private EditText text_local_path;
    private static AdapterApkList cla;
    private List<ObjectApkFile> apkFilesList;
    private RecyclerView recyclerView;
    public static PackageManager pm;
    private static List<ObjectApkFile> selected_files_list = new ArrayList<>();
    private static final String key_local_path = "SEARCH_FOLDER";
    String value_local_path;

    private final static String key_search_subfolders = "SEARCH_SUBFOLDERS";
    
    private final String toast_msg = "show_as_toast";
    private final String textview_msg = "show_in_textview";

    final String name_part_1 = FragmentApkSettings.name_part_1;
    final String name_part_2 = FragmentApkSettings.name_part_2;
    final String name_part_3 = FragmentApkSettings.name_part_3;
    final String name_part_4 = FragmentApkSettings.name_part_4;
    final String name_part_5 = FragmentApkSettings.name_part_5;
    final String name_part_6 = FragmentApkSettings.name_part_6;
    final String name_part_7 = FragmentApkSettings.name_part_7;
    final String name_part_8 = FragmentApkSettings.name_part_8;

    private final String key_sorting = "SORT BY";
    private String value_sorting;
    private final String key_order_by = "INVERSE SORTING";

    private final String sort_apks_by_name = "SORT_BY_NAME";
    private final String sort_apks_by_date = "SORT_BY_DATE";
    private final String sort_apks_by_size = "SORT_BY_SIZE";

    private final String order_apks_decreasing = "ORDER_INCREASING";
    private final String order_apks_increasing = "ORDER_DECREASING";

    private String sort_apks_by;
    private String order_apks_by;
    final String name_format_data_saved = FragmentApkSettings.name_format_data_saved;
    private boolean label_reset = false;

    private final String SEARCH_APKS = "SEARCH_APKS";
    private final String RENAME_APKS = "RENAME_APKS";
    private final String ROOT_DELETE = "ROOT_DELETE_APKS";
    private final String NO_ROOT_DELETE = "NO_ROOT_DELETE_APKS";
    private final String ROOT_AUTOMOVE_TO_REPO = "ROOT_AUTOMOVE_TO_REPO_APKS";
    private final String NO_ROOT_AUTOMOVE_TO_REPO = "NO_ROOT_AUTOMOVE_TO_REPO_APKS";
    private final String ROOT_MOVE_MANUALLY = "ROOT_MOVE_MANUALLY";
    private final String ROOT_COPY_MANUALLY = "ROOT_COPY_MANUALLY";
    private final String ROOT_INSTALL = "ROOT_INSTALL_APKS";
    private final String NO_ROOT_INSTALL = "NO_ROOT_INSTALL_APKS";
    private final String ROOT_UNINSTALL = "ROOT_UNINSTALL_APKS";
    private final String NO_ROOT_UNINSTALL = "NO_ROOT_UNINSTALL_APKS";
    private final String BUILD_REPO = "BUILD_REPOSITORY";

    private File dir_path;
    private ApkAsyncTasks apkAsyncTask;
    
    public FragmentApkFiles() {
        // Required empty public constructor
    }

    static {
        Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR);
        Shell.Config.verboseLogging(BuildConfig.DEBUG);
        Shell.Config.setTimeout(10);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Log.i(TAG,"onCreate() : rootAccess : "+rootAccess);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_apk_manager, container, false);
        recyclerView = v.findViewById(R.id.r_view);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(llm);
        setRetainInstance(true);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(getContext(), llm.getOrientation());
        recyclerView.addItemDecoration(mDividerItemDecoration);
        context = getContext();
        pm = getContext().getPackageManager();

        Button btn_local_path = v.findViewById(R.id.btn_browse_local_path);
        text_local_path = v.findViewById(R.id.edit_search_folder);
        msg_textview = v.findViewById(R.id.text_msgs);

        text_local_path.setText(ActivityMain.sharedPrefApkManager.getString(key_local_path, "Path Not Set"));

        dir_path = new File(text_local_path.getText().toString());

        if (dir_path.exists() & dir_path.isDirectory())
        {
            sort_apks_by = ActivityMain.sharedPrefApkManager.getString(value_sorting, sort_apks_by_name);
            if(apkAsyncTask==null){
                showMsg(toast_msg,"New ApkAsyncTask Created : ",false);
                apkAsyncTask = new ApkAsyncTasks(SEARCH_APKS);
                apkAsyncTask.execute(dir_path);
            }else{
                showMsg(toast_msg,"Old ApkAsyncTask Used : ",false);
            }
        }else{
            showMsg(toast_msg,"Set a Valid Folder To Load Files.",false);
        }

        cla = new AdapterApkList(apkFilesList, getContext());
        recyclerView.setAdapter(cla);
        cla.notifyDataSetChanged();
//        showMsgDefault("");

        registerForContextMenu(recyclerView);

        btn_local_path.setOnClickListener(v1 -> {

            Intent i = new Intent(getContext(), FilePickerActivity.class);
            i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
            i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
            i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
            i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

            File f = new File(text_local_path.getText().toString());
            Log.i(TAG,f.getAbsolutePath());

            if (f.exists() & f.isFile()) {
                i.putExtra(FilePickerActivity.EXTRA_START_PATH, f.getParent());
                Log.i(TAG," exists but is file"+f.getAbsolutePath());
            } else if (f.exists() & f.isDirectory() & f.listFiles()!=null){
                i.putExtra(FilePickerActivity.EXTRA_START_PATH, f.getAbsolutePath());
                Log.i(TAG,"EXISTS AND IS FOLDER"+f.getAbsolutePath());

            }else{
                i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());
                Log.i(TAG,"DOES NOT EXISTS . So"+f.getAbsolutePath());
            }

            startActivityForResult(i, REQUEST_CODE_BROWSE_LOCAL_PATH);
        });
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {

            case REQUEST_CODE_BROWSE_LOCAL_PATH:
                if (resultCode == RESULT_OK) {
                    //Use the provided utility method to parse the result

                    Uri uri = data.getData();
                    File file = Utils.getFileForUri(uri);
//                    File file = new File(uri.getPath());
                    prefEditApkManager.putString(key_local_path,file.getAbsolutePath()).commit();
                    text_local_path.setText(file.getAbsolutePath());

                    msg_textview.setText("You Should Check RENAME Settings on files on different folder");
                    apkAsyncTask = new ApkAsyncTasks(SEARCH_APKS);
                    apkAsyncTask.execute(file);
                }

            case REQUEST_CODE_DESTINATION_MOVE_FOLDER:
                if (resultCode == RESULT_OK) {

                    Uri uri = data.getData();
                  File file = Utils.getFileForUri(uri);
                  File file1 = new File(uri.getPath());

                  Log.i(TAG," file : "+file.getAbsolutePath());
                  Log.i(TAG,"file 1 : "+file1.getAbsolutePath());

                    if(file.exists() & file.isDirectory()){
                        if(rootAccess){
                            showMsg(toast_msg, "Going for Root Move Apk : " + true,false);
                            new ApkAsyncTasks(ROOT_MOVE_MANUALLY).execute(selected_files_list,file);
                        }else{
                            showMsg(toast_msg, "Going for NO Root Move Apk : " + false,false);
                            String NO_ROOT_MOVE_MANUALLY = "NO_ROOT_MOVE_MANUALLY";
                            new ApkAsyncTasks(NO_ROOT_MOVE_MANUALLY).execute(selected_files_list,file);
                        }
                    }else{
                        showMsg(toast_msg, "Invalid Move Location : ",false);
                    }
                }

            case REQUEST_CODE_DESTINATION_COPY_FOLDER:
                if (resultCode == RESULT_OK) {

                    Uri uri = data.getData();
                    File file = Utils.getFileForUri(uri);
//                    File file1 = new File(uri.getPath());
//
//                    Log.i(TAG," file : "+file.getAbsolutePath());
//                    Log.i(TAG,"file 1 : "+file1.getAbsolutePath());

                    if(file.exists() & file.isDirectory()){
                        if(rootAccess){
                            showMsg(toast_msg, "Going for Root COPY Apk : " + true,false);
                            new ApkAsyncTasks(ROOT_COPY_MANUALLY).execute(selected_files_list,file);
                        }else{
                            showMsg(toast_msg, "Going for NO Root COPY Apk : " + false,false);
                            String NO_ROOT_COPY_MANUALLY = "NO_ROOT_COPY_MANUALLY";
                            new ApkAsyncTasks(NO_ROOT_COPY_MANUALLY).execute(selected_files_list,file);
                        }
                    }else{
                        showMsg(toast_msg, "Invalid Move Location : ",false);
                    }
                }


        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu1, MenuInflater menuInflater) {
        Log.i(TAG, " onCreateOptionsMenu : ");

        menuInflater.inflate(R.menu.menu_apk_list, menu1);
        option_menu = menu1;
        super.onCreateOptionsMenu(menu1, menuInflater);

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        Log.i(TAG, " onPrepareOptionsMenu : ");

//-----------------------LOADING "SORT BY" FROM SHARED PREFERENCES---------------------------------------
        value_sorting = ActivityMain.sharedPrefApkManager.getString(key_sorting, sort_apks_by_name);
        Log.i(TAG, "Sorting Setting in Shared Preferences: " + value_sorting);

        if (value_sorting.equals(sort_apks_by_name)) {
            // Log.i(TAG," inside equating sort_apks_by_name : ");
            sort_apks_by = sort_apks_by_name;
            menu.findItem(R.id.menuitem_sortbyname).setChecked(true);
            //sort_apks_by = "Hello name";
        } else if (value_sorting.equals(sort_apks_by_date)) {
            //Log.i(TAG," inside equating sort_apks_by_date : ");
            sort_apks_by = sort_apks_by_date;
            menu.findItem(R.id.menuitem_sortbydate).setChecked(true);
            //sort_apks_by = "Hello date";

        } else if (value_sorting.equals(sort_apks_by_size)) {
            //Log.i(TAG," inside equating sort_apks_by_size : ");
            sort_apks_by = sort_apks_by_size;
            menu.findItem(R.id.menuitem_sortbysize).setChecked(true);
            //sort_apks_by = "Hello size";
        }
        Log.i(TAG, " Value of sort_apks_by : " + sort_apks_by);

//--------------------------------LOADING "ORDER BY" FROM SHARED PREFERENCES---------------------------------------

        String value_order_by = ActivityMain.sharedPrefApkManager.getString(key_order_by, order_apks_increasing);
        Log.i(TAG, " Found Ordering Settings in SHARED PREFERENCES: " + value_order_by);

        if (value_order_by.equals(order_apks_decreasing)) {
            option_menu.findItem(R.id.menuitem_decreasing).setChecked(true);
            order_apks_by = order_apks_decreasing;
        } else if (value_order_by.equals(order_apks_increasing)) {
            option_menu.findItem(R.id.menuitem_increasing).setChecked(true);
            order_apks_by = order_apks_increasing;
        }
        Log.i(TAG, " Value of order by : " + order_apks_by);

//----------------------------------------------------------------------------------------------------------------------
        rootAccess = sharedPrefSettings.getBoolean(key_root_access,false);
        if(rootAccess){
            if(!Shell.rootAccess()){
                rootAccess=false;
            }
        }

        Log.i(TAG,"onPrepareMenu() : rootAccess : "+rootAccess);

        if(rootAccess){
            option_menu.findItem(R.id.menuitem_root).setTitle("Root is Enabled");
        }else {
            option_menu.findItem(R.id.menuitem_root).setTitle("Root is Disabled");
        }

//--------------------------------------------------------------------------------------------------------------------
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        Log.i(TAG, " onOptionsItemSelected : " + item.getTitle() + " AND  isItemChecked()=" + item.isChecked());

        //--------------------------------SEARCH-----------------------------------------
        if (id == R.id.menuitem_load) {
            showMsg(toast_msg, SEARCH_APKS, false);
            dir_path = new File(text_local_path.getText().toString());
            new ApkAsyncTasks(SEARCH_APKS).execute(dir_path);
            cla = new AdapterApkList(apkFilesList, getContext());
            recyclerView.setAdapter(cla);
            cla.notifyDataSetChanged();
        }

        //-------------------------SEARCH_SUBFOLDERS-----------------------------------
        if (id == R.id.menuitem_subdir) {
            item.setChecked(!item.isChecked());
            prefEditApkManager.putBoolean(key_search_subfolders, item.isChecked()).commit();
        }

        //-------------------------REFRESH lIST-----------------------------------
        if (id == R.id.menuitem_refresh) {
            cla = new AdapterApkList(apkFilesList, getContext());
            recyclerView.setAdapter(cla);
            cla.notifyDataSetChanged();
        }

        //--------------------------SELECT_ROOT----------------------------------------------
        if (id == R.id.menuitem_root) {
            if (rootAccess){
               showMsg(toast_msg," ROOT Actions are Enabled. \n To turn off go to AppSettings",false);
            } else  {
                showMsg(toast_msg," ROOT Actions Disbled.\n To turn on go to AppSettings",false);
            }
        }

//########################################################################################################################

        //------------------INSTALL/UPDATE APPS--------------------------------
        AlertDialog.Builder builder;
        if (id == R.id.menuitem_install) {
            if (cla != null) {
                selected_files_list = cla.getSelectedItemsList();
                Toast.makeText(getContext(), " No. of selecetd files : " + cla.getSelectedItemsList().size(), Toast.LENGTH_LONG);
            }
            builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Confirm Installing " + selected_files_list.size() + " Files");
            builder.setMessage("Click Yes to Continue...");
            builder.setCancelable(false);
            boolean yes;
            builder.setPositiveButton("Yes", (dialog, which) -> {
                Toast.makeText(getContext(), "You've choosen to install.", Toast.LENGTH_SHORT).show();
                if (selected_files_list != null & selected_files_list.size() > 0) {

                    if (rootAccess) {
                        showMsg(toast_msg, " Going for ROOT Installation. ", false);
                        new ApkAsyncTasks(ROOT_INSTALL).execute(selected_files_list);
                    }
                    else {
                        new ApkAsyncTasks(NO_ROOT_INSTALL).execute(selected_files_list);
                        showMsg(toast_msg, " Going for No ROOT Installation. ", false);
                    }
                }else{
                    showMsg(toast_msg, "No Items Selected. ", false);
                }
            });

            builder.setNegativeButton("No", (dialog, which) -> Toast.makeText(getContext(), "Installation Cancelled.", Toast.LENGTH_SHORT).show());

            builder.show();
        }

        //-------------------PREPARE_REPOSITORY----------------------
        if (id == R.id.menuitem_make_repository) {
            String key_repository_folder = FragmentSettings.key_repository_folder;
            String path_not_set = "PATH NOT SET";
            File file1 = new File(sharedPrefSettings.getString(key_repository_folder, path_not_set));
            Log.i(TAG,"Found Repository PAth : "+ sharedPrefSettings.getString(key_repository_folder, path_not_set));

            builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Confirm Refreshing Repository Apks.\n This May take some time. ");
            builder.setMessage("Click Yes to Continue...");
            builder.setCancelable(false);

            builder.setPositiveButton("Yes", (dialog, which) -> {
                Toast.makeText(getContext(), "Rebuilding Repository Data.", Toast.LENGTH_SHORT).show();
                new ApkAsyncTasks(BUILD_REPO).execute(file1);
            });

            builder.setNegativeButton("No", (dialog, which) -> Toast.makeText(getContext(), "Reloading Repository Cancelled.", Toast.LENGTH_SHORT).show());
            builder.show();
        }

        //----------------------------MOVE MANUALLY------------------------------------------
        if(id == R.id.menuitem_move_manually){

            if (cla != null) {
                selected_files_list = cla.getSelectedItemsList();
                Toast.makeText(getContext(), " No. of selecetd files : " + cla.getSelectedItemsList().size(), Toast.LENGTH_LONG);
            }

            builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Confirm Moving " + selected_files_list.size() + " Files To Repository Folder");
            builder.setMessage("Click Yes to Continue...");
            builder.setCancelable(false);

            builder.setPositiveButton("Yes", (dialog, which) -> {
                Toast.makeText(getContext(), "Trying To Move Files.", Toast.LENGTH_SHORT).show();
                if (selected_files_list != null & selected_files_list.size() > 0) {
                    Intent i = new Intent(getContext(), FilePickerActivity.class);
                    i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                    i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
                    i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
                    startActivityForResult(i, REQUEST_CODE_DESTINATION_MOVE_FOLDER);

//                        if(rootAccess){
//                            showMsg(toast_msg, "Going for Root Move Apk" + rootAccess,false);
//                            new ApkAsyncTasks(ROOT_AUTOMOVE_TO_REPO).execute(selected_files_list);
//                        }else if(!rootAccess){
//                            showMsg(toast_msg, "Going for NoRoot Move Apk" + rootAccess,false);
//                            new ApkAsyncTasks(NO_ROOT_AUTOMOVE_TO_REPO).execute(selected_files_list);
//                        }

                } else {
                    Log.i(TAG, " No Items Selected.");
                    Toast.makeText(getContext(), "No Items Selected. ", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("No", (dialog, which) -> Toast.makeText(getContext(), "MOving Cancelled.", Toast.LENGTH_SHORT).show());

            builder.show();

        }


        //----------------------------MOVE MANUALLY------------------------------------------
        if(id == R.id.menuitem_copy_files){

            if (cla != null) {
                selected_files_list = cla.getSelectedItemsList();
                Toast.makeText(getContext(), " No. of selecetd files : " + cla.getSelectedItemsList().size(), Toast.LENGTH_LONG);
            }

            builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Confirm Copying " + selected_files_list.size() + " Files To Selected Folder");
            builder.setMessage("Click Yes to Continue...");
            builder.setCancelable(false);

            builder.setPositiveButton("Yes", (dialog, which) -> {
                Toast.makeText(getContext(), "Trying To Move Files.", Toast.LENGTH_SHORT).show();
                if (selected_files_list != null & selected_files_list.size() > 0) {
                    Intent i = new Intent(getContext(), FilePickerActivity.class);
                    i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                    i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
                    i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
                    startActivityForResult(i, REQUEST_CODE_DESTINATION_COPY_FOLDER);

//                        if(rootAccess){
//                            showMsg(toast_msg, "Going for Root Move Apk" + rootAccess,false);
//                            new ApkAsyncTasks(ROOT_AUTOMOVE_TO_REPO).execute(selected_files_list);
//                        }else if(!rootAccess){
//                            showMsg(toast_msg, "Going for NoRoot Move Apk" + rootAccess,false);
//                            new ApkAsyncTasks(NO_ROOT_AUTOMOVE_TO_REPO).execute(selected_files_list);
//                        }

                } else {
                    Log.i(TAG, " No Items Selected.");
                    Toast.makeText(getContext(), "No Items Selected. ", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("No", (dialog, which) -> Toast.makeText(getContext(), "MOving Cancelled.", Toast.LENGTH_SHORT).show());

            builder.show();

        }


        //-----------------------------AUTOMOVE FILES TO REPO----------------------
        if (id == R.id.menuitem_move_files) {

            if (cla != null) {
                selected_files_list = cla.getSelectedItemsList();
                Toast.makeText(getContext(), " No. of selecetd files : " + cla.getSelectedItemsList().size(), Toast.LENGTH_LONG);
            }

            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                    if(checkPermission()) {
                        String path = Environment.getExternalStorageDirectory().toString();
                        builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Confirm Moving " + selected_files_list.size() + " Files To Repository Folder");
                        builder.setMessage("Click Yes to Continue...");
                        builder.setCancelable(false);

                        builder.setPositiveButton("Yes", (dialog, which) -> {
                            Toast.makeText(getContext(), "Trying To Move Files.", Toast.LENGTH_SHORT).show();
                            if (selected_files_list != null & selected_files_list.size() > 0) {

                                if(rootAccess){
                                    showMsg(toast_msg, "Going for Root Move Apk : " + true,false);
                                    new ApkAsyncTasks(ROOT_AUTOMOVE_TO_REPO).execute(selected_files_list);
                                }else {
                                    showMsg(toast_msg, "Going for NoRoot Move Apk : " + false,false);
                                    new ApkAsyncTasks(NO_ROOT_AUTOMOVE_TO_REPO).execute(selected_files_list);
                                }

                            } else {
                                Log.i(TAG, " No Items Selected.");
                                Toast.makeText(getContext(), "No Items Selected. ", Toast.LENGTH_SHORT).show();
                            }
                        });

                        builder.setNegativeButton("No", (dialog, which) -> Toast.makeText(getContext(), "MOving Cancelled.", Toast.LENGTH_SHORT).show());

                        builder.show();
                    }else{
                        requestPermission(); // Code for permission
                    }
            }


        }


        //----------------------UNINSTALL APPS----------------------------------
        if (id == R.id.menuitem_uninstall) {

            if (cla != null) {
                selected_files_list = cla.getSelectedItemsList();
                Toast.makeText(getContext(), " No. of selecetd files : " + cla.getSelectedItemsList().size(), Toast.LENGTH_LONG);
            }

            builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Confirm Batch Uninstall " + selected_files_list.size() + " Apps");
            builder.setMessage("Are You Sure ");
            builder.setCancelable(false);

            builder.setPositiveButton("Yes", (dialog, which) -> {
                if (selected_files_list != null & selected_files_list.size() > 0) {
                    if (rootAccess) {
                            showMsg(toast_msg, "Going for Root Uninstallation" + true,false);
                            new ApkAsyncTasks(ROOT_UNINSTALL).execute(selected_files_list);
                    }else {
                        showMsg(toast_msg, "Going for NoRoot Uninstallation" + false,false);
                        new ApkAsyncTasks(NO_ROOT_UNINSTALL).execute(selected_files_list);
                    }
                }else{
                    Log.i(TAG, " No Items Selected.");
                    Toast.makeText(getContext(), "No Items Selected. ", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("No", (dialog, which) -> Toast.makeText(getContext(), "Uninstallation Cancelled.", Toast.LENGTH_SHORT).show());
            builder.show();
        }


        //----------------------DELETE FILES--------------------------------------
        if (id == R.id.menuitem_delete) {

            if (cla != null) {
                selected_files_list = cla.getSelectedItemsList();
                Toast.makeText(getContext(), " No. of selecetd files : " + cla.getSelectedItemsList().size(), Toast.LENGTH_LONG);
            }

            builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Confirm Deleting " + selected_files_list.size() + " Files");
            builder.setMessage("Are You Sure");
            builder.setCancelable(false);
            builder.setPositiveButton("Yes", (dialog, which) -> {
                Toast.makeText(getContext(), "You've choosen to delete files.", Toast.LENGTH_SHORT).show();

                if (selected_files_list != null & selected_files_list.size() > 0) {
                    if (rootAccess) {
                        showMsg(toast_msg, "Going for Root Apk Deletion" + true,false);
                        new ApkAsyncTasks(ROOT_DELETE).execute(selected_files_list);
                    }else {
                        showMsg(toast_msg, "Going for NoRoot Apk Deletion" + false,false);
                        new ApkAsyncTasks(NO_ROOT_DELETE).execute(selected_files_list);
                    }
                }else{
                    Log.i(TAG, " No Items Selected.");
                    Toast.makeText(getContext(), "No Items Selected. ", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("No", (dialog, which) -> Toast.makeText(getContext(), "Deletion Cancelled.", Toast.LENGTH_SHORT).show());

            builder.show();
            // Toast.makeText(getContext(), "Its good to delete redundant stuffs ...\n \n... Just Be Sure", Toast.LENGTH_LONG).show();
        }


        //----------------------RENAME APKS-----------------------------------------------
        if (id == R.id.menuitem_rename) {

            if (cla != null) {
                selected_files_list = cla.getSelectedItemsList();
                //Toast.makeText(getContext(), " No. of selecetd files : " + cla.getSelectedItemCount(), Toast.LENGTH_LONG);
            }
            Toast.makeText(getContext(), " ", Toast.LENGTH_LONG).show();

            builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Confirm Renaming " + selected_files_list.size() + " Files");
            builder.setMessage("Are You Sure");
            builder.setCancelable(false);
            boolean yes;
            builder.setPositiveButton("Yes", (dialog, which) -> {
                Toast.makeText(getContext(), "You've choosen to Rename.", Toast.LENGTH_SHORT).show();
                if (selected_files_list != null & selected_files_list.size() > 0) {
                        showMsg(toast_msg, "Going for Root Rename" + rootAccess,false);
                        new ApkAsyncTasks(RENAME_APKS).execute(selected_files_list);
                }else{
                    Log.i(TAG, " No Items Selected.");
                    Toast.makeText(getContext(), "No Items Selected. ", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("No", (dialog, which) -> Toast.makeText(getContext(), "Renaming Cancelled.", Toast.LENGTH_SHORT).show());

            builder.show();
        }

//########################################################################################################################

        //---------------------SELECT ALL-------------------------------------
        if (id == R.id.menuitem_select_all) {

            // item.setChecked(!item.isChecked());

            if (apkFilesList.size() > 0) {
                //-------------------Selecting each file--------------------
                for (ObjectApkFile item1 : apkFilesList) {
                    item1.select_box_state = true;
                }
            }
            else{
                Toast.makeText(getContext(), "First Load Files to Select", Toast.LENGTH_SHORT);
            }
            cla = new AdapterApkList(apkFilesList, getContext());
            recyclerView.setAdapter(cla);
            cla.notifyDataSetChanged();
        }

        //---------------------------UNSELECT_ALL-------------------------------------
        if (id == R.id.menuitem_unselect_all) {
            if (apkFilesList.size() > 0) {
                //-------------------Selecting each file--------------------
                for (ObjectApkFile l : apkFilesList) {
                    l.select_box_state = false;
                }
            }else{
                Toast.makeText(getContext(), "First Load Files to Select", Toast.LENGTH_SHORT);
            }
            cla = new AdapterApkList(apkFilesList, getContext());
            recyclerView.setAdapter(cla);
            cla.notifyDataSetChanged();
            showMsgDefault("");
        }
        
        //--------------------INVERT SELECTION------------------------------------
        if (id == R.id.menuitem_select_invert) {

            item.setChecked(!item.isChecked());

            if (apkFilesList.size() > 0) {
                //-------------------Selecting each file--------------------
                for (ObjectApkFile l : apkFilesList) {
                    l.select_box_state = !l.select_box_state;
                }
            } else {
                Toast.makeText(getContext(), "First Load Files to Select", Toast.LENGTH_SHORT);
            }
            cla = new AdapterApkList(apkFilesList, getContext());
            recyclerView.setAdapter(cla);
            cla.notifyDataSetChanged();
            showMsgDefault("");
        }

//########################################################################################################################

        //------------------------------------SORTING----------------------------------------
        if (id == R.id.menuitem_sortbyname) {
            if(!item.isChecked()){
                item.setChecked(true);
            }            sort_apks_by = sort_apks_by_name;
            prefEditApkManager.putString(key_sorting, sort_apks_by).commit();
            SortApkList(apkFilesList);
            cla.notifyDataSetChanged();
        }

        if (id == R.id.menuitem_sortbydate) {
            if(!item.isChecked()){
                item.setChecked(true);
            }            sort_apks_by = sort_apks_by_date;
            prefEditApkManager.putString(key_sorting, sort_apks_by).commit();
            SortApkList(apkFilesList);
            cla.notifyDataSetChanged();
        }

        if (id == R.id.menuitem_sortbysize) {
            if(!item.isChecked()){
                item.setChecked(true);
            }            sort_apks_by = sort_apks_by_size;
            prefEditApkManager.putString(key_sorting, sort_apks_by).commit();
            SortApkList(apkFilesList);
            cla.notifyDataSetChanged();
        }

        //----------------------------------ORDER BY-------------------------------------
        if (id == R.id.menuitem_decreasing) {
            if(!item.isChecked()){
                item.setChecked(true);
            }            order_apks_by = order_apks_decreasing;
            prefEditApkManager.putString(key_order_by, order_apks_by).commit();
            SortApkList(apkFilesList);
            cla.notifyDataSetChanged();
        }

        if (id == R.id.menuitem_increasing) {
            if(!item.isChecked()){
                item.setChecked(true);
            }            order_apks_by = order_apks_increasing;
            prefEditApkManager.putString(key_order_by, order_apks_by).commit();
            SortApkList(apkFilesList);
            cla.notifyDataSetChanged();
        }

//########################################################################################################################
        //-----------------------------------SELECT UPDATED APKS-------------------------------------
        if (id == R.id.menuitem_select_update) {
            if (cla != null & apkFilesList != null) {
                cla.SelectUpdatable();
                cla.notifyDataSetChanged();
            }
            showMsgDefault("");
        }

        //-----------------------------------SELECT NEW VERSIONS-------------------------------------
        if (id == R.id.menuitem__duplicate_new_version) {
            if (cla != null & apkFilesList != null){
                SelectNewVersionsApks(apkFilesList);
            }
            cla.notifyDataSetChanged();
        //showMsgDefault(false,"");        }
        showMsgDefault("");        }

    //-----------------------------------SELECT OLD VERSIONS-------------------------------------
        if (id == R.id.menuitem__duplicate_old_version) {
            if (cla != null & apkFilesList != null){
                SelectOldVersionsApks(apkFilesList);
            }
            cla.notifyDataSetChanged();
            showMsgDefault("");

        }

        //-----------------------------------SELECT SMALLER APKS-------------------------------------
        if (id == R.id.menuitem_duplicate_size_smaller) {
            if (cla != null & apkFilesList != null){
                SelectSmallerSizeApks(apkFilesList);
            }
            cla.notifyDataSetChanged();
            showMsgDefault("");        }

        //-----------------------------------SELECT BIGGER APKS-------------------------------------
        if (id == R.id.menuitem_duplicate_size_larger) {
            if (cla != null & apkFilesList != null){
                SelectBiggerSizeApks(apkFilesList);
            }
            cla.notifyDataSetChanged();
            label_reset = true;
            showMsgDefault("");        }

        //-----------------------------------SELECT NEWER APKS-------------------------------------
        if(id == R.id.menuitem_duplicate_time_new) {
            if (cla != null & apkFilesList != null){
                SelectNewerApks(apkFilesList);
            }
            cla.notifyDataSetChanged();
            label_reset = true;
            showMsgDefault("");        }

        //-----------------------------------SELECT OLDER APKS-------------------------------------
        if(id == R.id.menuitem_duplicate_time_old) {
            if (cla != null & apkFilesList != null){
                SelectOlderApks(apkFilesList);
            }
            cla.notifyDataSetChanged();
            label_reset = true;
            showMsgDefault(""); }

        return true;
    }

//########################################################################################################################

    private void SelectOldVersionsApks(List<ObjectApkFile> list){
        Comparator<ObjectApkFile> duplicate_file_name_finder = (o1, o2) -> {
            if(o1.pkg_name.equals(o2.pkg_name))
            {
                if(o1.apk_pkg_info.versionCode < o2.apk_pkg_info.versionCode){
                    o1.select_box_state=true;
                    o2.select_box_state=false;
                }else if(o1.apk_pkg_info.versionCode > o2.apk_pkg_info.versionCode){
                    o1.select_box_state=false;
                    o2.select_box_state=true;
                }else {
                    if(o1.file_creation_time < o2.file_creation_time){
                        o1.select_box_state=true;
                        o2.select_box_state=false;
                    }else{
                        o2.select_box_state=true;
                        o1.select_box_state=false;
                    }
                }
            }

            return 0;
        };
        Collections.sort(apkFilesList,duplicate_file_name_finder);
        label_reset = true;
        showMsgDefault("");    }

    private void SelectNewVersionsApks(List<ObjectApkFile> list){
        Comparator<ObjectApkFile> duplicate_file_name_finder = (o1, o2) -> {
            if(o1.pkg_name.equals(o2.pkg_name)){
                if(o1.apk_pkg_info.versionCode > o2.apk_pkg_info.versionCode){
                    o1.select_box_state=true;
                    o2.select_box_state=false;
                }else if(o1.apk_pkg_info.versionCode < o2.apk_pkg_info.versionCode){
                    o1.select_box_state=false;
                    o2.select_box_state=true;
                }else {
                    if(o1.file_creation_time < o2.file_creation_time){
                        o1.select_box_state=true;
                        o2.select_box_state=false;
                    }else{
                        o2.select_box_state=true;
                        o1.select_box_state=false;
                    }
                }
            }
            return 0;
        };
        Collections.sort(apkFilesList,duplicate_file_name_finder);
        label_reset = true;
        showMsgDefault("");
    }

    private void SelectSmallerSizeApks(List<ObjectApkFile> list){
        Comparator<ObjectApkFile> duplicate_file_name_finder = (o1, o2) -> {
            if(o1.pkg_name.equals(o2.pkg_name)){
                if(o1.file.length() < o2.file.length()){
                    o1.select_box_state=true;
                    o2.select_box_state=false;
                }
            }
            return 0;
        };
        Collections.sort(apkFilesList,duplicate_file_name_finder);
        label_reset = true;
        showMsgDefault("");    }

    private void SelectBiggerSizeApks(List<ObjectApkFile> list){
        Comparator<ObjectApkFile> duplicate_file_name_finder = (o1, o2) -> {
            if(o1.pkg_name.equals(o2.pkg_name)){
                if(o1.file.length() > o2.file.length()){
                    o1.select_box_state=true;
                    o2.select_box_state=false;
                }
            }
            return 0;
        };
        Collections.sort(apkFilesList,duplicate_file_name_finder);
        label_reset = true;
        showMsgDefault("");    }

    private void SelectNewerApks(List<ObjectApkFile> list){
        Comparator<ObjectApkFile> duplicate_file_name_finder = (o1, o2) -> {
            if(o1.pkg_name.equals(o2.pkg_name)){
                if(o1.file_creation_time > o2.file_creation_time){
                    o1.select_box_state=true;
                    o2.select_box_state=false;
                }
            }
            return 0;
        };
        Collections.sort(list,duplicate_file_name_finder);
        showMsgDefault("");    }

    private void SelectOlderApks(List<ObjectApkFile> list){
        Comparator<ObjectApkFile> duplicate_file_name_finder = (o1, o2) -> {
            if(o1.pkg_name.equals(o2.pkg_name)){
                if(o1.file_creation_time < o2.file_creation_time){
                    o1.select_box_state=true;
                    o2.select_box_state=false;
                }
            }
            return 0;
        };
        Collections.sort(list,duplicate_file_name_finder);
        showMsgDefault("");
    }

    private void showMsg(String msg_type, String msg, boolean isempty){

        String log_only = "show_in_log_only";
        if(msg_type.equals(toast_msg)){
            Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
            Log.v(TAG, msg);
        }
        else if(msg_type.equals(textview_msg)) {
            showMsgDefault(msg);
            Log.v(TAG, msg);
        }else if(msg_type.equals(log_only)){
            Log.i(TAG, msg);
        }
    }

    private void showMsgDefault(String str) {
        if(str.isEmpty()) {
            String default_string = "Total : " + apkFilesList.size() + "\t Selected : " + cla.getSelectedItemsList().size();
            msg_textview.setText(default_string);
        }else{
            msg_textview.setText(str);
        }
    }

//########################################################################################################################

    class ApkAsyncTasks extends AsyncTask<Object, Object, String> {

       final String command;

        ApkAsyncTasks(String command) {
            super();
            this.command=command;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i(TAG, "in OnPreExecute :");
        }


        @Override
        protected String doInBackground(Object... objects) {
            Log.i(TAG, "in DoInBackground :");

            if (command.equals(SEARCH_APKS)) {
                searchApks((File) objects[0]);
                SortApkList(apkFilesList);
                return "Searching completed.";
            } else if (command.equals(ROOT_INSTALL)) {
                RootInstall((List) objects[0]);
                return "Root Installation Completed.";
            } else if (command.equals(NO_ROOT_INSTALL)) {
                NoRootInstall((List) objects[0]);
                return "Installation Completed.";
            }else if (command.equals(NO_ROOT_UNINSTALL)) {
                NoRootUninstall((List) objects[0]);
                return "Root Uninstall Operation completed.";
            }else if (command.equals(ROOT_UNINSTALL)) {
                RootUninstall((List) objects[0]);
                return "Root Uninstall Operation completed.";
            } else if (command.equals(ROOT_DELETE)) {
                RootDeleteApks((List) objects[0]);
                return "Apk Deletion Operation completed.";
            } else if (command.equals(NO_ROOT_DELETE)) {
                NoRootDeleteApks((List) objects[0]);
                return "Apk Deletion Operation completed.";
            }else if (command.equals(RENAME_APKS)) {
                RenameApks((List) objects[0]);
                SortApkList(apkFilesList);
                return "Renaming completed.";
            }else if (command.equals(ROOT_AUTOMOVE_TO_REPO)) {
                RootAutoMoveApks((List) objects[0]);
                return "Renaming completed.";
            } else if (command.equals(NO_ROOT_AUTOMOVE_TO_REPO)) {
                NoRootAutoMoveApks((List) objects[0]);
                return "Renaming completed.";
            }else if (command.equals(BUILD_REPO)) {
                makeAllApkParentDirList((File)objects[0]);
            }else if(command.equals(ROOT_MOVE_MANUALLY)){
                RootManualMoveApks((List) objects[0],(File) objects[1]);
            }else if(command.equals(ROOT_COPY_MANUALLY)){
                RootManualCopyApks((List) objects[0],(File) objects[1]);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.i(TAG, "in OnPostExecute :");

           cla = new AdapterApkList(apkFilesList, getContext());
           recyclerView.setAdapter(cla);
           cla.notifyDataSetChanged();
        }

        String str0 ;
        String str1 ;

        @Override
        protected void onProgressUpdate(Object... objects) {
            super.onProgressUpdate(objects);

            str0 = ((String) objects[0]);
            str1 = ((String) objects[1]);

            if(str0.equals(toast_msg)){
                Toast.makeText(getContext(),str1, Toast.LENGTH_LONG).show();
            }
            else if(str0.equals(textview_msg)) {
                if(str1.equals("")){
                    showMsgDefault("");
                }else{
                    showMsgDefault(str1);
                }
            }

            cla.notifyDataSetChanged();

            Log.i(TAG, ((String) objects[1]));
        }

        void searchApks(File dir_path) {

            List<File> list_files = new ArrayList<>();
            apkFilesList = new ArrayList<>();

            //CHECK FOR CORRECT PATH
            if (dir_path.exists() & dir_path.isDirectory()) {
                // CHECK IF TO SEARCH IN SUBDIRECTORY
                publishProgress(toast_msg,"Path is set to : " + dir_path.getAbsolutePath());
                if (ActivityMain.sharedPrefApkManager.getBoolean(key_search_subfolders, true)) {
                    apkFilesList = getAllSubDirFiles(dir_path);
                    Log.i(TAG, apkFilesList.size() + " files found. Search in SubDirectory : ON");
                }
                // CHECK IF TO NOT SEARCH IN SUBDIRECTORY
                else if (!ActivityMain.sharedPrefApkManager.getBoolean(key_search_subfolders, true)) {
                    apkFilesList = getDirFiles(dir_path);
                    Log.i(TAG, apkFilesList.size() + " files found.Search in SubDirectory : OFF");
                }
            } else {              // IF INVALID FOLDER PATH
                publishProgress(textview_msg,"Select valid path.",false);
            }

            publishProgress(textview_msg," Total apk files found : "+apkFilesList.size(),false);
        }

        final List<ObjectApkFile> apklist = new ArrayList<>();
        final String path_not_set = "PATH NOT SET";
        int count_apkfilesList=0;

        List getAllSubDirFiles(File file1) {
            //  Log.i(TAG,"getAllSubDirFiles: "+source_apk.getNamePartsFromApkItem());
            ObjectApkFile ldm;
            if(file1.listFiles()!=null){
                for(File f1 : file1.listFiles()) {
                    //  Log.i(TAG,"File : "+source_apk.getNamePartsFromApkItem());
                    if (f1.isFile() & f1.getName().endsWith(".apk")) {
                        ldm = new ObjectApkFile(f1, getContext());
                        if (ldm.apk_pkg_info != null) {
                            apklist.add(ldm);
                            count_apkfilesList++;
                            publishProgress(textview_msg,count_apkfilesList+" - Adding "+ldm.file_name,false);
                        }
                        //Log.i(TAG,"File : "+source_apk.getNamePartsFromApkItem());
                    }else if (f1.isDirectory()) {
                        getAllSubDirFiles(f1);
                    }
                }
            }
            return apklist;
        }

        List getDirFiles(File file1) {
            count_apkfilesList = 0;
            List<ObjectApkFile> apklist = new ArrayList<>();
            for (File f1 : file1.listFiles()) {
                if (f1.isFile() & f1.getName().endsWith(".apk")) {
                    ObjectApkFile ldm = new ObjectApkFile(f1, getContext());
                    apklist.add(ldm);
                    count_apkfilesList++;
                    publishProgress(textview_msg,count_apkfilesList+" - Adding "+ldm.file_name,false);
                }
            }
            label_reset =true;
            return apklist;
        }

        //--------------------------ROOT INSTALL---------------------------------------
        void RootInstall(List<ObjectApkFile> files_list) {
            long apk_size;
            String command="";
            int count_success=0;
            int count_failed=0;
            int i=0;
            publishProgress(toast_msg,"No. of files to install : " + selected_files_list.size());
            ObjectOperation objop = new ObjectOperation(context, 126, " Installing Apps (Root) ", files_list.size());
            ActivityOperations.operationsHashMapList.put(126, objop);
            for(i=1;i<=files_list.size();i++) {
                ObjectApkFile file = files_list.get(i-1);
                try{
                    apk_size = file.file.length();
                    command = "cat \"" + file.file.getPath() + "\"|pm install -S " + apk_size;
                    objop.showNotification(i+"/"+files_list.size()+ " Installing "+file.app_name,i,(i*100)/files_list.size());
                    //  Log.i(TAG, "COMMAND :"+ command);
                    Shell.su(command).exec();
                    count_success++;
                    file.select_box_state=false;
                    publishProgress(textview_msg,count_success+" Installed "+file.file_name+" successfully.",false);
                }catch (Exception ex) {
                    count_failed++;
                    publishProgress(textview_msg,count_failed+" : "+file.file_name+" installation failed.",false);
                    Log.e(TAG, " ROOT INSTALL ERROR of " + file.file_name + " \nError : " + ex);
                }
            }
            i--;
            objop.cancelNotification();
            publishProgress(textview_msg,"Total Files Selected "+files_list.size()+
                    "\nInstalled Successfully : "+count_success+
                    "\tInstallation Failed : "+count_failed
                    ,false);
        }

        //--------------------------NO ROOT INSTALL---------------------------------------
        void NoRootInstall(List<ObjectApkFile> files_list) {

            int count_sucess=0;

            Intent i = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            i.setType("application/vnd.android.package-archive");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            ObjectOperation objop = new ObjectOperation(context, 127, " Installing Apps (No Root) ", files_list.size());
            ActivityOperations.operationsHashMapList.put(127, objop);
            Log.i(TAG, "Root Access " + rootAccess);
             int k =0;
            for(k=1;k<= files_list.size();k++)
            {
                ObjectApkFile file = files_list.get(k-1);
                objop.showNotification(k+"/"+files_list.size()+ " Installing "+file.app_name,k,(k*100)/files_list.size());
                Uri uri = FileProvider.getUriForFile(getActivity(),
                        BuildConfig.APPLICATION_ID + ".provider",
                        file.file);
                i.setData(uri);
                getContext().startActivity(i);
                publishProgress(textview_msg,count_sucess+" Renaming : \"" + file.file_name +false);
//                file.select_box_state = false;
            }
            objop.cancelNotification();

        }

        //--------------------------ROOT UNINSTALL---------------------------------------
        void RootUninstall(List<ObjectApkFile> files_list) {
            String command;
            List<String> std_err = new ArrayList<>();
            List<String> std_out = new ArrayList<>();
            int count_success=0;
            int count_failed=0;
            int count_not_installed=0;

            Log.i(TAG, "Root Access " + rootAccess);
            Log.i(TAG, " No. of files to UNINSTALL  " + selected_files_list.size());
            publishProgress(toast_msg,"No. of files to uninstall : " + selected_files_list.size());
            ObjectOperation objop = new ObjectOperation(context, 131, " Uninstalling Apps (Root) ", files_list.size());
            ActivityOperations.operationsHashMapList.put(131, objop);
            int k=0;
            for( k=1;k<files_list.size();k++)
            {
                ObjectApkFile file = files_list.get(k-1);
                objop.showNotification(k+"/"+files_list.size()+ " Uninstalling "+file.app_name,k,(k*100)/files_list.size());
                if (file.isInstalled) {
                    try {
                        command = "pm uninstall " + file.pkg_name;
                        Shell.su(command).to(std_out, std_err).exec();
                        count_success++;
                        Log.i(TAG, " ROOT Uninstalling " + file.file_name + " : " + std_out.get(0));
                        file.select_box_state=false;
                        publishProgress(textview_msg,"File "+count_success+" Installed "+file.file_name+" successfully.",false);
                    }catch(Exception ex) {
                        count_failed++;
                        Log.e(TAG, " ROOT INSTALL ERROR of " + file.file_name + " \nError : " + ex);
                        publishProgress(textview_msg,"File "+count_failed+" Installed "+file.file_name+" successfully.",false);
                    }
                }else {
                    count_not_installed++;
                }
            }
            objop.cancelNotification();
            publishProgress(textview_msg,"Total Selected "+files_list.size()+
                            "\t Not installed : "+count_not_installed+
                            "\nUninstallation Successfull : "+count_success+
                            "\tUninstallation Failed : "+count_failed
                            ,false);
        }

        void NoRootUninstall(List<ObjectApkFile> files_list) {

            int count_success=0;
            int count_failed=0;
            int count_not_installed=0;

            Log.i(TAG," Uninstall (No Root)");
            ObjectOperation objop = new ObjectOperation(context, 129, " Uninstalling Apps (Non Root) ", files_list.size());
            ActivityOperations.operationsHashMapList.put(129, objop);
            publishProgress(textview_msg,"NO_ROOT Uninstall."+"\tTotal Selected to Uninstall "+selected_files_list.size());
            int k=0;
            for( k=1;k<files_list.size();k++)
            {
                ObjectApkFile file = files_list.get(k-1);
                objop.showNotification(k+"/"+files_list.size()+ " Uninstalling "+file.app_name,k,(k*100)/files_list.size());
                if (file.isInstalled) {
                    publishProgress(textview_msg,count++ +" : Uninstalling " + file.app_name+". Select OK");
                    Intent intent = new Intent("android.intent.action.DELETE");
                    intent.setData(Uri.parse("package:" + file.pkg_name));
                    startActivity(intent);
                    publishProgress(textview_msg, (count_success++) + " : Uninstalled " + file.app_name);
                  //  file.select_box_state=false;
                }
                else{
                    publishProgress(textview_msg, (count_not_installed++)+" : Apk Not Installed " + file.app_name);
                }
            }
            objop.cancelNotification();
            publishProgress(textview_msg,"Total Selected "+files_list.size()+
                            "\t Not installed : "+count_not_installed+
                            "\nUninstallation Successfull : "+count_success+
                            "\tUninstallation Failed : "+count_failed,false); }

        void RootAutoMoveApks(List<ObjectApkFile> list) {
            String value_parent_folder;
            int count_moved = 0;
            int count_not_moved = 0;
            int count_cannot_move = 0;

            boolean status;
            ObjectOperation objop = new ObjectOperation(context, 137, " Auto Moving Apps (Root) ", list.size());
            ActivityOperations.operationsHashMapList.put(137, objop);
            int k=0;
            for( k=1;k<list.size();k++)
            {
                ObjectApkFile aldi = list.get(k-1);
                objop.showNotification(k+"/"+list.size()+ " Uninstalling "+aldi.app_name,k,(k*100)/list.size());
                value_parent_folder = sharedPrefRepository.getString(aldi.pkg_name, path_not_set);
                
                if (value_parent_folder.equals(path_not_set)) {
                    count_cannot_move++;
                } else {
                    String command = "mv \""+aldi.file.getAbsolutePath()+"\" \""+value_parent_folder + "/" + aldi.file_name+"\"";
                    try{
                        Shell.sh(command).exec();
                        publishProgress(textview_msg,"Moved " + aldi.file_name + " to " + value_parent_folder+" Successfully",false);
                      //apkFilesList.remove(aldi);
                        count_moved++;
                        apkFilesList.remove(aldi);
                    }catch(Exception ex){
                        Log.i(TAG,"Error Moving : "+ex);
                        publishProgress(textview_msg,"Failed Moving " + aldi.file_name + " to " + value_parent_folder,false);
                        count_not_moved++;
                    }
                }
            }
            objop.cancelNotification();
            publishProgress(textview_msg,"MOVING SUCCESSFUL :" + count_moved +
                                    "\nMOVING FAILED :" + count_not_moved+
                                    "\tCANNOT MOVE :" + count_cannot_move,false);
        }

        void NoRootAutoMoveApks(List<ObjectApkFile> list) {
            String value_parent_folder;
            int count_moved = 0;
            int count_not_moved = 0;
            int count_cannot_move = 0;
            boolean status;
            ObjectOperation objop = new ObjectOperation(context, 139, "Auto Moving Apps (No Root) ", list.size());
            ActivityOperations.operationsHashMapList.put(139, objop);
            int k=0;
            for( k=1;k<list.size();k++)
            {
                ObjectApkFile aldi = list.get(k-1);
                objop.showNotification(k+"/"+list.size()+ " Moving "+aldi.app_name,k,(k*100)/list.size());
                value_parent_folder = sharedPrefRepository.getString(aldi.pkg_name, path_not_set);

                if (value_parent_folder.equals(path_not_set)) {
                    count_cannot_move++;
                } else {
                    status = copyFileTo(new File(aldi.file.getAbsolutePath()), new File(value_parent_folder + "/" + aldi.file_name));
                    publishProgress(textview_msg," Moving status of:" + aldi.file_name +" : "+status,false);
                }
            }
            objop.cancelNotification();
            publishProgress(textview_msg,"MOVING SUCCESSFUL :" + count_moved +
                    "\nMOVING FAILED :" + count_not_moved+
                    "\tCANNOT MOVE :" + count_cannot_move,false);
        }

        void RootManualMoveApks(List<ObjectApkFile> list, File target) {
            int count_moved = 0;
            int count_not_moved = 0;

            if(target.exists() & target.isDirectory()){
                ObjectOperation objop = new ObjectOperation(context, 141, "Moving Apps (Root) ", list.size());
                ActivityOperations.operationsHashMapList.put(141, objop);
                int k=0;
                for( k=1;k<list.size();k++)
                {
                    ObjectApkFile aldi = list.get(k-1);
                    objop.showNotification(k+"/"+list.size()+ " Moving "+aldi.app_name,k,(k*100)/list.size());
                    String command = "mv \""+aldi.file.getAbsolutePath()+"\" \""+target.getAbsolutePath()+ "\"";
                    try{
                        Shell.sh(command).exec();
                        publishProgress(textview_msg,"Moved " + aldi.file_name + " to " + target.getAbsolutePath()+" successfully",false);
                        //apkFilesList.remove(aldi);
                        count_moved++;
                    }catch(Exception ex){
                        Log.i(TAG,"Error Moving : "+ex);
                        publishProgress(textview_msg,"Failed Moving " + aldi.file_name + " to " + target.getAbsolutePath(),false);
                        count_not_moved++;
                    }
                }
                objop.cancelNotification();
            }

            publishProgress(textview_msg,"MOVING SUCCESSFUL :" + count_moved +
                                                "\nMOVING FAILED :" + count_not_moved);
        }

        void RootManualCopyApks(List<ObjectApkFile> list, File target) {
            int count_moved = 0;
            int count_not_moved = 0;

            if(target.exists() & target.isDirectory()){
                ObjectOperation objop = new ObjectOperation(context, 141, "Copying Apks (Root) ", list.size());
                ActivityOperations.operationsHashMapList.put(141, objop);
                int k=0;
                for( k=1;k<list.size();k++)
                {
                    ObjectApkFile aldi = list.get(k-1);
                    objop.showNotification(k+"/"+list.size()+ " Copying "+aldi.app_name,k,(k*100)/list.size());
                    String command = "cp \""+aldi.file.getAbsolutePath()+"\" \""+target.getAbsolutePath()+ "\"";
                    try{
                        Shell.sh(command).exec();
                        publishProgress(textview_msg,"Copied " + aldi.file_name + " to " + target.getAbsolutePath()+" successfully",false);
                        //apkFilesList.remove(aldi);
                        count_moved++;
                    }catch(Exception ex){
                        Log.i(TAG,"Error Moving : "+ex);
                        publishProgress(textview_msg,"Failed Copying " + aldi.file_name + " to " + target.getAbsolutePath(),false);
                        count_not_moved++;
                    }
                }
                objop.cancelNotification();
            }
            publishProgress(textview_msg,"MOVING SUCCESSFUL :" + count_moved +
                    "\nMOVING FAILED :" + count_not_moved);
        }

        ObjectApkFile ldm;
        int count = 0;

        void makeAllApkParentDirList(File file1) {
            //Log.i(TAG,"getAllSubDirFiles: "+source_apk.getNamePartsFromApkItem());
            if (file1.exists() & file1.isDirectory()) {
                for (File f1 : file1.listFiles()) {
                    if (f1.isFile() & f1.getName().endsWith(".apk"))
                    {  //Log.i(TAG,"File : "+source_apk.getNamePartsFromApkItem());
                        ldm = new ObjectApkFile(f1, getContext());
                        if (ldm.apk_pkg_info != null)
                        {
                            prefEditRepository.putString(ldm.pkg_name, ldm.file.getParent()).commit();
                            count++;
                            publishProgress(textview_msg,count+" : "+ldm.file_name+" \nPATH: "+ldm.file.getParent(),false);
                        }
                    } else if (f1.isDirectory()) {
                        makeAllApkParentDirList(f1);
                    }
                }
                Log.i(TAG, "APKs Found in Repository: " + count);
            }
            else {
                publishProgress(toast_msg,"Set Proper Target/Moveto Folder Path in App Settings",false);
            }

            publishProgress(textview_msg,"Apks in Repository Count : " + count,false);
        }

        boolean copyFileTo(File src, File dest){

            boolean result ;
            FileChannel src_channel = null;
            FileChannel dest_channel = null;

            try{
                if(!dest.exists()) {
                    dest.createNewFile();
                }
                dest_channel = new FileOutputStream(dest).getChannel();
                Log.e(TAG,"Out Stream Opened Succesfully : ");
            }catch(Exception ex){
                Log.e(TAG,"Error opening out stream : "+ex);
            }

            try{
                src_channel = new FileInputStream(src).getChannel();
                Log.e(TAG,"Input Stream Opened Succesfully : ");
            }catch(Exception ex){
                Log.e(TAG,"Error opening input stream : "+ex);
            }


            try{
                dest_channel.transferFrom(src_channel,0,src_channel.size());
                Log.i(TAG,"Apk Extracted Succesfully.");
                result=true;
            }catch(Exception ex) {
                Log.e(TAG, "Error Extracting Apk file : " + ex);
                result=false;
            }finally {
                try {
                    if(null!=src_channel){
                        src_channel.close();
                    }
                    if(null!=dest_channel){
                        dest_channel.close();
                    }
                    Log.i(TAG,"Streams Closed Succesfully.");
                } catch (IOException ex) {
                    Log.e(TAG, "Error Closing Streams : " + ex);
                }
            }

            return result;
        }

        void NoRootDeleteApks(List<ObjectApkFile> files_list){
            boolean isDeleted;
            int count_deleted=0;
            String command;

            for (ObjectApkFile file : files_list) {
                isDeleted = file.file.delete();
                apkFilesList.remove(file);
                if(isDeleted){
                    count_deleted++;
                }
                Log.i(TAG, "Deletion of " + file.file_name + " : " + isDeleted);
                publishProgress(textview_msg,"Deletion of " + file.file_name + " : " + isDeleted,false);
            }

            publishProgress(textview_msg,"Total Files  : "+files_list.size()+"\nDelteted Succesfully "+count_deleted ,false);

        }

        void RootDeleteApks(List<ObjectApkFile> files_list) {
            boolean isDeleted;
            int count_deleted=0;
            String command;
            for (ObjectApkFile file : files_list) {
                try{
                    command = "rm -f \""+file.file.getAbsolutePath()+"\"";
                    Shell.sh(command).exec();
                    count_deleted++;
                    publishProgress("label", "Deleted Successfully " + file.file_name + " : ",false );
                    apkFilesList.remove(file);
                }catch(Exception ex){
                    Log.i(TAG, "Deletion Failed " + file.file_name + " Error : "+ex );
                }
            }
            publishProgress(textview_msg,"Total Files to Delete "+files_list.size()+"\tDeleted Successfully "+count_deleted,false);
        }

        void RenameApks(List<ObjectApkFile> files_list) {

            Log.i(TAG, "Renaming Files Count :" + files_list.size());

            int count_rename_passed=0;
            int count_rename_failed=0;

            for(ObjectApkFile f : files_list)
            {
                ClassApkOperation classApkOperationObject = new ClassApkOperation(f,getContext());
                classApkOperationObject.RenameThisApk();
                if(classApkOperationObject.result){
                    count_rename_passed++;
                    apkFilesList.remove(f);
                    apkFilesList.add(new ObjectApkFile(classApkOperationObject.dest_file,context));
                    publishProgress(textview_msg, count_rename_passed+" : "+ classApkOperationObject.apk_formatted_name+" renamed successfully.");
                }else if(!false){
                    count_rename_failed++;
                    publishProgress(textview_msg, classApkOperationObject.apk_formatted_name+" renaming failed.");
                }
            }

            searchApks(new File(ActivityMain.sharedPrefApkManager.getString(key_local_path,path_not_set)));

            publishProgress(textview_msg,"Total Selected to Rename : "
                    +files_list.size()+"\nSuccessfully Renamed : " + count_rename_passed
                    +"\nFailed Renamed for : " + count_rename_failed,false);
        }

    }

//########################################################################################################################

    private void SortApkList(List to_sort_list) {
        Comparator<ObjectApkFile> file_name_comparator = (ObjectApkFile l1, ObjectApkFile l2) -> l1.file_name.compareTo(l2.file_name);
        Comparator<ObjectApkFile> file_size_comparator = (ObjectApkFile l1, ObjectApkFile l2) -> Long.compare(l1.file.length(), l2.file.length());
        Comparator<ObjectApkFile> modified_date_comparator = (ObjectApkFile l1, ObjectApkFile l2) -> Long.compare(l1.file.lastModified(), l2.file.lastModified());
        Comparator<ObjectApkFile> creation_date_comparator = (ObjectApkFile l1, ObjectApkFile l2) -> Long.compare(l1.file_creation_time, l2.file_creation_time);

        Log.i(TAG, " In SortRootFileList : sort by = " + sort_apks_by + " ApkFilesList Count: " + apkFilesList.size());

        switch (sort_apks_by) {
            case sort_apks_by_name: {
                Collections.sort(to_sort_list, file_name_comparator);
                if (order_apks_by.equals(order_apks_decreasing)) {
                    Collections.reverse(apkFilesList);
                }
                Log.i(TAG, "in sort_apks_by_name");
                break;
            }

            case sort_apks_by_date: {
                Collections.sort(to_sort_list, creation_date_comparator);
                if (order_apks_by.equals(order_apks_decreasing)) {
                    Collections.reverse(apkFilesList);
                }
                Log.i(TAG, "in sort_apks_by_date");
                break;
            }

            case sort_apks_by_size: {
                Collections.sort(to_sort_list, file_size_comparator);
                if (order_apks_by.equals(order_apks_decreasing)) {
                    Collections.reverse(apkFilesList);
                }
                Log.i(TAG, "in sort_apks_by_size");
                break;
            }

            default: {
                Collections.sort(to_sort_list, file_name_comparator);
                break;
            }
        }

        apkFilesList = to_sort_list;
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(getContext(), "Write External Storage permission allows us to save files. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e("value", "Permission Granted, Now you can use local drive .");
            } else {
                Log.e("value", "Permission Denied, You cannot use local drive .");
            }
        }
    }
}

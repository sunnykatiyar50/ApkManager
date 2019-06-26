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
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static android.content.Intent.EXTRA_PACKAGE_NAME;
import static com.sunnykatiyar.AppManager.AppListActivity.prefEditorApkManager;
import static com.sunnykatiyar.AppManager.AppListActivity.prefEditRepository;
import static com.sunnykatiyar.AppManager.AppListActivity.sharedPrefAppSettings;
import static com.sunnykatiyar.AppManager.AppListActivity.sharedPrefRepository;

public class ApkListFragment extends Fragment {
    private static final int PERMISSION_REQUEST_CODE = 100;

    private static final String TAG = "MAIN ACTIVITY : ";
    Button btn_local_path;
    public static Menu option_menu;
    public static TextView text_msgs;

    Context context = getContext();

    EditText value_local_path;
    List<File> list_files;
    public static CustomApkListAdapter cla;
    List<ApkListDataItem> apkFilesList;
    RecyclerView recyclerView;
    public static PackageManager pm;
    public static List<ApkListDataItem> selected_files_list = new ArrayList<>();
    private boolean rootSelected;
    private boolean rootAccess;
    String value_global_path;
    final static String key_search_subfolders = "SEARCH_SUBFOLDERS";

    DividerItemDecoration mDividerItemDecoration;
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

    final String sort_apks_by_name = "SORT_BY_NAME";
    final String sort_apks_by_date = "SORT_BY_DATE";
    final String sort_apks_by_size = "SORT_BY_SIZE";

    final String order_apks_decreasing = "ORDER_INCREASING";
    final String order_apks_increasing = "ORDER_DECREASING";

    final String path_not_set = "PATH NOT SET";
    public String sort_apks_by;
    public String order_apks_by;
    final String name_format_data_saved = RenameApkFragment.name_format_data_saved;
    boolean label_reset = false;

    final String searchApks = "SEARCH_APKS";
    final String rename_apks = "RENAME_APKS";
    final String delete_apks= "DELETE_APKS";
    final String move_apks_to_repo = "MOVE_TO_REPO_APKS";
    final String install_apps = "SEARCH_APKS";
    final String uninstall_apps = "SEARCH_APKS";

    final String key_root_access = AppSettingsFragment.key_root_access;
    final String key_repository_folder = AppSettingsFragment.key_repository_folder;

    static {
        Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR);
        Shell.Config.verboseLogging(BuildConfig.DEBUG);
        //Shell.Config.setTimeout(10);
    }

    public ApkListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_apk_list, container, false);

        recyclerView = v.findViewById(R.id.r_view);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);

        mDividerItemDecoration = new DividerItemDecoration(getContext(), llm.getOrientation());
        recyclerView.addItemDecoration(mDividerItemDecoration);

        pm = getContext().getPackageManager();

        btn_local_path = v.findViewById(R.id.btn_browse_local_path);
        value_local_path = v.findViewById(R.id.edit_search_folder);

        value_global_path = AppListActivity.sharedPrefApkManager.getString(key_global_path, "Path Not Set");
        value_local_path.setText(value_global_path);

        File dir_path = new File(value_local_path.getText().toString());

        if (dir_path.exists() & dir_path.isDirectory()) {
            sort_apks_by = AppListActivity.sharedPrefApkManager.getString(value_sorting, sort_apks_by_name);
            new LongTask().execute("search", dir_path);
        } else {
            Toast.makeText(getContext(), "Set a Valid Folder To Load Files.", Toast.LENGTH_SHORT);
        }

        text_msgs = v.findViewById(R.id.text_msgs);
        cla = new CustomApkListAdapter(apkFilesList, getContext());
        recyclerView.setAdapter(cla);
        cla.notifyDataSetChanged();

        btn_local_path.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(getContext(), FilePickerActivity.class);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);

                File f = new File(value_local_path.toString());

                if (f.exists() & f.isFile()) {
                    i.putExtra(FilePickerActivity.EXTRA_START_PATH, f.getParent());
                } else if (f.exists() & f.isDirectory()) {
                    i.putExtra(FilePickerActivity.EXTRA_START_PATH, f.getAbsoluteFile());
                } else {
                    i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());
                }

                startActivityForResult(i, 2);
            }
        });

        setHasOptionsMenu(true);

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case 2:
                if (resultCode == RESULT_OK) {
                    //Use the provided utility method to parse the result
                    List<Uri> files = Utils.getSelectedFilesFromResult(data);
                    File file = Utils.getFileForUri(files.get(0));
                    value_local_path.setText(file.getPath());
                 //   btn_local_path.setEnabled(true);
                    text_msgs.setText("You Should Change RENAME Settings");
                }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu1, MenuInflater menuInflater) {
        Log.i(TAG, " onCreateOptionsMenu : ");

        menuInflater.inflate(R.menu.menu_apk_list, menu1);
        this.option_menu = menu1;
        super.onCreateOptionsMenu(menu1, menuInflater);

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        Log.i(TAG, " onPrepareOptionsMenu : ");

//-----------------------LOADING "SORT BY" FROM SHARED PREFERENCES---------------------------------------
        value_sorting = AppListActivity.sharedPrefApkManager.getString(key_sorting, sort_apks_by_name);
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

        value_order_by = AppListActivity.sharedPrefApkManager.getString(key_order_by, order_apks_increasing);
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
        option_menu.findItem(R.id.menuitem_root).setChecked(AppListActivity.sharedPrefApkManager.getBoolean("ROOT", true));
        rootSelected = option_menu.findItem(R.id.menuitem_root).isChecked();
//--------------------------------------------------------------------------------------------------------------------
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        Log.i(TAG, " onOptionsItemSelected : " + item.getTitle() + " AND  isItemChecked()=" + item.isChecked());

        //--------------------------------SEARCH-----------------------------------------
        if (id == R.id.menuitem_search) {

            File dir_path = new File(value_local_path.getText().toString());
            new LongTask().execute("search", dir_path);
            cla = new CustomApkListAdapter(apkFilesList, getContext());
            recyclerView.setAdapter(cla);
            cla.notifyDataSetChanged();
        }

        //-----------------------RELOAD------------------------------------------------
        if (id == R.id.menuitem_reset_path) {
            if (AppListActivity.sharedPrefApkManager.contains(key_global_path)) {
                value_global_path = AppListActivity.sharedPrefApkManager.getString(key_global_path, "Path Not Set");
                value_local_path.setText(value_global_path);
            }
        }

        //-------------------------SEARCH_SUBFOLDERS-----------------------------------
        if (id == R.id.menuitem_subdir) {
            item.setChecked(!item.isChecked());
            prefEditorApkManager.putBoolean(key_search_subfolders, item.isChecked()).commit();
        }

        //--------------------------SELECT_ROOT----------------------------------------------
        if (id == R.id.menuitem_root) {
            if (item.isChecked()) {
                item.setChecked(false);
                //  rootAccess = Shell.rootAccess();
                Toast.makeText(getContext(), " ROOT Actions Disabled.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "ROOT SELECTED :" + rootSelected);
                Log.e(TAG, "ROOT ACCESS :" + rootAccess);
            } else if (!item.isChecked()) {
                item.setChecked(true);
                rootSelected = true;
//              rootAccess = Shell.rootAccess();
                Toast.makeText(getContext(), " ROOT Actions Enabled.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "ROOT SELECTED :" + rootSelected);
                Log.e(TAG, "ROOT ACCESS :" + rootAccess);
            }
            prefEditorApkManager.putBoolean("ROOT", item.isChecked());
            prefEditorApkManager.commit();
        }

        //------------------INSTALL/UPDATE APPS--------------------------------
        if (id == R.id.menuitem_install) {

            if (cla != null) {
                this.selected_files_list = cla.getSelectedItemsList();
                Toast.makeText(getContext(), " No. of selecetd files : " + cla.getSelectedItemsList().size(), Toast.LENGTH_LONG);
            }

            builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Confirm Installing " + selected_files_list.size() + " Files");
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
                                Toast.makeText(getContext(), "No ROOT ACCESS. ", Toast.LENGTH_SHORT).show();
                            }
                        } else if (!rootSelected) {
                            new LongTask().execute("norootinstall", selected_files_list);
                        }

                    } else {
                        Log.i(TAG, " No Items Selected.");
                        Toast.makeText(getContext(), "No Items Selected. ", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(getContext(), "Installation Cancelled.", Toast.LENGTH_SHORT).show();
                }
            });

            builder.show();
        }

        //-------------------PREPARE_REPOSITORY----------------------
        if (id == R.id.menuitem_make_repository) {
            File file1 = new File(sharedPrefAppSettings.getString(key_repository_folder, path_not_set));
            Log.i(TAG,"Found Repository PAth : "+sharedPrefAppSettings.getString(key_repository_folder, path_not_set));

            builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Confirm Refreshing Repository Apks.\n This May take some time. ");
            builder.setMessage("Click Yes to Continue...");
            builder.setCancelable(false);

            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(getContext(), "Rebuilding Repository Data.", Toast.LENGTH_SHORT).show();
                    new LongTask().execute("record_paths", file1);
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(getContext(), "Reloading Repository Cancelled.", Toast.LENGTH_SHORT).show();
                }
            });
            builder.show();
        }

        //-------------------MOVE FILES----------------------
        if (id == R.id.menuitem_move_files) {

            if (cla != null) {
                this.selected_files_list = cla.getSelectedItemsList();
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

                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getContext(), "Trying To Move Files.", Toast.LENGTH_SHORT).show();
                                if (selected_files_list != null & selected_files_list.size() > 0) {
                                    new LongTask().execute("move", selected_files_list);
                                } else {
                                    Log.i(TAG, " No Items Selected.");
                                    Toast.makeText(getContext(), "No Items Selected. ", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getContext(), "MOving Cancelled.", Toast.LENGTH_SHORT).show();
                            }
                        });

                        builder.show();
                    }else{
                        requestPermission(); // Code for permission
                    }
            }


        }

        //-------------------MOVE AND REPLACE FILES----------------------
//        if (id == R.id.menuitem_move_replace) {
//
//            if (cla != null) {
//                this.selected_files_list = cla.getSelectedItemsList();
//                Toast.makeText(getContext(), " No. of selecetd files : " + cla.getSelectedItemsList().size(), Toast.LENGTH_LONG);
//            }
//
//            String state = Environment.getExternalStorageState();
//            if (Environment.MEDIA_MOUNTED.equals(state)) {
//                if(checkPermission()) {
//                    String path = Environment.getExternalStorageDirectory().toString();
//                    builder = new AlertDialog.Builder(getContext());
//                    builder.setTitle("Confirm Moving " + selected_files_list.size() + " Files To Repository Folder");
//                    builder.setMessage("Click Yes to Continue...");
//                    builder.setCancelable(false);
//
//                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            Toast.makeText(getContext(), "Trying To Move Files.", Toast.LENGTH_SHORT).show();
//                            if (selected_files_list != null & selected_files_list.size() > 0) {
//                                new LongTask().execute("replace", selected_files_list);
//                            } else {
//                                Log.i(TAG, " No Items Selected.");
//                                Toast.makeText(getContext(), "No Items Selected. ", Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    });
//
//                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            Toast.makeText(getContext(), "MOving Cancelled.", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//
//                    builder.show();
//                }else{
//                    requestPermission(); // Code for permission
//                }
//            }
//
//
//        }

        //---------------------SELECT ALL----------------------
        if (id == R.id.menuitem_select_all) {

            item.setChecked(!item.isChecked());

            if (apkFilesList.size() > 0) {
                //-------------------Selecting each file--------------------
                for (ApkListDataItem l : apkFilesList) {
                    l.select_box_state = item.isChecked();
                }
            } else {
                Toast.makeText(getContext(), "First Load Files to Select", Toast.LENGTH_SHORT);
            }
            cla = new CustomApkListAdapter(apkFilesList, getContext());
            recyclerView.setAdapter(cla);
            cla.notifyDataSetChanged();
//
//            File dir_path = new File(value_local_path.getText().toString());
//            if (dir_path.exists() & dir_path.isDirectory()) {
//                new LongTask().execute("search", dir_path);
//            } else {
//                Toast.makeText(getContext(), "Set Correct Path", Toast.LENGTH_SHORT);
//            }
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
                                Toast.makeText(getContext(), "No ROOT ACCESS. ", Toast.LENGTH_SHORT).show();
                            }
                        } else if (!rootSelected) {
                            new LongTask().execute("norootuninstall", selected_files_list);
                        }

                    } else {
                        Log.i(TAG, " No Items Selected.");
                        Toast.makeText(getContext(), "No Items Selected. ", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getContext(), " No. of selecetd files : " + cla.getSelectedItemsList().size(), Toast.LENGTH_LONG);
            }

            builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Confirm Deleting " + selected_files_list.size() + " Files");
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
            // Toast.makeText(getContext(), "Its good to delete redundant stuffs ...\n \n... Just Be Sure", Toast.LENGTH_LONG).show();
        }

        //----------------------RENAME APKS-----------------------------------------------
        if (id == R.id.menuitem_rename) {

            if (cla != null) {
                this.selected_files_list = cla.getSelectedItemsList();
                //Toast.makeText(getContext(), " No. of selecetd files : " + cla.getSelectedItemCount(), Toast.LENGTH_LONG);
            }
            Toast.makeText(getContext(), " ", Toast.LENGTH_LONG).show();

            builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Confirm Renaming " + selected_files_list.size() + " Files");
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
        if (id == R.id.menuitem_sortbyname) {
            item.setChecked(true);
            sort_apks_by = sort_apks_by_name;
            prefEditorApkManager.putString(key_sorting, sort_apks_by).commit();
            SortApkList();
        }

        if (id == R.id.menuitem_sortbydate) {
            item.setChecked(true);
            sort_apks_by = sort_apks_by_date;
            prefEditorApkManager.putString(key_sorting, sort_apks_by).commit();
            SortApkList();
        }

        if (id == R.id.menuitem_sortbysize) {
            item.setChecked(true);
            //    Log.i(TAG,"Clicked sort by size");
            sort_apks_by = sort_apks_by_size;
            prefEditorApkManager.putString(key_sorting, sort_apks_by).commit();
            SortApkList();
        }

        //-----------------------------------INVERSE SORTING-------------------------------------
        if (id == R.id.menuitem_decreasing) {
            item.setChecked(true);
            order_apks_by = order_apks_decreasing;
            prefEditorApkManager.putString(key_order_by, order_apks_by).commit();
            SortApkList();
        }

        if (id == R.id.menuitem_increasing) {
            item.setChecked(true);
            order_apks_by = order_apks_increasing;
            prefEditorApkManager.putString(key_order_by, order_apks_by).commit();
            SortApkList();
        }

        //-----------------------------------SELECT UPDATED APKS-------------------------------------
        if (id == R.id.menuitem_select_update) {
            if (cla != null & apkFilesList != null) {
                cla.SelectUpdatable();
                cla.notifyDataSetChanged();
            }
            ShowMsgInTextView();

        }

        //-----------------------------------SELECT NEW VERSIONS-------------------------------------
        if (id == R.id.menuitem__duplicate_new_version) {
            if (cla != null & apkFilesList != null){
                SelectNewVersionsApks(apkFilesList);
            }
            cla.notifyDataSetChanged();
            ShowMsgInTextView();
        }
        //-----------------------------------SELECT OLD VERSIONS-------------------------------------
        if (id == R.id.menuitem__duplicate_old_version) {
            if (cla != null & apkFilesList != null){
                SelectOldVersionsApks(apkFilesList);
            }
            cla.notifyDataSetChanged();
            ShowMsgInTextView();
        }
        //-----------------------------------SELECT SMALLER APKS-------------------------------------
        if (id == R.id.menuitem_duplicate_size_smaller) {
            if (cla != null & apkFilesList != null){
                SelectSmallerSizeApks(apkFilesList);
            }
            cla.notifyDataSetChanged();
            ShowMsgInTextView();
        }
        //-----------------------------------SELECT BIGGER APKS-------------------------------------
        if (id == R.id.menuitem_duplicate_size_smaller) {
            if (cla != null & apkFilesList != null){
                SelectBiggerSizeApks(apkFilesList);
            }
            cla.notifyDataSetChanged();
            ShowMsgInTextView();
        }
        //-----------------------------------SELECT NEWER APKS-------------------------------------
        if(id == R.id.menuitem_duplicate_time_new) {
            if (cla != null & apkFilesList != null){
                SelectNewerApks(apkFilesList);
            }
            cla.notifyDataSetChanged();
            ShowMsgInTextView();
        }
        //-----------------------------------SELECT OLDER APKS-------------------------------------
        if(id == R.id.menuitem_duplicate_time_new) {
            if (cla != null & apkFilesList != null){
                SelectOlderApks(apkFilesList);
            }
            cla.notifyDataSetChanged();
            ShowMsgInTextView();
        }

        return true;
    }

    public void SelectOldVersionsApks(List<ApkListDataItem> list){
        Comparator<ApkListDataItem> duplicate_file_name_finder = new Comparator<ApkListDataItem>() {
            @Override
            public int compare(ApkListDataItem o1, ApkListDataItem o2) {
                if(o1.pkg_name == o2.pkg_name){
                    if(o1.apk_pkg_info.versionCode < o2.apk_pkg_info.versionCode){
                        o1.select_box_state=true;
                        o2.select_box_state=false;
                    }
                }

                return 0;
            }
        };
        Collections.sort(apkFilesList,duplicate_file_name_finder);
    }

    public void SelectNewVersionsApks(List<ApkListDataItem> list){
        Comparator<ApkListDataItem> duplicate_file_name_finder = new Comparator<ApkListDataItem>() {
            @Override
            public int compare(ApkListDataItem o1, ApkListDataItem o2) {
                if(o1.pkg_name == o2.pkg_name){
                    if(o1.apk_pkg_info.versionCode > o2.apk_pkg_info.versionCode){
                        o1.select_box_state=true;
                        o2.select_box_state=false;
                    }
                }
                return 0;
            }
        };
        Collections.sort(apkFilesList,duplicate_file_name_finder);

    }

    public void SelectSmallerSizeApks(List<ApkListDataItem> list){
        Comparator<ApkListDataItem> duplicate_file_name_finder = new Comparator<ApkListDataItem>() {
            @Override
            public int compare(ApkListDataItem o1, ApkListDataItem o2) {
                if(o1.pkg_name == o2.pkg_name){
                    if(o1.file.length() < o2.file.length()){
                        o1.select_box_state=true;
                        o2.select_box_state=false;
                    }
                }
                return 0;
            }
        };
        Collections.sort(apkFilesList,duplicate_file_name_finder);

    }

    public void SelectBiggerSizeApks(List<ApkListDataItem> list){
        Comparator<ApkListDataItem> duplicate_file_name_finder = new Comparator<ApkListDataItem>() {
            @Override
            public int compare(ApkListDataItem o1, ApkListDataItem o2) {
                if(o1.pkg_name == o2.pkg_name){
                    if(o1.file.length() > o2.file.length()){
                        o1.select_box_state=true;
                        o2.select_box_state=false;
                    }
                }
                return 0;
            }
        };
        Collections.sort(apkFilesList,duplicate_file_name_finder);

    }

    public void SelectNewerApks(List<ApkListDataItem> list){
        Comparator<ApkListDataItem> duplicate_file_name_finder = new Comparator<ApkListDataItem>() {
            @Override
            public int compare(ApkListDataItem o1, ApkListDataItem o2) {
                if(o1.pkg_name == o2.pkg_name){
                    if(o1.file_creation_time > o2.file_creation_time){
                        o1.select_box_state=true;
                        o2.select_box_state=false;
                    }
                }
                return 0;
            }
        };
        Collections.sort(list,duplicate_file_name_finder);
    }

    public void SelectOlderApks(List<ApkListDataItem> list){
        Comparator<ApkListDataItem> duplicate_file_name_finder = new Comparator<ApkListDataItem>() {
            @Override
            public int compare(ApkListDataItem o1, ApkListDataItem o2) {
                if(o1.pkg_name == o2.pkg_name){
                    if(o1.file_creation_time < o2.file_creation_time){
                        o1.select_box_state=true;
                        o2.select_box_state=false;
                    }
                }
                return 0;
            }
        };
        Collections.sort(list,duplicate_file_name_finder);
    }


    public class LongTask extends AsyncTask<Object, Object, String> {
        public LongTask() {
            super();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i(TAG, "in OnPreExecute :");
        }

        @Override
        protected void onProgressUpdate(Object... objects) {

            if(((String) objects[0]).equals("toast")){
                Toast.makeText(getContext(), (String) objects[1], Toast.LENGTH_LONG).show();
            }else if(((String) objects[0]).equals("label_file")){
                text_msgs.setText("Loading : " + ((File) objects[1]).getName());
                // Log.i(TAG,objects[0].getClass().toString());
                Log.i(TAG, "Loading : " + ((File) objects[1]).getName());
            }else if(((String) objects[0]).equals("label_string")){
                text_msgs.setText((String) objects[1]);
            }

            super.onProgressUpdate(objects);
        }

        @Override
        protected String doInBackground(Object... objects) {
            Log.i(TAG, "in DoInBackground :");

            if (objects[0].equals("search")) {
                searchApks((File) objects[1]);
                return "Searching completed.";
            } else if (objects[0].equals("rootinstall")) {
                RootInstall((List) objects[1]);
                return "Root Installation Completed.";
            } else if (objects[0].equals("norootinstall")) {
                NoRootInstall((List) objects[1]);
                return "Installation Completed.";
            } else if (objects[0].equals("rootuninstall")) {
                RootUninstall((List) objects[1]);
                return "Root Uninstall Operation completed.";
            } else if (objects[0].equals("delete")) {
                DeleteApks((List) objects[1]);
                return "Apk Deletion Operation completed.";
            } else if (objects[0].equals("rename")) {
                RenameApks((List) objects[1]);
                return "Renaming completed.";
            } else if (objects[0].equals("move")) {
                MoveApks((List) objects[1]);
                return "Renaming completed.";
            } else if (objects[0].equals("record_paths")) {
                makeAllApkParentDirList((File)objects[1]);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.i(TAG, "in OnPostExecute :");
            SortApkList();
            //showSnackBar(s);
            //ShowMsgInTextView();
            super.onPostExecute(s);
        }

        public void searchApks(File dir_path) {

            list_files = new ArrayList<>();
            apkFilesList = new ArrayList<>();

            //CHECK FOR CORRECT PATH
            if (dir_path.exists() & dir_path.isDirectory()) {
                // CHECK IF TO SEARCH IN SUBDIRECTORY
                publishProgress("toast","Path is set to : " + dir_path.getAbsolutePath());
                if (AppListActivity.sharedPrefApkManager.getBoolean(key_search_subfolders, true)) {
                    apkFilesList = getAllSubDirFiles(dir_path);
                    Log.i(TAG, apkFilesList.size() + " files found. Search in SubDirectory : ON");
                }
                // CHECK IF TO NOT SEARCH IN SUBDIRECTORY
                else if (!AppListActivity.sharedPrefApkManager.getBoolean(key_search_subfolders, true)) {
                    apkFilesList = getDirFiles(dir_path);
                    Log.i(TAG, apkFilesList.size() + " files found.Search in SubDirectory : OFF");
                }
            } else {              // IF INVALID FOLDER PATH
                publishProgress("toast","Set Correct Path");
            }
            label_reset =true;
        }

        List<ApkListDataItem> apklist = new ArrayList<>();
        String path_not_set = "PATH NOT SET";

        public List getAllSubDirFiles(File file1) {
            //  Log.i(TAG,"getAllSubDirFiles: "+f1.getName());
            ApkListDataItem ldm;
            for (File f1 : file1.listFiles()) {
                //  Log.i(TAG,"File : "+f1.getName());
                if (f1.isFile() & f1.getName().endsWith(".apk")) {
                    ldm = new ApkListDataItem(f1, getContext());
                    if (ldm.apk_pkg_info != null) {
                        apklist.add(ldm);
                    }
                    //Log.i(TAG,"File : "+f1.getName());
                    publishProgress("label_file",f1);
                } else if (f1.isDirectory()) {
                    getAllSubDirFiles(f1);
                }
            }
            label_reset =true;
            return apklist;
        }

        public List getDirFiles(File file1) {
            List<ApkListDataItem> apklist = new ArrayList<>();
            for (File f1 : file1.listFiles()) {
                if (f1.isFile() & f1.getName().endsWith(".apk")) {
                    apklist.add(new ApkListDataItem(f1, getContext()));
                    publishProgress("label_file",f1);
                }
            }
            label_reset =true;
            return apklist;
        }

        public void RootInstall(List<ApkListDataItem> files_list) {
            long apk_size;
            String command;
            List<String> std_err = new ArrayList<>();
            List<String> std_out = new ArrayList<>();

            Log.i(TAG, "Root Access " + rootAccess);
            Log.i(TAG, " No. of files to UPDATE/INSALL  " + selected_files_list.size());
            //   Toast.makeText(getContext(), "No. of files to install : " + selected_files_list.size(), Toast.LENGTH_LONG).show();
            publishProgress("toast","No. of files to install : " + selected_files_list.size());

            for (ApkListDataItem file : files_list) {
                try {
                    apk_size = file.file.length();
                    command = "cat \"" + file.file.getPath() + "\"|pm install -S " + apk_size;
                    Shell.su(command).to(std_out, std_err).exec();
                    //  Log.i(TAG, " EXECUTING :"+ command);
                    Log.i(TAG, " ROOT installing " + file.file_name + " : " + std_out.get(0));
                    publishProgress("toast","Installation Output : " + std_out.get(0));
                } catch (Exception ex) {
                    if (!std_err.isEmpty()) {
                        //  Log.e(TAG, " ROOT INSTALL ERROR of " + file.file_name + " \nError : " + std_err.get(0));
                        publishProgress("toast"," Error installing \"" + file.file_name + "\"");
                    }
                    Log.e(TAG, " ROOT INSTALL ERROR of " + file.file_name + " \nError : " + ex);
                }
            }
            label_reset =true;
        }

        public void RootUninstall(List<ApkListDataItem> files_list) {
            String command;
            List<String> std_err = new ArrayList<>();
            List<String> std_out = new ArrayList<>();

            Log.i(TAG, "Root Access " + rootAccess);
            Log.i(TAG, " No. of files to UNINSTALL  " + selected_files_list.size());
            publishProgress("No. of files to uninstall : " + selected_files_list.size());

            for (ApkListDataItem file : files_list) {
                if (file.isInstalled) {
                    try {
                        command = "pm uninstall " + file.pkg_name;
                        Shell.su(command).to(std_out, std_err).exec();
                        Log.i(TAG, " EXECUTING :" + command);
                        Log.i(TAG, " ROOT Uninstalling " + file.file_name + " : " + std_out.get(0));
                        publishProgress("UnInstallation Output : " + std_out.get(0));
                    } catch (Exception ex) {
                        if (!std_err.isEmpty()) {
                            Log.e(TAG, " ROOT UNINSTALL ERROR : " + file.file_name + " \nError : " + std_err.get(0));
                            //Toast.makeText(getContext(), " Error installing \"" + file.file_name + "\"", Toast.LENGTH_SHORT).show();
                            //publishProgress(" Error Uninstalling \"" + file.file_name + "\"");
                        }
                        //publishProgress(" Error Uninstalling \"" + file.file_name + "ex");
                        Log.e(TAG, " ROOT INSTALL ERROR of " + file.file_name + " \nError : " + ex);
                    }
                }
            }
            label_reset =true;
        }

        public void Uninstall(List<ApkListDataItem> files_list) {
            String command;
            List<String> std_err = new ArrayList<>();
            List<String> std_out = new ArrayList<>();
            Intent i;
            Log.i(TAG, "Root Access " + rootAccess);
            Log.i(TAG, " No. of files to UNINSTALL  " + selected_files_list.size());
            publishProgress("No. of files to uninstall : " + selected_files_list.size());

            for (ApkListDataItem file : files_list) {
                if (file.isInstalled) {
                    Toast.makeText(getContext(), "Confirm to uninstall " + file.app_name, Toast.LENGTH_SHORT).show();
                    i = new Intent(Intent.ACTION_DELETE);
                    //i.setData(Uri.parse(p.packageName));
                    i.putExtra(EXTRA_PACKAGE_NAME, file.pkg_name);
                    getContext().startActivity(i);
                    Log.i(TAG, " Uninstalling " + file.file_name + " : " + std_out.get(0));
                }
            }
            label_reset =true;
        }

        public void MoveApks(List<ApkListDataItem> list) {
            String value_parent_folder;
            int count_moved = 0;
            int count_not_moved = 0;
            int count_cannot_move = 0;

            boolean status;

            for (ApkListDataItem aldi : list) {
                value_parent_folder = sharedPrefRepository.getString(aldi.pkg_name, path_not_set);

                if (value_parent_folder.equals(path_not_set)) {
                    count_cannot_move++;
                } else {
                    publishProgress("label_string"," Moving :" + aldi.file_name + "\nTo :" + value_parent_folder+"/");
                    String command = "mv \""+aldi.file.getAbsolutePath()+"\" \""+value_parent_folder + "/" + aldi.file_name+"\"";

                    try{
                        Shell.sh(command).exec();
                        publishProgress("toast","Moved " + aldi.file_name + " to " + value_parent_folder+" Successfully");
                        count_moved++;
                    }catch(Exception ex){
                        Log.i(TAG,"Error Moving : "+ex);
                        publishProgress("toast","Failed Moving " + aldi.file_name + " to " + value_parent_folder);
                        count_not_moved++;
                    }
                    // status = copyFileTo(new File(aldi.file.getAbsolutePath()), new File(value_parent_folder + "/" + aldi.file_name));
//                    if (status) {
//                        publishProgress("toast","Done Moved " + aldi.file_name + " to " + value_parent_folder);
//                        count_moved++;
//                    }
//                    else{
//                        publishProgress("toast","Failed Moving " + aldi.file_name + " to " + value_parent_folder);
//                        count_not_moved++;
//                    }
                }
            }
            label_reset = false;
            Log.i(TAG, "MOVING SUCCESSFUL FOR :" + count_moved);
            Log.i(TAG, "CANNOT MOVE :" + count_cannot_move);
            Log.i(TAG, "MOVING FAILED :" + count_not_moved);
            publishProgress("label_string","MOVING SUCCESSFUL :" + count_moved +
                                    "\nMOVING FAILED :" + count_not_moved+
                                    "\tCANNOT MOVE :" + count_cannot_move);
        }

//        public void ReplaceApks(List<ApkListDataItem> list) {
//            String value_parent_folder;
//            int count_moved = 0;
//            int count_not_moved = 0;
//            int count_cannot_move = 0;
//
//            boolean status;
//
//            for (ApkListDataItem aldi : list) {
//                value_parent_folder = sharedPrefRepository.getString(aldi.pkg_name, path_not_set);
//
//                if (value_parent_folder.equals(path_not_set)) {
//                    count_cannot_move++;
//                } else {
//                    publishProgress("label_string"," Moving :" + aldi.file_name + "\nTo :" + value_parent_folder+"/");
//                    String command = "mv \""+aldi.file.getAbsolutePath()+"\" \""+value_parent_folder + "/" + aldi.file_name+"\"";
//
//                    try{
//                        Shell.sh(command).exec();
//                        publishProgress("toast","Moved " + aldi.file_name + " to " + value_parent_folder+" Successfully");
//                        count_moved++;
//                    }catch(Exception ex){
//                        Log.i(TAG,"Error Moving : "+ex);
//                        publishProgress("toast","Failed Moving " + aldi.file_name + " to " + value_parent_folder);
//                        count_not_moved++;
//                    }
//                    // status = copyFileTo(new File(aldi.file.getAbsolutePath()), new File(value_parent_folder + "/" + aldi.file_name));
////                    if (status) {
////                        publishProgress("toast","Done Moved " + aldi.file_name + " to " + value_parent_folder);
////                        count_moved++;
////                    }
////                    else{
////                        publishProgress("toast","Failed Moving " + aldi.file_name + " to " + value_parent_folder);
////                        count_not_moved++;
////                    }
//                }
//            }
//
//            Log.i(TAG, "MOVING SUCCESSFUL FOR :" + count_moved);
//            Log.i(TAG, "CANNOT MOVE :" + count_cannot_move);
//            Log.i(TAG, "MOVING FAILED :" + count_not_moved);
//            publishProgress("label_string","MOVING SUCCESSFUL :" + count_moved +
//                    "\nMOVING FAILED :" + count_not_moved+
//                    "\tCANNOT MOVE :" + count_cannot_move);
//        }
//

        ApkListDataItem ldm;
        int count = 0;

        public void makeAllApkParentDirList(File file1) {
            //Log.i(TAG,"getAllSubDirFiles: "+f1.getName());
            if (file1.exists() & file1.isDirectory()) {
                for (File f1 : file1.listFiles()) {
                    if (f1.isFile() & f1.getName().endsWith(".apk"))
                    {  //Log.i(TAG,"File : "+f1.getName());
                        ldm = new ApkListDataItem(f1, getContext());
                        if (ldm.apk_pkg_info != null)
                        {
                            prefEditRepository.putString(ldm.pkg_name, ldm.file.getParent()).commit();
                            count++;
                            publishProgress("label_string",count+" "+ldm.file_name+" \nPATH: "+ldm.file.getParent());
                        }
                    } else if (f1.isDirectory()) {
                        makeAllApkParentDirList(f1);
                    }
                }
                Log.i(TAG, "Apks in Repository Count : " + count);
            }
            else {
                publishProgress("toast","Set Proper Target/Moveto Folder Path in App Settings");
            }
            publishProgress("label_string","Apks in Repository Count : " + count);
            label_reset = false;
        }

        public boolean copyFileTo(FileInputStream originFile, File destinationFile) {
            boolean exportDone = true;
            try {/*from ww  w .  ja v a 2  s .  c  o  m*/
                File sd = Environment.getExternalStorageDirectory();
                if (sd.canWrite()) {
                    // Create parent directories
//                    destinationFile.getParentFile().mkdirs();
                    FileChannel src = originFile.getChannel();
                    FileChannel dst = new FileOutputStream(destinationFile).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            } catch (Exception e) {
                exportDone = false;
                Log.i(TAG, "File Copy Status: " + exportDone+" \nError :"+e);
            }
            publishProgress("toast","File Copy Status : " + exportDone);
            Log.i(TAG, "File Copy Status : " + exportDone);
            return exportDone;
        }

        private boolean copyFileTo(File originFile, File destinationFile) {
            try {
                return copyFileTo(new FileInputStream(originFile), destinationFile);
            } catch (Exception e) {
                Log.i(TAG, "File Copy Status: " + false+" \nError :"+e);
                return false;
            }
        }

        public void NoRootInstall(List<ApkListDataItem> files_list) {

            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setType("application/vnd.android.package-archive");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Log.i(TAG, "Root Access " + rootAccess);

            for (ApkListDataItem file : files_list) {
//                i.setData(file.file_uri);
//                getContext().startActivity(i);
            }
            label_reset =true;
        }

        public void DeleteApks(List<ApkListDataItem> files_list) {

            boolean isDeleted;
            for (ApkListDataItem file : files_list) {
                isDeleted = file.file.delete();
                Log.i(TAG, "Deletion of " + file.file_name + " : " + isDeleted);
                publishProgress("label_string","Deletion of " + file.file_name + " : " + isDeleted);
            }
            label_reset =true;
        }

        public void RenameApks(List<ApkListDataItem> files_list) {
            Log.i(TAG, "Renaming Files Count :" + files_list.size());

            if (AppListActivity.sharedPrefApkManager.contains(name_format_data_saved)) {

                int part1 = AppListActivity.sharedPrefApkManager.getInt(name_part_1, 1);
                Log.i(TAG, "Part 1 :" + part1);

                String part2 = AppListActivity.sharedPrefApkManager.getString(name_part_2, "_v");
                Log.i(TAG, "Part 2 :" + part2);

                int part3 = AppListActivity.sharedPrefApkManager.getInt(name_part_3, 2);
                Log.i(TAG, "Part 3 :" + part3);

                String part4 = AppListActivity.sharedPrefApkManager.getString(name_part_4, "_");
                Log.i(TAG, "Part 4 :" + part4);

                int part5 = AppListActivity.sharedPrefApkManager.getInt(name_part_5, 3);
                Log.i(TAG, "Part 5 :" + part5);

                String part6 = AppListActivity.sharedPrefApkManager.getString(name_part_6, "");
                Log.i(TAG, "Part 6 :" + part6);

                int part7 = AppListActivity.sharedPrefApkManager.getInt(name_part_7, 0);
                Log.i(TAG, "Part 7 :" + part7);

                String part8 = AppListActivity.sharedPrefApkManager.getString(name_part_8, "");
                Log.i(TAG, "Part 8 :" + part8);

                File f1;
                File f2;
                String parent;
                boolean result;
                for (ApkListDataItem f : files_list) {
                    f1 = new File(f.file.getAbsolutePath());
                    Log.i(TAG, " f1 path : " + f.file.getAbsolutePath());
                    parent = f.file.getParent();
                    Log.i(TAG, " f1 parent : " + parent);
                    f2 = new File(parent + "/" + getName(f, part1) + part2 + getName(f, part3) + part4 + getName(f, part5) + part6 + getName(f, part7) + part8 + ".apk");
                    result = (f.file).renameTo(f2);
                    publishProgress("label_string","Renaming : \"" + f.file_name + "\" - " + result);
                    Log.i(TAG, "Renaming " + f.file_name + " : \"" + f2 + "\" - " + result);
                }
            } else {
                Log.i(TAG, "Set a proper Name Format First");
                publishProgress("toast","Set a proper Name Format First");
            }

            label_reset =true;
        }

        protected String getName(ApkListDataItem ld, int i) {
            switch (i) {
                case 0: {
                    return "";
                }
                case 1: {
                    return ld.app_name;
                }
                case 2: {
                    return ld.apk_version_name;
                }
                case 3: {
                    return ld.apk_version_code;
                }
                case 4: {
                    return ld.file_size;
                }
                case 5: {
                    return ld.pkg_name;
                }
                default: {
                    return "";
                }
            }
        }
    }

    public void ShowMsgInTextView() {
        if (label_reset) {
            String msg_text = "Total : " + apkFilesList.size() + "\t Selected : " + cla.getSelectedItemsList().size();
            text_msgs.setText(msg_text);
        }
        label_reset = false;
    }

    public void SortApkList() {
        Comparator<ApkListDataItem> file_name_comparator = (ApkListDataItem l1, ApkListDataItem l2) -> l1.file_name.compareTo(l2.file_name);
        Comparator<ApkListDataItem> file_size_comparator = (ApkListDataItem l1, ApkListDataItem l2) -> Long.compare(l1.file.length(), l2.file.length());
        Comparator<ApkListDataItem> modified_date_comparator = (ApkListDataItem l1, ApkListDataItem l2) -> Long.compare(l1.file.lastModified(), l2.file.lastModified());
        Comparator<ApkListDataItem> creation_date_comparator = (ApkListDataItem l1, ApkListDataItem l2) -> Long.compare(l1.file_creation_time, l2.file_creation_time);

        Log.i(TAG, " In SortApkList : sort by = " + sort_apks_by + " ApkFilesList Count: " + apkFilesList.size());

        switch (sort_apks_by) {
            case sort_apks_by_name: {
                Collections.sort(apkFilesList, file_name_comparator);
                if (order_apks_by == order_apks_decreasing) {
                    Collections.reverse(apkFilesList);
                }
                Log.i(TAG, "in sort_apks_by_name");
                break;
            }

            case sort_apks_by_date: {
                Collections.sort(apkFilesList, creation_date_comparator);
                if (order_apks_by == order_apks_decreasing) {
                    Collections.reverse(apkFilesList);
                }
                Log.i(TAG, "in sort_apks_by_date");
                break;
            }

            case sort_apks_by_size: {
                Collections.sort(apkFilesList, file_size_comparator);
                if (order_apks_by == order_apks_decreasing) {
                    Collections.reverse(apkFilesList);
                }
                Log.i(TAG, "in sort_apks_by_size");
                break;
            }

            default: {
                Collections.sort(apkFilesList, file_name_comparator);
                break;
            }
        }

        cla = new CustomApkListAdapter(apkFilesList, getContext());
        recyclerView.setAdapter(cla);

        cla.notifyDataSetChanged();
        ShowMsgInTextView();
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(getContext(), "Write External Storage permission allows us to save files. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e("value", "Permission Granted, Now you can use local drive .");
            } else {
                Log.e("value", "Permission Denied, You cannot use local drive .");
            }
            break;
        }
    }
}

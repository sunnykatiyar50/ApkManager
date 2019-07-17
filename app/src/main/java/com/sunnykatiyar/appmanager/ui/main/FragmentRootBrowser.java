package com.sunnykatiyar.appmanager.ui.main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sunnykatiyar.appmanager.ActivityOperations;
import com.sunnykatiyar.appmanager.AdapterRootBrowser;
import com.sunnykatiyar.appmanager.BuildConfig;
import com.sunnykatiyar.appmanager.ClassRootUtils;
import com.sunnykatiyar.appmanager.FragmentSettings;
import com.sunnykatiyar.appmanager.ObjectFile;
import com.sunnykatiyar.appmanager.R;
import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;

/**
 * A placeholder fragment containing a simple view.
 */
public class FragmentRootBrowser extends Fragment implements AdapterRootBrowser.MyRootBrowserCallBack,
        ClassRootUtils.notifyUIAboutOperation, ActivityOperations.CancelOperation {

    private static final String ARG_SECTION_NUMBER = "section_number";
    static final String TAG = "MYAPP : ROOT_BROWSER_FRAGMENT";
    private PageViewModel pageViewModel;
    public static final String key_root_access = FragmentSettings.key_root_access;
    private boolean rootAccess;
    private final String textview_msg ="textview_msg";
    private final String log_msg = "log_msg";
    private final String toast_msg ="toast_msg";

    final String key_sorting = "SORT BY";
    String value_sorting;
    final String key_order_by = "INVERSE SORTING";
    String value_order_by;

    final String PREF_NAME_APP_SETTINGS = "com.sunnykatiyar.appmanager.SETTINGS";
    public static SharedPreferences sharedPrefSettings;
    public static SharedPreferences.Editor prefAppSettings;

    private static final String PREF_NAME_ROOT_BROWSER = "com.sunnykatiyar.appmanager.ROOT_BROWSER";
    public static SharedPreferences sharedPrefRootBrowser;
    public static SharedPreferences.Editor prefEditRootBrowser;
    
    private static final String PREF_NAME_ROOT_BOOKMARKS = "com.sunnykatiyar.appmanager.ROOT_BOOKMARK_PATHS";
    public static SharedPreferences sharedPrefRootBookMarks;
    public static SharedPreferences.Editor prefEditRootBookmarks;

    final String key_spinner_position = "LAST_SELECTED_BOOKMARK";
    int value_spinner_position;

    final String path_not_set = "PATH NOT SET";

    final String sort_by_name = "SORT_BY_NAME";
    final String sort_by_date = "SORT_BY_DATE";
    final String sort_by_size = "SORT_BY_SIZE";
    final String sort_by_type = "SORT_BY_TYPE";

    boolean sort_folder_first = true ;

    final String order_increasing = "ORDER_INCREASING";
    final String order_decreasing = "ORDER_DECREASING";

    public String sort_by;
    public String order_by;

    Context context;
    TextView root_browser_textview_msgs;
    TextView root_browser_textview_path;
    RecyclerView root_browser_rview;
    ImageView root_browser_upButton;
    SpinnerAdapter spinnerAdapter_rootBrowser;
    Spinner spinner_root_browser;
    ProgressBar progressBar_rootBrowser;
    AdapterRootBrowser adapterRootBrowser;
    AdapterRootBrowser.MyRootBrowserCallBack myRootBrowserCallBack;
    ClassRootUtils.notifyUIAboutOperation notifyUIContext;
    BottomAppBar root_bottomAppBar;
    FloatingActionButton fab_action;
    ObjectFile rootObjectFile ;
    ObjectFile selectedObjectFile;
    String selectedPath;

    List<String> spinnerPathItems = new ArrayList<>();
    List<ObjectFile> objectFileList = new ArrayList<>();
    HashMap<String,List<ObjectFile>> cacheList = new HashMap<>();
    static List<ObjectFile> selectedFilesList = new ArrayList<>();

    String mount_system_ro = "mount -o ro,remount /system";
    String mount_system_rw = "mount -o rw,remount /system";
    int NEW_TASKS_ID = 0;
    int OPERATION_ID=0;
    int NO_OPERATION=0;
    boolean showHidden = false;
    final public static String key_show_hidden = "SHOW_HIDDEN_FILES";
    String value_show_hidden;
    final int OPERATION_COPY = 201;
    final int OPERATION_MOVE  = 204;
    final int OPERATION_RENAME = 209;
    final int OPERATION_DELETE = 212;
    final int OPERATION_INSTALL_APKS = 215;

    ClassRootUtils[] rootOperationsArray = new ClassRootUtils[20];
    ClassRootUtils searchTask ;
    static{
        Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR);
        Shell.Config.verboseLogging(BuildConfig.DEBUG);
        Shell.Config.setTimeout(10);
    }

    public static FragmentRootBrowser newInstance(int index){
        FragmentRootBrowser fragment = new FragmentRootBrowser();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = ViewModelProviders.of(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView( @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_root_browser, container, false);

        root_browser_textview_msgs = view.findViewById(R.id.root_browser_msg_text_view);
        root_browser_textview_path = view.findViewById(R.id.root_browser_path_textview);
        root_browser_rview = view.findViewById(R.id.root_browser_rview);
        root_browser_upButton = view.findViewById(R.id.root_browser_image_button_up);
        progressBar_rootBrowser =view.findViewById(R.id.root_browser_progress_bar);
        spinner_root_browser = view.findViewById(R.id.root_browser_storage_spinner);
        fab_action = view.findViewById(R.id.rootBrowser_fab_action);
        myRootBrowserCallBack = this;
        context = getContext();
        notifyUIContext=this;

        sharedPrefSettings = getContext().getSharedPreferences(context.getResources().getString(R.string.sharedPref_settings),
                MODE_PRIVATE);
        prefAppSettings = sharedPrefSettings.edit();

        sharedPrefRootBrowser = getContext().getSharedPreferences(context.getResources().getString(R.string.sharedPref_rootBrowser),
                MODE_PRIVATE);
        prefEditRootBrowser = sharedPrefRootBrowser.edit();

        sharedPrefRootBookMarks = getContext().getSharedPreferences(context.getResources().getString(R.string.sharedPref_rootBrowser_bookmarks),
                MODE_PRIVATE);
        prefEditRootBookmarks = sharedPrefRootBookMarks.edit();

        root_bottomAppBar = view.findViewById(R.id.root_bottomAppBar) ;
        root_bottomAppBar.replaceMenu(R.menu.files_bottom_menu);
        rootAccess = sharedPrefSettings.getBoolean(key_root_access, false);
        sort_by = sharedPrefRootBrowser.getString(key_sorting, sort_by_name);
        order_by = sharedPrefRootBrowser.getString(value_order_by, order_increasing);
        showHidden = sharedPrefRootBrowser.getBoolean(key_show_hidden, false);

        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(RecyclerView.VERTICAL);
        root_browser_rview.setLayoutManager(llm);
        DividerItemDecoration mdivider = new DividerItemDecoration(context, llm.getOrientation());
        root_browser_rview.addItemDecoration(mdivider);

        setSpinnerData();

        pageViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
               
            }
        });

        spinner_root_browser.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                showMsg(log_msg, "Spinner Item Selected :"+position);
                prefEditRootBrowser.putInt(key_spinner_position,position).commit();
                selectedPath =  spinnerPathItems.get(sharedPrefRootBrowser.getInt(key_spinner_position, 0));
                getSetFolderFiles(new ObjectFile(selectedPath));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        root_browser_upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String parent = selectedObjectFile.parent;
                if(null!=parent & new File(parent).exists()){
                    showMsg(log_msg, " UP BUTTON CLICK : New Parent Directory : "+parent);
                   // selectedObjectFile = new ObjectFile(parent);
                    getSetFolderFiles(new ObjectFile(parent));
                }else{
                    showMsg(toast_msg, " Cannot go Up... \nAlready at the root of the tree...");
                }
            }
        });

        spinner_root_browser.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                prefEditRootBookmarks.remove(spinnerPathItems.get(position)).commit();
                spinnerPathItems.remove(position);
                setSpinnerData();
                return true;
            }
        });

        root_bottomAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                ColorStateList colorStateList = ContextCompat.getColorStateList(context, R.color.color_state);
                if(adapterRootBrowser!=null){
                    selectedFilesList = adapterRootBrowser.getSelectedFileList();
                }

                switch(item.getItemId()){

                    case R.id.files_bottom_av_refresh:{
                       launchNewSearchTask(selectedObjectFile.path);
                        break;
                    }

                    case R.id.files_bottom_nav_create_new:{
                        showMsg(log_msg, "Create New Clicked.");
                        new ClassRootUtils(context,notifyUIContext).createNew(selectedObjectFile);
                        break;
                    }

                    case R.id.files_bottom_nav_select_all:{
                        selectAll(true);
                        showMsg(toast_msg, "Selected All");
                        adapterRootBrowser.notifyDataSetChanged();
                        break;
                    }

                    case R.id.files_bottom_nav_unselect_all:{
                        selectAll(false);
                        adapterRootBrowser.notifyDataSetChanged();
                        showMsg(toast_msg, "Unselected All");
                        break;
                    }

                    case R.id.files_bottom_nav_copy:{
                        OPERATION_ID = OPERATION_COPY;
                        showMsg(toast_msg, "File Ready to be Copied . Browse to target folder & paste");
                        fab_action.show();
                        adapterRootBrowser.selection_mode = false;
                        //selectAll(false);
                        showMsg(log_msg, "Selected files list : "+selectedFilesList.size());
                        break;
                    }

                    case R.id.files_bottom_nav_cut:{
                        OPERATION_ID = OPERATION_MOVE;
                        showMsg(toast_msg, "File Ready to be Moved . Browse to target folder & paste");
                        fab_action.show();
                        adapterRootBrowser.selection_mode=false;
                       // selectAll(false);
                        showMsg(log_msg, "Selected files list : "+selectedFilesList.size());
                      //fab_action.setImageResource(R.drawable.ic_content_paste_white_36dp);
                        break;
                    }

                    case R.id.files_bottom_nav_paste:{
                        launchPasteFilesTask();
                        break;
                    }

                    case R.id.files_bottom_nav_delete:{
                        launchDeleteFilesTask();
                        resetSelection();
                        break;
                    }

                    case R.id.files_bottom_nav_cancel:{
                        resetSelection();
                        break;
                    }

                    case R.id.files_bottom_nav_install:{
                        launchInstallApksTask();
                        break;
                    }

                    case R.id.files_bottom_nav_rename:{
                        NEW_TASKS_ID++  ;
                        rootOperationsArray[NEW_TASKS_ID] = new ClassRootUtils(context, notifyUIContext);
                        rootOperationsArray[NEW_TASKS_ID].rootRenameFile(selectedObjectFile);
                        break;
                    }
                }
                return true;
            }
        });

        fab_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               launchPasteFilesTask();
            }
        });
        
        return view;
    }

    private void setSpinnerData(){

        prefEditRootBookmarks.clear().commit();

        String rootPath = "/";
        rootObjectFile = new ObjectFile(rootPath);
        prefEditRootBookmarks.putString("root", rootPath).commit();

        File internalSDPath = Environment.getExternalStorageDirectory();
        if(internalSDPath.exists()){
            prefEditRootBookmarks.putString("Internal Storage", internalSDPath.getAbsolutePath()).commit();
        }
        
        String tempPath;
        spinnerPathItems = new ArrayList<>();
        Set<? extends Map.Entry<String, ?>> set = sharedPrefRootBookMarks.getAll().entrySet();
        for(Map.Entry entry : set){
             tempPath = entry.getValue().toString();
             if(new File(tempPath).exists()){
                 spinnerPathItems.add(tempPath);
             }
        }

       spinnerAdapter_rootBrowser = new ArrayAdapter<>(context,R.layout.listitem_storage_spinner,R.id.spinner_item, spinnerPathItems);
       spinner_root_browser.setAdapter(spinnerAdapter_rootBrowser);

        if(spinnerPathItems !=null & spinnerPathItems.size()>0){
            value_spinner_position = sharedPrefRootBrowser.getInt(key_spinner_position,0);
            selectedPath =  spinnerPathItems.get(value_spinner_position);
            spinner_root_browser.setSelection(value_spinner_position);
//          getSetFolderFiles(new ObjectFile(selectedPath));
        }

    }

    private void getSetFolderFiles(ObjectFile file){
        showMsg(log_msg, file.path);

        String parentPath;

        if(null!=file.path){
            parentPath = file.path;
            selectedObjectFile = file;
            setPathTextView(selectedObjectFile.path);
            if(cacheList.containsKey(parentPath)){
                showMsg(log_msg,"FOUND "+parentPath+" IN CACHE : SIZE "+cacheList.get(parentPath).size()) ;
                objectFileList = cacheList.get(parentPath);
                showMsg(log_msg,"Loading files from cache of :"+parentPath);
                setNewAdapter(objectFileList);
            }
            else{
                launchNewSearchTask(parentPath);
            }
        }
        else{
                showMsg(log_msg, "Not a valid folder path.");
        }
    }

    protected  void launchNewSearchTask(String parentPath){
        if(null!=searchTask) {
            if (searchTask.isRunning) {
                searchTask.cancelTask();
                Log.e(TAG, "TASK CANCEL REQUESTED FOR searchTask : ");
            }
        }
        showMsg(log_msg, "Starting Asynctask for " + parentPath);
        searchTask = new ClassRootUtils(context, notifyUIContext);
        searchTask.getFolderFilesFromShell(selectedObjectFile.path, showHidden);
    }

    void setNewAdapter(List<ObjectFile> list){
        if(null!=list){
            showMsg(log_msg, "in setNewAdapter : size : "+list.size());
            objectFileList = SortRootFileList(list);
            adapterRootBrowser = new AdapterRootBrowser(myRootBrowserCallBack, objectFileList, context);
            root_browser_rview.setAdapter(adapterRootBrowser);
            adapterRootBrowser.notifyDataSetChanged();
            showDefaultMsg(textview_msg);
        }
    }

    protected void resetSelection(){
        if(null!=selectedFilesList){
            selectedFilesList.clear();
            selectAll(false);
            adapterRootBrowser.selection_mode=false;
            adapterRootBrowser.selected_count=0;
        }
        OPERATION_ID = NO_OPERATION ;
    }

    protected void setPathTextView(String path){
        root_browser_textview_path.setText(path);
    }
    
    void showMsg(String msgtype, String str){

        if(null==str || str.isEmpty()){
            showDefaultMsg(msgtype);
        }else{
            if(msgtype.equals(toast_msg)){
                Toast.makeText(context,str,Toast.LENGTH_SHORT).show();
                Log.d(TAG,str);
            }else if(msgtype.equals(textview_msg)){
                root_browser_textview_msgs.setText(str);
                Log.d(TAG,str);
            }else if(msgtype.equals(log_msg)){
                Log.i(TAG,str);
            }
            //Log.d(TAG,str);
        }
    }

    void showDefaultMsg(String type){
        String msg1 = "Total Files : "+ objectFileList.size()+" Files Selected :"+adapterRootBrowser.getSelectedFileList().size();
        String msg = "Total Files : "+ objectFileList.size()+" Files Selected :"+adapterRootBrowser.selected_count;

        if(type.equals(log_msg)){
            Log.i(TAG,msg);
        }else if(type.equals(textview_msg)){
            root_browser_textview_msgs.setText(msg);
        }else{
            root_browser_textview_msgs.setText(msg);
            Log.i(TAG,msg);
        }
        Log.d(TAG,msg1)        ;
    }

    void selectAll(boolean enable){

        if(null!=objectFileList){
            for(ObjectFile f : objectFileList){
                f.isSelected = enable;
            }
        }

        showDefaultMsg(textview_msg);
        setNewAdapter(objectFileList);
    }

    void enableBottomActions(boolean action){
        if (action) {
            root_bottomAppBar.getMenu().setGroupVisible(R.id.menugroup_files_permament,true);
            root_bottomAppBar.getMenu().setGroupVisible(R.id.menugroup_files_option_group,false);
            root_bottomAppBar.getMenu().setGroupVisible(R.id.menugroup_files_action_group,true);
            if(adapterRootBrowser.selected_count==1){
                root_bottomAppBar.getMenu().setGroupVisible(R.id.menugroup_files_onefile_menu,true);
            }else{
                root_bottomAppBar.getMenu().setGroupVisible(R.id.menugroup_files_onefile_menu,false);
            }
        } else {
            root_bottomAppBar.getMenu().setGroupVisible(R.id.menugroup_files_permament,true);
            root_bottomAppBar.getMenu().setGroupVisible(R.id.menugroup_files_option_group,true);
            root_bottomAppBar.getMenu().setGroupVisible(R.id.menugroup_files_onefile_menu,false);
            root_bottomAppBar.getMenu().setGroupVisible(R.id.menugroup_files_action_group,false);
        }
    }

    protected void launchDeleteFilesTask(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm Deleting Files");
        builder.setMessage("Are You Sure to Delete "+selectedFilesList.size()+selectedObjectFile.name);
        builder.setPositiveButton("Yes", new AlertDialog.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                OPERATION_ID = OPERATION_DELETE;
                NEW_TASKS_ID++;
                rootOperationsArray[NEW_TASKS_ID] = new ClassRootUtils(context,notifyUIContext,NEW_TASKS_ID);
                rootOperationsArray[NEW_TASKS_ID].deleteFiles(selectedFilesList);
            }
        });
        builder.setNegativeButton("No", new AlertDialog.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
        resetSelection();
    }

    protected void launchPasteFilesTask(){

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
            if(OPERATION_ID == OPERATION_COPY){
                builder.setTitle("Confirm the Operation");
                builder.setMessage("Copy "+selectedFilesList.size()+" files to "+selectedObjectFile.name);
                builder.setPositiveButton("Confirm", new AlertDialog.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NEW_TASKS_ID++;
                        rootOperationsArray[NEW_TASKS_ID] = new ClassRootUtils(context, notifyUIContext, NEW_TASKS_ID);
                        rootOperationsArray[NEW_TASKS_ID].rootCopy(selectedFilesList, selectedObjectFile.path);
                    }
                });

            }else if(OPERATION_ID == OPERATION_MOVE){
                builder.setTitle("Confirm the Operation");
                builder.setMessage("Move "+selectedFilesList.size()+" files to "+selectedObjectFile.name);
                builder.setPositiveButton("Confirm", new AlertDialog.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NEW_TASKS_ID++;
                        rootOperationsArray[NEW_TASKS_ID] = new ClassRootUtils(context, notifyUIContext, NEW_TASKS_ID);
                        rootOperationsArray[NEW_TASKS_ID].rootMove(selectedFilesList, selectedObjectFile.path);
                    }
                });
            }

            builder.setNegativeButton("Cancel", new AlertDialog.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });


        builder.show();
        fab_action.hide();
        resetSelection();
    }

    protected void launchInstallApksTask(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm Apk Installation.");
        builder.setMessage("Are You Sure to install "+selectedFilesList.size()+" files.");
        builder.setPositiveButton("Confirm", new AlertDialog.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                NEW_TASKS_ID++;
                rootOperationsArray[NEW_TASKS_ID] = new ClassRootUtils(context, notifyUIContext,NEW_TASKS_ID);
                rootOperationsArray[NEW_TASKS_ID].installApksList(selectedFilesList);
            }
        });
        builder.setNegativeButton("Cancel Operation", new AlertDialog.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
        resetSelection();
    }

    protected  void launchRenameFilesTask(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm Copying Files");
        builder.setMessage("Are You Sure to copy "+selectedFilesList.size()+selectedObjectFile.name);
        builder.setPositiveButton("Confirm", new AlertDialog.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNegativeButton("Cancel", new AlertDialog.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
        resetSelection();
    }

    public List<ObjectFile> SortRootFileList(List to_sort_list) {
        Comparator<ObjectFile> file_name_comparator =
                (ObjectFile l1, ObjectFile l2) -> l1.name.toLowerCase().compareTo(l2.name.toLowerCase());
        Comparator<ObjectFile> file_size_comparator =
                (ObjectFile l1, ObjectFile l2) -> Long.compare(l1.long_size, l2.long_size);
        Comparator<ObjectFile> modified_date_comparator =
                (ObjectFile l1, ObjectFile l2) -> l1.mod_time.compareTo( l2.mod_time);
        Comparator<ObjectFile>  file_type_comparator =
                (ObjectFile l1, ObjectFile l2) -> l1.file_type.toLowerCase().compareTo(l2.file_type.toLowerCase());

      //  Log.i(TAG, " In SortRootFileList : sort by value = " + sort_by + "  order by value = " + order_by);

        switch(sort_by) {
            case sort_by_name: {
                Collections.sort(to_sort_list, file_name_comparator);
                if (order_by == order_decreasing) {
                    Collections.reverse(to_sort_list);
                }
                //  Log.i(TAG, "in sort_by_name");
                break;
            }

            case sort_by_date: {
                Collections.sort(to_sort_list, modified_date_comparator);
                if (order_by == order_decreasing) {
                    Collections.reverse(to_sort_list);
                }
                //    Log.i(TAG, "in sort_by_date");
                break;
            }

            case sort_by_size: {
                Collections.sort(to_sort_list, file_size_comparator);
                if (order_by == order_decreasing) {
                    Collections.reverse(to_sort_list);
                }
                //    Log.i(TAG, "in sort_by_size");
                break;
            }

            case sort_by_type: {
                Collections.sort(to_sort_list, file_type_comparator);
                if (order_by == order_decreasing) {
                    Collections.reverse(to_sort_list);
                }
//                Log.i(TAG, "in sort_by_type");
                break;
            }

            default: {
                Collections.sort(to_sort_list, file_name_comparator);
                break;
            }
        }
        return to_sort_list;
    }

    @Override
    public void openDocument(ObjectFile obj) {
        if(obj.isDirecrtory){
            getSetFolderFiles(obj);
        }else{
            if(obj.name.endsWith(".apk")){
                new ClassRootUtils(context, notifyUIContext, ++NEW_TASKS_ID).installApk(obj);
            }else{
//                Intent i = new Intent(Intent.ACTION_VIEW);
//                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                i.setData(Uri.fromParts(obj.path));
//                context.startActivity(i);

                Shell.sh("am start -a android.intent.action.VIEW -d "+obj.path);
            }
        }
    }

    @Override
    public void selectAllitems(boolean enable) {
          selectAll(enable);
    }

    @Override
    public void updateMsgTextview(String str) {
         showMsg(textview_msg, str);
    }

    @Override
    public void enableBottomActionBar(boolean action) {
        enableBottomActions(action);
    }

    @Override
    public void updateTextView(String msg_type, String msg) {
        showMsg(msg_type,msg);
    }

    @Override
    public void setNewObjectFilesList(String path, List<ObjectFile> list) {
        cacheList.put(path, list);
        showMsg(log_msg, "setNewAdapter : Directory cached : "+selectedObjectFile.path+" with of size"+list.size());
        showMsg(log_msg, "setNewAdapter : Total Directories in cache : "+cacheList.size());
        setNewAdapter(list);
        progressBar_rootBrowser.setVisibility(View.GONE);
    }

    @Override
    public void setprogressBar(boolean visible) {
        if(visible){
            progressBar_rootBrowser.setVisibility(View.VISIBLE);
        }else{
            progressBar_rootBrowser.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_files, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.menuitem_files_hidden).setChecked(sharedPrefRootBrowser.getBoolean(key_show_hidden,true));

        if(null!=selectedFilesList && null!=adapterRootBrowser){
            selectedFilesList =  adapterRootBrowser.getSelectedFileList();
            if(selectedFilesList.size()==1){
                 menu.findItem(R.id.menuitem_files_rename).setVisible(true);
                 menu.setGroupVisible(R.id.menugroup_files_action_group,true);

            }else if(selectedFilesList.size()>1){
                menu.findItem(R.id.menuitem_files_rename).setVisible(false);
                menu.setGroupVisible(R.id.menugroup_files_action_group,true);
            }else {
                menu.findItem(R.id.menuitem_files_rename).setVisible(false);
                menu.setGroupVisible(R.id.menugroup_files_action_group,false);
            }

        }
        else{
            menu.setGroupVisible(R.id.menugroup_files_option_group,false);
        }

        value_sorting = sharedPrefRootBrowser.getString(key_sorting, sort_by_name);
        value_order_by = sharedPrefRootBrowser.getString(value_order_by, order_increasing);

        showMsg(log_msg, " Sorting Value found :"+value_sorting);
        showMsg(log_msg, " Order by Value found :"+value_order_by);

        switch(value_sorting)
        {
            case sort_by_date:{
                sort_by = sort_by_date;
                menu.findItem(R.id.menuitem_files_sortbydate).setChecked(true);
                Log.i(TAG, "onPrepareOptionsMenu: matched sort_by_date : "+sort_by);
                break;
            }

            case sort_by_name:{
                sort_by = sort_by_name;
                menu.findItem(R.id.menuitem_files_sortbyname).setChecked(true);
                Log.i(TAG, "onPrepareOptionsMenu: matched sort_by_name : "+sort_by);
                break;
            }

            case sort_by_size:{
                sort_by = sort_by_size;
                menu.findItem(R.id.menuitem_files_sortbysize).setChecked(true);
                Log.i(TAG, "onPrepareOptionsMenu:matched sort_by_size : "+sort_by);
                break;
            }
        }

        switch(value_order_by) {

            case order_increasing: {
                order_by = order_increasing;
                menu.findItem(R.id.menuitem_files_increasing).setChecked(true);
                Log.i(TAG, "onPrepareOptionsMenu: matched  order_increasing: "+order_by);
                break;
            }

            case order_decreasing: {
                value_order_by = order_decreasing;
                menu.findItem(R.id.menuitem_files_decreasing).setChecked(true);
                Log.i(TAG, "onPrepareOptionsMenu: matched  order_decreasing: "+order_by);
                break;
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(adapterRootBrowser!=null & objectFileList.size()>0){
            selectedFilesList = adapterRootBrowser.getSelectedFileList();
        }

        switch(item.getItemId()){

            case R.id.menuitem_files_load:{
                launchNewSearchTask(selectedObjectFile.path);
                break;
            }

            case R.id.menuitem_files_copy_files:{
                OPERATION_ID = OPERATION_COPY;
                showMsg(toast_msg, "File Ready to be Copied . Browse to target folder & paste");
                fab_action.show();
                adapterRootBrowser.selection_mode = false;
                //selectAll(false);
                showMsg(log_msg, "Selected files list : "+selectedFilesList.size());
                break;
            }

            case R.id.menuitem_files_move_files:{
                OPERATION_ID = OPERATION_MOVE;
                showMsg(toast_msg, "File Ready to be Moved . Browse to target folder & paste");
                fab_action.show();
                adapterRootBrowser.selection_mode = false;
                showMsg(log_msg, "Selected files list : "+selectedFilesList.size());
                break;
            }

            case R.id.menuitem_files_paste:{
                launchPasteFilesTask();
                break;
            }

            case R.id.menuitem_files_delete:{
               launchDeleteFilesTask();
               break;
            }

            case R.id.menuitem_files_install:{
                launchInstallApksTask();
                break;
            }

            case R.id.menuitem_files_rename:{
                launchRenameFilesTask();
                break;
            }

            case R.id.menuitem_files_properties:{

                break;
            }

            case R.id.menuitem_add_bookmark:{
                prefEditRootBookmarks.putString(selectedObjectFile.name,selectedObjectFile.path).commit();
                setSpinnerData();
                break;
            }

    //-----------------------------SELECT FILES------------------------------------------------------
            case R.id.menuitem_files_select_all:{
                selectAll(true);
                break;
            }

            case R.id.menuitem_files_select_invert:{
                for(ObjectFile obj:objectFileList){
                    obj.isSelected = !obj.isSelected;
                }
                adapterRootBrowser.notifyDataSetChanged();
                break;
            }

            case R.id.menuitem_files_unselect_all:{
                selectAll(false);
                break;
            }


    //-----------------------------SORT_BY------------------------------------------------------
            case R.id.menuitem_files_sortbyname:{
                sort_by = sort_by_name;
                if(!item.isChecked()){
                    item.setChecked(true);
                }
                SortRootFileList(objectFileList);
                adapterRootBrowser.notifyDataSetChanged();
                prefEditRootBrowser.putString(key_sorting, sort_by_name).commit();
                break;
            }

            case R.id.menuitem_files_sortbydate:{
                sort_by = sort_by_date;
                if(!item.isChecked()){
                    item.setChecked(true);
                }
                SortRootFileList(objectFileList);
                adapterRootBrowser.notifyDataSetChanged();
                prefEditRootBrowser.putString(key_sorting, sort_by).commit();
                break;
            }

            case R.id.menuitem_files_sortbysize:{
                sort_by = sort_by_size;
                if(!item.isChecked()){
                    item.setChecked(true);
                }
                SortRootFileList(objectFileList);
                adapterRootBrowser.notifyDataSetChanged();
                prefEditRootBrowser.putString(key_sorting, sort_by).commit();
                break;
            }

            case R.id.menuitem_files_sortbytype:{
                sort_by = sort_by_type;
                if(!item.isChecked()){
                    item.setChecked(true);
                }
                SortRootFileList(objectFileList);
                prefEditRootBrowser.putString(key_sorting, sort_by).commit();
                adapterRootBrowser.notifyDataSetChanged();
                break;
            }

   //-----------------------------ORDER------------------------------------------------------
            case R.id.menuitem_files_increasing:{
                order_by = order_increasing;
                if(!item.isChecked()){
                    item.setChecked(true);
                }
                prefEditRootBrowser.putString(value_order_by, order_increasing).commit();
                SortRootFileList(objectFileList);
                adapterRootBrowser.notifyDataSetChanged();
                break;
            }

            case R.id.menuitem_files_decreasing:{
                order_by = order_decreasing;
                if(!item.isChecked()){
                    item.setChecked(true);
                }
                prefEditRootBrowser.putString(value_order_by, order_decreasing).commit();
                SortRootFileList(objectFileList);
                adapterRootBrowser.notifyDataSetChanged();
                break;
            }

            case R.id.menuitem_files_hidden: {
                item.setChecked(!item.isChecked());
                 showHidden = item.isChecked();
                 prefEditRootBrowser.putBoolean(key_show_hidden, showHidden).commit();
                launchNewSearchTask(selectedObjectFile.path);
                break;
            }

        }

        return false;
    }

    @Override
    public void cancelOperationById(int id) {
       rootOperationsArray[id].cancelTask();
    }

    @Override
    public void onStop() {
        super.onStop();
        cacheList.clear();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cacheList.clear();
    }
}
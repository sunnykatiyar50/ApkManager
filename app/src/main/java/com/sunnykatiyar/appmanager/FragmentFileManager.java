package com.sunnykatiyar.appmanager;


import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.DocumentsContract;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static android.app.Activity.RESULT_OK;
import static android.provider.DocumentsContract.Document.COLUMN_DISPLAY_NAME;
import static android.provider.DocumentsContract.Document.COLUMN_DOCUMENT_ID;
import static android.provider.DocumentsContract.Document.COLUMN_FLAGS;
import static android.provider.DocumentsContract.Document.COLUMN_ICON;
import static android.provider.DocumentsContract.Document.COLUMN_LAST_MODIFIED;
import static android.provider.DocumentsContract.Document.COLUMN_MIME_TYPE;
import static android.provider.DocumentsContract.Document.COLUMN_SIZE;
import static android.provider.DocumentsContract.Document.COLUMN_SUMMARY;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentFileManager extends Fragment implements AdapterFileManager.AdapterCallBack,
        ClassNoRootUtils.updateUINoRootUtils, ActivityOperations.CancelOperation {

    private Context context;

    private final String TAG = "MYAPP : FRAGMENT_FILES : ";

    private final String textview_msg ="textview_msg";
    private final String log_msg = "log_msg";
    private final String toast_msg ="toast_msg";

    private final int REQUEST_CODE_EXTERNAL_STORAGE = 677;

    private static final String key_storage_spinner_position = "STORAGE_SPINNER_SELECTED_POSITION";
    private int NEW_TASK_ID = 0;
    private RecyclerView files_rview;
    private TextView files_path_textview;
    private TextView files_msg_textview;

    private Spinner storage_spinner;
    private ProgressBar files_progress_bar;
    private Uri selected_tree_uri;
    private Uri selected_directory_uri;
    private ObjectDocumentFile selected_object_file;

    private final String key_show_hidden = "SHOW_HIDDEN_FILES";
    String value_show_hidden;
    private List<Uri> storages_uri_list;
    private List<String> storage_spinner_items;

    private List<ObjectDocumentFile> objectDocumentFileList;
    private List<ObjectDocumentFile> selectedFilesList;

    private AdapterFileManager files_adapter;
    private LoadFilesAsyncTask loadFilesTask;
    EditText toolbar_edittext;
    private ContentResolver resolver ;

    private FloatingActionButton fab_action;
    private BottomAppBar bottomAppBar;

    private final String key_sort_by = "SORT_BY";
    private final String key_order_by = "ORDER_BY";

    private final String sort_by_date = "SORT_BY_DATE";
    private final String sort_by_size = "SORT_BY_SIZE";
    private final String sort_by_name = "SORT_BY_NAME";
    private final String sort_by_type = "SORT_BY_TYPE";
    private final String order_increasing = "ORDER_INCREASING";
    private final String order_decreasing = "ORDER_DECREASING";

    private String global_sort_by;
    private String global_order_by;

    public static ClassNoRootUtils[] NoRootOperations;
    private ClassNoRootUtils.updateUINoRootUtils myUpdates;
    //cacheList
    private final HashMap<Uri,List<ObjectDocumentFile>> cacheList = new HashMap<>();

    final int NO_OPERATION = 0;
    boolean   isCancelled = false;
    private int OPERATION_TYPE=0;
    private final int OPERATION_COPY = 201;
    private final int OPERATION_MOVE  = 204;
    private final int OPERATION_RENAME = 209;
    private final int OPERATION_DELETE = 212;
    final int OPERATION_INSTALL_APKS = 215;

    private final static String[] doc_projection = {COLUMN_DOCUMENT_ID, COLUMN_DISPLAY_NAME, COLUMN_SIZE,
                                                   COLUMN_LAST_MODIFIED, COLUMN_MIME_TYPE,
                                                   COLUMN_SUMMARY, COLUMN_FLAGS, COLUMN_ICON};

    public FragmentFileManager() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_files_manager, container, false);

        files_rview = view.findViewById(R.id.files_rview);
        files_path_textview = view.findViewById(R.id.files_path_textview);
        files_msg_textview = view.findViewById(R.id.files_msgTextView);
        files_msg_textview.setText(" check");
        storage_spinner = view.findViewById(R.id.files_storage_spinner);
        NoRootOperations = new ClassNoRootUtils[20];
        myUpdates =this;
        ImageButton up_img_button = view.findViewById(R.id.files_image_button_up);
        ImageButton button_add_new = view.findViewById(R.id.files_toolbar_button_add_new);
        Button button_add_tree = view.findViewById(R.id.files_button_add_tree);
        ImageButton button_refresh = view.findViewById(R.id.files_toolbar_button_refresh);
        ImageButton button_properties = view.findViewById(R.id.files_toolbar_button_properties);
        ImageButton button_select = view.findViewById(R.id.files_toolbar_button_select);
        files_progress_bar = view.findViewById(R.id.files_progress_bar);
        bottomAppBar = view.findViewById(R.id.files_bottomAppBar);
        resolver = context.getContentResolver();
        global_sort_by =   ActivityMain.sharedPrefFileSettings.getString(key_sort_by, sort_by_name);
        global_order_by = ActivityMain.sharedPrefFileSettings.getString(key_order_by, order_increasing);

//      toolbar_edittext = v.findViewById(R.id.files_toolbar_edittext);
        fab_action = view.findViewById(R.id.fab_action);
        fab_action.hide();
        fab_action.setOnClickListener(v -> {
            if(OPERATION_TYPE==OPERATION_COPY){
                showMsg(log_msg, "OPERATION_COPY called");
                NEW_TASK_ID++;
                NoRootOperations[NEW_TASK_ID] = new ClassNoRootUtils(context,myUpdates,NEW_TASK_ID);
                NoRootOperations[NEW_TASK_ID].copyDocumentList(selectedFilesList, selected_object_file);
            }else if(OPERATION_TYPE == OPERATION_MOVE){
                showMsg(log_msg, "OPERATION_MOVE called");
                NEW_TASK_ID++;
                NoRootOperations[NEW_TASK_ID] = new ClassNoRootUtils(context,myUpdates,NEW_TASK_ID);
                NoRootOperations[NEW_TASK_ID].moveDocumentList(selectedFilesList, selected_object_file);
            }
            fab_action.hide();
            files_adapter.notifyDataSetChanged();
            selectedFilesList.clear();
            OPERATION_TYPE = 0;
        });

        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(RecyclerView.VERTICAL);
        files_rview.setLayoutManager(llm);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(context, llm.getOrientation());
        files_rview.addItemDecoration(mDividerItemDecoration);

//       Uri primary_uri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3A");
//       if(DocumentsContract.isTreeUri(primary_uri)){
//            ActivityMain.prefEditExternalStorages.putString(primary_uri.getLastPathSegment(),primary_uri.toString()).commit();
//       }

        setSpinnerNewData();

        if(selected_tree_uri != null){
            selected_directory_uri = selected_tree_uri;
            files_path_textview.setText(selected_tree_uri.getPath());
            selected_object_file = getObjectFileFromUri(selected_directory_uri);
            getSetFolderFiles(selected_object_file);
            files_adapter = new AdapterFileManager(this, objectDocumentFileList,getContext());
        }

        bottomAppBar.replaceMenu(R.menu.files_bottom_menu);

        bottomAppBar.getMenu().setGroupVisible(R.id.menugroup_files_permament,true);
        bottomAppBar.getMenu().setGroupVisible(R.id.menugroup_files_option_group,true);
        bottomAppBar.getMenu().setGroupVisible(R.id.menugroup_files_onefile_menu,false);
        bottomAppBar.getMenu().setGroupVisible(R.id.menugroup_files_action_group,false);

        bottomAppBar.setOnMenuItemClickListener(item -> {

            ColorStateList colorStateList = ContextCompat.getColorStateList(context, R.color.color_state);
            if(files_adapter!=null){
                selectedFilesList = files_adapter.getSelectedFiles();
            }

            switch(item.getItemId()){

                case R.id.files_bottom_av_refresh:{
                    setNewFilesAdapter();
                    break;
                }

                case R.id.files_bottom_nav_create_new:{
                    showMsg(log_msg, "Create New Clicked.");
                    new ClassNoRootUtils(context,myUpdates,NEW_TASK_ID++).createNew(selected_object_file);
                    break;
                }
                case R.id.files_bottom_nav_select_all:{
                    selectAll(true);
//                      menuItem.setIconTintList(colorStateList);
                    showMsg(toast_msg, "Selected All");
                    files_adapter.notifyDataSetChanged();
                    break;
                }

                case R.id.files_bottom_nav_unselect_all:{
                    selectAll(false);
                    files_adapter.notifyDataSetChanged();
                    showMsg(toast_msg, "Unselected All");
                    break;
                }

                case R.id.files_bottom_nav_copy:{
                    OPERATION_TYPE = OPERATION_COPY;
                    showMsg(toast_msg, "File Ready to be Copied . Browse to target folder & paste");
                    fab_action.show();
                    files_adapter.selection_mode=false;
                    break;
                }

                case R.id.files_bottom_nav_cut:{
                    OPERATION_TYPE = OPERATION_MOVE;
                    showMsg(toast_msg, "File Ready to be Moved . Browse to target folder & paste");
                    fab_action.show();
                    files_adapter.selection_mode=false;
                    break;
                }

                case R.id.files_bottom_nav_paste:{
                   launchPasteTask();
                   break;
                }

                case R.id.files_bottom_nav_rename:{
                    renameFile();
                    break;
                }

                case R.id.files_bottom_nav_delete:{
                    launchDeleteFilesTask();
                    break;
                }

                case R.id.files_bottom_nav_cancel:{
                    reset_selection();
                    break;
                }

                case R.id.files_bottom_nav_install:{
                   launchInstallApkTask();
                    break;
                }

            }

            return false;
        });

        //--------------------------------SPINNER_ITEM_SELECTION_LISTENER--------------------------------------------
        storage_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                showMsg(log_msg, " Spinner Selection :" +position);
                selected_tree_uri = storages_uri_list.get(position);
                showMsg(log_msg, " Spinner Selection :" +selected_tree_uri.toString());
                ActivityMain.prefEditSettings.putInt(key_storage_spinner_position,position).commit();
                selected_directory_uri = selected_tree_uri;
                selected_object_file = getObjectFileFromUri(selected_tree_uri);
                getSetFolderFiles(selected_object_file);
                files_path_textview.setText(storage_spinner_items.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //-------------------------------------UP_BUTTON_FUNCTIONALITY---------------------------------------------
        up_img_button.setOnClickListener(v -> {
            Uri parent_uri = selected_object_file.parent_uri;
            Log.i(TAG," UP_BUTTON_CLICK : parentUri :" + parent_uri);

            if(parent_uri != null){
                selected_object_file = getObjectFileFromUri(parent_uri);
                Log.i(TAG," UP_BUTTON_CLICK : NEW_OBJECTFILE SET :" +selected_object_file.uri);
                getSetFolderFiles(selected_object_file);
            }else{
                showMsg(toast_msg,"You are at the root of the tree.. Cannoy go up !!");
            }
        });


//        -----------------------------BACK_BUTTON_ACTION---------------------------------------------------------
        view.setOnKeyListener((v, keyCode, event) -> {
            Log.i(TAG, "keyCode: " + keyCode);
            if( keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                Log.i(TAG, "onKey Back listener is working!!!");
//                    getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                  selected_object_file = getObjectFileFromUri(selected_object_file.parent_uri);
                  getSetFolderFiles(selected_object_file);
                return true;
            }
            return false;
        });

        button_add_tree.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
            i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
            i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
            startActivityForResult(i, REQUEST_CODE_EXTERNAL_STORAGE);
        });

        return view;
    }

    private void setSpinnerNewData(){
        storage_spinner_items = new ArrayList<>();
        storages_uri_list = new ArrayList<>();

            //--------------------------------Getting ALL TREES--------------------------------------------
            Map<String,?> storage_uri_strings = ActivityMain.sharedPrefExternalStorages.getAll();
            for(Map.Entry<String,?> str : storage_uri_strings.entrySet()){
                Uri temp_uri = Uri.parse(str.getValue().toString());

                if(DocumentsContract.isTreeUri(temp_uri)){
                    showMsg(log_msg,"Tree Uri : "+temp_uri);
                    storages_uri_list.add(temp_uri);
                }else{
                    showMsg(log_msg,"Not tree uri : "+temp_uri);
                }

            }
            
            //------------------------SET_STORAGE_SPINNER_ITEMS--------------------------------------------
            if(storages_uri_list !=null & storages_uri_list.size()>0){
                for(Uri uri: storages_uri_list){
                    storage_spinner_items.add(uri.getLastPathSegment());
                }
            }

        ArrayAdapter<String> spinner_adapter = new ArrayAdapter<>(context, R.layout.listitem_storage_spinner, R.id.spinner_item, storage_spinner_items);
            storage_spinner.setAdapter(spinner_adapter);

        int value_storage_spinner_position = ActivityMain.sharedPrefSettings.getInt(key_storage_spinner_position, 0);

            if(value_storage_spinner_position <= storages_uri_list.size()){
                storage_spinner.setSelection(value_storage_spinner_position);
            }
    }

    private void setNewFilesAdapter(){
        SortFileList(objectDocumentFileList);
        files_adapter = new AdapterFileManager(this, objectDocumentFileList, context);
        files_rview.setAdapter(files_adapter);
        //files_adapter.notifyDataSetChanged();
        if(cacheList.containsKey(selected_object_file.uri)){
            cacheList.put(selected_object_file.uri, objectDocumentFileList);
        }
        showDefaultMsg(textview_msg);
    }

    private ObjectDocumentFile getObjectFileFromUri(Uri uri){
        showMsg(log_msg,"---------------------------GETOBJECTFILEFROMURI(Uri)-----BEGIN-----------------------");
        ObjectDocumentFile objectDocumentFile = null;
        Uri objectDocUri = null;

        if(DocumentsContract.isTreeUri(uri) & DocumentsContract.isDocumentUri(context,uri)){
            objectDocUri = uri;
//            showMsg(log_msg,"Uri Received : "+uri);
//            showMsg(log_msg,"isDocumentUri : "+DocumentsContract.isDocumentUri(context,uri));
//            showMsg(log_msg,"isTreeUri     : "+DocumentsContract.isTreeUri(uri));
//            showMsg(log_msg,"DocumentId    : "+DocumentsContract.getTreeDocumentId(uri));
        }
        else if(DocumentsContract.isTreeUri(uri) & !DocumentsContract.isDocumentUri(context,uri)){
            objectDocUri = DocumentsContract.buildDocumentUriUsingTree(uri,DocumentsContract.getTreeDocumentId(uri));
//            showMsg(log_msg,"DocumentUriFromTree (treeUri,treeDocId) : "+objectDocUri);
//            showMsg(log_msg,"isDocumentUri : "+DocumentsContract.isDocumentUri(context,objectDocUri));
//            showMsg(log_msg,"isTreeUri     : "+DocumentsContract.isTreeUri(objectDocUri));
//            showMsg(log_msg,"DocumentId    : "+DocumentsContract.getDocumentId(objectDocUri));
        }
        else if(!DocumentsContract.isTreeUri(uri) & DocumentsContract.isDocumentUri(context,uri)){
            objectDocUri = DocumentsContract.buildDocumentUri(uri.getAuthority(),uri.getLastPathSegment());
//            showMsg(log_msg,"DocumentUri(Authority,Segment)     : "+objectDocUri);
//            showMsg(log_msg,"isDocumentUri : "+DocumentsContract.isDocumentUri(context,objectDocUri));
//            showMsg(log_msg,"isTreeUri     : "+DocumentsContract.isTreeUri(objectDocUri));
//            showMsg(log_msg,"DocumentId    : "+DocumentsContract.getDocumentId(objectDocUri));
        }

        showMsg(log_msg,"Built objectUri Value =  "+objectDocUri);

        Cursor c = resolver.query(objectDocUri,doc_projection,null,null,null);
        if (c != null) {
            c.moveToFirst();
            objectDocumentFile = new ObjectDocumentFile(c,objectDocUri,context);
        }
        
        showMsg(log_msg,"objectDocumentFile : "+ objectDocumentFile.uri);
       // showMsg(log_msg,"---------------------------GETOBJECTFILEFROMURI-----END-----------------------");
     //   showMsg(log_msg,"\n                                                                                  -");

        return objectDocumentFile;
    }

    private void showMsg(String msgtype, String str){

        if(null == str || str.isEmpty()){
            showDefaultMsg(msgtype);
        }else{
            if(msgtype.equals(toast_msg)){
                Toast.makeText(context,str,Toast.LENGTH_SHORT).show();
                Log.v(TAG,str);
            }else if(msgtype.equals(textview_msg)){
                files_msg_textview.setText(str);
                Log.v(TAG,str);
            }else if(msgtype.equals(log_msg)){
                Log.i(TAG,str);
            }
        }
    }

    private void showDefaultMsg(String type){
       //String msg1 = "Total Files : "+ objectDocumentFileList.size()+" Files Selected :"+files_adapter.getSelectedFiles().size();
        String msg = "Total Files : "+ objectDocumentFileList.size()+" Files Selected :"+files_adapter.selected_count;

        Log.i(TAG, "in DEFAULT_MSG : ")    ;

         if(type.equals(textview_msg)){
            files_msg_textview.setText(msg);
            Log.v(TAG, msg);
        }else if(type.equals(toast_msg)){
            Toast.makeText(context,msg,Toast.LENGTH_SHORT).show();
            Log.v(TAG,msg);
        }else if(type.equals(log_msg)){
             Log.i(TAG,msg);
         }
    }

    private void selectAll(boolean enable){

        if(null!=objectDocumentFileList & !objectDocumentFileList.isEmpty()){
            for(ObjectDocumentFile obj : objectDocumentFileList){
                obj.check_box_state = enable;
            }
            files_adapter.selection_mode = enable;
            setNewFilesAdapter();
        }else{
           showMsg(toast_msg, "No Files Loaded.");
        }

    }

    private void getSetFolderFiles(ObjectDocumentFile obj){

        selected_object_file = obj;
        showMsg(log_msg,"SELECTED_OBJECT_FILE : "+selected_object_file.uri);
        files_path_textview.setText(selected_object_file.uri.getLastPathSegment());

        if(cacheList.containsKey(obj.uri)){
            objectDocumentFileList = cacheList.get(obj.uri);
            selectAll(false);
            showMsg(log_msg, " Found "+obj.uri+" in Cache... Loading list size : "+objectDocumentFileList.size());
            setNewFilesAdapter();
        }else{
              launchNewFilesTask(obj);
        }

    }

    private void launchNewFilesTask(ObjectDocumentFile obj){

        selected_object_file = obj;
        
        if(loadFilesTask!=null){
            loadFilesTask.cancel(true);
            loadFilesTask = null;
        }

        showMsg(log_msg, " Uri Not in Cache... Loading  Fresh : "+selected_object_file.uri);

        loadFilesTask = new LoadFilesAsyncTask(this,selected_object_file.uri);
        loadFilesTask.execute();
    }

    private void launchPasteTask(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if(OPERATION_TYPE == OPERATION_COPY){
            builder.setTitle("Confirm the Operation");
            builder.setMessage("Copy "+selectedFilesList.size()+" files to "+selected_object_file.file_name);
            builder.setPositiveButton("Confirm", (dialog, which) -> {
                    showMsg(log_msg, "OPERATION_COPY called");
                    NEW_TASK_ID++;
                    NoRootOperations[NEW_TASK_ID] = new ClassNoRootUtils(context,myUpdates,NEW_TASK_ID);
                    NoRootOperations[NEW_TASK_ID].copyDocumentList(selectedFilesList, selected_object_file);
            });

        }else if(OPERATION_TYPE == OPERATION_MOVE){
            builder.setTitle("Confirm the Operation");
            builder.setMessage("Move "+selectedFilesList.size()+" files to "+selected_object_file.file_name);
            builder.setPositiveButton("Confirm", (dialog, which) -> {
                showMsg(log_msg, "OPERATION_MOVE called");
                NEW_TASK_ID++;
                NoRootOperations[NEW_TASK_ID] = new ClassNoRootUtils(context,myUpdates,NEW_TASK_ID);
                NoRootOperations[NEW_TASK_ID].moveDocumentList(selectedFilesList, selected_object_file);
            });
        }

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
        fab_action.hide();
        files_adapter.notifyDataSetChanged();
    }

    private  void launchInstallApkTask(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm the Operation");
        builder.setMessage("Are You Sure to install "+selectedFilesList.size()+" files.");
        builder.setPositiveButton("Confirm", (dialog, which) -> {
            showMsg(log_msg, "OPERATION_APK_INSTALLATION called");
            NEW_TASK_ID++;
            NoRootOperations[NEW_TASK_ID] = new ClassNoRootUtils(context,myUpdates,NEW_TASK_ID);
            NoRootOperations[NEW_TASK_ID].installApkList(selectedFilesList);
        });
        builder.setNegativeButton("Cancel Operation", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void launchDeleteFilesTask(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm Delete Operation");
        builder.setMessage("Delete "+selectedFilesList.size()+" files from "+selected_object_file.file_name);
        builder.setPositiveButton("Yes", (dialog, which) -> {
            OPERATION_TYPE = OPERATION_DELETE;
            NEW_TASK_ID++;
            NoRootOperations[NEW_TASK_ID] = new ClassNoRootUtils(context,myUpdates,NEW_TASK_ID);
            NoRootOperations[NEW_TASK_ID].deleteDocumentList(selectedFilesList);
            selectedFilesList.clear();
            OPERATION_TYPE = 0;
        });
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void renameFilesTask(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm Renaming Files");
        builder.setMessage("Are You Sure to rename "+selectedFilesList.size()+selected_object_file.file_name);
        builder.setPositiveButton("Confirm", (dialog, which) -> {

        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void reset_selection(){
        selectAll(false);
        if(null!=selectedFilesList){
            selectedFilesList.clear();
        }
        files_adapter.selection_mode=false;
        files_adapter.notifyDataSetChanged();
        OPERATION_TYPE = 0;
    }

    private void renameFile(){
        if(selectedFilesList.size()==1){
            OPERATION_TYPE = OPERATION_RENAME;
            new ClassNoRootUtils(context,this,NEW_TASK_ID++).renameDocument(selectedFilesList.get(0));
            showMsg(textview_msg, " Renaming "+selectedFilesList.get(0).file_name);
            OPERATION_TYPE = 0;
        }
        files_adapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE_EXTERNAL_STORAGE) {
            if (resultCode == RESULT_OK) {
                Uri uri_extsd = data.getData();
                ActivityMain.prefEditExternalStorages.putString(uri_extsd.getLastPathSegment(), uri_extsd.toString()).commit();
                showMsg(log_msg, "Added : " + uri_extsd.getPath());
                setSpinnerNewData();
                // Persist access permissions.
                final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getActivity().getContentResolver().takePersistableUriPermission(uri_extsd, takeFlags);
            }
        }
//        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void openDocument(ObjectDocumentFile obj) {

        showMsg(log_msg,"Received Clicked Document from RecyclerViewAdapterOnClick : " +obj.file_name);

        if(obj.isDirectory){
            getSetFolderFiles(obj);
        }
        else{
            if(obj.file_name.endsWith(".apk")){
                Intent i = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                i.setType("application/vnd.android.package-archive");
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                i.setData(obj.uri);
                context.startActivity(i);
            }else{
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                i.setData(obj.uri);
                context.startActivity(i);
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
    public void enableActionOptionsInBottombar(boolean selection_mode) {

        if (selection_mode){
            bottomAppBar.getMenu().setGroupVisible(R.id.menugroup_files_permament,true);
            bottomAppBar.getMenu().setGroupVisible(R.id.menugroup_files_option_group,false);
            bottomAppBar.getMenu().setGroupVisible(R.id.menugroup_files_action_group,true);
            bottomAppBar.getMenu().findItem(R.id.files_bottom_nav_paste).setVisible(false);
            if(files_adapter.selected_count==1){
                 bottomAppBar.getMenu().setGroupVisible(R.id.menugroup_files_onefile_menu,true);
             }else{
                 bottomAppBar.getMenu().setGroupVisible(R.id.menugroup_files_onefile_menu,false);
             }
        } else {
            bottomAppBar.getMenu().setGroupVisible(R.id.menugroup_files_permament,true);
            bottomAppBar.getMenu().setGroupVisible(R.id.menugroup_files_option_group,true);
            bottomAppBar.getMenu().setGroupVisible(R.id.menugroup_files_onefile_menu,false);
            bottomAppBar.getMenu().setGroupVisible(R.id.menugroup_files_action_group,false);
        }
    }

    @Override
    public void updateTextView(String str) {
       showMsg(textview_msg, str);
    }

    @Override
    public void refreshList(boolean refresh) {
        if(refresh){
            files_adapter.notifyDataSetChanged();
        }else{
            setNewFilesAdapter();
        }
    }

    @Override
    public void setprogressBar(boolean visible) {

    }

    @Override
    public void taskCompletedAction(int type, ObjectDocumentFile targetObj) {

//        if(selected_object_file.uri.equals(targetObj.uri)){
//           launchNewFilesTask(targetObj);
//        }

//        if(cacheList.containsKey(targetObj.uri)){
//            cacheList.remove(targetObj.uri);
//        }
        showMsg(log_msg, "in TaskCompletedAction : "+type);

        if(type == OPERATION_RENAME || type== OPERATION_COPY || type == OPERATION_DELETE || type== OPERATION_MOVE){
            cacheList.remove(targetObj.uri);
            
            launchNewFilesTask(selected_object_file);
            showMsg(log_msg, "Operation Completed Reloading "+selected_object_file.file_name);
        }

    }

    @Override
    public void cancelOperationById(int id) {
        NoRootOperations[id].cancelTask();
    }


    class LoadFilesAsyncTask extends AsyncTask<Void,String,Void> {

        final Uri local_search_uri;
        final AdapterFileManager.AdapterCallBack callBack;
        int count;
        final String TAG = " LOADFILESASYNCTASK : ";

        LoadFilesAsyncTask(AdapterFileManager.AdapterCallBack callBack, Uri uri) {
            super();
            local_search_uri = uri;
            this.callBack = callBack;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            objectDocumentFileList = new ArrayList<>();
            files_progress_bar.setVisibility(View.VISIBLE);
            count = 0 ;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            getChildrenList(local_search_uri);
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            showMsg(values[0], values[1]);

        }

        @Override
        protected void onPostExecute(Void voids) {
            super.onPostExecute(voids);
            setNewFilesAdapter();
            cacheList.put(local_search_uri, objectDocumentFileList);
            showMsg(log_msg, " Cache added of : "+local_search_uri);
            files_progress_bar.setVisibility(View.GONE);
            showMsg(textview_msg,"");
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            setNewFilesAdapter();
            files_progress_bar.setVisibility(View.GONE);
        }

        void getChildrenList(Uri search_uri){
           List<Uri> childeren_uri_list =  new ArrayList<>();
           Uri children_tree_uri = null;

            // publishProgress(log_msg,"isTreeUri : "+DocumentsContract.isTreeUri(search_uri));

            //  publishProgress(log_msg,"CHILDREN_TREE_URI : "+children_tree_uri);

            publishProgress(log_msg,"------------------------------getChildrenList : Begin----------------------------------------- ");
            publishProgress(log_msg,"Search_uri : "+search_uri);
            if(DocumentsContract.isTreeUri(search_uri)){
                children_tree_uri = DocumentsContract.buildChildDocumentsUriUsingTree(search_uri,DocumentsContract.getDocumentId(search_uri));
            }else{
                children_tree_uri = DocumentsContract.buildChildDocumentsUriUsingTree(search_uri,DocumentsContract.getTreeDocumentId(search_uri));
            }
            try (Cursor cursor = resolver.query(children_tree_uri, doc_projection, null, null,
                    COLUMN_DISPLAY_NAME + " DESC")) {
                publishProgress(log_msg, "isCursorNull : " + (cursor == null));
                count = 0;
                while (cursor.moveToNext()) {
                    final String doc_id = cursor.getString(0);
                    final Uri child_uri = DocumentsContract.buildDocumentUriUsingTree(search_uri, doc_id);
                    objectDocumentFileList.add(new ObjectDocumentFile(cursor, child_uri, context));
                    if (isCancelled()) {
                        break;

                    }
                    childeren_uri_list.add(child_uri);
                    // publishProgress(textview_msg,"Loading : "+(count++)+" : "+child_uri.getLastPathSegment());
                }
            } catch (Exception ex) {
                publishProgress(log_msg, "Error Searching , Exception : " + ex);
            }
        //  publishProgress(textview_msg,"SELECTED_OBJECT_FILE : END :  "+ selected_object_file.uri);
        //  publishProgress(log_msg,"------------------------------getChildrenList : End----------------------------------------- ");
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_files,menu);
        menu.findItem(R.id.menuitem_files_copy_files).setVisible(false);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.menuitem_files_paste).setChecked(ActivityMain.sharedPrefFileSettings
                .getBoolean(key_show_hidden,false));

        if(files_adapter!=null){
            selectedFilesList = files_adapter.getSelectedFiles()  ;
            if(selectedFilesList.size()>0){
                menu.setGroupVisible(R.id.menugroup_files_action_group, true);
                if(OPERATION_TYPE==1 || OPERATION_TYPE==2){
                    menu.findItem(R.id.menuitem_files_paste).setVisible(true);
                    menu.findItem(R.id.menuitem_files_paste).setTitleCondensed("Paste files ("+selectedFilesList.size()+" files)");
                }

//                for(ObjectDocumentFile obj:selectedFilesList){
//                    if(obj.file_name.endsWith(".apk")){
//                        menu.findItem(R.id.menuitem_files_install);
//                        break;
//                    }
//                }
             
            }else{
                menu.setGroupVisible(R.id.menugroup_files_action_group, false);
                menu.findItem(R.id.menuitem_files_paste).setVisible(false);
            }
        }

        String value_sort_by = ActivityMain.sharedPrefFileSettings.getString(key_sort_by, sort_by_name);
        String value_order_by = ActivityMain.sharedPrefFileSettings.getString(key_order_by, order_increasing);
        showMsg(log_msg," Default Sorting Found :"+ value_sort_by);
        showMsg(log_msg," Default Order Found :"+ value_order_by);


        switch(value_sort_by){

            case sort_by_name:{
                global_sort_by = sort_by_name;
                menu.findItem(R.id.menuitem_files_sortbyname).setChecked(true);
                break;
            }

            case sort_by_date:{
                global_sort_by = sort_by_date;
                menu.findItem(R.id.menuitem_files_sortbydate).setChecked(true);
                break;
            }

            case sort_by_size:{
                global_sort_by = sort_by_size;
                menu.findItem(R.id.menuitem_files_sortbysize).setChecked(true);
                break;
            }

            case sort_by_type:{
                global_sort_by = sort_by_type;
                menu.findItem(R.id.menuitem_files_sortbytype).setChecked(true);
                break;
            }
        }

        switch(value_order_by){

            case order_increasing: {
                global_order_by = order_increasing;
                menu.findItem(R.id.menuitem_files_increasing).setChecked(true);
                break;
            }

            case order_decreasing: {
                global_order_by = order_decreasing;
                menu.findItem(R.id.menuitem_files_decreasing).setChecked(true);
                break;
            }

        }

        menu.findItem(R.id.menuitem_files_hidden).
                setChecked(ActivityMain.sharedPrefFileSettings.getBoolean(key_show_hidden,true));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(files_adapter!=null){
            selectedFilesList = files_adapter.getSelectedFiles();
        }

        switch(item.getItemId()){

//----------------------------------------LOAD FILES-------------------------------------------------------
            case R.id.menuitem_files_load:{
                launchNewFilesTask(selected_object_file);
                break;
            }

//----------------------------------------REFRESH_LIST-------------------------------------------------------
//            case R.id.menuitem_files_refresh:{
//                files_adapter.notifyDataSetChanged();
//                break;
//            }
            
//----------------------------------------RENAME_FILE------------------------------------------------------
            case R.id.menuitem_files_rename:{
                renameFile();
                break;
            }

//---------------------------------------SHOW_HIDDEN_FILES--------------------------------------------
            case R.id.menuitem_files_hidden: {
                item.setChecked(!item.isChecked());
                boolean showHidden = item.isChecked();
                ActivityMain.prefEditFileSettings.putBoolean(key_show_hidden, item.isChecked()).commit();
                launchNewFilesTask(selected_object_file);
                break;
            }

//----------------------------------------COPY_FILES------------------------------------------------------
            case R.id.menuitem_files_copy_files:{

                OPERATION_TYPE = OPERATION_COPY;
                //fab_action.setVisibility(View.VISIBLE);
                fab_action.show();
                files_adapter.selection_mode=false;
                showMsg(textview_msg, selectedFilesList.size()+" files selected.Now Browse to a folder to Copy ");

                break;
            }

//----------------------------------------MOVE_FILES-------------------------------------------------------
            case R.id.menuitem_files_move_files:{
                if(files_adapter!=null){
                    selectedFilesList = files_adapter.getSelectedFiles();
                }
                OPERATION_TYPE = OPERATION_MOVE;
                fab_action.show();
                files_adapter.selection_mode=false;
                showMsg(textview_msg, selectedFilesList.size()+" files selected. Browse to a folder to Move");
                break;
            }

//----------------------------------------PASTE_FILES-------------------------------------------------------
            case R.id.menuitem_files_paste:{
                launchPasteTask();
                break;
            }

//----------------------------------------DELETE_FILES-------------------------------------------------------
            case R.id.menuitem_files_delete:{
                launchDeleteFilesTask();
                break;
            }

//----------------------------------INSTALL_APKS-----------------------------------------------------------
            case R.id.menuitem_install:{
                launchInstallApkTask();
            }
//-----------------------------------FILE_SELECTION------------------------------------------------------
            case R.id.menuitem_files_select_all:{
                showMsg(log_msg, " SelectAll Clicked");
                selectAll(true);
                showDefaultMsg(textview_msg);
                break;
            }

            case R.id.menuitem_files_unselect_all:{
                showMsg(log_msg, " UnSelectAll Clicked");
                selectAll(false);
                showDefaultMsg(textview_msg);
                break;
            }

            case R.id.menuitem_files_select_invert:{
                objectDocumentFileList.forEach(obj-> obj.check_box_state = !obj.check_box_state);

                if(files_adapter.selected_count==0){
                    files_adapter.selection_mode=false;
                }

                setNewFilesAdapter();
                showDefaultMsg(textview_msg);

                break;
            }

//----------------------------------------SORT_BY-------------------------------------------------------
            case R.id.menuitem_files_sortbyname:{
                global_sort_by = sort_by_name;
                if(!item.isChecked()){
                    item.setChecked(true);
                }
                SortFileList(objectDocumentFileList);
                files_adapter.notifyDataSetChanged();
                ActivityMain.prefEditFileSettings.putString(key_sort_by, global_sort_by).commit();
                break;
            }

            case R.id.menuitem_files_sortbydate:{
                global_sort_by = sort_by_date;
                if(!item.isChecked()){
                    item.setChecked(true);
                }
                SortFileList(objectDocumentFileList);
                files_adapter.notifyDataSetChanged();
                ActivityMain.prefEditFileSettings.putString(key_sort_by, global_sort_by).commit();
                break;
            }

            case R.id.menuitem_files_sortbysize:{
                global_sort_by = sort_by_size;
                if(!item.isChecked()){
                    item.setChecked(true);
                }
                SortFileList(objectDocumentFileList);
                ActivityMain.prefEditFileSettings.putString(key_sort_by, global_sort_by).commit();
                files_adapter.notifyDataSetChanged();
                break;
            }

            case R.id.menuitem_files_sortbytype:{
                global_sort_by = sort_by_type;
                if(!item.isChecked()){
                    item.setChecked(true);
                }
                SortFileList(objectDocumentFileList);
                ActivityMain.prefEditFileSettings.putString(key_sort_by, global_sort_by).commit();
                files_adapter.notifyDataSetChanged();
                break;
            }

//-----------------------------------ORDER_BY-------------------------------------------------------
            case R.id.menuitem_files_increasing:{
                global_order_by = order_increasing;
                if(!item.isChecked()){
                    item.setChecked(true);
                }
                SortFileList(objectDocumentFileList);
                files_adapter.notifyDataSetChanged();
                ActivityMain.prefEditFileSettings.putString(key_order_by, global_order_by).commit();
                break;
            }

            case R.id.menuitem_files_decreasing:{
                global_order_by = order_decreasing;
                if(!item.isChecked()){
                    item.setChecked(true);
                }
                SortFileList(objectDocumentFileList);
                files_adapter.notifyDataSetChanged();
                ActivityMain.prefEditFileSettings.putString(key_order_by, global_order_by).commit();
                break;
            }



        }
        return true;
    }

    private void SortFileList(List to_sort_list) {
        Comparator<ObjectDocumentFile> file_name_comparator = 
                (ObjectDocumentFile l1, ObjectDocumentFile l2) -> l1.file_name.toLowerCase().compareTo(l2.file_name.toLowerCase());
        Comparator<ObjectDocumentFile> file_size_comparator = 
                (ObjectDocumentFile l1, ObjectDocumentFile l2) -> Long.compare(l1.size_long, l2.size_long);
        Comparator<ObjectDocumentFile> modified_date_comparator = 
                (ObjectDocumentFile l1, ObjectDocumentFile l2) -> Long.compare(l1.time_long, l2.time_long);
        Comparator<ObjectDocumentFile> file_type_comparator =
                (ObjectDocumentFile l1, ObjectDocumentFile l2) -> l1.file_type.compareTo(l2.file_type);

        Log.i(TAG, " In SortRootFileList : sort by value = " + global_sort_by + " order by value = " + global_order_by);

        switch(global_sort_by) {
            case sort_by_name:{
                Collections.sort(to_sort_list, file_name_comparator);

                if (global_order_by.equals(order_decreasing)) {
                    Collections.reverse(to_sort_list);
                }

                Log.i(TAG, "in sort_by_name");
                break;
            }

            case sort_by_date: {
                Collections.sort(to_sort_list, modified_date_comparator);
                if (global_order_by.equals(order_decreasing)) {
                    Collections.reverse(to_sort_list);
                }
                Log.i(TAG, "in sort_by_date");
                break;
            }

            case sort_by_size: {
                Collections.sort(to_sort_list, file_size_comparator);
                if (global_order_by.equals(order_decreasing)) {
                    Collections.reverse(to_sort_list);
                }
                Log.i(TAG, "in sort_by_size");
                break;
            }

            case sort_by_type: {
                Collections.sort(to_sort_list, file_type_comparator);
                if (global_order_by.equals(order_decreasing)) {
                    Collections.reverse(to_sort_list);
                }
                Log.i(TAG, "in sort_by_size");
                break;
            }

            default: {
                Collections.sort(to_sort_list, file_name_comparator);
                break;
            }
        }

        objectDocumentFileList = to_sort_list;
    }
    
    @Override
    public void onStart() {
        super.onStart();
        if(null!=selected_object_file) {
            launchNewFilesTask(selected_object_file);
        }
    }
    
}

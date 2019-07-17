package com.sunnykatiyar.appmanager;


import android.content.ContentResolver;
import android.content.Context;
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
public class FragmentFileManager extends Fragment implements AdapterFileManager.MyCallBack,
        ClassNoRootUtils.TextViewUpdateInterface, ActivityOperations.CancelOperation {

    private Context context;

    private final String TAG = "MYAPP : FRAGMENT_FILES : ";
    private final String textview_msg ="textview_msg";
    private final String log_msg = "log_msg";
    private final String toast_msg ="toast_msg";
    final int REQUEST_CODE_EXTERNAL_STORAGE = 677;
    ArrayAdapter<String> spinner_adapter;

    private static final String key_storage_spinner_position = "STORAGE_SPINNER_SELECTED_POSITION";
    private int value_storage_spinner_position;
    int NEW_TASK_ID = 0;
    RecyclerView files_rview;
    TextView files_path_textview;
    TextView files_msg_textview;
    Spinner storage_spinner;
    ProgressBar files_progress_bar;
    Button button_add_tree;
    Uri selected_tree_uri;
    Uri selected_directory_uri;
    ObjectDocumentFile selected_object_file;

    boolean showHidden = false;
    final String key_show_hidden = "SHOW_HIDDEN_FILES";
    String value_show_hidden;
    List<Uri> storages_uri_list;
    List<String> storage_spinner_items;

    List<ObjectDocumentFile> objectDocumentFileList;
    List<ObjectDocumentFile> selectedFilesList;

    AdapterFileManager files_adapter;
    LoadFilesAsyncTask loadFilesTask;
    ImageButton up_img_button;
    ImageButton button_add_new;
    ImageButton button_refresh;
    ImageButton button_properties;
    ImageButton button_select;
    EditText toolbar_edittext;
    ContentResolver resolver ;

    FloatingActionButton fab_action;
    BottomAppBar bottomAppBar;

    final String key_sort_by = "SORT_BY";
    final String key_order_by = "ORDER_BY";
    String value_sort_by;
    String value_order_by;

    final String sort_by_date = "SORT_BY_DATE";
    final String sort_by_size = "SORT_BY_SIZE";
    final String sort_by_name = "SORT_BY_NAME";
    final String sort_by_type = "SORT_BY_TYPE";
    final String order_increasing = "ORDER_INCREASING";
    final String order_decreasing = "ORDER_DECREASING";

    String global_sort_by;
    String global_order_by;

    public static ClassNoRootUtils[] NoRootOperations;
    ClassNoRootUtils.TextViewUpdateInterface myUpdates;
    //cacheList
    HashMap<Uri,List<ObjectDocumentFile>> cacheList = new HashMap<>();

    private int SELECTED_OPERATION = 0;
    private int OPERATION_COPY = 101;
    private int OPERATION_DELETE = 102;
    private int OPERATION_MOVE = 103;
    private int OPERATION_RENAME = 104;

    public final static String[] doc_projection = {COLUMN_DOCUMENT_ID, COLUMN_DISPLAY_NAME, COLUMN_SIZE,
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
        storage_spinner = view.findViewById(R.id.files_storage_spinner);
        NoRootOperations = new ClassNoRootUtils[20];
        files_msg_textview = view.findViewById(R.id.files_msg_text_view);
        myUpdates =this;
        up_img_button = view.findViewById(R.id.files_image_button_up);
        button_add_new =view.findViewById(R.id.files_toolbar_button_add_new);
        button_add_tree =view.findViewById(R.id.files_button_add_tree);
        button_refresh =view.findViewById(R.id.files_toolbar_button_refresh);
        button_properties = view.findViewById(R.id.files_toolbar_button_properties);
        button_select = view.findViewById(R.id.files_toolbar_button_select);
        files_progress_bar = view.findViewById(R.id.files_progress_bar);
        bottomAppBar = view.findViewById(R.id.files_bottomAppBar);
        resolver = context.getContentResolver();
        global_sort_by =   ActivityMain.sharedPrefFileSettings.getString(key_sort_by, sort_by_name);
        global_order_by = ActivityMain.sharedPrefFileSettings.getString(key_order_by, order_increasing);

//      toolbar_edittext = v.findViewById(R.id.files_toolbar_edittext);
        fab_action = view.findViewById(R.id.fab_action);
        fab_action.hide();
        fab_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(SELECTED_OPERATION==OPERATION_COPY){
                    showMsg(log_msg, "OPERATION_COPY called");
                    NEW_TASK_ID++;
                    NoRootOperations[NEW_TASK_ID] = new ClassNoRootUtils(context,myUpdates,NEW_TASK_ID);
                    NoRootOperations[NEW_TASK_ID].copyDocumentList(selectedFilesList, selected_object_file);
                }else if(SELECTED_OPERATION == OPERATION_MOVE){
                    showMsg(log_msg, "OPERATION_MOVE called");
                    NEW_TASK_ID++;
                    NoRootOperations[NEW_TASK_ID] = new ClassNoRootUtils(context,myUpdates,NEW_TASK_ID);
                    NoRootOperations[NEW_TASK_ID].moveDocumentList(selectedFilesList, selected_object_file);
                }
                fab_action.hide();
                files_adapter.notifyDataSetChanged();
                selectedFilesList.clear();
                SELECTED_OPERATION = 0;
            }
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

        bottomAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                ColorStateList colorStateList = ContextCompat.getColorStateList(context, R.color.color_state);

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
//                        bottomAppBar.getMenu().findItem(R.id.menuitem_files_select_all).setVisible(true);
//                        bottomAppBar.getMenu().findItem(R.id.menuitem_files_unselect_all).setVisible(false);
                        break;
                    }

                    case R.id.files_bottom_nav_copy:{
                        if(files_adapter!=null){
                            selectedFilesList = files_adapter.getSelectedFiles();
                        }
                        SELECTED_OPERATION = OPERATION_COPY;
                        showMsg(toast_msg, "File Ready to be Copied . Browse to target folder & paste");
                        fab_action.show();
                        files_adapter.selection_mode=false;
                        showMsg(textview_msg, selectedFilesList.size()+" files selected.Now Browse to a folder to Copy ");
                        break;
                    }

                    case R.id.files_bottom_nav_cut:{
                        if(files_adapter!=null){
                            selectedFilesList = files_adapter.getSelectedFiles();
                        }
                        SELECTED_OPERATION = OPERATION_MOVE;
                        showMsg(toast_msg, "File Ready to be Moved . Browse to target folder & paste");
                        fab_action.show();
                        files_adapter.selection_mode=false;
                        showMsg(textview_msg, selectedFilesList.size()+" files selected. Browse to a folder to Move");
                        fab_action.setImageResource(R.drawable.ic_content_paste_white_36dp);
                        break;
                    }

                    case R.id.files_bottom_nav_paste:{
                        if(SELECTED_OPERATION==OPERATION_COPY){
                            showMsg(log_msg, "OPERATION_COPY called");
                            NEW_TASK_ID++;
                            NoRootOperations[NEW_TASK_ID] = new ClassNoRootUtils(context,myUpdates,NEW_TASK_ID);
                            NoRootOperations[NEW_TASK_ID].copyDocumentList(selectedFilesList, selected_object_file);
                        }else if(SELECTED_OPERATION == OPERATION_MOVE){
                            showMsg(log_msg, "OPERATION_MOVE called");
                            NEW_TASK_ID++;
                            NoRootOperations[NEW_TASK_ID] = new ClassNoRootUtils(context,myUpdates,NEW_TASK_ID);
                            NoRootOperations[NEW_TASK_ID].moveDocumentList(selectedFilesList, selected_object_file);
                        }
                        fab_action.hide();
                        files_adapter.notifyDataSetChanged();
                        break;
                    }

                    case R.id.files_bottom_nav_delete:{
                        if(files_adapter!=null){
                            selectedFilesList = files_adapter.getSelectedFiles();
                        }
                        SELECTED_OPERATION = OPERATION_DELETE;
                        NEW_TASK_ID++;
                        NoRootOperations[NEW_TASK_ID] = new ClassNoRootUtils(context,myUpdates,NEW_TASK_ID);
                        NoRootOperations[NEW_TASK_ID].deleteDocumentList(selectedFilesList);
                        if(null!=selectedFilesList){
                            selectedFilesList.clear();
                        }
                        files_adapter.notifyDataSetChanged();
                        SELECTED_OPERATION = 0;
                        break;
                    }

                    case R.id.files_bottom_nav_cancel:{
                        selectAll(false);
                        if(null!=selectedFilesList){
                            selectedFilesList.clear();
                        }
                        files_adapter.notifyDataSetChanged();
                        files_adapter.selection_mode=false;
                        SELECTED_OPERATION = 0;
                        break;
                    }

                    case R.id.files_bottom_nav_install:{
                        selectAll(false);
                        if(null!=selectedFilesList){
                            selectedFilesList = files_adapter.getSelectedFiles();
                        }
                         NEW_TASK_ID++;
                        NoRootOperations[NEW_TASK_ID] = new ClassNoRootUtils(context, myUpdates);
                        NoRootOperations[NEW_TASK_ID].installApks(selectedFilesList);
                        files_adapter.notifyDataSetChanged();
                        files_adapter.selection_mode=false;
                        SELECTED_OPERATION = 0;
                        break;
                    }


                }
                
                return false;
            }
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
        up_img_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri parent_uri = selected_object_file.parent_uri;
                Log.i(TAG," UP_BUTTON_CLICK : parentUri :" + parent_uri);

                if(parent_uri != null){
                    selected_object_file = getObjectFileFromUri(parent_uri);
                    Log.i(TAG," UP_BUTTON_CLICK : NEW_OBJECTFILE SET :" +selected_object_file.uri);
                    getSetFolderFiles(selected_object_file);
                }else{
                    showMsg(toast_msg,"You are at the root of the tree.. Cannoy go up !!");
                }
            }
        });


//        -----------------------------BACK_BUTTON_ACTION---------------------------------------------------------
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.i(TAG, "keyCode: " + keyCode);
                if( keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    Log.i(TAG, "onKey Back listener is working!!!");
//                    getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                      selected_object_file = getObjectFileFromUri(selected_object_file.parent_uri);
                      getSetFolderFiles(selected_object_file);
                    return true;
                }
                return false;
            }
        });

        button_add_tree.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
                startActivityForResult(i, REQUEST_CODE_EXTERNAL_STORAGE);
            }
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

            spinner_adapter = new ArrayAdapter<>(context,R.layout.listitem_storage_spinner,R.id.spinner_item,storage_spinner_items);
            storage_spinner.setAdapter(spinner_adapter);

            value_storage_spinner_position = ActivityMain.sharedPrefSettings.getInt(key_storage_spinner_position,0);

            if(value_storage_spinner_position <= storages_uri_list.size()){
                storage_spinner.setSelection(value_storage_spinner_position);
            }
    }

    private void setNewFilesAdapter(){
        SortFileList(objectDocumentFileList);
        files_adapter = new AdapterFileManager(this, objectDocumentFileList, context);
        files_rview.setAdapter(files_adapter);
        //files_adapter.notifyDataSetChanged();
        showDefaultMsg(textview_msg);
    }

    private ObjectDocumentFile getObjectFileFromUri(Uri uri){
        showMsg(log_msg,"---------------------------GETOBJECTFILEFROMURI(Uri)-----BEGIN-----------------------");
        ObjectDocumentFile objectDocumentFile = null;
        Uri objectDocUri = null;

        if(DocumentsContract.isTreeUri(uri) & DocumentsContract.isDocumentUri(context,uri)){
            objectDocUri = uri;
            showMsg(log_msg,"Uri Received : "+uri);
//            showMsg(log_msg,"isDocumentUri : "+DocumentsContract.isDocumentUri(context,uri));
//            showMsg(log_msg,"isTreeUri     : "+DocumentsContract.isTreeUri(uri));
//            showMsg(log_msg,"DocumentId    : "+DocumentsContract.getTreeDocumentId(uri));
        }else if(DocumentsContract.isTreeUri(uri) & !DocumentsContract.isDocumentUri(context,uri)){
            objectDocUri = DocumentsContract.buildDocumentUriUsingTree(uri,DocumentsContract.getTreeDocumentId(uri));
            showMsg(log_msg,"DocumentUriFromTree (treeUri,treeDocId) : "+objectDocUri);
//            showMsg(log_msg,"isDocumentUri : "+DocumentsContract.isDocumentUri(context,objectDocUri));
//            showMsg(log_msg,"isTreeUri     : "+DocumentsContract.isTreeUri(objectDocUri));
//            showMsg(log_msg,"DocumentId    : "+DocumentsContract.getDocumentId(objectDocUri));
        }else if(!DocumentsContract.isTreeUri(uri) & DocumentsContract.isDocumentUri(context,uri)){
            objectDocUri = DocumentsContract.buildDocumentUri(uri.getAuthority(),uri.getLastPathSegment());
            showMsg(log_msg,"DocumentUri(Authority,Segment)     : "+objectDocUri);
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
        showMsg(log_msg,"---------------------------GETOBJECTFILEFROMURI-----END-----------------------");
        showMsg(log_msg,"\n                                                                                  -");

        return objectDocumentFile;
    }

    private void showMsg(String msgtype, String str){

        if(null==str || str.isEmpty()){
            showDefaultMsg(msgtype);
        }else{
            if(msgtype.equals(toast_msg)){
                Toast.makeText(context,str,Toast.LENGTH_SHORT).show();
            }else if(msgtype.equals(textview_msg)){
                files_msg_textview.setText(str);
            }
            Log.i(TAG,str);
        }
    }

    private void showDefaultMsg(String type){
       // String msg1 = "Total Files : "+ objectDocumentFileList.size()+" Files Selected :"+files_adapter.getSelectedFiles().size();
        String msg = "Total Files : "+ objectDocumentFileList.size()+" Files Selected :"+files_adapter.selected_count;

        if(type.equals(log_msg)){
            Log.i(TAG,msg);
        }else if(type.equals(textview_msg)){
            files_msg_textview.setText(msg);
        }else{
            files_msg_textview.setText(msg);
            Log.i(TAG,msg);
        }
      //  Log.i(TAG,msg1)        ;
    }

    public void selectAll(boolean enable){

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
            showMsg(log_msg, " Found "+obj.uri+" in Cache... Loading list size : "+objectDocumentFileList.size());
            setNewFilesAdapter();
        }else{
              getSetFreshFilesTask(obj);
        }

    }

    public void getSetFreshFilesTask(ObjectDocumentFile obj){

        selected_object_file = obj;
        if(loadFilesTask!=null){
            loadFilesTask.cancel(true);
            loadFilesTask = null;
        }
        showMsg(log_msg, " Uri Not in Cache... Loading  Fresh : "+selected_object_file.uri);

        loadFilesTask = new LoadFilesAsyncTask(this,selected_object_file.uri);
        loadFilesTask.execute();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch(requestCode){
            case REQUEST_CODE_EXTERNAL_STORAGE : if(resultCode==RESULT_OK){
                Uri uri_extsd = data.getData();
                ActivityMain.prefEditExternalStorages.putString(uri_extsd.getLastPathSegment(),uri_extsd.toString()).commit();
                showMsg(log_msg,"Added : "+uri_extsd.getPath());
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

        if (selection_mode) {
            bottomAppBar.getMenu().setGroupVisible(R.id.menugroup_files_permament,true);
            bottomAppBar.getMenu().setGroupVisible(R.id.menugroup_files_option_group,false);
            bottomAppBar.getMenu().setGroupVisible(R.id.menugroup_files_action_group,true);
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
        files_msg_textview.setText(str);
    }

    @Override
    public void updateReloadList(boolean refresh) {
        if(refresh){
            files_adapter.notifyDataSetChanged();
        }else{
            setNewFilesAdapter();
        }
    }

    @Override
    public void cancelOperationById(int id) {
        NoRootOperations[id].cancelTask();
    }

    public class LoadFilesAsyncTask extends AsyncTask<Void,String,Void> {

        Uri local_search_uri;
        AdapterFileManager.MyCallBack callBack;
        int count;
        final String TAG = " LOADFILESASYNCTASK : ";

        public LoadFilesAsyncTask(AdapterFileManager.MyCallBack callBack, Uri uri) {
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
            showMsg((String) values[0],(String) values[1]);

        }

        @Override
        protected void onPostExecute(Void voids) {
            super.onPostExecute(voids);
            setNewFilesAdapter();
            cacheList.put(local_search_uri, objectDocumentFileList);
            showMsg(log_msg, " Cache added of : "+local_search_uri);
            files_progress_bar.setVisibility(View.GONE);
            showMsg(textview_msg,"  on PostExecute : Total Files : "+ objectDocumentFileList.size());
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            setNewFilesAdapter();
            files_progress_bar.setVisibility(View.GONE);
        }

        protected void getChildrenList(Uri search_uri){
           List<Uri> childeren_uri_list =  new ArrayList<>();
           Uri children_tree_uri = null;
           Cursor cursor = null;
           publishProgress(log_msg,"------------------------------getChildrenList : Begin----------------------------------------- ");

           publishProgress(log_msg,"Search_uri : "+search_uri);

          // publishProgress(log_msg,"isTreeUri : "+DocumentsContract.isTreeUri(search_uri));

           if(DocumentsContract.isTreeUri(search_uri)){
               children_tree_uri = DocumentsContract.buildChildDocumentsUriUsingTree(search_uri,DocumentsContract.getDocumentId(search_uri));
           }else{
               children_tree_uri = DocumentsContract.buildChildDocumentsUriUsingTree(search_uri,DocumentsContract.getTreeDocumentId(search_uri));
           }

          //  publishProgress(log_msg,"CHILDREN_TREE_URI : "+children_tree_uri);

            try{
               cursor = resolver.query(children_tree_uri, doc_projection,null,null,
                       COLUMN_DISPLAY_NAME+" DESC");
               publishProgress(log_msg,"isCursorNull : "+(cursor == null));
                count=0;
               while(cursor.moveToNext()){
                   final String doc_id = cursor.getString(0);
                   final Uri child_uri =  DocumentsContract.buildDocumentUriUsingTree(search_uri, doc_id);
                   objectDocumentFileList.add(new ObjectDocumentFile(cursor,child_uri,context));
                   if(isCancelled()){
                       break;

                   }
                   childeren_uri_list.add(child_uri);
                 // publishProgress(textview_msg,"Loading : "+(count++)+" : "+child_uri.getLastPathSegment());
               }
           }catch(Exception ex){
               publishProgress(log_msg,"Error Searching , Exception : "+ex);
           }finally {
               if(cursor!=null){
                   cursor.close();
               }
           }
        //  publishProgress(textview_msg,"SELECTED_OBJECT_FILE : END :  "+ selected_object_file.uri);
        //  publishProgress(log_msg,"------------------------------getChildrenList : End----------------------------------------- ");
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_files,menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if(files_adapter!=null){
            selectedFilesList = files_adapter.getSelectedFiles()  ;
            if(selectedFilesList.size()>0){
                menu.setGroupVisible(R.id.menugroup_files_action_group, true);
                if(SELECTED_OPERATION==1 || SELECTED_OPERATION==2){
                    menu.findItem(R.id.menuitem_files_paste).setVisible(true);
                    menu.findItem(R.id.menuitem_files_paste).setTitleCondensed("Paste files ("+selectedFilesList.size()+" files)");
                }

                for(ObjectDocumentFile obj:selectedFilesList){
                    if(obj.file_name.endsWith(".apk")){
                        menu.findItem(R.id.menuitem_files_install);
                        break;
                    }
                }
             
            }else{
                menu.setGroupVisible(R.id.menugroup_files_action_group, false);
                menu.findItem(R.id.menuitem_files_paste).setVisible(false);
            }
        }

        value_sort_by =   ActivityMain.sharedPrefFileSettings.getString(key_sort_by, sort_by_name);
        value_order_by = ActivityMain.sharedPrefFileSettings.getString(key_order_by, order_increasing);
        showMsg(log_msg," Default Sorting Found :"+value_sort_by);
        showMsg(log_msg," Default Order Found :"+value_order_by);


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

        switch(item.getItemId()){

//----------------------------------------LOAD FILES-------------------------------------------------------
            case R.id.menuitem_files_load:{
                getSetFreshFilesTask(selected_object_file);
                break;
            }

//----------------------------------------REFRESH_LIST-------------------------------------------------------
//            case R.id.menuitem_files_refresh:{
////                files_adapter.notifyDataSetChanged();
////                break;
////            }
            
//----------------------------------------RENAME_FILE------------------------------------------------------
            case R.id.menuitem_files_rename:{
                if(files_adapter!=null){
                    selectedFilesList = files_adapter.getSelectedFiles();
                }
                if(selectedFilesList.size()==1){
                    SELECTED_OPERATION = OPERATION_RENAME;
                    new ClassNoRootUtils(context,this,NEW_TASK_ID++).renameDocument(selectedFilesList.get(0));
                    showMsg(textview_msg, " Renaming "+selectedFilesList.get(0).file_name);
                    SELECTED_OPERATION = 0;
                }
                files_adapter.notifyDataSetChanged();
                break;
            }

//----------------------------------------COPY_FILES------------------------------------------------------
            case R.id.menuitem_files_copy_files:{
                if(files_adapter!=null){
                    selectedFilesList = files_adapter.getSelectedFiles();
                }
                SELECTED_OPERATION = OPERATION_COPY;
                //fab_action.setVisibility(View.VISIBLE);
                fab_action.show();
                files_adapter.selection_mode=false;
                showMsg(textview_msg, selectedFilesList.size()+" files selected.Now Browse to a folder to Copy ");
                fab_action.setImageResource(R.drawable.ic_content_paste_black_24dp);
                break;
            }

//----------------------------------------MOVE_FILES-------------------------------------------------------
            case R.id.menuitem_files_move_files:{
                if(files_adapter!=null){
                    selectedFilesList = files_adapter.getSelectedFiles();
                }
                SELECTED_OPERATION = OPERATION_MOVE;
                fab_action.show();
                files_adapter.selection_mode=false;
                showMsg(textview_msg, selectedFilesList.size()+" files selected. Browse to a folder to Move");
                fab_action.setImageResource(R.drawable.ic_content_paste_white_36dp);
                break;
            }

//----------------------------------------PASTE_FILES-------------------------------------------------------
            case R.id.menuitem_files_paste:{
                if(SELECTED_OPERATION==OPERATION_COPY){
                    showMsg(log_msg, "OPERATION_COPY called");
                    NEW_TASK_ID++;
                    NoRootOperations[NEW_TASK_ID] = new ClassNoRootUtils(context,myUpdates,NEW_TASK_ID);
                    NoRootOperations[NEW_TASK_ID].copyDocumentList(selectedFilesList, selected_object_file);
                }else if(SELECTED_OPERATION == OPERATION_MOVE){
                    showMsg(log_msg, "OPERATION_MOVE called");
                    NEW_TASK_ID++;
                    NoRootOperations[NEW_TASK_ID] = new ClassNoRootUtils(context,myUpdates,NEW_TASK_ID);
                    NoRootOperations[NEW_TASK_ID].moveDocumentList(selectedFilesList, selected_object_file);
                }
                fab_action.hide();
                files_adapter.notifyDataSetChanged();
                selectedFilesList.clear();
                SELECTED_OPERATION = 0;
                break;
            }

//----------------------------------------DELETE_FILES-------------------------------------------------------
            case R.id.menuitem_files_delete:{
                if(files_adapter!=null){
                    selectedFilesList = files_adapter.getSelectedFiles();
                }
                SELECTED_OPERATION = OPERATION_DELETE;

                NEW_TASK_ID++;
                NoRootOperations[NEW_TASK_ID] = new ClassNoRootUtils(context,myUpdates,NEW_TASK_ID);
                NoRootOperations[NEW_TASK_ID].deleteDocumentList(selectedFilesList);
                selectedFilesList.clear();
                SELECTED_OPERATION = 0;
                break;
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
                objectDocumentFileList.forEach(obj->{
                    obj.check_box_state = !obj.check_box_state;
                });

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

            case R.id.menuitem_files_hidden: {
                showHidden = item.isChecked();
                ActivityMain.prefEditFileSettings.putBoolean(key_show_hidden, item.isChecked()).commit();
                break;
            }

        }
        return true;
    }

    public void SortFileList(List to_sort_list) {
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
                if (global_order_by == order_decreasing) {
                    Collections.reverse(to_sort_list);
                }
                Log.i(TAG, "in sort_by_name");
                break;
            }

            case sort_by_date: {
                Collections.sort(to_sort_list, modified_date_comparator);
                if (global_order_by == order_decreasing) {
                    Collections.reverse(to_sort_list);
                }
                Log.i(TAG, "in sort_by_date");
                break;
            }

            case sort_by_size: {
                Collections.sort(to_sort_list, file_size_comparator);
                if (global_order_by == order_decreasing) {
                    Collections.reverse(to_sort_list);
                }
                Log.i(TAG, "in sort_by_size");
                break;
            }

            case sort_by_type: {
                Collections.sort(to_sort_list, file_type_comparator);
                if (global_order_by == order_decreasing) {
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

}

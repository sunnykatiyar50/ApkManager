package com.sunnykatiyar.appmanager;


import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;
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
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentFiles extends Fragment implements AdapterFilesList.MyCallBack {


    private Context context;

    private final String TAG = "FRAGMENT_FILES : ";
    private final String textview_msg ="textview_msg";
    private final String log_msg = "log_msg";
    private final String toast_msg ="toast_msg";

    ArrayAdapter<String> spinner_adapter;

    private static final String key_extsd_uri = FragmentSettings.key_extsd_uri;
    private static final String path_not_set = FragmentSettings.path_not_set;
    private static final String storage_spinner_selected_position_key = "STORAGE_SPINNER_SELECTED_POSITION";
    private int storage_spinner_selected_position_value;

    RecyclerView files_rview;
    TextView files_path_textview;
    TextView files_msg_textview;
    Spinner storage_spinner;
    ProgressBar files_progress_bar;

    Uri selected_tree_uri;
    Uri selected_directory_uri;
    ObjectFile selected_object_file;

    List<DocumentFile> storages_doc_file_list;
    List<Uri> storages_uri_list;
    List<String> storage_spinner_items;

    List<ObjectFile> objectFileList;
    AdapterFilesList files_adapter;
    LoadFilesAsyncTask loadFilesTask;
    ImageButton up_img_button;
    ContentResolver resolver ;

    final String key_sort_by = "SORT_BY";
    final String order_by = "ORDER_BY";

    String value_sort_by;
    String value_order_by;

    String global_sort_by;
    String global_order_by;

    String sort_by_date = "SORT_BY_DATE";
    String sort_by_size = "SORT_BY_SIZE";
    String sort_by_name = "SORT_BY_NAME";
    String sort_by_type = "SORT_BY_TYPE";

    String order_increasing = "ORDER_INCREASING";
    String order_decreasing = "ORDER_DECREASING";

    public final static String[] doc_projection = {DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                                                   DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                                                   DocumentsContract.Document.COLUMN_SIZE,
                                                   DocumentsContract.Document.COLUMN_LAST_MODIFIED,
                                                   DocumentsContract.Document.COLUMN_MIME_TYPE,
                                                   DocumentsContract.Document.COLUMN_SUMMARY,
                                                   DocumentsContract.Document.COLUMN_FLAGS,
                                                   DocumentsContract.Document.COLUMN_ICON};

    public final static String[] root_projection = {DocumentsContract.Root.COLUMN_DOCUMENT_ID,
                                                    DocumentsContract.Root.COLUMN_TITLE,
                                                    DocumentsContract.Root.COLUMN_ROOT_ID,
                                                    DocumentsContract.Root.COLUMN_CAPACITY_BYTES,
                                                    DocumentsContract.Root.COLUMN_AVAILABLE_BYTES,
                                                    DocumentsContract.Root.COLUMN_MIME_TYPES,
                                                    DocumentsContract.Root.COLUMN_SUMMARY,
                                                    DocumentsContract.Root.COLUMN_FLAGS,
                                                    DocumentsContract.Root.COLUMN_ICON};

    public FragmentFiles() {
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

        View v = inflater.inflate(R.layout.fragment_files_manager, container, false);

        files_rview = v.findViewById(R.id.files_rview);
        files_path_textview = v.findViewById(R.id.files_path_textview);
        storage_spinner = v.findViewById(R.id.files_storage_spinner);
        files_msg_textview = v.findViewById(R.id.files_msg_textview);
        up_img_button = v.findViewById(R.id.files_image_button_up);
        resolver = context.getContentResolver();
        files_progress_bar = v.findViewById(R.id.files_progress_bar);
        storages_doc_file_list = new ArrayList<>();
        storage_spinner_items = new ArrayList<>();
        storages_uri_list = new ArrayList<>();

        //---------------------readingAllStorages--------------------------------------------
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

//        if(storages_doc_file_list !=null & storages_doc_file_list.size()>0){
//            for(DocumentFile f: storages_doc_file_list){
//                storage_spinner_items.add(f.getUri().getPath());
//            }
//        }

//------------------------SET_STORAGE_SPINNER_ITEMS--------------------------------------------
        if(storages_uri_list !=null & storages_uri_list.size()>0){
            for(Uri uri: storages_uri_list){
                storage_spinner_items.add(uri.getPath());
            }
        }

        spinner_adapter = new ArrayAdapter<>(context,android.R.layout.simple_spinner_dropdown_item,storage_spinner_items);
        storage_spinner.setAdapter(spinner_adapter);

        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(RecyclerView.VERTICAL);
        files_rview.setLayoutManager(llm);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(context, llm.getOrientation());
        files_rview.addItemDecoration(mDividerItemDecoration);

        if(selected_tree_uri != null){
            selected_directory_uri = selected_tree_uri;
            files_path_textview.setText(selected_tree_uri.getPath());
            selected_object_file = getObjectFileFromUri(selected_directory_uri);
            newAsyncTask(selected_object_file);
            files_adapter = new AdapterFilesList(this, objectFileList,getContext());
        }



        //--------------------------------SPINNER_ITEM_SELECTION_LISTENER--------------------------------------------
        storage_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                files_path_textview.setText(storage_spinner_items.get(position));
                selected_tree_uri = storages_uri_list.get(position);
                selected_directory_uri = selected_tree_uri;
//              selected_tree_doc_file = storages_doc_file_list.get(position);
//              selected_directory_doc_file = selected_tree_doc_file;
                selected_object_file = getObjectFileFromUri(selected_tree_uri);
                newAsyncTask(selected_object_file);

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
                    Log.i(TAG," UP_BUTTON_CLICK : SELECTED_OBJECTFILE SET :" +selected_object_file.uri);
                    newAsyncTask(selected_object_file);
                }else{
                    showMsg(toast_msg,"You are at the root of the tree.. Cannoy go up !!");
                }
            }
        });

        //-----------------------------BACK_BUTTON_ACTION---------------------------------------------------------
        v.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.i(TAG, "keyCode: " + keyCode);
                if( keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    Log.i(TAG, "onKey Back listener is working!!!");
//                    getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                      selected_object_file = getObjectFileFromUri(selected_object_file.parent_uri);
                      newAsyncTask(selected_object_file);
                    return true;
                }
                return false;
            }
        });

        return v;
    }

    private ObjectFile getObjectFileFromUri(Uri uri){
        showMsg(log_msg,"---------------------------GETOBJECTFILEFROMURI(Uri)-----BEGIN-----------------------");
        ObjectFile objectFile = null;
        Uri objectDocUri = null;

        showMsg(log_msg,"Uri Received : "+uri);
        showMsg(log_msg,"isDocumentUri : "+DocumentsContract.isDocumentUri(context,uri));
        showMsg(log_msg,"isTreeUri     : "+DocumentsContract.isTreeUri(uri));
        showMsg(log_msg,"DocumentId    : "+DocumentsContract.getTreeDocumentId(uri));

        Uri docUri = DocumentsContract.buildDocumentUri(uri.getAuthority(),uri.getLastPathSegment());
        showMsg(log_msg,"DocumentUri(Authority,Segment)     : "+docUri);
        showMsg(log_msg,"isDocumentUri : "+DocumentsContract.isDocumentUri(context,docUri));
        showMsg(log_msg,"isTreeUri     : "+DocumentsContract.isTreeUri(docUri));
        showMsg(log_msg,"DocumentId    : "+DocumentsContract.getDocumentId(docUri));

        Uri docUri2 = DocumentsContract.buildDocumentUriUsingTree(uri,DocumentsContract.getTreeDocumentId(uri));
        showMsg(log_msg,"DocumentUriFromTree (treeUri,treeDocId) : "+docUri2);
        showMsg(log_msg,"isDocumentUri : "+DocumentsContract.isDocumentUri(context,docUri2));
        showMsg(log_msg,"isTreeUri     : "+DocumentsContract.isTreeUri(docUri2));
        showMsg(log_msg,"DocumentId    : "+DocumentsContract.getDocumentId(docUri2));

        if(uri.getLastPathSegment().endsWith(":")){
            showMsg(log_msg,"ROOT URI Found: Using : buildDocumentUriUsingTree,getDocumentId");
            objectDocUri = DocumentsContract.buildDocumentUriUsingTree(uri,DocumentsContract.getTreeDocumentId(uri));
        }
        else{
          showMsg(log_msg,"Not Root URI: Using : buildDocumentUriUsingTree,getDocumentId");
          objectDocUri= DocumentsContract.buildDocumentUriUsingTree(uri,DocumentsContract.getDocumentId(uri));
        }

        showMsg(log_msg,"Built objectUri Value =  "+objectDocUri);

        Cursor c = resolver.query(objectDocUri,doc_projection,null,null,null);
        if (c != null) {
            c.moveToFirst();
            objectFile = new ObjectFile(c,objectDocUri,context);
        }
        showMsg(log_msg,"objectFile : "+objectFile.uri);
        showMsg(log_msg,"---------------------------GETOBJECTFILEFROMURI-----END-----------------------");
        showMsg(log_msg,"\n                                                                                  -");

        return objectFile;
    }


    private void showMsg(String msgtype, String str){

        if(msgtype.equals(toast_msg)){
            Toast.makeText(context,str,Toast.LENGTH_SHORT).show();
        }else if(msgtype.equals(textview_msg)){
            files_msg_textview.setText(str);
        }
        Log.i(TAG,str);
    }

    @Override
    public void openDirectory(ObjectFile obj) {
        showMsg(log_msg,"Received FOLDER TO SEARCH from RecyclerViewAdapterOnClick : " +obj.file_name);
        newAsyncTask(obj);
    }

    private void newAsyncTask(ObjectFile obj){
        if(loadFilesTask!=null){
            loadFilesTask.cancel(true);
            loadFilesTask = null;
        }

        selected_object_file = obj;
        showMsg(log_msg,"SELECTED_OBJECT_FILE : "+selected_object_file.uri);

        loadFilesTask = new LoadFilesAsyncTask(this,selected_object_file.uri);
        files_path_textview.setText(selected_object_file.uri.getPath());
        loadFilesTask.execute();
    }


    public class LoadFilesAsyncTask extends AsyncTask<Void,String,Void> {

        Uri local_search_uri;
        AdapterFilesList.MyCallBack callBack;
        int count;
        final String TAG = " LOADFILESASYNCTASK : ";

        public LoadFilesAsyncTask(AdapterFilesList.MyCallBack callBack,Uri uri) {
            super();
            local_search_uri = uri;
            this.callBack = callBack;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            objectFileList = new ArrayList<>();
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

            files_adapter = new AdapterFilesList(callBack, objectFileList, context);
            files_rview.setAdapter(files_adapter);
            files_adapter.notifyDataSetChanged();
            files_progress_bar.setVisibility(View.GONE);
            showMsg(textview_msg,"Total Files : "+objectFileList.size());
        }

        protected void getChildrenList(Uri search_uri){
           List<Uri> childeren_uri_list =  new ArrayList<>();
           Uri children_tree_uri = null;
           Cursor cursor = null;
           publishProgress(log_msg,"------------------------------getChildrenList : Begin----------------------------------------- ");

           publishProgress(log_msg,"Search_uri : "+search_uri);

           publishProgress(log_msg,"isTreeUri : "+DocumentsContract.isTreeUri(search_uri));

           if(DocumentsContract.isTreeUri(search_uri)){
               children_tree_uri = DocumentsContract.buildChildDocumentsUriUsingTree(search_uri,DocumentsContract.getDocumentId(search_uri));
           }else{
               children_tree_uri = DocumentsContract.buildChildDocumentsUriUsingTree(search_uri,DocumentsContract.getTreeDocumentId(search_uri));
           }

            publishProgress(log_msg,"CHILDREN_TREE_URI : "+children_tree_uri);

            try{
               cursor = resolver.query(children_tree_uri,doc_projection,null,null,
                       DocumentsContract.Document.COLUMN_DISPLAY_NAME+" ASC");
               publishProgress(log_msg,"isCursorNull : "+(cursor==null));

               while(cursor.moveToNext()){
                   final String doc_id = cursor.getString(0);
                   final Uri child_uri =  DocumentsContract.buildDocumentUriUsingTree(search_uri, doc_id);
                //  publishProgress(textview_msg," child_uri.getLastPathSegment : "+child_uri.getLastPathSegment());
                   objectFileList.add(new ObjectFile(cursor,child_uri,context));
                   if(isCancelled()){
                       break;
                   }
                   childeren_uri_list.add(child_uri);
               }
           }catch(Exception ex){
               publishProgress(log_msg,"Error Searching , Exception : "+ex);
           }finally {
               if(cursor!=null){
                   cursor.close();
               }
           }
          publishProgress(textview_msg,"SELECTED_OBJECT_FILE : END :  "+ selected_object_file.uri);
          publishProgress(log_msg,"------------------------------getChildrenList : End----------------------------------------- ");
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){

//----------------------------------------LOAD FILES-------------------------------------------------------
            case R.id.menuitem_files_load:{

                break;
            }
//----------------------------------------REFRESH_LIST-------------------------------------------------------

            case R.id.menuitem_files_refresh:{

                break;
            }
//----------------------------------------COPY_FILES------------------------------------------------------

            case R.id.menuitem_files_copy_files:{

                break;
            }
//----------------------------------------MOVE_FILES-------------------------------------------------------

            case R.id.menuitem_files_move_files:{

                break;
            }
//----------------------------------------DELETE_FILES-------------------------------------------------------

            case R.id.menuitem_files_delete:{

                break;
            }
//----------------------------------------SORT_BY-------------------------------------------------------
            case R.id.menuitem_files_sortbyname:{

                break;
            }

            case R.id.menuitem_files_sortbydate:{

                break;
            }

            case R.id.menuitem_files_sortbysize:{

                break;
            }
//-----------------------------------ORDER_BY-------------------------------------------------------
            case R.id.menuitem_files_increasing:{

                break;
            }

            case R.id.menuitem_files_decreasing:{

                break;
            }

            default : break;

        }
        return true;
    }

}

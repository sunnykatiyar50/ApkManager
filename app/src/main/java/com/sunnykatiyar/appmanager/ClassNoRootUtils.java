package com.sunnykatiyar.appmanager;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.DocumentsContract;
import android.provider.DocumentsProvider;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.sunnykatiyar.appmanager.ActivityOperations.adapter_operations_list;

public class ClassNoRootUtils {

    Context context;
    public final static String[] doc_projection = {DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_SIZE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_SUMMARY,
            DocumentsContract.Document.COLUMN_FLAGS,
            DocumentsContract.Document.COLUMN_ICON};


    final String TAG = "MYAPP : NOROOT_FILE_OPERATION";
    private ContentResolver resolver;
    private final String textview_msg ="textview_msg";
    private final String log_msg = "log_msg";
    private final String toast_msg ="toast_msg";

    private final int OPERATION_COPY = 101;
    private final int OPERATION_DELETE = 102;
    private final int OPERATION_GETCHILDERN = 103;
    private final int OPERATION_MOVE = 104;
    private final int OPERATION_RENAME = 105;
    final int REQUEST_CODE_EXTSD_CARD_PATH = 2323;
    
    public OperationAsyncTasks myOperationAsyncTask;
    private int OPERATION_ID;
    boolean isCancelled=false;
    //---------Interfaces-------------------------------
    TextViewUpdateInterface myOpUpdate;
    UpdateOperationObject myOperationUpdates;

    public interface UpdateOperationObject {
        void updateCurrentFileNum(int num);
    }

    public interface TextViewUpdateInterface {
        void updateTextView(String str);
        void updateReloadList(boolean refresh);
    }

    public ClassNoRootUtils(Context context, TextViewUpdateInterface updates, int noti_id){
        this.context= context;
        this.resolver= context.getContentResolver();
        this.myOpUpdate = updates;
        this.OPERATION_ID = noti_id;
    }

    public ClassNoRootUtils(Context context, TextViewUpdateInterface updates){
        this.context= context;
        this.resolver= context.getContentResolver();
        this.myOpUpdate = updates;
    }

    public void createNew(ObjectDocumentFile selected_object_file){
        showMsg(log_msg,"createNew Called :");

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Create New ");
        builder.setMessage("Choose what to create ");
        EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setPositiveButton("New Folder", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AlertDialog.Builder builder1 =  new AlertDialog.Builder(context)
                        .setTitle("Enter Folder Name")
                        .setView(input)
                        .setPositiveButton("Create", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    DocumentsContract.createDocument(resolver,selected_object_file.uri,DocumentsContract.Document.MIME_TYPE_DIR,input.getText().toString());
                                } catch (FileNotFoundException e) {
                                    showMsg(log_msg,"Error Creating new Folder : "+e);
                                }
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }) ;
                builder1.show();
            }
        });

        builder.setNegativeButton("New File", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AlertDialog.Builder builder2 = new AlertDialog.Builder(context)
                        .setTitle("Enter File Name")
                        .setView(input)
                        .setPositiveButton("Create", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    DocumentsContract.createDocument(resolver,selected_object_file.uri,null,input.getText().toString());
                                } catch (FileNotFoundException e) {
                                    showMsg(log_msg,"Error Creating new Folder : "+e);
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                 dialog.dismiss();
                            }
                        }) ;
            builder2.show();
            }
        });
        builder.show();
    }

    public void copyDocumentList(List<ObjectDocumentFile> list, ObjectDocumentFile targetObj){
        showMsg(log_msg,"Executing copyDocumentList ... files list size :"+list.size());
        myOperationAsyncTask = new OperationAsyncTasks(OPERATION_COPY);
        myOperationAsyncTask.execute(new ArrayList<>(list),targetObj);
    }

    public void moveDocumentList(List<ObjectDocumentFile> list, ObjectDocumentFile targetObj){
        showMsg(log_msg,"Executing moveDocumentList  list size : "+list.size());
        myOperationAsyncTask = new OperationAsyncTasks(OPERATION_MOVE);
        myOperationAsyncTask.execute(new ArrayList<>(list),targetObj);
    }

    public void deleteDocumentList(List<ObjectDocumentFile> flist){
        this.myOperationAsyncTask = new OperationAsyncTasks(OPERATION_DELETE);
        //List<ObjectDocumentFile> list = new ArrayList<>(flist) ;
        showMsg(log_msg,"Calling Asynctask on deleteDocument : list received size : "+flist.size());
        this.myOperationAsyncTask.execute(new ArrayList<>(flist));
    }


    public void renameDocument(ObjectDocumentFile obj){
        showMsg(log_msg,"Rename Called :");
        String new_name;
        
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Type new name with extension ");
        builder.setMessage("Select one to crete corresponding");
        EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(obj.file_name);
        input.selectAll();
        builder.setView(input);
        builder.setPositiveButton("Done", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                                DocumentsContract.renameDocument(context.getContentResolver(), obj.uri, input.getText().toString());
                                showMsg(textview_msg, "Renamed successfully " + obj.file_name);
                            }catch(FileNotFoundException e) {
                                showMsg(log_msg, " Unable to rename " + obj.file_name + " Error : " + e);
                            }
                    }
                });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
            }
        });

        builder.show();
    }

    public void installApks(List<ObjectDocumentFile> list){

        for(ObjectDocumentFile obj:list){
            Intent i = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            i.setType("application/vnd.android.package-archive");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            i.setData(obj.uri);
            context.startActivity(i);
        }
    }

    public void cancelTask(){
        if(myOperationAsyncTask !=null){
            isCancelled = true;
            myOperationAsyncTask.cancel(true);
            showMsg(toast_msg,"Moving Task Cancelled");
        }
        else{
            showMsg(log_msg, "No Copy Task Running");
        }
    }

    public void cancelGetChildren(){
               isCancelled=true;
    }

    public List<ObjectDocumentFile> getChildrenList(Uri search_uri){
        List<Uri> childeren_uri_list =  new ArrayList<>();
        List<ObjectDocumentFile> objectDocumentFileList = new ArrayList();

        Uri children_tree_uri = null;
        Cursor cursor = null;
        showMsg(log_msg,"------------------------------getChildrenList : Begin----------------------------------------- ");

        showMsg(log_msg,"Search_uri : "+search_uri);

        showMsg(log_msg,"isTreeUri : "+DocumentsContract.isTreeUri(search_uri));

        if(DocumentsContract.isTreeUri(search_uri)){
            children_tree_uri = DocumentsContract.buildChildDocumentsUriUsingTree(search_uri,DocumentsContract.getDocumentId(search_uri));
        }else{
            children_tree_uri = DocumentsContract.buildChildDocumentsUriUsingTree(search_uri,DocumentsContract.getTreeDocumentId(search_uri));
        }

        showMsg(log_msg,"CHILDREN_TREE_URI : "+children_tree_uri);

        try{
            cursor = resolver.query(children_tree_uri, doc_projection,null,null,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME+" DESC");
            showMsg(log_msg,"isCursorNull : "+(cursor == null));
            int count=0;
            while(cursor.moveToNext()){
                final String doc_id = cursor.getString(0);
                final Uri child_uri =  DocumentsContract.buildDocumentUriUsingTree(search_uri, doc_id);
                objectDocumentFileList.add(new ObjectDocumentFile(cursor,child_uri,context));
                childeren_uri_list.add(child_uri);
                showMsg(textview_msg,"Loading : "+(count++)+" : "+child_uri.getLastPathSegment());
                if(isCancelled){
                    break;
                }
            }
        }catch(Exception ex){
            showMsg(log_msg,"Error Searching , Exception : "+ex);
        }finally {
            if(cursor!=null){
                cursor.close();
            }
        }
        showMsg(log_msg,"------------------------------getChildrenList : End----------------------------------------- ");

        return objectDocumentFileList;
    }

    public ObjectDocumentFile getObjectFileFromUri(Uri uri){
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

    public Uri getDocumentFromUri(Uri uri){
         Uri newDocumentUri = uri;
         return newDocumentUri;
    }

    private void showMsg(String msgtype, String str){

        if(msgtype.equals(toast_msg)){
            Toast.makeText(context,str,Toast.LENGTH_SHORT).show();
        }else if(msgtype.equals(textview_msg)){
            myOpUpdate.updateTextView(str);
        }
        Log.i(TAG,str);
    }

    private void getDefaultContentprovider(){
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        i.setType(DocumentsContract.Document.MIME_TYPE_DIR);
////        i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
//        i.putExtra(, )
//        context.startActivityForResult(i, REQUEST_CODE_EXTSD_CARD_PATH);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

//      super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case REQUEST_CODE_EXTSD_CARD_PATH:{
                if(resultCode == RESULT_OK){
                    Uri uri_extsd = data.getData();
                    ActivityMain.prefEditExternalStorages.putString(uri_extsd.getLastPathSegment(),uri_extsd.toString()).commit();
                    showMsg(log_msg,"Added : "+uri_extsd.getPath());
                    // Persist access permissions.
                    final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    context.getContentResolver().takePersistableUriPermission(uri_extsd, takeFlags);

                }
            }
        }
    }

    public class OperationAsyncTasks extends AsyncTask<Object,String,Void> {

        int OPERATION_TYPE;
        ObjectOperation myOperationObject;

        public OperationAsyncTasks(int op_type) {
            super();
            OPERATION_TYPE = op_type;
            Log.i(TAG,"inAsyncTAsk "+op_type);
        }

        @Override
        protected Void doInBackground(Object... objects) {

            switch(OPERATION_TYPE){

                case OPERATION_DELETE:{

                    List<ObjectDocumentFile> list;
                    list = (List<ObjectDocumentFile>) objects[0];
                    ObjectDocumentFile obj;
                    Log.i(TAG,"IN ASYNCTASK FOUND DELETE OPERATION : list size :"+ list.size());
                    myOperationObject = new ObjectOperation(context, OPERATION_ID,"Delete Operation ",list.size());
                    ActivityOperations.operationsHashMapList.put(OPERATION_ID,myOperationObject);
                    int k;
                    for(k=1; k<=list.size(); k++)
                    {
//                        Log.i(TAG,"IN DELETE OPERATION for loop "+k);
                        obj = list.get(k-1);
                        Log.i(TAG,"Deleting  "+obj.uri);
                        try{
                            myOperationObject.showNotification(k+"/"+list.size()+" - Deleting "+obj.file_name,k);
                            DocumentsContract.deleteDocument(context.getContentResolver(),obj.uri);
                            publishProgress(textview_msg,"Deleted successfully "+obj.file_name);
                        }catch (FileNotFoundException e) {
                            showMsg(log_msg,"Unable to delete "+obj.file_name+". Error : "+e);
                        }
                        if(isCancelled()){
                            break;
                        }
                    }
                    k--;
                    myOperationObject.showNotification(k+" files DELETED successfully.",k);
                    break;
                }

                case OPERATION_COPY:{
                    ObjectDocumentFile obj;
                    List<ObjectDocumentFile> list;
                    ObjectDocumentFile targetObj;
                    list = (List<ObjectDocumentFile>) objects[0];
                    targetObj = (ObjectDocumentFile) objects[1];
                    myOperationObject = new ObjectOperation(context, OPERATION_ID,"Copy Operation ",list.size());
                    ActivityOperations.operationsHashMapList.put(OPERATION_ID,myOperationObject);
                    int i;
                    for(i=1;i<=list.size();i++)
                    {
                        obj = list.get(i-1);
                        Log.i(TAG,"Copying  "+obj.uri +" to "+targetObj.uri);
                        try {
                            myOperationObject.showNotification(i+"/"+list.size()+" - Copying "+obj.file_name+" to : "+targetObj.file_name,i);
                            DocumentsContract.copyDocument(context.getContentResolver(),obj.uri,targetObj.uri);
//                          myOperationUpdates.updateCurrentFileNum(i);
                            publishProgress(textview_msg,"Copied successfully "+obj.file_name);
                        }catch(FileNotFoundException e) {
                            showMsg(log_msg,"Unable to copy "+obj.file_name+". Error : "+e);
                        }
                        if(isCancelled()){
                            break;
                        }
                    }
                    i--;
                    myOperationObject.showNotification(i+" files COPIED successfully to "+targetObj.file_name,i);
                    break;
                }

                case OPERATION_MOVE:{
                    ObjectDocumentFile obj;
                    List<ObjectDocumentFile> list;
                    ObjectDocumentFile targetObj;
                    list = (List<ObjectDocumentFile>) objects[0];
                    targetObj = (ObjectDocumentFile) objects[1];
                    myOperationObject = new ObjectOperation(context, OPERATION_ID,"Move Operation ",list.size());
                    Log.i(TAG,"IN ASYNCTASK FOUND MOVE OPERATION ");
                    ActivityOperations.operationsHashMapList.put(OPERATION_ID,myOperationObject);
                    int i;
                    for(i=1;i<=list.size();i++)
                    {
                        obj = list.get(i-1);
                        Log.i(TAG,"Moving  "+obj.uri +" to "+targetObj.uri);
                        try {
                            myOperationObject.showNotification(i+"/"+list.size()+" - Moving "+obj.file_name+" to "+targetObj.file_name,i);
                            DocumentsContract.moveDocument(context.getContentResolver(), obj.uri, obj.parent_uri, targetObj.uri);
                            publishProgress(textview_msg,"Moved successfully "+obj.file_name);
                        }catch (FileNotFoundException e) {
                            showMsg(log_msg,"Unable to MOVE "+obj.file_name+". Error : "+e);
                        }
                        if(isCancelled()){
                            break;
                        }
                    }
                    i--;
                    myOperationObject.showNotification(i+" files MOVED successfully to "+targetObj.file_name,i);
                    break;
                }

            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if(adapter_operations_list!=null){
               adapter_operations_list.notifyDataSetChanged();
            }
            myOpUpdate.updateReloadList(true);
            showMsg(values[0],values[1]);
        }
        
    }

}

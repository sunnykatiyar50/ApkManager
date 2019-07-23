package com.sunnykatiyar.skmanager;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.DocumentsContract;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.sunnykatiyar.skmanager.ActivityOperations.adapter_operations_list;

public class ClassNoRootUtils {

    private final Context context;
    private final static String[] doc_projection = {DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_SIZE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_SUMMARY,
            DocumentsContract.Document.COLUMN_FLAGS,
            DocumentsContract.Document.COLUMN_ICON};


    private final String TAG = "MYAPP : NOROOT_FILE_OPERATION";
    private final ContentResolver resolver;

    private final String textview_msg ="textview_msg";
    private final String log_msg = "log_msg";
    private final String toast_msg ="toast_msg";

    private int OPERATION_ID;
    final int NO_OPERATION = 0;
    private boolean   isCancelled = false;
    private final int OPERATION_COPY = 201;
    private final int OPERATION_MOVE  = 204;
    private final int OPERATION_DELETE = 212;
    private final int OPERATION_INSTALL_APKS = 215;
    final int GET_FOLDER_FILES = 218;
    final int GET_SUBFOLDERS_FILES = 221;
    final int GET_FILTERED_FILES  = 224;
    final int GET_EMPTY_FOLDERS = 227;
    final int GET_FOLDER_FILES_BY_SHELL = 230;
    private OperationAsyncTasks myOperationAsyncTask;

    //---------Interfaces-------------------------------
    private final updateUINoRootUtils updateUINoRootUtils;
    UpdateOperationNoRootutils updateOperationNoRootutils;

    public interface UpdateOperationNoRootutils {
        void updateCurrentFileNum(int num);
    }

    public interface updateUINoRootUtils {
        void updateTextView(String str);
        void refreshList(boolean refresh);
        void setprogressBar(boolean visible);
        void taskCompletedAction(int type,ObjectDocumentFile targetObj);
    }

    public ClassNoRootUtils(Context context, updateUINoRootUtils updates, int noti_id){
        this.context= context;
        this.resolver= context.getContentResolver();
        this.updateUINoRootUtils = updates;
        this.OPERATION_ID = noti_id;
    }

    public ClassNoRootUtils(Context context, updateUINoRootUtils updates){
        this.context= context;
        this.resolver= context.getContentResolver();
        this.updateUINoRootUtils = updates;
    }

    public void createNew(ObjectDocumentFile selected_object_file){
        showMsg(log_msg,"createNew Called :");

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Create New ");
        builder.setMessage("Choose what to create ");
        EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setPositiveButton("New Folder", (dialog, which) -> {
            AlertDialog.Builder builder1 =  new AlertDialog.Builder(context)
                    .setTitle("Enter Folder Name")
                    .setView(input)
                    .setPositiveButton("Create", (dialog14, which14) -> {
                        try {
                            DocumentsContract.createDocument(resolver, selected_object_file.uri, DocumentsContract.Document.MIME_TYPE_DIR, input.getText().toString());
                        } catch (FileNotFoundException e) {
                            showMsg(log_msg, "Error Creating new Folder : " + e);
                        }
                    }).setNegativeButton("Cancel", (dialog13, which13) -> dialog13.dismiss()) ;
            builder1.show();
        });

        builder.setNegativeButton("New File", (dialog, which) -> {
            AlertDialog.Builder builder2 = new AlertDialog.Builder(context)
                    .setTitle("Enter File Name")
                    .setView(input)
                    .setPositiveButton("Create", (dialog12, which12) -> {
                        try {
                            DocumentsContract.createDocument(resolver, selected_object_file.uri, null, input.getText().toString());
                        } catch (FileNotFoundException e) {
                            showMsg(log_msg, "Error Creating new Folder : " + e);
                        }
                    })
                    .setNegativeButton("Cancel", (dialog1, which1) -> dialog1.dismiss()) ;
        builder2.show();
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
        builder.setPositiveButton("Done", (dialog, which) -> {
            try {
                    DocumentsContract.renameDocument(context.getContentResolver(), obj.uri, input.getText().toString());
                    showMsg(textview_msg, "Renamed successfully " + obj.file_name);
                }catch(FileNotFoundException e) {
                    showMsg(log_msg, " Unable to rename " + obj.file_name + " Error : " + e);
                }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
        int OPERATION_RENAME = 209;
        updateUINoRootUtils.taskCompletedAction(OPERATION_RENAME, obj);
    }

    public void installApkList(List<ObjectDocumentFile> flist){
        this.myOperationAsyncTask = new OperationAsyncTasks(OPERATION_INSTALL_APKS);
        showMsg(log_msg,"Calling Asynctask on deleteDocument : list received size : "+flist.size());
        this.myOperationAsyncTask.execute(new ArrayList<>(flist));

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
            updateUINoRootUtils.updateTextView(str);
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

        int REQUEST_CODE_EXTSD_CARD_PATH = 2323;
        if (requestCode == REQUEST_CODE_EXTSD_CARD_PATH) {
            if (resultCode == RESULT_OK) {
                Uri uri_extsd = data.getData();
                ActivityMain.prefEditExternalStorages.putString(uri_extsd.getLastPathSegment(), uri_extsd.toString()).commit();
                showMsg(log_msg, "Added : " + uri_extsd.getPath());
                // Persist access permissions.
                final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                context.getContentResolver().takePersistableUriPermission(uri_extsd, takeFlags);

            }
        }
    }

    public class OperationAsyncTasks extends AsyncTask<Object,String,Void> {

        final int OPERATION_TYPE;
        ObjectOperation myOperationObject;
        List<ObjectDocumentFile> list;
        ObjectDocumentFile targetFolderObjFile;

        OperationAsyncTasks(int op_type) {
            super();
            OPERATION_TYPE = op_type;
            Log.i(TAG,"inAsyncTAsk "+op_type);
        }

        @Override
        protected Void doInBackground(Object... objects) {

            switch(OPERATION_TYPE){

                case OPERATION_DELETE:{
                    list = (List<ObjectDocumentFile>) objects[0];
                    targetFolderObjFile = list.get(0);
                    deleteTask();
                    break;
                }

                case OPERATION_COPY:{
                    list = (List<ObjectDocumentFile>) objects[0];
                    targetFolderObjFile = (ObjectDocumentFile) objects[1];
                    copyTask();
                    break;
                }

                case OPERATION_MOVE:{
                    list = (List<ObjectDocumentFile>) objects[0];
                    targetFolderObjFile = (ObjectDocumentFile) objects[1];
                    moveTask();
                    break;
                }

                case OPERATION_INSTALL_APKS:{
                    list = (List<ObjectDocumentFile>) objects[0];
                    installApksTask();
                    break;
                }

            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            
            if(adapter_operations_list != null){
                adapter_operations_list.notifyDataSetChanged();
            }
            
            updateUINoRootUtils.refreshList(true);
            showMsg(values[0],values[1]);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            showMsg(log_msg," in OnPostExecute : Operation Finished = "+OPERATION_TYPE);

            if(OPERATION_TYPE == OPERATION_COPY || OPERATION_TYPE == OPERATION_DELETE){
                showMsg(log_msg," in OnPostExecute : taskCompleted Action Called "+OPERATION_TYPE);
                updateUINoRootUtils.taskCompletedAction(OPERATION_TYPE, targetFolderObjFile);
            }
        }

        private void deleteTask(){

            ObjectDocumentFile obj;
            Log.i(TAG,"IN ASYNCTASK FOUND DELETE OPERATION : list size :"+ list.size());
            myOperationObject = new ObjectOperation(context, OPERATION_ID,"Delete Operation ",list.size());
            ActivityOperations.operationsHashMapList.put(OPERATION_ID,myOperationObject);
            int k;
            for(k=1; k<=list.size(); k++)
            {
                obj = list.get(k-1);
                Log.i(TAG,"Deleting  "+obj.uri);
                try{
                    myOperationObject.showNotification(k+"/"+list.size()+" - Deleting "+obj.file_name,k,(k*100)/list.size());
                    DocumentsContract.deleteDocument(context.getContentResolver(),obj.uri);
                    publishProgress(textview_msg,"Deleted successfully "+obj.file_name);
                }catch(Exception e) {
                    showMsg(log_msg,"Unable to delete "+obj.file_name+". Error : "+e);
                }
                if(isCancelled()){
                    break;
                }
            }
            k--;
            myOperationObject.cancelNotification();
            //myOperationObject.showNotification(k+" files DELETED successfully.",k,list.size(),k);
           // updateUINoRootUtils.updateTextView(k+" files DELETED successfully.");
        }

        private void copyTask(){
            ObjectDocumentFile obj;
            myOperationObject = new ObjectOperation(context, OPERATION_ID,"Copying files to "+targetFolderObjFile.file_name,list.size());
            ActivityOperations.operationsHashMapList.put(OPERATION_ID,myOperationObject);
            int i;
            DocumentFile temp;
            for(i=1;i<=list.size();i++)
            {
                obj = list.get(i-1);
                Log.i(TAG,"Copying  "+obj.uri +" to "+ targetFolderObjFile.uri);
                try {
                    myOperationObject.showNotification(i+"/"+list.size()+" Copying "+obj.file_name,i,(i*100)/list.size());
                    publishProgress(textview_msg,"Copying "+obj.file_name);
                    obj.file_doc = DocumentFile.fromTreeUri(context,obj.uri);
                    copyDocFile(obj, targetFolderObjFile,i);
                    publishProgress(textview_msg,"Copied successfully "+obj.file_name);
                }catch(Exception e) {
                    showMsg(log_msg,"Unable to copy "+obj.file_name+". Error : "+e);
                }
                if(isCancelled()){
                    break;
                }
            }
            i--;
            myOperationObject.cancelNotification();
        }

        private void moveTask(){
            ObjectDocumentFile obj;
            myOperationObject = new ObjectOperation(context, OPERATION_ID,"Moving Files to "+ targetFolderObjFile.file_name,list.size());
            Log.i(TAG,"IN ASYNCTASK FOUND MOVE OPERATION ");
            ActivityOperations.operationsHashMapList.put(OPERATION_ID,myOperationObject);
            int i;
            for(i=1;i<=list.size();i++)
            {
                obj = list.get(i-1);
                Log.i(TAG,"Moving  "+obj.uri +" to "+ targetFolderObjFile.uri);
                try {
                    myOperationObject.showNotification(i+"/"+list.size()+" - Moving "+obj.file_name,i,(i*100)/list.size());
                    DocumentsContract.moveDocument(context.getContentResolver(), obj.uri, obj.parent_uri, targetFolderObjFile.uri);
                    publishProgress(textview_msg,"Moved successfully "+obj.file_name);
                }catch(Exception e) {
                    showMsg(log_msg,"Unable to MOVE "+obj.file_name+". Error : "+e);
                }
                if(isCancelled()){
                    break;
                }
            }
            i--;
            myOperationObject.cancelNotification();
            //myOperationObject.showNotification(i+" files MOVED successfully to ",i,list.size(),i);
        }

        private void installApksTask()
        {
            ObjectDocumentFile obj;
            Intent intent;
            myOperationObject = new ObjectOperation(context, OPERATION_ID,"Install Operation ",list.size());
            Log.i(TAG,"IN ASYNCTASK - INSTALL OPERATION ");
            ActivityOperations.operationsHashMapList.put(OPERATION_ID,myOperationObject);
            int i;
            for(i=1;i<=list.size();i++){
                obj = list.get(i);
                myOperationObject.showNotification(i+"/"+list.size()+" - Installing "+obj.file_name,i,(i*100)/list.size());
                intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                intent.setData(obj.uri);
                intent.setType("application/vnd.android.package-archive");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(intent);
            }
            i--;
            myOperationObject.cancelNotification();
            //myOperationObject.showNotification(i+" files installed successfully.",i,+list.size(),i);
        }

        private boolean copyDocFile(ObjectDocumentFile srcFile, ObjectDocumentFile targetFolder,int currFileNum){

            InputStream in = null;
            OutputStream out = null;
            Uri targetFileUri = null;

            BufferedInputStream buf_in = null;
            BufferedOutputStream buf_out = null;

            boolean result ;
            int currSize=0;
            int totalSize;

            try{
                targetFileUri = DocumentsContract.createDocument(resolver,targetFolder.uri,srcFile.mime_type, srcFile.file_name);
            }catch (FileNotFoundException e) {
                showMsg(log_msg," Unable create target document uri");
            }

            try{
                in = resolver.openInputStream(srcFile.uri);
                buf_in = new BufferedInputStream(in);
                showMsg(log_msg," Uri to copy from : "+srcFile.uri);

                out = resolver.openOutputStream(targetFileUri);
                buf_out = new BufferedOutputStream(out);
                showMsg(log_msg,"uri to copy to : "+targetFileUri);

                totalSize = (int) srcFile.size_long;
                int onePercentSize = totalSize/100;
                int percent=0;
                int tempSize = 0;
                byte[] buffer = new byte[1024];
                int data;

                while((data = buf_in.read()) != -1){
                    buf_out.write(data);
                    currSize = currSize+1;
                    tempSize = tempSize+1;

                    if(tempSize >= onePercentSize){
                        percent++;
//                        showMsg(log_msg, "Percent Copied: "+percent);
                        myOperationObject.showNotification(currFileNum+"/"+list.size()+" - "+convertSizeToFormat(currSize)+"/"+convertSizeToFormat(totalSize)+
                                " - "+srcFile.file_name ,currFileNum, percent);
                        if(adapter_operations_list != null){
                            ActivityOperations.adapter_operations_list.notifyDataSetChanged();
                        }
                        tempSize=0;
                    }
                }

                buf_out.flush();
                result=true;
//                showMsg(log_msg, "Total Size : "+totalSize);
//                showMsg(log_msg, "Size Copied : "+currSize);
//                showMsg(log_msg, "Final Percent Copied: "+percent);


            }catch(Exception e) {
                showMsg(log_msg, "copy file : "+e);
            }finally{
                try {
                    if(null!=buf_in){
                        buf_in.close();
                    }
                    if(null!=buf_out){
                        buf_out.close();
                    }
                    if(null!=in){
                        in.close();
                    }
                    if(null!=out){
                        out.close();
                    }
                    Log.i(TAG, "Streams Closed successfully.");
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }

            return true;
        }

        String convertSizeToFormat(long length) {
            final long KB = 1024;
            final long MB = 1024 * 1024;
            final long GB = 1024 * 1024 * 1024;

            final DecimalFormat format = new DecimalFormat("###.##");

            if (length > GB) {
                return format.format((float)length / GB) + " GB";
            }
            if (length > MB) {
                return format.format((float)length / MB) + " MB";
            }
            if (length > KB) {
                return format.format((float)length / KB) + " KB";
            }

            return format.format(length) + " Bytes";
        }

    }

}

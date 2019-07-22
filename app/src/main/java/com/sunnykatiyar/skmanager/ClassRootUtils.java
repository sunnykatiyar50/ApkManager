package com.sunnykatiyar.skmanager;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;

import com.sunnykatiyar.skmanager.ui.main.FragmentRootBrowser;
import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ClassRootUtils {

    private final Context context;

    private final String key_root_value = FragmentSettings.key_root_access;
    private final boolean root_access;

    private final String TAG = "MYAPP : ROOT_FILE_OPERATION";
    private ContentResolver resolver;

    private final String textview_msg ="textview_msg";
    private final String log_msg = "log_msg";
    private final String toast_msg ="toast_msg";

    private int OPERATION_ID=0;
    public boolean isRunning = false;
    final int NO_OPERATION = 0;
    private boolean   isCancelled = false;
    private final int OPERATION_COPY = 201;
    private final int OPERATION_MOVE  = 204;
    private final int OPERATION_RENAME = 209;
    private final int OPERATION_DELETE = 212;
    private final int OPERATION_INSTALL_APKS = 215;
    final int GET_FOLDER_FILES = 218;
    private final int GET_FOLDER_FILES_BY_SHELL = 230;

    private final notifyUIAboutOperation notifyUI;
    private final SharedPreferences sharedPrefSettings;
    SharedPreferences.Editor prefAppSettings;
    private LoadRootTasks runTask;
    private final List<ObjectFile> shellFileList = new ArrayList<>();
    private final List<File> empty_files = new ArrayList<>();
    List<ObjectFile> filteredFileList = new ArrayList<>();
    private final List<ObjectFile> allFileList = new ArrayList<>();
    String value_show_hidden;




    public interface notifyUIAboutOperation {
        void updateTextView(String msg_type,String msg);
        void searchCompleted(String path, List<ObjectFile> list);
        void setprogressBar(boolean visible);
        void taskCompletedAction(int type,String path);
    }

    public ClassRootUtils(Context context, notifyUIAboutOperation act, int id){
       this.context = context;
       this.OPERATION_ID =id;
       notifyUI = act;
       sharedPrefSettings = context.getSharedPreferences(context.getResources().getString(R.string.sharedPref_settings), Context.MODE_PRIVATE);
       root_access = sharedPrefSettings.getBoolean(key_root_value,false);
    }

    public ClassRootUtils(Context context, notifyUIAboutOperation act){
        this.context = context;
        notifyUI = act;
        sharedPrefSettings = context.getSharedPreferences(context.getResources().getString(R.string.sharedPref_settings), Context.MODE_PRIVATE);
        root_access = sharedPrefSettings.getBoolean(key_root_value,false);
    }
//-------------------------------------MULTI FILE OPERATIONS----------------------------------------------------------------

    public void getFolderFilesFromShell(String path, boolean showHidden){

        runTask = new LoadRootTasks(GET_FOLDER_FILES_BY_SHELL,null) ;
        runTask.execute(path, showHidden);
       // Log.i(TAG, path +" is returning "+shellFileList.size()+" files.");

    }

    public void rootCopy(List<ObjectFile> srcPathList, String dest_path){
        runTask = new LoadRootTasks(OPERATION_COPY,  new ArrayList<>(srcPathList)) ;
        runTask.execute(dest_path);
        Log.e(TAG,"Executing ROOT COPY Operation : ");
    }

    public void rootMove(List<ObjectFile> srcPathList, String dest_path){
        runTask = new LoadRootTasks(OPERATION_MOVE,  new ArrayList<>(srcPathList));
        runTask.execute(dest_path);
        Log.e(TAG,"Executing ROOT Move Operation : ");
    }

    public void rootRename(List<ObjectFile> srcPathList, String dest_path){
        runTask = new LoadRootTasks(OPERATION_RENAME,srcPathList);
        runTask.execute(dest_path);
        Log.e(TAG,"Executing ROOT Rename Operation: ");
    }

    public void deleteFiles(List<ObjectFile> srcPathList) {
        runTask = new LoadRootTasks(OPERATION_DELETE, new ArrayList<>(srcPathList));
        runTask.execute();
        Log.e(TAG,"Executing ROOT Delete Operation : ");
    }

    public void  installApksList(List<ObjectFile> apksList){
        runTask = new LoadRootTasks(OPERATION_INSTALL_APKS, new ArrayList<>(apksList));
        runTask.execute();
    }

//------------------------------------------SINGLE FILE OPERATIONS-----------------------------------------------------------

    public void createNew(ObjectFile objectfile){
        notifyUI.updateTextView(log_msg,"createNew Called :");

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
                            String command = "mkdir \"" + Paths.get(objectfile.path, input.getText().toString()) + "\"";
                            Shell.su(command).exec();
                        } catch (Exception e) {
                            notifyUI.updateTextView(log_msg, "Error Creating new Folder : " + e);
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
                            String command = "touch \"" + Paths.get(objectfile.path, input.getText().toString()) + "\"";
                            Shell.su(command).exec();
                        } catch (Exception e) {
                            notifyUI.updateTextView(log_msg, "Error Creating new Folder : " + e);
                        }
                    })
                    .setNegativeButton("Cancel", (dialog1, which1) -> dialog1.dismiss()) ;
            builder2.show();
        });
        builder.show();
    }

    public void installApk(ObjectFile file){
        long apk_size;
        String command;
        try{
            apk_size = file.long_size;
            command = "cat \"" + file.path + "\"|pm install -S " + apk_size;
            //  Log.i(TAG, "COMMAND :"+ command);
            Shell.su(command).exec();
            notifyUI.updateTextView(textview_msg," Installed "+file.name+" successfully.");
        }catch (Exception ex) {
            notifyUI.updateTextView(textview_msg,file.name+" installation failed.");
            Log.e(TAG, " ROOT INSTALL ERROR of " + file.name + " \nError : " + ex);
        }
    }

    public void rootRenameFile(ObjectFile file){

        EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(file.name);
        input.selectAll();
        AlertDialog.Builder builder1 =  new AlertDialog.Builder(context)
                .setTitle("Enter New Name")
                .setView(input)
                .setPositiveButton("Done", (dialog, which) -> {
                    try {
                        String command = "mv \""+ file.path+"\" \""+Paths.get(file.parent, input.getText().toString());
                        Shell.su(command).exec();
                        notifyUI.updateTextView(toast_msg,"Renamed File succesfully : ");
                    }catch(Exception e) {
                        notifyUI.updateTextView(toast_msg,"Error Renaming File : "+"\nEnter a Valid Name");
                    }
                }).setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()) ;
        builder1.show();
    }

    public List<ObjectFile> getFolderFiles(File file){
        List<ObjectFile> list = new ArrayList<>();

        if(file.isDirectory()){
            for(File f:file.listFiles()){
                list.add(new ObjectFile(f));
            }
        }
        return list;
    }

    private List<ObjectFile> getAllSubfolderFiles(File folder){

        if(folder.isDirectory()){
            for(File f : folder.listFiles()){
                if(f.isFile()){
                    allFileList.add(new ObjectFile(f));
                }else{
                    getAllSubfolderFiles(f);
                }
            }
        }else{
            allFileList.add(new ObjectFile(folder));
        }

        return allFileList;
    }
    
    private List<File> rootSearchEmptyFolders(File file){

        if(file.listFiles().length>0){
            for(File f:file.listFiles())
            {
                if(f.isDirectory()){
                    if(f.listFiles().length==0){
                        empty_files.add(f);
                    }else{
                        rootSearchEmptyFolders(f);
                    }
                }
            }
        }else{
            empty_files.add(file);
        }
        return empty_files;
    }

    public void deleteEmptyFolders(List<File> fileList){

        if(!fileList.isEmpty()){
            if(root_access){
                for(File f:fileList){
                    try{
                        String command = "rmdir \""+f.getAbsolutePath()+"\"";
                        Shell.su(command).exec();
                    }catch(Exception ex){
                        Log.e(TAG,"Error copying files by root method : "+ex);
                    }
                }
            }else{
                Log.e(TAG,"Error getting root access ");
            }
        }else{
            Log.e(TAG,"EmptyList ");
        }
    }

    public boolean copyFileTo(File src, File dest){

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
                src_channel.close();
                dest_channel.close();
                Log.i(TAG,"Streams Closed Succesfully.");
            } catch (IOException ex) {
                Log.e(TAG, "Error Closing Streams : " + ex);
            }
        }

        return result;
    }

    private void setStreams(Uri src_uri,Uri dest_uri){
        FileInputStream fis;
        FileOutputStream fos;

        try {
            fis = new FileInputStream(new File(src_uri.getPath()));
            fos = new FileOutputStream(new File(dest_uri.getPath()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void cancelTask(){
        if(runTask !=null){
            isCancelled = true;
            runTask.cancel(true);
            notifyUI.updateTextView(log_msg,"in cancelTask : TASK CANCELLED");
        }
        else{
            notifyUI.updateTextView(log_msg, "NO TASK RUNNING");
        }
    }

    private class LoadRootTasks extends AsyncTask{

        int OPERATION_TYPE = 0;
        long finishTime;
        String searchPath;
        String targetPath;
        ObjectOperation myOperationObject;
        final List<ObjectFile> list;
        long startTime ;

        LoadRootTasks(int ops, List<ObjectFile> list){
            super();
            OPERATION_TYPE =  ops;
            this.list = list;
            Log.i(TAG,"in Asynctask Opertaion "+OPERATION_TYPE);
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            startTime = new Date().getTime();
            isRunning = true;
            if(OPERATION_TYPE == GET_FOLDER_FILES_BY_SHELL){
                notifyUI.setprogressBar(true);
            }
        }

        @Override
        protected Object doInBackground(Object[] objects) {

            int GET_EMPTY_FOLDERS = 227;
            int GET_FILTERED_FILES = 224;
            int GET_SUBFOLDERS_FILES = 221;
            switch(OPERATION_TYPE){

                case GET_FOLDER_FILES_BY_SHELL:{
                    searchPath = (String) objects[0];
                    getSendFilesFromfolder(searchPath);
                    break;
                }

                case OPERATION_COPY :{
                    Log.i(TAG," inDoInBG :OPERATION COPY");
                    targetPath = (String) objects[0] ;
                    copyFileTask(targetPath);
                    break;
                }

                case OPERATION_MOVE :{
                    Log.i(TAG," inDoInBG :OPERATION MOVE");
                    targetPath = (String) objects[0] ;
                    moveFilesTask(targetPath);
                    break;
                }

                case OPERATION_RENAME:{
                    targetPath = (String) objects[0] ;

                    break;
                }

                case OPERATION_DELETE:{
                    Log.i(TAG," inDoInBG :OPERATION DELETE");
                    deleteFilesTask();
                    break;
                }

                case OPERATION_INSTALL_APKS:{
                    Log.i(TAG," inDoInBG :OPERATION INSTALL APKS");
                    installApksTask();
                   break;
                }


            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            finishTime = new Date().getTime();
            isRunning =false;
            Log.i(TAG, "PostExecute :ISTASKCANCELLED variable : "+isCancelled);

            if(!isCancelled & OPERATION_TYPE==GET_FOLDER_FILES_BY_SHELL){
                notifyUI.searchCompleted(searchPath, shellFileList);
                Log.i(TAG, "PostExecute : Returning "+shellFileList.size()+" files of : "+searchPath);
//              notifyUI.setprogressBar(false);
            }

            if(OPERATION_TYPE == OPERATION_COPY || OPERATION_TYPE==OPERATION_DELETE || OPERATION_TYPE==OPERATION_MOVE){
                 notifyUI.taskCompletedAction(OPERATION_TYPE,targetPath);
            }
           
            Log.i(TAG, "PostExecute : Files Found : "+shellFileList.size()+" files.");
            notifyUI.updateTextView(log_msg, "Times to Open "+(finishTime-startTime)/1000+"s");
        }

        @Override
        protected void onProgressUpdate(Object[] values) {
            super.onProgressUpdate(values);
            notifyUI.updateTextView(textview_msg, (String)values[0]);
        }
        
        private void getSendFilesFromfolder(String searchPath){
            List<String> oup;
            int count = 0 ;
            String key_show_hidden = FragmentRootBrowser.key_show_hidden;
            boolean showHidden = FragmentRootBrowser.sharedPrefRootBrowser.getBoolean(key_show_hidden,true);
            Log.i(TAG," in GET_FOLDER_FILES_BY_SHELL");
            if(new File(searchPath).exists())
            {
                try {
                    if(showHidden){
                        String command = "ls -A \""+searchPath+"\"";
                        // Log.i(TAG," Command : "+command);
                        oup = Shell.su(command).exec().getOut();
                        notifyUI.updateTextView(log_msg, " Output Size :" + oup.size());
                        //Log.i(TAG,"OUP : "+oup.get(0));
                        for(String str : oup){
                            if(!str.contains("denied")){
                                shellFileList.add(new ObjectFile(searchPath+'/'+str));
                                publishProgress(count++ +" : "+ str);
                                if(isCancelled()){
                                    break;
                                }
                            }

                        }
                    }else{
                        String command = "ls \""+searchPath+"\"";
                        //Log.i(TAG," Command : "+command);
                        oup = Shell.su(command).exec().getOut();
                        //Log.i(TAG,"OUP : "+oup.get(0));
                        notifyUI.updateTextView(log_msg, "Output Size :" + oup.size());
                        for (String str : oup) {
                            if(!str.contains("denied")){
                                shellFileList.add(new ObjectFile(searchPath+'/'+str));
                                publishProgress(count++ + " : "+str);
                                if(isCancelled()){
                                    break;
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    Log.i(TAG," Error Getting FilePath : Ex : "+ex);
                }
            }
        }

        private void copyFileTask(String dest_path){
            myOperationObject = new ObjectOperation(context, OPERATION_ID, "Operation Copy"+" to "+dest_path, list.size());
            ActivityOperations.operationsHashMapList.put(OPERATION_ID, myOperationObject);
            int i;
            for(i=1; i<= list.size(); i++){
                ObjectFile obj = list.get(i-1);
                try{
                    myOperationObject.showNotification(i+"/"+ list.size()+" - Copying "+obj.name, i,(i*100)/list.size());
                    String command = "cp \"" + obj.path + "\" \"" + dest_path + "\"";
                    Shell.su(command).exec();
                    publishProgress(textview_msg, " Copied Successfully " + obj.path);
                } catch (Exception ex) {
                    Log.e(TAG, "Error copying files by root method : " + ex);
                }
                if(isCancelled()){
                    Log.e(TAG, "Task Cancelled " );
                    break;
                }
            }
            i--;
            myOperationObject.cancelNotification();

//          myOperationObject.showNotification(i+" files COPIED successfully to "+dest_path,i, (i*100)/list.size());
        }

        private void moveFilesTask(String dest_path){
            myOperationObject = new ObjectOperation(context, OPERATION_ID, "Operation Move to "+dest_path, list.size());
            ActivityOperations.operationsHashMapList.put(OPERATION_ID, myOperationObject);
            int i;
            for(i=1; i<= list.size(); i++){
                ObjectFile src_file = list.get(i-1);
                try {
                    myOperationObject.showNotification(i+"/"+ list.size()
                            +" - Moving "+src_file.name, i,(i*100)/list.size());
                    String command = "mv \"" + src_file.path + "\" \"" + dest_path + "\"";
                    Shell.su(command).exec().getOut();
                    publishProgress(textview_msg, " Copied Successfully " + src_file.path);
                } catch (Exception ex) {
                    Log.e(TAG, "Error copying files by root method : " + ex);
                }
                if(isCancelled()){
                    break;
                }
            }
            i--;
            myOperationObject.cancelNotification();

//            myOperationObject.showNotification(i+" files MOVED successfully",i, (i*100)/list.size());
        }

        private void installApksTask(){
            long apk_size;
            String command;
            ObjectFile file;
            myOperationObject = new ObjectOperation(context, OPERATION_ID, "Deleting Files", list.size());
            ActivityOperations.operationsHashMapList.put(OPERATION_ID, myOperationObject);
            int i;
            for(i=1; i<= list.size(); i++){
                file = list.get(i);
                myOperationObject.showNotification(i+"/"+ list.size()+" Installing "+ file.name, i, (i*100)/list.size());
                try{
                    apk_size = file.long_size;
                    command = "cat \"" + file.path + "\"|pm install -S " + apk_size;
                    //Log.i(TAG, "COMMAND :"+ command);
                    Shell.su(command).exec();
                    publishProgress(textview_msg, " Installed " + file.name + " successfully.");
                }catch (Exception ex) {
                    publishProgress(textview_msg, file.name + " installation failed.");
                    Log.e(TAG, " ROOT INSTALL ERROR of " + file.name + " \nError : " + ex);
                }
            }
            i--;
            myOperationObject.cancelNotification();

//          myOperationObject.showNotification("Apk Installation Finished.",i, (i*100)/list.size());
            publishProgress(textview_msg,"");
        }

        private void deleteFilesTask(){
            ObjectFile src_file;
            Log.i(TAG,"Deleting Files"+ list.size());
            myOperationObject = new ObjectOperation(context, OPERATION_ID, "Deleting Files", list.size());
            ActivityOperations.operationsHashMapList.put(OPERATION_ID, myOperationObject);
            int i=0;
            for(i=1; i<= list.size(); i++){
                try{
                    src_file = list.get(i-1);
                    myOperationObject.showNotification(i+"/"+ list.size()+" Deleting "+ src_file.name, i,(i*100)/list.size());
                    String command = "rm -rf \""+src_file.path+"\"";
                    Shell.su(command).exec();
//                  notifyUI.updateTextView(textview_msg, "Deleted Successfully "+src_file.path);
                }catch(Exception ex){
                    Log.e(TAG,"Error deleting files by root method : "+ex);
                }
                if(isCancelled()){
                    break;
                }
            }
            i--;
            myOperationObject.cancelNotification();

//            myOperationObject.showNotification("Delete Operation Finished.",i, (i*100)/list.size());
        }

        private void renameFilesTask(String dest_path){
            myOperationObject = new ObjectOperation(context, OPERATION_ID, "Operation Rename", list.size());
            ActivityOperations.operationsHashMapList.put(OPERATION_ID, myOperationObject);
            int i=0;
            for(i=1; i<= list.size(); i++){
                try{
                    ObjectFile src_file = list.get(i-1);
                    myOperationObject.showNotification(i+"/"+ list.size()+" Renaming "+ src_file.name,i, (i*100)/list.size());
                    String command = "mv \""+src_file.path+"\" \""+dest_path+"\"";
                    // Shell.su(command).exec().getOut();
                    publishProgress(textview_msg, " Copied Successfully "+src_file.path);
                }catch(Exception ex){
                    Log.e(TAG,"Error copying files by root method : "+ex);
                }
                if(isCancelled()){
                    break;
                }
            }
            i--;
            myOperationObject.cancelNotification();
//            myOperationObject.showNotification(i+" files Renamed successfully.",i, (i*100)/list.size());
        }

    }

}

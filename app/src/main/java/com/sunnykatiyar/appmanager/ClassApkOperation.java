package com.sunnykatiyar.appmanager;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;

import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class ClassApkOperation {

    Context context;
    //Activity activity;
    ObjectApkFile apkItem;
    String apk_formatted_name;
    String app_formatted_name;
    boolean result = false;
    public File parent_folder;
    ObjectAppPackageName appItem;
    File source_file;
    File dest_file;
    String command;
    ContentResolver resolver;

    final String TAG = "MYAPP : MOVE_APK  : ";

    final String name_part_1 = FragmentApkSettings.name_part_1;
    final String name_part_2 = FragmentApkSettings.name_part_2;
    final String name_part_3 = FragmentApkSettings.name_part_3;
    final String name_part_4 = FragmentApkSettings.name_part_4;
    final String name_part_5 = FragmentApkSettings.name_part_5;
    final String name_part_6 = FragmentApkSettings.name_part_6;
    final String name_part_7 = FragmentApkSettings.name_part_7;
    final String name_part_8 = FragmentApkSettings.name_part_8;
    final String name_format_data_saved = FragmentApkSettings.name_format_data_saved;

    public static final String key_repository_folder = FragmentSettings.key_repository_folder;
    public static final String key_root_access = FragmentSettings.key_root_access;
    public static final String path_not_set = FragmentSettings.path_not_set;
    public static final String key_export_apk_enable = FragmentSettings.key_export_apk_enable;
    public static final String key_export_apk_uri = FragmentSettings.key_export_apk_uri;
    public String value_export_apk_path;
    public boolean value_export_apk_enable;

    public static boolean root_access;

    static {
        Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR);
        Shell.Config.verboseLogging(BuildConfig.DEBUG);
        Shell.Config.setTimeout(10);
    }

    //------------------------------------RENAMING APK_____________________________________________________

    public ClassApkOperation(ObjectApkFile item, Context c){

        this.apkItem = item;
        this.context = c ;
       //this.activity = activity;
        root_access = ActivityMain.sharedPrefSettings.getBoolean(key_root_access,false);
        Log.i(TAG," ROOT_ACCESS : "+root_access);
        resolver = c.getContentResolver();
        source_file = apkItem.file;
        setNameFormatFromApk();

    }

    private void setNameFormatFromApk() {
        if (ActivityMain.sharedPrefApkManager.contains(name_format_data_saved)) {

            int part1 = ActivityMain.sharedPrefApkManager.getInt(name_part_1, 1);
            Log.i(TAG, "Part 1 :" + part1);

            String part2 = ActivityMain.sharedPrefApkManager.getString(name_part_2, "_v");
            Log.i(TAG, "Part 2 :" + part2);

            int part3 = ActivityMain.sharedPrefApkManager.getInt(name_part_3, 2);
            Log.i(TAG, "Part 3 :" + part3);

            String part4 = ActivityMain.sharedPrefApkManager.getString(name_part_4, "_");
            Log.i(TAG, "Part 4 :" + part4);

            int part5 = ActivityMain.sharedPrefApkManager.getInt(name_part_5, 3);
            Log.i(TAG, "Part 5 :" + part5);

            String part6 = ActivityMain.sharedPrefApkManager.getString(name_part_6, "");
            Log.i(TAG, "Part 6 :" + part6);

            int part7 = ActivityMain.sharedPrefApkManager.getInt(name_part_7, 0);
            Log.i(TAG, "Part 7 :" + part7);

            String part8 = ActivityMain.sharedPrefApkManager.getString(name_part_8, "");
            Log.i(TAG, "Part 8 :" + part8);

            this.apk_formatted_name = getNameFromApkFile(apkItem, part1) + part2 + getNameFromApkFile(apkItem, part3) + part4 + getNameFromApkFile(apkItem, part5) + part6 + getNameFromApkFile(apkItem, part7) + part8 + ".apk";
        }

        Log.i(TAG," APK Formatted Name : "+this.apk_formatted_name);
    }

    private String getNameFromApkFile(ObjectApkFile ld, int i) {

    //   Log.i(TAG,"GetNAme fromItem : "+ld.app_name);
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

    public void RenameThisApk() {
        parent_folder = apkItem.file.getParentFile();
        Log.i(TAG, "Dest parent_folder : " + parent_folder);

        dest_file = new File(parent_folder + "/" + this.apk_formatted_name);
        Log.i(TAG, "Dest file : " + dest_file);

//--------------------------RENAME APK WITHOUT ROOT-----------------------------------
        if(!root_access){
            if(parent_folder.canWrite()){
                Log.i(TAG, "parent_folder : " + parent_folder);
                this.result = (source_file).renameTo(dest_file);
                if(result){
                    Log.i(TAG," : Renamed \"" + source_file.getName() + "\" successfully ");
                }else{
                    Log.i(TAG," : Renaming failed for \"" + source_file.getName());
                }
            }
            else{
                Log.i(TAG," : Renaming failed for \"" + source_file.getName() + "\". Cannot Write to "+ parent_folder.getAbsolutePath());
            }

//--------------------------RENAME APK VIA ROOT----------------------------------------
        }else if(root_access){
            command = "mv \""+ source_file +"\" \""+ dest_file +"\"";
            Log.i(TAG,"COMMAND : "+command);
            try{
                Shell.su(command).exec();
                this.result = true;
                Log.i(TAG, source_file.getAbsolutePath()+" is renamed to  "+this.apk_formatted_name);
            }catch(Exception ex){
                this.result=false;
                Log.e(TAG," : Renaming failed for : \"" + source_file.getAbsolutePath()+"Error Exception : "+ex);
            }
        }

        Log.i(TAG, "Renaming " + apkItem.file_name + " : \"" + dest_file + "\" - " + result);
    }

    
//------------------------------------EXTRACTING APK_____________________________________________________

    public ClassApkOperation(ObjectAppPackageName appitem, Context c){
        this.appItem = appitem;
        this.context = c ;
       // this.activity = activity;
        root_access = ActivityMain.sharedPrefSettings.getBoolean(key_root_access,false);
        Log.i(TAG," ROOT_ACCESS : "+root_access);
        resolver = c.getContentResolver();
    }

    private void setNameFormatFromApp() {
        if (ActivityMain.sharedPrefApkManager.contains(name_format_data_saved)) {

            int part1 = ActivityMain.sharedPrefApkManager.getInt(name_part_1, 1);
            Log.i(TAG, "Part 1 :" + part1);

            String part2 = ActivityMain.sharedPrefApkManager.getString(name_part_2, "_v");
            Log.i(TAG, "Part 2 :" + part2);

            int part3 = ActivityMain.sharedPrefApkManager.getInt(name_part_3, 2);
            Log.i(TAG, "Part 3 :" + part3);

            String part4 = ActivityMain.sharedPrefApkManager.getString(name_part_4, "_");
            Log.i(TAG, "Part 4 :" + part4);

            int part5 = ActivityMain.sharedPrefApkManager.getInt(name_part_5, 3);
            Log.i(TAG, "Part 5 :" + part5);

            String part6 = ActivityMain.sharedPrefApkManager.getString(name_part_6, "");
            Log.i(TAG, "Part 6 :" + part6);

            int part7 = ActivityMain.sharedPrefApkManager.getInt(name_part_7, 0);
            Log.i(TAG, "Part 7 :" + part7);

            String part8 = ActivityMain.sharedPrefApkManager.getString(name_part_8, "");
            Log.i(TAG, "Part 8 :" + part8);

            this.app_formatted_name = getNameFromApp(appItem, part1) + part2 + getNameFromApp(appItem, part3)
                    + part4 + getNameFromApp(appItem, part5) + part6 + getNameFromApp(appItem, part7) + part8 + ".apk";
        }

        Log.i(TAG," APP Formatted Name : "+this.app_formatted_name);
    }

    private String getNameFromApp(ObjectAppPackageName ld, int i) {
        switch (i) {
            case 0: {
                return "";
            }
            case 1: {
                Log.i(TAG,"Get App Name from Item : "+ld.app_name);
                return ld.app_name;
            }
            case 2: {
                   Log.i(TAG,"Get Version Name from Item : "+ld.app_version_name);
                return ld.app_version_name;
            }
            case 3: {
                Log.i(TAG,"Get Version Code from Item : "+ld.app_version_code);
                return ld.app_version_code;
            }
            case 4: {
                Log.i(TAG,"Get App Size from Item : "+ld.apk_size);
                return ld.apk_size;
            }
            case 5: {
                return ld.pkg_name;
            }
            default: {
                return "";
            }
        }
    }

    public void extractApk(){
        setNameFormatFromApp();
        source_file = this.appItem.apk_file;
        new ExtractTask().execute();
    }

    private class ExtractTask extends AsyncTask<Void,String,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
          extractApkOf();
          return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
             showToast(values[0]);
        }

        private void extractApkOf(){
            String parent = ActivityMain.sharedPrefSettings.getString(key_repository_folder,path_not_set);

            Log.i(TAG, "\nParent value from preference : " + parent);

            parent_folder = new File(parent);
            //DocumentFile parent_doc = DocumentFile.fromTreeUri(context, parent_uri);

            if(!parent_folder.exists() || !parent_folder.isDirectory()){
                publishProgress("\nParent value not exists : " + parent_folder);
                parent_folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            }

//--------------------------------EXTRACT APK WITHOUT ROOT-----------------------------------
            if(!root_access){

                if(!parent_folder.canWrite()){
                    publishProgress("\n Default Parent Folder not Writable : " + parent_folder);
                    parent_folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                }

                dest_file = new File(parent_folder + "/" + app_formatted_name);
                publishProgress(" Apk will be extracted as : " + dest_file);

                if(parent_folder.canWrite()){
                    copyTo(source_file, dest_file);
                }
                else{
                    //result = (source_file).renameTo(dest_file);
                    Log.i(TAG," : Renaming failed for \"" + source_file.getName() + "\". Cannot Write to "+parent_folder.getAbsolutePath());
                }

            }
//-----------------------------------ROOT EXTRACT APK-----------------------------------
            else if(root_access){
                dest_file = new File(parent_folder + "/" + app_formatted_name);
                publishProgress(" Apk will be extracted as : " + dest_file);

                command = "cp \""+ source_file +"\" \""+ dest_file +"\"";
                try{
                    Shell.sh(command).exec();
                     publishProgress(" Apk extracted successfully : " + dest_file.getAbsolutePath());
                }catch(Exception ex){
                     publishProgress("ROOT : Apk Extraction failed : "+app_formatted_name);
                }
            }

            //-------------------------------URI METHOD-----------------------------------------------------------
            Uri parent_uri = Uri.parse(ActivityMain.sharedPrefSettings.getString(key_export_apk_uri,path_not_set));
            Uri parentDocUri = DocumentsContract.buildDocumentUriUsingTree(parent_uri, DocumentsContract.getTreeDocumentId(parent_uri));
            Uri base_apk_src_uri = DocumentFile.fromFile(source_file).getUri();
            Uri target_uri;
            ContentResolver resolver = context.getContentResolver();
            String mime_type = MimeTypeMap.getSingleton().getMimeTypeFromExtension("apk");

            try{
                target_uri = DocumentsContract.createDocument(resolver, parentDocUri, mime_type, app_formatted_name);
                DocumentsContract.copyDocument(ActivityMain.resolver, base_apk_src_uri, target_uri);
                Log.i(TAG,"APK EXPORTED BY URI METHOD to: "+target_uri.getPath());
            }catch (FileNotFoundException e) {
                Log.i(TAG,"APK EXPORT ERROR URI METHOD : "+e);
            }
        }
        
    }

//    private void extractApkOf(){
//
//        String parent = ActivityMain.sharedPrefSettings.getString(key_repository_folder,path_not_set);
//        Log.i(TAG, "\nParent value from preference : " + parent);
//
//        parent_folder = new File(parent);
//
//        if(!parent_folder.exists() || !parent_folder.isDirectory()){
//            Log.i(TAG, "\nParent value not exists : " + parent_folder);
//            parent_folder = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
//        }
//
//       // dest_file = new File(parent_folder + "/" + this.apk_formatted_name);
//         dest_file = new File(parent_folder + "/" + this.apk_formatted_name);
//        Log.i(TAG, " Apk will be saved to : " + dest_file);
//
//
////--------------------------------EXTRACT APK WITHOUT ROOT-----------------------------------
//        if(!root_access){
//            //  if(dest_doc_file.canWrite()){
//            //     copyTo(source_uri, dest_uri);
//            //   Log.i(TAG,"Writing to Destination Allowed.");
//            //  }else{
//            //  this.result = (source_doc_file).renameTo(dest_doc_file);
//            //   Log.i(TAG," : Renaming failed for \"" + source_doc_file.getName() + "\". Cannot Write to "+parent_folder.getUri().getPath());
//            //}
//        }
////-----------------------------------ROOT EXTRACT APK-----------------------------------
//        else if(root_access){
//            command = "cp \""+ source_file +"\" \""+ dest_file +"\"";
//            try{
//                Shell.sh(command).exec();
//
//                showToast(" Apk extracted successfully : " + dest_file.getAbsolutePath());
//            }catch(Exception ex){
//                showToast("ROOT : Apk Extraction failed : "+apk_formatted_name);
//            }
//        }
//    }
    
    private void showToast(String str){
        Toast.makeText(ActivityMain.context,str,Toast.LENGTH_LONG).show();
        Log.i(TAG, str);
    }

    public boolean copyTo(File src, File dest){

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

}

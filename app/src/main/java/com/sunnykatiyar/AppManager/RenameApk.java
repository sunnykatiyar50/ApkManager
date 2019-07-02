package com.sunnykatiyar.AppManager;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public class RenameApk{

    Context context;
    ApkListDataItem apkItem;
    String apk_formatted_name;
    boolean result = false;
    DocumentFile parent;
    Uri parent_uri;
    DocumentFile source_doc_file;
    DocumentFile dest_doc_file;
    Uri source_uri;
    Uri dest_uri;
    String command;
    ContentResolver resolver;

    final String TAG = "FORMATAPKNAME";
    final String name_part_1 = RenameApkFragment.name_part_1;
    final String name_part_2 = RenameApkFragment.name_part_2;
    final String name_part_3 = RenameApkFragment.name_part_3;
    final String name_part_4 = RenameApkFragment.name_part_4;
    final String name_part_5 = RenameApkFragment.name_part_5;
    final String name_part_6 = RenameApkFragment.name_part_6;
    final String name_part_7 = RenameApkFragment.name_part_7;
    final String name_part_8 = RenameApkFragment.name_part_8;
    final String name_format_data_saved = RenameApkFragment.name_format_data_saved;

    public static final String key_repository_folder = AppSettingsFragment.key_repository_folder;
    public static final String key_root_access = AppSettingsFragment.key_root_access;
    public static boolean root_access;

    final String path_not_set = AppSettingsFragment.path_not_set;

    public RenameApk(ApkListDataItem item, Context c){

        this.apkItem = item;
        Log.i(TAG," APK file_doc: "+apkItem.file_doc.canRead());
        Log.i(TAG," APK file readable: "+apkItem.file.canRead());
        Log.i(TAG," APKITEM Filename : "+apkItem.file_name);
        Log.i(TAG," APKITEM Appname : "+apkItem.app_name);
        Log.i(TAG," APKITEM Size : "+apkItem.file_size);
        Log.i(TAG," APKITEM PKGname : "+apkItem.pkg_name);


        this.context = c ;
//      this.pkg_info = pkginfo;
        root_access = MainActivity.sharedPrefAppSettings.getBoolean(key_root_access,false);
        setFormat();
        set_source_file();
        resolver = c.getContentResolver();
    }

    public void setFormat() {
        if (MainActivity.sharedPrefApkManager.contains(name_format_data_saved)) {

            int part1 = MainActivity.sharedPrefApkManager.getInt(name_part_1, 1);
            Log.i(TAG, "Part 1 :" + part1);

            String part2 = MainActivity.sharedPrefApkManager.getString(name_part_2, "_v");
            Log.i(TAG, "Part 2 :" + part2);

            int part3 = MainActivity.sharedPrefApkManager.getInt(name_part_3, 2);
            Log.i(TAG, "Part 3 :" + part3);

            String part4 = MainActivity.sharedPrefApkManager.getString(name_part_4, "_");
            Log.i(TAG, "Part 4 :" + part4);

            int part5 = MainActivity.sharedPrefApkManager.getInt(name_part_5, 3);
            Log.i(TAG, "Part 5 :" + part5);

            String part6 = MainActivity.sharedPrefApkManager.getString(name_part_6, "");
            Log.i(TAG, "Part 6 :" + part6);

            int part7 = MainActivity.sharedPrefApkManager.getInt(name_part_7, 0);
            Log.i(TAG, "Part 7 :" + part7);

            String part8 = MainActivity.sharedPrefApkManager.getString(name_part_8, "");
            Log.i(TAG, "Part 8 :" + part8);

            this.apk_formatted_name = getNameFromItem(apkItem, part1) + part2 + getNameFromItem(apkItem, part3) + part4 + getNameFromItem(apkItem, part5) + part6 + getNameFromItem(apkItem, part7) + part8 + ".apk";
        }
    }

    private String getNameFromItem(ApkListDataItem ld, int i) {
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

    private void set_source_file(){
        source_doc_file = apkItem.file_doc;
        source_uri = apkItem.file_uri;
        Log.i(TAG, " source_doc_file path : " + apkItem.file.getAbsolutePath());
    }

    public void RenameThisApk() {
        parent = apkItem.file_doc.getParentFile();
        Log.i(TAG, " source_doc_file parent : " + parent);
        //dest_doc_file = new DocumentFile.fromFile(new File(parent + "/" + this.apk_formatted_name));

//--------------------------RENAME APK WITHOUT ROOT-----------------------------------
        if(!root_access){
            if(parent.canWrite()){
                Log.i(TAG, " source_doc_file parent : " + parent);
                this.result = (source_doc_file).renameTo(this.apk_formatted_name);
                if(result){
                    Log.i(TAG," : Renamed \"" + source_doc_file.getName() + "\" successfully ");
                }else{
                    Log.i(TAG," : Renaming failed for \"" + source_doc_file.getName());
                }
            }
            else{
                Log.i(TAG," : Renaming failed for \"" + source_doc_file.getName() + "\". Cannot Write to "+parent.getUri().getPath());
            }
//--------------------------RENAME APK VIA ROOT-----------------------------------
        }else if(root_access){
            command = "mv \""+ source_doc_file +"\" \""+ dest_doc_file +"\"";
            try{
                Shell.su(command).exec();
                Log.i(TAG, source_doc_file.getUri().getPath()+" is renamed to  "+this.apk_formatted_name);
            }catch(Exception ex){
                Log.e(TAG," : Renaming failed for : \"" + source_doc_file.getUri().getPath()+"Error Exception : "+ex);
            }
        }

        Log.i(TAG, "Renaming " + apkItem.file_name + " : \"" + dest_doc_file + "\" - " + result);
    }

    public void extractApkOf(){

       // String repo_str = MainActivity.sharedPrefAppSettings.getString(key_repository_folder,path_not_set);
       // Log.i(TAG, "\nRetrieved Repo String : " + repo_str);
        Uri repo_uri = Uri.parse(MainActivity.sharedPrefAppSettings.getString(key_repository_folder,path_not_set));
        Log.i(TAG, "\nRetrieved Repo Uri : " + repo_uri.toString());

        parent = DocumentFile.fromTreeUri(context,repo_uri);
        parent_uri = repo_uri;

        Log.i(TAG, "\nParent value from preference : " + parent);

        if(!parent.exists() || !parent.isDirectory()){
            Log.i(TAG, "\nParent value not exists : " + parent);
            parent = DocumentFile.fromFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
        }

        Log.i(TAG, " Apk will be extracted to: " + parent);
        dest_doc_file = DocumentFile.fromFile(new File(parent + "/" + this.apk_formatted_name));
        dest_uri = Uri.parse(parent_uri+"%2F"+this.apk_formatted_name);

//--------------------------------EXTRACT APK WITHOUT ROOT-----------------------------------
        if(!root_access){
          //  if(dest_doc_file.canWrite()){
                copyTo(source_uri, dest_uri);
             //   Log.i(TAG,"Writing to Destination Allowed.");
          //  }else{
              //  this.result = (source_doc_file).renameTo(dest_doc_file);
             //   Log.i(TAG," : Renaming failed for \"" + source_doc_file.getName() + "\". Cannot Write to "+parent.getUri().getPath());
            //}
        }
//-----------------------------------ROOT EXTRACT APK-----------------------------------
        else if(root_access){
            command = "cp \""+ source_doc_file +"\" \""+ dest_doc_file +"\"";
            try{
                Shell.sh(command).exec();
                Log.i(TAG,"ROOT : Apk Extracted successfully : "+apk_formatted_name);
            }catch(Exception ex){
                Log.i(TAG,"ROOT : Apk Extraction failed : "+apk_formatted_name);
            }
        }
    }


    public boolean copyTo(Uri src,Uri dest){

        boolean result = false;
        OutputStream outStream = null;
        InputStream inputStream = null;
        try{
            outStream = resolver.openOutputStream(dest);
            Log.e(TAG,"Out Stream Opened Succesfully : ");
        }catch(Exception ex){
            Log.e(TAG,"Error opening out stream : "+ex);
        }

        try{
            inputStream = resolver.openInputStream(src);
            Log.e(TAG,"Input Stream Opened Succesfully : ");
        }catch(Exception ex){
            Log.e(TAG,"Error opening input stream : "+ex);
        }


        try{
            if(outStream!=null & inputStream!=null)
            {
                while(inputStream.read()!=-1){
                    outStream.write(inputStream.read());
                }
                outStream.flush();;
                outStream.close();;
                inputStream.close();
                Log.e(TAG,"Finished writing file succesfully: ");
            }
        }catch(Exception ex){
            Log.e(TAG,"Error writing file : "+ex);
        }

        return result;
    }

}

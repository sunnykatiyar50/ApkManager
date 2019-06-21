package com.sunnykatiyar.ApkManager;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class ListDataItem {

//    Uri file_uri;
//    String file_path;
//    Context context;
    File file;
    ApplicationInfo app_info;
    PackageInfo apk_pkg_info;
    PackageInfo app_pkg_info;
  //  PackageInfo pkg_info;
    String app_name ;
    String file_name ;
    String file_size ;
    String app_version_name;
    String app_version_code;
    String apk_version_name;
    String apk_version_code;
  //  Drawable icon;
    boolean isInstalled ;
    boolean isUpdatable ;
    boolean select_box_state ;

    String pkg_name ;
    private final String TAG = "ListDataItem :";
    String last_install_time;
    String last_modified_time;
    long initial_modified_time;
    long file_creation_time;
    PackageManager pm = MainActivity.pm;

    private static final DecimalFormat format = new DecimalFormat("#.##");
    private static final long MB = 1024 * 1024;
    private static final long KB = 1024;
    private static final long GB = 1024 * 1024 * 1024;

    public ListDataItem(File f1, Context context){

        super();

        this.file = f1;

        try{
            BasicFileAttributes attr = Files.readAttributes(f1.toPath(), BasicFileAttributes.class);
            this.file_creation_time = attr.creationTime().toMillis();
            Log.i(TAG,"File Creation Time of \""+file_name+"\" is "+getTime(this.file_creation_time));
        }catch(Exception ex){
            Log.i(TAG,"Error getting File Creation Time of - "+file_name);
        }

        try
        {
            this.apk_pkg_info = pm.getPackageArchiveInfo(file.getAbsolutePath(),0);
            this.pkg_name = apk_pkg_info.packageName;
            this.initial_modified_time = this.file.lastModified();
            this.apk_version_name = apk_pkg_info.versionName;
            this.apk_version_code = String.valueOf(apk_pkg_info.versionCode);
            this.app_info = apk_pkg_info.applicationInfo ;
            this.last_modified_time = getTime(file.lastModified());
//-------------------------------------------------------------------------
            this.app_info.sourceDir       = file.getAbsolutePath();
            this.app_info.publicSourceDir = file.getAbsolutePath();
///---------------------------------------------------------------------
            this.app_name = (String) this.app_info.loadLabel(pm);
            this.file_name = file.getName();
            this.file_size = getSize(file);
            this.isInstalled = false;
            this.isUpdatable = false;
            this.select_box_state=false;
        }catch(Exception ex)
        {
              Log.e(TAG,"Invalid apk file found :"+file.getName()+"  -  "+ex);
        }

        try{
            app_pkg_info = pm.getPackageInfo(pkg_name,0);
            this.last_install_time = getTime(app_pkg_info.lastUpdateTime);
            app_version_code = String.valueOf(app_pkg_info.versionCode);
            app_version_name = app_pkg_info.versionName;

            if(Integer.parseInt(apk_version_code) > Integer.parseInt(app_version_code)){
                this.isInstalled =  true;
                this.isUpdatable = true;
            }
            else if(Integer.parseInt(apk_version_code) <= Integer.parseInt(app_version_code))
            {
                this.isInstalled =  true;
                this.isUpdatable = false;
            }

//            if(isInstalled){
//                this.app_name = (String) app_pkg_info.applicationInfo.loadLabel(pm);
//                //this.icon = apk_pkg_info.applicationInfo.loadLogo(pm);
//            }else if(!isInstalled){
//                this.app_name=file_name;
//            }

        }catch(Exception ex){
            Log.e(TAG," : "+ex);
            app_version_code = "";
            app_version_name = "Not Available ";
        }
    }

    public String getSize(File file) {

        if (!file.isFile()) {
            throw new IllegalArgumentException("Expected a file");
        }
        final double length = file.length();

        if(length>GB){
            return format.format(length / GB) + " MB";
        }
        if (length > MB) {
            return format.format(length / MB) + " MB";
        }
        if (length > KB) {
            return format.format(length / KB) + " KB";
        }

        return format.format(length) + "Bytes";
    }

    public String getTime(long time){
        DateFormat dateFormat = new SimpleDateFormat("hh:mm a dd-MM-yy");
        String strDate = dateFormat.format(time);
        return strDate;
    }

}

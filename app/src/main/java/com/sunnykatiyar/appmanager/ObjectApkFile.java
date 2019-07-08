package com.sunnykatiyar.appmanager;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class ObjectApkFile {

//    String file_path;
//    Context context;

    private final String TAG = "ObjectApkFile :";

    File file;
  //  DocumentFile file_doc;
  //  Uri file_uri;
    ApplicationInfo app_info;
    PackageInfo apk_pkg_info;
    PackageInfo app_pkg_info;
  //PackageInfo pkg_info;
    String app_name ;
    String file_name ;
    String file_size ;
    String app_version_name;
    String app_version_code;
    String apk_version_name;
    String apk_version_code;
    String pkg_name ;
    String str_file_creation_time;
    String str_app_update_time;
    Uri parent;

    boolean isInstalled ;
    boolean isUpdatable ;
    boolean select_box_state ;

    long app_install_time;
    long app_update_time;
    long file_creation_time;
    long file_modified_time;

    PackageManager pm = FragmentApkFiles.pm;

    private static final DecimalFormat format = new DecimalFormat("#.##");
    private static final long MB = 1024 * 1024;
    private static final long KB = 1024;
    private static final long GB = 1024 * 1024 * 1024;

    public ObjectApkFile(File f1, Context context){

        super();
        this.file = f1;
      //this.file_doc = DocumentFile.fromFile(file);
        this.file_name = file.getName();
       //this.file_uri = this.file.toURI();
       //this.parent = this.file_doc.getParentFile().getUri();

//        Log.i(TAG,"ISDIRECTORY()"+file_doc.isDirectory());
//        Log.i(TAG,"URI : "+file_uri.toString());
//        Log.i(TAG,"GETSCHEME(): "+file_uri.getScheme());
//        Log.i(TAG,"GETSCHEMESPECIFICPART: "+file_uri.getSchemeSpecificPart());
//        Log.i(TAG,"ENCODEDSCHEMESPECIFICPART: "+file_uri.getEncodedSchemeSpecificPart());
//        Log.i(TAG,"GETPATH(): "+file_uri.getPath());
//        Log.i(TAG,"GETENCODEDPATH(): "+file_uri.getEncodedPath());
//        Log.i(TAG,"GETLASTPATHSEGMENT(): "+file_uri.getLastPathSegment());
//        Log.i(TAG,"GETQUERY(): "+file_uri.getQuery());
//        Log.i(TAG,"GETENCODEDQUERY(): "+file_uri.getEncodedQuery());
//        Log.i(TAG,"GETHOST(): "+file_uri.getHost());
//        int i=1;
//        for(String str:file_uri.getPathSegments()){
//            Log.i(TAG,"GETPATHSEGMENTS(): "+(i++)+" : "+str);
//        }
//
//        Log.i(TAG,"GETUSERINFO: "+file_uri.getUserInfo());
//        Log.i(TAG,"GETENCODEDUSERINFO(): "+file_uri.getEncodedUserInfo());
//        Log.i(TAG,"GETAUTHORITY: "+file_uri.getAuthority());
//        Log.i(TAG,"GETFRAGMENT(): "+file_uri.getFragment());
//        Log.i(TAG,"ISABSOLUTE(): "+file_uri.isAbsolute());

        try{
            BasicFileAttributes attr = Files.readAttributes(f1.toPath(), BasicFileAttributes.class);
            this.file_creation_time = attr.creationTime().toMillis();
            this.file_modified_time = attr.lastModifiedTime().toMillis();
          //Log.i(TAG,"File Creation Time of \""+file_name+"\" is "+file_creation_time);
        }catch(Exception ex){
            Log.i(TAG,"Error getting File Creation Time of - "+file_name);
        }

        try
        {
            this.apk_pkg_info = pm.getPackageArchiveInfo(file.getAbsolutePath(),0);
            this.pkg_name = apk_pkg_info.packageName;
            this.apk_version_name = apk_pkg_info.versionName;
            this.apk_version_code = String.valueOf(apk_pkg_info.versionCode);
            this.app_info = apk_pkg_info.applicationInfo ;
//----------------------ASSIGNING APK DIRECTORY---------------------------------------------------
            this.app_info.sourceDir       = file.getAbsolutePath();
            this.app_info.publicSourceDir = file.getAbsolutePath();
///---------------------------------------------------------------------
            this.app_name = (String) this.app_info.loadLabel(pm);
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
            this.app_install_time = app_pkg_info.firstInstallTime;
            this.app_update_time = app_pkg_info.lastUpdateTime;
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

        }catch(Exception ex){
            Log.e(TAG," : "+ex);
            app_version_code = "";
            app_version_name = "Not Available ";
        }

        this.str_file_creation_time = getTime(this.file_creation_time);
        this.str_app_update_time = getTime(this.app_update_time);

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

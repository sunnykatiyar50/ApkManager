package com.sunnykatiyar.appmanager;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.io.File;

public class ObjectAppPackageName {

        private final String TAG = "ObjectApkFile :";

        String pkg_name;
        ApplicationInfo app_info;
        PackageInfo app_pkg_info;
        String app_name ;
        String app_version_name;
        String app_version_code;
        String str_app_update_time;
        boolean select_box_state ;
        String apk_size;
        File apk_file;
        long app_install_time;
        long app_update_time;
        Context context;
        PackageManager pm;

        private static final DecimalFormat format = new DecimalFormat("#.##");
        private static final long MB = 1024 * 1024;
        private static final long KB = 1024;
        private static final long GB = 1024 * 1024 * 1024;

        public ObjectAppPackageName(String pkg_name, Context context){

            super();
            this.context=context;
            pm = context.getPackageManager();
            this.pkg_name = pkg_name;

            try{
                this.app_pkg_info = pm.getPackageInfo(this.pkg_name,0);
                this.app_info = this.app_pkg_info.applicationInfo;
                this.app_name = pm.getApplicationLabel(this.app_info).toString();
                this.apk_file = new File(this.app_info.sourceDir);
                this.app_install_time = this.app_pkg_info.firstInstallTime;
                this.app_update_time = this.app_pkg_info.lastUpdateTime;
                this.app_version_code = String.valueOf(app_pkg_info.versionCode);
                this.app_version_name = this.app_pkg_info.versionName;
                this.apk_size = getSize(apk_file.length());
                this.str_app_update_time = getTime(this.app_update_time);
            }catch(Exception ex){
                Log.e(TAG," Exception Reading PkgInfo for : "+pkg_name+" Exception : "+ex);
            }
        }

        public String getSize(long length) {

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


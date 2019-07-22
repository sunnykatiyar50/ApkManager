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

class ObjectAppPackageName {

    final String pkg_name;
    String app_name ;
    String app_version_name;
    String app_version_code;
    boolean select_box_state ;
    String apk_size;
    String insaller_pkg;
    File apk_file;
    PackageInfo app_pkg_info;
    ApplicationInfo app_info;

    private static final DecimalFormat format = new DecimalFormat("#.##");
        private static final long MB = 1024 * 1024;
        private static final long KB = 1024;
        private static final long GB = 1024 * 1024 * 1024;

        public ObjectAppPackageName(String pkg_name, Context context){

            super();
            Context context1 = context;
            PackageManager pm = context.getPackageManager();
            this.pkg_name = pkg_name;

            try{
                this.app_pkg_info = pm.getPackageInfo(this.pkg_name, 0);
                this.insaller_pkg = pm.getInstallerPackageName(this.pkg_name);
                this.app_info = app_pkg_info.applicationInfo;
                this.app_name = pm.getApplicationLabel(app_info).toString();
                this.apk_file = new File(app_info.sourceDir);
                long app_install_time = app_pkg_info.firstInstallTime;
                long app_update_time = app_pkg_info.lastUpdateTime;
                this.app_version_code = String.valueOf(app_pkg_info.versionCode);
                this.app_version_name = app_pkg_info.versionName;
                this.apk_size = getSize(apk_file.length());
                String str_app_update_time = getTime(app_update_time);
            }catch(Exception ex){
                String TAG = "ObjectApkFile :";
                Log.e(TAG," Exception Reading PkgInfo for : "+pkg_name+" Exception : "+ex);
            }
        }

        private String getSize(long length) {

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

        private String getTime(long time){
            DateFormat dateFormat = new SimpleDateFormat("hh:mm a dd-MM-yy");
            String strDate = dateFormat.format(time);
            return strDate;
        }

    }


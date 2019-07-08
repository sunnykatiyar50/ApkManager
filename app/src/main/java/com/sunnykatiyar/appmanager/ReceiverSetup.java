package com.sunnykatiyar.appmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import static com.sunnykatiyar.appmanager.ActivityMain.serviceIntent;

public class ReceiverSetup extends BroadcastReceiver {

    private final String TAG= "RECEIVER_PACKAGE_ADDED/REMOVED : ";

    ObjectAppPackage appItem;
    PackageManager pm;
    PackageInfo pkg_info;
    ApplicationInfo app_info;
    ClassApkOperation apkOperationObject;
    public ReceiverSetup( ) {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ShowLog(context,"Package Installed INTENT RECEIVED");
        String pkg_name = intent.getDataString();
        String pkg_name_from_uid = context.getPackageManager().getNameForUid(intent.getIntExtra(Intent.EXTRA_UID,0));

        pm = context.getPackageManager();

        switch(intent.getAction()){

            case Intent.ACTION_PACKAGE_ADDED :{
                Log.i(TAG,"Intent.ACTION_PACKAGE_ADDED");
                Log.i(TAG,"Package Added Name : "+pkg_name);
                Log.i(TAG,"Package Added Name from uid : "+pkg_name_from_uid);
                try {
                    app_info = pm.getPackageInfo(pkg_name,0).applicationInfo;
                    appItem = new ObjectAppPackage(pkg_name,context);
                    apkOperationObject = new ClassApkOperation(appItem,context);
                    apkOperationObject.extractApk();
                    ShowLog(context,"Package Extracted to - "+ apkOperationObject.parent_folder.getAbsolutePath());
                }catch(Exception ex) {
                    ShowLog(context,"Error Extracting. Exception :  "+ex);
                }
                break;
            }

            case Intent.ACTION_PACKAGE_REMOVED :{
                Log.i(TAG,"Intent.ACTION_PACKAGE_REMOVED");
                ShowLog(context,"Package Removed name: "+pkg_name);
                ShowLog(context,"Package Removed: "+pkg_name_from_uid);
                break;
            }

            case Intent.ACTION_BOOT_COMPLETED :{
                Log.i(TAG,"Intent.ACTION_BOOT_COMPLETED");
                if(serviceIntent ==null){
                    serviceIntent = new Intent(context, ServiceSetup.class);
                    context.startForegroundService(serviceIntent);
                }
                break;
            }

            case Intent.ACTION_REBOOT :{
                Log.i(TAG,"Intent.ACTION_REBOOT");
                if(serviceIntent ==null){
                    serviceIntent = new Intent(context, ServiceSetup.class);
                    context.startForegroundService(serviceIntent);
                }
                break;
            }
        }
    }
    
    public void ShowLog(Context context,String str){
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
        Log.i(TAG,str);
    }
    
}

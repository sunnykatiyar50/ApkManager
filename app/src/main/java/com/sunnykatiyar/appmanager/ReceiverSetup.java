package com.sunnykatiyar.appmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.util.List;

import static com.sunnykatiyar.appmanager.ActivityMain.serviceIntent;

public class ReceiverSetup extends BroadcastReceiver {

    private final String TAG= "RECEIVER_PACKAGE_ADDED/REMOVED : ";

    public ReceiverSetup( ) {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        showLog(context,"Package Installed INTENT RECEIVED");
        String pkg_name = intent.getData().getSchemeSpecificPart();
        String pkg_name_from_uid = context.getPackageManager().getNameForUid(intent.getIntExtra(Intent.EXTRA_UID,0));


        switch(intent.getAction()){

            case Intent.ACTION_PACKAGE_ADDED :{
                Log.i(TAG,"Intent.ACTION_PACKAGE_ADDED");
                Log.i(TAG,"Package Name Direct : "+pkg_name);
                Log.i(TAG,"Package Added Name from uid : "+pkg_name_from_uid);
                PackageManager pm = context.getPackageManager();

                try {
                    ObjectAppPackageName appItem = new ObjectAppPackageName(pkg_name, context);
                    ClassApkOperation apkOperationObject = new ClassApkOperation(appItem, context);
                        Log.i(TAG,"Extracting Apk of size :"+ appItem.apk_size);
                        apkOperationObject.extractApk();
                      //  showLog(context,"Package Extracted to - "+ apkOperationObject);
                }catch(Exception ex) {
                    showLog(context,"Error Extracting. Exception :  "+ex);
                }
                break;

            }

            case Intent.ACTION_PACKAGE_REMOVED :{
                Log.i(TAG,"Intent.ACTION_PACKAGE_REMOVED");
                showLog(context,"Package Removed name: "+pkg_name);
//              showLog(context,"Package Name from uid Removed: "+pkg_name_from_uid);
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
    
    private void showLog(Context context, String str){
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
        Log.i(TAG,str);
    }
    
}

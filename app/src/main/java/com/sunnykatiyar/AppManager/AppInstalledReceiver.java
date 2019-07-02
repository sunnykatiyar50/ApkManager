package com.sunnykatiyar.AppManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import static android.content.Intent.EXTRA_UID;

public class AppInstalledReceiver extends BroadcastReceiver {

    private final String TAG= "APPINSTALLED_RECEIVER";

    public AppInstalledReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ShowLog(context,"Package Installed INTENT RECEIVED");

        String pkg_name="";
        switch(intent.getAction()){

            case Intent.ACTION_PACKAGE_ADDED :{
                Log.i(TAG,context.getPackageManager().getNameForUid(intent.getIntExtra(Intent.EXTRA_UID,0)));
                break;
            }

            case Intent.ACTION_PACKAGE_REMOVED :{
                Log.i(TAG,context.getPackageManager().getNameForUid(intent.getIntExtra(Intent.EXTRA_UID,0)));
                break;
            }
        }
    }

    @Override
    public IBinder peekService(Context myContext, Intent service) {
        return super.peekService(myContext, service);
    }
    
    public void ShowLog(Context context,String str){
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
        Log.i(TAG,str);
    }
    
}

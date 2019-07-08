package com.sunnykatiyar.appmanager;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;

public class ServiceSetup extends Service {
    ReceiverSetup myReceiver;
    IntentFilter intentFilter;
    Context context;
    NotificationSetup myNotification;
    final int NOTIFICATION_ID = 23;
    private final String TAG = "RECEIVER LISTENER SERVICE";
    String service_noti_content = "To Hide This : Long Press Here > Click AppInfo icon on Top-Right > Select Notifications > Uncheck Foreground Service";
    String service_noti_title = "Foreground Service Notification";

    public ServiceSetup() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        context = getApplicationContext();
        myReceiver = new ReceiverSetup();
        intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addDataScheme("package");
        getApplicationContext().registerReceiver(myReceiver,intentFilter);
        Log.i(TAG," Listener Service Registered");

        myNotification = new NotificationSetup(context, service_noti_content, service_noti_title);
        myNotification.prepareServiceNotification();

        startForeground(NOTIFICATION_ID,myNotification.service_notification);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        myNotification.notiManager.cancel(NOTIFICATION_ID);
        ActivityMain.serviceIntent=null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}

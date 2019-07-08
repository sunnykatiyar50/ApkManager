package com.sunnykatiyar.appmanager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class NotificationSetup extends Notification {

    NotificationChannel notiChannel;
    NotificationManager notiManager;
    NotificationCompat.Builder notiBuilder;
    Context context;
    final String TAG = " MY_NOTIFICATION : " ;

    final String SERVICE_NOTI_CHANNEL_ID = "FOREGROUND_SERVICE_CHANNEL";
    final String NORMAL_NOTI_CHANNEL_ID = "NORMAL_NOTIFICATION_CHANNEL";
    final CharSequence SERVICE_NOTI_CHANNEL_NAME = "Foreground Service Notification Channel";
    final CharSequence NORMAL_NOTI_CHANNEL_NAME = "Normal Notification Channel";
    final int CHANNEL_IMPORTANCE = NotificationManager.IMPORTANCE_DEFAULT;

    Notification service_notification;
    Notification normal_notification;
    String content = "You can disable from app_info";
    String title = "Listening for Package Addition/removal";
    String service_noti_description = "Disable this channel if you dont want it";
    String normal_noti_description = "All important notifications from app will be shown in this channel";


    public NotificationSetup(Context context, String content, String title) {
        super();
        this.context = context;
        this.title = title;
        this.content = content;
        notiManager = context.getSystemService(NotificationManager.class);
    }

    private void createServiceNotiChannel(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            Log.i(TAG,"IN CREATE PERSISTANT CHANNEL : ");
            notiChannel = new NotificationChannel(SERVICE_NOTI_CHANNEL_ID,SERVICE_NOTI_CHANNEL_NAME,CHANNEL_IMPORTANCE);
            notiChannel.setDescription(service_noti_description);
            notiChannel.setLightColor(Color.BLUE);
            notiChannel.setSound(null,null);
            notiChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            notiManager.createNotificationChannel(notiChannel);
        }
    }

    private void createNormalNotiChannel(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            Log.i(TAG,"IN CREATE CHANNEL : ");
            notiChannel = new NotificationChannel(NORMAL_NOTI_CHANNEL_ID,NORMAL_NOTI_CHANNEL_NAME,CHANNEL_IMPORTANCE);
            notiChannel.setDescription(normal_noti_description);
            notiChannel.setLightColor(Color.GREEN);
            notiChannel.setSound(null,null);
            notiChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            notiManager.createNotificationChannel(notiChannel);
        }
    }

    public void prepareServiceNotification(){

        createServiceNotiChannel();
        Intent noti_click_intent = new Intent(context, ActivityMain.class);
        noti_click_intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        PendingIntent pdi = PendingIntent.getActivity(context,0,noti_click_intent,0);

        notiBuilder = new NotificationCompat.Builder(this.context,SERVICE_NOTI_CHANNEL_ID);

        service_notification = notiBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_android)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentTitle(this.title)
                .setContentText(this.content)
                .setContentIntent(pdi)
                .setStyle(new NotificationCompat.BigTextStyle())
                .setAutoCancel(false)
                .build();

    }

    public void prepareNormalNotification(){

        createNormalNotiChannel();
        Intent noti_click_intent = new Intent(context, ActivityMain.class);
        noti_click_intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        PendingIntent pdi = PendingIntent.getActivity(context,0,noti_click_intent,0);

        notiBuilder = new NotificationCompat.Builder(this.context,NORMAL_NOTI_CHANNEL_ID);

        normal_notification = notiBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_android)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentTitle(this.title)
                .setContentText(this.content)
                .setContentIntent(pdi)
                .setAutoCancel(true)
                .build();
    }


}

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

    private NotificationChannel notiChannel;
    final NotificationManager notiManager;
    private NotificationCompat.Builder notiBuilder;
    private final Context context;
    final String TAG = " MY_NOTIFICATION : " ;

    private final String SERVICE_NOTI_CHANNEL_ID = "FOREGROUND_SERVICE_CHANNEL";
    private final String NORMAL_NOTI_CHANNEL_ID = "NORMAL_NOTIFICATION_CHANNEL";
    private final String TASKS_NOTI_CHANNEL_ID = "TASKS_NOTIFICATIONS_CHANNEL";
    private final CharSequence SERVICE_NOTI_CHANNEL_NAME = "Foreground Service Notification Channel";
    private final CharSequence NORMAL_NOTI_CHANNEL_NAME = "Normal Notification Channel";
    private final CharSequence TASKS_NOTI_CHANNEL_NAME = "Tasks Notification Channel";

    Notification service_notification;
    Notification task_notification;


    public NotificationSetup(Context context) {
        super();
        this.context = context;
        notiManager = context.getSystemService(NotificationManager.class);
    }

    private void createServiceNotiChannel(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
          //  Log.i(TAG,"IN CREATE PERSISTANT CHANNEL : ");
            int CHANNEL_IMPORTANCE = NotificationManager.IMPORTANCE_DEFAULT;
            notiChannel = new NotificationChannel(SERVICE_NOTI_CHANNEL_ID,SERVICE_NOTI_CHANNEL_NAME, CHANNEL_IMPORTANCE);
            //  String content = "You can disable from app_info";
            //  private String title = "Listening for Package Addition/removal";
            String service_noti_description = "Disable this channel if you dont want it";
            notiChannel.setDescription(service_noti_description);
            notiChannel.setLightColor(Color.BLUE);
            notiChannel.setSound(null,null);
            notiChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            notiManager.createNotificationChannel(notiChannel);
        }
    }

    public void prepareServiceNotification(String content, String title){

        createServiceNotiChannel();
        Intent noti_click_intent = new Intent(context, ActivityMain.class);
        noti_click_intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        PendingIntent pdi = PendingIntent.getActivity(context,0,noti_click_intent,0);

        notiBuilder = new NotificationCompat.Builder(this.context,SERVICE_NOTI_CHANNEL_ID);

        service_notification = notiBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_android)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pdi)
                .setStyle(new NotificationCompat.BigTextStyle())
                .setAutoCancel(false)
                .build();

    }

    private void createNormalNotiChannel(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
           // Log.i(TAG,"IN CREATE CHANNEL : ");
            notiChannel = new NotificationChannel(NORMAL_NOTI_CHANNEL_ID,NORMAL_NOTI_CHANNEL_NAME,NotificationManager.IMPORTANCE_MIN);
            String normal_noti_description = "All important notifications from app will be shown in this channel";
            notiChannel.setDescription(normal_noti_description);
            notiChannel.setLightColor(Color.GREEN);
            notiChannel.setSound(null,null);
            notiChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            notiManager.createNotificationChannel(notiChannel);
        }
    }

    public void prepareNormalNotification(String content, String title){

        createNormalNotiChannel();
        Intent noti_click_intent = new Intent(context, ActivityMain.class);
        noti_click_intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        PendingIntent pdi = PendingIntent.getActivity(context,0,noti_click_intent,0);

        notiBuilder = new NotificationCompat.Builder(this.context,NORMAL_NOTI_CHANNEL_ID);

        Notification normal_notification = notiBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_android)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pdi)
                .setAutoCancel(true)
                .build();
    }
    
    private void createTaskNotiChannel(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
          //  Log.i(TAG,"IN CREATE CHANNEL : ");
            notiChannel = new NotificationChannel(TASKS_NOTI_CHANNEL_ID,TASKS_NOTI_CHANNEL_NAME,NotificationManager.IMPORTANCE_MIN);
            String tasks_noti_description = "All Tasks notifications from app will be shown in this channel";
            notiChannel.setDescription(tasks_noti_description);
            notiChannel.setLightColor(Color.GREEN);
            notiChannel.setSound(null,null);
            notiChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            notiManager.createNotificationChannel(notiChannel);
        }
    }

    public void prepareTaskNotification(String content, String title, int progress){

        createTaskNotiChannel();
        Intent noti_click_intent = new Intent(context, ActivityOperations.class);
        noti_click_intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        PendingIntent pdi = PendingIntent.getActivity(context,0,noti_click_intent,0);

        notiBuilder = new NotificationCompat.Builder(this.context,TASKS_NOTI_CHANNEL_ID);
        task_notification = notiBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_android)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(pdi)
                .setProgress(100, progress, false)
                .setAutoCancel(false)
                .setStyle(new NotificationCompat.BigTextStyle())
                .addAction(R.drawable.ic_cancel_white_24dp,"Cancel",pdi)
                .build();
    }


}

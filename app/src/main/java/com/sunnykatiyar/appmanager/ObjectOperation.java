package com.sunnykatiyar.appmanager;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

import androidx.core.app.NotificationCompat;

public class ObjectOperation {

    Context context;
    public String operationTitle;
    public String operationDetails;
    public int operationID;
    public String operationStatus;
    public int totalFiles;
    public int currentFileNum;
    public NotificationSetup notificationSetup;
    public NotificationManager notimgr;
    public Notification myNotification;
    
    public ObjectOperation(Context c, int id, String type, int totalFiles) {
        this.context = c;
        this.operationID = id;
        this.operationTitle = type;
        this.totalFiles = totalFiles;
        this.notimgr = (NotificationManager) c.getSystemService(context.NOTIFICATION_SERVICE);
        notificationSetup = new NotificationSetup(context);
        myNotification = notificationSetup.task_notification;
    }

    public void showNotification(String desc, int curr_num){
        this.operationDetails = desc;
        this.currentFileNum = curr_num;

        if(this.currentFileNum < totalFiles){
            operationStatus = "Status: Running";
        }else if(this.currentFileNum == totalFiles){
            operationStatus = "Status: Completed";
        }else{
            operationStatus = "Status: Unknown";
        }
        
        notificationSetup.prepareTaskNotification(operationDetails, operationTitle);
        myNotification = notificationSetup.task_notification;
        notimgr.notify(operationID, myNotification);
    }

//    public void showNotificationCancelable(){
//        myNotification = notificationSetup.notiBuilder.setOngoing(true)
//                .setSmallIcon(R.drawable.ic_android)
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                .setContentTitle(operationTitle)
//                .setContentText("Operation Status")
//                .setAutoCancel(false)
//                .setStyle(new NotificationCompat.BigTextStyle())
//                .build();
//        notimgr.notify(operationID, myNotification);
//    }

}

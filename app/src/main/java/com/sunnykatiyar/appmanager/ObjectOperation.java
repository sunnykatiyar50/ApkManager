package com.sunnykatiyar.appmanager;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

class ObjectOperation {

    public final String operationTitle;
    public String operationDetails;
    private final int operationID;
    public String operationStatus;
    public int totalFiles=100;
    public int currentFileNum=0;
    public int progress = 0;
    private final NotificationSetup notificationSetup;
    private final NotificationManager notimgr;
    private Notification myNotification;
    
    public ObjectOperation(Context c, int id, String noti_title, int totalFiles) {
        Context context = c;
        this.operationID = id;
        this.operationTitle = noti_title;
        this.totalFiles = totalFiles;
        this.notimgr = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationSetup = new NotificationSetup(context);
        myNotification = notificationSetup.task_notification;
    }

    public void showNotification(String desc, int curr_num, int progress){
        this.operationDetails = desc;
        this.currentFileNum = curr_num;
        this.progress = progress;

        if(this.currentFileNum < totalFiles){
            operationStatus = "Status: Running";
        }else if(this.currentFileNum == totalFiles){
            operationStatus = "Status: Completed";
        }else{
            operationStatus = "Status: Unknown";
        }
        
        notificationSetup.prepareTaskNotification(operationDetails, operationTitle, this.progress);
        myNotification = notificationSetup.task_notification;
        notimgr.notify(operationID, myNotification);
    }

    public void cancelNotification(){
        notificationSetup.notiManager.cancel(operationID);
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

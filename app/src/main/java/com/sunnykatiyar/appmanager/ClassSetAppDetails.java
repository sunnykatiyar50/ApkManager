package com.sunnykatiyar.appmanager;

import android.content.pm.ActivityInfo;
import android.content.pm.ConfigurationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.SharedLibraryInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


class ClassSetAppDetails {

    private final PackageManager appInfo_pm;
    private final PackageInfo pkg;

    public ClassSetAppDetails(PackageManager appInfo_pm, PackageInfo pkg){
        this.appInfo_pm=appInfo_pm;
        this.pkg=pkg;
    }

    public HashMap<String, List<String>> setListData() {

                    HashMap<String, List<String>> expandable_list = new HashMap<>();
                             List<String> permissions_list=new ArrayList<>();
                            List<String> req_permissions_list=new ArrayList<>();
                            List<String>  providers_list=new ArrayList<>();
                            List<String>  receivers_list=new ArrayList<>();
                            List<String>  activities_list=new ArrayList<>();
                            List<String> install_SourceApp= new ArrayList<>();
                            List<String> DataDirectory_Path= new ArrayList<>();
                            List<String> lastUpdate_Time= new ArrayList<>();
                            List<String> version_code=new ArrayList<>() ;
                            List<String> targetSdk = new ArrayList<>();
                            List<String> minSdk = new ArrayList<>();
                            List<String> configs = new ArrayList<>();
                            List<String> category = new ArrayList<>();
                            List<String> uids = new ArrayList<>();
                            List<String> processName = new ArrayList<>();
                            List<String> sharedLibs = new ArrayList<>();
                            List<String> installTime = new ArrayList<>();
                            List<String> installLocation = new ArrayList<>();
                            List<String> requested_features = new ArrayList<>();
                            List<String> services_list = new ArrayList<>();
                            List<String> apkPath = new ArrayList<>();
                            List<String> split_apks_names = new ArrayList<>();
                            List<String> split_apks_path = new ArrayList<>();

        PackageInfo pkg1;


        // --------------------ADD SPLIT APKS LIST-----------------------------------

            // pkg1=appinfo_pm.getPackageInfo(pkg.packageName,PackageManager.PER);
            if(pkg.splitNames!=null) {
                split_apks_names = Arrays.asList(pkg.splitNames);
            }else{
                split_apks_names.add("No Split APKs");
            }

        expandable_list.put("Split Apk Names", split_apks_names);


        // --------------------ADD SERVICES INFO-----------------------------------
        ServiceInfo[] servicesInfos = pkg.services;

        if(servicesInfos!=null){
            for(ServiceInfo ser : servicesInfos){
                if(ser!=null){
                    services_list.add(ser.name);
                }
            }
            if(services_list.size()==0){
                services_list.add("Not Available");
            }
        }else{
            services_list.add("Not Available");
        }
        expandable_list.put("Services", services_list);


        //-----------------------ADD VERSION-----------------------------
        version_code.add(pkg.versionName+"_"+ pkg.versionCode);
        expandable_list.put("Version ",version_code);

//        //-----------------------ADD Category-----------------------------
//        category.add(pkg.applicationInfo.category);
//        expandable_list.put("Category ",category);


        // --------------------ADD PERMISSIONS-----------------------------------
//        PermissionInfo[] permissionInfos = pkg.permissions;
//        try {
//           // pkg1=appinfo_pm.getPackageInfo(pkg.packageName,PackageManager.PER);
//            if(permissionInfos!=null) {
//                for (PermissionInfo per : permissionInfos) {
//                    permissions_list.add(per.name);
//                }
//            }else permissions_list.add("No Permissions");
//        }catch(Exception ex) {
//            providers_list.add("Unable To Access");
//        }
//        expandable_list.put("All Permissions", permissions_list);

        // --------------------ADD INSTALL LOCATION-----------------------------------
        int loc = pkg.installLocation;
        if(loc==0){
            installLocation.add(" INSTALL_LOCATION_AUTO");
        }else if(loc==1){
            installLocation.add("INSTALL_LOCATION_INTERNAL_ONLY");
        }else if(loc==2){
            installLocation.add("INSTALL_LOCATION_PREFER_EXTERNAL");
        }else {
            installLocation.add("<Unknown>");
        }
        expandable_list.put("Install Location", installLocation);


        //---------------------Add PROCESS NAME--------------------------------
        processName.add(pkg.applicationInfo.processName);
        expandable_list.put("Process Name",processName);


        //--------------------FIRST INSTALL TIME------------------------------------
        installTime.add(new SimpleDateFormat().format(pkg.firstInstallTime));
        expandable_list.put("Installed on ",installTime);


        //--------------------ADD LAST_UPDATE_TIME------------------------------------
        lastUpdate_Time.add(new SimpleDateFormat().format(pkg.lastUpdateTime));
        expandable_list.put("Last Updated On",lastUpdate_Time);


        //-----------------------ADD DataDirectory Path------------------------------------------------
        DataDirectory_Path.add(pkg.applicationInfo.dataDir);
        expandable_list.put("Data Directory Path ",DataDirectory_Path);


        //-----------------------ADD APK PATH-----------------------------------------------
        apkPath.add(pkg.applicationInfo.sourceDir);
        expandable_list.put("Base Apk Path ",apkPath);


        //-----------------------USER ID------------------------------------------------
        List<SharedLibraryInfo> shlibs = appInfo_pm.getSharedLibraries(0);
        for(SharedLibraryInfo sh :shlibs){
            sharedLibs.add(sh.getName()+" - "+sh.getType());
        }
        expandable_list.put("Shared Libraries",sharedLibs);


        //-----------------------SPLIT APK Path------------------------------------------------
        if(pkg.applicationInfo.splitSourceDirs != null){
           split_apks_path =Arrays.asList(pkg.applicationInfo.splitSourceDirs);
        }else{
            split_apks_path.add("No Split APK Paths ");
        }
        expandable_list.put("Split APK Paths",split_apks_path );


        //-----------------------Add Target SDK----------------------------------------------
        targetSdk.add(String.valueOf(pkg.applicationInfo.targetSdkVersion));
        expandable_list.put("Target SDK Version",targetSdk);

        //-----------------------Add Minimum SDK----------------------------------------------
        minSdk.add(String.valueOf(pkg.applicationInfo.minSdkVersion));
        expandable_list.put("Minimum SDK Version",minSdk);


        // --------------------ADD INSTALLER SOURCE-----------------------------------
        install_SourceApp.add(appInfo_pm.getInstallerPackageName(pkg.packageName));
        expandable_list.put("Installer Package", install_SourceApp);


        // -------------------REQUESTED PERMISSIONS LIST-------------------------
        try {
            pkg1 = appInfo_pm.getPackageInfo(pkg.packageName,PackageManager.GET_PERMISSIONS);
            String[] req_permissions_array=pkg1.requestedPermissions;
            if(req_permissions_array!=null){
                for (int temp = 0; temp < req_permissions_array.length; temp++) {
                    req_permissions_list.add(req_permissions_array[temp]);
                }
            }else  req_permissions_list.add("No Permission Requested");
        } catch (PackageManager.NameNotFoundException e) {
            req_permissions_list.add("Unable To Access");
        }
        expandable_list.put("Permissions Requested", req_permissions_list);


        //--------------- ---------- GET ACTIVITIES--------------------------------------------
        try {
            pkg1= appInfo_pm.getPackageInfo(pkg.packageName,PackageManager.GET_ACTIVITIES);
            ActivityInfo[] activity_array = pkg1.activities;
            if(activity_array!=null){
                for(ActivityInfo a:activity_array){
                    activities_list.add(a.name);
                }
                }else activities_list.add("No Activities");
        } catch (PackageManager.NameNotFoundException e) {
            activities_list.add("Unable To Access");
        }
        expandable_list.put("Activities", activities_list);

        // ----------------------------------GET FEATURES-----------------------------------------------
        try {
            FeatureInfo[] feat = pkg.reqFeatures;
            if(feat!=null){
                for (int temp = 0; temp<feat.length; temp++) {
                    requested_features.add(feat[temp].name);
                }}
            else requested_features.add("No Featues Requested");
        } catch (Exception e) {
            requested_features.add("Unable To Access");
        }
        expandable_list.put("Features Requested", requested_features);


        // ----------------------------------GET PROVIDERS-----------------------------------------------
        try {
            pkg1=appInfo_pm.getPackageInfo(pkg.packageName,PackageManager.GET_PROVIDERS);
            ProviderInfo[] provider_array=pkg1.providers;
            if(provider_array!=null){
                for (int temp = 0; temp<provider_array.length; temp++) {
                    providers_list.add(provider_array[temp].name);
                }}
            else providers_list.add("No Providers");
        } catch (PackageManager.NameNotFoundException e) {
            providers_list.add("Unable To Access");
        }
        expandable_list.put("Providers", providers_list);


        // -------------------------------GET RECEIVERS --------------------------------------------
        try {
             pkg1=appInfo_pm.getPackageInfo(pkg.packageName,PackageManager.GET_RECEIVERS);
            ActivityInfo[] receiver_array=pkg1.receivers;
             if(receiver_array!=null){
                for (int temp = 0; temp < receiver_array.length; temp++) {
                    receivers_list.add(receiver_array[temp].name);
                }}
             else receivers_list.add("No Receivers");
        } catch (PackageManager.NameNotFoundException e) {
            receivers_list.add("Unable To Access");
        }
        expandable_list.put("Receivers", receivers_list);

        //-----------------------UID----------------------------------------------
        try {
            int uid = appInfo_pm.getPackageUid(pkg.packageName, 0);
            uids.add(String.valueOf(uid));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            uids.add("Not available");
        }
        expandable_list.put("UserID (uid) ", uids);

        //-----------------------CONFIG PREFERENCES----------------------------------------------
//        ConfigurationInfo[] cons =  pkg.configPreferences;
//        if(null!=cons){
//            for(ConfigurationInfo info : cons){
//                configs.add(info.);
//            }
//        }else{
//           configs.add("No ConfigPreference") ;
//        }
//        expandable_list.put("Configuration Info", configs);

        //-----------------------Add Target SDK----------------------------------------------
//        appInfo_pm.get

        return expandable_list;
    }

}

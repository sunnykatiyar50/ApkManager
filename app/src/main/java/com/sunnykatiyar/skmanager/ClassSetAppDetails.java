package com.sunnykatiyar.skmanager;

import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ConfigurationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.SharedLibraryInfo;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


class ClassSetAppDetails {

    private final PackageManager packageManager;
    private final PackageInfo pkg;
    final String TAG = " MYAPP : SETAPPDETAILS : ";
    HashMap<String, List<String>> expandable_list = new HashMap<>();

    List<String> permissions_granted=new ArrayList<>();
    List<String> all_permissions=new ArrayList<>();
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

    private static final int CATEGORY_GAMES = ApplicationInfo.CATEGORY_GAME;
    private static final int CATEGORY_AUDIO = ApplicationInfo.CATEGORY_AUDIO;
    private static final int CATEGORY_VIDEO = ApplicationInfo.CATEGORY_VIDEO;
    private static final int CATEGORY_MAPS = ApplicationInfo.CATEGORY_MAPS;
    private static final int CATEGORY_IMAGE = ApplicationInfo.CATEGORY_IMAGE;
    private static final int CATEGORY_SOCIAL = ApplicationInfo.CATEGORY_SOCIAL;
    private static final int CATEGORY_PRODUCTIVITY = ApplicationInfo.CATEGORY_PRODUCTIVITY;
    private static final int CATEGORY_NEWS = ApplicationInfo.CATEGORY_NEWS;
    private static final int CATEGORY_UNDEFINED = ApplicationInfo.CATEGORY_UNDEFINED;


    public ClassSetAppDetails(PackageManager pm, PackageInfo pkg){
        this.packageManager = pm;
        this.pkg = pkg ;
        expandable_list = setListData();
    }

    public HashMap<String, List<String>> setListData() {

        // --------------------ADD SPLIT APKS LIST-----------------------------------
         if(pkg.splitNames!=null) {
                split_apks_names = Arrays.asList(pkg.splitNames);
            }else{
              //  split_apks_names.add("No Split APKs");
         }
         expandable_list.put("Split Apk Names", split_apks_names);


        //-----------------------ADD VERSION-----------------------------
        version_code.add(pkg.versionName+"_"+ pkg.versionCode);
        expandable_list.put("Version ",version_code);

        //-----------------------ADD Category-----------------------------
//        if(pkg.applicationInfo.category){
//
//        }
        category.add(getCategory(pkg));
        expandable_list.put("Category ",category);


        // --------------------ADD INSTALL LOCATION-----------------------------------
        installLocation = getInstalledLocation(pkg);
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
        sharedLibs = getSharedLibs(pkg);
        expandable_list.put("Shared Libraries",sharedLibs);


        //-----------------------SPLIT APK Path------------------------------------------------
        if(pkg.applicationInfo.splitSourceDirs != null){
           split_apks_path =Arrays.asList(pkg.applicationInfo.splitSourceDirs);
        }else{
            //split_apks_path.add("No Split APK Paths ");
        }
        expandable_list.put("Split APK Paths",split_apks_path );

        //-----------------------Add Target SDK----------------------------------------------
        targetSdk.add(String.valueOf(pkg.applicationInfo.targetSdkVersion));
        expandable_list.put("Target SDK Version",targetSdk);

        //-----------------------Add Minimum SDK----------------------------------------------
        minSdk.add(String.valueOf(pkg.applicationInfo.minSdkVersion));
        expandable_list.put("Minimum SDK Version",minSdk);

        // --------------------ADD INSTALLER SOURCE-----------------------------------
        String installer = packageManager.getInstallerPackageName(pkg.packageName);
        if(null!=installer){
            if(!installer.isEmpty()){
              install_SourceApp.add(installer);
            }
        }
        expandable_list.put("Installer Package", install_SourceApp);

        //-----------------------UID----------------------------------------------
        try {
            int uid = packageManager.getPackageUid(pkg.packageName, 0);
            uids.add(String.valueOf(uid));
        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
          //  uids.add("Not available");
        }
        expandable_list.put("UserID (uid) ", uids);

        // -------------------PERMISSIONS REQUIRED LIST-------------------------
        all_permissions = getAllPermList(pkg);
        expandable_list.put("Permissions Required", all_permissions);

        permissions_granted = getGrantedPermList(pkg);
        expandable_list.put("Permissions Granted", permissions_granted);

        // --------------------------ADD SERVICES INFO-----------------------------------
        services_list = getServicesList(pkg) ;
        expandable_list.put("Services", services_list);

//      --------------------------GET ACTIVITIES--------------------------------------------
        activities_list = getActivitiesList(pkg);
        expandable_list.put("Activities", activities_list);

        //---------------------------GET FEATURES-----------------------------------------------
        requested_features =  getReqFeatures(pkg);
        expandable_list.put("Features Requested", requested_features);


        // --------------------------GET PROVIDERS-----------------------------------------------
        providers_list = getProvidersList(pkg);
        expandable_list.put("Providers", providers_list);


        //--------------------------GET RECEIVERS --------------------------------------------
        receivers_list = getReceiversList(pkg);
        expandable_list.put("Receivers", receivers_list);

        //-----------------------CONFIG PREFERENCES----------------------------------------------
        configs = getConfigs(pkg);
        expandable_list.put("Configuration Info", configs);

        return expandable_list;
    }


    public List<String> getServicesList(PackageInfo pkg){
        List<String> services_list = new ArrayList<>();
        PackageInfo pkg1;
        try {
            pkg1= packageManager.getPackageInfo(pkg.packageName,PackageManager.GET_SERVICES|PackageManager.MATCH_DISABLED_COMPONENTS);
            ServiceInfo[] servicesInfos = pkg1.services;
            if(servicesInfos!=null){
                for(ServiceInfo ser : servicesInfos){
                    if(ser!=null){
                        services_list.add(ser.name);
                    }
                }

                if(services_list.size()==0){
//                    services_list.add("Not Available");
                }
            }else{
//                services_list.add("Not Available");
            }
        } catch (PackageManager.NameNotFoundException e) {
//            services_list.add("Unable to Access");
        }
        return  services_list;
    }

    public List<String> getReceiversList(PackageInfo pkg){
        List<String> receivers_list = new ArrayList<>();
        PackageInfo pkg1;

        try {
            pkg1= packageManager.getPackageInfo(pkg.packageName,PackageManager.GET_RECEIVERS|PackageManager.MATCH_DISABLED_COMPONENTS);
            ActivityInfo[] receiver_array=pkg1.receivers;
            if(receiver_array!=null){
                for (int temp = 0; temp < receiver_array.length; temp++) {
                    receivers_list.add(receiver_array[temp].name);
                }
            }
//          else receivers_list.add("No Receivers");
        } catch (PackageManager.NameNotFoundException e) {
//            receivers_list.add("Unable To Access");
        }

        return receivers_list;
    }

    public List<String> getActivitiesList(PackageInfo pkg){
        List<String> activities_list = new ArrayList<>();
        PackageInfo pkg1;
        try {
            pkg1= packageManager.getPackageInfo(pkg.packageName,PackageManager.GET_ACTIVITIES|PackageManager.MATCH_DISABLED_COMPONENTS);
            ActivityInfo[] activity_array = pkg1.activities;
            if(activity_array!=null){
                for(ActivityInfo a:activity_array){
                    activities_list.add(a.name);
                }
            }
//            else activities_list.add("No Activities");
        } catch(Exception e) {
//            activities_list.add("Unable To Access");
        }

        return activities_list;
    }

    public List<String> getProvidersList(PackageInfo pkg){
        List<String> providers_list = new ArrayList<>();
        PackageInfo pkg1;
        try {
            pkg1= packageManager.getPackageInfo(pkg.packageName,PackageManager.GET_PROVIDERS|PackageManager.MATCH_DISABLED_COMPONENTS);
            ProviderInfo[] provider_array=pkg1.providers;
            if(provider_array!=null){
                for (int temp = 0; temp<provider_array.length; temp++) {
                    providers_list.add(provider_array[temp].name);
                }}
//            else providers_list.add("No Providers");
        } catch (PackageManager.NameNotFoundException e) {
//            providers_list.add("Unable To Access");
        }
        return providers_list;
    }

    public List<String> getGrantedPermList(PackageInfo pkg){
        List<String> permissions_granted=new ArrayList<>();
        List<String> all_permissions=new ArrayList<>();

        PackageInfo pkg1;
        try {
            pkg1 = packageManager.getPackageInfo(pkg.packageName, PackageManager.GET_PERMISSIONS);
            String[] req_perms_array =  pkg1.requestedPermissions;
            if(req_perms_array!=null){
                for (int i = 0; i < pkg1.requestedPermissions.length; i++) {
                    if((pkg1.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0){
                        permissions_granted.add(pkg1.requestedPermissions[i]);
                    }
                }
            }else{
//                all_permissions.add("No Permission Required");
//                permissions_granted.add("No Permission Granted");
            }
        }catch(Exception e) {
//            all_permissions.add("Unable To Access");
//            permissions_granted.add("Unable To Access");

        }

        return permissions_granted;
    }

    public List<String> getAllPermList(PackageInfo pkg){
        List<String> permissions_all=new ArrayList<>();

        PackageInfo pkg1;
        try {
            pkg1 = packageManager.getPackageInfo(pkg.packageName, PackageManager.GET_PERMISSIONS);
            String[] req_perms_array =  pkg1.requestedPermissions;
            if(req_perms_array!=null){
                permissions_all = Arrays.asList(req_perms_array);
            }else{
//                permissions_all.add("No Permission Required");
//                permissions_granted.add("No Permission Granted");
            }
        }catch(Exception e) {
//            permissions_all.add("Unable To Access");
//            permissions_granted.add("Unable To Access");

        }

        return permissions_all;
    }

    public List<String> getInstalledLocation(PackageInfo pkg){
        List<String> installLocation = new ArrayList<>();
        int loc = pkg.installLocation;
        
        if(loc==0){
            installLocation.add("INSTALL_LOCATION_AUTO");
        }else if(loc==1){
            installLocation.add("INSTALL_LOCATION_INTERNAL_ONLY");
        }else if(loc==2){
            installLocation.add("INSTALL_LOCATION_PREFER_EXTERNAL");
        }else {
          // installLocation.add(String.valueOf(pkg.installLocation));
        }

        return installLocation;
    }

    public List<String> getConfigs(PackageInfo pkg) {
        List<String> configs = new ArrayList<>();
        PackageInfo pkg1;
        try {
            pkg1 = packageManager.getPackageInfo(pkg.packageName, PackageManager.GET_CONFIGURATIONS);
            ConfigurationInfo[] cons =  pkg1.configPreferences;
            if(null!=cons){
                for(ConfigurationInfo info : cons){
                    configs.add(info.toString());
                }
            }else{
//                configs.add("No ConfigPreference") ;
            }
        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//            configs.add("Unable to Access");
        }

        return configs;
    }

    public List<String> getReqFeatures(PackageInfo pkg) {
        List<String> requested_features = new ArrayList<>();
        try {
            FeatureInfo[] feat = pkg.reqFeatures;
            if(feat!=null){
                Log.i(TAG, "Featues list found : "+feat.length);
                for (int temp = 0; temp<feat.length; temp++) {
                    requested_features.add(feat[temp].name);
                }
            }
            else {
//                requested_features.add("No Featues Requested");
                //Log.i(TAG, "Featues Not found : ");
            }
        } catch (Exception e) {
//            requested_features.add("Unable To Access");
        }

        return requested_features;
    }

    public List<String> getSharedLibs(PackageInfo pkg) {

        String[] sharedLibsArr ;
        List<String> sharedLibs = new ArrayList<>();
        try {
            ApplicationInfo appinfo = packageManager.getApplicationInfo(pkg.packageName, PackageManager.GET_SHARED_LIBRARY_FILES);
            sharedLibsArr = appinfo.sharedLibraryFiles;
            if(sharedLibsArr!=null){
                for(String str:sharedLibsArr){
                    sharedLibs.add(str);
                }
            }else{
//                sharedLibs.add("No Features");
            }
        }catch(Exception e) {
//            sharedLibs.add("Not Available");
        }
        return sharedLibs;
    }

    public String getCategory(PackageInfo pkg){

        switch( pkg.applicationInfo.category){

            case CATEGORY_AUDIO: {
                return   "CATEGORY_AUDIO"  ;
            }

            case CATEGORY_GAMES: {
                return   "CATEGORY_GAMES"  ;
            }

            case CATEGORY_VIDEO: {
                return   "CATEGORY_VIDEO"  ;
            }

            case CATEGORY_MAPS: {
                return   "CATEGORY_MAPS"  ;
            }

            case CATEGORY_SOCIAL: {
                return   "CATEGORY_SOCIAL"  ;
            }

            case CATEGORY_IMAGE: {
                return   "CATEGORY_IMAGE"  ;
            }

            case CATEGORY_PRODUCTIVITY: {
                return   "CATEGORY_PRODUCTIVITY" ;
            }

            case CATEGORY_NEWS : {
                return   "CATEGORY_NEWS"  ;
            }

            default : {
                return "CATEGORY UNDEFINED"  ;
            }

        }
    }

}

package com.sunnykatiyar.skmanager;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.provider.Settings;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Intent.createChooser;
import static com.topjohnwu.superuser.internal.InternalUtils.getContext;

public class ActivityAppDetails extends AppCompatActivity {

    private static final String TAG = "MYAPP : APPDETAILS ACTIVITY : ";

    private final Activity activity = ActivityAppDetails.this;
  
    private ExpandableListView expandableListView;
    private List<String> headers;
    private AdapterExpandableList exp_adapter;
    String clickedPackageLabel;
  //String clickedPackageName;
    private PackageManager packageManager ;
    private ActivityManager activityManager;
    private ClipboardManager clipboardManager;
    private PackageInfo clickedPackageInfo;
    Intent received_intent;
    //AppMenu appinfo_optionmenu;
    private boolean rootAccess;
    private static final String key_root_access = FragmentSettings.key_root_access;
    final String path_not_set = "PATH NOT SET";
    private PackageManager pm;
    public ClassSetAppDetails appDetails;
    HashMap<String, List<String>> expListMap;

    static {
        Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR);
        Shell.Config.verboseLogging(BuildConfig.DEBUG);
        Shell.Config.setTimeout(10);
    }

    private static final DecimalFormat format = new DecimalFormat("#.##");
    private static final long MB = 1024 * 1024;
    private static final long KB = 1024;
    private static final long GB = 1024 * 1024 * 1024;
    public List<Integer> expandedPosList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_details);
        Toolbar toolbar = findViewById(R.id.toolbar_app_details);
        setSupportActionBar(toolbar);
        
        received_intent = getIntent();
        packageManager = getPackageManager();
        clipboardManager = (ClipboardManager) this.getSystemService(CLIPBOARD_SERVICE);
        String clickedPackage = received_intent.getStringExtra("PACKAGE_NAME");

        try {
            clickedPackageInfo =  packageManager.getPackageInfo(clickedPackage, 0);
            clickedPackageLabel = (String) clickedPackageInfo.applicationInfo.loadLabel(packageManager);
        } catch (Exception e) {
            try {
                clickedPackageInfo =  packageManager.getPackageInfo(getPackageName(), 0);
                clickedPackageLabel = (String) clickedPackageInfo.applicationInfo.loadLabel(packageManager);
            } catch (PackageManager.NameNotFoundException e1) {
                e1.printStackTrace();
            }
        }

        getSupportActionBar().setTitle(clickedPackageLabel);
        getSupportActionBar().setElevation(5);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ImageView appinfo_icon = findViewById(R.id.app_info_icon);
        TextView appinfo_appname = findViewById(R.id.app_name);
        TextView app_version = findViewById(R.id.version_num);
        TextView appinfo_pkgname = findViewById(R.id.pkg_name);
        TextView install_date = findViewById(R.id.install_date);
        TextView app_type = findViewById(R.id.details_app_type);
        TextView app_size = findViewById(R.id.app_size);
        rootAccess = ActivityMain.sharedPrefSettings.getBoolean(key_root_access,false);
        pm = this.getPackageManager();

        install_date.setText(new SimpleDateFormat().format(clickedPackageInfo.firstInstallTime));
        appinfo_pkgname.setText(clickedPackageInfo.packageName);
        app_version.setText(clickedPackageInfo.versionName);
        
        if(clickedPackageInfo.applicationInfo.sourceDir.startsWith("/system/")){
            app_type.setText("System");
            app_type.setTextColor(getContext().getColor(R.color.red));
        }else{
            app_type.setText("User");
            app_type.setTextColor(getContext().getColor(R.color.light_green));
        }
        appinfo_icon.setImageDrawable(packageManager.getApplicationIcon(clickedPackageInfo.applicationInfo));
        appinfo_appname.setText(packageManager.getApplicationLabel(clickedPackageInfo.applicationInfo));
        app_size.setText(getSize(new File(clickedPackageInfo.applicationInfo.sourceDir)));

        expandableListView = findViewById(R.id.expandable_list);

        appDetails = new ClassSetAppDetails(packageManager, clickedPackageInfo);
        expListMap = appDetails.expandable_list;
        headers = new ArrayList<>(expListMap.keySet());

        Collections.sort(headers, (s, t1) -> s.compareToIgnoreCase(t1));

        exp_adapter = new AdapterExpandableList(this, headers, expListMap,clickedPackageInfo,appDetails);


        expandableListView.setAdapter(exp_adapter);

        expandedPosList.clear();

        for(int i=0;i<headers.size();i++)
        {
            if(expListMap.get(headers.get(i)).size()==1){
               // expandedPosList.add(i);
                expandableListView.expandGroup(i);
            }
        }


        appinfo_pkgname.setTooltipText(appinfo_pkgname.getText());
        app_version.setTooltipText(app_version.getText());


        expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            Intent activityIntent = new Intent();
            Log.e("in Appdetails","click child item");

            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);

            //-------------------------------ACTIVITIES CLICK------------------------------------------------
            if(exp_adapter.getGroup(groupPosition).toString().equals("Activities"))
            {
                String clicked_activity = exp_adapter.getChild(groupPosition,childPosition).toString();
                activityIntent.setComponent(new ComponentName(clickedPackageInfo.packageName,clicked_activity) );
                Toast.makeText(v.getContext(), clicked_activity, Toast.LENGTH_SHORT).show();
                try{
                    startActivity(activityIntent);
                }catch(SecurityException s){
                    showMsg(clickedPackageLabel+" developer has not allowed to launch activity from outside app.");
                }
            }


            //-------------------------------RECEIVER CLICK------------------------------------------------
            if(exp_adapter.getGroup(groupPosition).toString().contains("Receiver"))
            {
                String receiverName = exp_adapter.getChild(groupPosition, childPosition).toString();
                Log.i(TAG,"clicked : "+receiverName);
                toggleComponent(packageManager, clickedPackageInfo.packageName, receiverName);


            }

            //-------------------------------SERVICE CLICK------------------------------------------------
            if(exp_adapter.getGroup(groupPosition).toString().contains("Service"))
            {
                String serviceName = exp_adapter.getChild(groupPosition, childPosition).toString();
                Log.i(TAG,"clicked : "+serviceName);
                toggleComponent(packageManager, clickedPackageInfo.packageName, serviceName);

            }

            //-------------------------------PROVIDER CLICK------------------------------------------------
            if(exp_adapter.getGroup(groupPosition).toString().contains("Provider")){
                String providername = exp_adapter.getChild(groupPosition, childPosition).toString();
                Log.i(TAG,"clicked : "+providername);
                toggleComponent(packageManager, clickedPackageInfo.packageName, providername);
            }

            //-------------------------------PERMISSIONS CLICK------------------------------------------------
            if(exp_adapter.getGroup(groupPosition).toString().contains("Permissions"))
            {
                String clicked_perm= exp_adapter.getChild(groupPosition,childPosition).toString();
                Toast.makeText(v.getContext(),clicked_perm , Toast.LENGTH_SHORT).show();
                try{
                    if(appDetails.permissions_granted.contains(clicked_perm)){
                        Toast.makeText(v.getContext(),"Permission Revoked"+clicked_perm , Toast.LENGTH_SHORT).show();
                        String command =  "pm revoke " +clickedPackageInfo.packageName+" "+clicked_perm;
                        Log.i(TAG,"Command : " +command);
                        Shell.su(command).exec();
                    }
                    else{
                        Toast.makeText(v.getContext(),"Permission Granted "+clicked_perm, Toast.LENGTH_SHORT).show();
                        String command =  "pm grant " +clickedPackageInfo.packageName+" "+clicked_perm;
                        Log.i(TAG,"Command : " +command);
                        Shell.su(command).exec();
                    }
                    setNewAdapter();
                }catch(Exception ex){
                Log.e(TAG, "Error Changing Component State : " + ex);
            }
            }

            Log.i(TAG,"View Updated.");
            
            return true;
        });
        
        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            int LastExpandedGroup = -1;
            @Override
            public void onGroupExpand(int position) {
                if (LastExpandedGroup != -1 && position != LastExpandedGroup && exp_adapter.getChildrenCount(LastExpandedGroup) >= 6) {
                    expandableListView.collapseGroup(LastExpandedGroup);
                }
                LastExpandedGroup = position;
                Toast.makeText(getApplicationContext(), headers.get(position), Toast.LENGTH_SHORT);
                if(!expandedPosList.contains(position)){
                    expandedPosList.add(position);
                }
            }

        });

        expandableListView.setOnGroupCollapseListener(position -> {
            Toast.makeText(getApplication(), headers.get(position), Toast.LENGTH_SHORT) ;
            
            try{
                if(expandedPosList.contains(position)){
                    expandedPosList.remove(position);
                }
            }catch(Exception ex){
                Log.i(TAG," Error removing index "+ex);
            }


        });

    }

    private void setNewAdapter(){

        Log.e(TAG,"before appdetails") ;

        appDetails = new ClassSetAppDetails(packageManager, clickedPackageInfo);
        Log.e(TAG,"before explistmap") ;

        expListMap = appDetails.expandable_list;
        Log.e(TAG,"headers") ;

        headers = new ArrayList<>(expListMap.keySet());
        Log.e(TAG,"sorting") ;

        Collections.sort(headers, (s, t1) -> s.compareToIgnoreCase(t1));
        Log.e(TAG,"before adapter") ;

        exp_adapter = new AdapterExpandableList(this, headers, expListMap, clickedPackageInfo, appDetails);
        Log.e(TAG,"setting adapter") ;

        expandableListView.setAdapter(exp_adapter);
        Log.e(TAG,"before loop ") ;
        int k;

        List<Integer> list = new ArrayList<>(expandedPosList);

        for(int i=0; i < list.size(); i++){
            Log.e(TAG,"expanding header : "+list.get(i)) ;
            expandableListView.expandGroup(list.get(i));
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_app_details,menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu) ;
        int app_state = packageManager.getApplicationEnabledSetting(clickedPackageInfo.packageName);
        Log.i(TAG,"APP STATE (ActivityAppDetails Menu) : "+app_state);
        if (app_state==1 || app_state == 0) {
            menu.findItem(R.id.disable_app_item).setTitle(R.string.disable_app);
        }else if(app_state==2){
            menu.findItem(R.id.disable_app_item).setTitle(R.string.enable_app);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        PackageInfo p = clickedPackageInfo;
        String applabel = clickedPackageLabel;
        int id = item.getItemId();

        switch (id) {

            case R.id.launch_item: {
                showMsg("Launching " + applabel);
                i = packageManager.getLaunchIntentForPackage(p.packageName);
                activity.startActivity(i);
                break;
            }

            case R.id.playstore_item: {
                showMsg("Opening " + applabel + " in Playstore");
                i = new Intent();
                i.setData(Uri.parse("market://details?id=" + p.packageName));
                activity.startActivity(i);
                break;
            }

            case R.id.sys_appinfo_item: {
                i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + p.packageName));
                activity.startActivity(i);
                break;
            }

            case R.id.uninstall_item: {
                showMsg("Uninstalling  " + applabel);
                if (!rootAccess) {
                    i = new Intent(Intent.ACTION_DELETE);
                    i.setData(Uri.parse(p.packageName));
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //i.putExtra(EXTRA_PACKAGE_NAME, p.packageName);
                    activity.startActivity(i);

                } else {
                    try {
                       showMsg("Uninstallation RootAccess :" + Shell.rootAccess());
                        Shell.su("pm uninstall " + p.packageName).exec();
                        showMsg(p.applicationInfo.loadLabel(packageManager).toString() + " uninstalled Successfully.");
                    } catch (Exception ex) {
                        showMsg("Uninstallation Failed :" + ex);
                    }
                }
                break;
            }

            case R.id.extractApk_item: {
                extract_apk(clickedPackageInfo.packageName);
                break;
            }

            case R.id.copy_appname_item: {
                copyToClipboard("App Name", applabel);
                showMsg( "App Name Copied");
                break;
            }

            case R.id.copy_pkgname_item: {
                copyToClipboard("Package Name", p.packageName);
               showMsg("Package Name Copied : "+p.packageName);
                break;
            }

            case R.id.copy_link_item: {
                copyToClipboard("Playstore Link", "market://details?id=" + p.packageName);
                showMsg("Playstore link copied");
                break;
            }

            case R.id.share_link_item: {
                i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, "market://details?id=" + p.packageName);
                activity.startActivity(createChooser(i, "Share Link Via"));
                break;
            }

            case R.id.killapp_item: {
                kill_app();
                break;
            }

            case R.id.clear_data_item: {
               clear_data();
                break;
            }

            case R.id.disable_app_item: {
                disable_app(item);
                break;
            }

            case R.id.keepdata_install_item: {
                keepdata_uninstall();
                break;
            }

            case R.id.revoke_perm_item:{
                revokePermission();
                break;
            }

            case R.id.grant_all_perm:{
                grantAllPermissions();
                break;
            }

            case android.R.id.home: {
                finish();
                break;
            }

            default: {
                Log.i(TAG, "unknown menu item clicked");
                break;
            }
        }
        return true;
    }

    private void toggleComponent(PackageManager pm, String pkgname, String componentString){
        ComponentName componentName = new ComponentName(pkgname,componentString);
        showMsg("Component : "+componentName);
        int result = isComponentEnabled(pm, pkgname, componentString);
        Log.i(TAG,"Component State received : "+result);

        try{
//            if(result == 0){
//                String command = "pm enable "+pkgname+"/"+componentString;
//                showMsg(command);
//                Shell.sh(command).exec();
//                showMsg(" Component Toggled to : "+pm.getComponentEnabledSetting(componentName));
//
//            }
            //else

            if(result == 1){
                showMsg(" Component will be Disabled : "+0);
                String command = "pm disable "+pkgname+"/"+componentString;
                showMsg(command);
                Shell.sh(command).exec();
                showMsg(" Component Toggled to : "+pm.getComponentEnabledSetting(componentName));

            }else if(result == 2){
                showMsg(" Component Enabled : "+componentString);
                String command = "pm enable "+pkgname+"/"+componentString;
                showMsg(command);
                Shell.sh(command).exec();
                showMsg(" Component Toggled to: "+pm.getComponentEnabledSetting(componentName));

            }else {
                showMsg(" Unexpected Component Status. ");
            }

//          packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_, 0);

        }catch(Exception e){
            Log.e(TAG,"Error Changing Component State") ;
        }

        setNewAdapter();
    }

    public static int isComponentEnabled(PackageManager pm, String pkgName, String clsName) {

        ComponentName componentName = new ComponentName(pkgName, clsName);
        int componentEnabledSetting = pm.getComponentEnabledSetting(componentName);
        Log.i(TAG,"Component Value from PACKAGE_MANAGER : "+componentEnabledSetting);

        switch (componentEnabledSetting) {

            case PackageManager.COMPONENT_ENABLED_STATE_DISABLED:
                return 2;
            case PackageManager.COMPONENT_ENABLED_STATE_ENABLED:
                return 1;
            case PackageManager.COMPONENT_ENABLED_STATE_DEFAULT:

            default:
                // We need to get the application info to get the component's default state
                try {
                    PackageInfo packageInfo = pm.getPackageInfo(pkgName,
                            PackageManager.GET_ACTIVITIES
                            | PackageManager.GET_RECEIVERS
                            | PackageManager.GET_SERVICES
                            | PackageManager.GET_PROVIDERS
                            | PackageManager.GET_DISABLED_COMPONENTS);

                    List<ComponentInfo> components = new ArrayList<>();
                    if (packageInfo.activities != null) Collections.addAll(components, packageInfo.activities);
                    if (packageInfo.services != null) Collections.addAll(components, packageInfo.services);
                    if (packageInfo.providers != null) Collections.addAll(components, packageInfo.providers);

                    Log.i(TAG," Component List Size "+ components.size());
                    
                    for (ComponentInfo componentInfo : components) {
                        if (componentInfo.name.contains(clsName)){
                            Log.i(TAG,"Component Info Name : "+componentInfo.name);
                            
                            if(componentInfo.isEnabled()){
                                return 1;
                            }else
                                return 2;
                           // return componentInfo.enabled;
                            //This is the default value (set in AndroidManifest.xml)
                            //return componentInfo.isEnabled(); //Whole package dependant
                        }
                    }

                    //the component is not declared in the AndroidManifest
                    return 1;
                }catch(PackageManager.NameNotFoundException e) {
                    // the package isn't installed on the device
                    Log.e(TAG,"NameNot Found Exception : "+e);
                    return 2;
            }
        }
    }

    private void revokePermission(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Revoking permissions for "+clickedPackageInfo.packageName);
        builder.setMessage("Click Yes to Continue...");
        builder.setCancelable(false);

        builder.setPositiveButton("Yes", (dialog, which) -> {
            try {

                List<String> list = appDetails.permissions_granted;
                if(null!=list){
                    for(int i = 0; i< list.size(); i++){
                        // if((clicked_pkg.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0){
                        String command =  "pm revoke " +clickedPackageInfo.packageName+" "+list.get(i);
                        Log.i(TAG,"Command : " +command);
                        Shell.sh(command).exec();
                        // }
                    }
                    Log.i(TAG, clickedPackageLabel + " permission revoked status : DONE " );
                }
                Log.i(TAG, clickedPackageLabel + " permission revoked Successfully");
                setNewAdapter();
            }catch(Exception ex) {
                Log.e(TAG, "Revoke Permission :" + ex);
            }
        });

        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        builder.show();

    }

    private void grantAllPermissions(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Granting All permissions to "+clickedPackageInfo.packageName);
        builder.setMessage("Click Yes to Continue...");
        builder.setCancelable(false);

        builder.setPositiveButton("Yes", (dialog, which) -> {
            try {
                List<String> list = appDetails.all_permissions;
                if(null!=list){
                    for(int i = 0; i< list.size(); i++){
                        // if((clicked_pkg.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0){
                        String command =  "pm grant " +clickedPackageInfo.packageName+" "+list.get(i);
                        Log.i(TAG,"Command : " +command);
                        Shell.sh(command).exec();
                        // }
                    }
                    Log.i(TAG, clickedPackageLabel + " permission granted status : DONE " );
                }
                setNewAdapter();
                Log.i(TAG, clickedPackageLabel + " permissions granted Successfully");
            }catch(Exception ex) {
                Log.e(TAG, "Grant Permisiion status : " + ex);
            }
        });

        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        builder.show();

    }

    private void keepdata_uninstall(){

        showMsg("Confirm to uninstall " + clickedPackageLabel);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm to uninstall but keep data " + clickedPackageLabel);
        builder.setMessage("Click Yes to Continue...");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", (dialog, which) -> {
            showMsg("Uninstalling without deleting data "+clickedPackageLabel);
            try {
                String command = "pm uninstall -k "+clickedPackageInfo.packageName;
                Shell.sh(command).exec();
                showMsg("Uninstalling " + clickedPackageLabel + " App without Deleting Data.");
            } catch (Exception ex) {
                Log.e(TAG, "Uninstalling App Failed For " + clickedPackageLabel);
            }
        });

        builder.setNegativeButton("No", (dialog, which) -> {
            showMsg("Uninstallation Cancelled of " + clickedPackageLabel);
            dialog.dismiss();
        });
        builder.show();
    }

    private void disable_app(MenuItem item){
        int app_state = packageManager.getApplicationEnabledSetting(clickedPackageInfo.packageName);
        Log.i(TAG,"APP STATE : "+app_state);

        if (app_state == 1 || app_state == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Confirm to disable " + clickedPackageLabel);
            builder.setMessage("Click Yes to Continue...");
            builder.setCancelable(false);
            builder.setPositiveButton("Yes", (dialog, which) -> {
                showMsg("Confirmed Disabling "+clickedPackageLabel);

                if (!rootAccess) {
                    pm.setApplicationEnabledSetting(clickedPackageInfo.packageName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
                    showMsg("Disabled App " + clickedPackageLabel);
                } else {
                    try {
                        String command = "pm disable " + clickedPackageInfo.packageName;
                        Shell.su(command).exec();
                        showMsg("Disabled App " + clickedPackageLabel);
                        item.setTitle(R.string.enable_app);
                    } catch (Exception ex) {
                        Log.e(TAG, "Disabling App Failed For " + clickedPackageLabel);
                    }
                }
            });

            builder.setNegativeButton("No", (dialog, which) -> showMsg(" Disabling Cancelled of " + clickedPackageLabel));
            builder.show();

        }
        else if(app_state==2){
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Confirm to Enable " + clickedPackageLabel);
            builder.setMessage("Click Yes to Continue...");
            builder.setCancelable(false);

            builder.setPositiveButton("Yes", (dialog, which) -> {
                showMsg("Confirmed Enabling"+clickedPackageLabel);
                if (!rootAccess) {
                    pm.setApplicationEnabledSetting(clickedPackageInfo.packageName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0);
                    showMsg("Enabled App  " + clickedPackageLabel);
                } else {
                    try {
                        String command = "pm enable " + clickedPackageInfo.packageName;
                        Shell.su().exec();
                        showMsg("Enabled App " + clickedPackageLabel);
                        item.setTitle(R.string.disable_app);
                    } catch (Exception ex) {
                        Log.e(TAG, "Enabling App Failed For " + clickedPackageLabel);
                    }
                }
            });

            builder.setNegativeButton("No", (dialog, which) -> showMsg("Enabling Cancelled for " + clickedPackageLabel));
            builder.show();
        }
    }

    private void kill_app(){

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Force Stop " + clickedPackageLabel);
        builder.setMessage("Click Yes to Continue...");
        builder.setCancelable(false);

        builder.setPositiveButton("Yes", (dialog, which) -> {
            showMsg("Confirmed Force Stopping "+clickedPackageLabel);
            if (!rootAccess) {
                activityManager.killBackgroundProcesses(clickedPackageInfo.packageName);
                Toast.makeText(getContext(), "Stopping process by NOROOT method of " + clickedPackageLabel, Toast.LENGTH_SHORT).show();
            } else {
                try {
                    String command = "am force-stop " + clickedPackageInfo.packageName;
                    Shell.su(command).exec();
                    Log.e(TAG, "Force Stopped " + clickedPackageLabel);
                } catch (Exception ex) {
                    Log.e(TAG, "Killing Cancelled" + clickedPackageLabel);
                }
            }
        });

        builder.setNegativeButton("No", (dialog, which) -> showMsg("Cancelled Stopping " + clickedPackageLabel));

        builder.show();
    }

    private void clear_data(){

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("You will lose all settings/Data of " + clickedPackageInfo.packageName);
        builder.setMessage("Click Yes to Continue...");
        builder.setCancelable(false);

        builder.setPositiveButton("Yes", (dialog, which) -> {
            showMsg("Confirmed Clearing data"+clickedPackageInfo.packageName);
            if (!rootAccess) {
                activityManager.killBackgroundProcesses(clickedPackageInfo.packageName);
                showMsg("Data Cleared by NOROOT method for  " + clickedPackageInfo.packageName);
            } else {
                try {
                    String command = "pm clear " + clickedPackageInfo.packageName;
                    Shell.su(command).exec();
                    showMsg("Cleared Data by ROOT method of  " + clickedPackageInfo.packageName);
                } catch (Exception ex) {
                    Log.e(TAG, "Data Clear Failed by ROOT method For " + clickedPackageInfo.packageName);
                }
            }
        });

        builder.setNegativeButton("No", (dialog, which) -> showMsg("Clearing Data Cancelled for " + clickedPackageInfo.packageName));
        builder.show();




    }

    private void extract_apk(String pkg_name){
        ClassApkOperation classApkOperationObject;
        Log.i(TAG, "Clicked Extract Apk of : "+pkg_name);

        File apk = new File(clickedPackageInfo.applicationInfo.sourceDir);
        Log.i(TAG, "found");
        classApkOperationObject = new ClassApkOperation(new ObjectAppPackageName(pkg_name, getContext()), getContext());
        classApkOperationObject.extractApk();
        //  showMsg("Package Extracted to - " + apkOperationObject.parent_folder.getAbsolutePath());
        Toast.makeText(getContext(), "Extracting Apk " + clickedPackageInfo.packageName, Toast.LENGTH_SHORT).show();
    }

    private void showMsg(String str){
        Log.i(TAG,str);
        Toast.makeText(activity,str,Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Toast.makeText(this," "+item.getTitle()+" clicked",Toast.LENGTH_SHORT).show();
        return  true;
    }

    private void copyToClipboard(String Title, String clip) {
        ClipData clipData = ClipData.newPlainText(Title, clip);
        clipboardManager.setPrimaryClip(clipData);
    }

    private String getSize(File file) {

        if (!file.isFile()) {
            throw new IllegalArgumentException("Expected a file");
        }
        final double length = file.length();

        if(length>GB){
            return format.format(length / GB) + " MB";
        }
        if (length > MB) {
            return format.format(length / MB) + " MB";
        }
        if (length > KB) {
            return format.format(length / KB) + " KB";
        }

        return format.format(length) + "Bytes";
    }
    

}

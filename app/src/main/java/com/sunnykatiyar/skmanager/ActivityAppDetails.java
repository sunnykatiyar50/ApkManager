package com.sunnykatiyar.skmanager;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListAdapter;
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

import static android.content.Intent.createChooser;
import static com.topjohnwu.superuser.internal.InternalUtils.getContext;

public class ActivityAppDetails extends AppCompatActivity {

    private static final String TAG = "MYAPP : APPDETAILS ACTIVITY : ";

    private final Activity activity = ActivityAppDetails.this;
  
    private ExpandableListView expandableListView;
    private List<String> headers;
    private ExpandableListAdapter exp_adapter;
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

    ClassApkOperation classApkOperationObject;

    static {
        Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR);
        Shell.Config.verboseLogging(BuildConfig.DEBUG);
        Shell.Config.setTimeout(10);
    }

    private static final DecimalFormat format = new DecimalFormat("#.##");
    private static final long MB = 1024 * 1024;
    private static final long KB = 1024;
    private static final long GB = 1024 * 1024 * 1024;

    public ClassSetAppDetails appDetails;
    HashMap<String, List<String>> detailed_Exp_List;
    
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
        TextView app_size = findViewById(R.id.app_size);
        rootAccess = ActivityMain.sharedPrefSettings.getBoolean(key_root_access,false);
        pm = this.getPackageManager();

        install_date.setText(new SimpleDateFormat().format(clickedPackageInfo.firstInstallTime));
        appinfo_pkgname.setText(clickedPackageInfo.packageName);
        app_version.setText(clickedPackageInfo.versionName);
        appinfo_icon.setImageDrawable(packageManager.getApplicationIcon(clickedPackageInfo.applicationInfo));
        appinfo_appname.setText(packageManager.getApplicationLabel(clickedPackageInfo.applicationInfo));
        app_size.setText(getSize(new File(clickedPackageInfo.applicationInfo.sourceDir)));

        appDetails = new ClassSetAppDetails(packageManager, clickedPackageInfo);
        expandableListView = findViewById(R.id.expandable_list);
        detailed_Exp_List = appDetails.expandable_list;
        headers = new ArrayList<>(detailed_Exp_List.keySet());

        Collections.sort(headers, (s, t1) -> s.compareToIgnoreCase(t1));

        exp_adapter = new AdapterExpandableList(this, headers, detailed_Exp_List);
        expandableListView.setAdapter(exp_adapter);

        expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            Intent activityIntent = new Intent();
            Log.e("in Appdetails","i can detect a childclick");
            //Toast.makeText(v.getContext(),"i can detect a childclick", Toast.LENGTH_SHORT).show();


            if(exp_adapter.getGroup(groupPosition).toString().equals("Activities"))
            {
                String clicked_activity = exp_adapter.getChild(groupPosition,childPosition).toString();
                activityIntent.setComponent(new ComponentName(clickedPackageInfo.packageName,clicked_activity) );
                Toast.makeText(v.getContext(), clicked_activity, Toast.LENGTH_SHORT).show();
                startActivity(activityIntent);

            }

            if(exp_adapter.getGroup(groupPosition).toString().equals("Permissions- Requested"))
            {
                String clicked_perm= exp_adapter.getChild(groupPosition,childPosition).toString();
                Toast.makeText(v.getContext(),clicked_perm , Toast.LENGTH_SHORT).show();

               // if(ActivityMain.)

            }


            return true;
        });

        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            int LastExpandedGroup = -1;
            @Override
            public void onGroupExpand(int position) {
                if (LastExpandedGroup != -1 && position != LastExpandedGroup && exp_adapter.getChildrenCount(LastExpandedGroup) >= 2) {
                    expandableListView.collapseGroup(LastExpandedGroup);
                }
                LastExpandedGroup = position;
                Toast.makeText(getApplicationContext(), headers.get(position), Toast.LENGTH_SHORT);
            }
        });

        expandableListView.setOnGroupCollapseListener(position -> Toast.makeText(getApplication(), headers.get(position), Toast.LENGTH_SHORT));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_app_details,menu);
        if (clickedPackageInfo.applicationInfo.enabled) {
            menu.findItem(R.id.disable_app_item).setTitle(R.string.disable_app);
        } else {
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

            case R.id.reset_perm_item:{
                revokePermission();
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

    private void revokePermission(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Revoking permissions for "+clickedPackageInfo.packageName);
        builder.setMessage("Click Yes to Continue...");
        builder.setCancelable(false);

        builder.setPositiveButton("Yes", (dialog, which) -> {
            try {

//                showMsg("Revoke permission via terminal :" + Shell.rootAccess());
//                String[] reqPerms = clickedPackageInfo.requestedPermissions;
//                for(String perm : reqPerms){
//                    String command =  "pm revoke " +clickedPackageInfo.packageName+" "+perm;
//                    Log.i(TAG,"Command : " +command);
//                    Shell.sh(command).exec();
//                }

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
            }catch(Exception ex) {
                Log.e(TAG, "Revoke Permission :" + ex);
            }
        });

        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        builder.show();

    }

    private void keepdata_uninstall(){

        showMsg("Confirm to uninstall " + clickedPackageLabel);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
        if (clickedPackageInfo.applicationInfo.enabled) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
                        String command = "pm disable-user " + clickedPackageInfo.packageName;
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
        else {
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

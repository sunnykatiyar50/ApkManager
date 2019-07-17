package com.sunnykatiyar.appmanager;

import android.app.Activity;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static android.content.Intent.createChooser;
import static com.sunnykatiyar.appmanager.FragmentAppManager.activityManager;
import static com.sunnykatiyar.appmanager.FragmentAppManager.appContext;
import static com.sunnykatiyar.appmanager.FragmentAppManager.clipboardManager;
import static com.sunnykatiyar.appmanager.FragmentAppManager.mainpm;
import static com.sunnykatiyar.appmanager.AdapterAppList.clicked_pkg;
import static com.sunnykatiyar.appmanager.AdapterAppList.clicked_pkg_label;
import static com.topjohnwu.superuser.internal.InternalUtils.getContext;

public class ActivityAppDetails extends AppCompatActivity {

    private static final String TAG = "MYAPP : APPDETAILS ACTIVITY : ";

    protected Activity activity = ActivityAppDetails.this;
    ImageView appinfo_icon;
    TextView appinfo_appname;
    TextView app_version;
    TextView appinfo_pkgname;
    TextView install_date;
    TextView app_size;
    PackageManager appinfo_pm = mainpm;
    PackageInfo appinfo_clicked_pkg = AdapterAppList.clicked_pkg;
    protected static Toolbar toolbar;
    ExpandableListView expandableListView;
    HashMap<String, List<String>> Detailed_Exp_List;
    List<String> headers;
    ExpandableListAdapter exp_adapter;
    //AppMenu appinfo_optionmenu;
    boolean rootAccess;
    public static final String key_root_access = FragmentSettings.key_root_access;
    final String path_not_set = "PATH NOT SET";
    PackageManager pm;
    ClassApkOperation classApkOperationObject;

    static {
        Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR);
        Shell.Config.verboseLogging(BuildConfig.DEBUG);
        //Shell.Config.setTimeout(10);
    }

    private static final DecimalFormat format = new DecimalFormat("#.##");
    private static final long MB = 1024 * 1024;
    private static final long KB = 1024;
    private static final long GB = 1024 * 1024 * 1024;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_details);
        toolbar = findViewById(R.id.toolbar_app_details);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(clicked_pkg_label);
        getSupportActionBar().setElevation(5);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        appinfo_icon = findViewById(R.id.app_info_icon);
        appinfo_appname =  findViewById(R.id.app_name);
        app_version = findViewById(R.id.version_num);
        appinfo_pkgname = findViewById(R.id.pkg_name);
        install_date = findViewById(R.id.install_date);
        app_size = findViewById(R.id.app_size);
        rootAccess = ActivityMain.sharedPrefSettings.getBoolean(key_root_access,false);
        pm = this.getPackageManager();

        install_date.setText(new SimpleDateFormat().format(appinfo_clicked_pkg.firstInstallTime));
        appinfo_pkgname.setText(appinfo_clicked_pkg.packageName);
        app_version.setText(appinfo_clicked_pkg.versionName);
        appinfo_icon.setImageDrawable(appinfo_pm.getApplicationIcon(appinfo_clicked_pkg.applicationInfo));
        appinfo_appname.setText(appinfo_pm.getApplicationLabel(appinfo_clicked_pkg.applicationInfo));
        app_size.setText(getSize(new File(appinfo_clicked_pkg.applicationInfo.sourceDir)));

        ClassSetAppDetails appDetails = new ClassSetAppDetails(mainpm, appinfo_clicked_pkg);
        expandableListView = findViewById(R.id.expandable_list);
        Detailed_Exp_List = appDetails.setListData();
        headers = new ArrayList<>(Detailed_Exp_List.keySet());

        Collections.sort(headers, new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                return s.compareToIgnoreCase(t1);
            }
        });

        exp_adapter = new AdapterExpandableList(this, headers, Detailed_Exp_List);
        expandableListView.setAdapter(exp_adapter);

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Intent activityIntent = new Intent();
                Log.e("in Appdetails","i can detect a childclick");
                //Toast.makeText(v.getContext(),"i can detect a childclick", Toast.LENGTH_SHORT).show();
                String clicked_activity = exp_adapter.getChild(groupPosition,childPosition).toString();
                activityIntent.setComponent(new ComponentName(appinfo_clicked_pkg.packageName,clicked_activity) );
                if (exp_adapter.getGroup(groupPosition).toString()=="Activities")
                {
                    Toast.makeText(v.getContext(), clicked_activity, Toast.LENGTH_SHORT).show();
                    startActivity(activityIntent);

                }
                return true;
            }
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

        expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int position) {
                Toast.makeText(getApplication(), headers.get(position), Toast.LENGTH_SHORT);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_app_details,menu);
        if (clicked_pkg.applicationInfo.enabled) {
            menu.findItem(R.id.disable_app_item).setTitle(R.string.disable_app);
        } else {
            menu.findItem(R.id.disable_app_item).setTitle(R.string.enable_app);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        PackageInfo p = appinfo_clicked_pkg;
        String applabel = (String) p.applicationInfo.loadLabel(mainpm);
        int id = item.getItemId();

        switch (id) {

            case R.id.launch_item: {
                showMsg("Launching " + applabel);
                i = mainpm.getLaunchIntentForPackage(p.packageName);
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
                i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + p.packageName));
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
                        showMsg(p.applicationInfo.loadLabel(mainpm).toString() + " uninstalled Successfully.");
                    } catch (Exception ex) {
                        showMsg("Uninstallation Failed :" + ex);
                    }
                }
                break;
            }

            case R.id.extractApk_item: {
               extract_apk(clicked_pkg.packageName);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(appContext);
        builder.setTitle("Confirm Revoking permissions for "+clicked_pkg_label);
        builder.setMessage("Click Yes to Continue...");
        builder.setCancelable(false);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    showMsg("Revoke permission via terminal :" + Shell.rootAccess());
                   // Shell.sh("pm reset-permissions " + clicked_pkg.packageName).exec();
                    Log.i(TAG, clicked_pkg.applicationInfo.loadLabel(mainpm).toString() + " permission revoked Successfully");
                } catch (Exception ex) {
                    Log.e(TAG, "Revoke Permission :" + ex);
                }
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();

    }

    private void keepdata_uninstall(){

        showMsg("Confirm to uninstall " + clicked_pkg_label);
        AlertDialog.Builder builder = new AlertDialog.Builder(appContext);
        builder.setTitle("Confirm to uninstall but keep data " + clicked_pkg_label);
        builder.setMessage("Click Yes to Continue...");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showMsg("Uninstalling without deleting data "+clicked_pkg_label);
                try {
                    String command = "pm uninstall -k "+clicked_pkg.packageName;
                    Shell.sh(command).exec();
                    showMsg("Uninstalling " + clicked_pkg_label + " App without Deleting Data.");
                } catch (Exception ex) {
                    Log.e(TAG, "Uninstalling App Failed For " + clicked_pkg_label);
                }
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showMsg("Uninstallation Cancelled of " + clicked_pkg_label);
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void disable_app(MenuItem item){
        if (clicked_pkg.applicationInfo.enabled) {
            AlertDialog.Builder builder = new AlertDialog.Builder(appContext);
            builder.setTitle("Confirm to disable " + clicked_pkg_label);
            builder.setMessage("Click Yes to Continue...");
            builder.setCancelable(false);
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showMsg("Confirmed Disabling "+clicked_pkg_label);

                    if (!rootAccess) {
                        pm.setApplicationEnabledSetting(clicked_pkg.packageName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
                        showMsg("Disabled App " + clicked_pkg_label);
                    } else if (rootAccess) {
                        try {
                            String command = "pm disable-user " + clicked_pkg.packageName;
                            Shell.su(command).exec();
                            showMsg("Disabled App " + clicked_pkg_label);
                            item.setTitle(R.string.enable_app);
                        } catch (Exception ex) {
                            Log.e(TAG, "Disabling App Failed For " + clicked_pkg_label);
                        }
                    }                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showMsg(" Disabling Cancelled of " + clicked_pkg_label);

                }
            });
            builder.show();

        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Confirm to Enable " + clicked_pkg_label);
            builder.setMessage("Click Yes to Continue...");
            builder.setCancelable(false);

            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showMsg("Confirmed Enabling"+clicked_pkg_label);
                    if (!rootAccess) {
                        pm.setApplicationEnabledSetting(clicked_pkg.packageName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0);
                        showMsg("Enabled App  " + clicked_pkg_label);
                    } else if (rootAccess) {
                        try {
                            String command = "pm enable " + clicked_pkg.packageName;
                            Shell.su().exec();
                            showMsg("Enabled App " + clicked_pkg_label);
                            item.setTitle(R.string.disable_app);
                        } catch (Exception ex) {
                            Log.e(TAG, "Enabling App Failed For " + clicked_pkg_label);
                        }
                    }
                }
            });

            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showMsg("Enabling Cancelled for " + clicked_pkg_label);

                }
            });
            builder.show();
        }
    }

    private void kill_app(){

        AlertDialog.Builder builder = new AlertDialog.Builder(appContext);
        builder.setTitle("Force Stop " + clicked_pkg_label);
        builder.setMessage("Click Yes to Continue...");
        builder.setCancelable(false);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showMsg("Confirmed Force Stopping "+clicked_pkg_label);
                if (!rootAccess) {
                    activityManager.killBackgroundProcesses(clicked_pkg.packageName);
                    Toast.makeText(getContext(), "Stopping process by NOROOT method of " + clicked_pkg_label, Toast.LENGTH_SHORT).show();
                } else if (rootAccess) {
                    try {
                        String command = "am force-stop " + clicked_pkg.packageName;
                        Shell.su(command).exec();
                        Log.e(TAG, "Force Stopped " + clicked_pkg_label);
                    } catch (Exception ex) {
                        Log.e(TAG, "Killing Cancelled" + clicked_pkg_label);
                    }
                }            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showMsg("Cancelled Stopping " + clicked_pkg_label);

            }
        });

        builder.show();
    }

    private void clear_data(){

        AlertDialog.Builder builder = new AlertDialog.Builder(appContext);
        builder.setTitle("You will lose all settings/Data of " + clicked_pkg_label);
        builder.setMessage("Click Yes to Continue...");
        builder.setCancelable(false);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showMsg("Confirmed Clearing data"+clicked_pkg_label);
                if (!rootAccess) {
                    activityManager.killBackgroundProcesses(clicked_pkg.packageName);
                    showMsg("Data Cleared by NOROOT method for  " + clicked_pkg_label);
                } else if (rootAccess) {
                    try {
                        String command = "pm clear " + clicked_pkg.packageName;
                        Shell.su(command).exec();
                        showMsg("Cleared Data by ROOT method of  " + clicked_pkg_label);
                    } catch (Exception ex) {
                        Log.e(TAG, "Data Clear Failed by ROOT method For " + clicked_pkg_label);
                    }
                }            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showMsg("Clearing Data Cancelled for " + clicked_pkg_label);

            }
        });
        builder.show();




    }

    private void extract_apk(String pkg_name){
        ClassApkOperation classApkOperationObject;
        Log.i(TAG, "Clicked Extract Apk of : "+pkg_name);

        File apk = new File(clicked_pkg.applicationInfo.sourceDir);
        if (apk == null) {
            Toast.makeText(getContext(), "No Apk Available", Toast.LENGTH_SHORT);
        } else {
            Log.i(TAG, "found");
            classApkOperationObject = new ClassApkOperation(new ObjectAppPackageName(pkg_name, getContext()), getContext());
            classApkOperationObject.extractApk();
            //  showMsg("Package Extracted to - " + apkOperationObject.parent_folder.getAbsolutePath());
        }
        Toast.makeText(getContext(), "Extracting Apk " + clicked_pkg_label, Toast.LENGTH_SHORT).show();
    }

    void showMsg(String str){
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

    public String getSize(File file) {

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

package com.sunnykatiyar.skmanager;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static android.content.Intent.createChooser;
import static com.sunnykatiyar.skmanager.FragmentAppManager.activityManager;
import static com.sunnykatiyar.skmanager.FragmentAppManager.appContext;
import static com.sunnykatiyar.skmanager.FragmentAppManager.clipboardManager;
import static com.sunnykatiyar.skmanager.FragmentAppManager.mainpm;
import static com.topjohnwu.superuser.internal.InternalUtils.getContext;

public class AdapterAppList extends RecyclerView.Adapter<ViewHolderAppList>{

    private PackageManager pm;
    private List<PackageInfo> myAppList = new ArrayList<>();
    private Activity activity;
    private final String TAG = "APPLIST ADAPTER : ";
    public static PackageInfo clicked_pkg;
    public static String clicked_pkg_label;
    private boolean rootAccess ;
    private static final String key_root_access = FragmentSettings.key_root_access;
    final String path_not_set = "PATH NOT SET";
    private static final DecimalFormat format = new DecimalFormat("#.##");
    private static final long MB = 1024 * 1024;
    private static final long KB = 1024;
    private static final long GB = 1024 * 1024 * 1024;
    ActivityAppDetails activityAppDetails;

    static {
        Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR);
        Shell.Config.verboseLogging(BuildConfig.DEBUG);
        Shell.Config.setTimeout(10);
    }

    public AdapterAppList(PackageManager pkgmgr, List<PackageInfo> myAppList, Activity activity) {
        this.pm = pkgmgr;
        this.activity = activity;
        this.myAppList = myAppList;
        this.rootAccess = ActivityMain.sharedPrefSettings.getBoolean(key_root_access,false);
        //this.filteredAppList = myAppList;
    }

    @NonNull
    @Override
    public ViewHolderAppList onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater in = LayoutInflater.from(context);
        View view = in.inflate(R.layout.listitem_app_list,viewGroup,false);

        ViewHolderAppList cat = new ViewHolderAppList(view);
        return cat;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolderAppList vholder, final int i) {

        final PackageInfo pkginfo = this.myAppList.get(i);
        vholder.pkgname.setText(pkginfo.packageName);
        vholder.version.setText("v" + pkginfo.versionName);
        vholder.appname.setText(pm.getApplicationLabel(pkginfo.applicationInfo).toString());
        vholder.applogo.setImageDrawable(pm.getApplicationIcon(pkginfo.applicationInfo));
        vholder.install_date.setText(getTime(pkginfo.lastUpdateTime));
        File f = new File(pkginfo.applicationInfo.sourceDir);
        vholder.app_size.setText(getSize(f.length()));

        if(pkginfo.applicationInfo.sourceDir.startsWith("/system/")){
            vholder.app_type.setText("System App");
            vholder.app_type.setTextColor(getContext().getColor(R.color.red));
        }else{
            vholder.app_type.setText("User App");
            vholder.app_type.setTextColor(getContext().getColor(R.color.light_green));
        }

        vholder.text_extra.setText(String.valueOf(pkginfo.versionCode));
        activityAppDetails = new ActivityAppDetails();

        vholder.itemView.setOnClickListener(null);
        vholder.itemView.setOnClickListener(v -> {
            clicked_pkg = myAppList.get(i);
            clicked_pkg_label = mainpm.getApplicationLabel(clicked_pkg.applicationInfo).toString();
            Log.i(TAG, "Item clicked :" + clicked_pkg_label);
            Toast.makeText(appContext, clicked_pkg_label, Toast.LENGTH_SHORT).show();
            Intent appInfo = new Intent(activity, activityAppDetails.getClass());
            appInfo.putExtra("PACKAGE_NAME", clicked_pkg.packageName);
            activity.startActivity(appInfo);
        });

        vholder.itemView.setOnCreateContextMenuListener(null);
        vholder.itemView.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
            clicked_pkg = myAppList.get(i);
            clicked_pkg_label = mainpm.getApplicationLabel(clicked_pkg.applicationInfo).toString();
            activityAppDetails.appDetails = new ClassSetAppDetails(pm, clicked_pkg);

            MenuInflater inflater = activity.getMenuInflater();
            inflater.inflate(R.menu.menu_app_details, menu);
            PackageInfo p = myAppList.get(i);
            Log.i(TAG, " CLICKED PACKAGE: " + (clicked_pkg==null));

            menu.setHeaderTitle(clicked_pkg_label);

            if (clicked_pkg != null) {
//----------------------------------------------------SET  DISABLE MENU ITEM----------------------------------------------------
                if (clicked_pkg.applicationInfo.enabled) {
                    menu.findItem(R.id.disable_app_item).setTitle(R.string.disable_app);
                } else {
                    menu.findItem(R.id.disable_app_item).setTitle(R.string.enable_app);
                }

//--------------------------------------------------------LAUNCH APP----------------------------------------------------
                menu.findItem(R.id.launch_item).setOnMenuItemClickListener(item -> {
                    showMsg("Launching " + clicked_pkg_label);
                    Intent i14 = mainpm.getLaunchIntentForPackage(clicked_pkg.packageName);
                    activity.startActivity(i14);
                    return true;
                });

//--------------------------------------------------------OPEN IN PLAYSTORE----------------------------------------------------
                menu.findItem(R.id.playstore_item).setOnMenuItemClickListener(item -> {
                    showMsg("Opening " + clicked_pkg_label + " in Playstore");
                    Intent i13 = new Intent();
                    i13.setData(Uri.parse("market://details?id=" + clicked_pkg.packageName));
                    activity.startActivity(i13);
                    return true;
                });

//--------------------------------------------------------OPEN_APPINFO----------------------------------------------------
                menu.findItem(R.id.sys_appinfo_item).setOnMenuItemClickListener(item -> {
                    Intent i12 = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + clicked_pkg.packageName));
                    i12.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    activity.startActivity(i12);
                    return true;
                });

//--------------------------------------------------------UNINSTALL APP AND DATA----------------------------------------------------
                menu.findItem(R.id.uninstall_item).setOnMenuItemClickListener(item -> {
                    uninstall();
                    return true;
                });

//----------------------------------------------------EXTRACT APK----------------------------------------------------
                menu.findItem(R.id.extractApk_item).setOnMenuItemClickListener(item -> {
                    Log.i(TAG, "PACKAGE TO BE EXTRACTED : " + clicked_pkg.packageName);
                    extract_apk(clicked_pkg.packageName);
                    return true;
                });

//-------------------------------------------------------COPY MENU----------------------------------------------------
                menu.findItem(R.id.copy_appname_item).setOnMenuItemClickListener(item -> {
                    copyToClipboard("App Name", clicked_pkg_label);
                    Toast.makeText(activity, "App Name Copied", Toast.LENGTH_SHORT).show();
                    return true;
                });

                menu.findItem(R.id.copy_pkgname_item).setOnMenuItemClickListener(item -> {
                    copyToClipboard("Package Name", clicked_pkg.packageName);
                    Toast.makeText(activity, "Package Name Copied", Toast.LENGTH_SHORT).show();
                    return true;
                });

                menu.findItem(R.id.copy_link_item).setOnMenuItemClickListener(item -> {
                    copyToClipboard("Playstore Link", "market://details?id=" + clicked_pkg.packageName);
                    Toast.makeText(activity, "Playstore link copied", Toast.LENGTH_SHORT).show();
                    return true;
                });

                menu.findItem(R.id.share_link_item).setOnMenuItemClickListener(item -> {
                    Intent i1 = new Intent(Intent.ACTION_SEND);
                    i1.setType("text/plain");
                    i1.putExtra(Intent.EXTRA_TEXT, "market://details?id=" + clicked_pkg.packageName);
                    activity.startActivity(createChooser(i1, "Share Link Via"));
                    return true;
                });

//--------------------------------------------------------KILL_APP----------------------------------------------------

                menu.findItem(R.id.killapp_item).setOnMenuItemClickListener(item -> {
                    kill_app();
                    return true;
                });

//---------------------------------------------------CLEAR_DATA---------------------------------------------------

                menu.findItem(R.id.clear_data_item).setOnMenuItemClickListener(item -> {
                    clear_data();
                    return true;
                });

//--------------------------------------------------DISABLE/ENABLE_APP----------------------------------------------------
                menu.findItem(R.id.disable_app_item).setOnMenuItemClickListener(item -> {
                    disable_app(item);
                    return true;
                });

//----------------------------------------------------UNINSTALL APP BUT KEEP DATA----------------------------------------------------
                menu.findItem(R.id.keepdata_install_item).setOnMenuItemClickListener(item -> {
                    keepdata_uninstall();
                    return true;
                });

//--------------------------------REVOKING PERMISSIONS----------------------------------------------------
                menu.findItem(R.id.revoke_perm_item).setOnMenuItemClickListener(item -> {
                    revokePermission();
                    return true;
                });

//--------------------------------------------GRANTING PERMISIIONS---------------------------------------------------
                menu.findItem(R.id.grant_all_perm).setOnMenuItemClickListener(item -> {
                    grantAllPermissions();
                    return true;
                });
            }
            else {
                showMsg("Clicked Pkg = null.");
            }
        });

    }

    private void revokePermission(){
        AlertDialog.Builder builder = new AlertDialog.Builder(appContext);
        builder.setTitle("Confirm Revoking permissions for "+clicked_pkg_label);
        builder.setMessage("Click Yes to Continue...");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", (dialog, which) -> {
                try {
                    showMsg("Revoking permission via terminal :" + Shell.rootAccess());
                    List<String> list = activityAppDetails.appDetails.permissions_granted;
                    if(null!=list){
                        for(int i = 0; i< list.size(); i++){
                            // if((clicked_pkg.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0){
                            String command =  "pm revoke " +clicked_pkg.packageName+" "+list.get(i);
                            Log.i(TAG,"Command : " +command);
                            Shell.sh(command).exec();
                            // }
                        }
                        Log.i(TAG, clicked_pkg_label + " permission revoked status : DONE " );
                    }



//                    PermissionInfo[] allPerms = clicked_pkg.permissions;
//
//                    if(null!=allPerms){
//                        for(PermissionInfo perm : allPerms){
//                            String command =  "pm revoke " +clicked_pkg.packageName+" "+perm.name;
//                            Log.i(TAG,"Command : " +command);
//                            Shell.su(command).exec();
//                        }
//                    }

                } catch (Exception ex) {
                    Log.e(TAG, "Revoke Permission :" + ex);
                }
        });

        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        builder.show();

    }

    private void grantAllPermissions(){
        AlertDialog.Builder builder = new AlertDialog.Builder(appContext);
        builder.setTitle("Confirm Granting All permissions to "+clicked_pkg.packageName);
        builder.setMessage("Click Yes to Continue...");
        builder.setCancelable(false);

        builder.setPositiveButton("Yes", (dialog, which) -> {
            try {
                List<String> list = activityAppDetails.appDetails.all_permissions;
                if(null!=list){
                    for(int i = 0; i< list.size(); i++){
                        // if((clicked_pkg.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0){
                        String command =  "pm grant " +clicked_pkg.packageName+" "+list.get(i);
                        Log.i(TAG,"Command : " +command);
                        Shell.sh(command).exec();
                        // }
                    }
                    Log.i(TAG, clicked_pkg_label + " permission granted status : DONE " );
                }
                Log.i(TAG, clicked_pkg_label + " permissions granted Successfully");
            }catch(Exception ex) {
                Log.e(TAG, "Grant Permisiion status : " + ex);
            }
        });

        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        builder.show();

    }

    private void uninstall(){

        AlertDialog.Builder builder = new AlertDialog.Builder(appContext);
        builder.setTitle("Confirm to uninstall " + clicked_pkg_label);
        builder.setMessage("Click Yes to Continue...");
        builder.setCancelable(false);

        builder.setPositiveButton("Yes", (dialog, which) -> {
            showMsg("Confirmed Uninstalling"+clicked_pkg_label);
            if (!rootAccess) {
                Intent intent = new Intent("android.intent.action.DELETE");
                intent.setData(Uri.parse("package:" + clicked_pkg.packageName));
                appContext.startActivity(intent);
            } else {
                try {
                    showMsg("Uninstallation by RootAccess :" + Shell.rootAccess());
                    Shell.sh("pm uninstall " + clicked_pkg.packageName).exec();
                    Log.i(TAG, clicked_pkg.applicationInfo.loadLabel(mainpm).toString() + " uninstalled Successfully by ROOT method.");
                } catch (Exception ex) {
                    Log.i(TAG, "Uninstallation Failed by ROOT method :" + ex);
                }
            }
        });

        builder.setNegativeButton("No", (dialog, which) -> {
            showMsg("Uninstallation Cancelled of " + clicked_pkg_label);
            dialog.dismiss();
        });
        builder.show();
    }

    private void extract_apk(String pkg_name){
        ClassApkOperation classApkOperationObject;
        Log.i(TAG, "Clicked Extract Apk of : "+pkg_name);

        File apk = new File(clicked_pkg.applicationInfo.sourceDir);
        Log.i(TAG, "found");
        classApkOperationObject = new ClassApkOperation(new ObjectAppPackageName(pkg_name, getContext()), getContext());
        classApkOperationObject.extractApk();
        //  showMsg("Package Extracted to - " + apkOperationObject.parent_folder.getAbsolutePath());
        Toast.makeText(getContext(), "Extracting Apk " + clicked_pkg_label, Toast.LENGTH_SHORT).show();
    }

    private void keepdata_uninstall(){

        showMsg("Confirm to uninstall " + clicked_pkg_label);
        AlertDialog.Builder builder = new AlertDialog.Builder(appContext);
        builder.setTitle("Confirm to uninstall but keep data " + clicked_pkg_label);
        builder.setMessage("Click Yes to Continue...");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", (dialog, which) -> {
            showMsg("Uninstalling without deleting data "+clicked_pkg_label);
            try {
                String command = "pm uninstall -k "+clicked_pkg.packageName;
                Shell.sh(command).exec();
                showMsg("Uninstalling " + clicked_pkg_label + " App without Deleting Data.");
            } catch (Exception ex) {
                Log.e(TAG, "Uninstalling App Failed For " + clicked_pkg_label);
            }
        });

        builder.setNegativeButton("No", (dialog, which) -> {
            showMsg("Uninstallation Cancelled of " + clicked_pkg_label);
             dialog.dismiss();
        });
        builder.show();
    }

    private void kill_app(){

        AlertDialog.Builder builder = new AlertDialog.Builder(appContext);
        builder.setTitle("Force Stop " + clicked_pkg_label);
        builder.setMessage("Click Yes to Continue...");
        builder.setCancelable(false);

        builder.setPositiveButton("Yes", (dialog, which) -> {
            showMsg("Confirmed Force Stopping "+clicked_pkg_label);
            if (!rootAccess) {
                activityManager.killBackgroundProcesses(clicked_pkg.packageName);
                Toast.makeText(getContext(), "Stopping process by NOROOT method of " + clicked_pkg_label, Toast.LENGTH_SHORT).show();
            } else {
                try {
                    String command = "am force-stop " + clicked_pkg.packageName;
                    Shell.su(command).exec();
                    Log.e(TAG, "Force Stopped " + clicked_pkg_label);
                } catch (Exception ex) {
                    Log.e(TAG, "Killing Cancelled" + clicked_pkg_label);
                }
            }
        });

        builder.setNegativeButton("No", (dialog, which) -> showMsg("Cancelled Stopping " + clicked_pkg_label));

        builder.show();
    }

    private void clear_data(){

        AlertDialog.Builder builder = new AlertDialog.Builder(appContext);
        builder.setTitle("You will lose all settings/Data of " + clicked_pkg_label);
        builder.setMessage("Click Yes to Continue...");
        builder.setCancelable(false);

        builder.setPositiveButton("Yes", (dialog, which) -> {
            showMsg("Confirmed Clearing data"+clicked_pkg_label);
            if (!rootAccess) {
                activityManager.killBackgroundProcesses(clicked_pkg.packageName);
                showMsg("Data Cleared by NOROOT method for  " + clicked_pkg_label);
            } else {
                try {
                    String command = "pm clear " + clicked_pkg.packageName;
                    Shell.su(command).exec();
                    showMsg("Cleared Data by ROOT method of  " + clicked_pkg_label);
                } catch (Exception ex) {
                    Log.e(TAG, "Data Clear Failed by ROOT method For " + clicked_pkg_label);
                }
            }
        });

        builder.setNegativeButton("No", (dialog, which) -> showMsg("Clearing Data Cancelled for " + clicked_pkg_label));
        builder.show();




    }

    private void disable_app(MenuItem item){
        if (clicked_pkg.applicationInfo.enabled) {


            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Confirm to disable " + clicked_pkg_label);
            builder.setMessage("Click Yes to Continue...");
            builder.setCancelable(false);
            builder.setPositiveButton("Yes", (dialog, which) -> {
                showMsg("Confirmed Disabling "+clicked_pkg_label);

                if (!rootAccess) {
                    pm.setApplicationEnabledSetting(clicked_pkg.packageName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
                    showMsg("Disabled App " + clicked_pkg_label);
                } else {
                    try {
                        String command = "pm disable-user " + clicked_pkg.packageName;
                        Shell.su(command).exec();
                        showMsg("Disabled App " + clicked_pkg_label);
                        item.setTitle(R.string.enable_app);
                    } catch (Exception ex) {
                        Log.e(TAG, "Disabling App Failed For " + clicked_pkg_label);
                    }
                }
            });

            builder.setNegativeButton("No", (dialog, which) -> showMsg(" Disabling Cancelled of " + clicked_pkg_label));
            builder.show();

        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Confirm to Enable " + clicked_pkg_label);
            builder.setMessage("Click Yes to Continue...");
            builder.setCancelable(false);

            builder.setPositiveButton("Yes", (dialog, which) -> {
                showMsg("Confirmed Enabling"+clicked_pkg_label);
                if (!rootAccess) {
                    pm.setApplicationEnabledSetting(clicked_pkg.packageName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0);
                    showMsg("Enabled App  " + clicked_pkg_label);
                } else {
                    try {
                        String command = "pm enable " + clicked_pkg.packageName;
                        Shell.su().exec();
                        showMsg("Enabled App " + clicked_pkg_label);
                        item.setTitle(R.string.disable_app);
                    } catch (Exception ex) {
                        Log.e(TAG, "Enabling App Failed For " + clicked_pkg_label);
                    }
                }
            });

            builder.setNegativeButton("No", (dialog, which) -> showMsg("Enabling Cancelled for " + clicked_pkg_label));
            builder.show();
        }
    }

    private String getTime(long time){
        DateFormat dateFormat = new SimpleDateFormat("hh:mm a dd-MM-yy");
        String strDate = dateFormat.format(time);
        return strDate;
    }

    private void copyToClipboard(String Title, String clip) {
        ClipData clipData = ClipData.newPlainText(Title, clip);
        clipboardManager.setPrimaryClip(clipData);
    }

    private String getSize(long length) {

        final long KB = 1024;
        final long MB = 1024 * 1024;
        final long GB = 1024 * 1024 * 1024;

        final DecimalFormat format = new DecimalFormat("###.##");

        if (length > GB) {
            return format.format((float)length / GB) + " GB";
        }
        if (length > MB) {
            return format.format((float)length / MB) + " MB";
        }
        if (length > KB) {
            return format.format((float)length / KB) + " KB";
        }

        return format.format(length) + " Bytes";
    }

    private void showMsg(String str){
        Toast.makeText(activity,str,Toast.LENGTH_SHORT).show();
        Log.i(TAG, str);
    }

    @Override
    public int getItemCount() {
        if(this.myAppList==null){
            return 0;
        }else
            return this.myAppList.size();
    }

    @Override
    public long getItemId(int i)
    {
        return 0;
    }

}

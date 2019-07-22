//package com.sunnykatiyar.skmanager;
//
//import android.content.ClipData;
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.PackageInfo;
//import android.net.Uri;
//import android.util.Log;
//import android.widget.Toast;
//import com.topjohnwu.superuser.Shell;
//
//import java.io.File;
//
//import static android.content.Intent.createChooser;
//import static com.sunnykatiyar.skmanager.FragmentAppManager.activityManager;
//import static com.sunnykatiyar.skmanager.FragmentAppManager.clipboardManager;
//import static com.sunnykatiyar.skmanager.FragmentAppManager.mainpm;
//import static com.sunnykatiyar.skmanager.AdapterAppList.clicked_pkg;
//
//
///**
// * Created by Sunny Katiyar on 14-06-2017.
// */
//
//public class AppMenu {
//
//    final public String TAG = " APP_MENU_ACTIVITY : ";
//    final public String copy_pname = "Copy Package Name";
//    final public String copy_name ="Copy App Name";
//    final public String copy_link = "Copy Playstore Link";
//    final public String extract_apk = "Extract Apk";
//    final public String copy = "Copy & Share";
//    final public String share = "Share link";
//    final public String launch_app="Launch App";
//    final public String open_sysAppInfo="Open System AppInfo";
//    final public String kill_app="Kill App";
//    final public String open_market="Open in PlayStore";
//    final public String uninstall_app="Uninstall App";
//
//
//    int itemname;
//    PackageInfo pkg_info;
//    String applabel;
//    Intent i;
//    Context context;
//    private String dest_folder_name;
//    ObjectApkFile apkitem;
//    ClassApkOperation apkOperationObject;
//    boolean rootAccess;
//    public static final String key_root_access = FragmentSettings.key_root_access;
//    final String path_not_set = "PATH NOT SET";
//
//    static {
//        Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR);
//        Shell.Config.verboseLogging(BuildConfig.DEBUG);
//        //Shell.Config.setTimeout(10);
//    }
//
//    public AppMenu(int ClickedMenuItem,String applabel,PackageInfo pkginfo,Context context){
//        itemname = ClickedMenuItem;
//        this.pkg_info = pkginfo;
//        this.applabel= applabel;
//        this.context = context;
//        rootAccess = ActivityMain.sharedPrefSettings.getBoolean(key_root_access,false);
//        dest_folder_name = ActivityMain.sharedPrefApkManager.getString(FragmentApkFiles.key_local_path,path_not_set);
//        apkitem = new ObjectApkFile(new File(pkginfo.applicationInfo.sourceDir),context);
//        Log.i(TAG," Constructor : ");
//    }
//
//    public void PerAppMenu() {
//
//        switch(itemname){
//
//            case R.id.launch_item: {
//                Toast.makeText(context, "Launching " + applabel, Toast.LENGTH_SHORT).show();
//                i = mainpm.getLaunchIntentForPackage(pkg_info.packageName);
//                context.startActivity(i);
//                break;
//            }
//            case R.id.playstore_item: {
//                Toast.makeText(context, "Opening " + applabel + " in Playstore", Toast.LENGTH_SHORT).show();
//                i = new Intent();
//                i.setData(Uri.parse("market://details?id=" + pkg_info.packageName));
//                context.startActivity(i);
//                break;
//            }
//            case R.id.sys_appinfo_item: {
//                i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + pkg_info.packageName));
//                context.startActivity(i);
//                break;
//            }
//
//            case R.id.uninstall_item: {
//                Toast.makeText(context, "Confirm to uninstall " + applabel, Toast.LENGTH_SHORT).show();
//                if(!rootAccess){
//                    i = new Intent(Intent.ACTION_DELETE);
//                    i.setData(Uri.parse(pkg_info.packageName));
//                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    //i.putExtra(EXTRA_PACKAGE_NAME, pkg_info.packageName);
//                    context.startActivity(i);
//                }else{
//                    try{
//                        Log.i(TAG,"Uninstallation RootAccess :"+Shell.rootAccess());
//                        Shell.su("pm uninstall "+ pkg_info.packageName).exec();
//                        Log.i(TAG, pkg_info.applicationInfo.loadLabel(mainpm).toString()+" uninstalled Successfully.");
//                    }catch(Exception ex){
//                        Log.i(TAG,"Uninstallation Failed :"+ex);
//                    }
//                }
//                break;
//            }
//
//            case R.id.extractApk_item : {
//                Log.i(TAG,"Clicked Extract Apk");
//
//                File apk = new File(clicked_pkg.applicationInfo.sourceDir);
//                if(apk==null){
//                    Toast.makeText(context,"No Apk Available",Toast.LENGTH_SHORT);
//                }else{
//                    Log.i(TAG,"APk is not null");
//                    apkOperationObject = new ClassApkOperation(new ObjectApkFile(apk,context),context);
//                    apkOperationObject.extractApk();
//                }
//                Toast.makeText(context, "Extracting Apk " + applabel, Toast.LENGTH_SHORT).show();
//
//            }
//
//            case R.id.copy_appname_item: {
//            copyToClipboard("App Name", applabel);
//            Toast.makeText(context, "App Name Copied", Toast.LENGTH_SHORT).show();
//            break;
//            }
//
//            case R.id.copy_pkgname_item: {
//                copyToClipboard("Package Name", pkg_info.packageName);
//                Toast.makeText(context, "Package Name Copied", Toast.LENGTH_SHORT).show();
//                break;
//            }
//            case R.id.copy_link_item: {
//                copyToClipboard("Playstore Link", "market://details?id=" + pkg_info.packageName);
//                Toast.makeText(context, "Playstore link copied", Toast.LENGTH_SHORT).show();
//                break;
//            }
//            case R.id.share_link_item: {
//                Intent i = new Intent(Intent.ACTION_SEND);
//                i.setType("text/plain");
//                i.putExtra(Intent.EXTRA_TEXT, "market://details?id=" + pkg_info.packageName);
//                context.startActivity(createChooser(i, "Share Link Via"));
//                break;
//            }
//            case R.id.killapp_item: {
//                activityManager.killBackgroundProcesses(applabel);
//                Toast.makeText(context, "Stopping Background Tasks for " + applabel, Toast.LENGTH_SHORT).show();
//                break;
//            }
//
//            case R.id.clear_data_item: {
//                activityManager.killBackgroundProcesses(applabel);
//                Toast.makeText(context, "Cleared App Data of  " + applabel, Toast.LENGTH_SHORT).show();
//                break;
//            }
//
//            case R.id.disable_app_item: {
//                Toast.makeText(context, "Disable Apps " + applabel, Toast.LENGTH_SHORT).show();
//                activityManager.killBackgroundProcesses(applabel);
//                break;
//            }
//
//            default: {
//                Toast.makeText(context, "Unknown Item Click Detected " + applabel, Toast.LENGTH_SHORT).show();
//            }
//
//            }
//        }
//
//    private void copyToClipboard(String Title, String clip) {
//        ClipData clipData = ClipData.newPlainText(Title, clip);
//        clipboardManager.setPrimaryClip(clipData);
//    }
//
//}
//

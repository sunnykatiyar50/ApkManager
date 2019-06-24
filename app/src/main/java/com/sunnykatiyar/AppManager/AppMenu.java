package com.sunnykatiyar.AppManager;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.Toast;
import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import static android.content.Intent.EXTRA_PACKAGE_NAME;
import static android.content.Intent.createChooser;
import static com.sunnykatiyar.AppManager.ApkRenameActivity.name_part_1;
import static com.sunnykatiyar.AppManager.ApkRenameActivity.name_part_2;
import static com.sunnykatiyar.AppManager.ApkRenameActivity.name_part_3;
import static com.sunnykatiyar.AppManager.ApkRenameActivity.name_part_4;
import static com.sunnykatiyar.AppManager.ApkRenameActivity.name_part_5;
import static com.sunnykatiyar.AppManager.ApkRenameActivity.name_part_6;
import static com.sunnykatiyar.AppManager.ApkRenameActivity.name_part_7;
import static com.sunnykatiyar.AppManager.ApkRenameActivity.name_part_8;
import static com.sunnykatiyar.AppManager.AppListFragment.clipboardManager;
import static com.sunnykatiyar.AppManager.AppListFragment.mainpm;


/**
 * Created by Sunny Katiyar on 14-06-2017.
 */

public class AppMenu {

    final public String TAG = " APP_MENU_ACTIVITY : ";
    final public String copy_pname = "Copy Package Name";
    final public String copy_name ="Copy App Name";
    final public String copy_link = "Copy Playstore Link";
    final public String extract_apk = "Extract Apk";
    final public String copy = "Copy & Share";
    final public String share = "Share link";
    final public String launch_app="Launch App";
    final public String open_sysAppInfo="Open System AppInfo";
    final public String kill_app="Kill App";
    final public String open_market="Open in PlayStore";
    final public String uninstall_app="Uninstall App";

    public final static  String PREF_NAME = ApkRenameActivity.PREF_NAME;
    public final static String key_global_path = ApkRenameActivity.key_global_path;

    SharedPreferences sharedPref ;
    Snackbar snackbar;
    SharedPreferences.Editor prefEditor;
    int itemname;
    PackageInfo p;
    String applabel;
    Intent i;
    Context menu_context;
    String dest_folder_name;
    boolean root_selected;
    File source_apk;
    File dest_apk;
    String apk_source_path;

    static {
        Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR);
        Shell.Config.verboseLogging(BuildConfig.DEBUG);
        //Shell.Config.setTimeout(10);
    }

    public AppMenu(int ClickedMenuItem,String applabel,PackageInfo menu_pkginfo,Context context){
        itemname = ClickedMenuItem;
        p=menu_pkginfo;
        this.applabel=applabel;
        menu_context=context;
        sharedPref = context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);
        root_selected = sharedPref.getBoolean("ROOT",false);
        dest_folder_name = sharedPref.getString(ApkRenameActivity.key_global_path,"Path Not Set");
        Log.i(TAG," Constructor : ");
    }


    public void PerAppMenu() {
        switch(itemname){
            case R.id.launch_item: {
                Toast.makeText(menu_context, "Launching " + applabel, Toast.LENGTH_SHORT).show();
                i = mainpm.getLaunchIntentForPackage(p.packageName);
                menu_context.startActivity(i);
                break;
            }
            case R.id.playstore_item: {
                Toast.makeText(menu_context, "Opening " + applabel + " in Playstore", Toast.LENGTH_SHORT).show();
                i = new Intent();
                i.setData(Uri.parse("market://details?id=" + p.packageName));
                menu_context.startActivity(i);
                break;
            }
            case R.id.sys_appinfo_item: {
                i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + p.packageName));
                menu_context.startActivity(i);
                break;
            }

            case R.id.uninstall_item: {
                Toast.makeText(menu_context, "Confirm to uninstall " + applabel, Toast.LENGTH_SHORT).show();
                if(!root_selected){
                    i = new Intent(Intent.ACTION_DELETE);
                    //   i.setData(Uri.parse(p.packageName));
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.putExtra(EXTRA_PACKAGE_NAME, p.packageName);
                    menu_context.startActivity(i);
                }else{
                    List<String> oup =  new ArrayList<>();
                    try{
                        Log.i(TAG,"Uninstallation RootAccess :"+Shell.rootAccess());
                        Shell.su("pm uninstall "+p.packageName).to(oup).exec();
                        Log.i(TAG,p.applicationInfo.loadLabel(mainpm).toString()+" uninstalled Successfully.");
                    }catch(Exception ex){
                        Log.i(TAG,"Uninstallation Failed :"+ex);
                    }
                }
                break;
            }

            case R.id.extractApk_item : {
                Log.i(TAG,"Clicked Extract Apk");
                Toast.makeText(menu_context, "Extracting Apk " + applabel, Toast.LENGTH_SHORT).show();
                new ExtractApkTask().execute();
            }
             case R.id.copy_appname_item: {
            copyToClipboard("App Name", applabel);
            Toast.makeText(menu_context, "App Name Copied", Toast.LENGTH_SHORT).show();
            break;
            }

            case R.id.copy_pkgname_item: {
                copyToClipboard("Package Name", p.packageName);
                Toast.makeText(menu_context, "Package Name Copied", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.copy_link_item: {
                copyToClipboard("Playstore Link", "market://details?id=" + p.packageName);
                Toast.makeText(menu_context, "Playstore link copied", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.share_link_item: {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, "market://details?id=" + p.packageName);
                menu_context.startActivity(createChooser(i, "Share Link Via"));
                break;
            }
            /*      case kill_app: {
                Toast.makeText(menu_context, "Stopping Background Tasks for " + applabel, Toast.LENGTH_SHORT).show();
                activityManager.killBackgroundProcesses(applabel);
                break;
            }
 */

            }
        }

    public void copyToClipboard(String Title, String clip) {
        ClipData clipData = ClipData.newPlainText(Title, clip);
        clipboardManager.setPrimaryClip(clipData);
    }
    public void set_dest_apk(){
        Log.i(TAG,"in get_apk_name");

        int part1 = sharedPref.getInt(name_part_1,1);
        Log.i(TAG,"Part 1 :"+ part1);

        String part2 = sharedPref.getString(name_part_2,"_v");
        Log.i(TAG,"Part 2 :"+part2);

        int part3 =sharedPref.getInt(name_part_3,2);
        Log.i(TAG,"Part 3 :"+ part3);

        String part4 = sharedPref.getString(name_part_4,"_");
        Log.i(TAG,"Part 4 :"+part4);

        int part5 =sharedPref.getInt(name_part_5,3);
        Log.i(TAG,"Part 5 :"+ part5);

        String part6 = sharedPref.getString(name_part_6,"");
        Log.i(TAG,"Part 6 :"+part6);

        int part7 =sharedPref.getInt(name_part_7,0);
        Log.i(TAG,"Part 7 :"+ part7);

        String part8 = sharedPref.getString(name_part_8,"");
        Log.i(TAG,"Part 8 :"+part8);

        this.dest_apk = new File(this.dest_folder_name+"/"+getName(part1)+part2+getName(part3)+part4+getName(part5)+part6+getName(part7)+part8+".apk");

    }

    public void set_source_apk(){
        apk_source_path = p.applicationInfo.sourceDir;
        this.source_apk = new File(apk_source_path);
    }

    protected String getName(int i){
        switch(i){
            case 0:{  return "";              }
            case 1:{  return applabel;     }
            case 2:{  return this.p.versionName;    }
            case 3:{  return String.valueOf(this.p.versionCode);  }
            case 4:{  return p.applicationInfo.processName;  }
            case 5:{  return this.p.packageName; }
            default : { return "";             }
        }
    }

    public class ExtractApkTask extends AsyncTask<Void,String,String>{

        public ExtractApkTask() {
            super();
        }

        @Override
        protected void onPreExecute() {
            Log.i(TAG," OnPreExecute : ");
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            Toast.makeText(menu_context, values[0], Toast.LENGTH_SHORT).show();
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            Log.i(TAG," OnPostExecute : ");
            super.onPostExecute(s);
        }

        @Override
        protected String doInBackground(Void... voids) {
            ExtractApk();
            return null;
        }

        public void ExtractApk(){
            Log.i(TAG," In ExtractAPK() :");
            set_dest_apk();
            set_source_apk();
            List<String> oup = new ArrayList<>();
            List<String> err = new ArrayList<>();
            String command;
            if(new File((dest_folder_name)).exists())
            {  Log.i(TAG,"ExtractApk() : dest_folder_set");
                if(root_selected)
                {
                    command = "cp \""+source_apk+"\" \""+dest_apk+"\"";
                    Log.i(TAG," Command :"+command);
                    try{
                        Log.i(TAG,"Root Access :"+Shell.rootAccess());
//                      Shell.su("ls").to(err).exec();
                        Shell.su(command).to(oup,err).exec();
                         Log.i(TAG,applabel+" Apk Extracted Successfully.");
                         publishProgress(applabel+" Apk Extracted Successfully.");
                    }catch(Exception ex){
                        Log.i(TAG,"Extracting failed Error : "+ex);
                        publishProgress(applabel+" Apk Extraction Failed.");
                    }

                }else{
                            Log.i(TAG,"ExtractApk() : No Root Method");
                            if(source_apk.exists() & source_apk.isFile()){
                                Log.i(TAG," source file path : "+source_apk.getAbsolutePath());
                                copyFileTo(source_apk,dest_apk);
                                //  result = (file).renameTo(f2);
                                //Log.i(TAG,"Renaming "+f.file_name+" : \""+f2+"\" - "+result);
                    }else{
                        Log.i(TAG," source apk does not exist or is not file : ");
                    }
                }
            }else{ Toast.makeText(menu_context,"Set a valid path in ApkRename Settings to Extract Apk",Toast.LENGTH_SHORT); }
        }


        public  boolean copyFileTo(FileInputStream originFile, File destinationFile) {
            boolean exportDone = true;
            try {/*from ww  w .  ja v a 2  s .  c  o  m*/
                File sd = Environment.getExternalStorageDirectory();
                if (sd.canWrite()) {
                    // Create parent directories
                    destinationFile.getParentFile().mkdirs();

                    FileChannel src = originFile.getChannel();
                    FileChannel dst = new FileOutputStream(destinationFile).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            } catch (Exception e) {
                exportDone = false;
                Log.i(TAG," Apk Extraction Done: "+exportDone);
            }
            publishProgress(" Apk Extraction Done: "+exportDone);
            Log.i(TAG," Apk Extraction Done: "+exportDone);
            return exportDone;
        }

        public boolean copyFileTo(File originFile, File destinationFile) {
            try {
                return copyFileTo(new FileInputStream(originFile), destinationFile);
            } catch (FileNotFoundException e) {
                return false;
            }
        }

    }

}


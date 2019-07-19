package com.sunnykatiyar.appmanager;

import android.app.Activity;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.google.android.material.navigation.NavigationView;
import com.topjohnwu.superuser.Shell;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;

public class ActivityMain extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MYAPP : APPLIST ACTIVITY : ";

    public static Toolbar toolbar_main;

    public static MenuItem search_menuItem;
    public static NotificationManager notimgr ;
    public static Context context;
    public static ContentResolver resolver;
    //////SHARED PREFERENCES APPLIST
    Activity activity = ActivityMain.this;

    public static SharedPreferences sharedPrefAppSettings;
    public static SharedPreferences.Editor prefEditAppSettings;

    public static SharedPreferences sharedPrefSettings;
    public static SharedPreferences.Editor prefEditSettings;

    public static SharedPreferences sharedPrefApkManager;
    public static SharedPreferences.Editor prefEditApkManager;

    public static SharedPreferences sharedPrefRepository;
    public static SharedPreferences.Editor prefEditRepository;

    public static SharedPreferences sharedPrefFileSettings;
    public static SharedPreferences.Editor prefEditFileSettings;

    public static SharedPreferences sharedPrefExternalStorages;
    public static SharedPreferences.Editor prefEditExternalStorages;

    NavigationView navigationView;

    public static FragmentManager fm;
    public static FragmentTransaction ft;

    final String TAG_APK_LIST  = "apk_list_activity";
    final String TAG_APP_LIST  = "app_list_activity";
    final String TAG_APP_SETTINGS  = "app_settings_activity";
    final String TAG_FILE_RENAMER  = "file_renamer_activity";
    final String TAG_FILE_RENAMER_FORMAT  = "file_renamer_format_activity";
    final String TAG_APK_RENAME_FORMAT  = "apk_renamer_format_activity";

    final String key_last_fragment = "LAST_FRAGMENT_ID";
    public static final String key_export_apk_enable = FragmentSettings.key_export_apk_enable;
    public static final String key_export_apk_path = FragmentSettings.key_export_apk_uri;
    public String value_export_apk_path;
    public boolean value_export_apk_enable;

    int CURRENT_FRAGMENT = 0;
    public static FragmentAppManager fragmentAppManager;
    FragmentFileSettings fragmentFileSettings;
    FragmentApkSettings fragmentApkSettings;
    FragmentSettings fragmentSettings;
    FragmentApkFiles fragmentApkFiles;
    FragmentAbout aboutFragment;
    FragmentHelp fragmentHelp;
    FragmentFileManager fragmentFileManager;
    ActivityOperations activityOperations;
    ServiceSetup myService;
    public static Intent serviceIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_list);

        toolbar_main = findViewById(R.id.toolbar_applist);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        setSupportActionBar(toolbar_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ActionBarDrawerToggle toggle =
                new ActionBarDrawerToggle(this, drawer, toolbar_main, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        
        activityOperations = ActivityOperations.getInstanceOf();
        context = getApplicationContext();
        resolver = getContentResolver();
        navigationView = findViewById(R.id.nav_view_applist);
        navigationView.setNavigationItemSelectedListener(this);

        if(Shell.rootAccess()){
            navigationView.getMenu().findItem(R.id.nav_root_browser).setVisible(true);
        }else{
            navigationView.getMenu().findItem(R.id.nav_root_browser).setVisible(false);
        }

        sharedPrefAppSettings = getSharedPreferences(context.getResources().getString(R.string.sharedPref_appsSettings),MODE_PRIVATE);
        prefEditAppSettings = sharedPrefAppSettings.edit();

        sharedPrefSettings = getSharedPreferences(context.getResources().getString(R.string.sharedPref_settings),MODE_PRIVATE);
        prefEditSettings = sharedPrefSettings.edit();

        sharedPrefApkManager = getSharedPreferences(context.getResources().getString(R.string.sharedPref_apkSettings),MODE_PRIVATE);
        prefEditApkManager = sharedPrefApkManager.edit();

        sharedPrefRepository = getSharedPreferences(context.getResources().getString(R.string.sharedPref_settings),MODE_PRIVATE);
        prefEditRepository = sharedPrefRepository.edit();

        sharedPrefFileSettings = getSharedPreferences(context.getResources().getString(R.string.sharedPref_filesSettings),MODE_PRIVATE);
        prefEditFileSettings = sharedPrefFileSettings.edit();

        sharedPrefExternalStorages = getSharedPreferences(context.getResources().getString(R.string.sharedPref_externalPaths),MODE_PRIVATE);
        prefEditExternalStorages = sharedPrefExternalStorages.edit();

        fragmentAppManager = new FragmentAppManager();
        fragmentSettings = new FragmentSettings();
        fragmentFileSettings = new FragmentFileSettings();
        fragmentApkFiles = new FragmentApkFiles();
        fragmentApkSettings = new FragmentApkSettings();
        aboutFragment = new FragmentAbout();
        fragmentHelp = new FragmentHelp();
        fragmentFileManager = new FragmentFileManager();

        if(sharedPrefSettings.getBoolean(key_export_apk_enable,false)){
            serviceIntent =  new Intent(this, ServiceSetup.class);
            startForegroundService(serviceIntent);
        }else{
            if(ServiceSetup.service_running_count>0){
                Log.i(TAG,"Service Running Already");
            }
        }



        fm = this.getSupportFragmentManager();

        CURRENT_FRAGMENT = sharedPrefSettings.getInt(key_last_fragment,0);
        setFragment(CURRENT_FRAGMENT);
        if(navigationView.getMenu().findItem(CURRENT_FRAGMENT).getTitle()==null){
            Log.e(TAG,"CURRENT_FRAGMENT VALUE = "+"the App is Opened first Time");
        }else{
            Log.e(TAG,"CURRENT_FRAGMENT VALUE = "+navigationView.getMenu().findItem(CURRENT_FRAGMENT).getTitle());
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        setFragment(item.getItemId());
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        CURRENT_FRAGMENT = sharedPrefSettings.getInt(key_last_fragment,0);
        setFragment(CURRENT_FRAGMENT);
    }

    private void setFragment(int id){

        switch(id){
            case R.id.nav_apklist:{
                Log.e(TAG,"NavBar ApkList Fragment ");
                ft = fm.beginTransaction();
                ft.replace(R.id.fragment_container, fragmentApkFiles);
                ft.addToBackStack(null);
                ft.commit();
                prefEditSettings.putInt(key_last_fragment,id).commit();
                navigationView.getMenu().findItem(id).setChecked(true);
                toolbar_main.setTitle(R.string.apk_manager_activity_name);
                Log.i(TAG,": onresume : "+navigationView.getMenu().findItem(id).getTitle());
                break;
            }

            case R.id.nav_applist:{
                Log.e(TAG,"NavBar App List ");
                ft = fm.beginTransaction();
                ft.replace(R.id.fragment_container, fragmentAppManager);
                ft.addToBackStack(null);
                ft.commit();
                prefEditSettings.putInt(key_last_fragment,id).commit();
                navigationView.getMenu().findItem(id).setChecked(true);
                toolbar_main.setTitle(R.string.app_explorer_activity_name);
                Log.i(TAG,": onresume : "+navigationView.getMenu().findItem(id).getTitle());
                break;
            }

            case R.id.nav_app_settings:{
                //  Log.e(TAG,"NavBar App Settings ");
                ft = fm.beginTransaction();
                ft.replace(R.id.fragment_container, fragmentSettings);
                ft.addToBackStack(null);
                ft.commit();
                prefEditSettings.putInt(key_last_fragment,id).commit();
                toolbar_main.setTitle(R.string.app_settings_fragment_name);
                navigationView.getMenu().findItem(id).setChecked(true);
                Log.i(TAG,": onresume : "+navigationView.getMenu().findItem(id).getTitle());
                break;
            }

            case R.id.nav_apk_rename_format:{
                // Log.e(TAG,"NavBar Rename APK fragment begin ");
                ft = fm.beginTransaction();
                ft.replace(R.id.fragment_container, fragmentApkSettings);
                ft.addToBackStack(null);
                ft.commit();
                prefEditSettings.putInt(key_last_fragment,id).commit();
                navigationView.getMenu().findItem(id).setChecked(true);
                toolbar_main.setTitle(R.string.apk_renamer_fragment_name);
                Log.i(TAG,": onresume : "+navigationView.getMenu().findItem(id).getTitle());
                break;
            }

            case R.id.nav_file_list:{
                ft = fm.beginTransaction();
                ft.replace(R.id.fragment_container, fragmentFileManager);
                ft.addToBackStack(null);
                ft.commit();
                prefEditSettings.putInt(key_last_fragment,id).commit();
                navigationView.getMenu().findItem(id).setChecked(true);
                toolbar_main.setTitle(R.string.file_list_fragment_name);
                Log.i(TAG,": onresume : "+navigationView.getMenu().findItem(id).getTitle());
                break;
            }

            case R.id.nav_file_rename_format:{
                //Log.e(TAG,"NavBar Rename Files fragment begin ");
                ft = fm.beginTransaction();
                ft.replace(R.id.fragment_container, fragmentFileSettings);
                ft.addToBackStack(null);
                ft.commit();
                prefEditSettings.putInt(key_last_fragment,id).commit();
                navigationView.getMenu().findItem(id).setChecked(true);
                toolbar_main.setTitle(R.string.file_format_fragment_name);
                Log.i(TAG,": onresume : "+navigationView.getMenu().findItem(id).getTitle());
                break;
            }
            case R.id.nav_about : {
                ft = fm.beginTransaction();
                ft.replace(R.id.fragment_container, aboutFragment);
                ft.addToBackStack(null);
                ft.commit();
                prefEditSettings.putInt(key_last_fragment,id).commit();
                navigationView.getMenu().findItem(id).setChecked(true);
                toolbar_main.setTitle(R.string.about_fragment_name);
                Log.i(TAG,": onresume : "+navigationView.getMenu().findItem(id).getTitle());
                break;
            }
            case R.id.nav_help: {
                ft = fm.beginTransaction();
                ft.replace(R.id.fragment_container, fragmentHelp);
                ft.addToBackStack(null);
                ft.commit();
                prefEditSettings.putInt(key_last_fragment,id).commit();
                navigationView.getMenu().findItem(id).setChecked(true);
                toolbar_main.setTitle(R.string.help_fragment_name);
                Log.i(TAG,": onresume : "+navigationView.getMenu().findItem(id).getTitle());
                break;
            }

            case R.id.nav_operations: {
                Intent i =  new Intent(this, ActivityOperations.class);
                startActivity(i);
              //  prefEditSettings.putInt(key_last_fragment,id).commit();
              //  navigationView.getMenu().findItem(id).setChecked(true);
                Log.i(TAG,": onresume : OPERATIONS ACTIVITY");
                break;
            }

            case R.id.nav_root_browser: {
                Intent i =  new Intent(this, ActivityRootBrowser.class);
                startActivity(i);
              //  prefEditSettings.putInt(key_last_fragment,id).commit();
              //  navigationView.getMenu().findItem(id).setChecked(true);
                Log.i(TAG,": onresume : ROOTBROWSER ACTIVITY");
                break;
            }

            default:{
                Log.i(TAG,": onresume : DEFAULT");
                ft = fm.beginTransaction();
                ft.replace(R.id.fragment_container, fragmentAppManager);
                ft.addToBackStack(null);
                ft.commit();
                prefEditSettings.putInt(key_last_fragment,R.id.nav_applist).commit();
                navigationView.getMenu().findItem(R.id.nav_applist).setChecked(true);
                Log.i(TAG,": onresume : "+navigationView.getMenu().findItem(R.id.nav_applist).getTitle());
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(fm.getBackStackEntryCount()>0)
            fm.popBackStack();
        else
          super.onBackPressed();
    }
    
}

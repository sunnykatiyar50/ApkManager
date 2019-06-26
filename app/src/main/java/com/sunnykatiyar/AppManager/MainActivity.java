package com.sunnykatiyar.AppManager;

import android.app.Activity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "APPLIST ACTIVITY : ";

    public static Toolbar toolbar_main;

    public static MenuItem search_menuItem;
    public static NotificationManager notimgr ;

    //////SHARED PREFERENCES APPLIST
    Activity activity = MainActivity.this;

    final String PREF_NAME_APPLIST = "com.sunnykatiyar.AppManager.APPLIST";
    public static SharedPreferences sharedPrefAppList;
    public static SharedPreferences.Editor prefEditorAppList;

    final String PREF_NAME_APP_SETTINGS = "com.sunnykatiyar.AppManager.APP_SETTINGS";
    public static SharedPreferences sharedPrefAppSettings;
    public static SharedPreferences.Editor prefEditAppSettings;

    final String PREF_NAME_APKMANAGER = "com.sunnykatiyar.AppManager.RENAME";
    public static SharedPreferences sharedPrefApkManager;
    public static SharedPreferences.Editor prefEditorApkManager;

    final String PREF_NAME_REPOSITORY = "com.sunnykatiyar.AppManager.MOVETOFOLDERS";
    public static SharedPreferences sharedPrefRepository;
    public static SharedPreferences.Editor prefEditRepository;

    NavigationView navigationView;

    public static FragmentManager fm;
    public static FragmentTransaction ft;

    final String TAG_APK_LIST  = "apk_list_activity";
    final String TAG_APP_LIST  = "app_list_activity";
    final String TAG_APP_SETTINGS  = "app_settings_activity";
    final String TAG_FILE_RENAMER  = "file_renamer_activity";
    final String TAG_FILE_RENAMER_FORMAT  = "file_renamer_format_activity";
    final String TAG_APK_RENAME_FORMAT  = "apk_renamer_format_activity";

    int CURRENT_FRAGMENT = 0;
    public static AppListFragment appListFragment;
    RenameFilesFragment renameFilesFragment;
    RenameApkFragment renameApkFragment;
    AppSettingsFragment appSettingsFragment;
    ApkListFragment apkListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_list);

        toolbar_main = findViewById(R.id.toolbar_applist);
        toolbar_main.setTitle(R.string.app_explorer_activity_name);
        setSupportActionBar(toolbar_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar_main, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view_applist);
        navigationView.setNavigationItemSelectedListener(this);

//        notimgr = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);

        sharedPrefAppList = getSharedPreferences(PREF_NAME_APPLIST,MODE_PRIVATE);
        prefEditorAppList = sharedPrefAppList.edit();

        sharedPrefAppSettings = getSharedPreferences(PREF_NAME_APP_SETTINGS,MODE_PRIVATE);
        prefEditAppSettings = sharedPrefAppSettings.edit();

        sharedPrefApkManager = getSharedPreferences(PREF_NAME_APKMANAGER,MODE_PRIVATE);
        prefEditorApkManager = sharedPrefApkManager.edit();

        sharedPrefRepository = getSharedPreferences(PREF_NAME_REPOSITORY,MODE_PRIVATE);
        prefEditRepository = sharedPrefRepository.edit();

        appListFragment = new AppListFragment();
        appSettingsFragment = new AppSettingsFragment();
        renameFilesFragment = new RenameFilesFragment();
        apkListFragment = new ApkListFragment();
        renameApkFragment = new RenameApkFragment();

        fm = this.getSupportFragmentManager();

        Log.e(TAG,"CURRENT_FRAGMENT VALUE = "+CURRENT_FRAGMENT);

        if(this.CURRENT_FRAGMENT == 0){
            this.CURRENT_FRAGMENT = R.id.nav_applist;
            Log.e(TAG,"CURRENT_FRAGMENT VALUE = "+navigationView.getMenu().findItem(CURRENT_FRAGMENT).getTitle());
        }

        Log.e(TAG,"CURRENT_FRAGMENT VALUE = "+navigationView.getMenu().findItem(CURRENT_FRAGMENT).getTitle());
        navigationView.getMenu().findItem(CURRENT_FRAGMENT).setChecked(true);
        setFragment(CURRENT_FRAGMENT);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        setFragment(item.getItemId());

  //      int id = item.getItemId();


//        if (id == R.id.nav_apk_rename_format) {
//            Log.e(TAG,"NavBar Rename APK fragment begin ");
//            ft = fm.beginTransaction();
//            ft.replace(R.id.fragment_container, renameApkFragment);
//            ft.addToBackStack(null);
//            ft.commit();
//            CURRENT_FRAGMENT = id;
//            Log.i(TAG,": onNavItem Selected"+id);
//
//        } else if (id == R.id.nav_apklist) {
//            Log.e(TAG,"NavBar ApkList Fragment ");
//
//            ft = fm.beginTransaction();
//            ft.replace(R.id.fragment_container, apkListFragment);
//            ft.addToBackStack(null);
//            ft.commit();
//            Log.i(TAG,": onNavItem Selected"+id);
//            CURRENT_FRAGMENT = id;
//            item.setChecked(true);
//
//        } else if (id == R.id.nav_file_renamer) {
//            Log.e(TAG,"NavBar Rename Files fragment begin ");
//            ft = fm.beginTransaction();
//            ft.replace(R.id.fragment_container, renameFilesFragment);
//            ft.addToBackStack(null);
//            ft.commit();
//            CURRENT_FRAGMENT = id;
//            Log.i(TAG,": onNavItem Selected"+id);
//
//
//        } else if (id == R.id.nav_applist) {
//            Log.e(TAG,"NavBar App List ");
//            ft = fm.beginTransaction();
//            ft.replace(R.id.fragment_container, appListFragment);
//            ft.addToBackStack(null);
//            ft.commit();
//            CURRENT_FRAGMENT = id;
//            item.setChecked(true);
//            Log.i(TAG,": onNavItem Selected"+id);
//
//        } else if (id == R.id.nav_app_settings) {
//            Log.e(TAG,"NavBar App Settings ");
//            ft = fm.beginTransaction();
//            ft.replace(R.id.fragment_container, appSettingsFragment);
//            ft.addToBackStack(null);
//            ft.commit();
//            Log.i(TAG,": onNavItem Selected"+id);
//            CURRENT_FRAGMENT = id;
//
//        }
//        else if (id == R.id.nav_send) {
//            CURRENT_FRAGMENT = id;
//            Log.i(TAG,": onNavItem Selected"+id);
//
//        }
//        else  if (id == R.id.nav_about) {
//            Toast.makeText(getApplication(),"About Selected", Toast.LENGTH_SHORT).show();
//            startActivity(new Intent(this,About.class));
//                      CURRENT_FRAGMENT = id;
//            Log.i(TAG,": onNavItem Selected"+id);
//
////          Toast.makeText(context, "Don't be so curious about me. I am in the making.. ", Toast.LENGTH_LONG).show();
//        }
//        else if (id == R.id.nav_help) {
//            CURRENT_FRAGMENT = id;
//            Log.i(TAG,": onNavItem Selected"+id);
//            Toast.makeText(activity, " If you can't help yourself.. Nobody can.", Toast.LENGTH_LONG).show();
//        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setFragment(CURRENT_FRAGMENT);
    }

    private void setFragment(int id){

        Log.e(TAG,"CURRENT_FRAGMENT VALUE = "+navigationView.getMenu().findItem(id).getTitle());

        switch(id){
            case R.id.nav_apklist:{
                Log.e(TAG,"NavBar ApkList Fragment ");
                ft = fm.beginTransaction();
                ft.replace(R.id.fragment_container, apkListFragment);
                ft.addToBackStack(null);
                ft.commit();
                Log.e(TAG,"NavBar ApkList Fragment End");
                CURRENT_FRAGMENT = id;
                navigationView.getMenu().findItem(R.id.nav_apklist).setChecked(true);
                Log.i(TAG,": onresume : "+navigationView.getMenu().findItem(R.id.nav_apklist).getTitle());
                break;
            }

            case R.id.nav_applist:{
                Log.e(TAG,"NavBar App List ");
                ft = fm.beginTransaction();
                ft.replace(R.id.fragment_container, appListFragment);
                ft.addToBackStack(null);
                ft.commit();
                CURRENT_FRAGMENT = id;
                navigationView.getMenu().findItem(R.id.nav_applist).setChecked(true);
                Log.i(TAG,": onresume : "+navigationView.getMenu().findItem(R.id.nav_apklist).getTitle());
                break;
            }

            case R.id.nav_app_settings:{
                //  Log.e(TAG,"NavBar App Settings ");
                ft = fm.beginTransaction();
                ft.replace(R.id.fragment_container, appSettingsFragment);
                ft.addToBackStack(null);
                ft.commit();
                CURRENT_FRAGMENT = id;
                Log.i(TAG,": onresume : "+navigationView.getMenu().findItem(id).getTitle());
                break;
            }

            case R.id.nav_apk_rename_format:{
                // Log.e(TAG,"NavBar Rename APK fragment begin ");
                ft = fm.beginTransaction();
                ft.replace(R.id.fragment_container, renameApkFragment);
                ft.addToBackStack(null);
                ft.commit();
                CURRENT_FRAGMENT = id;
                Log.i(TAG,": onresume : "+navigationView.getMenu().findItem(id).getTitle());
                break;
            }

            case R.id.nav_file_renamer:{
                CURRENT_FRAGMENT = id;
                Log.i(TAG,": onresume : "+navigationView.getMenu().findItem(id).getTitle());

                break;
            }

            case R.id.nav_file_rename_format:{
                //Log.e(TAG,"NavBar Rename Files fragment begin ");
                ft = fm.beginTransaction();
                ft.replace(R.id.fragment_container, renameFilesFragment);
                ft.addToBackStack(null);
                ft.commit();
                CURRENT_FRAGMENT = id;
                Log.i(TAG,": onresume : "+navigationView.getMenu().findItem(id).getTitle());
                break;
            }
            case R.id.nav_about : {
                Toast.makeText(getApplication(),"About Selected", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this,About.class));
                CURRENT_FRAGMENT = id;
                Log.i(TAG,": onresume : "+navigationView.getMenu().findItem(id).getTitle());
                break;
            }
            case R.id.nav_help: {
                CURRENT_FRAGMENT = id;
                Toast.makeText(activity, " If you can't help yourself.. Nobody can.", Toast.LENGTH_LONG).show();
                Log.i(TAG,": onresume : "+navigationView.getMenu().findItem(id).getTitle());
                break;
            }

            default:{
                Log.i(TAG,": onresume : DEFAULT");
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(fm.getBackStackEntryCount()>0)
            fm.popBackStack();
      else super.onBackPressed();
    }

}

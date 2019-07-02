package com.sunnykatiyar.AppManager;

import android.app.Activity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;

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

    final String key_last_fragment = "LAST_FRAGMENT_ID";

    int CURRENT_FRAGMENT = 0;
    public static AppListFragment appListFragment;
    RenameFilesFragment renameFilesFragment;
    RenameApkFragment renameApkFragment;
    AppSettingsFragment appSettingsFragment;
    ApkListFragment apkListFragment;
    AboutFragment aboutFragment;
    HelpFragment helpFragment;
    FilesListFragment filesListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_list);

        toolbar_main = findViewById(R.id.toolbar_applist);
        setSupportActionBar(toolbar_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar_main, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view_applist);
        navigationView.setNavigationItemSelectedListener(this);

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
        aboutFragment = new AboutFragment();
        helpFragment = new HelpFragment();
        filesListFragment = new FilesListFragment();

        fm = this.getSupportFragmentManager();

        CURRENT_FRAGMENT = sharedPrefAppSettings.getInt(key_last_fragment,0);
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
        CURRENT_FRAGMENT = sharedPrefAppSettings.getInt(key_last_fragment,0);
        setFragment(CURRENT_FRAGMENT);
    }

    private void setFragment(int id){

        switch(id){
            case R.id.nav_apklist:{
                Log.e(TAG,"NavBar ApkList Fragment ");
                ft = fm.beginTransaction();
                ft.replace(R.id.fragment_container, apkListFragment);
                ft.addToBackStack(null);
                ft.commit();
                prefEditAppSettings.putInt(key_last_fragment,id).commit();
                navigationView.getMenu().findItem(id).setChecked(true);
                toolbar_main.setTitle(R.string.apk_manager_activity_name);
                Log.i(TAG,": onresume : "+navigationView.getMenu().findItem(id).getTitle());
                break;
            }

            case R.id.nav_applist:{
                Log.e(TAG,"NavBar App List ");
                ft = fm.beginTransaction();
                ft.replace(R.id.fragment_container, appListFragment);
                ft.addToBackStack(null);
                ft.commit();
                prefEditAppSettings.putInt(key_last_fragment,id).commit();
                navigationView.getMenu().findItem(id).setChecked(true);
                toolbar_main.setTitle(R.string.app_explorer_activity_name);
                Log.i(TAG,": onresume : "+navigationView.getMenu().findItem(id).getTitle());
                break;
            }

            case R.id.nav_app_settings:{
                //  Log.e(TAG,"NavBar App Settings ");
                ft = fm.beginTransaction();
                ft.replace(R.id.fragment_container, appSettingsFragment);
                ft.addToBackStack(null);
                ft.commit();
                prefEditAppSettings.putInt(key_last_fragment,id).commit();
                toolbar_main.setTitle(R.string.app_settings_fragment_name);
                navigationView.getMenu().findItem(id).setChecked(true);
                Log.i(TAG,": onresume : "+navigationView.getMenu().findItem(id).getTitle());
                break;
            }

            case R.id.nav_apk_rename_format:{
                // Log.e(TAG,"NavBar Rename APK fragment begin ");
                ft = fm.beginTransaction();
                ft.replace(R.id.fragment_container, renameApkFragment);
                ft.addToBackStack(null);
                ft.commit();
                prefEditAppSettings.putInt(key_last_fragment,id).commit();
                navigationView.getMenu().findItem(id).setChecked(true);
                toolbar_main.setTitle(R.string.apk_renamer_fragment_name);
                Log.i(TAG,": onresume : "+navigationView.getMenu().findItem(id).getTitle());
                break;
            }

            case R.id.nav_file_list:{
                ft = fm.beginTransaction();
                ft.replace(R.id.fragment_container, filesListFragment);
                ft.addToBackStack(null);
                ft.commit();
                prefEditAppSettings.putInt(key_last_fragment,id).commit();
                navigationView.getMenu().findItem(id).setChecked(true);
                toolbar_main.setTitle(R.string.file_list_fragment_name);
                Log.i(TAG,": onresume : "+navigationView.getMenu().findItem(id).getTitle());
                break;
            }

            case R.id.nav_file_rename_format:{
                //Log.e(TAG,"NavBar Rename Files fragment begin ");
                ft = fm.beginTransaction();
                ft.replace(R.id.fragment_container, renameFilesFragment);
                ft.addToBackStack(null);
                ft.commit();
                prefEditAppSettings.putInt(key_last_fragment,id).commit();
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
                prefEditAppSettings.putInt(key_last_fragment,id).commit();
                navigationView.getMenu().findItem(id).setChecked(true);
                toolbar_main.setTitle(R.string.about_fragment_name);
                Log.i(TAG,": onresume : "+navigationView.getMenu().findItem(id).getTitle());
                break;
            }
            case R.id.nav_help: {
                ft = fm.beginTransaction();
                ft.replace(R.id.fragment_container, helpFragment);
                ft.addToBackStack(null);
                ft.commit();
                prefEditAppSettings.putInt(key_last_fragment,id).commit();
                navigationView.getMenu().findItem(id).setChecked(true);
                toolbar_main.setTitle(R.string.help_fragment_name);
                Log.i(TAG,": onresume : "+navigationView.getMenu().findItem(id).getTitle());                break;
            }

            default:{
                Log.i(TAG,": onresume : DEFAULT");
                ft = fm.beginTransaction();
                ft.replace(R.id.fragment_container, appListFragment);
                ft.addToBackStack(null);
                ft.commit();
                prefEditAppSettings.putInt(key_last_fragment,R.id.nav_applist).commit();
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
      else super.onBackPressed();
    }

}

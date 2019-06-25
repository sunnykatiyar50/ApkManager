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

public class AppListActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "APPLIST ACTIVITY : ";

    public static Toolbar toolbar_main;

    public static MenuItem search_menuItem;
    public static NotificationManager notimgr ;

    //////SHARED PREFERENCES APPLIST
    Activity activity = AppListActivity.this;

    final String PREF_NAME_APPLIST = "com.sunnykatiyar.AppManager.APPLIST";
    public static SharedPreferences sharedPrefAppList;
    public static SharedPreferences.Editor prefEditorAppList;

    final String PREF_NAME_SETTINGS = "com.sunnykatiyar.AppManager.APP_SETTINGS";
    public static SharedPreferences sharedPrefSettings;
    public static SharedPreferences.Editor prefEditSettings;

    final String PREF_NAME = "com.sunnykatiyar.AppManager.RENAME";
    public static SharedPreferences sharedPref;
    public static SharedPreferences.Editor prefEditor;

    NavigationView navigationView;

    public static FragmentManager fm;
    public static FragmentTransaction ft;

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
        navigationView.getMenu().findItem(R.id.nav_applist).setChecked(true);


        appListFragment = new AppListFragment();
        appSettingsFragment = new AppSettingsFragment();
        renameFilesFragment = new RenameFilesFragment();
        apkListFragment = new ApkListFragment();
        renameApkFragment = new RenameApkFragment();
        fm = this.getSupportFragmentManager();

//        notimgr = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);

        sharedPrefAppList = getSharedPreferences(PREF_NAME_APPLIST,MODE_PRIVATE);
        prefEditorAppList = sharedPrefAppList.edit();

        sharedPrefSettings = getSharedPreferences(PREF_NAME_SETTINGS,MODE_PRIVATE);
        prefEditSettings = sharedPrefSettings.edit();

        sharedPref = getSharedPreferences(PREF_NAME,MODE_PRIVATE);
        prefEditor =sharedPref.edit();

        Log.e(TAG,"in onCreate : Before transaction ");
            ft = fm.beginTransaction();
            ft.add(R.id.fragment_container, appListFragment);
            ft.commit();
        Log.e(TAG,"in onCreate : transaction committed ");
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_apk_rename_format) {
            Log.e(TAG,"NavBar Rename APK fragment begin ");
            ft = fm.beginTransaction();
            ft.replace(R.id.fragment_container, renameApkFragment);
            ft.addToBackStack(null);
            ft.commit();
            Log.e(TAG,"NavBar Rename APK Fragment End");

        } else if (id == R.id.nav_apklist) {
//            Intent i = new Intent(this, ApkListActivity.class);
//            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(i);
            Log.e(TAG,"NavBar ApkList Fragment ");
            ft = fm.beginTransaction();
            ft.replace(R.id.fragment_container, apkListFragment);
            ft.addToBackStack(null);
            ft.commit();
            Log.e(TAG,"NavBar ApkList Fragment End");

            item.setChecked(true);

        } else if (id == R.id.nav_file_renamer) {
            Log.e(TAG,"NavBar Rename Files fragment begin ");
            ft = fm.beginTransaction();
            ft.replace(R.id.fragment_container, renameFilesFragment);
            ft.addToBackStack(null);
            ft.commit();
            Log.e(TAG,"NavBar Rename Files Fragment End");


        } else if (id == R.id.nav_applist) {
            Log.e(TAG,"NavBar App List ");
            ft = fm.beginTransaction();
            ft.replace(R.id.fragment_container, appListFragment);
            ft.addToBackStack(null);
            ft.commit();
            item.setChecked(true);
            Log.e(TAG,"NavBar AppListEnd");

        } else if (id == R.id.nav_app_settings) {
            Log.e(TAG,"NavBar App Settings ");
            ft = fm.beginTransaction();
            ft.replace(R.id.fragment_container, appSettingsFragment);
            ft.addToBackStack(null);
            ft.commit();
            Log.e(TAG,"NavBar App Settings End");
        }
        else if (id == R.id.nav_send) {

        }
        else  if (id == R.id.nav_about) {
            Toast.makeText(getApplication(),"About Selected", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this,About.class));
//            Toast.makeText(context, "Don't be so curious about me. I am in the making.. ", Toast.LENGTH_LONG).show();
        }
        else if (id == R.id.nav_help) {
            Toast.makeText(activity, " If you can't help yourself.. Nobody can.", Toast.LENGTH_LONG).show();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        appListFragment = new AppListFragment();
        super.onResume();
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
    }

    @Override
    public void onBackPressed() {
        if(fm.getBackStackEntryCount()>0)
            fm.popBackStack();
      else super.onBackPressed();
    }

}

package com.sunnykatiyar.AppManager;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.AppCompatActivity;

import static com.sunnykatiyar.AppManager.AppListFragment.adapter;


public class AppListActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "APPLIST ACTIVITY : ";

    public AppListFragment appListFragment;
    public RenameFilesFragment renameFilesFragment;
    FragmentManager fm;
    FragmentTransaction ft;
    public static SearchView searchView;
    public static Toolbar toolbar_main;
    public static MenuItem search_menuItem;
    public static NotificationManager notimgr ;
    public static Notification.Builder myNotiBuilder;

    Activity activity = AppListActivity.this;
    final String PREF_NAME_APPLIST = "com.sunnykatiyar.AppManager.APPLIST";

    final String key_sorting = "SORT BY";
    String value_sorting;
    final String key_order_by = "INVERSE SORTING";
    String value_order_by;

    SharedPreferences sharedPrefAppList;
    SharedPreferences.Editor prefEditorAppList;
    static final String sort_by_name = "SORT_BY_NAME";
    static final String sort_by_update_time = "SORT_BY_UPDATE_TIME";
    static final String sort_by_install_time = "SORT_BY_INSTALL_TIME";
    static final String sort_by_size = "SORT_BY_SIZE";

    static final String order_decreasing = "ORDER_INCREASING";
    static final String order_increasing = "ORDER_DECREASING";

    public static String sort_by ;
    public static String order_by;
    public static String search_query="";
    NavigationView navigationView;
    AppSettingsFragment appSettingsFragment;

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
        navigationView.getMenu().findItem(R.id.nav_applist_activity).setChecked(true);

        fm = this.getFragmentManager();
        appListFragment = new AppListFragment();
        appSettingsFragment = new AppSettingsFragment();
        renameFilesFragment = new RenameFilesFragment();

        notimgr = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);

        sharedPrefAppList = getSharedPreferences(PREF_NAME_APPLIST,MODE_PRIVATE);
        prefEditorAppList = sharedPrefAppList.edit();

        Log.e(TAG,"in onCreate : Before transaction ");
            ft = fm.beginTransaction();
            ft.add(R.id.fragment_container, appListFragment);
            ft.commit();
        Log.e(TAG,"in onCreate : transaction committed ");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_applist, menu);
        search_menuItem = menu.findItem(R.id.search_item);
        search_menuItem.setVisible(false);

        searchView = (SearchView) search_menuItem.getActionView();
        searchView.setIconifiedByDefault(true);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search_query = query;
                ft = fm.beginTransaction();
              //  ft.remove(appListFragment).commit();
                appListFragment = new AppListFragment();
                ft.replace(R.id.fragment_container, appListFragment,query).commit();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                Log.e("in AppListActivity : ", " in QueryTextChangeListener ");
               // adapter.getFilter().filter(newText);
//                search_query = query;
//                ft = fm.beginTransaction();
//                //  ft.remove(appListFragment).commit();
//                appListFragment = new AppListFragment();
//                ft.replace(R.id.fragment_container, appListFragment,query).commit();
                return false;
            }});

        searchView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                Log.e("in AppListActivity : ", " in setOnclickListener ");
                if(b) toolbar_main.setTitle("");
            }

    });

        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                Log.e("in AppListActivity : ", " in setOnCloseListener ");
                adapter.notifyDataSetChanged();
                toolbar_main.setTitle(R.string.app_name);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        Log.i(TAG," onPrepareOptionsMenu : ");

//-----------------------LOADING "SORT BY" FROM SHARED PREFERENCES---------------------------------------
        value_sorting = sharedPrefAppList.getString(key_sorting,sort_by_name);
        Log.i(TAG,"Sorting Setting in Shared Preferences: "+value_sorting);

        if(value_sorting.equals(sort_by_name)){
            Log.i(TAG," inside equating sort_by_name : ");
            sort_by = sort_by_name;
            menu.findItem(R.id.menuitem_sortbyname).setChecked(true);
        }
        else if(value_sorting.equals(sort_by_update_time)){
            Log.i(TAG," inside equating sort_by_date : ");
            sort_by = sort_by_update_time;
            menu.findItem(R.id.menuitem_sortby_update_time).setChecked(true);
        }else if(value_sorting.equals(sort_by_install_time)){
            Log.i(TAG," inside equating sort_by_date : ");
            sort_by = sort_by_update_time;
            menu.findItem(R.id.menuitem_sortby_install_time).setChecked(true);
        }
        else if(value_sorting.equals(sort_by_size)){
            Log.i(TAG," inside equating sort_by_size : ");
            sort_by = sort_by_size;
            menu.findItem(R.id.menuitem_sortbysize).setChecked(true);
        }
          Log.i(TAG," Value of sort_by : " + sort_by);

//--------------------------------LOADING "ORDER BY" FROM SHARED PREFERENCES---------------------------------------

        value_order_by = sharedPrefAppList.getString(key_order_by,order_increasing);
        Log.i(TAG," Found Ordering Settings in SHARED PREFERENCES: "+ value_order_by);

        if(value_order_by.equals(order_decreasing)){
            menu.findItem(R.id.menuitem_decreasing).setChecked(true);
            order_by = order_decreasing;
        }
        else if(value_order_by.equals(order_increasing)){
            menu.findItem(R.id.menuitem_increasing).setChecked(true);
            order_by = order_increasing;
        }
        Log.i(TAG," Value of order by : " + order_by);

//----------------------------------------------------------------------------------------------------------------------
//--------------------------------------------------------------------------------------------------------------------
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         Toast t = new Toast(this);

         int id = item.getItemId();

         switch(id)
         {
             case R.id.search_item:{
                 Log.e("in AppListActivity : ", "Search Menu Button Clicked");
                                     break;            }
             case R.id.reload:{
                        ft = fm.beginTransaction();
                        ft.replace(R.id.fragment_container, appListFragment);
                        ft.commit();
                         break;         }

//             case R.id.settings:{
//                Toast.makeText(getApplication(),"OpeningSettings", Toast.LENGTH_SHORT).show();
//                 ft = fm.beginTransaction();
//                 ft.replace(R.id.fragment_container, renameFilesFragment );
//                 ft.commit();
//                 break;
//             }

             case R.id.menuitem_sortbyname : {
                item.setChecked(true);
                sort_by = sort_by_name;
                prefEditorAppList.putString(key_sorting, sort_by).commit();
                appListFragment.SortAppList();
                break;
            }

             case R.id.menuitem_sortby_install_time: {
                item.setChecked(true);
                 Log.i(TAG,"Clicked sort by size");
                 sort_by = sort_by_update_time;
                prefEditorAppList.putString(key_sorting, sort_by).commit();
                appListFragment.SortAppList();
                break;
             }

             case R.id.menuitem_sortby_update_time: {
                 item.setChecked(true);
                 Log.i(TAG,"Clicked sort by size");
                 sort_by = sort_by_update_time;
                 prefEditorAppList.putString(key_sorting, sort_by).commit();
                 appListFragment.SortAppList();
                 break;
             }

             case R.id.menuitem_sortbysize:{
                item.setChecked(true);
                Log.i(TAG,"Clicked sort by size");
                sort_by = sort_by_size;
                 prefEditorAppList.putString(key_sorting, sort_by).commit();
                appListFragment.SortAppList();
                break;
            }

            //-----------------------------------INVERSE SORTING-------------------------------------
             case R.id.menuitem_decreasing:{
                item.setChecked(true);
                order_by = order_decreasing;
                 prefEditorAppList.putString(key_order_by, order_by).commit();
                appListFragment.SortAppList();
                break;
            }

             case R.id.menuitem_increasing:{
                item.setChecked(true);
                order_by = order_increasing;
                 prefEditorAppList.putString(key_order_by, order_by).commit();
                appListFragment.SortAppList();
                break;
            }

             case android.R.id.home:   finish();
                 break;
         }
        return true;
    }
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_apk_rename_format) {
            Intent i = new Intent(this, ApkRenameActivity.class);
            startActivity(i);
            item.setChecked(false);

        } else if (id == R.id.nav_apklist_activity) {
            Intent i = new Intent(this, ApkListActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            item.setChecked(true);

        } else if (id == R.id.nav_file_renamer) {

            Log.e(TAG,"NavBar Rename Files fragment begin ");
            ft = fm.beginTransaction();
            ft.add(R.id.fragment_container, appSettingsFragment);
            ft.commit();
            Log.e(TAG,"NavBar Rename Files Fragment End");
//            Intent i = new Intent(this,RenameFiles.class);
//            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(i);
//            item.setChecked(false);
            // navigationView.getMenu().findItem(R.id.nav_file_renamer).setChecked(true);

        } else if (id == R.id.nav_applist_activity) {
            Intent i = new Intent(this,AppListActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            item.setChecked(true);
            navigationView.getMenu().findItem(R.id.nav_file_renamer).setChecked(true);

        } else if (id == R.id.nav_app_settings) {
            Log.e(TAG,"NavBar App Settings ");
            ft = fm.beginTransaction();
            ft.add(R.id.fragment_container, appSettingsFragment);
            ft.commit();
            Log.e(TAG,"NavBar App Settings End");
        } else if (id == R.id.nav_send) {

        }else  if (id == R.id.nav_about) {
            Toast.makeText(getApplication(),"About Selected", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this,About.class));
//            Toast.makeText(context, "Don't be so curious about me. I am in the making.. ", Toast.LENGTH_LONG).show();
        }else if (id == R.id.nav_help) {
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

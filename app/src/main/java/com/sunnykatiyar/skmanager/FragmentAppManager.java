package com.sunnykatiyar.skmanager;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.content.Context.CLIPBOARD_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static com.sunnykatiyar.skmanager.ActivityMain.toolbar_main;

public class FragmentAppManager extends Fragment {

    public static Context appContext;
    private static ProgressBar progressBar;
    public static PackageManager mainpm;
    private static List<PackageInfo> filtered_apps_list;
    private static List<PackageInfo> applist;
    private static RecyclerView app_listview;
    private static Long initFragmentTime;
    public static ActivityManager activityManager;
    public static ClipboardManager clipboardManager;
    private static AdapterAppList adapter;

    MyAsyncTask myAsyncTask;
    private final static String TAG = "APPLIST_FRAGMENT : ";
    private static TextView label_msgbox;

    List<ApplicationInfo> appInfoList;
    public static SharedPreferences sharedPrefAppSettings;
    public static SharedPreferences.Editor prefEditAppSettings;


    private static final String sort_apps_by_name = "SORT_BY_NAME";
    private static final String sort_apps_by_update_time = "SORT_BY_UPDATE_TIME";
    private static final String sort_apps_by_install_time = "SORT_BY_INSTALL_TIME";
    private static final String sort_apps_by_size = "SORT_BY_SIZE";

    private static final String order_apps_decreasing = "ORDER_INCREASING";
    private static final String order_apps_increasing = "ORDER_DECREASING";

    private static String sort_apps_by;
    private static String order_apps_by;
    private String search_query = "";

    private static final String key_sorting = "SORT_APPS_BY";
    private static final String key_order_by = "ORDER_APPS_BY";
    private static final String key_filter_apps = "FILTER_APPS_BY";
    int value_filter_apps;

    private static final int SYSTEM_APPS = PackageManager.MATCH_SYSTEM_ONLY;
    private static final int LAUNCHABLE_APPS = PackageManager.MATCH_DEFAULT_ONLY;
    private static final int USER_APPS = 103670;
    private static final int ALL_APPS = PackageManager.MATCH_ALL;
    private static final int DATA_ONLY_APPS = PackageManager.MATCH_UNINSTALLED_PACKAGES;

    private static final int CATEGORY_GAMES = ApplicationInfo.CATEGORY_GAME;
    private static final int CATEGORY_AUDIO = ApplicationInfo.CATEGORY_AUDIO;
    private static final int CATEGORY_VIDEO = ApplicationInfo.CATEGORY_VIDEO;
    private static final int CATEGORY_MAPS = ApplicationInfo.CATEGORY_MAPS;
    private static final int CATEGORY_IMAGE = ApplicationInfo.CATEGORY_IMAGE;
    private static final int CATEGORY_SOCIAL = ApplicationInfo.CATEGORY_SOCIAL;
    private static final int CATEGORY_PRODUCTIVITY = ApplicationInfo.CATEGORY_PRODUCTIVITY;
    private static final int CATEGORY_NEWS = ApplicationInfo.CATEGORY_NEWS;
    private static final int CATEGORY_UNDEFINED = ApplicationInfo.CATEGORY_UNDEFINED;

    private static final int APPS_STOPPED = ApplicationInfo.FLAG_STOPPED;
    private static final int APPS_ON_EXTERNAL_STORAGE = ApplicationInfo.FLAG_EXTERNAL_STORAGE;
    private static final int APPS_RUNNING = 193456;
    private static final int APPS_UPDATED_SYSTEM = ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;
    private static final int APPS_SUSPENDED = ApplicationInfo.FLAG_SUSPENDED;

    private final String toast_msg = "show_as_toast";
    private final String textview_msg = "show_in_textview";
    private final String log_msg = "show_as_toast";

    Activity context = getActivity();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e("in ApplistFragment : ", "in onCreateView ");
        setRetainInstance(true);
        View view = inflater.inflate(R.layout.fragment_app_manager, container, false);

        appContext = getContext();

        sharedPrefAppSettings = appContext.getSharedPreferences(appContext.getResources().getString(R.string.sharedPref_appsSettings), MODE_PRIVATE);
        prefEditAppSettings = sharedPrefAppSettings.edit();

        progressBar = view.findViewById(R.id.progress_bar);
        TextView progress_percent = view.findViewById(R.id.progress_int);
        app_listview = view.findViewById(R.id.app_recycleview_list);
        label_msgbox = view.findViewById(R.id.label_applist_msgbox);
        mainpm = getContext().getPackageManager();

        sort_apps_by = sharedPrefAppSettings.getString(key_sorting, sort_apps_by_name);
        value_filter_apps = sharedPrefAppSettings.getInt(key_filter_apps, LAUNCHABLE_APPS);

        clipboardManager = (ClipboardManager) getContext().getSystemService(CLIPBOARD_SERVICE);
        activityManager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);

        LinearLayoutManager llm = new LinearLayoutManager(appContext);
        llm.setOrientation(RecyclerView.VERTICAL);
        app_listview.setLayoutManager(llm);

        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(appContext, llm.getOrientation());
        app_listview.addItemDecoration(mDividerItemDecoration);


        if (myAsyncTask == null) {
            filtered_apps_list = new ArrayList<>();
            myAsyncTask = new MyAsyncTask(getActivity(), search_query);
            myAsyncTask.execute();
            adapter = new AdapterAppList(mainpm, filtered_apps_list, getActivity());
            showMsg(log_msg, "Launching asynctask : query = " + search_query, false);
        } else {
            showMsg(log_msg, "Using old aynctask for applist : query = " + search_query, false);
            adapter = new AdapterAppList(mainpm, filtered_apps_list, getActivity());
            app_listview.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }

        showMsgInTextView("");
        registerForContextMenu(app_listview);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.i(TAG, "in On Create Options");

        inflater.inflate(R.menu.menu_applist, menu);

        MenuItem search_menuItem = menu.findItem(R.id.search_item);
        search_menuItem.setVisible(true);

        SearchView searchView = (SearchView) search_menuItem.getActionView();
        searchView.setIconifiedByDefault(true);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search_query = query;
                myAsyncTask = new MyAsyncTask(getActivity(), search_query);
                myAsyncTask.execute();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                Log.e("in ActivityMain : ", " in QueryTextChangeListener ");
                search_query = query;
                myAsyncTask.cancel(true);
                myAsyncTask = new MyAsyncTask(getActivity(), search_query);
                myAsyncTask.execute();

                return true;
            }
        });

//        searchView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View view, boolean b) {
//                Log.e("in ActivityMain : ", " OnFocusChanged ");
//                if(b) toolbar_main.setTitle("");
//            }
//
//        });

        searchView.setOnClickListener(view -> toolbar_main.setTitle(""));

        searchView.setOnCloseListener(() -> {
            Log.e("in ActivityMain : ", " in setOnCloseListener ");
            adapter.notifyDataSetChanged();
            toolbar_main.setTitle(R.string.app_name);
            return false;
        });

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        Log.i(TAG, " onPrepareOptionsMenu : ");

//-----------------------LOADING "SORT BY" FROM SHARED PREFERENCES---------------------------------------
        String value_sorting = sharedPrefAppSettings.getString(key_sorting, sort_apps_by_name);
        Log.i(TAG, "Sorting Setting in Shared Preferences: " + value_sorting);

        if (value_sorting.equals(sort_apps_by_name)) {
            MenuItem item = menu.findItem(R.id.menuitem_sortappsby_name);
            if (!item.isChecked()) {
                item.setChecked(true);
            }
            sort_apps_by = sort_apps_by_name;
            Log.i(TAG, " inside equating sort_apps_by_name : ");
        } else if (value_sorting.equals(sort_apps_by_update_time)) {
            MenuItem item = menu.findItem(R.id.menuitem_sortappsby_updatetime);
            if (!item.isChecked()) {
                item.setChecked(true);
            }
            sort_apps_by = sort_apps_by_update_time;
            Log.i(TAG, " inside equating sort_by_update_time : ");
        } else if (value_sorting.equals(sort_apps_by_install_time)) {
            MenuItem item = menu.findItem(R.id.menuitem_sortappsby_installtime);
            if (!item.isChecked()) {
                item.setChecked(true);
            }
            sort_apps_by = sort_apps_by_update_time;
            Log.i(TAG, " inside equating sort_by_install_time : ");
        } else if (value_sorting.equals(sort_apps_by_size)) {
            MenuItem item = menu.findItem(R.id.menuitem_sortappsby_size);
            if (!item.isChecked()) {
                item.setChecked(true);
            }
            sort_apps_by = sort_apps_by_size;
            Log.i(TAG, " inside equating sort_apps_by_size : ");
        }
        Log.i(TAG, " Value of sort_apks_by : " + sort_apps_by);

//--------------------------------LOADING "ORDER BY" FROM SHARED PREFERENCES---------------------------------------

        String value_order_by = sharedPrefAppSettings.getString(key_order_by, order_apps_increasing);
        Log.i(TAG, " Found Ordering Settings in SHARED PREFERENCES: " + value_order_by);

        if (value_order_by.equals(order_apps_decreasing)) {
            MenuItem item = menu.findItem(R.id.menuitem_apps_decreasing);
            if (!item.isChecked()) {
                item.setChecked(true);
            }

            order_apps_by = order_apps_decreasing;
        } else if (value_order_by.equals(order_apps_increasing)) {
            MenuItem item = menu.findItem(R.id.menuitem_apps_increasing);
            if (!item.isChecked()) {
                item.setChecked(true);
            }

            order_apps_by = order_apps_increasing;
        }
        Log.i(TAG, " Value of order by : " + order_apps_by);

//----------------------------------------------------------------------------------------------------------------------


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.search_item: {
                Log.e("in ActivityMain : ", "Search Menu Button Clicked");
                break;
            }
            case R.id.reload: {
                myAsyncTask = new MyAsyncTask(getActivity(), search_query);
                myAsyncTask.execute();
                adapter = new AdapterAppList(mainpm, filtered_apps_list, getActivity());
                app_listview.setAdapter(adapter);
                break;
            }

            case R.id.apps_launchable: {

                if (!item.isChecked()) {
                    item.setChecked(true);
                }
                prefEditAppSettings.putInt(key_filter_apps, LAUNCHABLE_APPS).commit();
                myAsyncTask = new MyAsyncTask(getActivity(), search_query);
                myAsyncTask.execute();
                adapter = new AdapterAppList(mainpm, filtered_apps_list, getActivity());
                app_listview.setAdapter(adapter);
                break;
            }

            case R.id.apps_all: {
                if (!item.isChecked()) {
                    item.setChecked(true);
                }
                prefEditAppSettings.putInt(key_filter_apps, ALL_APPS).commit();
                myAsyncTask = new MyAsyncTask(getActivity(), search_query);
                myAsyncTask.execute();
                adapter = new AdapterAppList(mainpm, filtered_apps_list, getActivity());
                app_listview.setAdapter(adapter);
                break;
            }


            case R.id.apps_user_only: {
                if (!item.isChecked()) {
                    item.setChecked(true);
                }
                prefEditAppSettings.putInt(key_filter_apps, USER_APPS).commit();
                myAsyncTask = new MyAsyncTask(getActivity(), search_query);
                myAsyncTask.execute();
                adapter = new AdapterAppList(mainpm, filtered_apps_list, getActivity());
                app_listview.setAdapter(adapter);
                break;
            }


            case R.id.apps_system_only: {
                if (!item.isChecked()) {
                    item.setChecked(true);
                }
                prefEditAppSettings.putInt(key_filter_apps, SYSTEM_APPS).commit();
                myAsyncTask = new MyAsyncTask(getActivity(), search_query);
                myAsyncTask.execute();
                adapter = new AdapterAppList(mainpm, filtered_apps_list, getActivity());
                app_listview.setAdapter(adapter);
                break;
            }

            case R.id.apps_uninstalled: {
                if (!item.isChecked()) {
                    item.setChecked(true);
                }
                prefEditAppSettings.putInt(key_filter_apps, DATA_ONLY_APPS).commit();
                myAsyncTask = new MyAsyncTask(getActivity(), search_query);
                myAsyncTask.execute();
                adapter = new AdapterAppList(mainpm, filtered_apps_list, getActivity());
                app_listview.setAdapter(adapter);
                break;
            }

            case R.id.apps_updated_system: {
                if (!item.isChecked()) {
                    item.setChecked(true);
                }
                prefEditAppSettings.putInt(key_filter_apps, APPS_UPDATED_SYSTEM).commit();
                myAsyncTask = new MyAsyncTask(getActivity(), search_query);
                myAsyncTask.execute();
                adapter = new AdapterAppList(mainpm, filtered_apps_list, getActivity());
                app_listview.setAdapter(adapter);
                break;
            }

            case R.id.app_suspended: {
                if (!item.isChecked()) {
                    item.setChecked(true);
                }
                prefEditAppSettings.putInt(key_filter_apps, APPS_SUSPENDED).commit();
                myAsyncTask = new MyAsyncTask(getActivity(), search_query);
                myAsyncTask.execute();
                adapter = new AdapterAppList(mainpm, filtered_apps_list, getActivity());
                app_listview.setAdapter(adapter);
                break;
            }

            case R.id.app_running: {
                if (!item.isChecked()) {
                    item.setChecked(true);
                }
                prefEditAppSettings.putInt(key_filter_apps, APPS_RUNNING).commit();
                myAsyncTask = new MyAsyncTask(getActivity(), search_query);
                myAsyncTask.execute();
                adapter = new AdapterAppList(mainpm, filtered_apps_list, getActivity());
                app_listview.setAdapter(adapter);
                break;
            }

            case R.id.app_external_storage: {
                if (!item.isChecked()) {
                    item.setChecked(true);
                }
                prefEditAppSettings.putInt(key_filter_apps, APPS_ON_EXTERNAL_STORAGE).commit();
                myAsyncTask = new MyAsyncTask(getActivity(), search_query);
                myAsyncTask.execute();
                adapter = new AdapterAppList(mainpm, filtered_apps_list, getActivity());
                app_listview.setAdapter(adapter);
                break;
            }


            case R.id.app_stopped: {
                if (!item.isChecked()) {
                    item.setChecked(true);
                }
                prefEditAppSettings.putInt(key_filter_apps, APPS_STOPPED).commit();
                myAsyncTask = new MyAsyncTask(getActivity(), search_query);
                myAsyncTask.execute();
                adapter = new AdapterAppList(mainpm, filtered_apps_list, getActivity());
                app_listview.setAdapter(adapter);
                break;
            }

            case R.id.apps_productivity: {
                if (!item.isChecked()) {
                    item.setChecked(true);
                }
                prefEditAppSettings.putInt(key_filter_apps, CATEGORY_PRODUCTIVITY).commit();
                myAsyncTask = new MyAsyncTask(getActivity(), search_query);
                myAsyncTask.execute();
                adapter = new AdapterAppList(mainpm, filtered_apps_list, getActivity());
                app_listview.setAdapter(adapter);
                break;
            }

            case R.id.apps_news: {
                if (!item.isChecked()) {
                    item.setChecked(true);
                }
                prefEditAppSettings.putInt(key_filter_apps, CATEGORY_NEWS).commit();
                myAsyncTask = new MyAsyncTask(getActivity(), search_query);
                myAsyncTask.execute();
                adapter = new AdapterAppList(mainpm, filtered_apps_list, getActivity());
                app_listview.setAdapter(adapter);
                break;
            }

            case R.id.apps_social: {
                if (!item.isChecked()) {
                    item.setChecked(true);
                }
                prefEditAppSettings.putInt(key_filter_apps, CATEGORY_SOCIAL).commit();
                myAsyncTask = new MyAsyncTask(getActivity(), search_query);
                myAsyncTask.execute();
                adapter = new AdapterAppList(mainpm, filtered_apps_list, getActivity());
                app_listview.setAdapter(adapter);
                break;
            }

            case R.id.app_maps: {
                if (!item.isChecked()) {
                    item.setChecked(true);
                }
                prefEditAppSettings.putInt(key_filter_apps, CATEGORY_MAPS).commit();
                myAsyncTask = new MyAsyncTask(getActivity(), search_query);
                myAsyncTask.execute();
                adapter = new AdapterAppList(mainpm, filtered_apps_list, getActivity());
                app_listview.setAdapter(adapter);
                break;
            }

            case R.id.apps_images: {
                if (!item.isChecked()) {
                    item.setChecked(true);
                }
                prefEditAppSettings.putInt(key_filter_apps, CATEGORY_IMAGE).commit();
                myAsyncTask = new MyAsyncTask(getActivity(), search_query);
                myAsyncTask.execute();
                adapter = new AdapterAppList(mainpm, filtered_apps_list, getActivity());
                app_listview.setAdapter(adapter);
                break;
            }

            case R.id.apps_videos: {
                if (!item.isChecked()) {
                    item.setChecked(true);
                }
                prefEditAppSettings.putInt(key_filter_apps, CATEGORY_VIDEO).commit();
                myAsyncTask = new MyAsyncTask(getActivity(), search_query);
                myAsyncTask.execute();
                adapter = new AdapterAppList(mainpm, filtered_apps_list, getActivity());
                app_listview.setAdapter(adapter);
                break;
            }

            case R.id.apps_audio: {
                if (!item.isChecked()) {
                    item.setChecked(true);
                }
                prefEditAppSettings.putInt(key_filter_apps, CATEGORY_AUDIO).commit();
                myAsyncTask = new MyAsyncTask(getActivity(), search_query);
                myAsyncTask.execute();
                adapter = new AdapterAppList(mainpm, filtered_apps_list, getActivity());
                app_listview.setAdapter(adapter);
                break;
            }

            case R.id.apps_games: {
                if (!item.isChecked()) {
                    item.setChecked(true);
                }
                prefEditAppSettings.putInt(key_filter_apps, CATEGORY_GAMES).commit();
                myAsyncTask = new MyAsyncTask(getActivity(), search_query);
                myAsyncTask.execute();
                adapter = new AdapterAppList(mainpm, filtered_apps_list, getActivity());
                app_listview.setAdapter(adapter);
                break;
            }

            case R.id.app_undefined: {
                if (!item.isChecked()) {
                    item.setChecked(true);
                }
                prefEditAppSettings.putInt(key_filter_apps, CATEGORY_UNDEFINED).commit();
                myAsyncTask = new MyAsyncTask(getActivity(), search_query);
                myAsyncTask.execute();
                adapter = new AdapterAppList(mainpm, filtered_apps_list, getActivity());
                app_listview.setAdapter(adapter);
                break;
            }

            case R.id.menuitem_sortbyname: {
                if (!item.isChecked()) {
                    item.setChecked(true);
                }
                sort_apps_by = sort_apps_by_name;
                Log.i(TAG, "Clicked sort by name");
                prefEditAppSettings.putString(key_sorting, sort_apps_by).commit();
                SortAppList();
                break;
            }

            case R.id.menuitem_sortappsby_installtime: {
                if (!item.isChecked()) {
                    item.setChecked(true);
                }
                Log.i(TAG, "Clicked sort by size");
                sort_apps_by = sort_apps_by_update_time;
                prefEditAppSettings.putString(key_sorting, sort_apps_by).commit();
                SortAppList();
                break;
            }

            case R.id.menuitem_sortappsby_updatetime: {
                if (!item.isChecked()) {
                    item.setChecked(true);
                }
                Log.i(TAG, "Clicked sort by size");
                sort_apps_by = sort_apps_by_update_time;
                prefEditAppSettings.putString(key_sorting, sort_apps_by).commit();
                SortAppList();
                break;
            }

            case R.id.menuitem_sortappsby_size: {
                if (!item.isChecked()) {
                    item.setChecked(true);
                }
                Log.i(TAG, "Clicked sort by size");
                sort_apps_by = sort_apps_by_size;
                prefEditAppSettings.putString(key_sorting, sort_apps_by).commit();
                SortAppList();
                break;
            }

            //-----------------------------------INVERSE SORTING-------------------------------------
            case R.id.menuitem_apps_decreasing: {
                if (!item.isChecked()) {
                    item.setChecked(true);
                }
                order_apps_by = order_apps_decreasing;
                prefEditAppSettings.putString(key_order_by, order_apps_by).commit();
                SortAppList();
                break;
            }

            case R.id.menuitem_apps_increasing: {
                if (!item.isChecked()) {
                    item.setChecked(true);
                }
                order_apps_by = order_apps_increasing;
                prefEditAppSettings.putString(key_order_by, order_apps_by).commit();
                SortAppList();
                break;
            }
        }
        return true;
    }

    private void showMsg(String display_as, String msg, boolean isempty) {

        if (display_as.equals(toast_msg)) {
            Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
            Log.v(TAG, msg);
        } else if (display_as.equals(textview_msg)) {
            showMsgInTextView(msg);
            Log.v(TAG, msg);
        } else if (display_as.equals(log_msg)) {
            Log.i(TAG, msg);
        }

    }

    private void showMsgInTextView(String str) {
        if (str.isEmpty()) {
            String default_string = "Total Apps : " + filtered_apps_list.size();
            label_msgbox.setText(default_string);
        } else {
            label_msgbox.setText(str);
        }
    }

    class MyAsyncTask extends AsyncTask<Void, String, String> {

        final Activity activity;
        final String search_filter;
        final String TAG = "MYASYNCTASK ACTIVITY";

        MyAsyncTask(Activity a, String str) {
            this.activity = a;
            this.search_filter = str;
        }

        @Override
        protected void onPreExecute() {
            initFragmentTime = System.currentTimeMillis();

            Log.e("in MyAsyncTask :", "in onPreExecute : ");
            progressBar.setVisibility(View.VISIBLE);
//          progress_percent.setVisibility(View.VISIBLE);
            applist = new ArrayList<>();
            filtered_apps_list = new ArrayList<>();
        }

        @Override
        protected String doInBackground(Void... voids) {
            Log.e("in MyAsyncTask :", "in doInBackground : " + ((System.currentTimeMillis() - initFragmentTime) / 1000) + "s");
            LoadAppList(search_filter);
            return null;
        }

        @Override
        protected void onProgressUpdate(String... strings) {
            setLabelTextMsg(strings[0]);
        }

        void LoadAppList(String str) {
            Log.e("in MyAsyncTask :", "AppLoadList : " + ((System.currentTimeMillis() - initFragmentTime) / 1000) + "s");

            value_filter_apps = sharedPrefAppSettings.getInt(key_filter_apps, LAUNCHABLE_APPS);
            applist = new ArrayList<>();
            filtered_apps_list = new ArrayList<>();

            appInfoList = new ArrayList<>();

            int flags = 0;

            switch (value_filter_apps) {

                case LAUNCHABLE_APPS: {
                    applist = mainpm.getInstalledPackages(flags);
                    int i = 0;
                    String name;
                    for (PackageInfo p : applist) {
                        if (mainpm.getLaunchIntentForPackage(p.packageName) != null) {
                            name = p.applicationInfo.loadLabel(mainpm).toString();
                            //Log.i(TAG," Matching "+search_filter+" AND "+name+" And i = "+i);
                            if (name.toLowerCase().startsWith(str.toLowerCase())) {
                                //  Log.i(TAG," Matched "+search_filter+" AND "+name);
                                filtered_apps_list.add(p);
                                publishProgress((i++) + " : " + name);
                            }
                        }
                        if (isCancelled()) {
                            Log.i(TAG, "Task Cancelled");
                            break;
                        }
                    }
                    break;
                }

                case SYSTEM_APPS: {
                    flags = PackageManager.MATCH_SYSTEM_ONLY;

                    applist = mainpm.getInstalledPackages(flags);
                    int i = 0;
                    String name;
                    for (PackageInfo p : applist) {
                        // if(mainpm.getLaunchIntentForPackage(p.packageName) != null)
                        {
                            name = p.applicationInfo.loadLabel(mainpm).toString();
                            //Log.i(TAG," Matching "+search_filter+" AND "+name+" And i = "+i);
                            if (name.toLowerCase().startsWith(str.toLowerCase())) {
                                //  Log.i(TAG," Matched "+search_filter+" AND "+name);
                                filtered_apps_list.add(p);
                                publishProgress((i++) + " : " + name);
                            }
                        }
                        if (isCancelled()) {
                            Log.i(TAG, "Task Cancelled");
                            break;
                        }
                    }
                    break;
                }

                case ALL_APPS: {
                    flags = PackageManager.MATCH_ALL;

                    applist = mainpm.getInstalledPackages(flags);
                    int i = 0;
                    String name;
                    for (PackageInfo p : applist) {
                        // if(mainpm.getLaunchIntentForPackage(p.packageName) != null)
                        {
                            name = p.applicationInfo.loadLabel(mainpm).toString();
                            //Log.i(TAG," Matching "+search_filter+" AND "+name+" And i = "+i);
                            if (name.toLowerCase().startsWith(str.toLowerCase())) {
                                //  Log.i(TAG," Matched "+search_filter+" AND "+name);
                                filtered_apps_list.add(p);
                                publishProgress((i++) + " : " + name);
                            }
                        }
                        if (isCancelled()) {
                            Log.i(TAG, "Task Cancelled");
                            break;
                        }
                    }
                    break;
                }

                case DATA_ONLY_APPS: {

                    flags = PackageManager.MATCH_UNINSTALLED_PACKAGES| PackageManager.GET_META_DATA;
                    appInfoList = mainpm.getInstalledApplications(flags);
                    int i = 0;
                    String name;
                    for (ApplicationInfo info : appInfoList) {
                        if((info.flags & ApplicationInfo.FLAG_IS_DATA_ONLY) != 0) {
                            name = info.loadLabel(mainpm).toString();
                            if (name.toLowerCase().contains(str.toLowerCase())) {
                                try {
                                    filtered_apps_list.add(mainpm.getPackageInfo(info.packageName, 0));
                                } catch (PackageManager.NameNotFoundException e) {
                                    Log.e(TAG, "Error getting packageInfo for " + name);
                                }
                                publishProgress((i++) + " : " + name);
                            }
                        }
                        if (isCancelled()) {
                            Log.i(TAG, "Task Cancelled");
                            break;
                        }
                    }
                    break;
                }

                case USER_APPS: {
                    appInfoList = mainpm.getInstalledApplications(0);
                    int i = 0;
                    String name;
                    for (ApplicationInfo info : appInfoList) {
                        if ((info.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 1) {
                            name = info.loadLabel(mainpm).toString();
                            if (name.toLowerCase().contains(str.toLowerCase())) {
                                try {
                                    filtered_apps_list.add(mainpm.getPackageInfo(info.packageName, 0));
                                } catch (PackageManager.NameNotFoundException e) {
                                    Log.e(TAG, "Error getting packageInfo for " + name);
                                }
                                publishProgress((i++) + " : " + name);
                            }
                        }
                        if (isCancelled()) {
                            Log.i(TAG, "Task Cancelled");
                            break;
                        }
                    }
                    break;
                }

                case APPS_UPDATED_SYSTEM : {
                    appInfoList = mainpm.getInstalledApplications(PackageManager.MATCH_SYSTEM_ONLY);
                    int i = 0;
                    String name;
                    for (ApplicationInfo info : appInfoList) {
                        if ((info.flags & (ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0) {
                            name = info.loadLabel(mainpm).toString();
                            if (name.toLowerCase().contains(str.toLowerCase())) {
                                try {
                                    filtered_apps_list.add(mainpm.getPackageInfo(info.packageName, 0));
                                } catch (PackageManager.NameNotFoundException e) {
                                    Log.e(TAG, "Error getting packageInfo for " + name);
                                }
                                publishProgress((i++) + " : " + name);
                            }
                        }
                        if (isCancelled()) {
                            Log.i(TAG, "Task Cancelled");
                            break;
                        }
                    }
                    break;
                }

                case APPS_STOPPED : {
                    appInfoList = mainpm.getInstalledApplications(0);
                    int i = 0;
                    String name;
                    for (ApplicationInfo info : appInfoList) {
                        if ((info.flags & (ApplicationInfo.FLAG_STOPPED)) != 0) {
                            name = info.loadLabel(mainpm).toString();
                            if (name.toLowerCase().contains(str.toLowerCase())) {
                                try {
                                    filtered_apps_list.add(mainpm.getPackageInfo(info.packageName, 0));
                                } catch (PackageManager.NameNotFoundException e) {
                                    Log.e(TAG, "Error getting packageInfo for " + name);
                                }
                                publishProgress((i++) + " : " + name);
                            }
                        }
                        if (isCancelled()) {
                            Log.i(TAG, "Task Cancelled");
                            break;
                        }
                    }
                    break;
                }

                case APPS_RUNNING : {
                    appInfoList = mainpm.getInstalledApplications(0);
                    int i = 0;
                    String name;
                    for (ApplicationInfo info : appInfoList) {
                        if ((info.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) == 0) {
                            name = info.loadLabel(mainpm).toString();
                            if (name.toLowerCase().contains(str.toLowerCase())) {
                                try {
                                    filtered_apps_list.add(mainpm.getPackageInfo(info.packageName, 0));
                                } catch (PackageManager.NameNotFoundException e) {
                                    Log.e(TAG, "Error getting packageInfo for " + name);
                                }
                                publishProgress((i++) + " : " + name);
                            }
                        }
                        if (isCancelled()) {
                            Log.i(TAG, "Task Cancelled");
                            break;
                        }
                    }
                    break;
                }

                case APPS_ON_EXTERNAL_STORAGE : {
                    appInfoList = mainpm.getInstalledApplications(0);
                    int i = 0;
                    String name;
                    for (ApplicationInfo info : appInfoList) {
                        if ((info.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
                            name = info.loadLabel(mainpm).toString();
                            if (name.toLowerCase().contains(str.toLowerCase())) {
                                try {
                                    filtered_apps_list.add(mainpm.getPackageInfo(info.packageName, 0));
                                } catch (PackageManager.NameNotFoundException e) {
                                    Log.e(TAG, "Error getting packageInfo for " + name);
                                }
                                publishProgress((i++) + " : " + name);
                            }
                        }
                        if (isCancelled()) {
                            Log.i(TAG, "Task Cancelled");
                            break;
                        }
                    }
                    break;
                }

                case APPS_SUSPENDED : {
                    appInfoList = mainpm.getInstalledApplications(0);
                    int i = 0;
                    String name;
                    for (ApplicationInfo info : appInfoList) {
                        if ((info.flags & ApplicationInfo.FLAG_SUSPENDED) != 0) {
                            name = info.loadLabel(mainpm).toString();
                            if (name.toLowerCase().contains(str.toLowerCase())) {
                                try {
                                    filtered_apps_list.add(mainpm.getPackageInfo(info.packageName, 0));
                                } catch (PackageManager.NameNotFoundException e) {
                                    Log.e(TAG, "Error getting packageInfo for " + name);
                                }
                                publishProgress((i++) + " : " + name);
                            }
                        }
                        if (isCancelled()) {
                            Log.i(TAG, "Task Cancelled");
                            break;
                        }
                    }
                    break;
                }

                case CATEGORY_GAMES : {
                    appInfoList = mainpm.getInstalledApplications(0);
                    int i = 0;
                    String name;
                    for (ApplicationInfo info : appInfoList) {
                        if(info.category == ApplicationInfo.CATEGORY_GAME)
                        {
                            name = info.loadLabel(mainpm).toString();
                            if (name.toLowerCase().contains(str.toLowerCase())) {
                                try {
                                    filtered_apps_list.add(mainpm.getPackageInfo(info.packageName, 0));
                                } catch (PackageManager.NameNotFoundException e) {
                                    Log.e(TAG, "Error getting packageInfo for " + name);
                                }
                                publishProgress((i++) + " : " + name);
                            }
                        }
                        if (isCancelled()) {
                            Log.i(TAG, "Task Cancelled");
                            break;
                        }
                    }
                    break;
                }

                case CATEGORY_IMAGE : {
                    appInfoList = mainpm.getInstalledApplications(0);
                    int i = 0;
                    String name;
                    for (ApplicationInfo info : appInfoList) {
                        if ((info.category == ApplicationInfo.CATEGORY_IMAGE)) {
                            name = info.loadLabel(mainpm).toString();
                            if (name.toLowerCase().contains(str.toLowerCase())) {
                                try {
                                    filtered_apps_list.add(mainpm.getPackageInfo(info.packageName, 0));
                                } catch (PackageManager.NameNotFoundException e) {
                                    Log.e(TAG, "Error getting packageInfo for " + name);
                                }
                                publishProgress((i++) + " : " + name);
                            }
                        }
                        if (isCancelled()) {
                            Log.i(TAG, "Task Cancelled");
                            break;
                        }
                    }
                    break;
                }

                case CATEGORY_AUDIO : {
                    appInfoList = mainpm.getInstalledApplications(0);
                    int i = 0;
                    String name;
                    for (ApplicationInfo info : appInfoList) {
                        if ((info.category == ApplicationInfo.CATEGORY_AUDIO)) {
                            name = info.loadLabel(mainpm).toString();
                            if (name.toLowerCase().contains(str.toLowerCase())) {
                                try {
                                    filtered_apps_list.add(mainpm.getPackageInfo(info.packageName, 0));
                                } catch (PackageManager.NameNotFoundException e) {
                                    Log.e(TAG, "Error getting packageInfo for " + name);
                                }
                                publishProgress((i++) + " : " + name);
                            }
                        }
                        if (isCancelled()) {
                            Log.i(TAG, "Task Cancelled");
                            break;
                        }
                    }
                    break;
                }

                case CATEGORY_VIDEO : {
                    appInfoList = mainpm.getInstalledApplications(0);
                    int i = 0;
                    String name;
                    for (ApplicationInfo info : appInfoList) {
                        if (info.category == ApplicationInfo.CATEGORY_VIDEO) {
                            name = info.loadLabel(mainpm).toString();
                            if (name.toLowerCase().contains(str.toLowerCase())) {
                                try {
                                    filtered_apps_list.add(mainpm.getPackageInfo(info.packageName, 0));
                                } catch (PackageManager.NameNotFoundException e) {
                                    Log.e(TAG, "Error getting packageInfo for " + name);
                                }
                                publishProgress((i++) + " : " + name);
                            }
                        }
                        if (isCancelled()) {
                            Log.i(TAG, "Task Cancelled");
                            break;
                        }
                    }
                    break;
                }

                case CATEGORY_NEWS : {
                    appInfoList = mainpm.getInstalledApplications(0);
                    int i = 0;
                    String name;
                    for (ApplicationInfo info : appInfoList) {
                        if (info.category == ApplicationInfo.CATEGORY_NEWS) {
                            name = info.loadLabel(mainpm).toString();
                            if (name.toLowerCase().contains(str.toLowerCase())) {
                                try {
                                    filtered_apps_list.add(mainpm.getPackageInfo(info.packageName, 0));
                                } catch (PackageManager.NameNotFoundException e) {
                                    Log.e(TAG, "Error getting packageInfo for " + name);
                                }
                                publishProgress((i++) + " : " + name);
                            }
                        }
                        if (isCancelled()) {
                            Log.i(TAG, "Task Cancelled");
                            break;
                        }
                    }
                    break;
                }

                case CATEGORY_PRODUCTIVITY : {
                    appInfoList = mainpm.getInstalledApplications(0);
                    int i = 0;
                    String name;
                    for (ApplicationInfo info : appInfoList) {
                        if ((info.category ==ApplicationInfo.CATEGORY_PRODUCTIVITY)) {
                            name = info.loadLabel(mainpm).toString();
                            if (name.toLowerCase().contains(str.toLowerCase())) {
                                try {
                                    filtered_apps_list.add(mainpm.getPackageInfo(info.packageName, 0));
                                } catch (PackageManager.NameNotFoundException e) {
                                    Log.e(TAG, "Error getting packageInfo for " + name);
                                }
                                publishProgress((i++) + " : " + name);
                            }
                        }
                        if (isCancelled()) {
                            Log.i(TAG, "Task Cancelled");
                            break;
                        }
                    }
                    break;
                }

                case CATEGORY_SOCIAL : {
                    appInfoList = mainpm.getInstalledApplications(0);
                    int i = 0;
                    String name;
                    for (ApplicationInfo info : appInfoList) {
                        if (info.category == ApplicationInfo.CATEGORY_SOCIAL) {
                            name = info.loadLabel(mainpm).toString();
                            if (name.toLowerCase().contains(str.toLowerCase())) {
                                try {
                                    filtered_apps_list.add(mainpm.getPackageInfo(info.packageName, 0));
                                } catch (PackageManager.NameNotFoundException e) {
                                    Log.e(TAG, "Error getting packageInfo for " + name);
                                }
                                publishProgress((i++) + " : " + name);
                            }
                        }
                        if (isCancelled()) {
                            Log.i(TAG, "Task Cancelled");
                            break;
                        }
                    }
                    break;
                }

                case CATEGORY_MAPS : {
                    appInfoList = mainpm.getInstalledApplications(0);
                    int i = 0;
                    String name;
                    for (ApplicationInfo info : appInfoList) {
                        if (info.category == ApplicationInfo.CATEGORY_MAPS) {
                            name = info.loadLabel(mainpm).toString();
                            if (name.toLowerCase().contains(str.toLowerCase())) {
                                try {
                                    filtered_apps_list.add(mainpm.getPackageInfo(info.packageName, 0));
                                } catch (PackageManager.NameNotFoundException e) {
                                    Log.e(TAG, "Error getting packageInfo for " + name);
                                }
                                publishProgress((i++) + " : " + name);
                            }
                        }
                        if (isCancelled()) {
                            Log.i(TAG, "Task Cancelled");
                            break;
                        }
                    }
                    break;
                }

                case CATEGORY_UNDEFINED : {
                    appInfoList = mainpm.getInstalledApplications(0);
                    int i = 0;
                    String name;
                    for (ApplicationInfo info : appInfoList) {
                        if (info.category == ApplicationInfo.CATEGORY_UNDEFINED) {
                            name = info.loadLabel(mainpm).toString();
                            if (name.toLowerCase().contains(str.toLowerCase())) {
                                try {
                                    filtered_apps_list.add(mainpm.getPackageInfo(info.packageName, 0));
                                } catch (PackageManager.NameNotFoundException e) {
                                    Log.e(TAG, "Error getting packageInfo for " + name);
                                }
                                publishProgress((i++) + " : " + name);
                            }
                        }
                        if (isCancelled()) {
                            Log.i(TAG, "Task Cancelled");
                            break;
                        }
                    }
                    break;
                }
                
            }


        }

        @Override
        protected void onPostExecute(String s) {
            Log.i(TAG, "in AsyncTask OnPostExecute :");
            //  progress_percent.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);

            SortAppList();

            showMsgInTextView("");
            showMsg(toast_msg, "in onPostExecute : " + filtered_apps_list.size() + " apps in " + ((System.currentTimeMillis() - initFragmentTime) / 1000) + "s", false);
            Toast.makeText(activity, filtered_apps_list.size() + " Apps Loaded in " + ((System.currentTimeMillis() - initFragmentTime) / 1000) + "s", Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Log.i(TAG, "in AsyncTask OnCancelled :");
            app_listview.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }

    }

    private void SortAppList() {
        Comparator<PackageInfo> app_name_comparator = (PackageInfo p1, PackageInfo p2) -> p1.applicationInfo.loadLabel(mainpm).toString().compareTo(p2.applicationInfo.loadLabel(mainpm).toString());
        Comparator<PackageInfo> file_size_comparator = (PackageInfo p1, PackageInfo p2) -> Long.compare(new File(p1.applicationInfo.sourceDir).length(), new File(p2.applicationInfo.sourceDir).length());
        Comparator<PackageInfo> app_install_time_comparator = (PackageInfo p1, PackageInfo p2) -> Long.compare(p1.firstInstallTime, p2.firstInstallTime);
        Comparator<PackageInfo> app_update_time_comparator = (PackageInfo p1, PackageInfo p2) -> Long.compare(p1.lastUpdateTime, p2.lastUpdateTime);

        Log.i(TAG, " In Sorting Method : sort by = " + sort_apps_by);

        sort_apps_by = sharedPrefAppSettings.getString(key_sorting, sort_apps_by_name);
        order_apps_by = sharedPrefAppSettings.getString(key_order_by,order_apps_increasing);

        switch (sort_apps_by) {
            case sort_apps_by_name: {
                Collections.sort(filtered_apps_list, app_name_comparator);
                if (order_apps_by.equals(order_apps_decreasing)) {
                    Collections.reverse(filtered_apps_list);
                }
                Log.i(TAG, "in sort_apps_by_name");
                break;
            }

            case sort_apps_by_install_time: {
                Collections.sort(filtered_apps_list, app_install_time_comparator);
                if (order_apps_by.equals(order_apps_decreasing)) {
                    Collections.reverse(filtered_apps_list);
                }
                Log.i(TAG, "in sort_by_date");
                break;
            }

            case sort_apps_by_update_time: {
                Collections.sort(filtered_apps_list, app_update_time_comparator);
                if (order_apps_by.equals(order_apps_decreasing)) {
                    Collections.reverse(filtered_apps_list);
                }
                Log.i(TAG, "in sort_by_date");
                break;
            }

            case sort_apps_by_size: {
                Collections.sort(filtered_apps_list, file_size_comparator);
                if (order_apps_by.equals(order_apps_decreasing)) {
                    Collections.reverse(filtered_apps_list);
                }
                Log.i(TAG, "in sort_apps_by_size");
                break;
            }

            default: {
                Collections.sort(filtered_apps_list, app_name_comparator);
                break;
            }
        }

        adapter = new AdapterAppList(mainpm, filtered_apps_list, getActivity());
        app_listview.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        setLabelTextMsg("Total Apps : " + filtered_apps_list.size());
    }

    private void setLabelTextMsg(String str) {
        label_msgbox.setText(str);
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}


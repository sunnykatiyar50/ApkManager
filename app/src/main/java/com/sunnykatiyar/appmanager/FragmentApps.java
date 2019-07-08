package com.sunnykatiyar.appmanager;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ClipboardManager;
import android.content.Context;
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
import static com.sunnykatiyar.appmanager.ActivityMain.prefEditorAppList;
import static com.sunnykatiyar.appmanager.ActivityMain.sharedPrefAppList;
import static com.sunnykatiyar.appmanager.ActivityMain.toolbar_main;

public class FragmentApps extends Fragment {

    public static Context appContext;
    public static TextView progress_percent ;
    public static ProgressBar progressBar ;
    public static PackageManager  mainpm;
    public static List<PackageInfo> launchable_apps_list;
    public static List<PackageInfo> applist;
    public static RecyclerView app_listview;
    public static Long initFragmentTime;
    public static ActivityManager activityManager;
    public static ClipboardManager clipboardManager;
    public static AdapterAppList adapter;

    MyAsyncTask myAsyncTask;
    DividerItemDecoration mDividerItemDecoration;
    final static String TAG ="APPLIST_FRAGMENT : ";
    public static TextView label_msgbox;
    SearchView searchView;
    MenuItem search_menuItem;

    static final String sort_apps_by_name = "SORT_BY_NAME";
    static final String sort_apps_by_update_time = "SORT_BY_UPDATE_TIME";
    static final String sort_apps_by_install_time = "SORT_BY_INSTALL_TIME";
    static final String sort_apps_by_size = "SORT_BY_SIZE";

    static final String order_apps_decreasing = "ORDER_INCREASING";
    static final String order_apps_increasing = "ORDER_DECREASING";

    public static String sort_apps_by;
    public static String order_apps_by;
    public String search_query="";

    public static final String key_sorting = "SORT_APPS_BY";
    public static final String key_order_by = "ORDER_APPS_BY";

    final String toast_msg = "show_as_toast";
    final String textview_msg = "show_in_textview";
    final String log_only = "show_in_log_only";
    Activity context = getActivity();
    String value_sorting;
    String  value_order_by;
    LinearLayoutManager llm;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        initFragmentTime = System.currentTimeMillis();
        Log.e("in ApplistFragment : ","in onCreateView ");
        setRetainInstance(true);
        View v = inflater.inflate(R.layout.fragment_app_manager, container, false);
        progressBar = v.findViewById(R.id.progress_bar);
        progress_percent = v.findViewById(R.id.progress_int);
        app_listview =  v.findViewById(R.id.app_recycleview_list);
        label_msgbox = v.findViewById(R.id.label_applist_msgbox);
        mainpm = getContext().getPackageManager();

        sort_apps_by = sharedPrefAppList.getString(key_sorting, sort_apps_by_name);
        appContext = getContext();

        Log.e("in ApplistFragment : ","getInstalled Packages in "+((System.currentTimeMillis()-initFragmentTime)/1000)+"s");


        clipboardManager = (ClipboardManager) getContext().getSystemService(CLIPBOARD_SERVICE);
        activityManager= (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);

        llm = new LinearLayoutManager(appContext);
        llm.setOrientation(RecyclerView.VERTICAL);
        app_listview.setLayoutManager(llm);

        mDividerItemDecoration = new DividerItemDecoration(appContext,llm.getOrientation());
        app_listview.addItemDecoration(mDividerItemDecoration);

        if(myAsyncTask==null){
            launchable_apps_list = new ArrayList<>();
            myAsyncTask = new MyAsyncTask(getActivity(), search_query);
            myAsyncTask.execute();
            adapter = new AdapterAppList(mainpm, launchable_apps_list, getActivity());
         //   app_listview.setAdapter(adapter);
        //    adapter.notifyDataSetChanged();
            showMsg(toast_msg,"Launching asynctask : query = "+search_query,false);
        }else{
            showMsg(toast_msg,"Using old aynctask for applist : query = "+search_query,false);
            adapter = new AdapterAppList(mainpm, launchable_apps_list, getActivity());
            app_listview.setAdapter(adapter);
            adapter.notifyDataSetChanged();

        }

        showMsgInTextView(true,"");
        //MyAsyncTaskLoader m = new MyAsyncTaskLoader(getActivity(),"Fetching Apps");
        registerForContextMenu(app_listview);
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.i(TAG,"in On Create Options");

        inflater.inflate(R.menu.menu_applist, menu);

        search_menuItem = menu.findItem(R.id.search_item);
        search_menuItem.setVisible(true);

        searchView = (SearchView) search_menuItem.getActionView();
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
            }});

//        searchView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View view, boolean b) {
//                Log.e("in ActivityMain : ", " OnFocusChanged ");
//                if(b) toolbar_main.setTitle("");
//            }
//
//        });

        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toolbar_main.setTitle("");
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                Log.e("in ActivityMain : ", " in setOnCloseListener ");
                adapter.notifyDataSetChanged();
                toolbar_main.setTitle(R.string.app_name);
                return false;
            }
        });

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        Log.i(TAG," onPrepareOptionsMenu : ");

//-----------------------LOADING "SORT BY" FROM SHARED PREFERENCES---------------------------------------
        value_sorting = sharedPrefAppList.getString(key_sorting, sort_apps_by_name);
        Log.i(TAG,"Sorting Setting in Shared Preferences: "+value_sorting);

        if(value_sorting.equals(sort_apps_by_name)){
            menu.findItem(R.id.menuitem_sortappsby_name).setChecked(true);
            sort_apps_by = sort_apps_by_name;
            Log.i(TAG," inside equating sort_apps_by_name : ");
        }
        else if(value_sorting.equals(sort_apps_by_update_time)){
            menu.findItem(R.id.menuitem_sortappsby_updatetime).setChecked(true);
            sort_apps_by = sort_apps_by_update_time;
            Log.i(TAG," inside equating sort_by_update_time : ");
        }
        else if(value_sorting.equals(sort_apps_by_install_time)){
            menu.findItem(R.id.menuitem_sortappsby_installtime).setChecked(true);
            sort_apps_by = sort_apps_by_update_time;
            Log.i(TAG," inside equating sort_by_install_time : ");
        }
        else if(value_sorting.equals(sort_apps_by_size)){
            menu.findItem(R.id.menuitem_sortappsby_size).setChecked(true);
            sort_apps_by = sort_apps_by_size;
            Log.i(TAG," inside equating sort_apps_by_size : ");
        }
        Log.i(TAG," Value of sort_apks_by : " + sort_apps_by);

//--------------------------------LOADING "ORDER BY" FROM SHARED PREFERENCES---------------------------------------

        value_order_by = sharedPrefAppList.getString(key_order_by, order_apps_increasing);
        Log.i(TAG," Found Ordering Settings in SHARED PREFERENCES: "+ value_order_by);

        if(value_order_by.equals(order_apps_decreasing)){
            menu.findItem(R.id.menuitem_apps_decreasing).setChecked(true);
            order_apps_by = order_apps_decreasing;
        }
        else if(value_order_by.equals(order_apps_increasing)){
            menu.findItem(R.id.menuitem_apps_increasing).setChecked(true);
            order_apps_by = order_apps_increasing;
        }
        Log.i(TAG," Value of order by : " + order_apps_by);

//----------------------------------------------------------------------------------------------------------------------
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch(id)
        {
            case R.id.search_item:{
                Log.e("in ActivityMain : ", "Search Menu Button Clicked");
                break;
            }
            case R.id.reload:{
                myAsyncTask = new MyAsyncTask(getActivity(), search_query);
                myAsyncTask.execute();
                adapter = new AdapterAppList(mainpm, launchable_apps_list, getActivity());
                app_listview.setAdapter(adapter);
                break;
            }

            case R.id.menuitem_sortbyname : {
                item.setChecked(true);
                sort_apps_by = sort_apps_by_name;
                Log.i(TAG,"Clicked sort by name");
                prefEditorAppList.putString(key_sorting, sort_apps_by).commit();
                SortAppList();
                break;
            }

            case R.id.menuitem_sortappsby_installtime: {
                item.setChecked(true);
                Log.i(TAG,"Clicked sort by size");
                sort_apps_by = sort_apps_by_update_time;
                prefEditorAppList.putString(key_sorting, sort_apps_by).commit();
                SortAppList();
                break;
            }

            case R.id.menuitem_sortappsby_updatetime: {
                item.setChecked(true);
                Log.i(TAG,"Clicked sort by size");
                sort_apps_by = sort_apps_by_update_time;
                prefEditorAppList.putString(key_sorting, sort_apps_by).commit();
                SortAppList();
                break;
            }

            case R.id.menuitem_sortappsby_size:{
                item.setChecked(true);
                Log.i(TAG,"Clicked sort by size");
                sort_apps_by = sort_apps_by_size;
                prefEditorAppList.putString(key_sorting, sort_apps_by).commit();
                SortAppList();
                break;
            }

            //-----------------------------------INVERSE SORTING-------------------------------------
            case R.id.menuitem_apps_decreasing:{
                item.setChecked(true);
                order_apps_by = order_apps_decreasing;
                prefEditorAppList.putString(key_order_by, order_apps_by).commit();
                SortAppList();
                break;
            }

            case R.id.menuitem_apps_increasing:{
                item.setChecked(true);
                order_apps_by = order_apps_increasing;
                prefEditorAppList.putString(key_order_by, order_apps_by).commit();
                SortAppList();
                break;
            }
        }
        return true;
    }

    private void showMsg(String display_as, String msg, boolean isempty){

        if(display_as.equals(toast_msg)){
            Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
        }
        else if(display_as.equals(textview_msg)) {
            showMsgInTextView(isempty,msg);
        }else if(display_as.equals(log_only)){

        }

        Log.i(TAG, msg);
    }

    public void showMsgInTextView(boolean empty_string, String str) {
        if(empty_string) {
            String default_string = "Total Apps : " + launchable_apps_list.size();
            label_msgbox.setText(default_string);
        }else{
            label_msgbox.setText(str);
        }
    }

    class MyAsyncTask extends AsyncTask<Void, String , String> {

        Activity activity;
        String search_filter;
        final String TAG = "MYASYNCTASK ACTIVITY";

        public MyAsyncTask(Activity a,String str){
            this.activity = a;
            this.search_filter = str;
        }

        @Override
        protected void onPreExecute() {
            Log.e("in MyAsyncTask :","in onPreExecute : "+((System.currentTimeMillis() - initFragmentTime) / 1000) + "s");
            progressBar.setVisibility(View.VISIBLE);
//            progress_percent.setVisibility(View.VISIBLE);
            applist = new ArrayList<>();
            launchable_apps_list = new ArrayList<>();
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

        public void LoadAppList(String str){
            Log.e("in MyAsyncTask :", "AppLoadList : " + ((System.currentTimeMillis() - initFragmentTime) / 1000) + "s");
            applist = new ArrayList<>();
            launchable_apps_list = new ArrayList<>();
            applist = mainpm.getInstalledPackages(0);
            int i=0;
            String name ;
            for (PackageInfo p : applist) {
                if (mainpm.getLaunchIntentForPackage(p.packageName) != null)
                {
                    name = p.applicationInfo.loadLabel(mainpm).toString();
                    //Log.i(TAG," Matching "+search_filter+" AND "+name+" And i = "+i);
                    if(name.toLowerCase().contains(str.toLowerCase())){
                        //  Log.i(TAG," Matched "+search_filter+" AND "+name);
                        launchable_apps_list.add(p);
                        publishProgress((i++)+" : "+name);
                    }
                }
                if(isCancelled()){
                    Log.i(TAG,"Task Cancelled");
                    break;
                }
            }
        }

        @Override
        protected void onPostExecute(String s) {
            Log.i(TAG,"in AsyncTask OnPostExecute :");
            //  progress_percent.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);

            SortAppList();

            showMsgInTextView(true,"");
            showMsg(toast_msg,"in onPostExecute : "+launchable_apps_list.size()+" apps in "+((System.currentTimeMillis() - initFragmentTime) / 1000) + "s",false);
            Toast.makeText(activity, launchable_apps_list.size() + " Apps Loaded in " + ((System.currentTimeMillis() - initFragmentTime) / 1000) + "s", Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Log.i(TAG,"in AsyncTask OnCancelled :");
            app_listview.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }

    }

    public void SortAppList() {
        Comparator<PackageInfo> app_name_comparator = (PackageInfo p1, PackageInfo p2) -> p1.applicationInfo.loadLabel(mainpm).toString().compareTo(p2.applicationInfo.loadLabel(mainpm).toString());
        Comparator<PackageInfo> file_size_comparator = (PackageInfo p1, PackageInfo p2) -> Long.compare(new File(p1.applicationInfo.sourceDir).length(),new File(p2.applicationInfo.sourceDir).length());
        Comparator<PackageInfo> app_install_time_comparator = (PackageInfo p1, PackageInfo p2) -> Long.compare(p1.firstInstallTime,p2.firstInstallTime);
        Comparator<PackageInfo> app_update_time_comparator = (PackageInfo p1, PackageInfo p2) -> Long.compare(p1.lastUpdateTime,p2.lastUpdateTime);

        Log.i(TAG, " In Sorting Method : sort by = " + sort_apps_by);

        switch(sort_apps_by) {
            case sort_apps_by_name: {
                Collections.sort(launchable_apps_list, app_name_comparator);
                if (order_apps_by == order_apps_decreasing) {
                    Collections.reverse(launchable_apps_list);
                }
                Log.i(TAG, "in sort_apps_by_name");
                break;
            }

            case sort_apps_by_install_time: {
                Collections.sort(launchable_apps_list, app_install_time_comparator);
                if (order_apps_by == order_apps_decreasing) {
                    Collections.reverse(launchable_apps_list);
                }
                Log.i(TAG, "in sort_by_date");
                break;
            }

            case sort_apps_by_update_time: {
                Collections.sort(launchable_apps_list, app_update_time_comparator);
                if (order_apps_by == order_apps_decreasing) {
                    Collections.reverse(launchable_apps_list);
                }
                Log.i(TAG, "in sort_by_date");
                break;
            }

            case sort_apps_by_size: {
                Collections.sort(launchable_apps_list, file_size_comparator);
                if (order_apps_by == order_apps_decreasing) {
                    Collections.reverse(launchable_apps_list);
                }
                Log.i(TAG, "in sort_apps_by_size");
                break;
            }

            default: {
                Collections.sort(launchable_apps_list, app_name_comparator);
                break;
            }
        }

        adapter = new AdapterAppList(mainpm, launchable_apps_list, getActivity());
        app_listview.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        setLabelTextMsg("Total Apps : "+launchable_apps_list.size());
    }

    public void setLabelTextMsg(String str){
            label_msgbox.setText(str);
    }


    @Override
    public void onResume() {
        super.onResume();
    }
}


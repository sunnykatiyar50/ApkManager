package com.sunnykatiyar.AppManager;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.content.Context.CLIPBOARD_SERVICE;
import static com.sunnykatiyar.AppManager.AppListActivity.notimgr;
import static com.sunnykatiyar.AppManager.AppListActivity.order_by;
import static com.sunnykatiyar.AppManager.AppListActivity.order_decreasing;
import static com.sunnykatiyar.AppManager.AppListActivity.search_query;
import static com.sunnykatiyar.AppManager.AppListActivity.sort_by;
import static com.sunnykatiyar.AppManager.AppListActivity.sort_by_install_time;
import static com.sunnykatiyar.AppManager.AppListActivity.sort_by_size;
import static com.sunnykatiyar.AppManager.AppListActivity.sort_by_update_time;


public class AppListFragment extends Fragment {

    public static Context appContext;
    public static TextView progress_percent ;
    public static ProgressBar progressBar ;
    public static PackageManager  mainpm;
    public static List<PackageInfo> launchable_apps_list;
    public static List<PackageInfo> applist;
    public static RecyclerView app_listview;
//    public static PackageInfo clicked_pkg;
//    public static String clicked_pkg_label;
    public static Long initFragmentTime;
    public static ActivityManager activityManager;
    public static ClipboardManager clipboardManager;
    public static CustomAppListAdapter adapter;
    public AdapterView.AdapterContextMenuInfo contextMenuInfo;
    AppMenu contextmenu;
    public int item_index;
    MyAsyncTask myAsyncTask;
    DividerItemDecoration mDividerItemDecoration;
    final static String TAG ="APPLIST_FRAGMENT : ";
    public static TextView label_msgbox;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        initFragmentTime = System.currentTimeMillis();
        Log.e("in ApplistFragment : ","in onCreateView ");

        View v = inflater.inflate(R.layout.fragment_app_list, container, false);
        progressBar = v.findViewById(R.id.progress_bar);
        progress_percent = v.findViewById(R.id.progress_int);
        app_listview =  v.findViewById(R.id.app_recycleview_list);
        appContext = getContext();
        clipboardManager = (ClipboardManager) getContext().getSystemService(CLIPBOARD_SERVICE);
        activityManager= (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        label_msgbox = v.findViewById(R.id.label_applist_msgbox);
        mainpm = appContext.getPackageManager();
        Log.e("in ApplistFragment : ","getInstalled Packages in "+((System.currentTimeMillis()-initFragmentTime)/1000)+"s");

        if(myAsyncTask!=null){
            myAsyncTask.cancel(true);
            myAsyncTask=null;
        }

        Log.i(TAG,"Launching Fragment : query = "+search_query);
        myAsyncTask = new MyAsyncTask(getActivity(), AppListActivity.search_query);
        //launchable_apps_list = new ArrayList<>();
        myAsyncTask.execute();

        LinearLayoutManager llm = new LinearLayoutManager(appContext);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        app_listview.setLayoutManager(llm);

        mDividerItemDecoration = new DividerItemDecoration(appContext,llm.getOrientation());
        app_listview.addItemDecoration(mDividerItemDecoration);

      //MyAsyncTaskLoader m = new MyAsyncTaskLoader(getActivity(),"Fetching Apps");
        registerForContextMenu(app_listview);

        adapter = new CustomAppListAdapter(mainpm, launchable_apps_list, getActivity());
//        app_listview.setAdapter(adapter);
//        adapter.notifyDataSetChanged();

        return v;
    }

//    @Override
//    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//
//        getActivity().getMenuInflater().inflate(R.menu.menu_app_details,menu);
//        /*
//        super.onCreateContextMenu(menu, v, menuInfo);
//        menu.add(launch_app);
//        menu.add(extract_apk);
//        menu.add(open_market);
//        menu.add(open_sysAppInfo);
//        menu.add(uninstall_app);
//        menu.add(kill_app);
//        SubMenu copy_menu = menu.addSubMenu(copy);
//        copy_menu.add(copy_pname);
//        copy_menu.add(copy_name);
//        copy_menu.add(copy_link);
//        copy_menu.add(share);
//        AdapterView.AdapterContextMenuInfo contextMenuInfo = (AdapterView.AdapterContextMenuInfo) menu.getItem(1).getMenuInfo();
//        item_index=contextMenuInfo.position;
//          */  Log.e("Created Menu","onCreateMenu");
//    }
//
//    @Override
//    public boolean onContextItemSelected(MenuItem item) {
//        Log.e("Context Menu","onItemSelected");
//        Toast t = new Toast(getContext());
//        PackageInfo p;
//        Context menu_context = getContext();
//
//        int itemname = item.getItemId();
//
//        contextMenuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
//
//        if(contextMenuInfo!=null){
//                p= launchable_apps_list.get(contextMenuInfo.position); }
//        else {
//                p= launchable_apps_list.get(item_index);
//        }
//
//        String applabel = p.applicationInfo.loadLabel(mainpm).toString();
//
//        contextmenu = new AppMenu(itemname,applabel,p,menu_context);
//        contextmenu.PerAppMenu();
//
//        return true;
//    }

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
            //progressBar.setMax(launchable_apps_list.size());
            progress_percent.setVisibility(View.VISIBLE);
            progress_percent.setText("Fetching Apps...");
            applist = new ArrayList<>();
            launchable_apps_list = new ArrayList<>();
        }

        @Override
        protected String doInBackground(Void... voids) {
           Log.e("in MyAsyncTask :", "in doInBackground : " + ((System.currentTimeMillis() - initFragmentTime) / 1000) + "s");
           LoadAppList(search_filter);
           Log.e("in AppListFragment : ","Launchable Applist in "+((System.currentTimeMillis()-initFragmentTime)/1000)+"s");
           Log.e("in MyAsyncTask :", "out doInBackground : " + ((System.currentTimeMillis() - initFragmentTime) / 1000) + "s");
           return null;
        }

        @Override
        protected void onProgressUpdate(String... strings) {
            setLabelTextMsg(strings[0]);
            //   Log.e("in onProgressUpdate :",((System.currentTimeMillis()-initFragmentTime)/1000)+"searchAsyncTask");
            //   progress_percent.setText(values[0] / 4 + "/" + applist.size());
            //   progressBar.setProgress(values[0] / 4);
        }

        @Override
        protected void onPostExecute(String s) {
            progress_percent.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            SortAppList();
            AppListActivity.search_menuItem.setVisible(true);

            //   NOTIFICATION Implementation------------------------

            Notification.Builder myNotiBuilder = (Notification.Builder) new Notification.Builder(activity)
                    .setSmallIcon(R.drawable.ic_action_sort_1)
                    .setContentTitle("App Explorer")
                    .setContentText("AppList Fetching Complete");
            Intent i = new Intent(activity,AppListActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent p = PendingIntent.getActivity(activity,0,i,PendingIntent.FLAG_UPDATE_CURRENT);
            myNotiBuilder.setContentIntent(p);

            notimgr.notify(000101,myNotiBuilder.build());

            Log.e("in MyAsyncTask :", "in onPostExecute : "+launchable_apps_list.size()+" apps in "+((System.currentTimeMillis() - initFragmentTime) / 1000) + "s");
            Toast.makeText(activity, launchable_apps_list.size() + " Apps Loaded in " + ((System.currentTimeMillis() - initFragmentTime) / 1000) + "s", Toast.LENGTH_LONG).show();
        }

        public void LoadAppList(String str){

            applist = mainpm.getInstalledPackages(0);
            int i=0;
            String name ;
            for (PackageInfo p : applist) {
                if (mainpm.getLaunchIntentForPackage(p.packageName) != null){
                    name = p.applicationInfo.loadLabel(mainpm).toString();
                    //Log.i(TAG," Matching "+search_filter+" AND "+name+" And i = "+i);
                    if(name.toLowerCase().contains(search_filter.toLowerCase())){
                      //  Log.i(TAG," Matched "+search_filter+" AND "+name);
                        launchable_apps_list.add(p);
                        publishProgress((i++)+" : "+name);
                    }
                }
            }
        }
    }

    public void setLabelTextMsg(String str){
        label_msgbox.setText(str);
    }

    public void SortAppList() {
        Comparator<PackageInfo> app_name_comparator = (PackageInfo p1, PackageInfo p2) -> p1.applicationInfo.loadLabel(mainpm).toString().compareTo(p2.applicationInfo.loadLabel(mainpm).toString());
        Comparator<PackageInfo> file_size_comparator = (PackageInfo p1, PackageInfo p2) -> Long.compare(new File(p1.applicationInfo.sourceDir).length(),new File(p2.applicationInfo.sourceDir).length());
        Comparator<PackageInfo> app_install_time_comparator = (PackageInfo p1, PackageInfo p2) -> Long.compare(p1.firstInstallTime,p2.firstInstallTime);
        Comparator<PackageInfo> app_update_time_comparator = (PackageInfo p1, PackageInfo p2) -> Long.compare(p1.lastUpdateTime,p2.lastUpdateTime);

        Log.i(TAG, " In Sorting Method : sort by = " + sort_by);

        switch (sort_by) {
            case AppListActivity.sort_by_name: {
                Collections.sort(launchable_apps_list, app_name_comparator);
                if (order_by == order_decreasing) {
                    Collections.reverse(launchable_apps_list);
                }
                Log.i(TAG, "in sort_by_name");
                break;
            }

            case sort_by_install_time: {
                Collections.sort(launchable_apps_list, app_install_time_comparator);
                if (order_by == order_decreasing) {
                    Collections.reverse(launchable_apps_list);
                }
                Log.i(TAG, "in sort_by_date");
                break;
            }

            case sort_by_update_time: {
                Collections.sort(launchable_apps_list, app_update_time_comparator);
                if (order_by == order_decreasing) {
                    Collections.reverse(launchable_apps_list);
                }
                Log.i(TAG, "in sort_by_date");
                break;
            }

            case sort_by_size: {
                Collections.sort(launchable_apps_list, file_size_comparator);
                if (order_by == order_decreasing) {
                    Collections.reverse(launchable_apps_list);
                }
                Log.i(TAG, "in sort_by_size");
                break;
            }

            default: {
                Collections.sort(launchable_apps_list, app_name_comparator);
                break;
            }
        }

        adapter = new CustomAppListAdapter(mainpm, launchable_apps_list, getActivity());
        app_listview.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        setLabelTextMsg("Total Apps : "+launchable_apps_list.size());
    }


}


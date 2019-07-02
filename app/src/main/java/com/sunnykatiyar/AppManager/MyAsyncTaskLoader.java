package com.sunnykatiyar.AppManager;

import android.app.Activity;
import android.content.AsyncTaskLoader;
import android.content.pm.PackageInfo;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.Collections;
import java.util.Comparator;

import static com.sunnykatiyar.AppManager.MainActivity.search_menuItem;
import static com.sunnykatiyar.AppManager.AppListFragment.adapter;
import static com.sunnykatiyar.AppManager.AppListFragment.app_listview;
import static com.sunnykatiyar.AppManager.AppListFragment.applist;
import static com.sunnykatiyar.AppManager.AppListFragment.initFragmentTime;
import static com.sunnykatiyar.AppManager.AppListFragment.launchable_apps_list;
import static com.sunnykatiyar.AppManager.AppListFragment.mainpm;
import static com.sunnykatiyar.AppManager.AppListFragment.progressBar;
import static com.sunnykatiyar.AppManager.AppListFragment.progress_percent;

/**
 * Created by Sunny Katiyar on 06-04-2017.
 */

public class MyAsyncTaskLoader extends AsyncTaskLoader<Void> {

    Activity context;
    String Loading_text;

    public MyAsyncTaskLoader(Activity a,String s){
        super(a);
        Log.e("MyAsyncTaskLoader :","Constructor : "+((System.currentTimeMillis() - initFragmentTime) / 1000) + "s");
        this.context=a;
        this.Loading_text=s;
        progressBar.setVisibility(View.VISIBLE);
        progress_percent.setVisibility(View.VISIBLE);
        progress_percent.setText("Fetching Apps...");
    }

    @Override
    public Void loadInBackground() {
        Log.e("in MyAsyncTask :", "in doInBackground : " + ((System.currentTimeMillis() - initFragmentTime) / 1000) + "s");

        for (PackageInfo p : applist) {
            if (mainpm.getLaunchIntentForPackage(p.packageName) != null)
                launchable_apps_list.add(p);
        }
        Log.e("in AppListFragment : ","Launchable Applist in "+((System.currentTimeMillis()-initFragmentTime)/1000)+"s");

            Collections.sort(launchable_apps_list, new Comparator<PackageInfo>() {
                @Override
                public int compare(PackageInfo p1, PackageInfo t1) {
                    return p1.applicationInfo.loadLabel(mainpm).toString().compareTo(t1.applicationInfo.loadLabel(mainpm).toString());
                }
            });

            Log.e("in MyAsyncTask :", "out doInBackground : " + ((System.currentTimeMillis() - initFragmentTime) / 1000) + "s");
        onLoadComplete();
        return null;
    }

    public void onLoadComplete(){
        progress_percent.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        search_menuItem.setVisible(true);
        app_listview.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        Log.e("in MyAsyncTask :", "in onPostExecute : "+((System.currentTimeMillis() - initFragmentTime) / 1000) + "s");
        Toast.makeText(context, launchable_apps_list.size() + " Apps Loaded in " + ((System.currentTimeMillis() - initFragmentTime) / 1000) + "s", Toast.LENGTH_LONG).show();
    }
}

package com.sunnykatiyar.AppManager;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static com.sunnykatiyar.AppManager.AppListFragment.mainpm;
import static com.sunnykatiyar.AppManager.CustomAppListAdapter.clicked_pkg_label;

public class AppDetailsActivity extends AppCompatActivity {
    ImageView appInfo_icon;
    TextView appInfo_appname;
    TextView app_version;
    TextView appInfo_pkgname;
    TextView install_date;
    PackageManager appInfo_pm = mainpm;
    PackageInfo clicked_pkg = CustomAppListAdapter.clicked_pkg;
    protected static Toolbar toolbar;
    ExpandableListView expandableListView;
    HashMap<String, List<String>> Detailed_Exp_List;
    List<String> headers;
    ExpandableListAdapter exp_adapter;
    AppMenu optionmenu;
    private static final String TAG = "APPDETAILS ACTIVITY : ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_details);
        toolbar = findViewById(R.id.toolbar_app_details);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(clicked_pkg_label);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        appInfo_icon = findViewById(R.id.app_info_icon);
        appInfo_appname =  findViewById(R.id.app_name);
        app_version = findViewById(R.id.version_num);
        appInfo_pkgname = findViewById(R.id.pkg_name);
        install_date = findViewById(R.id.install_date);

        install_date.setText(new SimpleDateFormat().format(clicked_pkg.firstInstallTime));
        appInfo_pkgname.setText(clicked_pkg.packageName);
        app_version.setText(clicked_pkg.versionName);
        appInfo_icon.setImageDrawable(appInfo_pm.getApplicationIcon(clicked_pkg.applicationInfo));
        appInfo_appname.setText(appInfo_pm.getApplicationLabel(clicked_pkg.applicationInfo));

        SetAppDetails appDetails = new SetAppDetails(mainpm,clicked_pkg);
        expandableListView = findViewById(R.id.expandable_list);
        Detailed_Exp_List = appDetails.setListData();
        headers = new ArrayList<>(Detailed_Exp_List.keySet());


        Collections.sort(headers, new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                return s.compareToIgnoreCase(t1);
            }
        });

        exp_adapter = new CustomExpandableListAdapter(this, headers, Detailed_Exp_List);
        expandableListView.setAdapter(exp_adapter);

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Intent activityIntent = new Intent();
                Log.e("in Appdetails","i can detect a childclick");
                //Toast.makeText(v.getContext(),"i can detect a childclick", Toast.LENGTH_SHORT).show();
                String clicked_activity = exp_adapter.getChild(groupPosition,childPosition).toString();
                activityIntent.setComponent(new ComponentName(clicked_pkg.packageName,clicked_activity) );
                if (exp_adapter.getGroup(groupPosition).toString()=="Activities")
                {
                    Toast.makeText(v.getContext(), clicked_activity, Toast.LENGTH_SHORT).show();
                    startActivity(activityIntent);

                }
                return true;
            }
        });

    /*  int headerSize = adapter.getGroupCount();
        for (int i = 0; i < headerSize; i++) {
            if (adapter.getChildrenCount(i) <= 2)
                expandableListView.expandGroup(i);
        }
    */
        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            int LastExpandedGroup = -1;
            @Override
            public void onGroupExpand(int position) {
                if (LastExpandedGroup != -1 && position != LastExpandedGroup && exp_adapter.getChildrenCount(LastExpandedGroup) >= 2) {
                    expandableListView.collapseGroup(LastExpandedGroup);
                }
                LastExpandedGroup = position;
                Toast.makeText(getApplicationContext(), headers.get(position), Toast.LENGTH_SHORT);
            }
        });

        expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int position) {
                Toast.makeText(getApplication(), headers.get(position), Toast.LENGTH_SHORT);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_app_details,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Toast t = new Toast(getApplicationContext());
        Intent i;
        PackageInfo p = clicked_pkg;
        String applabel = (String) p.applicationInfo.loadLabel(mainpm);

        if (item.getItemId()== android.R.id.home)
                finish();
        else{
            optionmenu = new AppMenu(item.getItemId(), applabel, p, getApplicationContext());
            optionmenu.PerAppMenu();
        }
            return true;
 }

}

package com.sunnykatiyar.appmanager;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.sunnykatiyar.appmanager.ui.main.FragmentRootBrowser;
import com.sunnykatiyar.appmanager.ui.main.SectionsPagerAdapter;

public class ActivityRootBrowser extends AppCompatActivity {

    final int menuitem_new_tab = 10227;
    SectionsPagerAdapter sectionsPagerAdapter;
    ViewPager viewPager;
    TabLayout tabs;
    final int MAX = 10;
    FragmentRootBrowser fragArray[] = new FragmentRootBrowser[MAX];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_root_browser);

        Toolbar toolbar = findViewById(R.id.toolbar_rootBrowser);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tabs = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.view_pager);
        sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        viewPager.setAdapter(sectionsPagerAdapter);
        tabs.setupWithViewPager(viewPager);


//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0,menuitem_new_tab,0,"New Tab");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()){
//            case menuitem_new_tab : {
//                tabs.addTab(tabs.newTab());
//            }

            case android.R.id.home: {
                finish();
                break;
            }
        }
        return false;
    }

}
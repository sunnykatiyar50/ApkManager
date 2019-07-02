package com.sunnykatiyar.AppManager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static com.sunnykatiyar.AppManager.AppListFragment.TAG;
import static com.sunnykatiyar.AppManager.AppListFragment.appContext;
import static com.sunnykatiyar.AppManager.AppListFragment.mainpm;
import static com.topjohnwu.superuser.internal.InternalUtils.getContext;

public class CustomAppListAdapter extends RecyclerView.Adapter<CustomAppListViewHolder>{

    private PackageManager pm;
    private List<PackageInfo> myAppList = new ArrayList<>();
    private Activity activity;

    public static PackageInfo clicked_pkg;
    public static String clicked_pkg_label;

    private static final DecimalFormat format = new DecimalFormat("#.##");
    private static final long MB = 1024 * 1024;
    private static final long KB = 1024;
    private static final long GB = 1024 * 1024 * 1024;
    AppMenu appMenu;

    public CustomAppListAdapter(PackageManager pkgmgr, List<PackageInfo> myAppList, Activity activity) {
        this.pm = pkgmgr;
        this.activity = activity;
        this.myAppList = myAppList;
        //this.filteredAppList = myAppList;
    }

    @NonNull
    @Override
    public CustomAppListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater in = LayoutInflater.from(context);
        View view = in.inflate(R.layout.app_list_item,viewGroup,false);
        CustomAppListViewHolder cat = new CustomAppListViewHolder(view);
        return cat;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomAppListViewHolder vholder, int i) {

        PackageInfo pkginfo = this.myAppList.get(i);
        vholder.pkgname.setText(pkginfo.packageName);
        vholder.version.setText("v" + pkginfo.versionName);
        vholder.appname.setText(pm.getApplicationLabel(pkginfo.applicationInfo).toString());
        vholder.applogo.setImageDrawable(pm.getApplicationIcon(pkginfo.applicationInfo));
        vholder.install_date.setText(getTime(pkginfo.lastUpdateTime));
        File f = new File(pkginfo.applicationInfo.sourceDir);
        vholder.app_size.setText(getSize(f));
        vholder.text_extra.setText(String.valueOf(pkginfo.versionCode));

        vholder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clicked_pkg = myAppList.get(i);
                clicked_pkg_label = mainpm.getApplicationLabel(clicked_pkg.applicationInfo).toString();
                Log.i(TAG, "Item clicked :" + clicked_pkg_label);

                Toast.makeText(appContext, clicked_pkg_label, Toast.LENGTH_SHORT).show();
                Intent appInfo = new Intent(activity, AppDetailsActivity.class);
                activity.startActivity(appInfo);
            }
        });

        vholder.itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                // super.onCreateContextMenu(menu, v, menuInfo);
                MenuInflater inflater = activity.getMenuInflater();
                inflater.inflate(R.menu.menu_app_details, menu);
                PackageInfo p = myAppList.get(i);
                ;
                Log.i(TAG, " ITEM CREATE: " + v.getId());
//                Log.i(TAG," ITEM : "+);
            }
        });

        vholder.itemView.setOnContextClickListener(new View.OnContextClickListener() {

            @Override
            public boolean onContextClick(View v) {
                Toast.makeText(appContext, clicked_pkg_label, Toast.LENGTH_SHORT).show();

                Log.e("Context Menu", "onItemSelected");
                PackageInfo p = myAppList.get(i);
                ;
                Context menu_context = getContext();

                String applabel = p.applicationInfo.loadLabel(mainpm).toString();
                Log.i(TAG, " ITEM CLICK: " + v.getId());
                appMenu = new AppMenu(v.getId(), applabel, p, menu_context);
                appMenu.PerAppMenu();
                return false;
            }
        });



    }




    public String getTime(long time){
        DateFormat dateFormat = new SimpleDateFormat("hh:mm a dd-MM-yy");
        String strDate = dateFormat.format(time);
        return strDate;
    }


    public String getSize(File file) {

       // Log.i(TAG,file.getClass().toString());

        if (!file.isFile()) {
            throw new IllegalArgumentException("Expected a file");
        }

        final double length = file.length();

        if(length>GB){
            return format.format(length / GB) + " MB";
        }
        if (length > MB) {
            return format.format(length / MB) + " MB";
        }
        if (length > KB) {
            return format.format(length / KB) + " KB";
        }

        return format.format(length) + "Bytes";
    }

    @Override
    public int getItemCount() {
        if(this.myAppList==null){
            return 0;
        }else
            return this.myAppList.size();
    }

    @Override
    public long getItemId(int i)
    {
        return 0;
    }

}

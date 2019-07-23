package com.sunnykatiyar.skmanager;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

class AdapterExpandableList extends BaseExpandableListAdapter {

    private final Context context;
    private final List<String> headers;
    private final HashMap<String, List<String>> list;
    List component = new ArrayList();
    PackageInfo pkg ;
    ClassSetAppDetails appDetails;
    final String TAG = "MYAPP : EXLIST_ADAPTER : ";
    AdapterExpandableList adapter = this;

    public AdapterExpandableList(Context c,List<String> headers, HashMap<String,List<String>> list, PackageInfo pkg, ClassSetAppDetails appDetails)
    {
        this.context=c;
        this.pkg = pkg;
        this.list = list;
        this.headers = headers;
        this.appDetails = appDetails;
        component.add("Receivers");
        component.add("Activities");
        component.add("Providers");
        component.add("Services");
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        String property_name= (String) getGroup(i);
        if(view==null){
            view=inflater.inflate(R.layout.listitem_expandable_list_header,null);
        }
        TextView group_view= view.findViewById(R.id.ex_list_item_header);
        group_view.setText(property_name);

        return view;
    }

    @Override
    public View getChildView(final int i, final int i1, boolean b, View view, ViewGroup viewGroup) {
        final String property_fetched = (String) getChild(i, i1);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(view == null){
            view = inflater.inflate(R.layout.listitem_expandable_list,null);
        }

        TextView propertyView= null;
        propertyView = view.findViewById(R.id.ex_list_item_item);
        propertyView.setText(property_fetched);

        if(headers.get(i).contains("Activities")
                ||headers.get(i).contains("Receiver")
                ||headers.get(i).contains("Service")
                ||headers.get(i).contains("Provider")){
          //  Log.i(TAG,"Drawing Components");
            if(isComponentEnabled(context.getPackageManager(), pkg.packageName, property_fetched)){
                propertyView.setTextColor(context.getColor(R.color.light_green));
                propertyView.setPaintFlags(Paint.FAKE_BOLD_TEXT_FLAG);

            }else{
                propertyView.setTextColor(context.getColor(R.color.light_red));
                propertyView.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            }
        }

        if(headers.get(i).contains("Permission")){
            //Log.i(TAG,"Drawing Permissions");
            if(appDetails.permissions_granted.contains(property_fetched)){
                propertyView.setTextColor(context.getColor(R.color.light_green));
                propertyView.setPaintFlags(Paint.FAKE_BOLD_TEXT_FLAG);
            }else{
                propertyView.setTextColor(context.getColor(R.color.light_red));
                propertyView.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            }
        }

        return view;
    }

    @Override
    public int getGroupCount() {
        return list.size();
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
    
    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public int getChildrenCount(int i) {
     int child_num=list.get(headers.get(i)).size();
                return child_num;
    }

    @Override
    public Object getGroup(int i) {
        return this.headers.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return this.list.get(this.headers.get(i)).get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1)
    {
        return i1;
    }

    public static boolean isComponentEnabled(PackageManager pm, String pkgName, String clsName) {
        ComponentName componentName = new ComponentName(pkgName, clsName);
        int componentEnabledSetting = pm.getComponentEnabledSetting(componentName);

        switch (componentEnabledSetting) {

            case PackageManager.COMPONENT_ENABLED_STATE_DISABLED:
                return false;
            case PackageManager.COMPONENT_ENABLED_STATE_ENABLED:
                return true;
            case PackageManager.COMPONENT_ENABLED_STATE_DEFAULT:

            default:
                // We need to get the application info to get the component's default state
                try {
                    PackageInfo packageInfo = pm.getPackageInfo(pkgName, PackageManager.GET_ACTIVITIES
                            | PackageManager.GET_RECEIVERS
                            | PackageManager.GET_SERVICES
                            | PackageManager.GET_PROVIDERS
                            | PackageManager.GET_DISABLED_COMPONENTS);

                    List<ComponentInfo> components = new ArrayList<>();
                    if (packageInfo.activities != null) Collections.addAll(components, packageInfo.activities);
                    if (packageInfo.services != null) Collections.addAll(components, packageInfo.services);
                    if (packageInfo.providers != null) Collections.addAll(components, packageInfo.providers);

                    for (ComponentInfo componentInfo : components) {
                        if (componentInfo.name.equals(clsName)) {
                            return componentInfo.enabled; //This is the default value (set in AndroidManifest.xml)
                            //return componentInfo.isEnabled(); //Whole package dependant
                        }
                    }

                    // the component is not declared in the AndroidManifest
                    return false;
                } catch (PackageManager.NameNotFoundException e) {
                    // the package isn't installed on the device
                    return false;
                }
        }
    }

}

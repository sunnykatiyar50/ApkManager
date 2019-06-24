package com.sunnykatiyar.AppManager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

public class CustomExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> headers;
    private HashMap<String, List<String>> list;

    public CustomExpandableListAdapter(Context c, List<String> headers, HashMap<String,List<String>> list)
    {
        this.context=c;
        this.list=list;
        this.headers=headers;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        String property_name= (String) getGroup(i);
        if(view==null){
            view=inflater.inflate(R.layout.expandable_list_header,null);
        }
            TextView group_view= (TextView) view.findViewById(R.id.group_item);
           // view.setClickable(false);
            group_view.setText(property_name);
        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        String property_fetched = (String) getChild(i, i1);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(view==null){
            view=inflater.inflate(R.layout.expandable_list_item,null);
        }

        TextView property_value = (TextView) view.findViewById(R.id.item_value);
        property_value.setText(property_fetched);
      //  view.setClickable(false);

        view.setTooltipText(property_fetched);
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

}

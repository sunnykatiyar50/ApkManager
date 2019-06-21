package com.sunnykatiyar.ApkManager;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class CustomListAdapter extends RecyclerView.Adapter<CustomViewHolder> {

    List<ListDataItem> list ;
    Context context;
    ListDataItem temp;
    int selected_count;
    private String TAG = " Custom List Adapter : ";
    List<ListDataItem> selected_items_list;
    String msg_text;

    public CustomListAdapter(List<ListDataItem> apks_list, Context c) {
        super();
        this.list=apks_list;
        this.context=c;
        selected_items_list = new ArrayList<>();

    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater in = LayoutInflater.from(context);
        View view = in.inflate(R.layout.listitem_layout,viewGroup,false);
        CustomViewHolder cst = new CustomViewHolder(view);
      //Log.i(TAG , "On create view Holder");
        return cst;
    }

    @Override
    public void onBindViewHolder(@NonNull final CustomViewHolder cst, final int i) {

       temp = list.get(i);
//     Log.i(TAG , " : "+list.get(i).file_name);
//        Log.i(TAG , "Updatable : "+list.get(i).isUpdatable);
//        Log.i(TAG , "Installed : "+list.get(i).isInstalled);
//        Log.i(TAG , "CheckBoxState : "+list.get(i).select_box_state);
        int position = i;
         String ver_apk;
         String ver_app;


    if(list.get(i).apk_pkg_info != null){

        ver_apk=list.get(i).apk_version_name + " " + list.get(i).apk_version_code;
        ver_app=list.get(i).app_version_name + " " + list.get(i).app_version_code;

        if(list.get(i).isUpdatable == true){
            cst.file_name.setTextColor(ContextCompat.getColor(context, R.color.Updatable));
            cst.app_name.setTextColor(ContextCompat.getColor(context, R.color.Updatable));
            cst.text_extra.setTextColor(ContextCompat.getColor(context, R.color.Updatable));
            cst.text_extra.setText("#Update");
            cst.text_extra.setTypeface(Typeface.DEFAULT_BOLD);
            cst.file_name.setTypeface(Typeface.DEFAULT_BOLD);
            cst.app_name.setTypeface(Typeface.DEFAULT_BOLD);
            cst.apk_version.setTypeface(Typeface.DEFAULT_BOLD);
        }
        else if(list.get(i).isInstalled == true){
            cst.file_name.setTextColor(ContextCompat.getColor(context, R.color.InstalledOnly));
            cst.app_name.setTextColor(ContextCompat.getColor(context, R.color.InstalledOnly));
            cst.text_extra.setTextColor(ContextCompat.getColor(context, R.color.InstalledOnly));
            cst.text_extra.setTypeface(Typeface.DEFAULT);
            cst.file_name.setTypeface(Typeface.DEFAULT);
            cst.app_name.setTypeface(Typeface.DEFAULT);
            cst.apk_version.setTypeface(Typeface.DEFAULT);
            cst.text_extra.setText("Installed");
        }else if(list.get(i).isInstalled==false){
            cst.file_name.setTextColor(ContextCompat.getColor(context, R.color.Not_Installed));
            cst.app_name.setTextColor(ContextCompat.getColor(context, R.color.Not_Installed));
            cst.text_extra.setTextColor(ContextCompat.getColor(context, R.color.Not_Installed));
            cst.text_extra.setText("Not Installed");
            cst.text_extra.setTypeface(Typeface.DEFAULT);
            cst.file_name.setTypeface(Typeface.DEFAULT);
            cst.app_name.setTypeface(Typeface.DEFAULT);
            cst.apk_version.setTypeface(Typeface.DEFAULT);
        }

        cst.file_name.setText(list.get(i).file.getPath());
        cst.app_name.setText(list.get(i).app_name);
        cst.text_time.setText(list.get(i).last_install_time);
        cst.app_version.setText(ver_app);
        cst.apk_version.setText(ver_apk);
        cst.text_pkg_name.setText(list.get(i).pkg_name);
       // cst.text_extra.setText(list.get(i).file_mod_date);
        cst.file_size.setText(list.get(i).file_size);
        cst.app_icon.setImageDrawable(list.get(i).app_info.loadIcon(list.get(i).pm));
        //  Log.i(TAG , "On view Binder");

        cst.select_box.setOnCheckedChangeListener(null);
        cst.select_box.setChecked(list.get(i).select_box_state);

        cst.file_name.setTooltipText("File Path : "+list.get(i).file.getPath());
        cst.file_size.setTooltipText("File Size : " +list.get(i).file_size);
        cst.text_pkg_name.setTooltipText("Package Name : " +list.get(i).pkg_name);
        cst.apk_version.setTooltipText("Apk Version : " +list.get(i).apk_version_name);
        cst.app_version.setTooltipText("App Version : " +list.get(i).app_version_name);
        cst.text_time.setTooltipText("Last Update Time : "+list.get(i).last_install_time);

        cst.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cst.select_box.isChecked()){
                    cst.select_box.setChecked(false);
                    list.get(i).select_box_state = false;
                }
                else{
                    cst.select_box.setChecked(true);
                    list.get(i).select_box_state = false;
                }
            }
        });

    }

    cst.select_box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
        {
            int total=getItemCount();
            int sel = getSelectedItemsList().size();

            if(isChecked){
                list.get(i).select_box_state=true;
                //buttonView.setChecked(true);
//                 if(sel==total){
//                    MainActivity.option_menu.getItem(R.id.menuitem_select_all).setChecked(true);
//                }
//                else{
//                     MainActivity.option_menu.getItem(R.id.menuitem_select_all).setChecked(true);                }
            }
            else
            {
                list.get(i).select_box_state=false;
                //buttonView.setChecked(false);
            }

            sel = getSelectedItemsList().size();
            msg_text = "Total : "+total+"\t Selected : "+sel;
            MainActivity.text_msgs.setText(msg_text);

            Toast.makeText(context,list.get(i).app_name+ "selection state changed. \n Selected Count :"+sel,Toast.LENGTH_SHORT).show();
        }
        });

}

    public List<ListDataItem> getSelectedItemsList(){

        selected_items_list = new ArrayList<>();

        if(list!=null){
            for(ListDataItem list_item : list){
                if(list_item.select_box_state == true)
                {
                    selected_items_list.add(list_item);
                }
            }
        }

        return selected_items_list;
    }


    public void SelectUpdatable(){
        for(ListDataItem list_item : list){
            if(list_item.isInstalled == true){
                if(Integer.parseInt(list_item.apk_version_code) > Integer.parseInt(list_item.app_version_code))
                {
                    list_item.select_box_state = true;
                }
            }
        }
    }

//    public int getSelectedItemCount(){
//        if(selected_items_list != null)
//            return selected_items_list.size();
//        else
//            return 0;
//
//    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        if(this.list==null){
            return 0;
        }else{
            return this.list.size();
        }
    }

}

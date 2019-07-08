package com.sunnykatiyar.appmanager;

import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Typeface;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import java.util.ArrayList;
import java.util.List;

public class AdapterApkList extends RecyclerView.Adapter<ViewHolderApkList> {

    private String TAG = " Custom List Adapter : ";
    List<ObjectApkFile> list ;
    Context context;
    ObjectApkFile temp;
    List<ObjectApkFile> selected_items_list;
    String msg_text;
    private final String str_no_install = "Not Installed";
    private final String str_install = "Installed";
    private final String str_update = "#Update";

    ClipboardManager clipboardManager;

    public AdapterApkList(List<ObjectApkFile> apks_list, Context c) {
        super();
        this.list=apks_list;
        this.context=c;
        selected_items_list = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolderApkList onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        context = viewGroup.getContext();
        LayoutInflater in = LayoutInflater.from(context);
        View view = in.inflate(R.layout.listitem_apk_list,viewGroup,false);
        ViewHolderApkList cst = new ViewHolderApkList(view);
      //Log.i(TAG , "On create view Holder");
        return cst;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolderApkList cst, final int i) {

       temp = list.get(i);
        int position = i;
         String ver_apk;
         String ver_app;

    if(temp.apk_pkg_info != null) {

        ver_apk = temp.apk_version_name + " " + temp.apk_version_code;
        ver_app = temp.app_version_name + " " + temp.app_version_code;

        if (temp.isUpdatable == true) {
            cst.app_name.setTextColor(ContextCompat.getColor(context, R.color.Updatable));
            cst.text_app_install_status.setTextColor(ContextCompat.getColor(context, R.color.Updatable));
            cst.text_app_install_status.setText(str_update);
            cst.text_app_install_status.setTypeface(Typeface.DEFAULT_BOLD);
            cst.file_name.setTypeface(Typeface.DEFAULT_BOLD);
            cst.apk_version.setTypeface(Typeface.DEFAULT_BOLD);
        } else if (temp.isInstalled == true) {
            cst.app_name.setTextColor(ContextCompat.getColor(context, R.color.InstalledOnly));
            cst.text_app_install_status.setTextColor(ContextCompat.getColor(context, R.color.InstalledOnly));
            cst.text_app_install_status.setTypeface(Typeface.DEFAULT);
            cst.file_name.setTypeface(Typeface.DEFAULT);
            cst.apk_version.setTypeface(Typeface.DEFAULT);
            cst.text_app_install_status.setText("Installed");
        } else if (temp.isInstalled == false) {
            cst.app_name.setTextColor(ContextCompat.getColor(context, R.color.Not_Installed));
            cst.text_app_install_status.setTextColor(ContextCompat.getColor(context, R.color.Not_Installed));
            cst.text_app_install_status.setText(str_no_install);
            cst.text_app_install_status.setTypeface(Typeface.DEFAULT);
            cst.file_name.setTypeface(Typeface.DEFAULT);
            cst.apk_version.setTypeface(Typeface.DEFAULT);
        }

        cst.file_name.setText(temp.file.getPath());
        cst.app_name.setText(temp.app_name);
        cst.text_time.setText(temp.str_file_creation_time);
        cst.app_version.setText(ver_app);
        cst.apk_version.setText(ver_apk);
        cst.text_pkg_name.setText(temp.pkg_name);
        cst.file_size.setText(temp.file_size);
        cst.app_icon.setImageDrawable(temp.app_info.loadIcon(temp.pm));
        cst.select_box.setOnCheckedChangeListener(null);
        cst.select_box.setChecked(temp.select_box_state);


//------------------------------------------------TOOLTIP------------------------------------------
        cst.file_size.setTooltipText("File Size : " + temp.file_size);
        cst.text_pkg_name.setTooltipText("Package Name : " + temp.pkg_name);
        cst.apk_version.setTooltipText("Apk Version : " + temp.apk_version_name);
        cst.app_version.setTooltipText("App Version : " + temp.app_version_name);
        cst.text_time.setTooltipText("Last Update Time : " + temp.str_app_update_time);
        cst.app_name.setTooltipText(temp.pkg_name);
        cst.file_name.setTooltipText("File Path : " + temp.file.getAbsolutePath());
        cst.app_name.setTooltipText(temp.app_name);
        //  Log.i(TAG , "On view Binder");

        cst.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cst.select_box.isChecked()) {
                    cst.select_box.setChecked(false);
                    temp.select_box_state = false;
                } else {
                    cst.select_box.setChecked(true);
                    temp.select_box_state = false;
                }
            }
        });

    }

    cst.select_box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
        {
            int total = getItemCount();
            int sel ;

            if(isChecked){
                list.get(i).select_box_state=true;}
            else
            {
                list.get(i).select_box_state=false;
                //buttonView.setChecked(false);
            }

            sel = getSelectedItemsList().size();
            msg_text = "Total : "+total+"\t Selected : "+sel;
            FragmentApkFiles.msg_textview.setText(msg_text);
        }
        });

    }

    public List<ObjectApkFile> getSelectedItemsList(){

        selected_items_list = new ArrayList<>();

        if(list!=null){
            for(ObjectApkFile list_item : list){
                if(list_item.select_box_state == true)
                {
                    selected_items_list.add(list_item);
                }
            }
        }

        return selected_items_list;
    }

    public void SelectUpdatable(){
        for(ObjectApkFile list_item : list){
            if(list_item.isInstalled == true){
                if(Integer.parseInt(list_item.apk_version_code) > Integer.parseInt(list_item.app_version_code))
                {
                    list_item.select_box_state = true;
                }
            }
        }
    }

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

package com.sunnykatiyar.ApkManager;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomViewHolder extends RecyclerView.ViewHolder {

    TextView app_name;
    TextView file_name;
    TextView text_extra;
    TextView text_time;
    TextView text_pkg_name;
    TextView file_size;
    TextView app_version;
    TextView apk_version;
    ImageView app_icon ;

    CheckBox select_box;

    public CustomViewHolder(@NonNull View itemView) {
        super(itemView);
        this.text_time = itemView.findViewById(R.id.text_time);
        this.file_name = itemView.findViewById(R.id.text_apk_name);
        this.file_size = itemView.findViewById(R.id.text_file_size);
        this.app_name = itemView.findViewById(R.id.text_app_name);
        this.text_extra = itemView.findViewById(R.id.text_extra);
        this.text_pkg_name = itemView.findViewById(R.id.text_pkg_name);
        this.app_version=itemView.findViewById(R.id.text_app_version);
        this.apk_version=itemView.findViewById(R.id.text_apk_version);
        this.select_box = itemView.findViewById(R.id.file_select_box);
        this.app_icon  = itemView.findViewById(R.id.app_icon);
    }

}

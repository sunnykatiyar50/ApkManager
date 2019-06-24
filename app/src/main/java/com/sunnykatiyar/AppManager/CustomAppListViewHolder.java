package com.sunnykatiyar.AppManager;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomAppListViewHolder extends RecyclerView.ViewHolder {
    
    TextView appname;
    TextView pkgname;
    ImageView applogo;
    TextView app_size;
    TextView install_date;
    TextView version;
    TextView extra_info;

    public CustomAppListViewHolder(@NonNull View itemView) {
        super(itemView);
        this.appname = itemView.findViewById(R.id.app_name);
        this.pkgname = itemView.findViewById(R.id.pkg_name);
        this.version = itemView.findViewById(R.id.version_num);
        this.applogo = itemView.findViewById(R.id.app_icon);
        this.install_date = itemView.findViewById(R.id.label_install_time);
        this.extra_info = itemView.findViewById(R.id.label_extra);
        this.app_size = itemView.findViewById(R.id.label_app_size);
    }
}

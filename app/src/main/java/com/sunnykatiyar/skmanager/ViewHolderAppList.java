package com.sunnykatiyar.skmanager;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewHolderAppList extends RecyclerView.ViewHolder {
    
    final TextView appname;
    final TextView pkgname;
    final ImageView applogo;
    final TextView app_size;
    final TextView install_date;
    final TextView version;
    final TextView app_type;
    final TextView text_extra;
    View v;

    public ViewHolderAppList(@NonNull View itemView) {
        super(itemView);
        this.appname = itemView.findViewById(R.id.app_name);
        this.app_type = itemView.findViewById(R.id.app_type);
        this.pkgname = itemView.findViewById(R.id.pkg_name);
        this.version = itemView.findViewById(R.id.version_num);
        this.applogo = itemView.findViewById(R.id.app_icon);
        this.install_date = itemView.findViewById(R.id.label_install_time);
        this.text_extra = itemView.findViewById(R.id.label_extra);
        this.app_size = itemView.findViewById(R.id.label_app_size);
    }

}

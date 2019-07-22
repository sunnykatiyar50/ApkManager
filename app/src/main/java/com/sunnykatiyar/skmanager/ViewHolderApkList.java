package com.sunnykatiyar.skmanager;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewHolderApkList extends RecyclerView.ViewHolder {

    final TextView app_name;
    final TextView file_name;
    final TextView text_app_install_status;
    final TextView text_time;
    final TextView text_pkg_name;
    final TextView file_size;
    final TextView app_version;
    final TextView apk_version;
    final ImageView app_icon ;

    final CheckBox select_box;

    public ViewHolderApkList(@NonNull View itemView) {
        super(itemView);
        this.text_time = itemView.findViewById(R.id.text_time);
        this.file_name = itemView.findViewById(R.id.text_apk_name);
        this.file_size = itemView.findViewById(R.id.text_apk_size);
        this.app_name = itemView.findViewById(R.id.text_app_name);
        this.text_app_install_status = itemView.findViewById(R.id.text_app_status);
        this.text_pkg_name = itemView.findViewById(R.id.text_pkg_name);
        this.app_version=itemView.findViewById(R.id.text_app_version);
        this.apk_version=itemView.findViewById(R.id.text_apk_version);
        this.select_box = itemView.findViewById(R.id.apk_select_box);
        this.app_icon  = itemView.findViewById(R.id.app_icon);
    }

}

package com.sunnykatiyar.skmanager;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ViewHolderFileSelector extends RecyclerView.ViewHolder{

    final TextView selector_file_name;
    final TextView selector_file_type;
    final TextView selector_file_time;
    final TextView selector_file_perm;
    final CheckBox selector_file_checkbox;

    public ViewHolderFileSelector(@NonNull View itemView) {
        super(itemView);
        this.selector_file_name = itemView.findViewById(R.id.selector_file_name);
        this.selector_file_type = itemView.findViewById(R.id.selector_file_type);
        this.selector_file_time = itemView.findViewById(R.id.selector_file_time);
        this.selector_file_perm = itemView.findViewById(R.id.selector_file_perm);
        TextView selector_file_extra = itemView.findViewById(R.id.selector_file_extra);
        this.selector_file_checkbox = itemView.findViewById(R.id.selector_file_checkbox);
        ImageView selector_file_thumbnail = itemView.findViewById(R.id.selector_file_thumbnails);
    }

}

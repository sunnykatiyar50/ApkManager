package com.sunnykatiyar.appmanager;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ViewHolderFileSelector extends RecyclerView.ViewHolder{

    TextView selector_file_name;
    TextView selector_file_type;
    TextView selector_file_time;
    TextView selector_file_perm;
    TextView selector_file_extra;
    CheckBox selector_file_checkbox;
    ImageView selector_file_thumbnail;

    public ViewHolderFileSelector(@NonNull View itemView) {
        super(itemView);
        this.selector_file_name = itemView.findViewById(R.id.selector_file_name);
        this.selector_file_type = itemView.findViewById(R.id.selector_file_type);
        this.selector_file_time = itemView.findViewById(R.id.selector_file_time);
        this.selector_file_perm = itemView.findViewById(R.id.selector_file_perm);
        this.selector_file_extra = itemView.findViewById(R.id.selector_file_extra);
        this.selector_file_checkbox = itemView.findViewById(R.id.selector_file_checkbox);
        this.selector_file_thumbnail = itemView.findViewById(R.id.selector_file_thumbnails);
    }

}

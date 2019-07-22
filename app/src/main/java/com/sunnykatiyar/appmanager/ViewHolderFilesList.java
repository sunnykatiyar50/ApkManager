package com.sunnykatiyar.appmanager;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ViewHolderFilesList extends RecyclerView.ViewHolder {

    final TextView file_name;
    final TextView file_type;
    final TextView file_time;
    final TextView file_perm;
    final TextView file_size;
    final ImageView file_thumbnail;
    String TAG = "FILE_LIST_VIEWHOLDER :";

    public ViewHolderFilesList(@NonNull View itemView) {
        super(itemView);
        this.file_name = itemView.findViewById(R.id.file_item_name);
        this.file_type = itemView.findViewById(R.id.file_item_type);
        this.file_time = itemView.findViewById(R.id.file_item_time);
        this.file_perm = itemView.findViewById(R.id.file_item_perm);
        this.file_size = itemView.findViewById(R.id.file_item_size);
        CheckBox file_checkbox = itemView.findViewById(R.id.file_item_checkbox);
        this.file_thumbnail = itemView.findViewById(R.id.file_item_thumbnails);

    }
}

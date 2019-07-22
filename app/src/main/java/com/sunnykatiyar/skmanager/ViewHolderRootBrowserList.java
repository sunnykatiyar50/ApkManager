package com.sunnykatiyar.skmanager;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ViewHolderRootBrowserList extends RecyclerView.ViewHolder {

    final TextView root_browser_item_name;
    final TextView root_browser_item_type;
    final TextView root_browser_item_time;
    final TextView root_browser_item_perm;
    final TextView root_browser_inode;
    final TextView root_browser_owner;
    final TextView root_browser_item_size;
    final ImageView root_browser_item_thumbnail;
    String TAG = "root_browser_item_LIST_VIEWHOLDER :";

    public ViewHolderRootBrowserList(@NonNull View itemView) {
        super(itemView);

        this.root_browser_item_name = itemView.findViewById(R.id.root_browser_item_name);
        this.root_browser_item_type = itemView.findViewById(R.id.root_browser_item_type);
        this.root_browser_item_time = itemView.findViewById(R.id.root_browser_item_time);
        this.root_browser_inode = itemView.findViewById(R.id.root_browser_textview_inode);
        this.root_browser_owner = itemView.findViewById(R.id.root_browser_textview_owner);
        this.root_browser_item_perm = itemView.findViewById(R.id.root_browser_item_perm);
        this.root_browser_item_size = itemView.findViewById(R.id.root_browser_item_size);
        CheckBox root_browser_item_checkbox = itemView.findViewById(R.id.root_browser_item_checkbox);
        this.root_browser_item_thumbnail = itemView.findViewById(R.id.root_browser_item_thumbnails);
    }
}

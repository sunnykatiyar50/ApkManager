package com.sunnykatiyar.AppManager;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CustomFilesListViewHolder extends RecyclerView.ViewHolder {

    TextView text_file_name;
    TextView text_file_creation_time;
    TextView text_file_perm;
    TextView text_file_size;
    TextView text_file_ext;

    public CustomFilesListViewHolder(@NonNull View itemView) {


        super(itemView);

        text_file_name = itemView.findViewById(R.id.text_file_name);
        text_file_size = itemView.findViewById(R.id.text_file_size);
        text_file_perm = itemView.findViewById(R.id.text_file_perm);
        text_file_creation_time = itemView.findViewById(R.id.text_file_creation_time);
        text_file_ext = itemView.findViewById(R.id.text_file_ext);
    }
}

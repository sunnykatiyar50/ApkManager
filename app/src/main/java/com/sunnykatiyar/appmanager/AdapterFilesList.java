package com.sunnykatiyar.appmanager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AdapterFilesList extends RecyclerView.Adapter<ViewHolderFilesList> {

    private final String TAG = "ADAPTER_FILE_SELECTOR : ";
    private Context context;

    private List<ObjectFile> objectFileList;
    private List<ObjectFile> selected_objectFileslist;
    MyCallBack myCallBack;

    public interface MyCallBack{
//        void openDirectory(Uri uri);
        void openDirectory(ObjectFile obj);

    }

    public AdapterFilesList(MyCallBack act, List<ObjectFile> list,Context c) {
        context = c;
        objectFileList = list;
        this.myCallBack = act;
    }

    @NonNull
    @Override
    public ViewHolderFilesList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater in = LayoutInflater.from(context);
        View view = in.inflate(R.layout.listitem_files, parent, false);
        ViewHolderFilesList file_viewHolder = new ViewHolderFilesList(view);
        return file_viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderFilesList cflv, final int position) {

        ObjectFile local_objectfile = objectFileList.get(position);

       // Log.i(TAG, "Showing Document :"+local_file.file_name);

        cflv.file_name.setText(local_objectfile.file_name);
        cflv.file_time.setText(local_objectfile.modification_time);
        cflv.file_type.setText(local_objectfile.file_type);
        cflv.file_perm.setText(local_objectfile.access_permission);
        cflv.file_size.setText(local_objectfile.file_size);
        cflv.itemView.setOnClickListener(null);

        cflv.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//              file.check_box_state = !(file.check_box_state);
                Log.i(TAG, "CLICKED ITEM : "+local_objectfile.file_name);
//                Log.i(TAG, "CLICKED ITEM isDirectory: "+local_objectfile.file_doc.isDirectory());
//                Log.i(TAG, "CLICKED ITEM exists : "+local_objectfile.file_doc.exists());

                if(local_objectfile.file_doc.isDirectory()){
                    myCallBack.openDirectory(local_objectfile);
                }else if(local_objectfile.file_doc.isFile()){
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(local_objectfile.uri);
                    context.startActivity(i);
                }
            }
        });

        cflv.file_thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                local_objectfile.check_box_state=!(local_objectfile.check_box_state);
            }
        });

        cflv.file_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                local_objectfile.check_box_state=isChecked;
            }
        });

    }

    @Override
    public int getItemCount() {
        if(this.objectFileList ==null){
            return 0;
        }else
            return this.objectFileList.size();
    }

    public void getSelectedFiles(){
        for(ObjectFile obj: objectFileList){
            if(obj.check_box_state){
                selected_objectFileslist.add(obj);
            }
        }
    }

    public int getSelectedFilesCount(){
        getSelectedFiles();
        return selected_objectFileslist.size();
    }

}

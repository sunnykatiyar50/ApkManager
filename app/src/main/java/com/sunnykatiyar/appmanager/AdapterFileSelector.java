package com.sunnykatiyar.appmanager;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AdapterFileSelector extends RecyclerView.Adapter<ViewHolderFileSelector> {

    final String TAG = "ADAPTER_FILE_SELECTOR : ";
    Context context;
    List<ObjectFile> files_list ;
    List<ObjectFile> selected_files_list;
    LoadFilesTask loadFilesAsyncTask;

    public AdapterFileSelector(Context c, List<ObjectFile> list) {
        context = c;
        files_list = list;
    }

    @NonNull
    @Override
    public ViewHolderFileSelector onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        context = parent.getContext();
        LayoutInflater in = LayoutInflater.from(context);
        View view = in.inflate(R.layout.listitem_file_selector, parent, false);
        ViewHolderFileSelector cflv = new ViewHolderFileSelector(view);
        return cflv;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolderFileSelector cflv, final int position) {

        ObjectFile local_file = files_list.get(position);

        Log.i(TAG, "Showing Document :"+local_file.file_name);
        cflv.selector_file_name.setText(local_file.file_name);
        cflv.selector_file_time.setText(local_file.modification_time);
        cflv.selector_file_type.setText(local_file.file_type);
        cflv.selector_file_perm.setText(local_file.access_permission);

        cflv.itemView.setOnClickListener(null);
        cflv.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//              file.check_box_state = !(file.check_box_state);

                if(local_file.file_doc.isDirectory()){

                    loadFilesAsyncTask.cancel(true);
                    loadFilesAsyncTask = new LoadFilesTask(local_file.file_doc);
                    loadFilesAsyncTask.execute();
                    ActivityFileSelector.selector_adapter.notifyDataSetChanged();
                }else if(local_file.file_doc.isFile()){
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(local_file.uri);
                    context.startActivity(i);
                }
            }
        });

        cflv.selector_file_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                local_file.check_box_state=isChecked;
            }
        });

    }

    @Override
    public int getItemCount() {
        if(this.files_list==null){
            return 0;
        }else
            return this.files_list.size();
    }



    public void getSelectedFiles(){
        for(ObjectFile obj:files_list){
            if(obj.check_box_state){
                selected_files_list.add(obj);
            }
        }
    }

    public int getSelectedFilesCount(){
        getSelectedFiles();
        return selected_files_list.size();
    }



    public class LoadFilesTask extends AsyncTask<Void,String,List>{

        List<ObjectFile> child_doc_list = new ArrayList<>();
        DocumentFile local_doc;
        int count=0;

        public LoadFilesTask(DocumentFile f) {
            super();
            local_doc=f;
        }

        @Override
        protected List doInBackground(Void... voids) {
            getChildrenList(local_doc);
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ActivityFileSelector.selector_textview_path.setText(local_doc.getName());
        }

        @Override
        protected void onPostExecute(List list) {
            super.onPostExecute(list);
            notifyDataSetChanged();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            ActivityFileSelector.selector_textview_msgbox.setText((count++)+" : "+values[0]);
            notifyDataSetChanged();
        }

        protected List getChildrenList(DocumentFile local_doc){
            List<ObjectFile> local_list =  new ArrayList<>();

            for(DocumentFile f:local_doc.listFiles()){
                local_list.add(new ObjectFile(f,context));
                publishProgress(f.getName());
                if(!isCancelled()){
                    break;
                }
            }

            return local_list;
        }
    }



}

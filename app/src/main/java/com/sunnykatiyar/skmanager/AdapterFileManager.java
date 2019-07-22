package com.sunnykatiyar.skmanager;

import android.content.Context;
import android.os.Vibrator;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

class AdapterFileManager extends RecyclerView.Adapter<ViewHolderFilesList> {

    private final String TAG = "ADAPTER_FILE_MANAGER : ";
    private Context context;
    int selected_count ;
    public boolean selection_mode=false;
    private final List<ObjectDocumentFile> objectDocumentFileList;
    private final AdapterCallBack adapterCallBack;

    public interface AdapterCallBack {
        void openDocument(ObjectDocumentFile obj);
        void selectAllitems(boolean enable);
        void updateMsgTextview(String str);
        void enableActionOptionsInBottombar(boolean enable);
    }

    public AdapterFileManager(AdapterCallBack act, List<ObjectDocumentFile> list, Context c) {
        this.context = c;
        this.objectDocumentFileList = list;
        selected_count=getSelectedFilesCount();
        //Log.i(TAG,"List Size to Load : "+list.size());
        this.adapterCallBack = act;
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if(selected_count > 0) {
            selection_mode = true;
            adapterCallBack.enableActionOptionsInBottombar(true);
        }else{
            selection_mode = false;
            adapterCallBack.enableActionOptionsInBottombar(false);
        }
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
    public void onBindViewHolder(@NonNull ViewHolderFilesList cflv, int i) {

        ObjectDocumentFile obj = objectDocumentFileList.get(i);
        cflv.file_name.setText(obj.file_name);
        cflv.file_time.setText(obj.modification_time);
        cflv.file_type.setText(obj.file_type);
        cflv.file_perm.setText(obj.perm);
        cflv.file_size.setText(obj.file_size);

        //---------------------------------------FILE_TYPE----------------------------------------------
        if(obj.isDirectory){
            cflv.file_thumbnail.setImageResource(R.drawable.ic_folder_white_24dp);
        }else{
            cflv.file_thumbnail.setImageResource(R.drawable.ic_insert_drive_file_white_24dp);
        }

        //---------------------------------------SET_CHECK_BOX_STATE----------------------------------------------
        if(obj.check_box_state){
           // selected_count++;
           // Log.i(TAG,"Selected_Count = "+(selected_count));
            cflv.itemView.setBackgroundResource(R.color.tint_item_selected);
        }else{
            cflv.itemView.setBackgroundResource(R.color.tint_item_unselected);
        }


        cflv.itemView.setOnClickListener(null);

        cflv.itemView.setOnClickListener(v -> {
            Log.i(TAG," onClick selection mode = "+selection_mode);
            if(!selection_mode) {
                Log.i(TAG, "CLICKED ITEM : " + obj.file_name);
                adapterCallBack.openDocument(obj);
            }else {
                if(objectDocumentFileList.get(i).check_box_state){
                    objectDocumentFileList.get(i).check_box_state = false;
                    selected_count--;
                    cflv.itemView.setBackgroundResource(R.color.tint_item_unselected);
                    adapterCallBack.updateMsgTextview("");
                }else {
                    objectDocumentFileList.get(i).check_box_state = true;
                    selected_count++;
                    adapterCallBack.updateMsgTextview("");
                    cflv.itemView.setBackgroundResource(R.color.tint_item_selected);
                }
                Log.i(TAG, "CLICKED ITEM : " + obj.file_name+"   selected = "+obj.check_box_state);
                if(selected_count==0){
                        selection_mode = false;
                        v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                        adapterCallBack.enableActionOptionsInBottombar(false);
                }
            }
        });

        cflv.itemView.setOnLongClickListener(null);

        cflv.itemView.setOnLongClickListener(v -> {
           // selection_mode = !selection_mode;
            if(!selection_mode){
//                  //cflv.file_checkbox.setChecked(true);
                Log.i(TAG," New Selection Mode = "+ false);
                v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                selection_mode=true;
                objectDocumentFileList.get(i).check_box_state = true;
                selected_count++;
                adapterCallBack.updateMsgTextview("");
                adapterCallBack.enableActionOptionsInBottombar(true);
                cflv.itemView.setBackgroundResource(R.color.tint_item_selected);
            }else{
              //cflv.file_checkbox.setChecked(false);
                Log.i(TAG," New Selection Mode = "+ true);
                v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                selection_mode = false;
                selected_count=0;
                adapterCallBack.selectAllitems(false);
                adapterCallBack.updateMsgTextview("");
                adapterCallBack.enableActionOptionsInBottombar(false);
            }
            return true;
        });

//        cflv.file_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                 Log.i(TAG," isChecked = "+isChecked);
//
//                if(isChecked){
//                    selection_mode = true;
//                    objectDocumentFileList.get(i).check_box_state = isChecked;
//                    cflv.itemView.setBackgroundResource(R.color.tint_listitem_selected);
//                    selected_count++;
//                }else if(!isChecked){
//                    objectDocumentFileList.get(i).check_box_state = !isChecked;
//                    cflv.itemView.setBackgroundResource(R.color.tint_item_unselected);
//                    selected_count--;
//                }
//
//                if(selected_count==0){
//                    selection_mode =false;
//                    adapterCallBack.enableActionOptionsInBottombar(false);
//                }else if(selected_count==1){
//                    adapterCallBack.enableActionOptionsInBottombar(true);
//                }
//
//                adapterCallBack.updateMsgTextview(null);
//            }
//        });

    }

    @Override
    public int getItemCount() {
        if(this.objectDocumentFileList == null){
            return 0;
        }else
            return this.objectDocumentFileList.size();
    }

    public List<ObjectDocumentFile> getSelectedFiles(){
        List<ObjectDocumentFile> selected_objectDocumentFileslist = new ArrayList<>();
        for(ObjectDocumentFile obj: objectDocumentFileList){
            if(obj.check_box_state){
                selected_objectDocumentFileslist.add(obj);
            }
        }
        return selected_objectDocumentFileslist;
    }

    private int getSelectedFilesCount(){
        return getSelectedFiles().size();
    }

}

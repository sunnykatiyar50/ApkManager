package com.sunnykatiyar.appmanager;

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

public class AdapterFileManager extends RecyclerView.Adapter<ViewHolderFilesList> {

    private final String TAG = "ADAPTER_FILE_MANAGER : ";
    private Context context;
    int selected_count ;
    public boolean selection_mode=false;
    private List<ObjectDocumentFile> objectDocumentFileList;
    private List<ObjectDocumentFile> selected_objectDocumentFileslist;
    MyCallBack myCallBack;
    Vibrator vibrator;

    public interface MyCallBack{
        void openDocument(ObjectDocumentFile obj);
        void selectAllitems(boolean enable);
        void updateMsgTextview(String str);
        void enableActionOptionsInBottombar(boolean enable);
    }

    public AdapterFileManager(MyCallBack act, List<ObjectDocumentFile> list, Context c) {
        this.context = c;
        this.objectDocumentFileList = list;
        selected_count=getSelectedFilesCount();
        if(selected_count>0){
            selection_mode=true;
        }
        Log.i(TAG,"List Size to Load : "+list.size());
        this.myCallBack = act;
        this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
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
        cflv.file_perm.setText(obj.flags);
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
            Log.i(TAG,"Selected_Count = "+(selected_count));
            cflv.itemView.setBackgroundResource(R.color.tint_item_selected);
        }else{
            cflv.itemView.setBackgroundResource(R.color.tint_item_unselected);
        }


        cflv.itemView.setOnClickListener(null);

        cflv.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG," onClick selection mode = "+selection_mode);
                if(!selection_mode) {
                    Log.i(TAG, "CLICKED ITEM : " + obj.file_name);
                    myCallBack.openDocument(obj);
                }else if(selection_mode){
                    if(objectDocumentFileList.get(i).check_box_state==true){
                        objectDocumentFileList.get(i).check_box_state = false;
                        selected_count--;
                        cflv.itemView.setBackgroundResource(R.color.tint_item_unselected);
                        myCallBack.updateMsgTextview("");
                    }else {
                        objectDocumentFileList.get(i).check_box_state = true;
                        selected_count++;
                        myCallBack.updateMsgTextview("");
                        cflv.itemView.setBackgroundResource(R.color.tint_item_selected);
                    }
                    Log.i(TAG, "CLICKED ITEM : " + obj.file_name+"   selected = "+obj.check_box_state);
                    if(selected_count==0){
                            selection_mode = false;
                            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                            myCallBack.enableActionOptionsInBottombar(false);
                    }
                }
            }
        });

        cflv.itemView.setOnLongClickListener(null);

        cflv.itemView.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
               // selection_mode = !selection_mode;
                if(!selection_mode){
//                  //cflv.file_checkbox.setChecked(true);
                    Log.i(TAG," New Selection Mode = "+selection_mode);
                    v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    selection_mode=true;
                    objectDocumentFileList.get(i).check_box_state = true;
                    selected_count++;
                    myCallBack.updateMsgTextview("");
                    myCallBack.enableActionOptionsInBottombar(true);
                    cflv.itemView.setBackgroundResource(R.color.tint_item_selected);
                }else{
                  //cflv.file_checkbox.setChecked(false);
                    Log.i(TAG," New Selection Mode = "+selection_mode);
                    v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    selection_mode = false;
                    selected_count=0;
                    myCallBack.selectAllitems(false);
                    myCallBack.updateMsgTextview("");
                    myCallBack.enableActionOptionsInBottombar(false);
                }
                return true;
            }
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
//                    myCallBack.enableActionOptionsInBottombar(false);
//                }else if(selected_count==1){
//                    myCallBack.enableActionOptionsInBottombar(true);
//                }
//
//                myCallBack.updateMsgTextview(null);
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
        this.selected_objectDocumentFileslist = new ArrayList<>();
        for(ObjectDocumentFile obj: objectDocumentFileList){
            if(obj.check_box_state){
                selected_objectDocumentFileslist.add(obj);
            }
        }
        return selected_objectDocumentFileslist;
    }

    public int getSelectedFilesCount(){
        return getSelectedFiles().size();
    }

}

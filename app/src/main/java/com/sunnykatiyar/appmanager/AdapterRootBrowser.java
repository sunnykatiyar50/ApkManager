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

public class AdapterRootBrowser extends RecyclerView.Adapter<ViewHolderRootBrowserList> {

    private final String TAG = "ADAPTER_ROOT_BROWSER : ";

    public int selected_count ;
    public boolean selection_mode=false;
    private List<ObjectFile> fileList;
    private List<ObjectFile> selectedFileslist;
    MyRootBrowserCallBack myCallBack;
    Vibrator vibrator;
    Context context;

    
    public interface MyRootBrowserCallBack{
        void openDocument(ObjectFile obj);
        void selectAllitems(boolean enable);
        void updateMsgTextview(String str);
        void enableBottomActionBar(boolean action);
    }

    public AdapterRootBrowser(MyRootBrowserCallBack act, List<ObjectFile> list, Context c)
        {
            this.context = c;
            this.fileList = list;
            this.myCallBack = act;
            this.selected_count = getSelectedFileList().size();
            
            if(selected_count > 0) {
                selection_mode = true;
                myCallBack.enableBottomActionBar(true);
            }else{
                selection_mode = false;
                myCallBack.enableBottomActionBar(false);
            }
//            Log.i(TAG, "List Size to Load : " + list.size());
//            this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }
    @NonNull
    @Override
    public ViewHolderRootBrowserList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater in = LayoutInflater.from(parent.getContext());
        View v = in.inflate(R.layout.listitem_root_browser, parent, false);
        ViewHolderRootBrowserList vbrb = new ViewHolderRootBrowserList(v);
        return vbrb;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderRootBrowserList holder, int position) {

        ObjectFile objectFile = fileList.get(position);
        holder.root_browser_item_name.setText(objectFile.name);
        holder.root_browser_item_type.setText(objectFile.file_type);
        holder.root_browser_item_size.setText(objectFile.size);
        holder.root_browser_item_time.setText(objectFile.mod_time);
        holder.root_browser_item_thumbnail.setImageResource(objectFile.drawableIcon);
        holder.root_browser_item_perm.setText(objectFile.perm);
        holder.root_browser_owner.setText(objectFile.user_name);
        holder.root_browser_inode.setText(objectFile.inode);

        if(objectFile.isSelected){
            holder.itemView.setBackgroundResource(R.color.tint_item_selected);

        }else{
            holder.itemView.setBackgroundResource(R.color.tint_item_unselected);
        }

        holder.itemView.setOnClickListener(null);
        holder.itemView.setOnLongClickListener(null);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG," onClick selection mode = "+selection_mode);
                if(!selection_mode) {
                    Log.i(TAG, "CLICKED ITEM : " + objectFile.name);
                    myCallBack.openDocument(objectFile);
                }else if(selection_mode){
                    if(objectFile.isSelected==true){
                        objectFile.isSelected = false;
                        selected_count--;
                        holder.itemView.setBackgroundResource(R.color.tint_item_unselected);
                        myCallBack.updateMsgTextview("");
                    }else {
                        objectFile.isSelected = true;
                        selected_count++;
                        myCallBack.updateMsgTextview("");
                        holder.itemView.setBackgroundResource(R.color.tint_item_selected);
                    }
                    Log.i(TAG, "CLICKED ITEM : " + objectFile.name+"   selected = "+objectFile.isSelected);

                    if(selected_count==0){
                        selection_mode = false;
                        myCallBack.enableBottomActionBar(false);
                        v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    }
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener(){

            @Override
            public boolean onLongClick(View v) {

                if(!selection_mode){
//                  //cflv.file_checkbox.setChecked(true);
                    Log.i(TAG," New Selection Mode = "+selection_mode);
                    v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    selection_mode=true;
                    objectFile.isSelected = true;
                    selected_count++;
                    myCallBack.updateMsgTextview("");
                    myCallBack.enableBottomActionBar(true);
                    holder.itemView.setBackgroundResource(R.color.tint_item_selected);
                }else{
                    //cflv.file_checkbox.setChecked(false);
                    Log.i(TAG," New Selection Mode = "+selection_mode);
                    v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    selection_mode = false;
                    selected_count=0;
                    myCallBack.selectAllitems(false);
                    myCallBack.enableBottomActionBar(false);
                    myCallBack.updateMsgTextview("");
                }
              return true;
            }
        });

    }

    public List<ObjectFile> getSelectedFileList(){
        selectedFileslist = new ArrayList<>();
        List<ObjectFile> list =new ArrayList<>();
        for(ObjectFile f:fileList){
            if(f.isSelected){
                list.add(f);
            }
        }
        
        selectedFileslist=list;
        return list;
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

}

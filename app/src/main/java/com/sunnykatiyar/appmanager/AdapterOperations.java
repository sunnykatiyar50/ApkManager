package com.sunnykatiyar.appmanager;

import android.content.Context;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.LinkedHashMap;

import static com.sunnykatiyar.appmanager.FragmentFileManager.NoRootOperations;

class AdapterOperations extends RecyclerView.Adapter<AdapterOperations.ViewHolderOperationList> {

    Context context;
    LinkedHashMap<Integer, ObjectOperation> map;
    int CANCEL_BUTTON = 456789;
    OperationOptions myOperationOption;
    final String TAG = "ADAPTER_OPERATIONS  : ";
    public interface OperationOptions{
        void cancelOperation(int id);
    }

    public AdapterOperations(Context c, LinkedHashMap<Integer, ObjectOperation> map){
                 this.map = map  ;
                 context = c;
    }

    @NonNull
    @Override
    public ViewHolderOperationList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater =  LayoutInflater.from(context = parent.getContext());
        View v = inflater.inflate(R.layout.listitem_operation, parent, false) ;
        ViewHolderOperationList vhld = new ViewHolderOperationList(v);
        return vhld;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderOperationList holder, int position) {

        Integer id = (Integer) map.keySet().toArray()[position];
        ObjectOperation obj = map.get(id);
        holder.operationType.setText(obj.operationTitle);
        holder.operationStatus.setText(obj.operationStatus);
        holder.progressStatus.setText(obj.currentFileNum+"/"+obj.totalFiles);
        holder.progressBar.setMax(obj.totalFiles);
        holder.progressBar.setProgress(obj.currentFileNum);
        holder.operationDetails.setText(obj.operationDetails);

        holder.itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                
                menu.add(0, CANCEL_BUTTON, 100, "Cancel");

                menu.findItem(CANCEL_BUTTON).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
//                        myOperationOption.cancelOperation(id);
                        if(NoRootOperations[id]!=null){
                            NoRootOperations[id].cancelTask();
                            Log.i(TAG," TAsk cancelled with ID :"+id);
                        }
                        return false;
                    }
                });

            }
        });


    }

    @Override
    public int getItemCount() {
        if(map!=null){
            return map.size();
        }else {
            return 0;
        }
    }



    public class ViewHolderOperationList extends RecyclerView.ViewHolder
    {
        TextView operationType;
        TextView operationDetails;
        TextView operationStatus;
        TextView progressStatus;
        ProgressBar progressBar;

            public ViewHolderOperationList(@NonNull View itemView)
            {
                super(itemView);
                operationType = itemView.findViewById(R.id.operation_name_textview);
                operationDetails = itemView.findViewById(R.id.operation_details_textview);
                operationStatus = itemView.findViewById(R.id.operation_status_textView);
                progressStatus = itemView.findViewById(R.id.operation_progress_textview);
                progressBar = itemView.findViewById(R.id.operation_progressBar);
            }

    }

}

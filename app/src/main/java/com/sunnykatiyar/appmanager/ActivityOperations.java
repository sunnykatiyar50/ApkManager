package com.sunnykatiyar.appmanager;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedHashMap;

public class ActivityOperations extends AppCompatActivity implements AdapterOperations.OperationOptions{


    private RecyclerView operation_rview;
    public static AdapterOperations adapter_operations_list;
    private static ActivityOperations activityOperations ;
    public static final LinkedHashMap<Integer, ObjectOperation> operationsHashMapList = new LinkedHashMap<>();
    private CancelOperation myCancelOperation;
    final String TAG = "MYAPP : ACTIVITY OPERATIOSN";
    public static ActivityOperations getInstanceOf(){
        return activityOperations;
    }
    private TextView noview_Text;
    private ImageView noview_Image;
      public interface CancelOperation{
        void cancelOperationById(int id);
      }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operation);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        operation_rview = findViewById(R.id.operations_rview);
        noview_Image = findViewById(R.id.operation_noview_image);
        noview_Text = findViewById(R.id.operation_noview_textview);
        activityOperations = this;
        Context context = this;

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(RecyclerView.VERTICAL);
        operation_rview.setLayoutManager(llm);

        DividerItemDecoration mDivider  = new DividerItemDecoration(this, llm.getOrientation());
        operation_rview.addItemDecoration(mDivider);

        adapter_operations_list = new AdapterOperations(this,operationsHashMapList);
        enablelistView();
        operation_rview.setAdapter(adapter_operations_list);

//      ClassNoRootUtils cls = new ClassNoRootUtils(this);
        
    }
    
    public void addOperationObject(int id, ObjectOperation obj){
        operationsHashMapList.put(id, obj);
        adapter_operations_list = new AdapterOperations(this,operationsHashMapList);
        enablelistView();
    }

    private void enablelistView(){
        if(operationsHashMapList.size()>0){
            operation_rview.setVisibility(View.VISIBLE);
            operation_rview.setAdapter(adapter_operations_list);
            noview_Image.setVisibility(View.GONE);
            noview_Text.setVisibility(View.GONE);
        }else{
            operation_rview.setVisibility(View.GONE);
            noview_Image.setVisibility(View.VISIBLE);
            noview_Text.setVisibility(View.VISIBLE);
        }
    }
    @Override
    public void cancelOperation(int id) {
        myCancelOperation.cancelOperationById(id);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return true;
    }
}

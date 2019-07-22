package com.sunnykatiyar.appmanager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ActivityFileSelector extends AppCompatActivity {

    public static TextView selector_textview_msgbox;
    public static TextView selector_textview_path;
    Uri tree_uri;
    DocumentFile file_doc;
    List<Uri> child_uris;

    List<HashMap<String,Uri>> storage_paths;
    List<ObjectDocumentFile> external_dir_list;
    List<ObjectDocumentFile> file_list;
    List<DocumentFile> doc_files_list;
    private Context context;
    public static AdapterFileSelector selector_adapter;

    private final String TEXTVIEW_MSG ="textview_msg";

    private static final String key_extsd_uri = FragmentSettings.key_extsd_uri;
    private static final String path_not_set = FragmentSettings.path_not_set;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_selector);

        RecyclerView selecter_rview = findViewById(R.id.selector_activity_rview);
        selector_textview_msgbox = findViewById(R.id.selector_activity_msg_textview);
        selector_textview_path = findViewById(R.id.selector_activity_path_textview);
        Spinner storage_spinner = findViewById(R.id.selector_activity_storage_spinner);
        context = getApplicationContext();
        String string_uri_tree = ActivityMain.sharedPrefSettings.getString(key_extsd_uri, path_not_set);
        Uri tree_uri = Uri.parse(string_uri_tree);

        DocumentFile tree_doc_file = DocumentFile.fromTreeUri(this, tree_uri);
        List<ObjectDocumentFile> files_list = getChildrenList(tree_doc_file);

        selector_textview_path.setText(tree_doc_file.getName());

        String LOG_MSG = "log_msg";
        showMsg(LOG_MSG,"Setting Adapter with ListSize : "+ files_list.size());

        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(RecyclerView.VERTICAL);
        selecter_rview.setLayoutManager(llm);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(context, llm.getOrientation());
        selecter_rview.addItemDecoration(mDividerItemDecoration);

        selector_adapter = new AdapterFileSelector(this, files_list);
        selecter_rview.setAdapter(selector_adapter);
        selector_adapter.notifyDataSetChanged();

    }

    private List getChildrenList(DocumentFile local_doc){
        List<ObjectDocumentFile> local_list =  new ArrayList<>();

        for(DocumentFile f:local_doc.listFiles()){
            local_list.add(new ObjectDocumentFile(f,context));
            showMsg(TEXTVIEW_MSG,f.getName());
        }

        return local_list;
    }

    private void showMsg(String msgtype,String str){
        String TOAST_MSG = "toast_msg";
        if(msgtype.equals(TOAST_MSG)){
            Toast.makeText(getApplicationContext(),str,Toast.LENGTH_SHORT);
        }else if(msgtype.equals(TEXTVIEW_MSG)){
            selector_textview_msgbox.setText(str);
        }
        String TAG = "MYAPP : ACTIVITY_FILE_SELECTOR";
        Log.i(TAG,str);
    }
}

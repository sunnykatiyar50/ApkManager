package com.sunnykatiyar.skmanager;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Typeface;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static com.sunnykatiyar.skmanager.FragmentAppManager.clipboardManager;

public class AdapterApkList extends RecyclerView.Adapter<ViewHolderApkList> {

    private String TAG = "MYAPP : Custom List Adapter : ";
    private final List<ObjectApkFile> list ;
    private Context context;
    private ObjectApkFile temp;
    private List<ObjectApkFile> selected_items_list;
    private String msg_text;
    private final String str_install = "Installed";

    ClipboardManager clipboardManager;

    public AdapterApkList(List<ObjectApkFile> apks_list, Context c) {
        super();
        this.list=apks_list;
        this.context=c;
        clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        selected_items_list = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolderApkList onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        context = viewGroup.getContext();
        LayoutInflater in = LayoutInflater.from(context);
        View view = in.inflate(R.layout.listitem_apk_list,viewGroup,false);
        ViewHolderApkList cst = new ViewHolderApkList(view);
        return cst;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolderApkList cst, final int i) {

       temp = list.get(i);
        int position = i;
         String ver_apk;
         String ver_app;

    if(temp.apk_pkg_info != null) {

        ver_apk = temp.apk_version_name + " " + temp.apk_version_code;
        ver_app = temp.app_version_name + " " + temp.app_version_code;

        if (temp.isUpdatable) {
            cst.app_name.setTextColor(ContextCompat.getColor(context, R.color.Updatable));
            cst.text_app_install_status.setTextColor(ContextCompat.getColor(context, R.color.Updatable));
            String str_update = "#Update";
            cst.text_app_install_status.setText(str_update);
            cst.text_app_install_status.setTypeface(Typeface.DEFAULT_BOLD);
            cst.file_name.setTypeface(Typeface.DEFAULT_BOLD);
            cst.apk_version.setTypeface(Typeface.DEFAULT_BOLD);
        } else if (temp.isInstalled) {
            cst.app_name.setTextColor(ContextCompat.getColor(context, R.color.InstalledOnly));
            cst.text_app_install_status.setTextColor(ContextCompat.getColor(context, R.color.InstalledOnly));
            cst.text_app_install_status.setTypeface(Typeface.DEFAULT);
            cst.file_name.setTypeface(Typeface.DEFAULT);
            cst.apk_version.setTypeface(Typeface.DEFAULT);
            cst.text_app_install_status.setText("Installed");
        } else if (!false) {
            cst.app_name.setTextColor(ContextCompat.getColor(context, R.color.Not_Installed));
            cst.text_app_install_status.setTextColor(ContextCompat.getColor(context, R.color.Not_Installed));
            String str_no_install = "Not Installed";
            cst.text_app_install_status.setText(str_no_install);
            cst.text_app_install_status.setTypeface(Typeface.DEFAULT);
            cst.file_name.setTypeface(Typeface.DEFAULT);
            cst.apk_version.setTypeface(Typeface.DEFAULT);
        }

        cst.file_name.setText(temp.file.getPath());
        cst.app_name.setText(temp.app_name);
        cst.text_time.setText(temp.str_file_creation_time);
        cst.app_version.setText(ver_app);
        cst.apk_version.setText(ver_apk);
        cst.text_pkg_name.setText(temp.pkg_name);
        cst.file_size.setText(temp.file_size);
        cst.app_icon.setImageDrawable(temp.app_info.loadIcon(temp.pm));
        cst.select_box.setOnCheckedChangeListener(null);
        cst.select_box.setChecked(temp.select_box_state);

//------------------------------------------------TOOLTIP------------------------------------------
        cst.file_size.setTooltipText("File Size : " + temp.file_size);
        cst.text_pkg_name.setTooltipText("Package Name : " + temp.pkg_name);
        cst.apk_version.setTooltipText("Apk Version : " + temp.apk_version_name);
        cst.app_version.setTooltipText("App Version : " + temp.app_version_name);
        cst.text_time.setTooltipText("Last Update Time : " + temp.str_app_update_time);
        cst.app_name.setTooltipText(temp.pkg_name);
        cst.file_name.setTooltipText("File Path : " + temp.file.getAbsolutePath());

        cst.apk_version.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                copyToClipboard("Apk Version", cst.apk_version.getText().toString());
            }
        });

        cst.app_name.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                copyToClipboard("App Name", cst.app_name.getText().toString());
                Toast.makeText(context, "Copied to Clipboard\n "+cst.app_name.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });

        cst.file_name.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                copyToClipboard("Path", cst.file_name.getText().toString());
                Toast.makeText(context, "Copied to Clipboard\n "+cst.file_name.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });

        cst.text_pkg_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyToClipboard("Package Name", cst.text_pkg_name.getText().toString());
                Toast.makeText(context, "Copied to Clipboard\n "+cst.text_pkg_name.getText().toString(), Toast.LENGTH_SHORT).show();

            }
        });

        cst.app_name.setTooltipText(temp.app_name);
        //  Log.i(TAG , "On view Binder");

        cst.itemView.setOnClickListener(v -> {
            if (cst.select_box.isChecked()) {
                cst.select_box.setChecked(false);
                temp.select_box_state = false;
            } else {
                cst.select_box.setChecked(true);
                temp.select_box_state = false;
            }
        });

    }

    cst.select_box.setOnCheckedChangeListener((buttonView, isChecked) -> {
        int total = getItemCount();
        int sel ;

        //buttonView.setChecked(false);
        list.get(i).select_box_state = isChecked;

        sel = getSelectedItemsList().size();
        msg_text = "Total : "+total+"\t Selected : "+sel;
        FragmentApkFiles.msg_textview.setText(msg_text);
    });

    }

    public List<ObjectApkFile> getSelectedItemsList(){

        selected_items_list = new ArrayList<>();

        if(list!=null){
            for(ObjectApkFile list_item : list){
                if(list_item.select_box_state)
                {
                    selected_items_list.add(list_item);
                }
            }
        }

        return selected_items_list;
    }

    public void SelectUpdatable(){
        for(ObjectApkFile list_item : list){
            if(list_item.isInstalled){
                if(Integer.parseInt(list_item.apk_version_code) > Integer.parseInt(list_item.app_version_code))
                {
                    list_item.select_box_state = true;
                }
            }
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    private void copyToClipboard(String Title, String clip) {
        ClipData clipData = ClipData.newPlainText(Title, clip);
        clipboardManager.setPrimaryClip(clipData);
    }

    @Override
    public int getItemCount() {
        if(this.list==null){
            return 0;
        }else{
            return this.list.size();
        }
    }

}

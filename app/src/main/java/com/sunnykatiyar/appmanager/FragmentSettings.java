package com.sunnykatiyar.appmanager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;

import android.provider.DocumentsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.nononsenseapps.filepicker.FilePickerActivity;
import com.nononsenseapps.filepicker.Utils;
import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class FragmentSettings extends Fragment {

    private final String TAG = "MYAPP : SETTINGS FRAGMENT";
    private final int REQUEST_CODE_EXTSD_CARD_PATH = 17;
    private final int REQUEST_CODE_REPO_PATH = 23;
    private final int REQUEST_CODE_EXPORT_APK_PATH = 29;

    public static final String key_repository_folder = "REPOSITORY_FOLDER";
    public static final String key_root_access = "ROOT_ACCESS";
    public static final String key_extsd_uri = "EXTSD_CARD_URI";

    public static final String path_not_set = "PATH NOT SET";
    public static final String key_export_apk_enable = "EXPORT_APK_SWITCH";
    public static final String key_export_apk_uri = "EXPORT_APK_FOLDER";
    public String value_export_apk_path;
    public boolean value_export_apk_enable;

    public static String value_extsd_uri;
    public static boolean value_root_access ;
    public static boolean root_access;
    final String toast_msg = "TOAST_MESSAGE";
    final String log_msg = "LOG_MESSAGE";
    String value_repository_folder;
    int saved_storage_count;
    Button buttonSetRepoPath;
    Button clearRepositoryPref;
    Button button_open_selector;
    EditText text_repo_path;
    Switch root_switch;
    Context context = getContext();
    Button btn_remove_path;
    Button button_export_apk;
    Switch switch_export_apk;
    EditText editText_export_apk;
    Button btn_add_ext_path;
    Spinner storage_spinner;
    ArrayAdapter storage_spinner_adapter;

    public static SharedPreferences sharedPrefSettings;
    public static SharedPreferences.Editor prefEditSettings;
    public static SharedPreferences sharedPrefRepository;
    public static SharedPreferences.Editor prefEditRepository;
    public static SharedPreferences sharedPrefExternalStorages;
    public static SharedPreferences.Editor prefEditExternalStorages;
    Uri uri_extsd;
    Uri root_uri;
    Uri internal_storage_uri;

    String key_root_storage = "ROOT_PATH";
    String key_internal_storage = "INTERNAL_STORAGE_PATH";

    DocumentFile internal_storage_doc_file ;
    List<String> storage_spinner_list = new ArrayList<>();
    List<Uri> storages_uri_list = new ArrayList<>();

    public FragmentSettings() {
        // Required empty public constructor
    }

    static {
        Shell.Config.setFlags(Shell.FLAG_REDIRECT_STDERR);
        Shell.Config.verboseLogging(BuildConfig.DEBUG);
        Shell.Config.setTimeout(10);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
        sharedPrefSettings = context.getSharedPreferences(context.getResources().getString(R.string.sharedPref_settings),Context.MODE_PRIVATE);
        prefEditSettings = sharedPrefSettings.edit();
        sharedPrefRepository = context.getSharedPreferences(context.getResources().getString(R.string.sharedPref_settings),Context.MODE_PRIVATE);
        prefEditRepository = sharedPrefRepository.edit();
        sharedPrefExternalStorages = context.getSharedPreferences(context.getResources().getString(R.string.sharedPref_externalPaths),Context.MODE_PRIVATE);
        prefEditExternalStorages = sharedPrefExternalStorages.edit();
        value_root_access = sharedPrefSettings.getBoolean(key_root_access,false);
        value_repository_folder = sharedPrefSettings.getString(key_repository_folder,path_not_set);
        value_extsd_uri = sharedPrefSettings.getString(key_extsd_uri,path_not_set);
        value_export_apk_enable =   sharedPrefSettings.getBoolean(key_export_apk_enable,false);
//        Log.i(TAG,"Env.GETEXTERNALSTORAGEDIRECTORY() : "+Environment.getExternalStorageDirectory().getAbsolutePath());
//        Log.i(TAG,"Env.GETEXTERNALSTORAGESTATE() : "+Environment.getExternalStorageState());
//        Log.i(TAG,"Env.GETEXTERNALSTORAGEPUBLICDIRECTORY(DIRECTORY_DOCUMENTS) : "+ Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath());
//        Log.i(TAG,"Env.GETEXTERNALSTORAGESTATE(value_repository_folder) : "+Environment.getExternalStorageState(new File(value_repository_folder)));
//        Log.i(TAG,"Env.GETDATADIRECTORY(): "+Environment.getDataDirectory());
//        Log.i(TAG,"Env.GETDOWNLOADCACHEDIRECTORY() : "+ Environment.getDownloadCacheDirectory().getAbsolutePath());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_settings, container, false);

        text_repo_path = view.findViewById(R.id.text_path_repository);
        buttonSetRepoPath = view.findViewById(R.id.button_browse_repo);
        clearRepositoryPref = view.findViewById(R.id.button_clear_repo_data);
        root_switch = view.findViewById(R.id.switch_root_access);
        btn_remove_path = view.findViewById(R.id.buttton_clear_extsd);
        btn_add_ext_path = view.findViewById(R.id.buttton_add_extsd);
        button_open_selector = view.findViewById(R.id.button_open_selector);
        storage_spinner =view.findViewById(R.id.settings_storage_spinner);
        button_export_apk = view.findViewById(R.id.button_export_apk_folder);
        switch_export_apk = view.findViewById(R.id.switch_export_apk);
        editText_export_apk = view.findViewById(R.id.editText_apk_export_path);

        if(value_repository_folder.equals(path_not_set)){
            text_repo_path.setText("");
        }else{
            text_repo_path.setText(value_repository_folder);
        }


        //-----------------------------ROOT & INTERNAL_PATH TO SHARED_PREF-----------------------------------------------------------
//      root_doc_file = DocumentFile.fromFile(Environment.getRootDirectory());
//      prefEditExternalStorages.putString(key_root_storage,root_doc_file.getUri().toString()).commit();
        internal_storage_doc_file = DocumentFile.fromFile(Environment.getExternalStorageDirectory());
        prefEditExternalStorages.putString(key_internal_storage,internal_storage_doc_file.getUri().toString()).commit();
        saved_storage_count = sharedPrefExternalStorages.getAll().size();

        switch_export_apk.setChecked(value_export_apk_enable);
        editText_export_apk.setText(sharedPrefSettings.getString(key_export_apk_uri, path_not_set));

        //----------------------------SET ROOT SWITCH---------------------------------------------------------------
        root_switch.setChecked(getRootStatus(value_root_access));

        //----------------------------SET REPO FOLDER ----------------------------
        buttonSetRepoPath.setOnClickListener(v -> {

                    Intent i = new Intent(getContext(), FilePickerActivity.class);
                    //Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                    i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
                    i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);

                    value_repository_folder = sharedPrefSettings.getString(key_repository_folder, path_not_set);

                    File f = new File(value_repository_folder);

                    if(f.exists() & f.isFile()) {
                        i.putExtra(FilePickerActivity.EXTRA_START_PATH, f.getParent());
                    } else if (f.exists() & f.isDirectory()) {
                        i.putExtra(FilePickerActivity.EXTRA_START_PATH, f.getAbsolutePath());
                    } else {
                        i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());
                    }

                    startActivityForResult(i, REQUEST_CODE_REPO_PATH);
        });

        //---------------------------------CLEAR REPOSITORY BUTTON------------------------------------------
        clearRepositoryPref.setOnClickListener(v -> {
            prefEditRepository.clear().commit();
            prefEditSettings.remove(key_repository_folder).commit();
            text_repo_path.setText("");
            Log.i(TAG," Repository Preference Cleared.");
            Toast.makeText(getContext(), " Repository Preference Cleared.", Toast.LENGTH_SHORT).show();
        });

        //----------------------------SET_SPINNER_______________________________________________
        setSpinnerData();

        String temp = sharedPrefSettings.getString(key_export_apk_uri, null);
        if(null!=temp){
            editText_export_apk.setText(Uri.parse(temp).getPath());
        }


        //----------------------------SET EXTERNAL SDCARD ----------------------------
        btn_add_ext_path.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
               Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
                startActivityForResult(i, REQUEST_CODE_EXTSD_CARD_PATH);
            }
        });

        btn_remove_path.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Uri uri = storages_uri_list.get(storage_spinner.getSelectedItemPosition());
                prefEditExternalStorages.remove(uri.getLastPathSegment()).commit();
                showMsg(log_msg,"Removed : "+uri.getPath());
                setSpinnerData();
            }
        });

        //--------------------------------ROOT SWITCH CLICK LISTENER----------------------
        root_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"rootswitch checked : "+root_switch.isChecked());
                    root_access = getRootStatus(root_switch.isChecked());
                    root_switch.setChecked(root_access);
                    Toast.makeText(getActivity(),"Root permission availability :"+root_access,Toast.LENGTH_SHORT).show();
            }
        });
        
        button_open_selector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(),ActivityFileSelector.class);
                startActivity(i);
                Log.i(TAG,"STARTING FILE SELECTOR");
            }
        });

        switch_export_apk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefEditSettings.putBoolean(key_export_apk_enable, isChecked).commit();
                showMsg(log_msg, " Switch Enabled :"+isChecked);
                if(isChecked){
                   if(ServiceSetup.service_running_count==0){
                       Intent serviceIntent =  new Intent(getActivity(), ServiceSetup.class);
                       getActivity().startForegroundService(serviceIntent);
                   }else if(ServiceSetup.service_running_count>0){
                        Log.i(TAG,"Service Runnning Already");
                    }
                }
            }});

        button_export_apk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
                startActivityForResult(i, REQUEST_CODE_EXPORT_APK_PATH);
            }
        });

        return view;
    }

    public boolean getRootStatus(boolean isChecked){
        boolean temp;

        if(isChecked){
            try{
                temp = Shell.rootAccess();
                Log.i(TAG,"temp value  : "+temp);
                prefEditSettings.putBoolean(key_root_access,temp).commit();
                Toast.makeText(getContext(),"ROOT is Enabled",Toast.LENGTH_LONG);
                return temp;
            }catch(Exception ex){
                prefEditSettings.putBoolean(key_root_access,false).commit();
                Toast.makeText(getContext(),"ROOT Not Available",Toast.LENGTH_SHORT);
                return false;
            }
        }
        else{
            prefEditSettings.putBoolean(key_root_access,false).commit();
            Toast.makeText(getContext(),"ROOT is off",Toast.LENGTH_SHORT);
            return false;
        }
    }

    private void setSpinnerData(){

        storages_uri_list  = new ArrayList<>();
        storage_spinner_list = new ArrayList<>();

        Map<String,?> storage_uri_strings = sharedPrefExternalStorages.getAll();

        for(Map.Entry<String,?> str : storage_uri_strings.entrySet()){
            Uri temp_uri = Uri.parse(str.getValue().toString());

            if(DocumentsContract.isTreeUri(temp_uri)){
                showMsg(log_msg,"Tree Uri : "+temp_uri);
                storages_uri_list.add(temp_uri);
                storage_spinner_list.add(temp_uri.getPath());
            }else{
                showMsg(log_msg,"Not tree uri : "+temp_uri);
            }
        }

        storage_spinner_adapter = new ArrayAdapter(getContext(),android.R.layout.simple_spinner_dropdown_item,storage_spinner_list);
        storage_spinner.setAdapter(storage_spinner_adapter);
        storage_spinner_adapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_REPO_PATH:
                if (resultCode == RESULT_OK) {
                    Uri file_uri = data.getData();
                    File file = Utils.getFileForUri(file_uri);
                    text_repo_path.setText(file.getAbsolutePath());

                    if(file.exists() & file.isDirectory()){
                        prefEditSettings.putString(key_repository_folder,file.getAbsolutePath()).commit();
                        Log.i(TAG,"Repository Path Set: "+file.getPath());
                    }else{
                        Log.i(TAG,"Repository Path not Set: "+file.getPath());
                    }
                    Toast.makeText(getContext(), "Repository Path Set to : "+file.getPath(), Toast.LENGTH_SHORT).show();
                    break;
                }

            case REQUEST_CODE_EXTSD_CARD_PATH:{
                if(resultCode == RESULT_OK){
                    uri_extsd = data.getData();
                    prefEditExternalStorages.putString(uri_extsd.getLastPathSegment(),uri_extsd.toString()).commit();
                    showMsg(log_msg,"Added : "+uri_extsd.getPath());
                    setSpinnerData();
                    // Persist access permissions.
                    final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    getActivity().getContentResolver().takePersistableUriPermission(uri_extsd, takeFlags);

                }
                break;
            }

            case REQUEST_CODE_EXPORT_APK_PATH:{
                if(resultCode == RESULT_OK){
                    Uri uri = data.getData();
                    prefEditSettings.putString(key_export_apk_uri,uri.toString()).commit();
                    showMsg(log_msg,"Export Apk Path Set : "+uri.getPath());
                    editText_export_apk.setText(uri.getPath());
                    //Persist access permissions.
                    final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    getActivity().getContentResolver().takePersistableUriPermission(uri, takeFlags);
                }
            }
        }
    }

    private void showMsg(String msgtype, String str){

        if(msgtype.equals(toast_msg)){
            Toast.makeText(context,str,Toast.LENGTH_SHORT).show();
        }else {

        }
        Log.i(TAG,str);
    }



    //------------------------TEST----SHARE DIALOG DEMO ON BUTTON CLEAN----------------------------
//    public void sharetext(String text){
//        String mime = "text/plain";
//        String title = "Test Share";
//        Intent intent = ShareCompat.IntentBuilder.from(getActivity())
//                .setChooserTitle(title)
//                .setType(mime).setText(text).getIntent();
//        if(intent.resolveActivity(getContext().getPackageManager())!=null){
//            startActivity(intent);
//        }
//    }

}

package com.sunnykatiyar.AppManager;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.Nullable;
import androidx.core.app.ShareCompat;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.nononsenseapps.filepicker.FilePickerActivity;
import com.nononsenseapps.filepicker.Utils;
import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class AppSettingsFragment extends Fragment {

    private final String TAG = "APPSETTINGS FRAGMENT";
    private final int REQUEST_CODE_EXTSD_CARD_PATH = 17;
    private final int REQUEST_CODE_REPO_PATH = 23;


    public static final String key_repository_folder = "REPOSITORY_FOLDER";
    public static final String key_root_access = "ROOT_ACCESS";
    public static final String key_extsd_uri = "EXTSD_CARD_URI";
    public static final String path_not_set = "PATH NOT SET";

    public static String value_extsd_uri;
    public static boolean value_root_access ;
    public static boolean root_access;

    String value_repository_folder;

    Button buttonSetRepoPath;
    Button clearRepositoryPref;
    EditText text_repo_path;
    Switch root_switch;
    Context context = getActivity();
    Button btn_clear_extsd;
    Button btn_set_extsd;
    Uri uri_extsd;
    TextView label_extsd_path;

    public AppSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        value_root_access = MainActivity.sharedPrefAppSettings.getBoolean(key_root_access,false);
        root_access = getRootStatus(value_root_access);
        value_repository_folder = MainActivity.sharedPrefAppSettings.getString(key_repository_folder,path_not_set);
        value_extsd_uri =MainActivity.sharedPrefAppSettings.getString(key_extsd_uri,path_not_set);

//        Log.i(TAG,"External storage directory : "+Environment.getExternalStorageDirectory().getAbsolutePath());
//        Log.i(TAG,"External storage state : "+Environment.getExternalStorageState());
//        Log.i(TAG,"External storage public directory : "+ Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath());
//        Log.i(TAG,"External storage state of repo path : "+Environment.getExternalStorageState(new File(value_repository_folder)));
//        Log.i(TAG,"DATA DIRECTORY: "+Environment.getDataDirectory());
//        Log.i(TAG,"Download Cache Directory : "+ Environment.getDownloadCacheDirectory().getAbsolutePath());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_app_settings, container, false);

        text_repo_path = view.findViewById(R.id.text_path_repository);
        buttonSetRepoPath = view.findViewById(R.id.button_browse_repo);
        clearRepositoryPref = view.findViewById(R.id.button_clear_repo_data);
        root_switch = view.findViewById(R.id.switch_root_access);
        btn_clear_extsd = view.findViewById(R.id.buttton_clear_extsd);
        btn_set_extsd = view.findViewById(R.id.buttton_set_extsd);
        label_extsd_path = view.findViewById(R.id.label_exsd_path);


        if(value_extsd_uri.equals(path_not_set)){
            label_extsd_path.setText(R.string.hint_extsd_label);
        }else{
            label_extsd_path.setText(value_extsd_uri);
        }

        if(value_repository_folder.equals(path_not_set)){
            text_repo_path.setText("");
        }else{
            text_repo_path.setText(value_repository_folder);
        }

        root_switch.setChecked(root_access);


        //----------------------------SET REPO FOLDER ----------------------------

        buttonSetRepoPath.setOnClickListener(v -> {

                    Intent i = new Intent(getContext(), FilePickerActivity.class);
                    //Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                    i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
                    i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);

                    value_repository_folder = MainActivity.sharedPrefAppSettings.getString(key_repository_folder, path_not_set);

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
            MainActivity.prefEditRepository.clear().commit();
            MainActivity.prefEditAppSettings.remove(key_repository_folder).commit();
            text_repo_path.setText("");
            Log.i(TAG," Repository Preference Cleared.");
            Toast.makeText(getContext(), " Repository Preference Cleared.", Toast.LENGTH_SHORT).show();
        });

        //----------------------------SET EXTERNAL SDCARD ----------------------------

        btn_set_extsd.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
//               Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                Intent i = new Intent(getContext(), FilePickerActivity.class);
//                Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
//                i.addCategory(Intent.CATEGORY_OPENABLE);
//                 i.setType("");
                startActivityForResult(i, REQUEST_CODE_EXTSD_CARD_PATH);
            }
        });

        ContentResolver resolver = getContext().getContentResolver();
        List<UriPermission> perm_list = resolver.getPersistedUriPermissions();
        int k=0;
        for(UriPermission p:perm_list){
            Log.i(TAG," URi Permission "+k+" : "+p.toString());
            k++;
        }

        btn_clear_extsd.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // sharetext(text_repo_path.getText().toString());
                MainActivity.prefEditAppSettings.remove(key_extsd_uri).commit();
                label_extsd_path.setText(R.string.hint_extsd_label);
            }
        });


        //--------------------------------ROOT SWITCH CLICK LISTENER----------------------
        root_switch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            root_access = getRootStatus(isChecked);
            root_switch.setChecked(root_access);
        });

        return view;
    }

    //------------------------TEST----SHARE DIALOG DEMO ON BUTTON CLEAN----------------------------
    public void sharetext(String text){
        String mime = "text/plain";
        String title = "Test Share";
        Intent intent = ShareCompat.IntentBuilder.from(getActivity())
                .setChooserTitle(title)
                .setType(mime).setText(text).getIntent();
        if(intent.resolveActivity(getContext().getPackageManager())!=null){
            startActivity(intent);
        }
    }


    public boolean getRootStatus(boolean isChecked){
        if(isChecked)
        {
            if(Shell.rootAccess()){
                MainActivity.prefEditAppSettings.putBoolean(key_root_access,true).commit();
                Toast.makeText(getContext(),"ROOT is Enabled",Toast.LENGTH_LONG);
                return true;
            }
            else{
                MainActivity.prefEditAppSettings.putBoolean(key_root_access,false).commit();
                Toast.makeText(getContext(),"ROOT Not Available",Toast.LENGTH_SHORT);
                return false;
            }
        }else{
            MainActivity.prefEditAppSettings.putBoolean(key_root_access,false).commit();
            Toast.makeText(getContext(),"ROOT is off",Toast.LENGTH_SHORT);
            return false;
        }
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
                        MainActivity.prefEditAppSettings.putString(key_repository_folder,file.getAbsolutePath()).commit();
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
                    MainActivity.prefEditAppSettings.putString(key_extsd_uri,uri_extsd.toString()).commit();
                    Log.i(TAG,"External SD Uri : "+uri_extsd);
                    label_extsd_path.setText(uri_extsd.toString());
                    // Persist access permissions.
                  //  final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                  //  getActivity().getContentResolver().takePersistableUriPermission(uri_extsd, takeFlags);
                }
            }
        }
    }

}

package com.sunnykatiyar.AppManager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.nononsenseapps.filepicker.FilePickerActivity;
import com.nononsenseapps.filepicker.Utils;

import java.io.File;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class AppSettingsFragment extends Fragment {

    private final String TAG = "APPSETTINGS FRAGMENT";
    public static final String key_repository_folder = "REPOSITORY_FOLDER";
    public static final String key_root_access = "ROOT_ACCESS";

    String value_repository_folder;

    Button buttonSavePath;
    Button clearReositoryPref;
    EditText ediRepositoryPath;
    Switch root_selected;
    final String path_not_set = "PATH NOT SET";
    Context context = getActivity();

    public AppSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_app_settings, container, false);

        ediRepositoryPath = view.findViewById(R.id.text_path_repository);
        buttonSavePath = view.findViewById(R.id.button_save_paths);
        clearReositoryPref = view.findViewById(R.id.button_clear_repository_pref);
        value_repository_folder = AppListActivity.sharedPrefAppSettings.getString(key_repository_folder,"Path Not Set");
        ediRepositoryPath.setText(value_repository_folder);
        root_selected = view.findViewById(R.id.switch_root_access);
        ediRepositoryPath.setOnClickListener(onclickSetRepositoryPath);

        buttonSavePath.setOnClickListener(v -> {
            File f = new File(ediRepositoryPath.getText().toString());
            if(f.exists() & f.isDirectory()){
                AppListActivity.prefEditAppSettings.putString(key_repository_folder,f.getAbsolutePath()).commit();
            }
            Log.i(TAG,"Repository Path Set: "+f.getAbsolutePath());
            Toast.makeText(getContext(), "Repository Path Set to : "+f.getAbsolutePath(), Toast.LENGTH_SHORT).show();

        });

        root_selected.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppListActivity.prefEditAppSettings.putBoolean(key_root_access,isChecked).commit();
            Toast.makeText(getContext(), " ROOT Actions : "+isChecked, Toast.LENGTH_LONG).show();
            Log.e(TAG, "ROOT SELECTED :" + isChecked);
        });

        clearReositoryPref.setOnClickListener(v -> {
            AppListActivity.prefEditRepository.clear().commit();
            Log.i(TAG," Repository Preference Cleared.");
            Toast.makeText(getContext(), " Repository Preference Cleared.", Toast.LENGTH_SHORT).show();
        });
        return view;
    }

    EditText.OnClickListener onclickSetRepositoryPath = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Intent i = new Intent(getContext(),FilePickerActivity.class);
            i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
            i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
            i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
            value_repository_folder = AppListActivity.sharedPrefAppSettings.getString(key_repository_folder,path_not_set);
            File f = new File(value_repository_folder);

            if(f.exists() & f.isFile()){
                i.putExtra(FilePickerActivity.EXTRA_START_PATH, f.getParent());

            }else if(f.exists() & f.isDirectory()){
                i.putExtra(FilePickerActivity.EXTRA_START_PATH, f.getAbsoluteFile());
            }else{
                i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());
            }

            startActivityForResult(i, 4);

        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {


        switch (requestCode) {
            case 4:
                if (resultCode == RESULT_OK) {
                    List<Uri> files = Utils.getSelectedFilesFromResult(data);
                    File file = Utils.getFileForUri(files.get(0));
                    ediRepositoryPath.setText(file.getPath());
                    break;
                }

                super.onActivityResult(requestCode, resultCode, data);
        }
    }


}

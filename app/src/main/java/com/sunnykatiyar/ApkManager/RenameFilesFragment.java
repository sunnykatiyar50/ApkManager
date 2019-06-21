package com.sunnykatiyar.ApkManager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.nononsenseapps.filepicker.FilePickerActivity;
import com.nononsenseapps.filepicker.Utils;

import java.io.File;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * A placeholder fragment containing a simple view.
 */
public class RenameFilesFragment extends Fragment {


    public RenameFilesFragment() {
    }

    Button btn_browse;
    EditText file_search_path;
    Context context = this.getActivity();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rename_files, container, false);


        btn_browse = view.findViewById(R.id.btn_file_path);
        btn_browse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, FilePickerActivity.class);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);

                File f = new File(file_search_path.toString());

                if(f.exists() & f.isFile()){
                    i.putExtra(FilePickerActivity.EXTRA_START_PATH, f.getParent());

                }else if(f.exists() & f.isDirectory()){
                    i.putExtra(FilePickerActivity.EXTRA_START_PATH, f.getAbsoluteFile());
                }else{
                    i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());
                }

                startActivityForResult(i, 3);
            }
        });


        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        switch(requestCode)
        {
            case 3 : if(resultCode == RESULT_OK)
            {
                //Use the provided utility method to parse the result
                List<Uri> files = Utils.getSelectedFilesFromResult(data);
                File file = Utils.getFileForUri(files.get(0));
                file_search_path.setText(file.getPath());
            }
        }
    }


}

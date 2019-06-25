package com.sunnykatiyar.AppManager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.nononsenseapps.filepicker.FilePickerActivity;
import com.nononsenseapps.filepicker.Utils;

import java.io.File;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * A placeholder fragment containing a simple view.
 */
public class RenameFilesFragment extends android.support.v4.app.Fragment {

    public RenameFilesFragment() {
    }

    Button btn_browse;
    EditText file_search_path;
    Context context = getContext();
    TextView files_text_masg;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rename_files, container, false);
        btn_browse = view.findViewById(R.id.button_file_renamer_path);
        files_text_masg = view.findViewById(R.id.files_text_msg);
        file_search_path = view.findViewById(R.id.text_file_renamer_path);

        btn_browse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), FilePickerActivity.class);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
                i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());
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

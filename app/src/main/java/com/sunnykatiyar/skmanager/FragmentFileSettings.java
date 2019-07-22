package com.sunnykatiyar.skmanager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import androidx.annotation.Nullable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.nononsenseapps.filepicker.FilePickerActivity;
import com.nononsenseapps.filepicker.Utils;

import java.io.File;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * A placeholder fragment containing a simple view.
 */
class FragmentFileSettings extends Fragment {

    public FragmentFileSettings() {
    }

    private EditText file_search_path;
    Context context = getContext();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_files_settings, container, false);
        Button btn_browse = view.findViewById(R.id.button_file_renamer_path);
        TextView files_text_masg = view.findViewById(R.id.files_text_msg);
        file_search_path = view.findViewById(R.id.text_file_renamer_path);

        btn_browse.setOnClickListener(v -> {
            Intent i = new Intent(getContext(), FilePickerActivity.class);
            i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
            i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
            i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
            i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());
            startActivityForResult(i, 3);
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        if (requestCode == 3) {
            if (resultCode == RESULT_OK) {
                //Use the provided utility method to parse the result
                List<Uri> files = Utils.getSelectedFilesFromResult(data);
                File file = Utils.getFileForUri(files.get(0));
                file_search_path.setText(file.getPath());
            }
        }
    }


}

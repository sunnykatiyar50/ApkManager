package com.sunnykatiyar.AppManager;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FilesListFragment extends Fragment {


    Button button_browse_path ;
    TextView text_file_path;

    public FilesListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the fragment_help for this fragment

        View v = inflater.inflate(R.layout.fragment_files_list, container, false);

        button_browse_path = v.findViewById(R.id.button_files_browser);
        text_file_path = v.findViewById(R.id.text_files_tab_path);

        return v;
    }

}

package com.sunnykatiyar.AppManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
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

///**
// * A simple {@link Fragment} subclass.
// * Activities that contain this fragment must implement the
// * {@link AppSettingsFragment.OnFragmentInteractionListener} interface
// * to handle interaction events.
// * Use the {@link AppSettingsFragment#newInstance} factory method to
// * create an instance of this fragment.
// */

public class AppSettingsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static final String key_from_folder = "FROM_FOLDER";
    private static final String key_to_folder = "TO_FOLDER";
    String value_from_folder ;
    String value_to_folder ;

    Button buttonSavePaths;
    EditText editFromPath;
    EditText editToPath;
    String path_not_set = "PATH NOT SET";
    Context context = getActivity();


//    private OnFragmentInteractionListener mListener;

    public AppSettingsFragment() {
        // Required empty public constructor
    }
//
//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        void onFragmentInteraction(Uri uri);
//    }

    // TODO: Rename and change types and number of parameters
    public static AppSettingsFragment newInstance(String param1, String param2) {
        AppSettingsFragment fragment = new AppSettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_app_settings, container, false);

        value_from_folder = AppListActivity.sharedPrefSettings.getString(key_from_folder,"Path Not Set");
        value_to_folder = AppListActivity.sharedPrefSettings.getString(key_to_folder,"Path Not Set");

        editFromPath = view.findViewById(R.id.text_path_from);
        editToPath = view.findViewById(R.id.text_path_to);
        buttonSavePaths = view.findViewById(R.id.button_save_paths);

        editFromPath.setText(value_from_folder);
        editToPath.setText(value_to_folder);

        editFromPath.setOnClickListener(onclickSetPathFrom);
        editToPath.setOnClickListener(onclickSetPathTo);


        buttonSavePaths.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppListActivity.prefEditSettings.putString(key_from_folder,editFromPath.getText().toString());
                AppListActivity.prefEditSettings.putString(key_to_folder,editToPath.getText().toString());
                AppListActivity.prefEditSettings.commit();
            }
        });

        return view;
    }

    EditText.OnClickListener onclickSetPathFrom = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Intent i = new Intent(getContext(),FilePickerActivity.class);
            i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
            i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
            i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
            value_from_folder = AppListActivity.sharedPrefSettings.getString(key_to_folder,path_not_set);
            File f = new File(value_from_folder);

            if(f.exists() & f.isFile()){
                i.putExtra(FilePickerActivity.EXTRA_START_PATH, f.getParent());

            }else if(f.exists() & f.isDirectory()){
                i.putExtra(FilePickerActivity.EXTRA_START_PATH, f.getAbsoluteFile());
            }else{
                i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());
            }

            startActivityForResult(i, 3);

        }
    };

    EditText.OnClickListener onclickSetPathTo = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Intent i = new Intent(getContext(),FilePickerActivity.class);
            i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
            i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
            i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
            value_to_folder = AppListActivity.sharedPrefSettings.getString(key_to_folder,path_not_set);
            File f = new File(value_to_folder);

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

            case 3:
                if (resultCode == RESULT_OK) {
                    List<Uri> files = Utils.getSelectedFilesFromResult(data);
                    File file = Utils.getFileForUri(files.get(0));
                    editFromPath.setText(file.getPath());
                    break;
                }
            case 4:
                if (resultCode == RESULT_OK) {
                    List<Uri> files = Utils.getSelectedFilesFromResult(data);
                    File file = Utils.getFileForUri(files.get(0));
                    editToPath.setText(file.getPath());
                    break;
                }

                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        mListener = null;
    }


}

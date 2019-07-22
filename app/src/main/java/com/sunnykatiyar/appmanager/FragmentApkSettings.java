package com.sunnykatiyar.appmanager;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static com.sunnykatiyar.appmanager.ActivityMain.sharedPrefApkManager;


class FragmentApkSettings extends Fragment implements
        AdapterView.OnItemSelectedListener{

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private Spinner spinner1;
    private Spinner spinner3;
    private Spinner spinner5;
    private Spinner spinner7;
    private EditText edit2;
    private EditText edit4;
    private EditText edit6;
    private EditText edit8;

    private static List<String> spinner_items ;
    private final String TAG = "RENAME_APK_SETTINGS ";
    private TextView name_format;

    public final static  String PREF_NAME = "com.sunnykatiyar.appmanager.RENAME";
    public final static String key_global_path ="GLOBAL_PATH";
    public final static String name_part_1 = "NAME_PART_1";
    public final static String name_part_2 = "NAME_PART_2";
    public final static String name_part_3 = "NAME_PART_3";
    public final static String name_part_4 = "NAME_PART_4";
    public final static String name_part_5 = "NAME_PART_5";
    public final static String name_part_6 = "NAME_PART_6";
    public final static String name_part_7 = "NAME_PART_7";
    public final static String name_part_8 = "NAME_PART_8";

    private final static String empty = "Nothing";
    private final static String app_name = "AppName";
    private final static String version_name = "VersionName";
    private final static String version_code = "VersionCode";
    private final static String pkg_name = "PackageName";
    private final static String file_size = "FileSize";

    private final static String spinner_items_set= "SPINNER_ITEMS_SET";
    public final static String name_format_data_saved = "FORMAT_DATA_SAVED";
    public FragmentApkSettings() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String mParam1 = getArguments().getString(ARG_PARAM1);
            String mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_apk_settings, container, false);



        spinner_items = new ArrayList<>();

        spinner1 = v.findViewById(R.id.spinner1);
        edit2 = v.findViewById(R.id.editText2);
        spinner3 = v.findViewById(R.id.spinner3);
        edit4 = v.findViewById(R.id.editText4);
        spinner5 = v.findViewById(R.id.spinner5);
        edit6 = v.findViewById(R.id.editText6);
        spinner7 = v.findViewById(R.id.spinner7);
        edit8 = v.findViewById(R.id.editText8);

        spinner1.setOnItemSelectedListener(this);
        spinner3.setOnItemSelectedListener(this);
        spinner5.setOnItemSelectedListener(this);
        spinner7.setOnItemSelectedListener(this);

        Button button_save = v.findViewById(R.id.buttton_add_extsd);
        Button button_clear = v.findViewById(R.id.buttton_clear_extsd);
        name_format = v.findViewById(R.id.text_format);


        setNewSpinnerData();

        retrieveSpinnerData();

        setNameFormatLabel();

        button_save.setOnClickListener(v12 -> saveFormat());

        button_clear.setOnClickListener(v1 -> clearFormat());

        return v;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        return super.onOptionsItemSelected(item);
    }

    private String getItem(int i){
        switch(i){
            case 0:{  return "";              }
            case 1:{  return spinner_items.get(1) ;    }
            case 2:{  return spinner_items.get(2) ;    }
            case 3:{   return spinner_items.get(3) ;  }
            case 4:{  return spinner_items.get(4) ;   }
            case 5:{   return spinner_items.get(5) ;    }
            default : { return "";             }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.i(TAG,"OnClick Called: position :"+position);

        switch(parent.getId()){

            case R.id.spinner1 :{
                spinner1.setSelection(position);
                Log.i(TAG,"OnClick : Spinner1 : "+parent.getItemAtPosition(position).toString());
                Toast.makeText(getContext(),parent.getItemAtPosition(position).toString(),Toast.LENGTH_SHORT).show();
                break;
            }

            case R.id.spinner3 :{
                spinner3.setSelection(position);
                Log.i(TAG,"OnClick : Spinner3 : "+parent.getItemAtPosition(position).toString());
                Toast.makeText(getContext(),parent.getItemAtPosition(position).toString(),Toast.LENGTH_SHORT).show();
                break;
            }

            case R.id.spinner5 :{
                spinner5.setSelection(position);
                Log.i(TAG,"OnClick : Spinner5 : "+parent.getItemAtPosition(position).toString());
                Toast.makeText(getContext(),parent.getItemAtPosition(position).toString(),Toast.LENGTH_SHORT).show();
                break;
            }

            case R.id.spinner7 :{
                spinner7.setSelection(position);
                Log.i(TAG,"OnClick : Spinner6 : "+parent.getItemAtPosition(position).toString());
                Toast.makeText(getContext(),parent.getItemAtPosition(position).toString(),Toast.LENGTH_SHORT).show();
                break;
            }

            default :{   Log.i(TAG,"in switch spinner click unknown item :");
                            break;
                        }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        parent.setSelection(0);
    }

    private void setNewSpinnerData(){
        spinner_items = new ArrayList<>();
        spinner_items.add(empty);
        spinner_items.add(app_name);
        spinner_items.add(version_name);
        spinner_items.add(version_code);
        spinner_items.add(file_size);
        spinner_items.add(pkg_name);

        ArrayAdapter<String> spin_adapter1 = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, spinner_items);
        spin_adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<String> spin_adapter3 = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, spinner_items);
        spin_adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<String> spin_adapter5 = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, spinner_items);
        spin_adapter5.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<String> spin_adapter7 = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, spinner_items);
        spin_adapter7.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner1.setAdapter(spin_adapter1);
        spinner3.setAdapter(spin_adapter3);
        spinner5.setAdapter(spin_adapter5);
        spinner7.setAdapter(spin_adapter7);

        ActivityMain.prefEditApkManager.putBoolean(spinner_items_set,true).commit();
        Log.i(TAG,"Spinners Data is Set ... ");
    }

    private void clearFormat(){
        ActivityMain.prefEditApkManager.remove(name_part_1);
        ActivityMain.prefEditApkManager.remove(name_part_2);
        ActivityMain.prefEditApkManager.remove(name_part_3);
        ActivityMain.prefEditApkManager.remove(name_part_4);
        ActivityMain.prefEditApkManager.remove(name_part_5);
        ActivityMain.prefEditApkManager.remove(name_part_6);
        ActivityMain.prefEditApkManager.remove(name_part_7);
        ActivityMain.prefEditApkManager.remove(name_part_8);
        ActivityMain.prefEditApkManager.remove(name_format_data_saved);
        ActivityMain.prefEditApkManager.commit();

        Log.i(TAG,"Cleared Name Format Data");

        setNewSpinnerData();
    }

    private void saveFormat(){
        ActivityMain.prefEditApkManager.putInt(name_part_1,spinner1.getSelectedItemPosition());
        ActivityMain.prefEditApkManager.putString(name_part_2,edit2.getText().toString());
        ActivityMain.prefEditApkManager.putInt(name_part_3,spinner3.getSelectedItemPosition());
        ActivityMain.prefEditApkManager.putString(name_part_4,edit4.getText().toString());
        ActivityMain.prefEditApkManager.putInt(name_part_5,spinner5.getSelectedItemPosition());
        ActivityMain.prefEditApkManager.putString(name_part_6,edit6.getText().toString());
        ActivityMain.prefEditApkManager.putInt(name_part_7,spinner7.getSelectedItemPosition());
        ActivityMain.prefEditApkManager.putString(name_part_8,edit8.getText().toString());
        ActivityMain.prefEditApkManager.putBoolean(name_format_data_saved,true);
        ActivityMain.prefEditApkManager.commit();

        setNameFormatLabel();

        Log.i(TAG,"NameFormatData is SET");

    }

    private void setNameFormatLabel(){
        Log.i(TAG,"Set Name Format Label");

        String name_format_string = getItem(sharedPrefApkManager.getInt(name_part_1, 1)) +
                sharedPrefApkManager.getString(name_part_2, "_v") +
                getItem(sharedPrefApkManager.getInt(name_part_3, 2)) +
                sharedPrefApkManager.getString(name_part_4, "_") +
                getItem(sharedPrefApkManager.getInt(name_part_5, 3)) +
                sharedPrefApkManager.getString(name_part_6, "") +
                getItem(sharedPrefApkManager.getInt(name_part_7, 0)) +
                sharedPrefApkManager.getString(name_part_8, "");

        name_format.setText(name_format_string);
    }

    private void retrieveSpinnerData(){

        Log.i(TAG,"retrieveSpinnerData()");


       // if(sharedPrefApkManager.contains(name_format_data_saved)){
            Log.i(TAG,"Getting Data from SharedPreferences ... ");

            spinner1.setSelection(sharedPrefApkManager.getInt(name_part_1,1),true);
            Log.i(TAG,String.valueOf(sharedPrefApkManager.getInt(name_part_1,1)));

            edit2.setText(sharedPrefApkManager.getString(name_part_2,"_v"));

            spinner3.setSelection(sharedPrefApkManager.getInt(name_part_3,2),false);
            Log.i(TAG,String.valueOf(sharedPrefApkManager.getInt(name_part_3,2)));

            edit4.setText(sharedPrefApkManager.getString(name_part_4,"_"));

            spinner5.setSelection(sharedPrefApkManager.getInt(name_part_5,3));
            Log.i(TAG,String.valueOf(sharedPrefApkManager.getInt(name_part_5,3)));

            edit6.setText(sharedPrefApkManager.getString(name_part_6,""));

            spinner7.setSelection(sharedPrefApkManager.getInt(name_part_7,0));
            Log.i(TAG,String.valueOf(sharedPrefApkManager.getInt(name_part_7,0)));

            edit8.setText(sharedPrefApkManager.getString(name_part_8,""));
//        }
//        else{
//            Log.i(TAG,"NO FORMAT DATA SAVED IN SHARED_PREFERENCES");
//        }
    }

}

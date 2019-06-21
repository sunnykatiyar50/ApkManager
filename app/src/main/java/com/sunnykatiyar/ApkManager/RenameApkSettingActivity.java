package com.sunnykatiyar.ApkManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nononsenseapps.filepicker.FilePickerActivity;
import com.nononsenseapps.filepicker.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RenameApkSettingActivity extends AppCompatActivity  implements
        AdapterView.OnItemSelectedListener{

    Context context = RenameApkSettingActivity.this;
    public static Spinner spinner1;
    public static Spinner spinner3;
    public static Spinner spinner5;
    public static Spinner spinner7;

    public static EditText edit2;
    public static EditText edit4;
    public static EditText edit6;
    public static EditText edit8;

    public static List<String> spinner_items ;
    private final String TAG = "RENAME_APK_SETTINGS ";
    Button button_save;
    Button button_clear;
    Button button_get_path;
    TextView name_format;
    String name_format_string;
    EditText global_path;
    ArrayAdapter<String> spin_adapter1;
    ArrayAdapter<String> spin_adapter3;
    ArrayAdapter<String> spin_adapter5;
    ArrayAdapter<String> spin_adapter7;

    public static SharedPreferences sharedPref;
    public static SharedPreferences.Editor prefEditor;

    public final static  String PREF_NAME = "com.sunnykatiyar.ApkManager.RENAME";
    public final static String key_global_path ="GLOBAL_PATH";
    public final static  String name_part_1 = "NAME_PART_1";
    public final static  String name_part_2 = "NAME_PART_2";
    public final static String name_part_3 = "NAME_PART_3";
    public final static String name_part_4 = "NAME_PART_4";
    public final static String name_part_5 = "NAME_PART_5";
    public final static String name_part_6 = "NAME_PART_6";
    public final static String name_part_7 = "NAME_PART_7";
    public final static String name_part_8 = "NAME_PART_8";

    public final static String empty = "Nothing";
    public final static String app_name = "AppName";
    public final static String version_name = "VersionName";
    public final static String version_code = "VersionCode";
    public final static String pkg_name = "PackageName";
    public final static String file_size = "FileSize";

    public final static String spinner_items_set= "SPINNER_ITEMS_SET";
    public final static String name_format_data_saved = "FORMAT_DATA_SAVED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rename_apk_setting);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        sharedPref = getSharedPreferences(PREF_NAME,MODE_PRIVATE);
        prefEditor = sharedPref.edit();
        spinner_items = new ArrayList<>();

//        spinner1 = new Spinner(this);
//        spinner3= new Spinner(this);
//        spinner5= new Spinner(this);
//        spinner7= new Spinner(this);

//        edit2 = new EditText(this);
//        edit4 = new EditText(this);;
//        edit6 = new EditText(this);;
//        edit8 = new EditText(this);;

         spinner1 = findViewById(R.id.spinner1);
         edit2 = findViewById(R.id.editText2);
         spinner3 = findViewById(R.id.spinner3);
         edit4 = findViewById(R.id.editText4);
         spinner5 = findViewById(R.id.spinner5);
         edit6 = findViewById(R.id.editText6);
         spinner7 = findViewById(R.id.spinner7);
         edit8 = findViewById(R.id.editText8);

        spinner1.setOnItemSelectedListener(this);
        spinner3.setOnItemSelectedListener(this);
        spinner5.setOnItemSelectedListener(this);
        spinner7.setOnItemSelectedListener(this);

        button_save = findViewById(R.id.buttton_save);
        button_clear = findViewById(R.id.buttton_clear);
        name_format = findViewById(R.id.text_format);
        global_path=findViewById(R.id.edit_global_path);
        button_get_path = findViewById(R.id.button_global_path);

        setNewSpinnerData();
        
        retrieveSpinnerData();
        
        button_get_path.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, FilePickerActivity.class);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
                File f = new File(global_path.toString());

                if(f.exists() & f.isFile()){
                    i.putExtra(FilePickerActivity.EXTRA_START_PATH, f.getParent());

                }else if(f.exists() & f.isDirectory()){
                    i.putExtra(FilePickerActivity.EXTRA_START_PATH, f.getAbsoluteFile());
                }else{
                    i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());
                }                startActivityForResult(i, 2);
            }
        });

        global_path.setText(sharedPref.getString(key_global_path,"Path Not Set"));
        setNameFormatLabel();

        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    saveFormat();
            }
        });

        button_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearFormat();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {

        if(requestCode==2){
            if(resultCode == RESULT_OK)
            {
                List<Uri> files = Utils.getSelectedFilesFromResult(intent);
                File file = Utils.getFileForUri(files.get(0));
                global_path.setText(file.getPath());
                prefEditor.putString(key_global_path,global_path.getText().toString());
                prefEditor.commit();
            }
        }
    }

    @Override
    public void onBackPressed() {
            super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public String getItem(int i){
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
        Log.i(TAG,"OnClick Called: ");
        switch(view.getId()){
            case R.id.spinner1 :{
                parent.setSelection(position);
                Log.i(TAG,"OnClick : "+parent.getItemAtPosition(position).toString());
                Toast.makeText(context,parent.getItemAtPosition(position).toString(),Toast.LENGTH_SHORT);
            }

            case R.id.spinner3 :{
                spinner3.setSelection(position);
                Log.i(TAG,"OnClick : "+parent.getItemAtPosition(position).toString());
                Toast.makeText(context,parent.getItemAtPosition(position).toString(),Toast.LENGTH_SHORT);
            }

            case R.id.spinner5 :{
                parent.setSelection(position);
                Log.i(TAG,"OnClick : "+parent.getItemAtPosition(position).toString());
                Toast.makeText(context,parent.getItemAtPosition(position).toString(),Toast.LENGTH_SHORT);
            }

            case R.id.spinner7 :{
                spinner7.setSelection(position);
                Log.i(TAG,"OnClick : "+parent.getItemAtPosition(position).toString());
                Toast.makeText(context,parent.getItemAtPosition(position).toString(),Toast.LENGTH_SHORT);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        parent.setSelection(0);
    }

    public void setNewSpinnerData(){
        spinner_items = new ArrayList<>();
        spinner_items.add(empty);
        spinner_items.add(app_name);
        spinner_items.add(version_name);
        spinner_items.add(version_code);
        spinner_items.add(file_size);
        spinner_items.add(pkg_name);

        spin_adapter1 = new ArrayAdapter<>(context,android.R.layout.simple_spinner_item,spinner_items);
        spin_adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spin_adapter3 = new ArrayAdapter<>(context,android.R.layout.simple_spinner_item,spinner_items);
        spin_adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spin_adapter5 = new ArrayAdapter<>(context,android.R.layout.simple_spinner_item,spinner_items);
        spin_adapter5.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spin_adapter7 = new ArrayAdapter<>(context,android.R.layout.simple_spinner_item,spinner_items);
        spin_adapter7.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner1.setAdapter(spin_adapter1);
        spinner3.setAdapter(spin_adapter3);
        spinner5.setAdapter(spin_adapter5);
        spinner7.setAdapter(spin_adapter7);

//        prefEditor.putInt("__EMPTY__",0);
//        prefEditor.putInt("APP_NAME",1);
//        prefEditor.putInt("VERSION_NAME",2);
//        prefEditor.putInt("VERSION_CODE",3);
//        prefEditor.putInt("FILE_SIZE",4);
//        prefEditor.putInt("PACKAGE_NAME",5);

        prefEditor.putBoolean(spinner_items_set,true);
        Log.i(TAG,"Spinners Data is Set ... ");
        prefEditor.commit();
    }

    public void clearFormat(){
        prefEditor.remove(name_part_1);
        prefEditor.remove(name_part_2);
        prefEditor.remove(name_part_3);
        prefEditor.remove(name_part_4);
        prefEditor.remove(name_part_5);
        prefEditor.remove(name_part_6);
        prefEditor.remove(name_part_7);
        prefEditor.remove(name_part_8);
        prefEditor.remove(name_format_data_saved);
        prefEditor.commit();

        Log.i(TAG,"Cleared Name Format Data");


        setNewSpinnerData();
    }

    public void saveFormat(){
        prefEditor.putInt(name_part_1,spinner1.getSelectedItemPosition());
        prefEditor.putString(name_part_2,edit2.getText().toString());
        prefEditor.putInt(name_part_3,spinner3.getSelectedItemPosition());
        prefEditor.putString(name_part_4,edit4.getText().toString());
        prefEditor.putInt(name_part_5,spinner5.getSelectedItemPosition());
        prefEditor.putString(name_part_6,edit6.getText().toString());
        prefEditor.putInt(name_part_7,spinner7.getSelectedItemPosition());
        prefEditor.putString(name_part_8,edit8.getText().toString());
        prefEditor.putBoolean(name_format_data_saved,true);
        prefEditor.commit();

        setNameFormatLabel();

        Log.i(TAG,"NameFormatData is SET");

    }

    public void setNameFormatLabel(){
        name_format_string = getItem(sharedPref.getInt(name_part_1,1))+
                sharedPref.getString(name_part_2,"_v")+
                getItem(sharedPref.getInt(name_part_3,2))+
                sharedPref.getString(name_part_4,"_")+
                getItem(sharedPref.getInt(name_part_5,3))+
                sharedPref.getString(name_part_6,"")+
                getItem(sharedPref.getInt(name_part_7,0))+
                sharedPref.getString(name_part_8,"");

        name_format.setText(name_format_string);
    }
    
    public void retrieveSpinnerData(){
        
        if(sharedPref.contains(name_format_data_saved)){
            Log.i(TAG,"Getting Data from SharedPreferences ... ");

            spinner1.setSelection(sharedPref.getInt(name_part_1,1));
            Log.i(TAG,String.valueOf(sharedPref.getInt(name_part_1,1)));

            edit2.setText(sharedPref.getString(name_part_2,"_v"));

            spinner3.setSelection(sharedPref.getInt(name_part_3,2));
            Log.i(TAG,String.valueOf(sharedPref.getInt(name_part_3,2)));

            edit4.setText(sharedPref.getString(name_part_4,"_"));

            spinner5.setSelection(sharedPref.getInt(name_part_5,3));
            Log.i(TAG,String.valueOf(sharedPref.getInt(name_part_5,3)));

            edit6.setText(sharedPref.getString(name_part_6,""));

            spinner7.setSelection(sharedPref.getInt(name_part_7,0));
            Log.i(TAG,String.valueOf(sharedPref.getInt(name_part_7,0)));

            edit8.setText(sharedPref.getString(name_part_8,""));
        }
        else{
            Log.i(TAG,"NO FORMAT DATA SAVED IN SHARED_PREFERENCES");
        }
    }
}

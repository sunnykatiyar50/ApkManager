package com.sunnykatiyar.AppManager;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class FileOperations {


    Uri src_uri;
    Uri dest_uri;
    Context context;
    FileInputStream fis;
    FileOutputStream fos;

    public FileOperations(Uri src,Uri dest){
        this.src_uri = src;
        this.dest_uri = dest;
    }

    private void setStreams(){
        try {
            fis = new FileInputStream(new File(src_uri.getPath()));
            fos = new FileOutputStream(new File(dest_uri.getPath()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    


}

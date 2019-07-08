package com.sunnykatiyar.appmanager;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import static androidx.core.graphics.TypefaceCompatUtil.closeQuietly;

public class ClassFileOperations {



    Context context;

    final String TAG = "_CLASS_FILE_OPERATIONS : ";

    public ClassFileOperations(Context context){
       this.context =context;
    }

    private void setStreams( Uri src_uri,Uri dest_uri){
        FileInputStream fis;
        FileOutputStream fos;

        try {
            fis = new FileInputStream(new File(src_uri.getPath()));
            fos = new FileOutputStream(new File(dest_uri.getPath()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Uri[] listFiles(Context context, Uri self) {
        final ContentResolver resolver = context.getContentResolver();
        final Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(self,
                DocumentsContract.getDocumentId(self));
        final List<Uri> results = new ArrayList<Uri>();

        Cursor c = null;
        try {
            c = resolver.query(childrenUri, new String[] {
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID }, null, null, null);
            while (c.moveToNext()) {
                final String documentId = c.getString(0);
                final Uri documentUri = DocumentsContract.buildDocumentUriUsingTree(self,
                        documentId);
                results.add(documentUri);
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed query: " + e);
        } finally {
            closeQuietly(c);
        }

        return results.toArray(new Uri[results.size()]);
    }

    public boolean copyFileTo(File src, File dest){

        boolean result ;
        FileChannel src_channel = null;
        FileChannel dest_channel = null;

        try{
            if(!dest.exists()) {
                dest.createNewFile();
            }
            dest_channel = new FileOutputStream(dest).getChannel();
            Log.e(TAG,"Out Stream Opened Succesfully : ");
        }catch(Exception ex){
            Log.e(TAG,"Error opening out stream : "+ex);
        }

        try{
            src_channel = new FileInputStream(src).getChannel();
            Log.e(TAG,"Input Stream Opened Succesfully : ");
        }catch(Exception ex){
            Log.e(TAG,"Error opening input stream : "+ex);
        }


        try{
            dest_channel.transferFrom(src_channel,0,src_channel.size());
            Log.i(TAG,"Apk Extracted Succesfully.");
            result=true;
        }catch(Exception ex) {
            Log.e(TAG, "Error Extracting Apk file : " + ex);
            result=false;
        }finally {
            try {
                src_channel.close();
                dest_channel.close();
                Log.i(TAG,"Streams Closed Succesfully.");
            } catch (IOException ex) {
                Log.e(TAG, "Error Closing Streams : " + ex);
            }
        }

        return result;
    }

}

package com.sunnykatiyar.appmanager;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class ObjectFile {

    File file;
    DocumentFile file_doc;
    String doc_id;
    Uri uri;
    Uri parent_uri;
    String file_name;
    String file_size;
    String creation_time;
    String flags;
    String access_permission;
    String icon_path;
    final String TAG = "OBJECT_FILE :";
    String modification_time;
    String file_type;
    boolean check_box_state = false;
    Context context;
    
    
    public ObjectFile(Cursor cursor, Uri uri,Context context){

        this.context = context;
        this.uri = uri;
        this.file_doc = DocumentFile.fromTreeUri(context, uri);
        this.doc_id = cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID));
        this.file_name = cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
        this.file_type = cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
        icon_path = cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_ICON));
        this.flags = cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_FLAGS));
        this.file_size = convertSizeToFormat(cursor.getLong(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE)));

//        Bundle bundle  =     DocumentsContract.getDocu

//        if(file_doc.isDirectory()){
//            this.file_size = "Directory : "+file_doc.listFiles().length+" files";
//        }
        this.modification_time = convertTimeToFormat(cursor.getLong(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)));
//      this.parent_uri = Uri.parse(uri.toString().substring(0,uri.toString().lastIndexOf(file_name)));
        this.parent_uri = Uri.parse(uri.toString().replace(Uri.encode(file_name),""));

        if(this.doc_id.endsWith(":")){
            this.parent_uri=null;
        }
        //this.access_permission = getPermissionFromDoc(this.file_doc);

        Log.i(TAG,"-------------------------------------------------------------------------------------------------------------------");
        Log.i(TAG,"NEW_DOCUMENT_CREATED : "+file_name);
//      Log.i(TAG,"DOCUMENTID : "+doc_id);
        Log.i(TAG,"URI : "+uri);
        Log.i(TAG,"PAR : "+parent_uri);
//        Log.i(TAG,"\nDisplayName : "+file_name);
//        Log.i(TAG,"\nFLAGS : "+flags);
//        Log.i(TAG,"\nicon path : "+icon_path);
//        Log.i(TAG,"\ngetLastPathSegment : "+uri.getLastPathSegment());
//        Log.i(TAG,"\ngetEncodedSchemeSpecificPart : "+uri.getEncodedSchemeSpecificPart());
//        Log.i(TAG,"\ngetEncodedAuthority : "+uri.getEncodedAuthority());
//       Log.i(TAG,"\ngetEncodedPath : "+uri.getEncodedPath());
    }

    public ObjectFile(Uri uri, Context c){

        this.context = c;
        this.uri = uri;
        this.file = new File(uri.getPath())  ;
        this.file_doc = DocumentFile.fromSingleUri(context, uri);
        this.file_name = file_doc.getName();
        this.file_type = file_doc.getType();
        this.access_permission = getPermissionFromDoc(this.file_doc);
        this.file_size = getSizeFromFile(this.file);
        this.modification_time = convertTimeToFormat(file_doc.lastModified());

        Log.i(TAG," New Document CREATED "+this.file_name);
    }


    public ObjectFile(DocumentFile doc, Context c){
        this.context = c;
        this.file_doc = doc;
        this.uri = file_doc.getUri();
        this.file = new File(uri.getPath());
        this.file_name = file_doc.getName();
        this.file_size = getSizeFromDoc(file_doc);
        this.modification_time = convertTimeToFormat(file_doc.lastModified());
        this.file_type = getFileTypeFromDoc(file_doc);
        this.access_permission = getPermissionFromDoc(this.file_doc);

        Log.i(TAG," New Document CREATED "+file_name);

    }

    public ObjectFile(File file, Context c){
        this.context = c;
        this.file = file;
        this.file_doc = DocumentFile.fromFile(file);
        this.uri = file_doc.getUri();
        this.file_name = file.getName();
        this.file_size = getSizeFromFile(file);
        this.modification_time = convertTimeToFormat(file.lastModified());
        this.file_type = getFileTypeFromFile(file);
        this.access_permission = getPermissionFromDoc(this.file_doc);

        Log.i(TAG," New Document CREATED "+file_name);

    }

    public String getFileTypeFromFile(File file){

        String type = file.getName();

        type = (type.substring(type.lastIndexOf('.')+1,type.length()-1)).toUpperCase();

        if(file.isDirectory()){
            type = "Directory";
        }else if(type ==null){
            type = "Unknown";
        }

        return type;
    }

    public String getFileTypeFromDoc(DocumentFile df){

        String type = file_doc.getType();;

        if(df.isDirectory()){
            type = "Directory";
        }else if(type==null){
            type = "Unknown";
        }

        return type;
    }

    public String getSizeFromFile(File file){

        String size = "Not Available";

        if(file.isDirectory()){
            size = file.listFiles().length+" items";
        }else if(file.isFile()){
            size = convertSizeToFormat(file.length());
        }
        return size;
    }

    public String getSizeFromDoc(DocumentFile doc){

        String size = "Not Available";

        if(doc.isDirectory()){
            size = doc.listFiles().length+" files";
        }else if(doc.isFile()){
            size = convertSizeToFormat(doc.length());
        }
        return size;
    }

    public String getPermissionFromDoc(DocumentFile doc){

        String file_perm = "Not Available";

        if(doc.canRead() & !doc.canWrite()){
            file_perm = "Read Only";
        }else
        if(doc.canWrite() & !doc.canRead()){
            file_perm = "Write Only";
        }else
        if(doc.canRead() & doc.canWrite()){
            file_perm = "Read/Write";
        }

        return file_perm;

    }

    public String convertSizeToFormat(long length) {
        final long MB = 1024 * 1024;
        final long KB = 1024;
        final long GB = 1024 * 1024 * 1024;
        final DecimalFormat format = new DecimalFormat("#.##");

        if(length>GB){
            return format.format(length / GB) + " MB";
        }
        if (length > MB) {
            return format.format(length / MB) + " MB";
        }
        if (length > KB) {
            return format.format(length / KB) + " KB";
        }

        return format.format(length) + "Bytes";
    }

    public String convertTimeToFormat(long time){
        DateFormat dateFormat = new SimpleDateFormat("hh:mm a dd-MM-yy");
        String strDate = dateFormat.format(time);
        return strDate;
    }

}

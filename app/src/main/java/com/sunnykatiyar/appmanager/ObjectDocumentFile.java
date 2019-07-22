package com.sunnykatiyar.appmanager;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class ObjectDocumentFile {

    DocumentFile file_doc;
    final Uri uri;
    Uri parent_uri;
    final String file_name;
    String file_size;
    long size_long;
    long time_long;
    float size;
    String creation_time;
    final String perm;
    String summary;
    String icon_path;
    boolean isDirectory = false;
    final String TAG = "OBJECT_FILE :";
    final String modification_time;
    final String file_type;
    String mime_type;
    boolean check_box_state = false;
    private final Context context;
    Bitmap thumbnail;

    public ObjectDocumentFile(Cursor cursor, Uri uri, Context context) {

        this.context = context;
        this.uri = uri;

        this.file_doc = DocumentFile.fromSingleUri(context, uri);
//        Log.i(TAG, "Uri : "+ uri.toString());
//        Log.i(TAG, "FILE_DOC : "+ file_doc.getUri().toString());
//        Log.i(TAG, "is Tree uri : "+ DocumentsContract.isTreeUri(uri));
//        Log.i(TAG, "is Document uri : "+ DocumentsContract.isTreeUri(uri));

        String doc_id = cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID));
        this.file_name = cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
        this.mime_type = cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
        String flags = cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_FLAGS));
        this.perm = Integer.toBinaryString(Integer.valueOf(flags))+" ("+ flags +")";
        this.size_long =cursor.getLong(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE));
        this.file_size = convertSizeToFormat(this.size_long);

        if(this.mime_type.equals(DocumentsContract.Document.MIME_TYPE_DIR)) {
            this.file_type = "Folder";
            this.isDirectory = true;
            this.file_size = "" ;
        }else{
                this.file_type = file_name.substring(file_name.lastIndexOf('.') + 1);
        }

        this.time_long =  cursor.getLong(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED));
        this.modification_time = convertTimeToFormat(this.time_long);
        this.parent_uri = Uri.parse(uri.toString().replace(Uri.encode(file_name), ""));
        if(parent_uri.toString().endsWith("%2F")){
            this.parent_uri = Uri.parse(parent_uri.toString().substring(0,parent_uri.toString().length()-3));
        }

        if (doc_id.endsWith(":")) {
            this.parent_uri = null;
        }

//        this.perm = ;
//          this.perm = getPermissionFromDoc(this.file_doc);
//        DocumentsContract.Document.FLAG_SUPPORTS_THUMBNAIL;
//        Log.i(TAG, "-------------------------------------------------------------------------------------------------------------------");
//        Log.i(TAG, "NEW_DOCUMENT_CREATED : " + file_name);
//         Log.i(TAG,"DOCUMENTID : "+doc_id);
//        Log.i(TAG, "URI : " + uri);
//        Log.i(TAG, "PAR : " + parent_uri);
//        Log.i(TAG,"\nDisplayName : "+file_name);
//        Log.i(TAG,"\nFLAGS : "+flags);
//        Log.i(TAG,"\nicon path : "+icon_path);
//        Log.i(TAG,"\ngetLastPathSegment : "+uri.getLastPathSegment());
//        Log.i(TAG,"\ngetEncodedSchemeSpecificPart : "+uri.getEncodedSchemeSpecificPart());
//        Log.i(TAG,"\ngetEncodedAuthority : "+uri.getEncodedAuthority());
//       Log.i(TAG,"\ngetEncodedPath : "+uri.getEncodedPath());
    }

    public ObjectDocumentFile(DocumentFile doc, Context c) {
        this.context = c;
        this.file_doc = doc;
        this.uri = file_doc.getUri();
        File file = new File(uri.getPath());
        this.file_name = file_doc.getName();
        this.file_size = getSizeFromDoc(file_doc);
        this.modification_time = convertTimeToFormat(file_doc.lastModified());
        this.file_type = getFileTypeFromDoc(file_doc);
        this.perm = getPermissionFromDoc(this.file_doc);

       // Log.i(TAG, " New Document CREATED " + file_name);

    }

    private String getFileTypeFromDoc(DocumentFile df) {

        String type = file_doc.getType();

        if (df.isDirectory()) {
            type = "Directory";
        } else if (type == null) {
            type = "Unknown";
        }

        return type;
    }

    private String getSizeFromDoc(DocumentFile doc) {

        String size = "Not Available";

        if (doc.isDirectory()) {
            size = doc.listFiles().length + " files";
        } else if (doc.isFile()) {
            size = convertSizeToFormat(doc.length());
        }
        return size;
    }

    private String getPermissionFromDoc(DocumentFile doc) {

        String file_perm = "Not Available";

        if (doc.canRead() & !doc.canWrite()) {
            file_perm = "Read Only";
        } else if (doc.canWrite() & !doc.canRead()) {
            file_perm = "Write Only";
        } else if (doc.canRead() & doc.canWrite()) {
            file_perm = "Read/Write";
        }

        return file_perm;

    }

    private String convertSizeToFormat(long length) {
        final long KB = 1024;
        final long MB = 1024 * 1024;
        final long GB = 1024 * 1024 * 1024;

        final DecimalFormat format = new DecimalFormat("###.##");

        if (length > GB) {
            return format.format((float)length / GB) + " GB";
        }
        if (length > MB) {
            return format.format((float)length / MB) + " MB";
        }
        if (length > KB) {
            return format.format((float)length / KB) + " KB";
        }

        return format.format(length) + " Bytes";
    }

    private String convertTimeToFormat(long time) {
        DateFormat dateFormat = new SimpleDateFormat("hh:mm a dd-MM-yy");
        String strDate = dateFormat.format(time);
        return strDate;
    }

}

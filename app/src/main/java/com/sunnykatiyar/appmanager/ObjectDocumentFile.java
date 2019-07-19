package com.sunnykatiyar.appmanager;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.webkit.MimeTypeMap;

import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class ObjectDocumentFile {

    File file;
    DocumentFile file_doc;
    String doc_id;
    Uri uri;
    Uri parent_uri;
    String file_name;
    String file_size;
    long size_long;
    long time_long;
    String creation_time;
    String flags;
    String perm;
    String summary;
    String icon_path;
    boolean isDirectory = false;
    final String TAG = "OBJECT_FILE :";
    String modification_time;
    String file_type;
    String mime_type;
    boolean check_box_state = false;
    Context context;
    Bitmap thumbnail;

    public ObjectDocumentFile(Cursor cursor, Uri uri, Context context) {

        this.context = context;
        this.uri = uri;
        this.file_doc = DocumentFile.fromTreeUri(context, uri);
        this.doc_id = cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID));
        this.file_name = cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
        this.mime_type = cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
        this.flags = cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_FLAGS));
        this.perm = Integer.toBinaryString(Integer.valueOf(flags))+" ("+flags+")";
        this.size_long =cursor.getLong(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE));
        this.file_size = convertSizeToFormat(this.size_long);

        if(this.mime_type.equals(DocumentsContract.Document.MIME_TYPE_DIR)) {
            this.file_type = "Folder";
            this.isDirectory = true;
            this.file_size = "" ;
        }else{
//            if(null==MimeTypeMap.getSingleton().getExtensionFromMimeType(mime_type)) {
                this.file_type = file_name.substring(file_name.lastIndexOf('.') + 1);
//            }else{
//                this.file_type = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime_type);
//            }
        }
//      this.icon_path = cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_ICON));
//      this.summary = DocumentsContract.Document.COLUMN_SUMMARY;
//     Log.i(TAG, "SUMMARY : " + file_name + "  : " + summary);
//        try {
//            this.thumbnail = DocumentsContract.getDocumentThumbnail(context.getContentResolver(), uri, new Point(45, 45), null);
//        } catch (Exception ex) {
//            this.thumbnail = null;
//        }
        this.time_long =  cursor.getLong(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED));
        this.modification_time = convertTimeToFormat(this.time_long);
        this.parent_uri = Uri.parse(uri.toString().replace(Uri.encode(file_name), ""));
        if(parent_uri.toString().endsWith("%2F")){
            this.parent_uri = Uri.parse(parent_uri.toString().substring(0,parent_uri.toString().length()-3));
        }
     //   Log.i(TAG,"parentUri : "+parent_uri);
//      Log.i(TAG,"Name encoded : "+Uri.encode("/"+file_name));
        if (this.doc_id.endsWith(":")) {
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
        this.file = new File(uri.getPath());
        this.file_name = file_doc.getName();
        this.file_size = getSizeFromDoc(file_doc);
        this.modification_time = convertTimeToFormat(file_doc.lastModified());
        this.file_type = getFileTypeFromDoc(file_doc);
        this.perm = getPermissionFromDoc(this.file_doc);

       // Log.i(TAG, " New Document CREATED " + file_name);

    }

    public String getFileTypeFromDoc(DocumentFile df) {

        String type = file_doc.getType();
        ;

        if (df.isDirectory()) {
            type = "Directory";
        } else if (type == null) {
            type = "Unknown";
        }

        return type;
    }

    public String getSizeFromDoc(DocumentFile doc) {

        String size = "Not Available";

        if (doc.isDirectory()) {
            size = doc.listFiles().length + " files";
        } else if (doc.isFile()) {
            size = convertSizeToFormat(doc.length());
        }
        return size;
    }

    public String getPermissionFromDoc(DocumentFile doc) {

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

    public String convertSizeToFormat(long length) {
        final long MB = 1024 * 1024;
        final long KB = 1024;
        final long GB = 1024 * 1024 * 1024;
        final DecimalFormat format = new DecimalFormat("#.###");

        if (length > GB) {
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

    public String convertTimeToFormat(long time) {
        DateFormat dateFormat = new SimpleDateFormat("hh:mm a dd-MM-yy");
        String strDate = dateFormat.format(time);
        return strDate;
    }

}

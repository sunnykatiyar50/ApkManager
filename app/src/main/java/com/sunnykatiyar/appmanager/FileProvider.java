package com.sunnykatiyar.appmanager;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsProvider;

import androidx.annotation.Nullable;

import java.io.FileNotFoundException;

import static android.provider.DocumentsContract.Root.*;

public class FileProvider extends DocumentsProvider {

    private final static String[] DEFAULT_ROOT_PROJECTION =
            new String[]{ COLUMN_ROOT_ID, COLUMN_ICON, COLUMN_TITLE, COLUMN_FLAGS, COLUMN_DOCUMENT_ID};


    @Override
    public Cursor queryRoots(String[] projection) throws FileNotFoundException {

        MatrixCursor result = new MatrixCursor(projection != null ? projection : DEFAULT_ROOT_PROJECTION);

//        MatrixCursor.RowBuilder row = result.newRow();
//        row.add(Root.COLUMN_ROOT_ID, rootId);
//        row.add(Root.COLUMN_ICON, R.mipmap.ic_launcher);
//        row.add(Root.COLUMN_TITLE,getContext().getString(R.string.app_name));
//        row.add(Root.COLUMN_FLAGS, Root.FLAG_LOCAL_ONLY | Root.FLAG_SUPPORTS_CREATE);
//        row.add(Root.COLUMN_DOCUMENT_ID, rootDocumentId);

        return null;
    }

    @Override
    public Cursor queryDocument(String documentId, String[] projection) throws FileNotFoundException {
        return null;
    }

    @Override
    public Cursor queryChildDocuments(String parentDocumentId, String[] projection, String sortOrder) throws FileNotFoundException {
        return null;
    }

    @Override
    public ParcelFileDescriptor openDocument(String documentId, String mode, @Nullable CancellationSignal signal) throws FileNotFoundException {
        return null;
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public void ejectRoot(String rootId) {
        super.ejectRoot(rootId);
    }

}

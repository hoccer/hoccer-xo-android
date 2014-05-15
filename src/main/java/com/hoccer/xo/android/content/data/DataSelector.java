package com.hoccer.xo.android.content.data;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.content.IContentSelector;
import com.hoccer.xo.android.content.SelectedContent;
import com.hoccer.xo.release.R;

import java.io.File;

public class DataSelector implements IContentSelector {

    private String mName;
    private Drawable mIcon;

    public DataSelector(Context context) {
        mName = context.getResources().getString(R.string.content_data);
        mIcon = context.getResources().getDrawable(R.drawable.ic_attachment_select_data);
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public Drawable getContentIcon() {
        return mIcon;
    }

    @Override
    public Intent createSelectionIntent(Context context) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        return Intent.createChooser(intent, context.getString(R.string.file_chooser_string));
    }

    @Override
    public IContentObject createObjectFromSelectionResult(Context context, Intent intent) {
        Uri uri = intent.getData();
        SelectedContent contentObject = null;
        if (uri.getScheme().equals("content")) {
            contentObject = fromContentUri(context, uri, intent);
        }
        if (uri.getScheme().equals("file")) {
            contentObject = fromFileUri(uri, intent);
        }
        return contentObject;
    }

    private SelectedContent fromContentUri(Context context, Uri uri, Intent intent) {
        String[] filePathColumn = {
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.MIME_TYPE,
                MediaStore.MediaColumns.SIZE
        };

        Cursor cursor = context.getContentResolver().query(
                uri, filePathColumn, null, null, null);
        cursor.moveToFirst();

        int dataIndex = cursor.getColumnIndex(filePathColumn[0]);
        String filePath = cursor.getString(dataIndex);
        int nameIndex = cursor.getColumnIndex(filePathColumn[1]);
        String displayName = cursor.getString(nameIndex);
        displayName = displayName.substring(0, displayName.lastIndexOf("."));
        int mimeIndex = cursor.getColumnIndex(filePathColumn[2]);
        String mimeType = cursor.getString(mimeIndex);
        int sizeIndex = cursor.getColumnIndex(filePathColumn[3]);
        int fileSize = cursor.getInt(sizeIndex);

        cursor.close();

        if (filePath == null) {
            filePath = getKitKatPath(context, uri);
            if (filePath == null) {
                return null;
            }
        }

        SelectedContent contentObject = new SelectedContent(intent, "file://" + filePath);
        contentObject.setFileName(displayName);
        contentObject.setContentMediaType("data");
        contentObject.setContentType(mimeType);
        contentObject.setContentLength(fileSize);

        return contentObject;
    }

    private SelectedContent fromFileUri (Uri uri, Intent intent) {
        File f = new File(uri.getPath());
        String filePath = f.getPath();
        SelectedContent contentObject = new SelectedContent(intent, "file://" + filePath);
        String fileName = f.getName();
        fileName = fileName.substring(0, fileName.lastIndexOf("."));
        contentObject.setFileName(fileName);
        contentObject.setContentMediaType("data");
        contentObject.setContentType("");
        contentObject.setContentLength((int)f.length());

        return contentObject;
    }

    @SuppressLint("NewApi")
    private String getKitKatPath(Context context, Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        if (!isKitKat) {
            return null;
        }
        if (DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }
        }
        return null;
    }


    private String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    private boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }
}

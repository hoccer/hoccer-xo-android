package com.hoccer.xo.android.content.image;

import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.android.content.IContentSelector;
import com.hoccer.xo.android.content.SelectedContent;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import com.hoccer.xo.release.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ImageSelector implements IContentSelector {

    private String mName;

    public ImageSelector(Context context) {
        mName = context.getResources().getString(R.string.content_images);
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public Intent createSelectionIntent(Context context) {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.putExtra("return-data", true);
        return intent;
    }


    @Override
    public SelectedContent createObjectFromSelectionResult(Context context, Intent intent) {
        Uri selectedContent = intent.getData();
        if (selectedContent.toString().startsWith("content://com.google.android.gallery3d")) {
            return createFromPicasa(context, intent);
        } else {
            return createFromFile(context, intent);
        }
    }

    private SelectedContent createFromPicasa(final Context context, Intent intent) {
        Uri selectedContent = intent.getData();
        final String[] filePathColumn = {MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.DISPLAY_NAME};
        Cursor cursor = context.getContentResolver()
                .query(selectedContent, filePathColumn, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
            if (columnIndex != -1) {
                try {
                    String displayName = cursor.getString(columnIndex);
                    final Uri uriurl = selectedContent;
                    Bitmap bmp = getBitmap(context, displayName, uriurl);
                    File imageFile = new File(XoApplication.getAttachmentDirectory(), displayName);

                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(imageFile));

                    SelectedContent contentObject = new SelectedContent(intent,
                            "file://" + imageFile.getAbsolutePath());
                    contentObject.setContentMediaType("image");
                    contentObject.setContentLength((int) imageFile.length());
                    contentObject.setContentAspectRatio(
                            ((float) bmp.getWidth()) / ((float) bmp.getHeight()));
                    return contentObject;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    private Bitmap getBitmap(Context context, String fileName, Uri url) {
        File cacheDir = XoApplication.getAttachmentDirectory();
        File f = new File(cacheDir, fileName);
        try {
            InputStream is = null;
            if (url.toString().startsWith("content://com.google.android.gallery3d")) {
                is = context.getContentResolver().openInputStream(url);
            } else {
                is = new URL(url.toString()).openStream();
            }
            return BitmapFactory.decodeStream(is);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private SelectedContent createFromFile(Context context, Intent intent) {
        Uri selectedContent = intent.getData();
        String[] filePathColumn = {MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT};

        Cursor cursor = context.getContentResolver().query(
                selectedContent, filePathColumn, null, null, null);
        cursor.moveToFirst();

        int typeIndex = cursor.getColumnIndex(filePathColumn[0]);
        String fileType = cursor.getString(typeIndex);
        int dataIndex = cursor.getColumnIndex(filePathColumn[1]);
        String filePath = cursor.getString(dataIndex);
        int sizeIndex = cursor.getColumnIndex(filePathColumn[2]);
        int fileSize = cursor.getInt(sizeIndex);
        int widthIndex = cursor.getColumnIndex(filePathColumn[3]);
        int fileWidth = cursor.getInt(widthIndex);
        int heightIndex = cursor.getColumnIndex(filePathColumn[4]);
        int fileHeight = cursor.getInt(heightIndex);

        cursor.close();

        if (filePath == null) {
            filePath = selectedContent.toString();
        }

        SelectedContent contentObject = new SelectedContent(intent, "file://" + filePath);
        contentObject.setContentMediaType("image");
        contentObject.setContentLength(fileSize);
        if (fileWidth > 0 && fileHeight > 0) {
            contentObject.setContentAspectRatio(((float) fileWidth) / ((float) fileHeight));
        } else {
            try {
                Bitmap bmp = MediaStore.Images.Media
                        .getBitmap(context.getContentResolver(), selectedContent);
                contentObject.setContentAspectRatio(
                        ((float) bmp.getWidth()) / ((float) bmp.getHeight()));
            } catch (IOException e) {
            }
        }
        return contentObject;
    }


}

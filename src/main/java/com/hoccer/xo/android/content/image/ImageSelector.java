package com.hoccer.xo.android.content.image;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import com.hoccer.xo.android.content.SelectedContent;
import com.hoccer.xo.android.content.IContentSelector;

import java.io.IOException;

public class ImageSelector implements IContentSelector {

    @Override
    public String getName() {
        return "Image";
    }

    @Override
    public Intent createSelectionIntent(Context context) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        return intent;
    }

    @Override
    public SelectedContent createObjectFromSelectionResult(Context context, Intent intent) {
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

        if(filePath == null) {
            return null;
        }

        SelectedContent contentObject = new SelectedContent(intent, "file://" + filePath);
        contentObject.setContentMediaType("image");
        contentObject.setContentLength(fileSize);
        if(fileWidth > 0 && fileHeight > 0) {
            contentObject.setContentAspectRatio(((float)fileWidth) / ((float)fileHeight));
        } else {
            try {
                Bitmap bmp = MediaStore.Images.Media.getBitmap(context.getContentResolver(), selectedContent);
                contentObject.setContentAspectRatio(((float)bmp.getWidth()) / ((float)bmp.getHeight()));
            } catch (IOException e) {
            }
        }
        return contentObject;
    }

}

package com.hoccer.xo.android.content.image;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import com.hoccer.xo.android.content.SelectedContent;
import com.hoccer.xo.android.content.IContentSelector;

public class CaptureSelector implements IContentSelector {

    @Override
    public String getName() {
        return "Take photo";
    }

    @Override
    public Intent createSelectionIntent(Context context) {
        return new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    }

    @Override
    public SelectedContent createObjectFromSelectionResult(Context context, Intent intent) {
        Uri selectedContent = intent.getData();
        String[] filePathColumn = {MediaStore.Images.Media.MIME_TYPE,
                                    MediaStore.Images.Media.DATA,
                                    MediaStore.Images.Media.WIDTH,
                                    MediaStore.Images.Media.HEIGHT};

        Cursor cursor = context.getContentResolver().query(
                selectedContent, filePathColumn, null, null, null);
        cursor.moveToFirst();

        int typeIndex = cursor.getColumnIndex(filePathColumn[0]);
        String fileType = cursor.getString(typeIndex);
        int dataIndex = cursor.getColumnIndex(filePathColumn[1]);
        String filePath = cursor.getString(dataIndex);
        int widthIndex = cursor.getColumnIndex(filePathColumn[2]);
        int fileWidth = cursor.getInt(widthIndex);
        int heightIndex = cursor.getColumnIndex(filePathColumn[3]);
        int fileHeight = cursor.getInt(heightIndex);

        SelectedContent contentObject = new SelectedContent("file://" + filePath);
        contentObject.setContentMediaType("image");
        contentObject.setContentType(fileType);
        contentObject.setContentAspectRatio(((float)fileWidth) / ((float)fileHeight));

        cursor.close();

        return contentObject;
    }

}
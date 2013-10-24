package com.hoccer.xo.android.content.image;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import com.hoccer.xo.android.content.SelectedContent;
import com.hoccer.xo.android.content.IContentSelector;

public class GallerySelector implements IContentSelector {

    @Override
    public String getName() {
        return "Image";
    }

    @Override
    public Intent createSelectionIntent(Context context) {
        return new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
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

        if(filePath == null) {
            return null;
        }

        SelectedContent contentObject = new SelectedContent("file://" + filePath);
        contentObject.setContentMediaType("image");
        contentObject.setContentType(fileType);
        if(fileWidth > 0 && fileHeight > 0) {
            contentObject.setContentAspectRatio(((float)fileWidth) / ((float)fileHeight));
        }

        cursor.close();

        return contentObject;
    }

}

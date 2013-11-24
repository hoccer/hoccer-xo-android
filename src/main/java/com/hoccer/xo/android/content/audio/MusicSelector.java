package com.hoccer.xo.android.content.audio;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import com.hoccer.xo.android.content.SelectedContent;
import com.hoccer.xo.android.content.IContentSelector;

public class MusicSelector implements IContentSelector {

    @Override
    public String getName() {
        return "Music";
    }

    @Override
    public Intent createSelectionIntent(Context context) {
        return new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
    }

    @Override
    public SelectedContent createObjectFromSelectionResult(Context context, Intent intent) {
        Uri selectedContent = intent.getData();
        String[] filePathColumn = {MediaStore.Audio.Media.MIME_TYPE,
                                   MediaStore.Audio.Media.DATA,
                                   MediaStore.Audio.Media.SIZE};

        Cursor cursor = context.getContentResolver().query(
                selectedContent, filePathColumn, null, null, null);
        cursor.moveToFirst();

        int typeIndex = cursor.getColumnIndex(filePathColumn[0]);
        String fileType = cursor.getString(typeIndex);
        int dataIndex = cursor.getColumnIndex(filePathColumn[1]);
        String filePath = cursor.getString(dataIndex);
        int sizeIndex = cursor.getColumnIndex(filePathColumn[2]);
        int fileSize = cursor.getInt(sizeIndex);

        cursor.close();

        if(filePath == null) {
            return null;
        }

        SelectedContent contentObject = new SelectedContent("file://" + filePath);
        contentObject.setContentMediaType("audio");
        contentObject.setContentType(fileType);
        contentObject.setContentLength(fileSize);

        return contentObject;
    }

}

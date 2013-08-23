package com.hoccer.talk.android.content.audio;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import com.hoccer.talk.android.content.ContentObject;
import com.hoccer.talk.android.content.IContentSelector;

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
    public ContentObject createObjectFromSelectionResult(Context context, Intent intent) {
        Uri selectedContent = intent.getData();
        String[] filePathColumn = {MediaStore.Audio.Media.MIME_TYPE,
                                   MediaStore.Audio.Media.DATA};

        Cursor cursor = context.getContentResolver().query(
                selectedContent, filePathColumn, null, null, null);
        cursor.moveToFirst();

        int typeIndex = cursor.getColumnIndex(filePathColumn[0]);
        String fileType = cursor.getString(typeIndex);
        int dataIndex = cursor.getColumnIndex(filePathColumn[1]);
        String filePath = cursor.getString(dataIndex);

        if(filePath == null) {
            return null;
        }

        ContentObject contentObject = new ContentObject();
        contentObject.setMediaType("audio");
        contentObject.setMimeType(fileType);
        contentObject.setContentUrl(filePath);
        cursor.close();

        return contentObject;
    }

}

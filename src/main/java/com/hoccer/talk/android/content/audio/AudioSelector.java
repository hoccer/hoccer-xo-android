package com.hoccer.talk.android.content.audio;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import com.hoccer.talk.android.content.ContentObject;
import com.hoccer.talk.android.content.ContentSelector;
import com.hoccer.talk.android.util.IntentHelper;

public class AudioSelector extends ContentSelector {

    @Override
    public String getName() {
        return "Audio";
    }

    @Override
    public Intent createSelectionIntent(Context context) {
        return new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
    }

    @Override
    public ContentObject createObjectFromSelectionResult(Context context, Intent intent) {
        Uri selectedContent = intent.getData();
        String[] filePathColumn = {MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Media.DATA};

        Cursor cursor = context.getContentResolver().query(
                selectedContent, filePathColumn, null, null, null);
        cursor.moveToFirst();

        int typeIndex = cursor.getColumnIndex(filePathColumn[0]);
        String fileType = cursor.getString(typeIndex);
        int dataIndex = cursor.getColumnIndex(filePathColumn[1]);
        String filePath = cursor.getString(dataIndex);

        ContentObject contentObject = new ContentObject();
        contentObject.setMediaType(fileType);
        contentObject.setContentUrl(filePath);
        cursor.close();

        return contentObject;
    }

}

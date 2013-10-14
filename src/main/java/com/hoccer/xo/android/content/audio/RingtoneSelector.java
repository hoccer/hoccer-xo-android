package com.hoccer.xo.android.content.audio;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore;
import com.hoccer.xo.android.content.ContentObject;
import com.hoccer.xo.android.content.IContentSelector;
import org.apache.log4j.Logger;

public class RingtoneSelector implements IContentSelector {

    private static final Logger LOG = Logger.getLogger(RingtoneSelector.class);

    @Override
    public String getName() {
        return "Ringtone";
    }

    @Override
    public Intent createSelectionIntent(Context context) {
        return new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
    }

    @Override
    public ContentObject createObjectFromSelectionResult(Context context, Intent intent) {
        Uri selectedContent = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
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

        return contentObject;
    }
}

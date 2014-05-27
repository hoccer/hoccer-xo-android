package com.hoccer.xo.android.content.image;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.content.ContentMediaTypes;
import com.hoccer.xo.android.content.IContentSelector;
import com.hoccer.xo.android.content.SelectedContent;
import com.hoccer.xo.release.R;

public class VideoSelector implements IContentSelector {

    private String mName;
    private Drawable mIcon;

    public VideoSelector(Context context) {
        mName = context.getResources().getString(R.string.content_video);
        mIcon = context.getResources().getDrawable(R.drawable.ic_attachment_select_video);
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
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        intent.setType("video/*");
        return intent;
    }

    @Override
    public IContentObject createObjectFromSelectionResult(Context context, Intent intent) {
        Uri selectedContent = intent.getData();
        String[] filePathColumn = {
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.WIDTH,
                MediaStore.Video.Media.HEIGHT,
                MediaStore.Video.Media.TITLE
        };

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
        int fileNameIndex = cursor.getColumnIndex(filePathColumn[5]);
        String fileName = cursor.getString(fileNameIndex);

        cursor.close();

        if (filePath == null) {
            return null;
        }

        SelectedContent contentObject = new SelectedContent(intent, "file://" + filePath);
        contentObject.setFileName(fileName);
        contentObject.setContentMediaType(ContentMediaTypes.MediaTypeVideo);
        contentObject.setContentType(fileType);
        contentObject.setContentLength(fileSize);
        if (fileWidth > 0 && fileHeight > 0) {
            contentObject.setContentAspectRatio(((float) fileWidth) / ((float) fileHeight));
        }

        return contentObject;
    }

}

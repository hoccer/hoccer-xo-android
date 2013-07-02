package com.hoccer.talk.android.content.image;

import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;
import com.hoccer.talk.android.content.ContentObject;
import com.hoccer.talk.android.content.ContentSelector;
import com.hoccer.talk.android.util.IntentHelper;

public class ImageSelector extends ContentSelector {

    @Override
    public Intent createSelectionIntent(Context context) {
        return new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    }

    @Override
    public ContentObject createObjectFromSelectionResult(Context context, Intent intent) {
        return null;
    }

}

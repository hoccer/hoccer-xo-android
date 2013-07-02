package com.hoccer.talk.android.content.audio;

import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;
import com.hoccer.talk.android.content.ContentObject;
import com.hoccer.talk.android.content.ContentSelector;
import com.hoccer.talk.android.util.IntentHelper;

public class AudioSelector extends ContentSelector {

    @Override
    public Intent createSelectionIntent(Context context) {
        return new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
    }

    @Override
    public ContentObject createObjectFromSelectionResult(Context context, Intent intent) {
        return null;
    }

}

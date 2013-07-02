package com.hoccer.talk.android.content;

import android.content.Context;
import com.hoccer.talk.android.content.audio.AudioSelector;
import com.hoccer.talk.android.content.image.ImageSelector;

import java.util.ArrayList;
import java.util.List;

public class ContentRegistry {

    private static ContentRegistry INSTANCE = null;

    public static synchronized ContentRegistry get(Context applicationContext) {
        if(INSTANCE == null) {
            INSTANCE = new ContentRegistry(applicationContext);
        }
        return INSTANCE;
    }

    Context mContext;

    List<ContentSelector> mSelectors = new ArrayList<ContentSelector>();

    public ContentRegistry(Context context) {
        mContext = context;
    }

    public void selectAvatar() {

    }

    public void selectAttachment() {

    }

    private void initializeSelectors() {
        mSelectors.add(new ImageSelector());
        mSelectors.add(new AudioSelector());
    }

}

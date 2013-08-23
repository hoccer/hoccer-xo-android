package com.hoccer.talk.android.content.location;

import android.content.Context;
import android.content.Intent;
import com.hoccer.talk.android.content.ContentObject;
import com.hoccer.talk.android.content.IContentSelector;

public class MapsLocationSelector implements IContentSelector {

    @Override
    public String getName() {
        return "Location from Map";
    }

    @Override
    public Intent createSelectionIntent(Context context) {
        return null;
    }

    @Override
    public ContentObject createObjectFromSelectionResult(Context context, Intent intent) {
        return null;
    }

}

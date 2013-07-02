package com.hoccer.talk.android.content;

import android.content.Context;
import android.content.Intent;

public abstract class ContentSelector {

    public abstract Intent createSelectionIntent(Context context);

    public abstract ContentObject createObjectFromSelectionResult(Context context, Intent intent);

}

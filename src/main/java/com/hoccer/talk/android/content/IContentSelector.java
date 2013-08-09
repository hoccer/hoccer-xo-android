package com.hoccer.talk.android.content;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;

public interface IContentSelector {

    public abstract String getName();

    public abstract Intent createSelectionIntent(Context context);

    public abstract ContentObject createObjectFromSelectionResult(Context context, Intent intent);

}
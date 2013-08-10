package com.hoccer.talk.android.content;

import android.content.Context;
import android.content.Intent;

public interface IContentSelector {

    public abstract String getName();

    public abstract Intent createSelectionIntent(Context context);

    public abstract ContentObject createObjectFromSelectionResult(Context context, Intent intent);

}

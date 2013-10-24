package com.hoccer.xo.android.content;

import android.content.Context;
import android.content.Intent;
import com.hoccer.talk.content.IContentObject;

/**
 * Content selectors allow the user to select content from some source via intents
 */
public interface IContentSelector {

    /** Returns the name of this selector */
    public abstract String getName();

    /** Creates an intent for content selection */
    public abstract Intent createSelectionIntent(Context context);

    /** Handles the intent result, returning a content object */
    public abstract IContentObject createObjectFromSelectionResult(Context context, Intent intent);

}

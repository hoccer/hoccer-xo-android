package com.hoccer.talk.android.content;

import android.content.Context;
import android.content.Intent;

/**
 * Content selectors allow the user to select content from some source via intents
 */
public interface IContentSelector {

    /** Returns the name of this selector */
    public abstract String getName();

    /** Creates an intent for content selection */
    public abstract Intent createSelectionIntent(Context context);

    /** Handles the intent result, returning a content object */
    public abstract ContentObject createObjectFromSelectionResult(Context context, Intent intent);

}

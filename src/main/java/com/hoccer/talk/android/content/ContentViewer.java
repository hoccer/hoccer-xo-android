package com.hoccer.talk.android.content;

import android.view.View;

import java.util.zip.Inflater;

public abstract class ContentViewer {

    public abstract View getViewForObject(Inflater inflater, ContentObject object);

}

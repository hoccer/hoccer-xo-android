package com.hoccer.xo.android.content.data;

import android.content.Context;
import android.content.Intent;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.content.IContentSelector;
import com.hoccer.xo.android.content.SelectedContent;
import com.hoccer.xo.release.R;

public class DataSelector implements IContentSelector {

    private String mName;

    public DataSelector(Context context) {
        mName = context.getResources().getString(R.string.content_data);
    }

    @Override
    public String getName() {
        return mName;
    }


    @Override
    public Intent createSelectionIntent(Context context) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        return Intent.createChooser(intent, context.getString(R.string.file_chooser_string));
    }

    @Override
    public IContentObject createObjectFromSelectionResult(Context context, Intent intent) {
        SelectedContent content = null;
        return null;
    }
}

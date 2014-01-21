package com.hoccer.xo.android.content.data;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.activity.ProfileActivity;
import com.hoccer.xo.android.content.IContentSelector;
import com.hoccer.xo.android.content.SelectedContent;
import com.hoccer.xo.release.R;

public class DataSelector implements IContentSelector {
    @Override
    public String getName() {
        return "Data";
    }

    @Override
    public Intent createSelectionIntent(Context context) {
        Toast.makeText(context, R.string.feature_not_available, Toast.LENGTH_LONG).show();
        return new Intent(context, ProfileActivity.class);
    }

    @Override
    public IContentObject createObjectFromSelectionResult(Context context, Intent intent) {
        SelectedContent content = null;
        return null;
    }
}

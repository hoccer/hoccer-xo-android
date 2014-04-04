package com.hoccer.xo.android.content.location;

import android.content.Context;
import android.content.Intent;
import com.hoccer.xo.android.content.SelectedContent;
import com.hoccer.xo.android.content.IContentSelector;
import com.hoccer.xo.release.R;

public class MapsLocationSelector implements IContentSelector {

    private String mName;

    public MapsLocationSelector(Context context) {
        mName = context.getResources().getString(R.string.content_location);
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public Intent createSelectionIntent(Context context) {
        return new Intent(context, MapsLocationActivity.class);
    }

    @Override
    public SelectedContent createObjectFromSelectionResult(Context context, Intent intent) {
        SelectedContent content = null;
        if(intent.hasExtra(MapsLocationActivity.EXTRA_GEOJSON)) {
            String json = intent.getStringExtra(MapsLocationActivity.EXTRA_GEOJSON);
            content = new SelectedContent(json.getBytes());
            content.setContentMediaType("geolocation");
            content.setContentType("application/json");
        }
        return content;
    }

}

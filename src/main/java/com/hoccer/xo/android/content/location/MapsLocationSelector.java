package com.hoccer.xo.android.content.location;

import android.content.Context;
import android.content.Intent;
import com.hoccer.xo.android.content.SelectedContent;
import com.hoccer.xo.android.content.IContentSelector;

public class MapsLocationSelector implements IContentSelector {

    @Override
    public String getName() {
        return "Location";
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

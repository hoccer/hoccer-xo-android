package com.hoccer.xo.android.content.location;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.release.R;

public class MapsLocationActivity extends XoActivity {

    SupportMapFragment mMapFragment;

    GoogleMap mMap;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_maps_location;
    }

    @Override
    protected int getMenuResource() {
        return -1;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // get the map fragment
        if(mMapFragment == null) {
            mMapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        }
        // get and configure the map behind the fragment
        if(mMap == null) {
            mMap = mMapFragment.getMap();
            setupMap();
        }
    }

    private void setupMap() {
        mMap.setMyLocationEnabled(true);
    }

}

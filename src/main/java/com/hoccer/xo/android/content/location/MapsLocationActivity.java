package com.hoccer.xo.android.content.location;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.release.R;

public class MapsLocationActivity extends XoActivity
    implements GoogleMap.OnMyLocationButtonClickListener,
               GoogleMap.OnMapLongClickListener,
               View.OnClickListener {

    SupportMapFragment mMapFragment;

    GoogleMap mMap;

    Marker mMarker;

    Button mButtonOk;
    Button mButtonCancel;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_maps_location;
    }

    @Override
    protected int getMenuResource() {
        return -1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableUpNavigation();
        mButtonOk = (Button)findViewById(R.id.maps_location_ok);
        mButtonOk.setOnClickListener(this);
        mButtonCancel = (Button)findViewById(R.id.maps_location_cancel);
        mButtonCancel.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // get the map fragment
        if(mMapFragment == null) {
            mMapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.maps_location_map);
        }
        // get and configure the map behind the fragment
        if(mMap == null) {
            mMap = mMapFragment.getMap();
            setupMap();
        }
    }

    private void setupMap() {
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMapLongClickListener(this);
    }

    private void removeMarker() {
        if(mMarker != null) {
            mMarker.remove();
            mMarker = null;
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        removeMarker();
        return false;
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        removeMarker();
        MarkerOptions options =
                new MarkerOptions()
                    .position(latLng)
                    .draggable(true);
        mMarker = mMap.addMarker(options);
        Toast.makeText(this, "Location set", Toast.LENGTH_SHORT)
             .show();
    }

    @Override
    public void onClick(View v) {
        if(v == mButtonCancel) {
            finish();
        }
        if(v == mButtonOk) {
        }
    }

}

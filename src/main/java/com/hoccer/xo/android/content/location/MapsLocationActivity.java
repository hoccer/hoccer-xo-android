package com.hoccer.xo.android.content.location;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.release.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MapsLocationActivity extends XoActivity
    implements GoogleMap.OnMyLocationButtonClickListener,
               GoogleMap.OnMapLongClickListener,
               GoogleMap.OnMyLocationChangeListener,
               View.OnClickListener {

    public static final String EXTRA_GEOJSON = "com.hoccer.xo.android.GEOJSON";

    SupportMapFragment mMapFragment;

    GoogleMap mMap;

    Location mMyLocation;
    Marker mMarker;

    Button mButtonOk;
    Button mButtonCancel;

    ObjectMapper mJsonMapper;

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
        mJsonMapper = new ObjectMapper();
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
        mMyLocation = mMap.getMyLocation();
        update();
    }

    private void setupMap() {
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMyLocationChangeListener(this);
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
        update();
        return false;
    }

    @Override
    public void onMyLocationChange(Location location) {
        mMyLocation = location;
        update();
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
        update();
    }

    @Override
    public void onClick(View v) {
        if(v == mButtonCancel) {
            finish();
        }
        if(v == mButtonOk) {
            Intent intent = createResultIntent();
            returnResultIntent(intent);
        }
    }

    private LatLng getLocation() {
        if(mMarker != null) {
            return mMarker.getPosition();
        } else if (mMyLocation != null) {
            return new LatLng(mMyLocation.getLatitude(), mMyLocation.getLongitude());
        }
        return null;
    }

    private JsonNode createGeoJson() {
        LatLng latlng = getLocation();
        ObjectNode root = mJsonMapper.createObjectNode();
        ObjectNode location = mJsonMapper.createObjectNode();
        location.put("type", "point");
        ArrayNode coordinates = mJsonMapper.createArrayNode();
        coordinates.add(latlng.latitude);
        coordinates.add(latlng.longitude);
        location.put("coordinates", coordinates);
        root.put("location", location);
        root.put("previewImage", getPreview());
        return root;
    }

    private Intent createResultIntent() {
        Intent intent = new Intent();
        JsonNode json = createGeoJson();
        try {
            String string = mJsonMapper.writeValueAsString(json);
            intent.putExtra(EXTRA_GEOJSON, string);
        } catch (JsonProcessingException e) {
            LOG.error("JSON processing exception", e);
        }
        return intent;
    }

    private void returnResultIntent(Intent intent) {
        if (getParent() == null) {
            setResult(Activity.RESULT_OK, intent);
        } else {
            getParent().setResult(Activity.RESULT_OK, intent);
        }
        finish();
    }

    private void update() {
        LatLng location = getLocation();
        mButtonOk.setEnabled(location != null);
    }


    private static String PREVIEW_IMAGE = null;

    private String getPreview() {
        if(PREVIEW_IMAGE == null) {
            try {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                InputStream is = getResources().openRawResource(R.raw.location_preview_png);
                byte[] buf = new byte[4096];
                int bytesRead;
                while( (bytesRead = is.read(buf)) != -1 ) {
                    output.write(buf, 0, bytesRead);
                }
                output.flush();
                byte[] data = Base64.encode(output.toByteArray(), Base64.DEFAULT);
                output.close();
                PREVIEW_IMAGE = new String(data);
            } catch (IOException e) {
                LOG.error("error reading location preview resource", e);
            }
        }
        return PREVIEW_IMAGE;
    }
}

package com.hoccer.xo.android.nearby;

import com.hoccer.talk.client.XoClient;
import com.hoccer.talk.model.TalkEnvironment;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.android.error.EnvironmentUpdaterException;

import org.apache.log4j.Logger;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class EnvironmentUpdater implements LocationListener {

    private static final Logger LOG = Logger.getLogger(EnvironmentUpdater.class);

    private static final long MIN_UPDATE_TIME = 1000;

    private static final long MIN_UPDATE_MOVED = 5;

    private final XoClient mClient = XoApplication.getXoClient();

    private final LocationManager mLocationManager;

    private final WifiManager mWifiManager;

    private final Context mContext;

    private final boolean mNetworkProviderAvailable;

    private final boolean mGpsProviderAvailable;

    private boolean isEnabled = true;

    public EnvironmentUpdater(Context pContext) {
        mContext = pContext;

        mLocationManager = (LocationManager) pContext.getSystemService(Context.LOCATION_SERVICE);
        mWifiManager = (WifiManager) pContext.getSystemService(Context.WIFI_SERVICE);

        mNetworkProviderAvailable = mLocationManager.getAllProviders()
                .contains(LocationManager.NETWORK_PROVIDER);
        mGpsProviderAvailable = mLocationManager.getAllProviders()
                .contains(LocationManager.GPS_PROVIDER);
    }

    public Context getContext() {
        return mContext;
    }

    public void stopEnvironmentTracking() {
        mLocationManager.removeUpdates(this);
        mClient.sendDestroyEnvironment();
    }

    public void startEnvironmentTracking() throws EnvironmentUpdaterException {
        if(!mGpsProviderAvailable && !mNetworkProviderAvailable) {
            throw new EnvironmentUpdaterException("no source for environment information available");
        }

        if (mGpsProviderAvailable) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_UPDATE_TIME,
                    MIN_UPDATE_MOVED, this);
        }

        if (mNetworkProviderAvailable) {
            mLocationManager
                    .requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_UPDATE_TIME,
                            MIN_UPDATE_MOVED, this);
        }
    }

    public TalkEnvironment getEnvironment() {
        LOG.trace("getEnvironment()");
        TalkEnvironment theEnvironment = new TalkEnvironment();

        Location networkLocation = null;
        Location gpsLocation = null;
        if (mNetworkProviderAvailable) {
            networkLocation = mLocationManager
                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if (mGpsProviderAvailable) {
            gpsLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        Location location = null;
        if (gpsLocation != null && networkLocation != null) {
            // both available, select most precise
            if (gpsLocation.getAccuracy() < networkLocation.getAccuracy()) {
                location = gpsLocation;
            } else {
                location = networkLocation;
            }
        } else {
            location = gpsLocation != null ? gpsLocation : networkLocation;
        }
        if (location != null) {
            Double[] geoLocation = {location.getLongitude(), location.getLatitude()};
            theEnvironment.setGeoLocation(geoLocation);
            theEnvironment.setLocationType(location.getProvider());
            if (location.hasAccuracy()) {
                theEnvironment.setAccuracy(location.getAccuracy());
            } else {
                theEnvironment.setAccuracy(0.0f);
            }
        }

        // wifi scan result
        List<ScanResult> scanResults = mWifiManager.getScanResults();
        if (scanResults != null) {
            List<String> bssids = new ArrayList<String>();
            for (ScanResult scan : scanResults) {
                bssids.add(scan.BSSID);
            }
            theEnvironment.setBssids(bssids.toArray(new String[0]));
        }
        theEnvironment.setTimestamp(new Date());
        return theEnvironment;
    }


    @Override
    public void onLocationChanged(Location location) {
        LOG.debug("onLocationChanged:" + location.toString());
        if (isEnabled) {
            mClient.setEnvironment(getEnvironment());
            mClient.sendEnvironmentUpdate();
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        LOG.debug("onProviderDisabled:" + provider);
        mClient.sendDestroyEnvironment();
    }

    @Override
    public void onProviderEnabled(String provider) {
        LOG.debug("onProviderEnabled:" + provider);
        mClient.setEnvironment(getEnvironment());
        mClient.sendEnvironmentUpdate();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        LOG.debug("onStatusChanged:" + provider);
        if (isEnabled) {
            mClient.setEnvironment(getEnvironment());
            mClient.sendEnvironmentUpdate();
        }
    }

}


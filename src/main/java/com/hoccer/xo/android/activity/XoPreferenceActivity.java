package com.hoccer.xo.android.activity;

import com.hoccer.xo.android.XoConfiguration;
import com.hoccer.xo.release.R;

import org.apache.log4j.Logger;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

public class XoPreferenceActivity extends PreferenceActivity {

    private static final Logger LOG = Logger.getLogger(XoPreferenceActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LOG.debug("onCreate()");
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        if (XoConfiguration.DEVELOPMENT_MODE_ENABLED) {
            addPreferencesFromResource(R.xml.development_preferences);
        } else {
            addPreferencesFromResource(R.xml.preferences);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LOG.debug("onOptionsItemSelected(" + item.toString() + ")");
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

}

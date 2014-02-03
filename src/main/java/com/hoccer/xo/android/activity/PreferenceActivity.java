package com.hoccer.xo.android.activity;

import android.os.Bundle;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.hoccer.xo.android.XoConfiguration;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

public class PreferenceActivity extends SherlockPreferenceActivity {

    private static final Logger LOG = Logger.getLogger(PreferenceActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LOG.debug("onCreate()");
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (XoConfiguration.DEVELOPMENT_SETTINGS_ENABLED) {
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

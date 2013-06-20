package com.hoccer.talk.android.activity;

import android.os.Bundle;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.hoccer.talk.android.R;

public class PreferenceActivity extends SherlockPreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

}

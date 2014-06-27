package com.hoccer.xo.android.activity;

import android.app.Activity;
import android.os.Bundle;
import com.hoccer.xo.release.R;

/**
 * Created by nico on 27/06/2014.
 */
public class LegalImprintActivity extends Activity {

    public static final String DISPLAY_MODE = "display_mode";
    public static final int SHOW_ABOUT = 1;
    public static final int SHOW_LICENSES = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle intentExtras = getIntent().getExtras();
        if (intentExtras != null && intentExtras.containsKey(DISPLAY_MODE)) {
            int displayMode = intentExtras.getInt(DISPLAY_MODE, SHOW_ABOUT);
            if (displayMode == SHOW_ABOUT) {
                setContentView(R.layout.activity_about);
            } else if (displayMode == SHOW_LICENSES) {
                setContentView(R.layout.activity_licenses);
            }
        }

    }
}

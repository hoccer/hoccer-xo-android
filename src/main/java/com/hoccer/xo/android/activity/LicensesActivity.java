package com.hoccer.xo.android.activity;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.release.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Activity that displays our (long) license text
 */
public class LicensesActivity extends XoActivity {

    TextView mText;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_licenses;
    }

    @Override
    protected int getMenuResource() {
        return -1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LOG.debug("onCreate()");
        super.onCreate(savedInstanceState);

        enableUpNavigation();

        mText = (TextView)findViewById(R.id.licenses_textview);
        mText.setMovementMethod(new ScrollingMovementMethod());

        mText.setText(readText());
    }

    private String readText() {
        InputStream is = getResources().openRawResource(R.raw.licenses);
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int i;
        try {
            do {
                i = is.read(buffer);
                if(i != -1) {
                    os.write(buffer, 0, i);
                }
            } while (i != -1);
            is.close();
            os.flush();
            os.close();
        } catch (IOException e) {
            LOG.error("could not read license text", e);
            return "Could not read license text.";
        }

        return os.toString();
    }

}

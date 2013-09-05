package com.hoccer.talk.android.activity;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;
import com.hoccer.xo.R;
import com.hoccer.talk.android.TalkActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class LicensesActivity extends TalkActivity {

    TextView mText;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_licenses;
    }

    @Override
    protected int getMenuResource() {
        return R.menu.fragment_about;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        }

        return os.toString();
    }
}

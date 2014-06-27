package com.hoccer.xo.android.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by nico on 27/06/2014.
 */
public class LicensesFragment extends Fragment{

    private static final Logger LOG = Logger.getLogger(LicensesFragment.class);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_licenses, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView licenseView = (TextView) view.findViewById(R.id.tv_licenses);
        licenseView.setText(readLicenses());
    }

    private String readLicenses() {
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

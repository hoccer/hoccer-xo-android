package com.hoccer.xo.android.content.data;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.content.ContentView;
import com.hoccer.xo.android.content.ContentViewer;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

import java.net.URLConnection;

public class DataViewer extends ContentViewer<Button> {

    private static final Logger LOG = Logger.getLogger(DataViewer.class);

    ObjectMapper mJsonMapper;

    public DataViewer() {
        mJsonMapper = new ObjectMapper();
    }

    @Override
    public boolean canViewObject(IContentObject object) {
        return object.getContentMediaType().equals("data");
    }

    @Override
    protected Button makeView(Activity activity) {
        Button button = new Button(activity);
        button.setText(activity.getString(R.string.open_file));
        return button;
    }

    @Override
    protected void updateViewInternal(final Button view, ContentView contentView, final IContentObject contentObject, boolean isLightTheme) {
        if(contentObject.isContentAvailable()) {
            view.setVisibility(View.VISIBLE);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(contentObject.isContentAvailable()) {
                        String url = contentObject.getContentUrl();
                        if(url == null) {
                            url = contentObject.getContentDataUrl();
                        }
                        if(url != null) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            String type = URLConnection.guessContentTypeFromName(url);
                            Uri data = Uri.parse(url);
                            intent.setDataAndType(data, type);
                            try {
                                view.getContext().startActivity(intent);
                            } catch(ActivityNotFoundException exception) {
                                // TODO: tell the user there is no app installd which can handle the file
                                // for now we use a Toast!
                                Toast.makeText(view.getContext(),
                                        R.string.error_no_such_app,
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    protected void clearViewInternal(Button view) {

    }


}

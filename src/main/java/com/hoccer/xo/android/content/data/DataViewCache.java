package com.hoccer.xo.android.content.data;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.content.ContentView;
import com.hoccer.xo.android.content.ContentViewCache;
import com.hoccer.xo.release.R;

import java.net.URLConnection;

public class DataViewCache extends ContentViewCache<View> {
    ObjectMapper mJsonMapper;

    public DataViewCache() {
        mJsonMapper = new ObjectMapper();
    }

    @Override
    public boolean canViewObject(IContentObject object) {
        return object.getContentMediaType().equals("data");
    }

    @Override
    protected View makeView(Activity activity) {
        View view = View.inflate(activity, R.layout.content_data, null);
        return view;
    }

    @Override
    protected void updateViewInternal(final View view, ContentView contentView, final IContentObject contentObject, boolean isLightTheme) {
        if(contentObject.isContentAvailable()) {
            ImageButton openDocButton = (ImageButton) view.findViewById(R.id.ib_content_open);
            int imageResource = isLightTheme ? R.drawable.ic_dark_data
                    : R.drawable.ic_light_data;
            openDocButton.setImageResource(imageResource);
            TextView title = (TextView) view.findViewById(R.id.tv_doc_title);
                String fileName = contentObject.getFileName();
                if (contentObject.getContentDataUrl() != null) {
                    String extension = contentObject.getContentDataUrl().substring(
                            contentObject.getContentDataUrl().lastIndexOf("."));
                    fileName = fileName + extension;
                }
                title.setText(fileName);
                openDocButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (contentObject.isContentAvailable()) {
                            String url = contentObject.getContentUrl();
                            if (url == null) {
                                url = contentObject.getContentDataUrl();
                            }
                            if (url != null) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                String type = URLConnection.guessContentTypeFromName(url);
                                Uri data = Uri.parse(url);
                                intent.setDataAndType(data, type);
                                try {
                                    view.getContext().startActivity(intent);
                                } catch (ActivityNotFoundException exception) {
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
            int textColor = isLightTheme ? Color.BLACK : Color.WHITE;
            TextView description = (TextView) view.findViewById(R.id.tv_doc_description);
            title.setTextColor(textColor);
            description.setTextColor(textColor);
        }
    }

    @Override
    protected void clearViewInternal(View view) {

    }


}

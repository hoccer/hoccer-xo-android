package com.hoccer.xo.android.content.location;

import com.google.android.gms.maps.model.LatLng;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.android.content.ContentView;
import com.hoccer.xo.android.content.ContentViewProvider;
import com.hoccer.xo.android.content.SelectedContent;
import com.hoccer.xo.release.R;

import org.apache.log4j.Logger;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class LocationViewProvider extends ContentViewProvider<View> {

    private static final Logger LOG = Logger.getLogger(LocationViewProvider.class);

    ObjectMapper mJsonMapper;

    public LocationViewProvider() {
        mJsonMapper = new ObjectMapper();
    }

    @Override
    public boolean canViewObject(IContentObject object) {
        return object.getContentMediaType().equals("geolocation");
    }

    @Override
    protected View makeView(Activity activity) {
        View view = View.inflate(activity, R.layout.content_location, null);
        return view;
    }

    @Override
    protected void updateViewInternal(final View view, final ContentView contentView,
            final IContentObject contentObject, boolean isLightTheme) {
        final LatLng location = loadGeoJson(contentObject);
        if (location == null) {
            view.setVisibility(View.INVISIBLE);
        } else {
            view.setVisibility(View.VISIBLE);

            initTextViews(view, isLightTheme);
            initImageButton(view, contentObject, isLightTheme, location);
        }
    }

    private void initImageButton(final View view, final IContentObject contentObject,
            boolean isLightTheme, final LatLng location) {
        ImageButton openMapButton = (ImageButton) view.findViewById(R.id.ib_content_open);
        int imageResource = isLightTheme ? R.drawable.ic_dark_location
                : R.drawable.ic_light_location;
        openMapButton.setImageResource(imageResource);

        openMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (contentObject.isContentAvailable()) {
                    String url = contentObject.getContentUrl();
                    if (url == null) {
                        url = contentObject.getContentDataUrl();
                    }
                    if (url != null) {
                        String label = "Received Location";
                        String uriString = "http://maps.google.com/maps?q=loc:"
                                + location.latitude + "," + location.longitude + " (" + label
                                + ")";
                        Uri uri = Uri.parse(uriString);
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
                        view.getContext().startActivity(intent);
                    }
                }
            }
        });
    }

    private void initTextViews(View view, boolean isLightTheme) {
        int textColor = isLightTheme ? Color.BLACK : Color.WHITE;
        TextView title = (TextView) view.findViewById(R.id.tv_location_title);
        TextView description = (TextView) view.findViewById(R.id.tv_location_description);

        title.setTextColor(textColor);
        description.setTextColor(textColor);
    }

    @Override
    protected void clearViewInternal(View view) {
    }

    private LatLng loadGeoJson(IContentObject content) {
        LatLng result = null;
        try {
            if (!content.isContentAvailable()) {
                return null;
            }

            // XXX put this somewhere
            InputStream is = null;
            if (content instanceof SelectedContent) {
                SelectedContent selectedContent = ((SelectedContent) content);
                if (selectedContent.getData() != null) {
                    is = new ByteArrayInputStream(selectedContent.getData());
                }
            }
            if (is == null && content.getContentDataUrl() != null) {
                is = XoApplication.getXoClient().getHost().openInputStreamForUrl(
                        content.getContentDataUrl()
                );
            }
            if (is == null) {
                return null;
            }

            JsonNode json = mJsonMapper.readTree(is);
            if (json != null && json.isObject()) {
                LOG.info("parsing location: " + json.toString());
                JsonNode location = json.get("location");
                if (location != null && location.isObject()) {
                    JsonNode type = location.get("type");
                    if ("point".equals(type.asText())) {
                        JsonNode coordinates = location.get("coordinates");
                        if (coordinates.isArray() && coordinates.size() == 2) {
                            JsonNode lat = coordinates.get(0);
                            JsonNode lon = coordinates.get(1);
                            result = new LatLng(lat.asDouble(), lon.asDouble());
                        } else {
                            LOG.error("coordinates not an array of 2");
                        }
                    } else {
                        LOG.error("location is not a point");
                    }
                } else {
                    LOG.error("location node is not an object");
                }
            } else {
                LOG.error("root node is not object");
            }


        } catch (IOException e) {
            LOG.error("error loading geojson", e);
        }
        return result;
    }

}

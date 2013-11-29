package com.hoccer.xo.android.content.location;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.maps.model.LatLng;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.android.content.ContentView;
import com.hoccer.xo.android.content.ContentViewer;
import com.hoccer.xo.android.content.SelectedContent;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class LocationViewer extends ContentViewer<Button> {

    private static final Logger LOG = Logger.getLogger(LocationViewer.class);

    ObjectMapper mJsonMapper;

    public LocationViewer() {
        mJsonMapper = new ObjectMapper();
    }

    @Override
    public boolean canViewObject(IContentObject object) {
        return object.getContentMediaType().equals("location");
    }

    @Override
    protected Button makeView(Activity activity) {
        Button view = new Button(activity);
        view.setText("Show location");
        return view;
    }

    @Override
    protected void updateView(final Button view, final ContentView contentView, final IContentObject contentObject) {
        final LatLng location = loadGeoJson(contentObject);
        if(location == null) {
            view.setVisibility(View.INVISIBLE);
        } else {
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
                            String label = "Received Location";
                            String uriBegin = "geo:" + location.latitude + "," + location.longitude;
                            String query = location.latitude + "," + location.longitude + "(" + label + ")";
                            String encodedQuery = Uri.encode(query);
                            String uriString = uriBegin + "?q=" + encodedQuery;
                            Uri uri = Uri.parse(uriString);
                            Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
                            view.getContext().startActivity(intent);
                        }
                    }
                }
            });
        }
    }

    private LatLng loadGeoJson(IContentObject content) {
        LatLng result = null;
        try {
            if(!content.isContentAvailable()) {
                return null;
            }

            // XXX put this somewhere
            InputStream is = null;
            if(content instanceof SelectedContent) {
                SelectedContent selectedContent = ((SelectedContent)content);
                if(selectedContent.getData() != null) {
                    is = new ByteArrayInputStream(selectedContent.getData());
                }
            }
            if(is == null && content.getContentDataUrl() != null) {
                is = XoApplication.getXoClient().getHost().openInputStreamForUrl(
                        content.getContentDataUrl()
                );
            }
            if(is == null) {
                return null;
            }

            JsonNode json = mJsonMapper.readTree(is);
            if(json != null && json.isObject()) {
                JsonNode location = json.get("location");
                if(location != null && location.isObject()) {
                    JsonNode type = location.get("type");
                    if("point".equals(type.asText())) {
                        JsonNode coordinates = location.get("coordinates");
                        if(coordinates.isArray() && coordinates.size() == 2) {
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

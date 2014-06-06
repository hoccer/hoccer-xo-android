package com.hoccer.xo.android.view.chat.attachments;

import com.google.android.gms.maps.model.LatLng;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.content.SelectedContent;
import com.hoccer.xo.android.view.chat.ChatMessageItem;
import com.hoccer.xo.release.R;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;


public class ChatLocationItem extends ChatMessageItem {

    public ChatLocationItem(Context context, TalkClientMessage message) {
        super(context, message);
    }

    @Override
    public ChatItemType getType() {
        return ChatItemType.ChatItemWithLocation;
    }

    @Override
    protected void configureViewForMessage(View view) {
        super.configureViewForMessage(view);
        configureAttachmentViewForMessage(view);
    }

    @Override
    protected void displayAttachment(final IContentObject contentObject) {
        super.displayAttachment(contentObject);

        // add view lazily
        if (mContentWrapper.getChildCount() == 0) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            RelativeLayout locationLayout = (RelativeLayout) inflater.inflate(R.layout.content_location, null);
            TextView locationTextView = (TextView) locationLayout.findViewById(R.id.tv_location_description);
            TextView locationTitleView = (TextView) locationLayout.findViewById(R.id.tv_location_title);
            ImageButton locationButton = (ImageButton) locationLayout.findViewById(R.id.ib_content_location);

            int textColor = -1;
            int iconId = -1;
            if(mMessage.isIncoming()) {
                textColor = Color.BLACK;
                iconId = R.drawable.ic_dark_location;
            } else {
                textColor = Color.WHITE;
                iconId = R.drawable.ic_light_location;
            }

            locationTextView.setTextColor(textColor);
            locationTitleView.setTextColor(textColor);
            locationButton.setImageResource(iconId);

            locationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (contentObject.isContentAvailable()) {
                        String url = contentObject.getContentUrl();
                        if (url == null) {
                            url = contentObject.getContentDataUrl();
                        }
                        if (url != null) {
                            LatLng location = loadGeoJson(contentObject);
                            String label = "Received Location";
                            String uriString = "http://maps.google.com/maps?q=loc:"
                                    + location.latitude + "," + location.longitude + " (" + label
                                    + ")";
                            Uri uri = Uri.parse(uriString);
                            Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
                            XoActivity activity = (XoActivity)view.getContext();
                            activity.startExternalActivity(intent);
                        }
                    }
                }
            });

            mContentWrapper.addView(locationLayout);
        }


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

            ObjectMapper jsonMapper = new ObjectMapper();
            JsonNode json = jsonMapper.readTree(is);
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

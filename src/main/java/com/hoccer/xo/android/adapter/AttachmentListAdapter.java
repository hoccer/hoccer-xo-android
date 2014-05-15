package com.hoccer.xo.android.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.content.ContentMediaType;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.base.XoAdapter;
import com.hoccer.xo.android.content.MediaMetaData;
import com.hoccer.xo.release.R;
import android.os.Environment;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class AttachmentListAdapter extends XoAdapter {

    private List<TalkClientDownload> mAttachments;
    private List<MediaMetaData> mAttachmentMetaData;

    private String mContentMediaType;

    public AttachmentListAdapter(XoActivity pXoContext){
        super(pXoContext);
    }

    @Override
    public int getCount() {
        return mAttachments.size();
    }

    @Override
    public TalkClientDownload getItem(int position) {
        return mAttachments.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View attachmentView;
        if (convertView != null) {
            attachmentView = convertView;
        } else {
            attachmentView = mInflater.inflate(R.layout.attachmentlist_music_item, null);
        }

        // this is for AUDIO only. TODO: create for different media formats when necessary
        if (mContentMediaType != null) {
            if (mContentMediaType.equalsIgnoreCase(ContentMediaType.AUDIO)) {

                String fileName = mAttachmentMetaData.get(position).getFilePath();
                String titleName = mAttachmentMetaData.get(position).getTitle();
                String artistName = mAttachmentMetaData.get(position).getArtist();

                String verifiedFileName = (fileName != null) ? fileName : "Unknown Title";
                String verifiedTitleName = (titleName != null) ? titleName : verifiedFileName.substring( (Environment.getExternalStorageDirectory().getAbsolutePath() + R.string.app_name).length() + 1, (verifiedFileName.length() - 5) );
                String verifiedArtistName = (artistName != null) ? artistName : "Unknown Artist";

                ((TextView) attachmentView.findViewById(R.id.attachmentlist_item_title_name)).setText(verifiedTitleName);
                ((TextView) attachmentView.findViewById(R.id.attachmentlist_item_artist_name)).setText(verifiedArtistName);

                ImageView coverView = ((ImageView) attachmentView.findViewById(R.id.attachmentlist_item_image));

                byte[] cover = mAttachmentMetaData.get(position).getArtwork();

                if( cover != null ) {
                    Bitmap coverBitmap = BitmapFactory.decodeByteArray(cover, 0, cover.length);
                    coverView.setImageBitmap(coverBitmap);
                }
            }
        }
        return attachmentView;
    }

    @Override
    public boolean isActive() {
        return super.isActive();
    }

    public void setContentMediaType(String pContentMediaType) {
        mContentMediaType = pContentMediaType;
    }

    public void setAttachmentList(List<TalkClientDownload> pAttachments) {
        mAttachments = null;
        mAttachmentMetaData = null;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetInvalidated();
            }
        });

        mAttachments = pAttachments;

        if (mContentMediaType != null) {
            if (mContentMediaType.equalsIgnoreCase(ContentMediaType.AUDIO) || mContentMediaType.equalsIgnoreCase(ContentMediaType.VIDEO)) {
                fetchMetaDataFromAttachmentList();
            }
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    private void fetchMetaDataFromAttachmentList() {
        ArrayList<String> filePaths = new ArrayList<String>();
        for (TalkClientDownload attachment : mAttachments) {
            filePaths.add(attachment.getDataFile());
        }
        mAttachmentMetaData = MediaMetaData.factorMetaDataForFileList(filePaths);
    }
}
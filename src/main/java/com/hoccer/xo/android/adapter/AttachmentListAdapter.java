package com.hoccer.xo.android.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.hoccer.talk.client.IXoTransferListener;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.talk.client.model.TalkClientUpload;
import com.hoccer.talk.content.ContentMediaType;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.base.XoAdapter;
import com.hoccer.xo.android.content.MediaItem;
import com.hoccer.xo.android.content.MediaMetaData;
import com.hoccer.xo.android.service.MediaPlayerService;
import com.hoccer.xo.android.view.AttachmentAudioView;
import com.hoccer.xo.release.R;

import java.sql.SQLException;
import java.util.List;

public class AttachmentListAdapter extends XoAdapter implements IXoTransferListener{

    private List<TalkClientDownload> mAttachments;

    private String mContentMediaType;

    private int mConversationContactId = MediaPlayerService.UNDEFINED_CONTACT_ID;

    public AttachmentListAdapter(XoActivity pXoContext) {
        this(pXoContext, null, MediaPlayerService.UNDEFINED_CONTACT_ID);
    }

    public AttachmentListAdapter(XoActivity pXoContext, String pContentMediaType) {
        this(pXoContext, pContentMediaType, MediaPlayerService.UNDEFINED_CONTACT_ID);
    }

    public AttachmentListAdapter(XoActivity pXoContext, int pConversationContactId) {
        this(pXoContext, null, pConversationContactId);
    }

    public AttachmentListAdapter(XoActivity pXoContext, String pContentMediaType, int pConversationContactId) {
        super(pXoContext);
        setContentMediaType(pContentMediaType);
        setConversationContactId(pConversationContactId);
        loadAttachmentList();
    }

    public List<TalkClientDownload> getAttachments() {
        return mAttachments;
    }

    public int getConversationContactId() {
        return mConversationContactId;
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

        MediaItem mediaItem = MediaItem.create(mAttachments.get(position).getContentDataUrl());
        if (mediaItem == null) {
            mAttachments.remove(position);
            if (mAttachments.size() <= position) {
                return null;
            }
            return getView(position, convertView, parent);
        }

        AttachmentAudioView audioRowView;

        if (convertView == null) {
            audioRowView = new AttachmentAudioView(mActivity, mediaItem);
        } else  {
            audioRowView = (AttachmentAudioView) convertView;
        }

        getArtworkAsynchronously( audioRowView);
        //audioRowView.setArtworkImageView(MediaMetaData.getArtwork(mediaItem.getFilePath()));
        audioRowView.setTitleTextView(mediaItem.getMetaData().getTitleOrFilename(mediaItem.getFilePath()));
        String artist = mediaItem.getMetaData().getArtist();
        if (artist == null || artist.isEmpty()){
            artist = parent.getResources().getString(R.string.media_meta_data_unknown_artist);
        }
        audioRowView.setArtistTextView(artist);

        return audioRowView;
    }

    private void getArtworkAsynchronously(AttachmentAudioView audioRowView) {
        new DownloadArtworkTask().execute(audioRowView);
    }

    private class DownloadArtworkTask extends AsyncTask<AttachmentAudioView, Void, Bitmap> {

        AttachmentAudioView view;

        protected Bitmap doInBackground(AttachmentAudioView... params) {
            view = params[0];
            String filePath = view.getmMediaItem().getFilePath();

            byte[] artworkRaw = MediaMetaData.getArtwork(filePath);
            if ( artworkRaw != null) {
                return BitmapFactory.decodeByteArray(artworkRaw, 0, artworkRaw.length);
            }else{
                return null;
            }
        }

        protected void onPostExecute(Bitmap artwork) {
            super.onPostExecute(artwork);
            view.setArtworkImageBitmap(artwork);
        }
    }

    @Override
    public boolean isActive() {
        return super.isActive();
    }

    public void setContentMediaType(String pContentMediaType) {
        mContentMediaType = pContentMediaType;
    }

    public void setConversationContactId(int pConversationContactId) {
        mConversationContactId = pConversationContactId;
    }

    private void loadAttachmentList() {
        try {
            if (mContentMediaType != null) {
                if (mConversationContactId != MediaPlayerService.UNDEFINED_CONTACT_ID) {
                    mAttachments = getXoClient().getDatabase().findClientDownloadByMediaTypeAndConversationContactId(ContentMediaType.AUDIO, mConversationContactId);
                } else {
                    mAttachments = getXoClient().getDatabase().findClientDownloadByMediaType(mContentMediaType);
                }
            } else {
                mAttachments = getXoClient().getDatabase().findAllClientDownloads();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDownloadRegistered(TalkClientDownload download) {

    }

    @Override
    public void onDownloadStarted(TalkClientDownload download) {

    }

    @Override
    public void onDownloadProgress(TalkClientDownload download) {

    }

    @Override
    public void onDownloadFinished(TalkClientDownload download) {
        int contactId = MediaPlayerService.UNDEFINED_CONTACT_ID;

        try {
            TalkClientMessage message = XoApplication.getXoClient().getDatabase().findMessageByDownloadId(download.getClientDownloadId());
            contactId = message.getConversationContact().getClientContactId();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (download.getContentMediaType().equals(this.mContentMediaType)) {
            if ((mConversationContactId == MediaPlayerService.UNDEFINED_CONTACT_ID) || (mConversationContactId == contactId)) {
                mAttachments.add(0, download);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        }

    }

    @Override
    public void onDownloadStateChanged(TalkClientDownload download) {

    }

    @Override
    public void onUploadStarted(TalkClientUpload upload) {

    }

    @Override
    public void onUploadProgress(TalkClientUpload upload) {

    }

    @Override
    public void onUploadFinished(TalkClientUpload upload) {

    }

    @Override
    public void onUploadStateChanged(TalkClientUpload upload) {

    }
}

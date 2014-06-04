package com.hoccer.xo.android.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
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
import java.util.ArrayList;
import java.util.List;

public class AttachmentListAdapter extends XoAdapter implements IXoTransferListener{

    private List<MediaItem> mMediaItems;
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

    public List<MediaItem> getMediaItems() {
        return mMediaItems;
    }

    public int getConversationContactId() {
        return mConversationContactId;
    }

    @Override
    public int getCount() {
        return mMediaItems.size();
    }

    @Override
    public MediaItem getItem(int position) {
        return mMediaItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (mMediaItems.get(position) == null) {
            mMediaItems.remove(position);
            if (mMediaItems.size() <= position) {
                return null;
            }
            return getView(position, convertView, parent);
        }

        AttachmentAudioView audioRowView = (AttachmentAudioView) convertView;
        if (audioRowView == null) {
            audioRowView = new AttachmentAudioView(mActivity);
        }

        audioRowView.setMediaItem(mMediaItems.get(position));
        return audioRowView;
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
                mMediaItems.add(0, MediaItem.create(download.getContentDataUrl()));
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

    private void loadAttachmentList() {
        mMediaItems = new ArrayList<MediaItem>();
        try {
            List<TalkClientDownload> downloads;
            if (mContentMediaType != null) {
                if (mConversationContactId != MediaPlayerService.UNDEFINED_CONTACT_ID) {
                    downloads = getXoClient().getDatabase().findClientDownloadByMediaTypeAndConversationContactId(ContentMediaType.AUDIO, mConversationContactId);
                } else {
                    downloads = getXoClient().getDatabase().findClientDownloadByMediaType(mContentMediaType);
                }
            } else {
                downloads = getXoClient().getDatabase().findAllClientDownloads();
            }

            if (downloads != null) {
                for (TalkClientDownload download : downloads) {
                    mMediaItems.add(MediaItem.create(download.getContentDataUrl()));
                }

            }

        } catch (SQLException e) {
            LOG.error(e);
        }
    }
}

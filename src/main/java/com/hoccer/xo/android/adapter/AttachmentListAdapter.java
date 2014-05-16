package com.hoccer.xo.android.adapter;

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
import com.hoccer.xo.android.content.audio.MediaPlaylist;
import com.hoccer.xo.android.view.AttachmentAudioView;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AttachmentListAdapter extends XoAdapter implements IXoTransferListener {

    private List<TalkClientDownload> mAttachments;

    private String mContentMediaType;

    private int mConversationContactId = MediaPlaylist.UNDEFINED_CONTACT_ID;

    public AttachmentListAdapter(XoActivity pXoContext) {
        this(pXoContext, null, MediaPlaylist.UNDEFINED_CONTACT_ID);
    }

    public AttachmentListAdapter(XoActivity pXoContext, String pContentMediaType) {
        this(pXoContext, pContentMediaType, MediaPlaylist.UNDEFINED_CONTACT_ID);
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
        View attachmentView;
        if (convertView != null) {
            attachmentView = convertView;
        } else {
            // this is for AUDIO only. TODO: create for different media formats when necessary
            MediaItem mediaItem = MediaItem.create(mAttachments.get(position).getContentDataUrl());
            attachmentView = new AttachmentAudioView(mActivity, mInflater, mediaItem);
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

    public void setConversationContactId(int pConversationContactId) {
        mConversationContactId = pConversationContactId;
    }

    private void loadAttachmentList() {
        try {
            if (mContentMediaType != null) {
                if (mConversationContactId != MediaPlaylist.UNDEFINED_CONTACT_ID) {
                    mAttachments = getXoClient().getDatabase().findClientDownloadByMediaTypeAndConversationContactId(ContentMediaType.AUDIO, mConversationContactId);
                } else {
                    mAttachments = getXoClient().getDatabase().findClientDownloadByMediaType(mContentMediaType);
                }

                if (mContentMediaType.equalsIgnoreCase(ContentMediaType.AUDIO)) {
                    fetchMetaDataFromAttachmentList();
                }
            } else {
                mAttachments = getXoClient().getDatabase().findAllClientDownloads();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void fetchMetaDataFromAttachmentList() {
        ArrayList<String> filePaths = new ArrayList<String>();
        for (TalkClientDownload attachment : mAttachments) {
            filePaths.add(attachment.getDataFile());
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
        int contactId = MediaPlaylist.UNDEFINED_CONTACT_ID;

        try {
            TalkClientMessage message = XoApplication.getXoClient().getDatabase().findMessageByDownloadId(download.getClientDownloadId());
            contactId = message.getConversationContact().getClientContactId();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(download.getContentMediaType().equals(this.mContentMediaType)){
            if (mConversationContactId == contactId) {
                //mAttachments.add(0, download);
                loadAttachmentList();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetInvalidated();
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

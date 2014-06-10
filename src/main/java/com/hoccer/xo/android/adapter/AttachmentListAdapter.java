package com.hoccer.xo.android.adapter;

import android.app.Activity;
import android.graphics.Color;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.hoccer.talk.client.IXoTransferListener;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.talk.client.model.TalkClientUpload;
import com.hoccer.talk.content.ContentMediaType;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.content.AudioAttachmentItem;
import com.hoccer.xo.android.service.MediaPlayerService;
import com.hoccer.xo.android.view.AttachmentAudioView;
import org.apache.log4j.Logger;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AttachmentListAdapter extends BaseAdapter implements IXoTransferListener {

    protected Logger LOG = null;

    private final Activity mActivity;
    private List<AudioAttachmentItem> mAudioAttachmentItems;
    private String mContentMediaType;
    private int mConversationContactId = MediaPlayerService.UNDEFINED_CONTACT_ID;

    private SparseBooleanArray mSelections;

    public AttachmentListAdapter(XoActivity pXoContext) {
        this(pXoContext, null, MediaPlayerService.UNDEFINED_CONTACT_ID);
    }

    public AttachmentListAdapter(XoActivity pXoContext, String pContentMediaType) {
        this(pXoContext, pContentMediaType, MediaPlayerService.UNDEFINED_CONTACT_ID);
    }

    public AttachmentListAdapter(XoActivity pXoContext, int pConversationContactId) {
        this(pXoContext, null, pConversationContactId);
    }

    public AttachmentListAdapter(Activity activity, String pContentMediaType, int pConversationContactId) {
        mActivity = activity;
        setContentMediaType(pContentMediaType);
        setConversationContactId(pConversationContactId);
        loadAttachmentList();
    }

    public List<AudioAttachmentItem> getAudioAttachmentItems() {
        return mAudioAttachmentItems;
    }

    public int getConversationContactId() {
        return mConversationContactId;
    }

    @Override
    public int getCount() {
        return mAudioAttachmentItems.size();
    }

    @Override
    public AudioAttachmentItem getItem(int position) {
        return mAudioAttachmentItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (mAudioAttachmentItems.get(position) == null) {
            mAudioAttachmentItems.remove(position);
            if (mAudioAttachmentItems.size() <= position) {
                return null;
            }
            return getView(position, convertView, parent);
        }

        AttachmentAudioView audioRowView = (AttachmentAudioView) convertView;
        if (audioRowView == null) {
            audioRowView = new AttachmentAudioView(mActivity);
        }

        audioRowView.setMediaItem(mAudioAttachmentItems.get(position));

        if (mSelections != null) {
            if (mSelections.get(position)) {
                audioRowView.setBackgroundColor(Color.RED);
            } else {
                audioRowView.setBackgroundColor(Color.WHITE);
            }
        }
        return audioRowView;
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
                mAudioAttachmentItems.add(0, AudioAttachmentItem.create(download.getContentDataUrl(), download));
                mActivity.runOnUiThread(new Runnable() {
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

    public void removeItem(int pos) {
        mAudioAttachmentItems.remove(pos);
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    public void setSelections(SparseBooleanArray selections) {
        this.mSelections = selections;
    }

    private void loadAttachmentList() {
        mAudioAttachmentItems = new ArrayList<AudioAttachmentItem>();
        try {
            List<TalkClientDownload> downloads;
            if (mContentMediaType != null) {
                if (mConversationContactId != MediaPlayerService.UNDEFINED_CONTACT_ID) {
                    downloads = XoApplication.getXoClient().getDatabase().findClientDownloadByMediaTypeAndConversationContactId(ContentMediaType.AUDIO, mConversationContactId);
                } else {
                    downloads = XoApplication.getXoClient().getDatabase().findClientDownloadByMediaType(mContentMediaType);
                }
            } else {
                downloads = XoApplication.getXoClient().getDatabase().findAllClientDownloads();
            }

            if (downloads != null) {
                for (TalkClientDownload download : downloads) {
                    mAudioAttachmentItems.add(AudioAttachmentItem.create(download.getContentDataUrl(), download));
                }

            }

        } catch (SQLException e) {
            LOG.error(e);
        }
    }

    private boolean isRecordedAudio(String filePath) {
        File attachmentFile = new File(filePath);
        String fileName = attachmentFile.getName();
        if (fileName.contains("recording")) {
            return true;
        }
        return false;
    }
}
	
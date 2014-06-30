package com.hoccer.xo.android.adapter;

import android.app.Activity;
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
import com.hoccer.xo.android.view.AudioAttachmentView;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AttachmentListAdapter extends BaseAdapter implements IXoTransferListener {

    protected Logger LOG = Logger.getLogger(AttachmentListAdapter.class);

    private final Activity mActivity;
    private List<AudioAttachmentItem> mAudioAttachmentItems = new ArrayList<AudioAttachmentItem>();

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
    }

    public List<AudioAttachmentItem> getAudioAttachmentItems() {
        return mAudioAttachmentItems;
    }

    public void setAudioAttachmentItems( List<AudioAttachmentItem> items) {
        mAudioAttachmentItems.addAll(items);
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
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

        AudioAttachmentView audioRowView = (AudioAttachmentView) convertView;
        if (audioRowView == null) {
            audioRowView = new AudioAttachmentView(mActivity);
        }

        audioRowView.setMediaItem(mAudioAttachmentItems.get(position));
        audioRowView.updatePlayPauseView();

        if (mSelections != null) {
            audioRowView.getChildAt(0).setSelected(mSelections.get(position));
        }
        return audioRowView;
    }

    public void setContentMediaType(String pContentMediaType) {
        mContentMediaType = pContentMediaType;
    }

    public String getContentMediaType() {
        return mContentMediaType;
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
                mAudioAttachmentItems.add(0, AudioAttachmentItem.create(download.getContentDataUrl(), download, true));

                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if( mSelections != null && mSelections.size() > 0) {
                            updateCheckedItems();
                        }

                        notifyDataSetChanged();
                    }
                });
            }
        }
    }

    private void updateCheckedItems() {
        SparseBooleanArray updatedSelection = new SparseBooleanArray(mSelections.size());

        for( int i = 0; i < mSelections.size(); ++i){
            boolean b = mSelections.valueAt(i);
            int k = mSelections.keyAt(i);
            updatedSelection.put(k +1, b);
        }

        mSelections.clear();

        //@Info Setting mSelection to updatedSelection won't work since the reference changes, which is not allowed
        for( int i = 0; i < updatedSelection.size(); ++i){
            mSelections.append(updatedSelection.keyAt(i), updatedSelection.valueAt(i));
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

    public void removeItem(String filepath) {

        for(int i=0; i < mAudioAttachmentItems.size(); ++i){
            if ( mAudioAttachmentItems.get(i).getFilePath().equalsIgnoreCase(filepath)){
                mAudioAttachmentItems.remove(i);
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });

                return;
            }
        }
    }

    public void addItem(AudioAttachmentItem item){
        mAudioAttachmentItems.add(item);
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    public void clear(){
        mAudioAttachmentItems.clear();
    }

    public void setSelections(SparseBooleanArray selections) {
        this.mSelections = selections;
    }

    public void loadAttachmentList() {
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
                    if (!isRecordedAudio(download.getFileName())) {
                        AudioAttachmentItem newItem = AudioAttachmentItem.create(download.getContentDataUrl(), download, true);
                        if (newItem != null) {
                            mAudioAttachmentItems.add(newItem);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            LOG.error(e);
        }
    }

    private boolean isRecordedAudio(String fileName) {
        if (fileName.startsWith("recording")) {
            return true;
        }

        return false;
    }
}
	

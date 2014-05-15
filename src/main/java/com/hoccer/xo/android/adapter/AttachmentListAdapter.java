package com.hoccer.xo.android.adapter;

import android.view.View;
import android.view.ViewGroup;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.content.ContentMediaType;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.base.XoAdapter;
import com.hoccer.xo.android.content.MediaMetaData;
import com.hoccer.xo.android.view.AttachmentAudioView;

import java.util.ArrayList;
import java.util.List;

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
            // this is for AUDIO only. TODO: create for different media formats when necessary
            //        if (mContentMediaType != null) {
            //            if (mContentMediaType.equalsIgnoreCase(ContentMediaType.AUDIO)) {
            attachmentView = new AttachmentAudioView(mActivity, mInflater, mAttachmentMetaData.get(position));
            //        }
            //    }
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
        mAttachmentMetaData = MediaMetaData.create(filePaths);
    }

}

package com.hoccer.xo.android.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.model.TalkAttachment;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.base.XoAdapter;

import java.util.List;

/**
 * Created by nico on 09/05/2014.
 */
public class AttachmentListAdapter extends XoAdapter {

    private List<TalkClientDownload> mAttachments;
    private int mViewResourceId;
    private int mTextViewId;

    public AttachmentListAdapter(XoActivity pXoContext, List<TalkClientDownload> pAttachments, int pViewResourceId, int pTextViewId){
        super(pXoContext);
        LOG.error("AttachmentListAdapter contructor");
        mAttachments = pAttachments;
        LOG.error("Attachments: " + mAttachments.size());
        mViewResourceId = pViewResourceId;
        mTextViewId = pTextViewId;
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
            attachmentView = mInflater.inflate(mViewResourceId, null);
        }
        ((TextView) attachmentView.findViewById(mTextViewId)).setText(getDisplayName(position));
        return attachmentView;
    }

    private String getDisplayName(int pPosition) {
        TalkClientDownload attachment = mAttachments.get(pPosition);
        if (attachment.getMediaType().equalsIgnoreCase("audio")) {
            attachment.getFileName();
        }
        return mAttachments.get(pPosition).getFileName();
    }
}

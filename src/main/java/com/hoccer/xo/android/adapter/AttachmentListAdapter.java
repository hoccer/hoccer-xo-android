package com.hoccer.xo.android.adapter;

import android.database.DataSetObserver;
import android.database.Observable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.content.ContentMediaType;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.base.XoAdapter;
import com.hoccer.xo.android.content.MediaMetaData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nico on 09/05/2014.
 */
public class AttachmentListAdapter extends XoAdapter {

    private List<TalkClientDownload> mAttachments;
    private List<MediaMetaData> mAttachmentMetaData;
    private int mViewResourceId;
    private int mTextViewId;
    private String mContentMediaType;

    public AttachmentListAdapter(XoActivity pXoContext, int pViewResourceId, int pTextViewId){
        super(pXoContext);

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


    private String getDisplayName(int pPosition) {
        String displayName;
        TalkClientDownload attachment = mAttachments.get(pPosition);
        displayName = attachment.getFileName();
        return displayName;
    }

    private void fetchMetaDataFromAttachmentList() {
        ArrayList<String> filePaths = new ArrayList<String>();
        for (TalkClientDownload attachment : mAttachments) {
            filePaths.add(attachment.getDataFile());
        }
        mAttachmentMetaData = MediaMetaData.factorMetaDataForFileList(filePaths);
    }


}

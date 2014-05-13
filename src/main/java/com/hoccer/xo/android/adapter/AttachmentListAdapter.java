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
    private AttachmentListObserver mAttachmentListObserver;

    public AttachmentListAdapter(XoActivity pXoContext, List<TalkClientDownload> pAttachments, int pViewResourceId, int pTextViewId, String pContentMediaType){
        super(pXoContext);

        mAttachments = pAttachments;
        mViewResourceId = pViewResourceId;
        mTextViewId = pTextViewId;

        mAttachmentListObserver = new AttachmentListObserver();

        if (pContentMediaType.equalsIgnoreCase(ContentMediaType.AUDIO) || pContentMediaType.equalsIgnoreCase(ContentMediaType.VIDEO)) {
            fetchMetaDataFromAttachmentList();
        }
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

    public AttachmentListObserver getAttachmentListObserver() {
        return mAttachmentListObserver;
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

    class AttachmentListObserver extends DataSetObserver{
        @Override
        public void onChanged() {
            super.onChanged();

        }
    }

}

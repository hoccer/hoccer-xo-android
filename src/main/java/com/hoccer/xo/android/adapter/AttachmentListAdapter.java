package com.hoccer.xo.android.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.content.ContentMediaType;
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
    private String mAttachmentMediaType = ContentMediaType.UNKNOWN;

    public AttachmentListAdapter(XoActivity pXoContext, List<TalkClientDownload> pAttachments, int pViewResourceId, int pTextViewId){
        super(pXoContext);

        mAttachments = pAttachments;
        mViewResourceId = pViewResourceId;
        mTextViewId = pTextViewId;
    }

    public void setAttachmentMediaType(String pAttachmentMediaType) {
        mAttachmentMediaType = pAttachmentMediaType;
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
        String displayName;
        TalkClientDownload attachment = mAttachments.get(pPosition);
        displayName = attachment.getFileName();
        if (attachment.getMediaType().equalsIgnoreCase(ContentMediaType.AUDIO)) {
            //  TODO get ID3 tags vom audio file
            attachment.getFileName();
        }
        return displayName;
    }

}

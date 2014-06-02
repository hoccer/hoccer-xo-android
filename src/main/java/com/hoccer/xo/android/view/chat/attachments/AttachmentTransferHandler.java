package com.hoccer.xo.android.view.chat.attachments;


import android.view.View;
import com.hoccer.talk.client.XoTransferAgent;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.client.model.TalkClientUpload;
import com.hoccer.talk.content.ContentState;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.android.view.AttachmentTransferControlView;
import org.apache.log4j.Logger;

/**
 * This class handles interactions with an AttachmentTransferControlView.
 *
 * It receives click events and pauses / resumes the transfer of the given attachment.
 */
public class AttachmentTransferHandler implements View.OnClickListener {

    protected Logger LOG = Logger.getLogger(AttachmentTransferHandler.class);

    public enum TransferAction {
        NONE,
        REQUEST_DOWNLOAD,
        CANCEL_DOWNLOAD,
        REQUEST_UPLOAD,
        CANCEL_UPLOAD,
    }

    private AttachmentTransferControlView mTransferProgress;
    private TransferAction mTransferAction = TransferAction.NONE;

    private IContentObject mContent;

    public AttachmentTransferHandler(AttachmentTransferControlView transferProgress, IContentObject content) {
        mTransferProgress = transferProgress;
        mContent = content;
    }

    @Override
    public void onClick(View v) {
        if (v == mTransferProgress) {
            setTransferAction(getTransferState(mContent));
            switch (mTransferAction) {
                case REQUEST_DOWNLOAD:
                    if (mContent instanceof TalkClientDownload) {
                        TalkClientDownload download = (TalkClientDownload) mContent;
                        XoApplication.getXoClient().requestDownload(download);
                        // TODO: maybe we should rather let this be handled by the adapter / fragment / activity ?
                    }
                    break;
                case CANCEL_DOWNLOAD:
                    if (mContent instanceof TalkClientDownload) {
                        TalkClientDownload download = (TalkClientDownload) mContent;
                        XoApplication.getXoClient().cancelDownload(download);
                    }
                    break;
                case REQUEST_UPLOAD:
                    if (mContent instanceof TalkClientUpload) {
                        TalkClientUpload upload = (TalkClientUpload) mContent;
                        XoApplication.getXoClient().getTransferAgent().requestUpload(upload);
                    }
                    break;
                case CANCEL_UPLOAD:
                    if (mContent instanceof TalkClientUpload) {
                        TalkClientUpload upload = (TalkClientUpload) mContent;
                        XoApplication.getXoClient().getTransferAgent().cancelUpload(upload);
                    }
            }
        }
        mTransferProgress.invalidate();
    }

    private ContentState getTransferState(IContentObject object) {
        XoTransferAgent agent = XoApplication.getXoClient().getTransferAgent();
        ContentState state = object.getContentState();
        if (object instanceof TalkClientDownload) {
            TalkClientDownload download = (TalkClientDownload) object;
            switch (state) {
                case DOWNLOAD_DOWNLOADING:
                case DOWNLOAD_DECRYPTING:
                case DOWNLOAD_DETECTING:
                    if (agent.isDownloadActive(download)) {
                        return state;
                    } else {
                        return ContentState.DOWNLOAD_PAUSED;
                    }
            }
        }
        if (object instanceof TalkClientUpload) {
            TalkClientUpload upload = (TalkClientUpload) object;
            switch (state) {
                case UPLOAD_REGISTERING:
                case UPLOAD_ENCRYPTING:
                case UPLOAD_UPLOADING:
                    if (agent.isUploadActive(upload)) {
                        return state;
                    } else {
                        return ContentState.UPLOAD_PAUSED;
                    }
            }
        }
        return state;
    }

    private void setTransferAction(ContentState state) {
        mTransferProgress.setEnabled(true);
        mTransferAction = TransferAction.NONE;
        switch (state) {
            case DOWNLOAD_NEW:
            case DOWNLOAD_PAUSED:
                mTransferAction = TransferAction.REQUEST_DOWNLOAD;
                break;
            case DOWNLOAD_DETECTING:
                mTransferProgress.setEnabled(false); // TODO: is this needed / balanced?
                break;
            case DOWNLOAD_DECRYPTING:
            case DOWNLOAD_DOWNLOADING:
                mTransferAction = TransferAction.CANCEL_DOWNLOAD;
                break;
            case UPLOAD_NEW:
            case UPLOAD_PAUSED:
                mTransferAction = TransferAction.REQUEST_UPLOAD;
                break;
            case UPLOAD_REGISTERING:
                mTransferProgress.setEnabled(false); // TODO: is this needed / balanced?
                break;
            case UPLOAD_ENCRYPTING:
            case UPLOAD_UPLOADING:
                mTransferAction = TransferAction.CANCEL_UPLOAD;
                break;
            case SELECTED:
                break;
            case UPLOAD_FAILED:
                break;
            case UPLOAD_COMPLETE:
                break;
            case DOWNLOAD_FAILED:
                break;
            case DOWNLOAD_COMPLETE:
                break;
            default:
                break;
        }
    }
}

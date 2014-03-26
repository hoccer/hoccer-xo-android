package com.hoccer.xo.android.content.clipboard;


import android.content.Context;
import android.content.Intent;
import com.hoccer.talk.client.XoClientDatabase;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.client.model.TalkClientUpload;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.activity.ClipboardPreviewActivity;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.content.IContentSelector;
import com.hoccer.xo.android.fragment.CompositionFragment;
import org.apache.log4j.Logger;

import java.sql.SQLException;

public class ClipboardSelector implements IContentSelector {

    Logger LOG = Logger.getLogger(ClipboardSelector.class);

    Clipboard mClipboard;

    public ClipboardSelector(Context context) {
        super();

        mClipboard = Clipboard.get(context);
    }

    @Override
    public String getName() {
        return "Clipboard";
    }

    @Override
    public Intent createSelectionIntent(Context context) {
        Intent intent = new Intent(context, ClipboardPreviewActivity.class);
        intent.putExtra(Clipboard.CLIPBOARD_CONTENT_OBJECT_ID, mClipboard.getClipBoardAttachmentId());
        intent.putExtra(Clipboard.CLIPBOARD_CONTENT_OBJECT_TYPE, mClipboard.getClipboardContentObjectType());
        return intent;
    }

    private IContentObject createObjectFromClipboardData(Context context) throws SQLException {
        IContentObject contentObject = null;
        XoActivity activity = (XoActivity)context;
        XoClientDatabase database = activity.getXoDatabase();

        String type = mClipboard.getClipboardContentObjectType();
        if (type.equals(TalkClientUpload.class.getName())) {
            contentObject = database.findClientUploadById(mClipboard.getClipBoardAttachmentId());
        } else if (type.equals(TalkClientDownload.class.getName())) {
            contentObject = database.findClientDownloadById(mClipboard.getClipBoardAttachmentId());
        }
        return contentObject;
    }

    @Override
    public IContentObject createObjectFromSelectionResult(Context context, Intent intent) {
        IContentObject contentObject = null;
        if (intent != null) {
            if (intent.hasExtra(Clipboard.CLIPBOARD_CONTENT_OBJECT_ID)) {
                try {
                    contentObject = createObjectFromClipboardData(context);
                } catch (SQLException e) {
                    LOG.error("SQL Exception while retrieving clipboard object for selection intent", e);
                }
            }
        }
        mClipboard.clearClipBoard();
        return contentObject;
    }

    public boolean canProcessClipboard() {
        return mClipboard.canProcessClipboard();
    }

}

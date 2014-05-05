package com.hoccer.xo.android.content.clipboard;


import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import com.hoccer.talk.client.XoClientDatabase;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.client.model.TalkClientUpload;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.activity.ClipboardPreviewActivity;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.content.IContentSelector;
import com.whitelabel.gw.release.R;
import org.apache.log4j.Logger;

import java.sql.SQLException;

public class ClipboardSelector implements IContentSelector {

    Logger LOG = Logger.getLogger(ClipboardSelector.class);

    private Clipboard mClipboard;

    private String mName;
    private Drawable mIcon;

    public ClipboardSelector(Context context) {
        mName = context.getResources().getString(R.string.content_clipboard);
        mIcon = context.getResources().getDrawable(R.drawable.ic_attachment_select_data);
        mClipboard = Clipboard.get(context);
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public Drawable getContentIcon() {
        return mIcon;
    }

    @Override
    public Intent createSelectionIntent(Context context) {
        Intent intent = new Intent(context, ClipboardPreviewActivity.class);
        intent.putExtra(Clipboard.CLIPBOARD_CONTENT_OBJECT_ID, mClipboard.getClipBoardAttachmentId());
        intent.putExtra(Clipboard.CLIPBOARD_CONTENT_OBJECT_TYPE, mClipboard.getClipboardContentObjectType());
        return intent;
    }

    private IContentObject createObjectFromClipboardData(Context context) {
        IContentObject contentObject = null;
        XoActivity activity = (XoActivity) context;
        XoClientDatabase database = activity.getXoDatabase();

        String type = mClipboard.getClipboardContentObjectType();

        try {
            if (type.equals(TalkClientUpload.class.getName())) {
                contentObject = database.findClientUploadById(mClipboard.getClipBoardAttachmentId());
            } else if (type.equals(TalkClientDownload.class.getName())) {
                contentObject = database.findClientDownloadById(mClipboard.getClipBoardAttachmentId());
            }
        } catch (SQLException e) {
            LOG.error("SQL Exception while retrieving clipboard object", e);
        }

        return contentObject;
    }

    public IContentObject selectObjectFromClipboard(Context context) {
        IContentObject contentObject = createObjectFromClipboardData(context);
        mClipboard.clearClipBoard();
        return contentObject;
    }

    @Override
    public IContentObject createObjectFromSelectionResult(Context context, Intent intent) {
        IContentObject contentObject = null;
        if (intent != null) {
            if (intent.hasExtra(Clipboard.CLIPBOARD_CONTENT_OBJECT_ID)) {
                contentObject = createObjectFromClipboardData(context);
            }
        }
        mClipboard.clearClipBoard();
        return contentObject;
    }

    public boolean canProcessClipboard() {
        return mClipboard.canProcessClipboard();
    }
}

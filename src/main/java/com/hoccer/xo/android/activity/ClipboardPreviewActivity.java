package com.hoccer.xo.android.activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import com.hoccer.talk.client.XoClientDatabase;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.client.model.TalkClientUpload;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.content.clipboard.Clipboard;
import com.hoccer.xo.android.fragment.ClipboardPreviewFragment;
import com.hoccer.xo.release.R;

import java.sql.SQLException;


public class ClipboardPreviewActivity extends XoActivity {

    private ClipboardPreviewFragment mClipboardPreviewFragment;
    private int mClipboardContentObjectId;
    private String mClipboardContentObjectType;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_clipboard_preview;
    }

    @Override
    protected int getMenuResource() {
        return R.menu.fragment_clipboard_preview;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentManager fragmentManager = getFragmentManager();
        mClipboardPreviewFragment = (ClipboardPreviewFragment) fragmentManager.findFragmentById(R.id.activity_clipboard_preview_fragment);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(Clipboard.CLIPBOARD_CONTENT_OBJECT_ID)) {
                IContentObject contentObject = null;
                XoClientDatabase database = getXoDatabase();
                mClipboardContentObjectId = intent.getIntExtra(Clipboard.CLIPBOARD_CONTENT_OBJECT_ID, 0);
                mClipboardContentObjectType = intent.getStringExtra(Clipboard.CLIPBOARD_CONTENT_OBJECT_TYPE);
                try {
                    if (mClipboardContentObjectType.equals(TalkClientUpload.class.getName())) {
                        contentObject = database.findClientUploadById(mClipboardContentObjectId);
                    } else if (mClipboardContentObjectType.equals(TalkClientDownload.class.getName())) {
                        contentObject = database.findClientDownloadById(mClipboardContentObjectId);
                    }
                } catch (SQLException e) {
                    LOG.error("SQL Exception while retrieving clipboard object for selection intent", e);
                }
                mClipboardPreviewFragment.setContentObject(contentObject);
            }
        }
    }

    public void sendSelectionIntent() {
        Intent intent = new Intent();
        intent.putExtra(Clipboard.CLIPBOARD_CONTENT_OBJECT_ID, mClipboardContentObjectId);
        intent.putExtra(Clipboard.CLIPBOARD_CONTENT_OBJECT_TYPE, mClipboardContentObjectType);

        if (getParent() == null) {
            setResult(Activity.RESULT_OK, intent);
        } else {
            getParent().setResult(Activity.RESULT_OK, intent);
        }
        finish();
    }
}

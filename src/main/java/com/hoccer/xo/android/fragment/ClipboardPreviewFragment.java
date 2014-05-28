package com.hoccer.xo.android.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.activity.ClipboardPreviewActivity;
import com.hoccer.xo.android.base.XoFragment;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

public class ClipboardPreviewFragment extends XoFragment {

    private static final Logger LOG = Logger.getLogger(ClipboardPreviewFragment.class);

    private IContentObject mContentObject;

//    private OldContentView mContentView;
    private TextView mContentName;
    private TextView mContentSize;
    private Button mOkButton;
    private Button mCancelButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LOG.debug("onCreateView()");
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_clipboard_preview, container, false);
//        mContentView = (OldContentView) view.findViewById(R.id.content_view);

        View labels = view.findViewById(R.id.clipboard_preview_labels);
        mContentName = (TextView) labels.findViewById(R.id.content_name);
        mContentSize = (TextView) labels.findViewById(R.id.content_size);

        View buttons = view.findViewById(R.id.clipboard_preview_buttons);
        mOkButton = (Button) buttons.findViewById(R.id.clipboard_preview_button_ok);
        mCancelButton = (Button) buttons.findViewById(R.id.clipboard_preview_button_cancel);

        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectFileFromClipboard();
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getXoActivity().finish();
            }
        });

        return view;
    }

    public void setContentObject(IContentObject contentObject) {
        mContentObject = contentObject;
//        mContentView.displayContent(getXoActivity(), mContentObject, null);

        mContentName.setText("");
        mContentSize.setText(String.valueOf(contentObject.getContentLength()));
    }

    private void selectFileFromClipboard() {

    ClipboardPreviewActivity activity = (ClipboardPreviewActivity)getXoActivity();
        activity.sendSelectionIntent();
    }

    @Override
    public void onResume() {
        LOG.debug("onResume()");
        super.onResume();
    }

    @Override
    public void onPause() {
        LOG.debug("onPause()");
        super.onPause();
    }

    @Override
    public void onDestroy() {
        LOG.debug("onDestroy()");
        super.onDestroy();
    }


}


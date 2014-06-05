package com.hoccer.xo.android.activity;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;
import android.widget.PopupMenu;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.base.XoActionbarActivity;
import com.hoccer.xo.android.content.ContentView;
import com.hoccer.xo.android.content.clipboard.Clipboard;
import com.hoccer.xo.android.fragment.AudioAttachmentListFragment;
import com.hoccer.xo.android.fragment.GroupProfileFragment;
import com.hoccer.xo.android.fragment.MessagingFragment;
import com.hoccer.xo.android.fragment.SingleProfileFragment;
import com.hoccer.xo.release.R;

public class MessagingActivity extends XoActionbarActivity {

    public static final String EXTRA_CLIENT_CONTACT_ID = "clientContactId";

    ActionBar mActionBar;

    MessagingFragment mMessagingFragment;
    AudioAttachmentListFragment mAudioAttachmentListFragment;
    SingleProfileFragment mSingleProfileFragment;
    GroupProfileFragment mGroupProfileFragment;

    int mContactId;
    private IContentObject mClipboardAttachment;
    private getContactIdInConversation m_checkIdReceiver;


    @Override
    protected int getLayoutResource() {
        return R.layout.activity_messaging;
    }

    @Override
    protected int getMenuResource() {
        return R.menu.fragment_messaging;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LOG.debug("onCreate()");
        super.onCreate(savedInstanceState);

        // get action bar (for setting title)
        mActionBar = getActionBar();

        // enable up navigation
        enableUpNavigation();

        // register receiver for notification check
        IntentFilter filter = new IntentFilter("com.hoccer.xo.android.activity.MessagingActivity$getContactIdInConversation");
        filter.addAction("CHECK_ID_IN_CONVERSATION");
        m_checkIdReceiver = new getContactIdInConversation();
        registerReceiver(m_checkIdReceiver, filter);
    }

    @Override
    protected void onResume() {
        LOG.debug("onResume()");
        super.onResume();

        Intent intent = getIntent();

        // handle converse intent
        if (intent != null && intent.hasExtra(EXTRA_CLIENT_CONTACT_ID)) {
            mContactId = intent.getIntExtra(EXTRA_CLIENT_CONTACT_ID, -1);
            m_checkIdReceiver.setId(mContactId);
            if (mContactId == -1) {
                LOG.error("invalid contact id");
            } else {
                showMessagingFragment();
            }
        }
    }

    @Override
    protected void onPause() {
        LOG.debug("onPause()");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(m_checkIdReceiver);
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void showPopupForContentView(ContentView contentView) {
        IContentObject contentObject = contentView.getContent();

        if (contentObject.isContentAvailable()) {
            mClipboardAttachment = contentObject;

            PopupMenu popup = new PopupMenu(this, contentView);
            popup.getMenuInflater().inflate(R.menu.popup_menu_messaging, popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                public boolean onMenuItemClick(MenuItem item) {
                    popupItemSelected(item);
                    return true;
                }
            });

            popup.show();
        }
    }

    public void popupItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_copy_attachment:
                Clipboard clipboard = Clipboard.get(this);
                clipboard.storeAttachment(mClipboardAttachment);
                mClipboardAttachment = null;
        }
    }

    @Override
    public void clipBoardItemSelected(IContentObject contentObject) {
        mMessagingFragment.onAttachmentSelected(contentObject);
    }

    @Override
    protected void applicationWillEnterBackground() {
        super.applicationWillEnterBackground();
        mMessagingFragment.applicationWillEnterBackground();
    }

    public void showMessagingFragment() {

        Bundle bundle = new Bundle();
        bundle.putInt(SingleProfileFragment.ARG_CLIENT_CONTACT_ID, mContactId);

        mMessagingFragment = new MessagingFragment();
        mMessagingFragment.setArguments(bundle);

        mMessagingFragment.setMessagingFragmentListener(new MessagingFragment.IMessagingFragmentListener() {

            @Override
            public void onShowSingleProfileFragment() {
                showSingleProfileFragment();
            }

            @Override
            public void onShowGroupProfileFragment() {
                showGroupProfileFragment();
            }

            @Override
            public void onShowAudioAttachmentListFragment() {
                showAudioAttachmentListFragment();
            }
        });

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fl_messaging_fragment_container, mMessagingFragment);
        ft.commit();
    }

    private void showAudioAttachmentListFragment() {

        mAudioAttachmentListFragment = new AudioAttachmentListFragment();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fl_messaging_fragment_container, mAudioAttachmentListFragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    private void showGroupProfileFragment() {

        Bundle bundle = new Bundle();
        bundle.putInt(GroupProfileFragment.ARG_CLIENT_CONTACT_ID, mContactId);

        mGroupProfileFragment = new GroupProfileFragment();
        mGroupProfileFragment.setArguments(bundle);

        mGroupProfileFragment.setGroupProfileFragmentListener(new GroupProfileFragment.IGroupProfileFragmentListener() {
            @Override
            public void onShowMessageFragment() {
                showMessagingFragment();
            }

            @Override
            public void onShowAudioAttachmentListFragment() {
                showAudioAttachmentListFragment();
            }

        });

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fl_messaging_fragment_container, mGroupProfileFragment);
//        ft.addToBackStack(null);
        ft.commit();
    }

    private void showSingleProfileFragment() {

        Bundle bundle = new Bundle();
        bundle.putInt(SingleProfileFragment.ARG_CLIENT_CONTACT_ID, mContactId);

        mSingleProfileFragment = new SingleProfileFragment();
        mSingleProfileFragment.setArguments(bundle);

        mSingleProfileFragment.setSingleProfileFragmentListener(new SingleProfileFragment.ISingleProfileFragmentListener() {
            @Override
            public void onShowMessageFragment() {
                showMessagingFragment();
            }

            @Override
            public void onShowAudioAttachmentListFragment() {
                showAudioAttachmentListFragment();
            }

        });

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fl_messaging_fragment_container, mSingleProfileFragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    private class getContactIdInConversation extends BroadcastReceiver {
        private int m_contactId;

        public void setId(int id) {
            m_contactId = id;
        }

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            Intent intent = new Intent();
            intent.setAction("CONTACT_ID_IN_CONVERSATION");
            intent.putExtra("id", m_contactId);
            sendBroadcast(intent);
        }

    }

}

package com.hoccer.xo.android.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.*;
import android.widget.*;
import com.hoccer.talk.content.ContentMediaType;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.android.activity.AudioAttachmentListActivity;
import com.hoccer.xo.android.adapter.AttachmentListAdapter;
import com.hoccer.xo.android.adapter.AttachmentListFilterAdapter;
import com.hoccer.xo.android.base.XoListFragment;
import com.hoccer.xo.android.content.MediaItem;
import com.hoccer.xo.android.service.MediaPlayerService;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

public class AudioAttachmentListFragment extends XoListFragment {

    private MediaPlayerService mMediaPlayerService;

    public static final int ALL_CONTACTS_ID = -1;

    private final static Logger LOG = Logger.getLogger(AudioAttachmentListFragment.class);
    private ServiceConnection mConnection;
    private AttachmentListAdapter mAttachmentListAdapter;
    private AttachmentListFilterAdapter mFilterAdapter;
    private int mFilteredContactId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mFilteredContactId = ALL_CONTACTS_ID;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_audio_attachment_list, container, false);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().onCreateOptionsMenu(menu);


    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Intent contactIntent = getActivity().getIntent();
        if (contactIntent != null) {
            if (contactIntent.hasExtra(AudioAttachmentListActivity.EXTRA_CLIENT_CONTACT_ID)) {
                mFilteredContactId = contactIntent
                        .getIntExtra(AudioAttachmentListActivity.EXTRA_CLIENT_CONTACT_ID, ALL_CONTACTS_ID);
            }
        }
        
        loadAttachments();
        getListView().setOnItemClickListener(new OnAttachmentClickHandler());

        Intent intent = new Intent(getActivity(), MediaPlayerService.class);
        getActivity().startService(intent);
        bindService(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar ab = getActivity().getActionBar();
        ab.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        ab.setDisplayShowTitleEnabled(false);
        mFilterAdapter = new AttachmentListFilterAdapter(getXoActivity());
        ab.setListNavigationCallbacks(mFilterAdapter, new AttachmentListFilterHandler());
        ab.setSelectedNavigationItem(mFilterAdapter.getPosition(mFilteredContactId));

    }

    @Override
    public void onPause() {
        super.onPause();
        ActionBar ab = getActivity().getActionBar();
        ab.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        ab.setDisplayShowTitleEnabled(true);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unbindService(mConnection);
        XoApplication.getXoClient().unregisterTransferListener(mAttachmentListAdapter);
    }

    private void loadAttachments() {
        if (mAttachmentListAdapter != null) {
            XoApplication.getXoClient().unregisterTransferListener(mAttachmentListAdapter);
        }

        mAttachmentListAdapter = new AttachmentListAdapter(getXoActivity(), ContentMediaType.AUDIO,
                mFilteredContactId);
        XoApplication.getXoClient().registerTransferListener(mAttachmentListAdapter);
        setListAdapter(mAttachmentListAdapter);

    }

    private void bindService(Intent intent) {

        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MediaPlayerService.MediaPlayerBinder binder = (MediaPlayerService.MediaPlayerBinder) service;
                mMediaPlayerService = binder.getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mMediaPlayerService = null;
            }
        };

        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private class OnAttachmentClickHandler implements AdapterView.OnItemClickListener {
        //TODO if files have been added the position of the currently playing song needs to be changed!

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            MediaItem selectedItem = mAttachmentListAdapter.getItem(position);

            MediaItem currentlyPlayedItem = mMediaPlayerService.getCurrentMediaItem();

            switch (mMediaPlayerService.getPlaylistType()) {
                case ALL_MEDIA: {
                    if (mFilteredContactId == ALL_CONTACTS_ID && !mMediaPlayerService.isStopped()) {
                        if (selectedItem.equals(currentlyPlayedItem)) {
                            updateMediaList();
                            break;
                        }
                    }

                    setMediaList();
                    mMediaPlayerService.play(position);
                }
                break;
                case CONVERSATION_MEDIA: {
                    if (mFilteredContactId == mMediaPlayerService.getCurrentConversationContactId()
                            && !mMediaPlayerService.isStopped()) {
                        if (selectedItem.equals(currentlyPlayedItem)) {
                            updateMediaList();
                            break;
                        }
                    }

                    setMediaList();
                    mMediaPlayerService.play(position);
                }
                break;
                case SINGLE_MEDIA: {
                    setMediaList();
                    mMediaPlayerService.play(position);
                }
                break;
            }

            getXoActivity().showFullscreenPlayer();
        }

        private void setMediaList() {
            List<MediaItem> itemList = new ArrayList<MediaItem>();
            for (MediaItem mediaItem : mAttachmentListAdapter.getMediaItems()) {
                itemList.add(mediaItem);
            }
            mMediaPlayerService.setMediaList(itemList, mAttachmentListAdapter.getConversationContactId());
        }

        private void updateMediaList() {
            int numberOfMediaItemsToAdd = mAttachmentListAdapter.getCount() - mMediaPlayerService.getMediaListSize();

            for (int i = 0; i < numberOfMediaItemsToAdd; ++i) {
                mMediaPlayerService.addMedia(mAttachmentListAdapter.getItem(i));
            }
        }
    }

    private class AttachmentListFilterHandler implements ActionBar.OnNavigationListener {

        @Override
        public boolean onNavigationItemSelected(int itemPosition, long itemId) {
            int selectedContactId = Integer.valueOf(new Long(itemId).intValue());
            if (mFilteredContactId != selectedContactId) {
                mFilteredContactId = selectedContactId;
                loadAttachments();
            }

            return true;
        }
    }
}

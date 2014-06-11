package com.hoccer.xo.android.fragment;

import android.app.ActionBar;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.SparseBooleanArray;
import android.view.*;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.content.ContentMediaType;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.android.activity.AudioAttachmentListActivity;
import com.hoccer.xo.android.adapter.AttachmentListAdapter;
import com.hoccer.xo.android.adapter.AttachmentListFilterAdapter;
import com.hoccer.xo.android.base.XoListFragment;
import com.hoccer.xo.android.content.AudioAttachmentItem;
import com.hoccer.xo.android.service.MediaPlayerService;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AudioAttachmentListFragment extends XoListFragment {

    public static final String AUDIO_ATTACHMENT_REMOVED_ACTION = "com.hoccer.xo.android.fragment.AUDIO_ATTACHMENT_REMOVED_ACTION";
    private MediaPlayerService mMediaPlayerService;

    public static final int ALL_CONTACTS_ID = -1;

    private final static Logger LOG = Logger.getLogger(AudioAttachmentListFragment.class);
    private ServiceConnection mConnection;
    private AttachmentListAdapter mAttachmentListAdapter;
    private AttachmentListFilterAdapter mFilterAdapter;
    private int mFilteredContactId;
    private ActionMode mActionMode;
    private ActionMode.Callback mActionModeCallback;

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

        ListView listView = getListView();
        listView.setOnItemClickListener(new OnAttachmentClickHandler());
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                mAttachmentListAdapter.setSelections(getListView().getCheckedItemPositions());
                mAttachmentListAdapter.notifyDataSetChanged();
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.context_menu_fragment_messaging, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_delete_attachment:
                        deleteSelectedAttachments();
                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });

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

    private void deleteSelectedAttachments() {
        SparseBooleanArray checked = getListView().getCheckedItemPositions();
        LOG.error("#foo " + checked);
        for (int i = 0; i < getListView().getCount(); i++) {
            if (checked.get(i)) {
                deleteAudioAttachment(i);
            }
        }
    }

    private void deleteAudioAttachment(int pos) {
        AudioAttachmentItem item = mAttachmentListAdapter.getItem(pos);

        if (isPlaying(item.getFilePath())) {
            mMediaPlayerService.playNextByRepeatMode();
        }

        if (deleteFile(item.getFilePath())) {
            try {
                XoApplication.getXoClient().getDatabase().deleteMessageByTalkClientDownloadId(((TalkClientDownload) item.getContentObject()).getClientDownloadId());
                mAttachmentListAdapter.removeItem(pos);

                if (mMediaPlayerService != null && !mMediaPlayerService.isPaused())
                mMediaPlayerService.removeMedia(pos);

                Intent intent = new Intent(AUDIO_ATTACHMENT_REMOVED_ACTION);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
            } catch (SQLException e) {
                LOG.error("Error deleting message with client download id of " + ((TalkClientDownload) item.getContentObject()).getClientDownloadId());
                e.printStackTrace();
            }
        }
    }

    private boolean isPlaying(String filePath) {
        if (mMediaPlayerService != null) {
            switch (mMediaPlayerService.getPlaylistType()) {
                case ALL_MEDIA: {
                    if (mFilteredContactId == ALL_CONTACTS_ID && !mMediaPlayerService.isStopped()) {
                        if (filePath.equals(mMediaPlayerService.getCurrentMediaItem().getFilePath())) {
                            return true;
                        }
                    }
                }
                break;
                case CONVERSATION_MEDIA: {
                    if (mFilteredContactId == mMediaPlayerService.getCurrentConversationContactId()
                            && !mMediaPlayerService.isStopped()) {
                        if (filePath.equals(mMediaPlayerService.getCurrentMediaItem().getFilePath())) {
                            return true;
                        }
                    }
                }
                break;
            }
        }
        return false;
    }

    private boolean deleteFile(String filePath) {
        String path = Uri.parse(filePath).getPath();
        File file = new File(path);
        boolean deleted = file.delete();
        return deleted;
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

            AudioAttachmentItem selectedItem = mAttachmentListAdapter.getItem(position);

            if (isPlaying(selectedItem.getFilePath())) {
                updateMediaList();
            } else {
                setMediaList();
                mMediaPlayerService.play(position);
            }

            getXoActivity().showFullscreenPlayer();
        }

        private void setMediaList() {
            List<AudioAttachmentItem> itemList = new ArrayList<AudioAttachmentItem>();
            for (AudioAttachmentItem audioAttachmentItem : mAttachmentListAdapter.getAudioAttachmentItems()) {
                itemList.add(audioAttachmentItem);
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

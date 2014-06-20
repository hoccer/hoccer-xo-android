package com.hoccer.xo.android.fragment;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.*;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SpinnerAdapter;
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

    public static final String ARG_CLIENT_CONTACT_ID = "com.hoccer.xo.android.fragment.ARG_CLIENT_CONTACT_ID";
    public static final String AUDIO_ATTACHMENT_REMOVED_ACTION = "com.hoccer.xo.android.fragment.AUDIO_ATTACHMENT_REMOVED_ACTION";
    public static final String TALK_CLIENT_MESSAGE_ID_EXTRA = "com.hoccer.xo.android.fragment.TALK_CLIENT_MESSAGE_ID_EXTRA";

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
        super.onCreateOptionsMenu(menu, inflater);
        ActionBar ab = getActivity().getActionBar();
        ab.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        ab.setDisplayShowTitleEnabled(false);
        mFilterAdapter = new AttachmentListFilterAdapter(getXoActivity());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getArguments() != null) {
            int clientContactId = getArguments().getInt(ARG_CLIENT_CONTACT_ID);
            if (clientContactId == -1) {
                LOG.error("invalid contact id");
            } else {
                mFilteredContactId = clientContactId;
            }

        } else {
            LOG.error("Creating SingleProfileFragment without arguments is not supported.");
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
                        /*@Note Copy list in order to assure that the checked items are still available in the dialog.
                         *     The checked items might already have been reset by the list.*/
                        SparseBooleanArray checkedItemsCopy = new SparseBooleanArray();
                        SparseBooleanArray checkedItems = getListView().getCheckedItemPositions();

                        for( int i = 0; i < checkedItems.size(); ++i){
                            checkedItems.append(checkedItems.keyAt(i), checkedItems.valueAt(i));
                        }
                        showConfirmDeleteDialog(checkedItemsCopy);

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

    private void deleteSelectedAttachments(SparseBooleanArray checked) {

        int count = getListView().getCount();
        for (int pos = count - 1; pos >= 0; --pos) {

            int indexOfKey = checked.indexOfKey(pos);

            if ( indexOfKey >= 0){
                deleteAudioAttachment(pos);
            }
        }
    }

    private void showConfirmDeleteDialog(final SparseBooleanArray checkedItems) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.attachment_confirm_delete_dialog_message);
        builder.setPositiveButton(R.string.common_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteSelectedAttachments( checkedItems);
            }
        });
        builder.setNegativeButton(R.string.common_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteAudioAttachment(int pos) {
        AudioAttachmentItem item = mAttachmentListAdapter.getItem(pos);

        if (isPlaying(item)) {
            mMediaPlayerService.playNextByRepeatMode();
        } else if (isPaused(item)) {
            mMediaPlayerService.stop();
        }

        if (deleteFile(item.getFilePath())) {
            try {
                int downloadId = ((TalkClientDownload) item.getContentObject()).getClientDownloadId();
                XoApplication.getXoClient().getDatabase().deleteTalkClientDownloadbyId(downloadId);

                int messageId = XoApplication.getXoClient().getDatabase().findMessageByDownloadId(downloadId).getClientMessageId();
                XoApplication.getXoClient().getDatabase().deleteMessageById(messageId);

                mAttachmentListAdapter.removeItem(pos);

                mMediaPlayerService.removeMedia(pos);

                Intent intent = new Intent(AUDIO_ATTACHMENT_REMOVED_ACTION);
                intent.putExtra(TALK_CLIENT_MESSAGE_ID_EXTRA, messageId);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
            } catch (SQLException e) {
                LOG.error("Error deleting message with client download id of " + ((TalkClientDownload) item.getContentObject()).getClientDownloadId());
                e.printStackTrace();
            }
        }
    }

    private boolean isPaused(AudioAttachmentItem item) {
        if (mMediaPlayerService != null && mMediaPlayerService.isPaused()) {
            if (item.equals(mMediaPlayerService.getCurrentMediaItem())) {
                return true;
            }
        }
        return false;
    }

    private boolean isPlaying(AudioAttachmentItem item) {
        if (mMediaPlayerService != null && !mMediaPlayerService.isStopped() && !mMediaPlayerService.isPaused()) {
            if (item.equals(mMediaPlayerService.getCurrentMediaItem())) {
                return true;
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

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            AudioAttachmentItem selectedItem = mAttachmentListAdapter.getItem(position);

            setMediaList();

            if (isPlaying(selectedItem)) {
                mMediaPlayerService.updatePosition(position);
            } else if (isPaused(selectedItem)) {
                mMediaPlayerService.updatePosition(position);
                mMediaPlayerService.play();
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

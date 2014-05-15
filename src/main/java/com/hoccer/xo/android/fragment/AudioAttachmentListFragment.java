package com.hoccer.xo.android.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import com.hoccer.talk.client.XoClientDatabase;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.content.ContentMediaType;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.android.activity.AudioAttachmentListActivity;
import com.hoccer.xo.android.adapter.AttachmentListAdapter;
import com.hoccer.xo.android.base.XoListFragment;
import com.hoccer.xo.android.content.audio.AudioListManager;
import com.hoccer.xo.android.content.audio.MediaPlaylist;
import com.hoccer.xo.android.service.MediaPlayerService;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.List;

public class AudioAttachmentListFragment extends XoListFragment {

    private MediaPlayerService mMediaPlayerService;

    private final static Logger LOG = Logger.getLogger(AudioAttachmentListFragment.class);
    private ServiceConnection mConnection;
    private AttachmentListAdapter mAttachmentListAdapter;
    private AttachmentListObserver mAttachmentListObserver;

    private XoClientDatabase mDatabase = XoApplication.getXoClient().getDatabase();
    private int mConversationContactId;
    private List<TalkClientDownload> mAudioAttachmentList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_audio_attachment_list, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Intent contactIntent = getActivity().getIntent();
        if (contactIntent != null) {
            if (contactIntent.hasExtra(AudioAttachmentListActivity.EXTRA_CLIENT_CONTACT_ID)) {
                mConversationContactId = contactIntent.getIntExtra(AudioAttachmentListActivity.EXTRA_CLIENT_CONTACT_ID, -1);
            }
        }

        if (mConversationContactId != -1) {
            updateAudioAttachmentList();
        }

        mAttachmentListAdapter = new AttachmentListAdapter(getXoActivity());
        mAttachmentListAdapter.setContentMediaType(ContentMediaType.AUDIO);
        mAttachmentListAdapter.setAttachmentList(mAudioAttachmentList);
        setListAdapter(mAttachmentListAdapter);

        final MediaPlaylist playlist = MediaPlaylist.create(mAudioAttachmentList);

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                playlist.setCurrentIndex(position);
                mMediaPlayerService.start(playlist);
                getXoActivity().showFullscreenPlayer();
            }
        });
        mAttachmentListObserver = new AttachmentListObserver();

        Intent intent = new Intent(getActivity(), MediaPlayerService.class);
        getActivity().startService(intent);
        bindService(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        AudioListManager.get(getActivity()).registerObserver(mAttachmentListObserver);
    }

    @Override
    public void onPause() {
        super.onPause();
        AudioListManager.get(getActivity()).unregisterObserver(mAttachmentListObserver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unbindService(mConnection);
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

    public void updateAudioAttachmentList() {

        try {
            mAudioAttachmentList = mDatabase.
                    findClientDownloadByMediaTypeAndConversationContactId(ContentMediaType.AUDIO, mConversationContactId);
        } catch (SQLException e) {
            LOG.error("SQL query failed: " + e.getLocalizedMessage());
        }
    }

    class AttachmentListObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            super.onChanged();
//            loadAttachmentList();
        }
    }
}

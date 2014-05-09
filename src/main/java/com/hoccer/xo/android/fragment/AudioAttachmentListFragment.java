package com.hoccer.xo.android.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.hoccer.talk.model.TalkAttachment;
import com.hoccer.xo.android.adapter.AttachmentListAdapter;
import com.hoccer.xo.android.base.XoListFragment;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.List;

import com.hoccer.xo.android.content.audio.MediaPlayerService;

public class AudioAttachmentListFragment extends XoListFragment {

    private MediaPlayerService mMediaPlayerService;

    private final static Logger LOG = Logger.getLogger(AudioAttachmentListFragment.class);
    private List<TalkAttachment> mAudioAttachmentList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_audio_attachment_list, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        Intent intent = new Intent(getActivity(), MediaPlayerService.class);
        getActivity().startService(intent);
        bindService(intent);

        ListAdapter adapter;

        try {
            mAudioAttachmentList = getXoDatabase().findAttachmentsByMediaType("audio");
            adapter = new AttachmentListAdapter(getXoActivity(),
                    mAudioAttachmentList,R.layout.music_viewer_item, R.id.songTitle);
        } catch (SQLException e) {
            // TODO handle exception!
            LOG.warn(e);
            adapter = null;
        }

        setListAdapter(adapter);

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                mMediaPlayerService.start(mAudioAttachmentList.get(position).getUrl());

                getXoActivity().showFullscreenPlayer();
            }
        });
    }

    private void bindService(Intent intent){

        ServiceConnection connection = new ServiceConnection() {
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

        getActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }
}

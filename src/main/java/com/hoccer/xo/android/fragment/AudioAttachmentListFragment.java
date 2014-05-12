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
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.xo.android.adapter.AttachmentListAdapter;
import com.hoccer.xo.android.base.XoListFragment;
import com.hoccer.xo.android.service.MediaPlayerService;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.List;

public class AudioAttachmentListFragment extends XoListFragment {

    private MediaPlayerService mMediaPlayerService;

    private final static Logger LOG = Logger.getLogger(AudioAttachmentListFragment.class);
    private List<TalkClientDownload> mAudioAttachmentList;
    private ServiceConnection mConnection;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_audio_attachment_list, container, false);
        LOG.error("BAZINGA: AudioAttachmentListFragment.onCreateView");
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        LOG.error("BAZINGA: AudioAttachmentListFragment.onActivityCreated");
        Intent intent = new Intent(getActivity(), MediaPlayerService.class);
        getActivity().startService(intent);
        bindService(intent);

        ListAdapter adapter;

        try {
            LOG.error("BAZINGA: AudioAttachmentListFragment.onActivityCreated: lade mAudioAttachmentList");
            //mAudioAttachmentList = getXoDatabase().findAttachmentsByMediaType("audio");
            mAudioAttachmentList = getXoDatabase().findClientDownloadByMediaType("audio");
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
                mMediaPlayerService.start(mAudioAttachmentList.get(position).getContentDataUrl());

                getXoActivity().showFullscreenPlayer();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unbindService(mConnection);
    }

    private void bindService(Intent intent){

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
}

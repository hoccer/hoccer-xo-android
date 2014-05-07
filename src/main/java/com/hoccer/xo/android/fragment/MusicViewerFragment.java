package com.hoccer.xo.android.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import com.hoccer.xo.android.base.XoListFragment;
import com.hoccer.xo.android.content.audio.MusicLoader;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;

import com.hoccer.xo.android.content.audio.AudioPlayer;

public class MusicViewerFragment extends XoListFragment {

    private ArrayList<HashMap<String, String>> mSongsList = new ArrayList<HashMap<String, String>>();

    private AudioPlayer mAudioPlayer;

    private final static Logger LOG = Logger.getLogger(MusicViewerFragment.class);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_music_viewer, container, false);

        mAudioPlayer = AudioPlayer.get(getXoActivity().getApplicationContext());

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        ArrayList<HashMap<String, String>> songsListData = new ArrayList<HashMap<String, String>>();

        // TODO: get songs from custom folder Hoccer XO only
        MusicLoader plm = new MusicLoader(new String("/sdcard/" + getResources().getString(R.string.app_name)));

        this.mSongsList = plm.getPlayList();

        // looping through playlist
        for (int i = 0; i < mSongsList.size(); i++) {
            // creating new HashMap
            HashMap<String, String> song = mSongsList.get(i);

            // adding HashList to ArrayList
            songsListData.add(song);
        }

        // Adding menuItems to ListView
        ListAdapter adapter = new SimpleAdapter(getActivity(), songsListData,
                R.layout.music_viewer_item, new String[]{"songTitle"}, new int[]{
                R.id.songTitle}
        );

        setListAdapter(adapter);

        ListView lv = getListView();
        // listening to single listitem click
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // getting listitem index
                int songIndex = position;

                LOG.error("strangely, this worked now. Here is where you should start the Fullscreen Player");

                LOG.error(mSongsList.get(songIndex).values().toArray()[0].toString());

                mAudioPlayer.start(mSongsList.get(songIndex).values().toArray()[0].toString());

                getXoActivity().showFullscreenPlayer();
            }
        });
    }
}

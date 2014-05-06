package com.hoccer.xo.android.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import com.hoccer.xo.android.base.XoListFragment;
import com.hoccer.xo.android.content.audio.MusicLoader;
import com.hoccer.xo.release.R;

import com.hoccer.xo.android.activity.MusicViewerActivity;

import java.util.ArrayList;
import java.util.HashMap;

public class MusicViewerFragment extends XoListFragment {

    public ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ArrayList<HashMap<String, String>> songsListData = new ArrayList<HashMap<String, String>>();

        View view = inflater.inflate(R.layout.fragment_music_viewer, container, false);

        // TODO: get songs from custom folder Hoccer XO only
        MusicLoader plm = new MusicLoader(new String("/sdcard/" + getResources().getString(R.string.app_name)));

        this.songsList = plm.getPlayList();

        // looping through playlist
        for (int i = 0; i < songsList.size(); i++) {
            // creating new HashMap
            HashMap<String, String> song = songsList.get(i);

            // adding HashList to ArrayList
            songsListData.add(song);
        }

        // Adding menuItems to ListView
        ListAdapter adapter = new SimpleAdapter((MusicViewerActivity)getXoActivity(), songsListData,
                R.layout.music_viewer_item, new String[]{"songTitle"}, new int[]{
                R.id.songTitle}
        );

        setListAdapter(adapter);

        // TODO: OItemClickListener
        // insert OnItemClickListener here!

        return view;
    }
}

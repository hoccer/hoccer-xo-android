package com.hoccer.xo.android.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.*;
import com.hoccer.talk.client.model.TalkClientMediaCollection;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.android.XoDialogs;
import com.hoccer.xo.android.adapter.MediaCollectionListAdapter;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

import java.sql.SQLException;

public class MediaCollectionListFragment extends ListFragment {

    private final static Logger LOG = Logger.getLogger(AudioAttachmentListFragment.class);

    private MediaCollectionListAdapter mMediaCollectionListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_media_collection_list, container, false);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.fragment_media_collection_list, menu);

        menu.findItem(R.id.menu_collections).setVisible(false);
        getActivity().getActionBar().setTitle(getString(R.string.collections));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_media_collection:
                showCreateMediaCollectionDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        loadCollections();
    }

    private void loadCollections() {
        mMediaCollectionListAdapter = new MediaCollectionListAdapter(getActivity());
        setListAdapter(mMediaCollectionListAdapter);
    }

    private void showCreateMediaCollectionDialog() {
        XoDialogs.showCreateDialog("", R.string.new_media_collection, R.string.enter_new_collection_name, getActivity(),
                new XoDialogs.OnOkClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, String input) {
                        addNewMediaCollection(input);
                    }
                },
                new XoDialogs.OnCancelClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }
        );
    }

    private void addNewMediaCollection(String name) {
        TalkClientMediaCollection collection = null;
        try {
            collection = XoApplication.getXoClient().getDatabase().createMediaCollection(name);
        } catch (SQLException e) {
            LOG.error("Creating new media collection failed.", e);
        }
        mMediaCollectionListAdapter.add(collection);
    }
}
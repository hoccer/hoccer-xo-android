package com.hoccer.xo.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.hoccer.talk.client.model.TalkClientMediaCollection;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MediaCollectionListAdapter extends BaseAdapter {

    private Context mContext;
    private List<TalkClientMediaCollection> mMediaCollections = new ArrayList<TalkClientMediaCollection>();

    private Logger LOG = Logger.getLogger(MediaCollectionListAdapter.class);

    public MediaCollectionListAdapter(Context context) {
        this.mContext = context;
        try {
            loadMediaCollections();
        } catch (SQLException e) {
            LOG.error("Loading media collections failed.", e);
        }
    }

    @Override
    public int getCount() {
        return mMediaCollections.size();
    }

    @Override
    public Object getItem(int position) {
        return mMediaCollections.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mMediaCollections.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_media_collection, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.titleName = (TextView) (convertView.findViewById(R.id.tv_title_name));
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        TalkClientMediaCollection mediaCollection = mMediaCollections.get(position);
        viewHolder.titleName.setText(mediaCollection.getName());

        return convertView;
    }

    public void add(TalkClientMediaCollection mediaCollection) {
        mMediaCollections.add(mediaCollection);
        update();
    }

    private void update() {
        notifyDataSetChanged();
    }

    private void loadMediaCollections() throws SQLException {
        mMediaCollections = XoApplication.getXoClient().getDatabase().findAllMediaCollections();
    }

    private class ViewHolder {
        public TextView titleName;
    }
}

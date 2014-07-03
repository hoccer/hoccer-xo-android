package com.hoccer.xo.android.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.hoccer.xo.release.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nico on 02/07/2014.
 */
public class SearchResultsAdapter extends BaseAdapter {

    private List<Section> mSections = new ArrayList<Section>();
    private static int TYPE_SECTION_HEADER = 0;

    public SearchResultsAdapter() {
        super();
    }

    public void addSection(String caption, Adapter adapter) {
        mSections.add(new Section(caption, adapter));
    }

    public void clear() {
        mSections.clear();
    }

    @Override
    public Object getItem(int position) {
        for (Section section : this.mSections) {
            if (position == 0) {
                return section;
            }

            int size = section.getAdapter().getCount() + 1;

            if (position < size) {
                return section.getAdapter().getItem(position - 1);
            }

            position -= size;
        }

        return null;
    }

    @Override
    public int getCount() {
        int total = 0;

        for (Section section : mSections) {
            total += section.getAdapter().getCount() + 1;
        }

        return total;
    }

    @Override
    public int getViewTypeCount() {
        int total = 1;

        for (Section section : mSections) {
            total += section.getAdapter().getViewTypeCount();
        }

        return total;
    }

    @Override
    public int getItemViewType(int position) {
        int typeOffset = TYPE_SECTION_HEADER + 1;

        for (Section section : this.mSections) {
            if (position == 0) {
                return TYPE_SECTION_HEADER;
            }

            int size = section.getAdapter().getCount() + 1;

            if (position < size) {
                return typeOffset + section.getAdapter()
                        .getItemViewType(position - 1);
            }

            position -= size;
            typeOffset += section.getAdapter().getViewTypeCount();
        }

        return -1;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) != TYPE_SECTION_HEADER;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int sectionIndex = 0;

        for (Section section : this.mSections) {
            if (position == 0) {
                return getHeaderView(section.getCaption(), sectionIndex,
                        convertView, parent);
            }

            // size includes the header
            int size = section.getAdapter().getCount() + 1;

            if (position < size) {
                return section.getAdapter().getView(position - 1, convertView,
                        parent);
            }

            position -= size;
            sectionIndex++;
        }

        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private View getHeaderView(String caption, int index,
                               View convertView, ViewGroup parent){
        View headerView = convertView;
        if (headerView == null) {
            headerView = View.inflate(parent.getContext(), R.layout.item_section_header, null);
            TextView captionView = (TextView) headerView.findViewById(R.id.tv_section_header);
            captionView.setText(caption);
        }

        return headerView;
    }

    class Section {
        private String mCaption;
        private Adapter mAdapter;

        Section(String caption, Adapter adapter) {
            mCaption = caption;
            mAdapter = adapter;
        }

        String getCaption() {
            return mCaption;
        }

        Adapter getAdapter() {
            return mAdapter;
        }
    }
}

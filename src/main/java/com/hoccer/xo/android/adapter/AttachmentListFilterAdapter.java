package com.hoccer.xo.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.base.XoAdapter;
import com.hoccer.xo.android.fragment.AudioAttachmentListFragment;
import com.hoccer.xo.test.R;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nico on 03/06/2014.
 */
public class AttachmentListFilterAdapter extends XoAdapter {

    private final Context mContext;
    private List<FilterItem> mFilterItems = null;
    private LayoutInflater mLayoutInflater = null;

    public AttachmentListFilterAdapter(XoActivity activity) {
        super(activity);
        mContext = activity;
        mLayoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mFilterItems = new ArrayList<FilterItem>();
        loadContactsToFilter();
    }

    @Override
    public int getCount() {
        return mFilterItems.size();
    }

    @Override
    public Integer getItem(int position) {
        return new Integer(mFilterItems.get(position).getContactId());
    }

    @Override
    public long getItemId(int position) {
        return mFilterItems.get(position).getContactId();
    }

    public int getPosition(int contactId){
        for (FilterItem item : mFilterItems) {
            if (item.getContactId() == contactId) {
                return mFilterItems.indexOf(item);
            }
        }

        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View filterView;

        if (convertView == null) {
            filterView = mLayoutInflater.inflate(R.layout.spinner_contact_item, null);
        } else {
            filterView = convertView;
        }

        TextView filterLabel = (TextView) filterView.findViewById(R.id.tv_spinner_contact);
        filterLabel.setText(mFilterItems.get(position).getFilterLabel());
        return filterView;
    }

    private void loadContactsToFilter() {
        try {
            List<TalkClientContact> contacts = getXoClient().getDatabase().findAllClientContacts();

            for (TalkClientContact contact : contacts) {

                if (shouldShow(contact)) {
                    mFilterItems.add(new FilterItem(contact));
                }

            }
        } catch (SQLException e) {
            // TODO: proper exception handling
            LOG.error(e);
        } finally {
            mFilterItems.add(0, new FilterItem());
        }
    }

    private boolean shouldShow(TalkClientContact contact) {
        if (contact.isGroup()) {

            if (contact.isGroupInvolved() && contact.isGroupExisting() && !contact.getGroupPresence().isTypeNearby()) {
                return true;
            }

        } else if (contact.isClient()) {

            if (contact.isClientRelated()) {
                return true;
            }

        } else if (contact.isEverRelated()) {
            return true;
        }

        return false;
    }

    private class FilterItem {

        private String mDefaultFilterLabel;
        private TalkClientContact mContact;

        FilterItem() {
            mDefaultFilterLabel = mContext.getResources().getString(R.string.media_player_default_filter_label);
        };

        FilterItem(TalkClientContact contact) {
            mContact = contact;
        }

        String getFilterLabel() {
            if (mContact != null) {
                return mContact.getName();
            }

            return mDefaultFilterLabel;
        }

        int getContactId() {
            if (mContact != null) {
                return mContact.getClientContactId();
            }

            return AudioAttachmentListFragment.ALL_CONTACTS_ID;
        }

        TalkClientContact getContact() {
            return mContact;
        }

    }
}

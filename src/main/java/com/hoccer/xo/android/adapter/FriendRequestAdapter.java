package com.hoccer.xo.android.adapter;

import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.base.XoAdapter;
import com.hoccer.xo.android.view.AvatarView;
import com.hoccer.xo.release.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class FriendRequestAdapter extends XoAdapter {


    private List<TalkClientContact> mItems = null;

    private OnItemCountChangedListener mListener;

    public FriendRequestAdapter(XoActivity activity) {
        super(activity);
        mItems = new ArrayList<TalkClientContact>();
        loadPendingFreidnRequests();
    }

    private void loadPendingFreidnRequests() {
        int countBefore = getCount();
        mItems = mActivity.getXoDatabase().findAllPendingFriendRequests();
        if(mListener != null && countBefore != getCount()) {
            mListener.onItemCountChanged(getCount());
        }
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public TalkClientContact getItem(int i) {
        return mItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return mItems.get(i).getClientContactId();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        TalkClientContact contact = getItem(i);
        if(view == null) {
            LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_contact_client, null);
        }

        AvatarView avatarView = (AvatarView) view.findViewById(R.id.contact_icon);
        TextView nameView = (TextView) view.findViewById(R.id.contact_name);
        TextView descriptionView = (TextView) view.findViewById(R.id.contact_last_message);
        TextView timeView = (TextView) view.findViewById(R.id.contact_time);
        TextView unseenMessages = (TextView) view.findViewById(R.id.contact_unseen_messages);
        unseenMessages.setVisibility(View.GONE);

        avatarView.setContact(contact);

        nameView.setText(contact.getName());
        timeView.setText("");

        if (contact.getClientRelationship().invitedMe()) {
            descriptionView.setText(R.string.friend_request_invited_me_description);
        } else {
            descriptionView.setText(R.string.friend_request_invited_by_you_description);
        }

        return view;
    }

    @Override
    public void notifyDataSetChanged() {
        loadPendingFreidnRequests();
        super.notifyDataSetChanged();
    }

    public void setOnItemCountChangedListener(OnItemCountChangedListener listener) {
        mListener = listener;
    }
}

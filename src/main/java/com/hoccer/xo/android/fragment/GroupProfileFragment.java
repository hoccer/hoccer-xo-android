package com.hoccer.xo.android.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.hoccer.talk.client.IXoContactListener;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.talk.model.TalkGroup;
import com.hoccer.xo.android.adapter.ContactsAdapter;
import com.hoccer.xo.android.adapter.SimpleContactsAdapter;
import com.hoccer.xo.android.base.XoFragment;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

import java.util.UUID;

/**
 * Fragment for display and editing of group profiles.
 *
 */
public class GroupProfileFragment  extends XoFragment
        implements View.OnClickListener, IXoContactListener {

    private static final Logger LOG = Logger.getLogger(SingleProfileFragment.class);

    public enum Mode {
        PROFILE,
        CREATE_GROUP,
        EDIT_GROUP
    }

    private Mode mMode;

    private TextView mGroupName;
    private EditText mGroupNameEdit;
    private TextView mGroupMembersTitle;
    private ListView mGroupMembersList;

    ContactsAdapter mGroupMemberAdapter;
    TalkClientContact mContact;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LOG.debug("onCreate()");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LOG.debug("onCreateView()");

        View v = inflater.inflate(R.layout.fragment_group_profile, container, false);

        mGroupName = (TextView)v.findViewById(R.id.profile_group_name);
        mGroupNameEdit = (EditText)v.findViewById(R.id.profile_group_name_edit);
        mGroupMembersTitle = (TextView)v.findViewById(R.id.profile_group_name_title);
        mGroupMembersList = (ListView)v.findViewById(R.id.profile_group_members_list);

        return v;
    }


    @Override
    public void onResume() {
        LOG.debug("onResume()");
        super.onResume();
        getXoClient().registerContactListener(this);
    }

    @Override
    public void onPause() {
        LOG.debug("onPause()");
        super.onPause();
        getXoClient().unregisterContactListener(this);
    }

    @Override
    public void onDestroy() {
        LOG.debug("onDestroy()");
        super.onDestroy();
        if(mGroupMemberAdapter != null) {
            mGroupMemberAdapter.onPause();
            mGroupMemberAdapter.onDestroy();
            mGroupMemberAdapter = null;
        }
    }

    private void update(TalkClientContact contact) {
        LOG.debug("update(" + contact.getClientContactId() + ")");

        mGroupMembersTitle.setVisibility(contact.isGroupRegistered() ? View.VISIBLE : View.GONE);
        mGroupMembersList.setVisibility(contact.isGroupRegistered() ? View.VISIBLE : View.GONE);

        // apply data from the contact that needs to recurse
        String name = null;

        TalkGroup groupPresence = contact.getGroupPresence();
        if(groupPresence != null) {
            name = groupPresence.getGroupName();
        }

        if(name == null) {
            name = "";
        }
        mGroupName.setText(name);
        mGroupNameEdit.setText(name);

        mGroupName.setVisibility(View.VISIBLE);
        mGroupNameEdit.setVisibility(View.GONE);

        if (mMode == Mode.EDIT_GROUP) {
            mGroupName.setVisibility(View.GONE);
            mGroupNameEdit.setVisibility(View.VISIBLE);
        }

        if(mGroupMemberAdapter == null) {
            mGroupMemberAdapter = new SimpleContactsAdapter(getXoActivity());
            mGroupMemberAdapter.onCreate();
            mGroupMemberAdapter.onResume();
        }
        mGroupMemberAdapter.setFilter(new ContactsAdapter.Filter() {
            @Override
            public boolean shouldShow(TalkClientContact contact) {
                return contact.isClientGroupJoined(mContact);
            }
        });
        mGroupMemberAdapter.requestReload();
        mGroupMembersList.setAdapter(mGroupMemberAdapter);
    }

    public void refreshContact(TalkClientContact newContact) {
        LOG.debug("refreshContact()");

        mContact = newContact;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LOG.debug("updating ui");
                update(mContact);
            }
        });
    }

    public void showProfile(TalkClientContact contact) {
        mMode = Mode.PROFILE;
        if(contact != null) {
            LOG.debug("showProfile(" + contact.getClientContactId() + ")");
        }
        refreshContact(contact);

        if (contact.isEditable()) {
           editGroup(true);
        }
    }

    public void createGroup() {
        mMode = Mode.CREATE_GROUP;
        LOG.debug("createGroup()");

        String groupTag = UUID.randomUUID().toString();
        mContact = new TalkClientContact(TalkClientContact.TYPE_GROUP);
        mContact.updateGroupTag(groupTag);
        TalkGroup groupPresence = new TalkGroup();
        groupPresence.setGroupTag(groupTag);
        mContact.updateGroupPresence(groupPresence);
        update(mContact);
    }

    public void editGroup(boolean isEditing) {
        if (isEditing) {
            mMode = Mode.EDIT_GROUP;
        } else {
            mMode = Mode.PROFILE;
        }

        refreshContact(mContact);
        update(mContact);
    }

    public void addContact() {
        LOG.debug("addContact()");
        if(mContact != null) {
            //getXoClient().blockContact(mContact);
        }
    }

    public void removeContact() {
        LOG.debug("removeContact()");
        if(mContact != null) {
            //getXoClient().(mContact);
        }
    }

    public TalkClientContact getContact() {
        return mContact;
    }

    private boolean isMyContact(TalkClientContact contact) {
        return mContact != null && mContact == contact || mContact.getClientContactId() == contact.getClientContactId();
    }

    @Override
    public void onContactAdded(TalkClientContact contact) {

    }

    @Override
    public void onContactRemoved(TalkClientContact contact) {

    }

    @Override
    public void onClientPresenceChanged(TalkClientContact contact) {
        if(isMyContact(contact)) {
            refreshContact(contact);
        }
    }

    @Override
    public void onClientRelationshipChanged(TalkClientContact contact) {
        if(isMyContact(contact)) {
            refreshContact(contact);
        }
    }

    @Override
    public void onGroupPresenceChanged(TalkClientContact contact) {
        if(isMyContact(contact)) {
            refreshContact(contact);
        }
    }

    @Override
    public void onGroupMembershipChanged(TalkClientContact contact) {
        if(isMyContact(contact)) {
            refreshContact(contact);
        }
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    public void onAvatarSelected(IContentObject contentObject) {
    }

    @Override
    public void onServiceConnected() {
    }

}

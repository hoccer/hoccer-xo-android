package com.hoccer.xo.android.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.hoccer.talk.client.IXoContactListener;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.client.model.TalkClientUpload;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.talk.model.TalkGroup;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.android.adapter.ContactsAdapter;
import com.hoccer.xo.android.adapter.SimpleContactsAdapter;
import com.hoccer.xo.android.base.XoFragment;
import com.hoccer.xo.android.content.SelectedContent;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

import java.io.File;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Fragment for display and editing of group profiles.
 *
 */
public class GroupProfileFragment  extends XoFragment
        implements View.OnClickListener, IXoContactListener {

    private static final Logger LOG = Logger.getLogger(SingleProfileFragment.class);

    TextView mNameText;
    TextView mGroupMembersTitle;
    ListView mGroupMembersList;

    /*
    Button mGroupCreateButton;
    Button mGroupJoinButton;
    Button mGroupInviteButton;
    Button mGroupLeaveButton;
    Button mGroupKickButton;
    Button mGroupDeleteButton;
    */

    ContactsAdapter mGroupMemberAdapter;
    TalkClientContact mContact;
    IContentObject mAvatarToSet;

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

        // name
        mNameText = (TextView)v.findViewById(R.id.profile_group_name);

        //mNameEditButton = (ImageView)v.findViewById(R.id.profile_name_edit_button);
        //mNameEditButton.setOnClickListener(this);

        // group operations
        /*
        mGroupCreateButton = (Button)v.findViewById(R.id.profile_newgroup_create_button);
        mGroupCreateButton.setOnClickListener(this);
        mGroupJoinButton = (Button)v.findViewById(R.id.profile_group_join_button);
        mGroupJoinButton.setOnClickListener(this);
        mGroupInviteButton = (Button)v.findViewById(R.id.profile_group_invite_button);
        mGroupInviteButton.setOnClickListener(this);
        mGroupLeaveButton = (Button)v.findViewById(R.id.profile_group_leave_button);
        mGroupLeaveButton.setOnClickListener(this);
        mGroupKickButton = (Button)v.findViewById(R.id.profile_group_kick_button);
        mGroupKickButton.setOnClickListener(this);
        mGroupDeleteButton = (Button)v.findViewById(R.id.profile_group_delete_button);
        mGroupDeleteButton.setOnClickListener(this);
        */

        mGroupMembersTitle = (TextView)v.findViewById(R.id.profile_group_title);
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

        String avatarUrl;
        if(contact.isGroup()) {
            avatarUrl = "content://" + R.drawable.avatar_default_group_large;
        } else {
            avatarUrl = "content://" + R.drawable.avatar_default_contact_large;
        }
        TalkClientUpload avatarUpload = null;
        TalkClientDownload avatarDownload = null;
        if(contact.isSelf() || contact.isGroup()) {
            avatarUpload = contact.getAvatarUpload();
            if(avatarUpload != null) {
                if (avatarUpload.isContentAvailable()) {
                    avatarUrl = avatarUpload.getDataFile();
                }
            }
        }
        if(avatarUpload == null && (contact.isClient() || contact.isGroup())) {
            avatarDownload = contact.getAvatarDownload();
            if(avatarDownload != null) {
                if(avatarDownload.isContentAvailable()) {
                    avatarUrl = avatarDownload.getDataFile();
                    Uri uri = Uri.fromFile(new File(avatarUrl));
                    avatarUrl = uri.toString();
                }
            }
        }
        LOG.debug("avatar is " + avatarUrl);

        // group operations
        /*
        int groupCreateVisibility = (contact.isGroup() && !contact.isGroupRegistered()) ? View.VISIBLE : View.GONE;
        int groupJoinedVisibility = (contact.isGroupRegistered() && contact.isGroupJoined()) ? View.VISIBLE : View.GONE;
        int groupInvitedVisibility = (contact.isGroupRegistered() && contact.isGroupInvited()) ? View.VISIBLE : View.GONE;
        int groupAdminVisibility = (contact.isGroupRegistered() && contact.isGroupAdmin()) ? View.VISIBLE : View.GONE;
        int groupMemberVisibility = (contact.isGroupRegistered() && (contact.isGroupJoined() && !contact.isGroupAdmin())) ? View.VISIBLE : View.GONE;
        mGroupCreateButton.setVisibility(groupCreateVisibility);
        mGroupJoinButton.setVisibility(groupInvitedVisibility);
        mGroupInviteButton.setVisibility(groupAdminVisibility);
        mGroupLeaveButton.setVisibility(groupMemberVisibility);
        mGroupKickButton.setVisibility(groupAdminVisibility);
        mGroupDeleteButton.setVisibility(contact.isGroupRegistered() ? View.VISIBLE : View.GONE);
        */
        mGroupMembersTitle.setVisibility(contact.isGroupRegistered() ? View.VISIBLE : View.GONE);
        mGroupMembersList.setVisibility(contact.isGroupRegistered() ? View.VISIBLE : View.GONE);

        // apply data from the contact that needs to recurse
        String name = null;

            TalkGroup groupPresence = contact.getGroupPresence();
            if(groupPresence != null) {
                name = groupPresence.getGroupName();
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

        if(name == null) {
            //mGroupCreateButton.setEnabled(false);
        } else {
            //mGroupCreateButton.setEnabled(true);
        }
        if(name == null) {
            name = "<unnamed>";
        }
        LOG.debug("name is " + name);
        mNameText.setText(name);

        if(contact.isEditable()) {
            //mNameEditButton.setVisibility(View.VISIBLE);
        } else {
            //mNameEditButton.setVisibility(View.GONE);
        }
    }

    private void blockContact() {
        LOG.debug("blockContact()");
        if(mContact != null) {
            getXoClient().blockContact(mContact);
        }
    }

    private void unblockContact() {
        LOG.debug("unblockContact()");
        if(mContact != null) {
            getXoClient().unblockContact(mContact);
        }
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
        if(contact != null) {
            LOG.debug("showProfile(" + contact.getClientContactId() + ")");
        }
        refreshContact(contact);
    }

    public void createGroup() {

        LOG.debug("createGroup()");

        String groupTag = UUID.randomUUID().toString();
        mContact = new TalkClientContact(TalkClientContact.TYPE_GROUP);
        mContact.updateGroupTag(groupTag);
        TalkGroup groupPresence = new TalkGroup();
        groupPresence.setGroupTag(groupTag);
        mContact.updateGroupPresence(groupPresence);
        update(mContact);

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
        /*
        if(v == mNameOverlay || v == mNameText) {
            LOG.debug("onClick(nameOverlay|nameText|nameEditButton)");
            if(mContact != null && mContact.isEditable()) {
                XoDialogs.changeName(getXoActivity(), mContact);
            }
        }
        if(v == mGroupCreateButton) {
            LOG.debug("onClick(groupCreateButton)");
            if(mContact != null && ! mContact.isGroupRegistered()) {
                getXoClient().createGroup(mContact);
            }
        }
        if(v == mGroupJoinButton) {
            LOG.debug("onClick(groupJoinButton)");
            if(mContact != null && mContact.isGroup()) {
                getXoClient().joinGroup(mContact.getGroupId());
            }
        }
        if(v == mGroupLeaveButton) {
            LOG.debug("onClick(groupLeaveButton)");
            if(mContact != null && mContact.isGroupJoined() && !mContact.isGroupAdmin()) {
                XoDialogs.confirmGroupLeave(getXoActivity(), mContact);
            }
        }
        if(v == mGroupDeleteButton) {
            LOG.debug("onClick(groupDeleteButton)");
            if(mContact != null) {
                XoDialogs.confirmDeleteContact(getXoActivity(), mContact);
            }
        }
        if(v == mGroupInviteButton) {
            LOG.debug("onClick(groupInviteButton)");
            if(mContact != null && mContact.isGroup()) {
                XoDialogs.selectGroupInvite(getXoActivity(), mContact);
            }
        }
        if(v == mGroupKickButton) {
            LOG.debug("onClick(groupKickButton)");
            if(mContact != null && mContact.isGroup()) {
                XoDialogs.selectGroupKick(getXoActivity(), mContact);
            }
        }
        */
    }

    @Override
    public void onAvatarSelected(IContentObject contentObject) {
        LOG.debug("onAvatarSelected(" + contentObject.getContentDataUrl() + ")");
        mAvatarToSet = contentObject;
    }

    @Override
    public void onServiceConnected() {
        LOG.debug("onServiceConnected()");

        final IContentObject newAvatar = mAvatarToSet;
        mAvatarToSet = null;
        if(newAvatar != null) {
            XoApplication.getExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    LOG.debug("creating avatar upload");
                    TalkClientUpload upload = SelectedContent.createAvatarUpload(newAvatar);
                    try {
                        getXoDatabase().saveClientUpload(upload);
                        if(mContact.isSelf()) {
                            getXoClient().setClientAvatar(upload);
                        }
                        if(mContact.isGroup()) {
                            getXoClient().setGroupAvatar(mContact, upload);
                        }
                    } catch (SQLException e) {
                        LOG.error("sql error", e);
                    }
                }
            });
        }
    }

}

package com.hoccer.xo.android.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.hoccer.talk.client.IXoContactListener;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.client.model.TalkClientUpload;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.talk.model.TalkGroup;
import com.hoccer.talk.model.TalkPresence;
import com.hoccer.talk.model.TalkRelationship;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.android.XoDialogs;
import com.hoccer.xo.android.adapter.ContactsAdapter;
import com.hoccer.xo.android.adapter.SimpleContactsAdapter;
import com.hoccer.xo.android.base.XoFragment;
import com.hoccer.xo.android.content.SelectedContent;
import com.hoccer.xo.release.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Fragment for display and editing of profiles
 *
 */
public class ProfileFragment extends XoFragment
        implements View.OnClickListener, IXoContactListener {

    private static final Logger LOG = Logger.getLogger(ProfileFragment.class);

    public enum Mode {
        PROFILE,
        CREATE_GROUP,
        CREATE_SELF,
    }

    Mode mMode;

    LinearLayout mNameOverlay;
    TextView  mNameText;
    ImageView mNameEditButton;

    ImageView mAvatarImage;
    IContentObject mAvatarToSet;

    TextView mUserBlockStatus;
    Button   mUserBlockButton;
    Button   mUserDepairButton;
    Button   mUserDeleteButton;

    Button mGroupCreateButton;
    Button mGroupJoinButton;
    Button mGroupInviteButton;
    Button mGroupLeaveButton;
    Button mGroupKickButton;
    Button mGroupDeleteButton;

    TextView mGroupMembersTitle;
    ListView mGroupMembersList;

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

        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        // avatar
        mAvatarImage = (ImageView)v.findViewById(R.id.profile_avatar_image);
        mAvatarImage.setOnClickListener(this);

        // name
        mNameOverlay = (LinearLayout)v.findViewById(R.id.profile_name_overlay);
        mNameOverlay.setOnClickListener(this);
        mNameText = (TextView)v.findViewById(R.id.profile_name_text);
        mNameText.setOnClickListener(this);
        mNameEditButton = (ImageView)v.findViewById(R.id.profile_name_edit_button);
        mNameEditButton.setOnClickListener(this);

        // client operations
        mUserBlockStatus = (TextView)v.findViewById(R.id.profile_user_block_status);
        mUserBlockButton = (Button)v.findViewById(R.id.profile_user_block_button);
        mUserBlockButton.setOnClickListener(this);
        mUserDepairButton = (Button)v.findViewById(R.id.profile_user_depair_button);
        mUserDepairButton.setOnClickListener(this);
        mUserDeleteButton = (Button)v.findViewById(R.id.profile_user_delete_button);
        mUserDeleteButton.setOnClickListener(this);

        // group operations
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
        mGroupMembersTitle = (TextView)v.findViewById(R.id.profile_group_members_title);
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
    public void onClick(View v) {
        if(v == mAvatarImage) {
            LOG.debug("onClick(avatarSetButton)");
            if(mContact != null && mContact.isEditable()) {
                getXoActivity().selectAvatar();
            }
        }
        if(v == mNameOverlay || v == mNameText || v == mNameEditButton) {
            LOG.debug("onClick(nameOverlay|nameText|nameEditButton)");
            if(mContact != null && mContact.isEditable()) {
                XoDialogs.changeName(getXoActivity(), mContact);
            }
        }
        if(v == mUserBlockButton) {
            LOG.debug("onClick(userBlockButton)");
            if(mContact != null && mContact.isClient()) {
                TalkRelationship relationship = mContact.getClientRelationship();
                if(relationship != null) {
                    if(relationship.isBlocked()) {
                        unblockContact();
                    } else {
                        blockContact();
                    }
                }
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
        if(v == mUserDepairButton) {
            LOG.debug("onClick(userDepairButton)");
            if(mContact != null) {
                XoDialogs.confirmDepairContact(getXoActivity(), mContact);
            }
        }
        if(v == mUserDeleteButton) {
            LOG.debug("onClick(contactDeleteButton)");
            if(mContact != null) {
                XoDialogs.confirmDeleteContact(getXoActivity(), mContact);
            }
        }
    }

    @Override
    public void onAvatarSelected(IContentObject contentObject) {
        LOG.debug("onAvatarSelected(" + contentObject.getContentUrl() + ")");
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

    public TalkClientContact getContact() {
        return mContact;
    }

    public void showProfile(TalkClientContact contact) {
        if(contact != null) {
            LOG.debug("showProfile(" + contact.getClientContactId() + ")");
        }
        mMode = Mode.PROFILE;
        mContact = contact;
        refreshContact();
    }

    public void createSelf() {
        LOG.debug("createSelf()");
        mMode = Mode.CREATE_SELF;
    }

    public void createGroup() {
        LOG.debug("createGroup()");
        mMode = Mode.CREATE_GROUP;
        String groupTag = UUID.randomUUID().toString();
        mContact = new TalkClientContact(TalkClientContact.TYPE_GROUP);
        mContact.updateGroupTag(groupTag);
        TalkGroup groupPresence = new TalkGroup();
        groupPresence.setGroupTag(groupTag);
        mContact.updateGroupPresence(groupPresence);
        update(mContact);
    }

    private void update(TalkClientContact contact) {
        LOG.debug("update(" + contact.getClientContactId() + ")");

        String avatarUrl = null;
        if(contact.isGroup()) {
            avatarUrl = "content://" + R.drawable.avatar_default_group_large;
        } else {
            avatarUrl = "content://" + R.drawable.avatar_default_contact_large;
        }
        TalkClientUpload avatarUpload = null;
        TalkClientDownload avatarDownload = null;
        if(contact.isSelf() || contact.isGroup()) {
            LOG.info("checking for avatar upload");
            avatarUpload = contact.getAvatarUpload();
            if(avatarUpload != null) {
                if (avatarUpload.isContentAvailable()) {
                    LOG.info("avatar upload");
                    String file = avatarUpload.getDataFile();
                    if(file == null) {
                        LOG.warn("duh!? avatar upload without a file");
                        avatarUpload = null;
                    } else {
                        avatarUrl = file;
                    }
                } else {
                    LOG.warn("duh!? avatar upload not available");
                    avatarUpload = null;
                }
            }
        }
        if(avatarUpload == null && (contact.isClient() || contact.isGroup())) {
            LOG.info("checking for avatar download");
            avatarDownload = contact.getAvatarDownload();
            if(avatarDownload != null) {
                if(avatarDownload.isContentAvailable()) {
                    LOG.info("avatar download");
                    avatarUrl = avatarDownload.getDataFile();
                }
            }
        }
        LOG.info("avatar is " + avatarUrl);
        ImageLoader.getInstance().displayImage(avatarUrl, mAvatarImage);

        // client operations
        int clientVisibility = contact.isClient() ? View.VISIBLE : View.GONE;
        int clientRelatedVisibility = contact.isClientRelated() ? View.VISIBLE : View.GONE;
        mUserBlockStatus.setVisibility(clientRelatedVisibility);
        mUserBlockButton.setVisibility(clientRelatedVisibility);
        mUserDepairButton.setVisibility(clientRelatedVisibility);
        mUserDeleteButton.setVisibility(clientVisibility);
        // group operations
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
        mGroupMembersTitle.setVisibility(contact.isGroupRegistered() ? View.VISIBLE : View.GONE);
        mGroupMembersList.setVisibility(contact.isGroupRegistered() ? View.VISIBLE : View.GONE);

        // apply data from the contact that needs to recurse
        String name = null;

        if(contact.isClient() || contact.isSelf()) {
            TalkPresence presence = contact.getClientPresence();
            if(presence != null) {
                name = presence.getClientName();
            }
            if(contact.isClient()) {
                TalkRelationship relationship = contact.getClientRelationship();
                if(relationship != null) {
                    mUserBlockStatus.setVisibility(relationship.isBlocked() ? View.VISIBLE : View.GONE);
                    if(relationship.isBlocked()) {
                        mUserBlockButton.setText("Unblock this user");
                    } else {
                        mUserBlockButton.setText("Block this user");
                    }
                }
            }
        }
        if(contact.isGroup()) {
            TalkGroup groupPresence = contact.getGroupPresence();
            if(groupPresence != null) {
                name = groupPresence.getGroupName();
            }
            final ContactsAdapter adapter = new SimpleContactsAdapter(getXoActivity());
            adapter.setFilter(new ContactsAdapter.Filter() {
                @Override
                public boolean shouldShow(TalkClientContact contact) {
                    return contact.isClientGroupJoined(mContact);
                }
            });
            adapter.reload();
            mGroupMembersList.setAdapter(adapter);
        }
        if(name == null) {
            if(mMode == Mode.CREATE_GROUP) {
                name = "<chose a name>";
            }
            if(mMode == Mode.CREATE_SELF) {
                name = "<chose a name>";
            }
        }
        if(name == null) {
            name = "<unnamed>";
        }
        mNameText.setText(name);

        if(contact.isEditable()) {
            mNameEditButton.setVisibility(View.VISIBLE);
        } else {
            mNameEditButton.setVisibility(View.GONE);
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

    public void refreshContact() {
        LOG.debug("refreshContact()");
        if(mContact != null) {
            if(mMode == Mode.PROFILE) {
                LOG.debug("updating from db");
                try {
                    mContact = getXoDatabase().findClientContactById(mContact.getClientContactId());
                    if(mContact.isClient() || mContact.isGroup()) {
                        TalkClientDownload avatarDownload = mContact.getAvatarDownload();
                        if(avatarDownload != null) {
                            getXoDatabase().refreshClientDownload(avatarDownload);
                        }
                    }
                    if(mContact.isSelf()) {
                        TalkClientUpload avatarUpload = mContact.getAvatarUpload();
                        if(avatarUpload != null) {
                            getXoDatabase().refreshClientUpload(avatarUpload);
                        }
                    }
                } catch (SQLException e) {
                    LOG.error("SQL error", e);
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LOG.debug("updating ui");
                    update(mContact);
                }
            });
        }
    }

    private boolean isMyContact(TalkClientContact contact) {
        return mContact != null && mContact.getClientContactId() == contact.getClientContactId();
    }

    @Override
    public void onContactAdded(TalkClientContact contact) {
        // we don't care
    }

    @Override
    public void onContactRemoved(TalkClientContact contact) {
        // we don't care - if our own contact gets removed the activity will finish itself
    }

    @Override
    public void onClientPresenceChanged(TalkClientContact contact) {
        if(isMyContact(contact)) {
            mContact = contact;
            refreshContact();
        }
    }

    @Override
    public void onClientRelationshipChanged(TalkClientContact contact) {
        if(isMyContact(contact)) {
            mContact = contact;
            refreshContact();
        }
    }

    @Override
    public void onGroupPresenceChanged(TalkClientContact contact) {
        if(isMyContact(contact)) {
            mContact = contact;
            refreshContact();
        }
    }

    @Override
    public void onGroupMembershipChanged(TalkClientContact contact) {
        if(isMyContact(contact)) {
            mContact = contact;
            refreshContact();
        }
    }

}

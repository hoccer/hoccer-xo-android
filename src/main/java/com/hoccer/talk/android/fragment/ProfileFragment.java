package com.hoccer.talk.android.fragment;

import android.os.Bundle;
import android.os.RemoteException;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.hoccer.talk.android.R;
import com.hoccer.talk.android.TalkApplication;
import com.hoccer.talk.android.TalkFragment;
import com.hoccer.talk.android.content.ContentObject;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.client.model.TalkClientUpload;
import com.hoccer.talk.model.TalkPresence;
import com.hoccer.talk.model.TalkRelationship;
import com.nostra13.universalimageloader.core.ImageLoader;
import org.apache.log4j.Logger;

import java.io.File;
import java.sql.SQLException;

/**
 * Fragment for display and editing of profiles
 *
 * TODO relax defensive programming
 */
public class ProfileFragment extends TalkFragment
        implements View.OnClickListener, TextView.OnEditorActionListener {

    private static final Logger LOG = Logger.getLogger(ProfileFragment.class);

    TextView mNameText;
    EditText mNameEdit;
    Button   mNameSetButton;

    ImageView mAvatarImage;
    ContentObject mAvatarToSet;

    TextView mUserBlockStatus;
    Button   mUserBlockButton;
    Button   mUserDepairButton;
    Button   mUserDeleteButton;

    Button mGroupJoinButton;
    Button mGroupInviteButton;
    Button mGroupLeaveButton;
    Button mGroupKickButton;
    Button mGroupDisbandButton;
    Button mGroupDeleteButton;

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
        mNameText = (TextView)v.findViewById(R.id.profile_name_text);
        mNameEdit = (EditText)v.findViewById(R.id.profile_name_edit);
        mNameEdit.setOnEditorActionListener(this);
        mNameSetButton = (Button)v.findViewById(R.id.profile_name_set_button);
        mNameSetButton.setOnClickListener(this);

        // client operations
        mUserBlockStatus = (TextView)v.findViewById(R.id.profile_user_block_status);
        mUserBlockButton = (Button)v.findViewById(R.id.profile_user_block_button);
        mUserBlockButton.setOnClickListener(this);
        mUserDepairButton = (Button)v.findViewById(R.id.profile_user_depair_button);
        mUserDepairButton.setOnClickListener(this);
        mUserDeleteButton = (Button)v.findViewById(R.id.profile_user_delete_button);
        mUserDeleteButton.setOnClickListener(this);

        // group operations
        mGroupJoinButton = (Button)v.findViewById(R.id.profile_group_join_button);
        mGroupJoinButton.setOnClickListener(this);
        mGroupInviteButton = (Button)v.findViewById(R.id.profile_group_invite_button);
        mGroupInviteButton.setOnClickListener(this);
        mGroupLeaveButton = (Button)v.findViewById(R.id.profile_group_leave_button);
        mGroupLeaveButton.setOnClickListener(this);
        mGroupKickButton = (Button)v.findViewById(R.id.profile_group_kick_button);
        mGroupKickButton.setOnClickListener(this);
        mGroupDisbandButton = (Button)v.findViewById(R.id.profile_group_disband_button);
        mGroupDisbandButton.setOnClickListener(this);
        mGroupDeleteButton = (Button)v.findViewById(R.id.profile_group_delete_button);
        mGroupDeleteButton.setOnClickListener(this);

        return v;
    }

    @Override
    public void onClick(View v) {
        if(v == mAvatarImage) {
            LOG.debug("onClick(avatarSetButton)");
            if(mContact != null && mContact.isSelf()) {
                getTalkActivity().selectAvatar();
            }
        }
        if(v == mNameSetButton) {
            LOG.debug("onClick(nameSetButton)");
            if(mContact != null && mContact.isSelf()) {
                updateName();
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
        if(v == mGroupJoinButton) {
            LOG.debug("onClick(groupJoinButton)");
            if(mContact != null && mContact.isGroup()) {
                try {
                    getTalkService().joinGroup(mContact.getClientContactId());
                } catch (RemoteException e) {
                    LOG.error("remote error", e);
                }
            }
        }
        if(v == mGroupLeaveButton) {
            LOG.debug("onClick(groupLeaveButton)");
            if(mContact != null && mContact.isGroup()) {
                try {
                    getTalkService().leaveGroup(mContact.getClientContactId());
                } catch (RemoteException e) {
                    LOG.error("remote error", e);
                }
            }
        }
        if(v == mUserDepairButton) {
            LOG.debug("onClick(userDepairButton)");
            if(mContact != null) {
                depairContact();
            }
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if(v == mNameEdit) {
            if(actionId == EditorInfo.IME_ACTION_DONE) {
                LOG.debug("onEditorAction(nameEdit,IME_ACTION_DONE)");
                updateName();
                return true;
            }
        }
        return false;
    }

    @Override
    public void onAvatarSelected(ContentObject contentObject) {
        LOG.debug("onAvatarSelected(" + contentObject.getContentUrl() + ")");
        mAvatarToSet = contentObject;
    }

    @Override
    public void onServiceConnected() {
        LOG.debug("onServiceConnected()");

        final ContentObject newAvatar = mAvatarToSet;
        mAvatarToSet = null;
        if(newAvatar != null) {
            TalkApplication.getExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    LOG.info("creating avatar upload");
                    TalkClientUpload upload = ContentObject.createAvatarUpload(newAvatar);
                    try {
                        getTalkDatabase().saveClientUpload(upload);
                        getTalkService().setClientAvatar(upload.getClientUploadId());
                    } catch (SQLException e) {
                        LOG.error("sql error", e);
                    } catch (RemoteException e) {
                        LOG.error("remote error", e);
                    }
                }
            });
        }
    }

    public void showProfile(TalkClientContact contact) {
        if(contact != null) {
            LOG.debug("showProfile(" + contact.getClientContactId() + ")");
        }
        mContact = contact;
        refreshContact();
    }

    private void update(TalkClientContact contact) {
        LOG.debug("update(" + contact.getClientContactId() + ")");

        String avatarUrl = null;
        if(contact.isGroup()) {
            avatarUrl = "content://" + R.drawable.avatar_default_group_large;
        } else {
            avatarUrl = "content://" + R.drawable.avatar_default_contact_large;
        }
        if(contact.isClient() || contact.isGroup()) {
            TalkClientDownload avatarDownload = contact.getAvatarDownload();
            if(avatarDownload != null) {
                if(avatarDownload.getState() == TalkClientDownload.State.COMPLETE) {
                    File avatarFile = TalkApplication.getAvatarLocation(avatarDownload);
                    avatarUrl = "file://" + avatarFile.toString();
                }
            }
        }
        if(contact.isSelf()) {
            TalkClientUpload avatarUpload = contact.getAvatarUpload();
            if(avatarUpload != null) {
                if(avatarUpload.getState() == TalkClientUpload.State.COMPLETE) {
                    File avatarFile = TalkApplication.getAvatarLocation(avatarUpload);
                    avatarUrl = "file://" + avatarFile.toString();
                }
            }
        }
        ImageLoader.getInstance().displayImage(avatarUrl, mAvatarImage);

        boolean canEditName = contact.isSelf() || contact.isGroupAdmin();
        // name
        mNameText.setVisibility(canEditName ? View.GONE : View.VISIBLE);
        mNameEdit.setVisibility(canEditName ? View.VISIBLE : View.GONE);
        mNameSetButton.setVisibility(canEditName ? View.VISIBLE : View.GONE);
        // client operations
        int clientVisibility = contact.isClient() ? View.VISIBLE : View.GONE;
        int clientRelatedVisibility = contact.isClientRelated() ? View.VISIBLE : View.GONE;
        mUserBlockStatus.setVisibility(clientRelatedVisibility);
        mUserBlockButton.setVisibility(clientRelatedVisibility);
        mUserDepairButton.setVisibility(clientRelatedVisibility);
        mUserDeleteButton.setVisibility(clientVisibility);
        // group operations
        int groupJoinedVisibility = contact.isGroupJoined() ? View.VISIBLE : View.GONE;
        int groupInvitedVisibility = contact.isGroupInvited() ? View.VISIBLE : View.GONE;
        int groupAdminVisibility = contact.isGroupAdmin() ? View.VISIBLE : View.GONE;
        mGroupJoinButton.setVisibility(groupInvitedVisibility);
        mGroupInviteButton.setVisibility(groupAdminVisibility);
        mGroupLeaveButton.setVisibility(groupJoinedVisibility);
        mGroupKickButton.setVisibility(groupAdminVisibility);
        mGroupDisbandButton.setVisibility(groupAdminVisibility);
        mGroupDeleteButton.setVisibility(contact.isGroup() ? View.VISIBLE : View.GONE);

        // apply data from the contact that needs to recurse
        if(contact.isClient() || contact.isSelf()) {
            TalkPresence presence = contact.getClientPresence();
            if(presence != null) {
                mNameText.setText(presence.getClientName());
                mNameEdit.setText(presence.getClientName());
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
    }

    private void updateName() {
        LOG.debug("updateName()");
        try {
            if(mContact != null && mContact.isSelf()) {
                getTalkService().setClientName(mNameEdit.getText().toString());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void depairContact() {
        LOG.debug("depairContact()");
        if(mContact != null) {
            try {
                getTalkService().depairContact(mContact.getClientContactId());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void blockContact() {
        LOG.debug("blockContact()");
        if(mContact != null) {
            try {
                getTalkService().blockContact(mContact.getClientContactId());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void unblockContact() {
        LOG.debug("unblockContact()");
        if(mContact != null) {
            try {
                getTalkService().unblockContact(mContact.getClientContactId());
            } catch (RemoteException e) {
                LOG.error("sql error", e);
            }
        }
    }

    private void refreshContact() {
        LOG.debug("refreshContact()");
        if(mContact != null) {
            LOG.debug("updating from db");
            try {
                mContact = getTalkDatabase().findClientContactById(mContact.getClientContactId());
                if(mContact.isClient() || mContact.isGroup()) {
                    TalkClientDownload avatarDownload = mContact.getAvatarDownload();
                    if(avatarDownload != null) {
                        getTalkDatabase().refreshClientDownload(avatarDownload);
                    }
                }
                if(mContact.isSelf()) {
                    TalkClientUpload avatarUpload = mContact.getAvatarUpload();
                    if(avatarUpload != null) {
                        getTalkDatabase().refreshClientUpload(avatarUpload);
                    }
                }
            } catch (SQLException e) {
                LOG.error("SQL error", e);
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

    @Override
    public void onClientPresenceChanged(int contactId) {
        LOG.debug("onClientPresenceChanged(" + contactId + ")");
        if(mContact != null && mContact.getClientContactId() == contactId) {
            refreshContact();
        }
    }

    @Override
    public void onClientRelationshipChanged(int contactId) {
        LOG.debug("onClientRelationshipChanged(" + contactId + ")");
        if(mContact != null && mContact.getClientContactId() == contactId) {
            refreshContact();
        }
    }

    @Override
    public void onGroupPresenceChanged(int contactId) {
        LOG.debug("onGroupPresenceChanged(" + contactId + ")");
        if(mContact != null && mContact.getClientContactId() == contactId) {
            refreshContact();
        }
    }

    @Override
    public void onGroupMembershipChanged(int contactId) {
        LOG.debug("onGroupMembershipChanged(" + contactId + ")");
        if(mContact != null && mContact.getClientContactId() == contactId) {
            refreshContact();
        }
    }

    @Override
    public void onUploadStateChanged(int contactId, int uploadId, String state) throws RemoteException {
        LOG.debug("onUploadStateChanged(" + contactId + "," + uploadId + "," + state + ")");
        if(state == TalkClientUpload.State.COMPLETE.toString()) {
            refreshContact();
        }
    }

    @Override
    public void onDownloadStateChanged(int contactId, int downloadId, String state) throws RemoteException {
        LOG.debug("onDownloadStateChanged(" + contactId + "," + downloadId + "," + state + ")");
        if(state == TalkClientUpload.State.COMPLETE.toString()) {
            refreshContact();
        }
    }

}

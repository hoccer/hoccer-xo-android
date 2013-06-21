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
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.hoccer.talk.android.R;
import com.hoccer.talk.android.TalkFragment;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.model.TalkPresence;
import com.hoccer.talk.model.TalkRelationship;
import org.apache.log4j.Logger;

import java.sql.SQLException;

/**
 * Fragment for display and editing of profiles
 */
public class ProfileFragment extends TalkFragment implements View.OnClickListener, TextView.OnEditorActionListener {

    private static final Logger LOG = Logger.getLogger(ProfileFragment.class);

    TextView mNameText;
    EditText mNameEdit;
    Button   mNameSetButton;

    TextView mStatusText;
    EditText mStatusEdit;
    Button   mStatusSetButton;

    ImageView mAvatarImage;
    Button    mAvatarSetButton;

    TextView mUserBlockStatus;
    Button   mUserBlockButton;
    Button   mUserDepairButton;
    Button   mUserDeleteButton;

    Button mGroupInviteButton;
    Button mGroupLeaveButton;

    TalkClientContact mContact;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LOG.info("onCreate()");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LOG.info("onCreateView()");

        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        // avatar
        mAvatarImage = (ImageView)v.findViewById(R.id.profile_avatar_image);
        mAvatarSetButton = (Button)v.findViewById(R.id.profile_avatar_set_button);
        mAvatarSetButton.setOnClickListener(this);
        // name
        mNameText = (TextView)v.findViewById(R.id.profile_name_text);
        mNameEdit = (EditText)v.findViewById(R.id.profile_name_edit);
        mNameEdit.setOnEditorActionListener(this);
        mNameSetButton = (Button)v.findViewById(R.id.profile_name_set_button);
        mNameSetButton.setOnClickListener(this);
        // status
        mStatusText = (TextView)v.findViewById(R.id.profile_status_text);
        mStatusEdit = (EditText)v.findViewById(R.id.profile_status_edit);
        mStatusEdit.setOnEditorActionListener(this);
        mStatusSetButton = (Button)v.findViewById(R.id.profile_status_set_button);
        mStatusSetButton.setOnClickListener(this);
        // client operations
        mUserBlockStatus = (TextView)v.findViewById(R.id.profile_user_block_status);
        mUserBlockButton = (Button)v.findViewById(R.id.profile_user_block_button);
        mUserBlockButton.setOnClickListener(this);
        mUserDepairButton = (Button)v.findViewById(R.id.profile_user_depair_button);
        mUserDeleteButton.setOnClickListener(this);
        mUserDeleteButton = (Button)v.findViewById(R.id.profile_user_delete_button);
        mUserDeleteButton.setOnClickListener(this);
        // group operations
        mGroupInviteButton = (Button)v.findViewById(R.id.profile_group_invite_button);
        mGroupInviteButton.setOnClickListener(this);
        mGroupLeaveButton = (Button)v.findViewById(R.id.profile_group_leave_button);
        mGroupLeaveButton.setOnClickListener(this);

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        LOG.info("onCreateOptionsMenu()");
        inflater.inflate(R.menu.fragment_profile, menu);
    }

    @Override
    public void onClick(View v) {
        if(v == mNameSetButton) {
            LOG.info("onClick(nameSetButton)");
            updateName();
        }
        if(v == mStatusSetButton) {
            LOG.info("onClick(statusSetButton)");
            updateStatus();
        }
        if(v == mUserBlockButton) {
            LOG.info("onClick(userBlockButton)");
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
        if(v == mAvatarSetButton) {
            LOG.info("onClick(avatarSetButton)");
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if(v == mNameEdit) {
            if(actionId == EditorInfo.IME_ACTION_DONE) {
                LOG.info("onEditorAction(nameEdit,IME_ACTION_DONE)");
                updateName();
                return true;
            }
        }
        if(v == mStatusEdit) {
            if(actionId == EditorInfo.IME_ACTION_DONE) {
                LOG.info("onEditorAction(nameEdit,IME_ACTION_DONE)");
                updateName();
                return true;
            }
        }
        return false;
    }

    public void showProfile(TalkClientContact contact) {
        mContact = contact;

        // first, configure visibility based on just contact type
        // avatar-related
        mAvatarSetButton.setVisibility(contact.isSelf() ? View.VISIBLE : View.GONE);
        // name-related
        boolean canEditName = contact.isSelf(); // XXX can edit group name if admin
        mNameText.setVisibility(canEditName ? View.GONE : View.VISIBLE);
        mNameEdit.setVisibility(canEditName ? View.VISIBLE : View.GONE);
        mNameSetButton.setVisibility(canEditName ? View.VISIBLE : View.GONE);
        // status-related
        int statusTextVisibility = contact.isSelf() ? View.VISIBLE : View.GONE;
        int statusEditVisibility = contact.isSelf() ? View.GONE : View.VISIBLE;
        if(contact.isGroup()) {
            statusTextVisibility = View.GONE;
            statusEditVisibility = View.GONE;
        }
        mStatusText.setVisibility(statusTextVisibility);
        mStatusEdit.setVisibility(statusEditVisibility);
        mStatusSetButton.setVisibility(statusEditVisibility);
        // client operations
        int clientVisibility = contact.isClient() ? View.VISIBLE : View.GONE;
        mUserBlockStatus.setVisibility(clientVisibility);
        mUserBlockButton.setVisibility(clientVisibility);
        mUserDepairButton.setVisibility(clientVisibility);
        mUserDeleteButton.setVisibility(clientVisibility);
        // group-related
        int groupVisibility = contact.isGroup() ? View.VISIBLE : View.GONE;
        mGroupInviteButton.setVisibility(groupVisibility);
        mGroupLeaveButton.setVisibility(groupVisibility);

        // now apply data from the contact
        if(contact.isClient() || contact.isSelf()) {
            TalkPresence presence = contact.getClientPresence();
            if(presence != null) {
                mNameText.setText(presence.getClientName());
                mNameEdit.setText(presence.getClientName());
                mStatusText.setText(presence.getClientStatus() + " (" + presence.getConnectionStatus() + ")");
                mStatusEdit.setText(presence.getClientStatus());
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
        try {
            if(mContact.isSelf()) {
                getTalkService().setClientName(mNameEdit.getText().toString());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void updateStatus() {
        try {
            if(mContact.isSelf()) {
                getTalkService().setClientStatus(mStatusEdit.getText().toString());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void blockContact() {
        LOG.info("blockContact()");
        if(mContact != null) {
            try {
                getTalkService().blockContact(mContact.getClientContactId());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void unblockContact() {
        LOG.info("unblockContact()");
        if(mContact != null) {
            try {
                getTalkService().unblockContact(mContact.getClientContactId());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void refreshContact() {
        LOG.info("refreshContact()");
        if(mContact != null) {
            try {
                mContact = getTalkDatabase().findClientContactById(mContact.getClientContactId());
            } catch (SQLException e) {
                LOG.error("sql error", e);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showProfile(mContact);
                }
            });
        }
    }

    @Override
    public void onClientPresenceChanged(int contactId) {
        LOG.info("onClientPresenceChanged(" + contactId + ")");
        if(mContact != null && mContact.getClientContactId() == contactId) {
            refreshContact();
        }
    }

    @Override
    public void onClientRelationshipChanged(int contactId) {
        LOG.info("onClientRelationshipChanged(" + contactId + ")");
        if(mContact != null && mContact.getClientContactId() == contactId) {
            refreshContact();
        }
    }

    @Override
    public void onGroupPresenceChanged(int contactId) {
        LOG.info("onGroupPresenceChanged(" + contactId + ")");
        if(mContact != null && mContact.getClientContactId() == contactId) {
            refreshContact();
        }
    }

}

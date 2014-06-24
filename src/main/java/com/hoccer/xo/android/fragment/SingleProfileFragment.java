package com.hoccer.xo.android.fragment;

import com.hoccer.talk.client.IXoContactListener;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.client.model.TalkClientUpload;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.talk.model.TalkPresence;
import com.hoccer.talk.model.TalkRelationship;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.android.XoDialogs;
import com.hoccer.xo.android.activity.ContactsActivity;
import com.hoccer.xo.android.activity.SingleProfileActivity;
import com.hoccer.xo.android.base.XoFragment;
import com.hoccer.xo.android.content.SelectedContent;
import com.hoccer.xo.release.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.log4j.Logger;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.sql.SQLException;

/**
 * Fragment for display and editing of single-contact profiles.
 */
public class SingleProfileFragment extends XoFragment
        implements View.OnClickListener, IXoContactListener, ActionMode.Callback {

    private static final Logger LOG = Logger.getLogger(SingleProfileFragment.class);

    private Mode mMode;

    private TextView mNameText;

    private TextView mKeyText;

    private ImageView mAvatarImage;

    private IContentObject mAvatarToSet;

    private TalkClientContact mContact;

    private EditText mEditName;

    private boolean isRegistered = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LOG.debug("onCreate()");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_single_profile, container, false);
        mAvatarImage = (ImageView) v.findViewById(R.id.profile_avatar_image);
        mNameText = (TextView) v.findViewById(R.id.tv_profile_name);
        mKeyText = (TextView) v.findViewById(R.id.tv_profile_key);
        mEditName = (EditText) v.findViewById(R.id.et_profile_name);

        return v;
    }

    private void initInviteButton(final TalkClientContact contact) {
        Button inviteButton = (Button) getView().findViewById(R.id.btn_profile_invite);
        if(contact == null || contact.isSelf() || contact.isGroup()) {
            inviteButton.setVisibility(View.GONE);
            return;
        } else {
            inviteButton.setVisibility(View.VISIBLE);
        }

        try {
            getXoDatabase().refreshClientContact(contact);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(contact.getClientRelationship() == null || (contact.getClientRelationship().getState() != null && contact.getClientRelationship().getState().equals(TalkRelationship.STATE_NONE))) {
            inviteButton.setText(R.string.friend_request_add_as_friend);
            inviteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getXoActivity().getXoClient().inviteFriend(contact);
                }
            });
        } else if(contact.getClientRelationship().getState() != null && contact.getClientRelationship().getState().equals(TalkRelationship.STATE_INVITED)) {
            inviteButton.setText(R.string.friend_request_cancel_invitation);
            inviteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getXoActivity().getXoClient().disinviteFriend(contact);
                }
            });
        } else if(contact.getClientRelationship().getState() != null && contact.getClientRelationship().getState().equals(TalkRelationship.STATE_INVITED_ME)) {
            inviteButton.setText(R.string.friend_request_accept_invitation);
            inviteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getXoActivity().getXoClient().acceptFriend(contact);
                }
            });
        } else {
            inviteButton.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_profile_block:
                doBlockUnblockAction();
                break;
            case R.id.menu_profile_unblock:
                doBlockUnblockAction();
                break;
            case R.id.menu_profile_delete:
                if (mContact != null) {
                    XoDialogs.showYesNoDialog("ContactDeleteDialog",
                            R.string.dialog_delete_contact_title,
                            R.string.dialog_delete_contact_message,
                            getXoActivity(),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    getXoActivity().getXoClient().deleteContact(mContact);
                                    getXoActivity().hackReturnedFromDialog();
                                }
                            },
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {

                                }
                            });
                }
                break;
            case R.id.menu_profile_edit:
                getActivity().startActionMode(this);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        LOG.debug("onResume()");
        super.onResume();
        getXoClient().registerContactListener(this);
        setHasOptionsMenu(true);
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
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.profile_avatar_image) {
            if (mContact != null && mContact.isEditable()) {
                getXoActivity().selectAvatar();
            }
        }
    }

    private void doBlockUnblockAction() {
        if (mContact != null && mContact.isClient()) {
            TalkRelationship relationship = mContact.getClientRelationship();
            if (relationship != null) {
                if (relationship.isBlocked()) {
                    unblockContact();
                } else {
                    blockContact();
                }
            }
        }
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
        if (newAvatar != null) {
            XoApplication.getExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    LOG.debug("creating avatar upload");
                    TalkClientUpload upload = SelectedContent.createAvatarUpload(newAvatar);
                    try {
                        getXoDatabase().saveClientUpload(upload);
                        if (mContact.isSelf()) {
                            getXoClient().setClientAvatar(upload);
                        }
                        if (mContact.isGroup()) {
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
        if (contact != null) {
            LOG.debug("showProfile(" + contact.getClientContactId() + ")");
        }
        mMode = Mode.PROFILE;
        refreshContact(contact);
    }

    public void createSelf() {
        LOG.debug("createSelf()");
        mMode = Mode.CREATE_SELF;
        mContact = getXoClient().getSelfContact();
        if(mContact.getPublicKey() == null) {
            isRegistered = false;
            getActivity().startActionMode(this);
        }
        update(mContact);
    }

    public void confirmSelf() {
        LOG.debug("confirmSelf()");
        mMode = Mode.CONFIRM_SELF;
        mContact = getXoClient().getSelfContact();
        update(mContact);
    }

    private void update(TalkClientContact contact) {
        LOG.debug("update(" + contact.getClientContactId() + ")");

        String avatarUrl = null;
        if (contact.isGroup()) {
            avatarUrl = "drawable://" + R.drawable.avatar_default_group_large;
        } else {
            avatarUrl = "drawable://" + R.drawable.avatar_default_contact_large;
        }
        TalkClientUpload avatarUpload = null;
        TalkClientDownload avatarDownload = null;
        if (contact.isSelf() || contact.isGroup()) {
            avatarUpload = contact.getAvatarUpload();
            if (avatarUpload != null) {
                if (avatarUpload.isContentAvailable()) {
                    avatarUrl = avatarUpload.getDataFile();
                }
            }
        }
        if (avatarUpload == null && (contact.isClient() || contact.isGroup())) {
            avatarDownload = contact.getAvatarDownload();
            if (avatarDownload != null) {
                if (avatarDownload.isContentAvailable()) {
                    avatarUrl = avatarDownload.getDataFile();
                    Uri uri = Uri.fromFile(new File(avatarUrl));
                    avatarUrl = uri.toString();
                }
            }
        }
        LOG.debug("avatar is " + avatarUrl);
        ImageLoader.getInstance().displayImage(avatarUrl, mAvatarImage);

        // apply data from the contact that needs to recurse
        String name = null;

        if (contact.isClient() || contact.isSelf()) {
            if (contact.isSelf() && !contact.isSelfRegistered()) {
                name = contact.getSelf().getRegistrationName();
            } else {
                TalkPresence presence = contact.getClientPresence();
                if (presence != null) {
                    name = presence.getClientName();
                }
            }
        }
        if (name == null) {
            if (mMode == Mode.CREATE_SELF) {
                name = "<chose a name>";
            }
        }
        if (name == null) {
            name = "<unnamed>";
        }
        LOG.debug("name is " + name);
        mNameText.setText(name);

        mKeyText.setText(getFingerprint());

        initInviteButton(contact);
    }

    public String getFingerprint() {
        String keyId = "";
        if (isRegistered) {
            keyId = mContact.getPublicKey().getKeyId();
        } else {
            return "";
        }
        keyId = keyId.toUpperCase();

        char[] chars = keyId.toCharArray();
        int length = chars.length;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(chars[i]);
            if ((i % 2) == 1) {
                builder.append(":");
            }

        }
        builder.deleteCharAt(builder.lastIndexOf(":"));
        return builder.toString();
    }

    private void blockContact() {
        LOG.debug("blockContact()");
        XoDialogs.showYesNoDialog("BlockContactDialog",
                R.string.dialog_block_user_title,
                R.string.dialog_block_user_message,
                getXoActivity(),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (mContact != null) {
                            getXoClient().blockContact(mContact);
                            getXoActivity().finish();
                        }
                    }
                },
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
    }

    private void unblockContact() {
        LOG.debug("unblockContact()");
        if (mContact != null) {
            getXoClient().unblockContact(mContact);
            getXoActivity().finish();
        }
    }

    public void refreshContact(TalkClientContact newContact) {
        LOG.debug("refreshContact()");
        if (mMode == Mode.PROFILE) {
            mContact = newContact;
            try {
                getXoDatabase().refreshClientContact(mContact);
                if (mContact.getAvatarDownload() != null) {
                    getXoDatabase().refreshClientDownload(mContact.getAvatarDownload());
                }
                if (mContact.getAvatarUpload() != null) {
                    getXoDatabase().refreshClientUpload(mContact.getAvatarUpload());
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

    private boolean isMyContact(TalkClientContact contact) {
        return mContact != null && mContact == contact || mContact.getClientContactId() == contact
                .getClientContactId();
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
        if (isMyContact(contact)) {
            refreshContact(contact);
        }
    }

    @Override
    public void onClientRelationshipChanged(TalkClientContact contact) {
        if (isMyContact(contact)) {
            refreshContact(contact);
        }
    }

    @Override
    public void onGroupPresenceChanged(TalkClientContact contact) {

    }

    @Override
    public void onGroupMembershipChanged(TalkClientContact contact) {

    }

    // Actionmode Callbacks
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        mEditName.setVisibility(View.VISIBLE);
        mNameText.setVisibility(View.INVISIBLE);
        if (isRegistered) {
            mEditName.setText(mNameText.getText());
        }
        mAvatarImage.setOnClickListener(this);
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        String nameString = mEditName.getText().toString();
        mNameText.setText(nameString);
        mEditName.setVisibility(View.GONE);
        mNameText.setVisibility(View.VISIBLE);
        mAvatarImage.setOnClickListener(null);

        if (!isRegistered) {
            mContact.getSelf().setRegistrationName(nameString);
            mContact.updateSelfConfirmed();
            getXoClient().register();
            SingleProfileActivity activity = (SingleProfileActivity) getXoActivity();
            activity.confirmSelf();
            getXoClient().setClientString(nameString, "happy");
            Intent intent = new Intent(activity, ContactsActivity.class);
            activity.startActivity(intent);
        } else {
            getXoClient().setClientString(nameString, "happier");
            refreshContact(mContact);
            update(mContact);
        }

    }

    public enum Mode {
        PROFILE,
        CREATE_SELF,
        CONFIRM_SELF
    }

}

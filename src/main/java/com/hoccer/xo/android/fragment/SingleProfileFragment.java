package com.hoccer.xo.android.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.hoccer.talk.client.IXoContactListener;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.client.model.TalkClientUpload;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.talk.model.TalkPresence;
import com.hoccer.talk.model.TalkRelationship;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.android.XoDialogs;
import com.hoccer.xo.android.activity.SingleProfileActivity;
import com.hoccer.xo.android.base.XoFragment;
import com.hoccer.xo.android.content.SelectedContent;
import com.hoccer.xo.release.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import org.apache.log4j.Logger;

import java.io.File;
import java.sql.SQLException;

/**
 * Fragment for display and editing of single-contact profiles.
 *
 */
public class SingleProfileFragment extends XoFragment
        implements View.OnClickListener, IXoContactListener {

    private static final Logger LOG = Logger.getLogger(SingleProfileFragment.class);

    public enum Mode {
        PROFILE,
        CREATE_SELF,
        CONFIRM_SELF
    }

    Mode mMode;

    LinearLayout mNameOverlay;
    TextView  mNameText;
    ImageView mNameEditButton;

    ImageView mAvatarImage;
    IContentObject mAvatarToSet;

    Button mSelfRegisterButton;

    TextView mUserBlockStatus;
    Button   mUserBlockButton;
    Button   mUserDepairButton;
    Button   mUserDeleteButton;

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

        View v = inflater.inflate(R.layout.fragment_single_profile, container, false);

        // avatar
        mAvatarImage = (ImageView)v.findViewById(R.id.profile_avatar_image);
        mAvatarImage.setOnClickListener(this);

        // name
        mNameOverlay = (LinearLayout)v.findViewById(R.id.profile_name_overlay);
        mNameOverlay.setOnClickListener(this);
        mNameText = (TextView)v.findViewById(R.id.profile_group_name);
        mNameText.setOnClickListener(this);
        mNameEditButton = (ImageView)v.findViewById(R.id.profile_name_edit_button);
        mNameEditButton.setOnClickListener(this);

        // self operations
        mSelfRegisterButton = (Button)v.findViewById(R.id.profile_self_register_button);
        mSelfRegisterButton.setOnClickListener(this);

        // client operations
        mUserBlockStatus = (TextView)v.findViewById(R.id.profile_user_block_status);
        mUserBlockButton = (Button)v.findViewById(R.id.profile_user_block_button);
        mUserBlockButton.setOnClickListener(this);
        mUserDepairButton = (Button)v.findViewById(R.id.profile_user_depair_button);
        mUserDepairButton.setOnClickListener(this);
        mUserDeleteButton = (Button)v.findViewById(R.id.profile_user_delete_button);
        mUserDeleteButton.setOnClickListener(this);

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
        if(v == mSelfRegisterButton) {
            LOG.debug("onClick(selfRegister)");
            if(mContact != null && mContact.isSelf()) {
                mContact.updateSelfConfirmed();
                getXoClient().register();
                SingleProfileActivity activity = (SingleProfileActivity) getXoActivity();
                activity.confirmSelf();
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

    public TalkClientContact getContact() {
        return mContact;
    }

    public void showProfile(TalkClientContact contact) {
        if(contact != null) {
            LOG.debug("showProfile(" + contact.getClientContactId() + ")");
        }
        mMode = Mode.PROFILE;
        refreshContact(contact);
    }

    public void createSelf() {
        LOG.debug("createSelf()");
        mMode = Mode.CREATE_SELF;
        mContact = getXoClient().getSelfContact();
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
        ImageLoader.getInstance().displayImage(avatarUrl, mAvatarImage);

        // self operations
        int selfRegistrationVisibility = contact.isSelf() && mMode == Mode.CREATE_SELF ? View.VISIBLE : View.GONE;
        mSelfRegisterButton.setVisibility(selfRegistrationVisibility);
        // client operations
        int clientVisibility = contact.isClient() ? View.VISIBLE : View.GONE;
        int clientRelatedVisibility = contact.isClientRelated() ? View.VISIBLE : View.GONE;
        mUserBlockStatus.setVisibility(clientRelatedVisibility);
        mUserBlockButton.setVisibility(clientRelatedVisibility);
        mUserDepairButton.setVisibility(clientRelatedVisibility);
        mUserDeleteButton.setVisibility(clientVisibility);

        // apply data from the contact that needs to recurse
        String name = null;

        if(contact.isClient() || contact.isSelf()) {
            if(contact.isSelf() && !contact.isSelfRegistered()) {
                name = contact.getSelf().getRegistrationName();
            } else {
                TalkPresence presence = contact.getClientPresence();
                if(presence != null) {
                    name = presence.getClientName();
                }
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
        if(name == null) {
            if(mMode == Mode.CREATE_SELF) {
                name = "<chose a name>";
            }
        }
        if(name == null) {
            name = "<unnamed>";
        }
        LOG.debug("name is " + name);
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

    public void refreshContact(TalkClientContact newContact) {
        LOG.debug("refreshContact()");
        if(mMode == Mode.PROFILE) {
            mContact = newContact;
            try {
                getXoDatabase().refreshClientContact(mContact);
                if(mContact.getAvatarDownload() != null) {
                    getXoDatabase().refreshClientDownload(mContact.getAvatarDownload());
                }
                if(mContact.getAvatarUpload() != null) {
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
        return mContact != null && mContact == contact || mContact.getClientContactId() == contact.getClientContactId();
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

    }

    @Override
    public void onGroupMembershipChanged(TalkClientContact contact) {

    }

}

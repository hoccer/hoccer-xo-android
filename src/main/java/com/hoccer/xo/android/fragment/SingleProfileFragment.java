package com.hoccer.xo.android.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
        implements View.OnClickListener, IXoContactListener, ActionMode.Callback {

    private static final Logger LOG = Logger.getLogger(SingleProfileFragment.class);

    private EditText mEditName;

    public enum Mode {
        PROFILE,
        CREATE_SELF,
        CONFIRM_SELF
    }

    private Mode mMode;

    private TextView mNameText;
    private TextView mKeyText;
//    private ImageView mNameEditButton;

    private ImageView mAvatarImage;
    private IContentObject mAvatarToSet;

//    private TextView mUserBlockStatus;

    private TalkClientContact mContact;

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
        mNameText = (TextView)v.findViewById(R.id.tv_profile_name);
//        mNameText.setOnClickListener(this);

        mKeyText = (TextView) v.findViewById(R.id.tv_profile_key);

        mEditName = (EditText) v.findViewById(R.id.et_profile_name);

        // client operations
//        mUserBlockStatus = (TextView)v.findViewById(R.id.profile_user_block_status);
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if(!mContact.isSelf()) {
            inflater.inflate(R.menu.fragment_single_profile, menu);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        /*if(mContact.isSelf()) {
            MenuItem blockItem = menu.findItem(R.id.menu_profile_block);
            MenuItem deleteItem = menu.findItem(R.id.menu_profile_delete);
            blockItem.setVisible(false);
            deleteItem.setVisible(false);
        }*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_profile_block:
                doBlockAction();
                break;
            case R.id.menu_profile_delete:
                if(mContact != null) {
                    XoDialogs.confirmDeleteContact(getXoActivity(), mContact);
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        LOG.debug("onResume()");
        super.onResume();
        getXoClient().registerContactListener(this);
        if(mContact.isSelf()) {
            this.getActivity().startActionMode(this);
        }
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
        if(v.getId() == R.id.profile_avatar_image) {
            if(mContact != null && mContact.isEditable()) {
                getXoActivity().selectAvatar();
            }
        }
        /*
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
        }*/
    }

    private void doBlockAction() {
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

        // client operations
        int clientVisibility = contact.isClient() ? View.VISIBLE : View.GONE;
        int clientRelatedVisibility = contact.isClientRelated() ? View.VISIBLE : View.GONE;
//        mUserBlockStatus.setVisibility(clientRelatedVisibility);

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
            /*if(contact.isClient()) {
                TalkRelationship relationship = contact.getClientRelationship();
                if(relationship != null) {
                    mUserBlockStatus.setVisibility(relationship.isBlocked() ? View.VISIBLE : View.GONE);
                    if(relationship.isBlocked()) {
                        mUserBlockButton.setText("Unblock this user");
                    } else {
                        mUserBlockButton.setText("Block this user");
                    }
                }
            }*/
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

        mKeyText.setText(getFingerprint());

/*        if(contact.isEditable()) {
            mNameEditButton.setVisibility(View.VISIBLE);
        } else {
            mNameEditButton.setVisibility(View.GONE);
        }*/
    }

    public String getFingerprint() {
        String keyId = mContact.getPublicKey().getKeyId();
        keyId = keyId.toUpperCase();

        char[] chars = keyId.toCharArray();
        int length = chars.length;
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < length; i++) {
            builder.append(chars[i]);
            if((i%2) == 1) {
                builder.append(":");
            }

        }
        builder.deleteCharAt(builder.lastIndexOf(":"));
        return builder.toString();
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


    // Actionmode Callbacks
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        mEditName.setVisibility(View.VISIBLE);
        mNameText.setVisibility(View.INVISIBLE);
        mEditName.setText(mNameText.getText());
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mNameText.setText(mEditName.getText().toString());
        mEditName.setVisibility(View.GONE);
        mNameText.setVisibility(View.VISIBLE);
    }

}

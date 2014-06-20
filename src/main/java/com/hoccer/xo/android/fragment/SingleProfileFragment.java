package com.hoccer.xo.android.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.hoccer.talk.client.IXoContactListener;
import com.hoccer.talk.client.XoClientDatabase;
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

import java.io.File;
import java.sql.SQLException;

/**
 * Fragment for display and editing of single-contact profiles.
 */
public class SingleProfileFragment extends XoFragment
        implements View.OnClickListener, IXoContactListener, ActionMode.Callback {

    public static final String ARG_CREATE_SELF = "ARG_CREATE_SELF";
    public static final String ARG_CLIENT_CONTACT_ID = "ARG_CLIENT_CONTACT_ID";

    private static final Logger LOG = Logger.getLogger(SingleProfileFragment.class);

    private Mode mMode;

    private TextView mNameText;

    private TextView mKeyText;

    private ImageView mAvatarImage;

    private IContentObject mAvatarToSet;

    private TalkClientContact mContact;

    private EditText mEditName;

    private boolean isRegistered = true;

    private Menu mMenu;

    public interface ISingleProfileFragmentListener {
        public void onShowMessageFragment();

        public void onShowAudioAttachmentListFragment();
    }

    private ISingleProfileFragmentListener mSingleProfileFragmentListener;

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

        if (getArguments() != null) {
            if (getArguments().getBoolean(ARG_CREATE_SELF)) {
                createSelf();
            } else {
                int clientContactId = getArguments().getInt(ARG_CLIENT_CONTACT_ID);
                try {
                    mContact = XoApplication.getXoClient().getDatabase().findClientContactById(clientContactId);
                    showProfile();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } else {
            LOG.error("Creating SingleProfileFragment without arguments is not supported.");
        }

        return v;
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);

        menuInflater.inflate(R.menu.fragment_single_profile, menu);

        if (mContact != null) {
            boolean isSelf = mMode == Mode.CREATE_SELF || mContact.isSelf();

            menu.findItem(R.id.menu_my_profile).setVisible(!isSelf);
            if (mContact.isSelf()) {
                menu.findItem(R.id.menu_profile_edit).setVisible(true);
                menu.findItem(R.id.menu_profile_block).setVisible(false);
                menu.findItem(R.id.menu_profile_unblock).setVisible(false);
                menu.findItem(R.id.menu_profile_delete).setVisible(false);
            } else {
                if (mContact.isNearby()) {
                    menu.findItem(R.id.menu_profile_edit).setVisible(false);
                    menu.findItem(R.id.menu_profile_delete).setVisible(false);
                    menu.findItem(R.id.menu_profile_block).setVisible(false);
                    menu.findItem(R.id.menu_profile_unblock).setVisible(false);
                } else {
                    menu.findItem(R.id.menu_profile_single).setVisible(false);
                    TalkRelationship relationship = mContact.getClientRelationship();
                    if (relationship == null || relationship.isBlocked()) { // todo != null correct
                        menu.findItem(R.id.menu_profile_block).setVisible(false);
                        menu.findItem(R.id.menu_profile_unblock).setVisible(true);
                        menu.findItem(R.id.menu_audio_attachment_list).setVisible(true);
                    } else {
                        menu.findItem(R.id.menu_profile_block).setVisible(true);
                        menu.findItem(R.id.menu_profile_unblock).setVisible(false);
                        menu.findItem(R.id.menu_audio_attachment_list).setVisible(true);
                    }
                }
            }
        }

        mMenu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mContact != null) {
            switch (item.getItemId()) {
                case R.id.menu_profile_block:
                    XoDialogs.confirmBlockContact(getXoActivity(), mContact);
                    return true;
                case R.id.menu_profile_unblock:
                    getXoClient().unblockContact(mContact);
                    return true;
                case R.id.menu_profile_delete:
                    XoDialogs.confirmDeleteContact(getXoActivity(), mContact);
                    return true;
                case R.id.menu_profile_edit:
                    getActivity().startActionMode(this);
                    return true;
                case R.id.menu_audio_attachment_list:
                    mSingleProfileFragmentListener.onShowAudioAttachmentListFragment();
                    return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.profile_avatar_image) {
            if (mContact != null && mContact.isEditable()) {
                getXoActivity().selectAvatar();
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

    private void showProfile() {
        if (mContact != null) {
            LOG.debug("showProfile(" + mContact.getClientContactId() + ")");
        }
        mMode = Mode.PROFILE;
        refreshContact(mContact);
    }

    private void createSelf() {
        LOG.debug("createSelf()");
        mMode = Mode.CREATE_SELF;
        mContact = getXoClient().getSelfContact();
        if (mContact.getPublicKey() == null) {
            isRegistered = false;
            getActivity().startActionMode(this);
        }
        update(mContact);
        updateActionBar();
        finishActivityIfContactDeleted();
    }

    public void updateActionBar() {
        LOG.debug("update(" + mContact.getClientContactId() + ")");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getActivity().getActionBar().setTitle(mContact.getName());
                if (mMode == Mode.CREATE_SELF) {
                    getActivity().getActionBar().setTitle(R.string.welcome_to_title);
                } else {
                    if (mContact.isSelf()) {
                        getActivity().getActionBar().setTitle(R.string.my_profile_title);
                    }
                }
            }
        });
    }

    public void finishActivityIfContactDeleted() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mContact.isDeleted()) {
                    getActivity().finish();
                }
            }
        });
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
    }

    public void setSingleProfileFragmentListener(ISingleProfileFragmentListener singleProfileFragmentListener) {
        this.mSingleProfileFragmentListener = singleProfileFragmentListener;
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

    public void refreshContact(TalkClientContact newContact) {
        LOG.debug("refreshContact()");
        if (mMode == Mode.PROFILE) {
            mContact = newContact;
            try {
                XoClientDatabase database = XoApplication.getXoClient().getDatabase();
                database.refreshClientContact(mContact);
                if (mContact.getAvatarDownload() != null) {
                    database.refreshClientDownload(mContact.getAvatarDownload());
                }
                if (mContact.getAvatarUpload() != null) {
                    database.refreshClientUpload(mContact.getAvatarUpload());
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
        if (isMyContact(contact))  {
            getActivity().finish();
        }
    }

    @Override
    public void onClientPresenceChanged(TalkClientContact contact) {
        if (isMyContact(contact)) {
            refreshContact(contact);
            updateActionBar();
            finishActivityIfContactDeleted();
        }
    }

    @Override
    public void onClientRelationshipChanged(TalkClientContact contact) {
        if (isMyContact(contact)) {
            refreshContact(contact);
            getActivity().invalidateOptionsMenu();
            updateActionBar();
            finishActivityIfContactDeleted();
        }
    }

    @Override
    public void onGroupPresenceChanged(TalkClientContact contact) {
        if (isMyContact(contact)) {
            updateActionBar();
            finishActivityIfContactDeleted();
        }
    }

    @Override
    public void onGroupMembershipChanged(TalkClientContact contact) {
        if (isMyContact(contact)) {
            updateActionBar();
            finishActivityIfContactDeleted();
        }
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

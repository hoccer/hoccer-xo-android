package com.hoccer.xo.android.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import com.hoccer.talk.client.IXoContactListener;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.client.model.TalkClientUpload;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.talk.model.TalkGroup;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.android.XoDialogs;
import com.hoccer.xo.android.adapter.ContactsAdapter;
import com.hoccer.xo.android.adapter.GroupContactsAdapter;
import com.hoccer.xo.android.base.XoFragment;
import com.hoccer.xo.android.content.SelectedContent;
import com.hoccer.xo.release.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import org.apache.log4j.Logger;

import java.io.File;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Fragment for display and editing of group profiles.
 */
public class GroupProfileFragment extends XoFragment
        implements View.OnClickListener, IXoContactListener, ActionMode.Callback {

    private static final Logger LOG = Logger.getLogger(SingleProfileFragment.class);

    public enum Mode {
        PROFILE,
        CREATE_GROUP,
        EDIT_GROUP
    }

    private Mode mMode;

    private String mGroupName;
    private TextView mGroupNameText;
    private EditText mGroupNameEdit;
    private Button mGroupCreateButton;
    private LinearLayout mGroupMembersContainer;
    private TextView mGroupMembersTitle;
    private ListView mGroupMembersList;

    private ContactsAdapter mGroupMemberAdapter;
    private TalkClientContact mGroup;
    private IContentObject mAvatarToSet;
    private ImageView mAvatarImage;

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

        mAvatarImage = (ImageView) v.findViewById(R.id.profile_group_profile_image);
        mGroupNameText = (TextView) v.findViewById(R.id.profile_group_name);
        mGroupNameEdit = (EditText) v.findViewById(R.id.profile_group_name_edit);
        mGroupCreateButton = (Button) v.findViewById(R.id.profile_group_button_create);
        mGroupCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveGroup();
                getXoActivity().finish();
            }
        });
        mGroupMembersContainer = (LinearLayout) v.findViewById(R.id.profile_group_members_container);
        mGroupMembersTitle = (TextView) mGroupMembersContainer.findViewById(R.id.profile_group_members_title);
        mGroupMembersList = (ListView) mGroupMembersContainer.findViewById(R.id.profile_group_members_list);
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
        if (mGroupMemberAdapter != null) {
            mGroupMemberAdapter.onPause();
            mGroupMemberAdapter.onDestroy();
            mGroupMemberAdapter = null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_group_profile_edit:
                getActivity().startActionMode(this);
                break;
            case R.id.menu_group_profile_reject_invitation:
                checkRejectInvitation();
                break;
            case R.id.menu_group_profile_join:
                joinGroup();
                break;
            case R.id.menu_group_profile_leave:
                checkLeaveGroup();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void saveGroup() {
        String newGroupName = mGroupNameEdit.getText().toString();
        if (newGroupName.isEmpty()) {
            newGroupName = "";
        }

        if (mMode == Mode.CREATE_GROUP) {
            if (mGroup != null && !mGroup.isGroupRegistered()) {
                mGroup.getGroupPresence().setGroupName(newGroupName);
                getXoClient().createGroup(mGroup);
                mMode = Mode.PROFILE;
            }
        } else if (mMode == Mode.EDIT_GROUP) {
            if (!newGroupName.equals(mGroupName)) {
                getXoClient().setGroupName(mGroup, newGroupName);
            }
        }
    }

    private void updateAvatar(TalkClientContact contact) {

        String avatarUrl = "drawable://" + R.drawable.avatar_default_group_large;

        TalkClientUpload avatarUpload;
        TalkClientDownload avatarDownload;

        avatarUpload = contact.getAvatarUpload();
        if (avatarUpload != null) {
            if (avatarUpload.isContentAvailable()) {
                avatarUrl = avatarUpload.getDataFile();
            }
        }

        if (avatarUpload == null) {
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
    }

    private void update(TalkClientContact contact) {
        LOG.debug("update(" + contact.getClientContactId() + ")");

        updateAvatar(contact);

        mGroupMembersTitle.setVisibility(contact.isGroupRegistered() ? View.VISIBLE : View.GONE);
        mGroupMembersList.setVisibility(contact.isGroupRegistered() ? View.VISIBLE : View.GONE);

        String name = null;

        TalkGroup groupPresence = contact.getGroupPresence();
        if (groupPresence != null) {
            name = groupPresence.getGroupName();
        }

        if (name == null) {
            name = "";
        }
        mGroupName = name;
        mGroupNameText.setText(name);
        mGroupNameEdit.setText(name);

        switch(mMode) {
            case PROFILE:
                mGroupNameText.setVisibility(View.VISIBLE);
                mGroupNameEdit.setVisibility(View.GONE);
                mGroupCreateButton.setVisibility(View.GONE);
                mGroupMembersContainer.setVisibility(View.VISIBLE);
                break;
            case CREATE_GROUP:
                mGroupNameText.setVisibility(View.GONE);
                mGroupNameEdit.setVisibility(View.VISIBLE);
                mGroupCreateButton.setVisibility(View.VISIBLE);
                mGroupMembersContainer.setVisibility(View.GONE);
                break;
            case EDIT_GROUP:
                mGroupNameText.setVisibility(View.GONE);
                mGroupNameEdit.setVisibility(View.VISIBLE);
                mGroupCreateButton.setVisibility(View.VISIBLE);
                mGroupCreateButton.setVisibility(View.GONE);
                mGroupMembersContainer.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }

        if (mGroupMemberAdapter == null) {
            mGroupMemberAdapter = new GroupContactsAdapter(getXoActivity(), mGroup);
            mGroupMemberAdapter.onCreate();
            mGroupMemberAdapter.onResume();
        }
        mGroupMemberAdapter.setFilter(new ContactsAdapter.Filter() {
            @Override
            public boolean shouldShow(TalkClientContact contact) {
                return contact.isClientGroupInvited(mGroup) || contact.isClientGroupJoined(mGroup);
            }
        });
        mGroupMemberAdapter.requestReload();
        mGroupMembersList.setAdapter(mGroupMemberAdapter);
    }

    public void refreshContact(TalkClientContact newContact) {
        LOG.debug("refreshContact()");

        mGroup = newContact;

        if (mMode == Mode.PROFILE) {
            try {
                getXoDatabase().refreshClientContact(mGroup);
                if (mGroup.getAvatarDownload() != null) {
                    getXoDatabase().refreshClientDownload(mGroup.getAvatarDownload());
                }
                if (mGroup.getAvatarUpload() != null) {
                    getXoDatabase().refreshClientUpload(mGroup.getAvatarUpload());
                }
            } catch (SQLException e) {
                LOG.error("SQL error", e);
            }
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LOG.debug("updating ui");
                if (mGroup.isDeleted()) {
                    getXoActivity().finish();
                } else {
                    update(mGroup);
                }
            }
        });
    }

    public void showProfile(TalkClientContact contact) {
        mMode = Mode.PROFILE;
        if (contact != null) {
            LOG.debug("showProfile(" + contact.getClientContactId() + ")");
        }
        refreshContact(contact);
    }

    public void createGroup() {
        mMode = Mode.CREATE_GROUP;
        LOG.debug("createGroup()");

        String groupTag = UUID.randomUUID().toString();
        mGroup = new TalkClientContact(TalkClientContact.TYPE_GROUP);
        mGroup.updateGroupTag(groupTag);
        TalkGroup groupPresence = new TalkGroup();
        groupPresence.setGroupTag(groupTag);
        mGroup.updateGroupPresence(groupPresence);
        update(mGroup);
    }

    private void manageGroupMembers() {
        LOG.debug("manageGroupMembers()");
        XoDialogs.selectGroupManage(getXoActivity(), mGroup);
    }

    private void deleteGroup() {
        getXoClient().deleteContact(mGroup);
    }

    private void rejectInvitation() {
        leaveGroup();
        getXoActivity().finish();
    }

    private void joinGroup() {
        getXoClient().joinGroup(mGroup.getGroupId());
        getXoActivity().finish();
    }

    private void leaveGroup() {
        getXoClient().leaveGroup(mGroup.getGroupId());
        getXoActivity().finish();
    }

    public TalkClientContact getContact() {
        return mGroup;
    }

    private boolean isMyContact(TalkClientContact contact) {
        return mGroup != null && mGroup == contact || mGroup.getClientContactId() == contact.getClientContactId();
    }

    @Override
    public void onContactAdded(TalkClientContact contact) {
        LOG.debug("onContactAdded");
    }

    @Override
    public void onContactRemoved(TalkClientContact contact) {
        LOG.debug("onContactRemoved");
        if (isMyContact(contact)) {
            getXoActivity().finish();
        }
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
        if (isMyContact(contact)) {
            refreshContact(contact);
        }
    }

    @Override
    public void onGroupMembershipChanged(TalkClientContact contact) {
        if (isMyContact(contact)) {
            refreshContact(contact);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.profile_group_profile_image) {
            if (mGroup != null && mGroup.isEditable()) {
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
                        if (mGroup.isSelf()) {
                            getXoClient().setClientAvatar(upload);
                        }
                        if (mGroup.isGroup()) {
                            getXoClient().setGroupAvatar(mGroup, upload);
                        }
                    } catch (SQLException e) {
                        LOG.error("sql error", e);
                    }
                }
            });
        }
    }

    private void configureMenuItems(Menu menu) {

        MenuItem editGroup = menu.findItem(R.id.menu_group_profile_edit);
        MenuItem addPerson = menu.findItem(R.id.menu_group_profile_add_person);
        MenuItem deleteGroup = menu.findItem(R.id.menu_group_profile_delete);

        editGroup.setVisible(true);
        addPerson.setVisible(false);
        deleteGroup.setVisible(false);
        mAvatarImage.setOnClickListener(null);

        if (mMode == Mode.EDIT_GROUP) {
            editGroup.setVisible(false);
            mAvatarImage.setOnClickListener(this);
            deleteGroup.setVisible(true);
            addPerson.setVisible(true);
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        actionMode.getMenuInflater().inflate(R.menu.fragment_group_profile, menu);

        mMode = Mode.EDIT_GROUP;
        configureMenuItems(menu);
        update(mGroup);
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        LOG.debug("onOptionsItemSelected(" + menuItem.toString() + ")");
        switch (menuItem.getItemId()) {
            case R.id.menu_group_profile_delete:
                checkDeleteGroup();
                break;
            case R.id.menu_group_profile_add_person:
                manageGroupMembers();
                break;
        }
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        mAvatarImage.setOnClickListener(null);
        String newGroupName = mGroupNameEdit.getText().toString();
        if (!newGroupName.isEmpty()) {
            saveGroup();
        }
        mMode = Mode.PROFILE;
        update(mGroup);
    }

    private void checkDeleteGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getXoActivity());
        builder.setTitle(R.string.delete_group_title);
        builder.setMessage(R.string.delete_group_question);
        builder.setCancelable(true);
        builder.setNegativeButton(R.string.common_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int index) {
                LOG.debug("onClick(Cancel)");
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(R.string.common_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int index) {
                LOG.debug("onClick(Ok)");
                deleteGroup();
                dialog.dismiss();
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void checkRejectInvitation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getXoActivity());
        builder.setTitle(R.string.reject_invitation_title);
        builder.setMessage(R.string.reject_invitation_question);
        builder.setCancelable(true);
        builder.setNegativeButton(R.string.common_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int index) {
                LOG.debug("onClick(Cancel)");
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(R.string.common_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int index) {
                LOG.debug("onClick(Ok)");
                rejectInvitation();
                dialog.dismiss();
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void checkLeaveGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getXoActivity());
        builder.setTitle(R.string.leave_title);
        builder.setMessage(R.string.leave_question);
        builder.setCancelable(true);
        builder.setNegativeButton(R.string.common_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int index) {
                LOG.debug("onClick(Cancel)");
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(R.string.common_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int index) {
                LOG.debug("onClick(Ok)");
                leaveGroup();
                dialog.dismiss();
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();
    }
}
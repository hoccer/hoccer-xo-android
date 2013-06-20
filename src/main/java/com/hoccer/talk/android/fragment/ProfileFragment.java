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
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.hoccer.talk.android.R;
import com.hoccer.talk.android.TalkFragment;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.model.TalkPresence;
import com.hoccer.talk.model.TalkRelationship;
import org.apache.log4j.Logger;

import java.sql.SQLException;

public class ProfileFragment extends TalkFragment implements View.OnClickListener, TextView.OnEditorActionListener {

    private static final Logger LOG = Logger.getLogger(ProfileFragment.class);

    TextView mNameText;
    EditText mNameEdit;
    Button mNameSetButton;
    TextView mStatusText;
    EditText mStatusEdit;
    Button mStatusSetButton;
    ImageView mAvatarImage;

    Button mSelfSetAvatarButton;

    TextView mUserBlockStatus;
    Button mUserBlockButton;

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

        mNameText = (TextView)v.findViewById(R.id.profile_name_text);
        mNameEdit = (EditText)v.findViewById(R.id.profile_name_edit);
        mNameEdit.setOnEditorActionListener(this);
        mNameSetButton = (Button)v.findViewById(R.id.profile_name_set_button);
        mNameSetButton.setOnClickListener(this);
        mStatusText = (TextView)v.findViewById(R.id.profile_status_text);
        mStatusEdit = (EditText)v.findViewById(R.id.profile_status_edit);
        mStatusEdit.setOnEditorActionListener(this);
        mStatusSetButton = (Button)v.findViewById(R.id.profile_status_set_button);
        mStatusSetButton.setOnClickListener(this);
        mAvatarImage = (ImageView)v.findViewById(R.id.profile_avatar_image);
        mSelfSetAvatarButton = (Button)v.findViewById(R.id.profile_self_set_avatar);
        mSelfSetAvatarButton.setOnClickListener(this);
        mUserBlockStatus = (TextView)v.findViewById(R.id.profile_user_block_status);
        mUserBlockButton = (Button)v.findViewById(R.id.profile_user_block_button);
        mUserBlockButton.setOnClickListener(this);

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
            updateName();
        }
        if(v == mStatusSetButton) {
            updateStatus();
        }
        if(v == mUserBlockButton) {
        }
        if(v == mSelfSetAvatarButton) {
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if(v == mNameEdit) {
            if(actionId == EditorInfo.IME_ACTION_DONE) {
                updateName();
                return true;
            }
        }
        if(v == mStatusEdit) {
            if(actionId == EditorInfo.IME_ACTION_DONE) {
                updateName();
                return true;
            }
        }
        return false;
    }

    public void showProfile(TalkClientContact contact) {
        mContact = contact;

        boolean canEditName = contact.isSelf();
        mNameText.setVisibility(canEditName ? View.GONE : View.VISIBLE);
        mNameEdit.setVisibility(canEditName ? View.VISIBLE : View.GONE);
        mNameSetButton.setVisibility(canEditName ? View.VISIBLE : View.GONE);

        boolean canEditStatus = contact.isSelf();
        mStatusText.setVisibility(canEditStatus ? View.GONE : View.VISIBLE);
        mStatusEdit.setVisibility(canEditStatus ? View.VISIBLE : View.GONE);
        mStatusSetButton.setVisibility(canEditStatus ? View.VISIBLE : View.GONE);

        // self
        mSelfSetAvatarButton.setVisibility(contact.isSelf() ? View.VISIBLE : View.GONE);
        // client
        mUserBlockStatus.setVisibility(contact.isClient() ? View.VISIBLE : View.GONE);
        mUserBlockButton.setVisibility(contact.isClient() ? View.VISIBLE : View.GONE);
        // group

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
            getTalkService().setClientName(mNameEdit.getText().toString());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void updateStatus() {
        try {
            getTalkService().setClientStatus(mStatusEdit.getText().toString());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void refreshContact() {
        LOG.info("refreshing contact");
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

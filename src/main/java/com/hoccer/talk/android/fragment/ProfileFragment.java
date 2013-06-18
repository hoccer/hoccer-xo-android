package com.hoccer.talk.android.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.hoccer.talk.android.R;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.model.TalkPresence;
import com.hoccer.talk.model.TalkRelationship;
import org.apache.log4j.Logger;

public class ProfileFragment extends SherlockFragment implements View.OnClickListener {

    private static final Logger LOG = Logger.getLogger(ProfileFragment.class);

    TextView mNameText;
    TextView mStatusText;
    ImageView mAvatarImage;

    Button mSelfSetAvatarButton;

    TextView mUserBlockStatus;
    Button mUserBlockButton;

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
        mStatusText = (TextView)v.findViewById(R.id.profile_status_text);
        mAvatarImage = (ImageView)v.findViewById(R.id.profile_avatar_image);
        mSelfSetAvatarButton = (Button)v.findViewById(R.id.profile_self_set_avatar);
        mUserBlockStatus = (TextView)v.findViewById(R.id.profile_user_block_status);
        mUserBlockButton = (Button)v.findViewById(R.id.profile_user_block_button);

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        LOG.info("onCreateOptionsMenu()");
        inflater.inflate(R.menu.fragment_profile, menu);
    }

    @Override
    public void onClick(View v) {
        if(v == mUserBlockButton) {
        }
        if(v == mSelfSetAvatarButton) {
        }
    }

    public void showProfile(TalkClientContact contact) {
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
                mStatusText.setText(presence.getClientStatus() + " (" + presence.getConnectionStatus() + ")");
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

}

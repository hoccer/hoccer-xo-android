package com.hoccer.talk.android.activity;

import android.content.Intent;
import android.os.Bundle;
import com.hoccer.talk.android.R;
import com.hoccer.talk.android.TalkActivity;
import com.hoccer.talk.android.fragment.ProfileFragment;
import com.hoccer.talk.client.model.TalkClientContact;

import java.sql.SQLException;

public class ProfileActivity extends TalkActivity {

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_profile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        if(intent != null && intent.hasExtra("clientContactId")) {
            int contactId = intent.getIntExtra("clientContactId", -1);
            if(contactId == -1) {
                LOG.error("Invalid ccid");
                return;
            }
            try {
                TalkClientContact contact = getTalkClientDatabase().findClientContactById(contactId);
                if(contact != null) {
                    ProfileFragment frag = (ProfileFragment)getSupportFragmentManager().findFragmentById(R.id.activity_profile_fragment);
                    frag.showProfile(contact);
                }
            } catch (SQLException e) {
                LOG.error("NO EXTRA", e);
            }

        } else {
            LOG.info("NO EXTRA");
        }
    }

}

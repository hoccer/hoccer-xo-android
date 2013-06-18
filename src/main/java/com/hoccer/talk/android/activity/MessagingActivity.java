package com.hoccer.talk.android.activity;

import android.content.Intent;
import com.hoccer.talk.android.R;
import com.hoccer.talk.android.TalkActivity;
import com.hoccer.talk.android.fragment.MessagingFragment;
import com.hoccer.talk.client.model.TalkClientContact;

import java.sql.SQLException;

public class MessagingActivity extends TalkActivity {

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_messaging;
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
                MessagingFragment fragment = (MessagingFragment)getSupportFragmentManager().findFragmentById(R.id.activity_messaging_fragment);
                fragment.converseWithContact(contact);
            } catch (SQLException e) {
                LOG.error("NO EXTRA", e);
            }

        } else {
            LOG.info("NO EXTRA");
        }
    }

}

package com.hoccer.talk.android.content.vcard;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import com.hoccer.talk.android.content.ContentObject;
import com.hoccer.talk.android.content.IContentSelector;

public class ContactSelector implements IContentSelector {

    @Override
    public String getName() {
        return "Contact";
    }

    @Override
    public Intent createSelectionIntent(Context context) {
        return new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
    }

    @Override
    public ContentObject createObjectFromSelectionResult(Context context, Intent intent) {
        Uri selectedContent = intent.getData();
        String[] columns = {
        };

        Cursor cursor = context.getContentResolver().query(
                selectedContent, columns, null, null, null);
        cursor.moveToFirst();

        return null;
    }

}

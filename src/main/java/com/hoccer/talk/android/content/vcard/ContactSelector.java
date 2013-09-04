package com.hoccer.talk.android.content.vcard;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import com.hoccer.talk.android.content.ContentObject;
import com.hoccer.talk.android.content.IContentSelector;
import org.apache.log4j.Logger;

public class ContactSelector implements IContentSelector {

    private static final Logger LOG = Logger.getLogger(ContactSelector.class);

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
                ContactsContract.Contacts.LOOKUP_KEY
        };

        Cursor cursor = context.getContentResolver().query(
                selectedContent, columns, null, null, null);
        cursor.moveToFirst();


        int lookupKeyIndex = cursor.getColumnIndex(columns[0]);
        String lookupKey = cursor.getString(lookupKeyIndex);

        String contentUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_VCARD_URI, lookupKey).toString();

        // XXX what the heck!? the android constant seems broken.
        if(contentUri.startsWith("content:/com.android.contacts")) {
            contentUri = contentUri.replace("content:/com.android.contacts", "content://com.android.contacts");
        }

        ContentObject co = new ContentObject();
        co.setMediaType(ContactsContract.Contacts.CONTENT_VCARD_TYPE);
        co.setMediaType("contact");
        co.setContentUrl(contentUri);

        return co;
    }

}

package com.hoccer.xo.android.content.vcard;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import com.hoccer.xo.android.content.SelectedContent;
import com.hoccer.xo.android.content.IContentSelector;
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
    public SelectedContent createObjectFromSelectionResult(Context context, Intent intent) {
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

        SelectedContent co = new SelectedContent(contentUri);
        co.setContentType(ContactsContract.Contacts.CONTENT_VCARD_TYPE);
        co.setContentMediaType("contact");

        return co;
    }

}
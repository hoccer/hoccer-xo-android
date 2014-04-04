package com.hoccer.xo.android.content.vcard;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import com.hoccer.xo.android.content.SelectedContent;
import com.hoccer.xo.android.content.IContentSelector;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;

public class ContactSelector implements IContentSelector {

    private static final Logger LOG = Logger.getLogger(ContactSelector.class);

    private String mName;

    public ContactSelector(Context context) {
        mName = context.getResources().getString(R.string.content_contact);
    }

    @Override
    public String getName() {
        return mName;
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

        Uri contentUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_VCARD_URI, lookupKey);
        String contentUriPath = contentUri.toString();

        // XXX what the heck!? the android constant seems broken.
        if(contentUriPath.startsWith("content:/com.android.contacts")) {
            contentUriPath = contentUriPath.replace("content:/com.android.contacts", "content://com.android.contacts");
        }

        cursor.close();

        SelectedContent co = new SelectedContent(intent, contentUriPath);
        co.setContentType(ContactsContract.Contacts.CONTENT_VCARD_TYPE);
        co.setContentMediaType("vcard");


        AssetFileDescriptor fd = null;
        long fileSize = 0;

        try {
            fd = context.getContentResolver().openAssetFileDescriptor(contentUri, "r");

            //FileInputStream fis = fd.createInputStream();
            //byte[] buf = new byte[(int) fd.getDeclaredLength()];
            //fis.read(buf);
            fileSize = fd.getLength();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        co.setContentLength((int)fileSize);

        return co;
    }

}

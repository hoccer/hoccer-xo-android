package com.hoccer.xo.android.adapter;

import com.hoccer.xo.android.base.XoActivity;

import android.net.Uri;
import android.provider.ContactsContract;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

/**
 * Created by jacob on 30.06.14.
 */
public class SystemContactAdapter extends BaseAdapter {

    private final XoActivity mContext;

    public SystemContactAdapter(XoActivity xoActivity) {
        mContext = xoActivity;
        loadContactsFromAddressbook();
    }


    // TODO: see http://stackoverflow.com/questions/7114573/get-contacts-mobile-number-only
    private void loadContactsFromAddressbook() {

        String[] projection = {
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER,
                ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI,
        };

        mContext.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, projection, ContactsContract.Contacts.HAS_PHONE_NUMBER, new String[]{"1"}, null);
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        return null;
    }

    private class SystemContact {

        private String mDisplayName;
        private Uri mDisplayImage;
        private String mMobilePhoneNumber;


        private SystemContact(String displayName, Uri displayImage, String mobilePhoneNumber) {
            mDisplayName = displayName;
            mDisplayImage = displayImage;
            mMobilePhoneNumber = mobilePhoneNumber;
        }

        public String getDisplayName() {
            return mDisplayName;
        }

        public void setDisplayName(String displayName) {
            mDisplayName = displayName;
        }

        public Uri getDisplayImage() {
            return mDisplayImage;
        }

        public void setDisplayImage(Uri displayImage) {
            mDisplayImage = displayImage;
        }

        public String getMobilePhoneNumber() {
            return mMobilePhoneNumber;
        }

        public void setMobilePhoneNumber(String mobilePhoneNumber) {
            mMobilePhoneNumber = mobilePhoneNumber;
        }
    }
}

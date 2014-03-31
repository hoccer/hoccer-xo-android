package com.hoccer.xo.android.content.clipboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.client.model.TalkClientUpload;
import com.hoccer.talk.content.IContentObject;

public class Clipboard {

    public static final String CLIPBOARD_CONTENT_OBJECT_ID = "CLIPBOARD_CONTENT_OBJECT_ID";
    public static final String CLIPBOARD_CONTENT_OBJECT_TYPE = "CLIPBOARD_CONTENT_OBJECT_TYPE";

    private static Clipboard INSTANCE = null;

    private Context mContext;
    private int mClipBoardAttachmentId;
    private String mClipBoardAttachmentType;

    private static SharedPreferences sPreferences;
    private static SharedPreferences.OnSharedPreferenceChangeListener sPreferencesListener;

    public static synchronized Clipboard get(Context applicationContext) {
        if (INSTANCE == null) {
            INSTANCE = new Clipboard(applicationContext);
        }
        return INSTANCE;
    }

    public Clipboard(Context context) {
        super();

        mContext = context;

        initialize();
    }

    public int getClipBoardAttachmentId() {
        return mClipBoardAttachmentId;
    }

    public String getClipboardContentObjectType() {
        return mClipBoardAttachmentType;
    }

    private void initialize() {

        sPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        sPreferencesListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(CLIPBOARD_CONTENT_OBJECT_ID) || key.equals(CLIPBOARD_CONTENT_OBJECT_TYPE)) {
                    updateValuesFromPreferences();
                }
            }
        };
        sPreferences.registerOnSharedPreferenceChangeListener(sPreferencesListener);

        updateValuesFromPreferences();
    }

    void updateValuesFromPreferences() {
        mClipBoardAttachmentId = sPreferences.getInt(CLIPBOARD_CONTENT_OBJECT_ID, 0);
        mClipBoardAttachmentType = sPreferences.getString(CLIPBOARD_CONTENT_OBJECT_TYPE, null);
    }

    public void storeAttachment(IContentObject contentObject) {
        int id = 0;
        String type = null;
        if (contentObject instanceof TalkClientUpload) {
            TalkClientUpload upload = (TalkClientUpload) contentObject;
            id = upload.getClientUploadId();
            type = contentObject.getClass().getName();
        } else if (contentObject instanceof TalkClientDownload) {
            TalkClientDownload download = (TalkClientDownload) contentObject;
            id = download.getClientDownloadId();
            type = contentObject.getClass().getName();
        }
        storeIntToClipboard(CLIPBOARD_CONTENT_OBJECT_ID, id);
        storeStringToClipboard(CLIPBOARD_CONTENT_OBJECT_TYPE, type);
    }

    private void storeIntToClipboard(String key, int value) {
        SharedPreferences.Editor editor = sPreferences.edit();
        editor.remove(key).commit();
        editor.putInt(key, value).commit();
    }

    private void storeStringToClipboard(String key, String value) {
        SharedPreferences.Editor editor = sPreferences.edit();
        editor.remove(key).commit();
        editor.putString(key, value).commit();
    }

    public void clearClipBoard() {
        SharedPreferences.Editor editor = sPreferences.edit();
        editor.remove(Clipboard.CLIPBOARD_CONTENT_OBJECT_ID).commit();
        editor.remove(Clipboard.CLIPBOARD_CONTENT_OBJECT_TYPE).commit();
    }

    public boolean canProcessClipboard() {
        return (mClipBoardAttachmentId > 0 && mClipBoardAttachmentType != null);
    }
}

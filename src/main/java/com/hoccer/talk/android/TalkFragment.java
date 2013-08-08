package com.hoccer.talk.android;

import android.app.Activity;
import android.os.IBinder;
import android.os.RemoteException;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.hoccer.talk.android.content.ContentObject;
import com.hoccer.talk.android.service.ITalkClientService;
import com.hoccer.talk.android.service.ITalkClientServiceListener;
import com.hoccer.talk.client.TalkClientDatabase;
import org.apache.log4j.Logger;

import java.io.File;

/**
 * Base class for fragments working with the talk client
 *
 * This encapsulated commonalities:
 *  - access to activity for db and services
 */
public class TalkFragment extends SherlockFragment implements ITalkClientServiceListener {

    protected Logger LOG = null;

    private TalkActivity mActivity;

    public TalkFragment() {
        LOG = Logger.getLogger(getClass());
    }

    /** Just a dummy so we can implement the listener interface */
    @Override
    public IBinder asBinder() {
        return null;
    }

    public File getAvatarDirectory() {
        return new File(mActivity.getFilesDir(), "avatars");
    }

    public TalkActivity getTalkActivity() {
        return mActivity;
    }

    public TalkClientDatabase getTalkDatabase() {
        return mActivity.getTalkClientDatabase();
    }

    public ITalkClientService getTalkService() {
        return mActivity.getTalkClientService();
    }

    public void runOnUiThread(Runnable runnable) {
        mActivity.runOnUiThread(runnable);
    }

    @Override
    public void onAttach(Activity activity) {
        LOG.info("onAttach()");
        super.onAttach(activity);

        if(activity instanceof TalkActivity) {
            mActivity = (TalkActivity)activity;
        } else {
            throw new RuntimeException("Talk fragments need to be in a talk activity");
        }

        mActivity.registerTalkFragment(this);
    }

    @Override
    public void onDetach() {
        LOG.info("onDetach()");
        super.onDetach();

        if(mActivity != null) {
            mActivity.unregisterTalkFragment(this);
            mActivity = null;
        }
    }

    public void onServiceConnected() {
    }

    public void onServiceDisconnected() {
    }

    public void onAttachmentSelected(ContentObject contentObject) {
    }

    public void onAvatarSelected(ContentObject contentObject) {
    }

    @Override
    public void onClientStateChanged(int state) {
    }

    @Override
    public void onContactAdded(int contactId) {
    }

    @Override
    public void onContactRemoved(int contactId) {
    }

    @Override
    public void onTokenPairingFailed(String token) {
    }

    @Override
    public void onTokenPairingSucceeded(String token) {
    }

    @Override
    public void onClientPresenceChanged(int contactId) {
    }

    @Override
    public void onClientRelationshipChanged(int contactId) {
    }

    @Override
    public void onGroupCreationSucceeded(int contactId) {
    }

    @Override
    public void onGroupCreationFailed() {
    }

    @Override
    public void onGroupPresenceChanged(int contactId) {
    }

    @Override
    public void onGroupMembershipChanged(int contactId) {
    }

    @Override
    public void onMessageAdded(int contactId, int messageId) throws RemoteException {
    }

    @Override
    public void onMessageRemoved(int contactId, int messageId) throws RemoteException {
    }

    @Override
    public void onMessageStateChanged(int contactId, int messageId) throws RemoteException {
    }

    @Override
    public void onUploadAdded(int contactId, int downloadId) throws RemoteException {
    }

    @Override
    public void onUploadRemoved(int contactId, int downloadId) throws RemoteException {
    }

    @Override
    public void onUploadProgress(int contactId, int downloadId) throws RemoteException {
    }

    @Override
    public void onUploadStateChanged(int contactId, int downloadId, String state) throws RemoteException {
    }

    @Override
    public void onDownloadAdded(int contactId, int downloadId) throws RemoteException {
    }

    @Override
    public void onDownloadRemoved(int contactId, int downloadId) throws RemoteException {
    }

    @Override
    public void onDownloadProgress(int contactId, int downloadId) throws RemoteException {
    }

    @Override
    public void onDownloadStateChanged(int contactId, int downloadId, String state) throws RemoteException {
    }
}

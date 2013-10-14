package com.hoccer.talk.android;

import android.app.Activity;
import android.os.IBinder;
import android.os.RemoteException;
import com.actionbarsherlock.app.SherlockFragment;
import com.hoccer.talk.android.content.ContentObject;
import com.hoccer.talk.android.service.IXoClientService;
import com.hoccer.talk.client.TalkClientDatabase;
import org.apache.log4j.Logger;

import java.io.File;

/**
 * Base class for fragments working with the talk client
 *
 * This encapsulated commonalities:
 *  - access to activity for db and services
 */
public class XoFragment extends SherlockFragment implements IXoFragment {

    protected Logger LOG = null;

    private XoActivity mActivity;

    public XoFragment() {
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

    public XoActivity getXoActivity() {
        return mActivity;
    }

    public TalkClientDatabase getXoDatabase() {
        return mActivity.getXoDatabase();
    }

    public IXoClientService getXoService() {
        return mActivity.getXoService();
    }

    public void runOnUiThread(Runnable runnable) {
        mActivity.runOnUiThread(runnable);
    }

    @Override
    public void onAttach(Activity activity) {
        LOG.debug("onAttach()");
        super.onAttach(activity);

        if(activity instanceof XoActivity) {
            mActivity = (XoActivity)activity;
        } else {
            throw new RuntimeException("talk fragments need to be in a talk activity");
        }

        mActivity.registerTalkFragment(this);
    }

    @Override
    public void onDetach() {
        LOG.debug("onDetach()");
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

    @Override
    public void onSmsTokensChanged() throws RemoteException {
    }

}

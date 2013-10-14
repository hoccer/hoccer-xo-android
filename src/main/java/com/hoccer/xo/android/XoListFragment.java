package com.hoccer.xo.android;

import android.app.Activity;
import android.os.IBinder;
import android.os.RemoteException;
import com.actionbarsherlock.app.SherlockListFragment;
import com.hoccer.talk.client.TalkClientDatabase;
import com.hoccer.xo.android.content.ContentObject;
import com.hoccer.xo.android.service.IXoClientService;
import org.apache.log4j.Logger;

import java.io.File;

public abstract class XoListFragment extends SherlockListFragment implements IXoFragment {

    protected Logger LOG = null;

    private XoActivity mActivity;

    public XoListFragment() {
        LOG = Logger.getLogger(getClass());
    }

    /* just so we can implement the service listener interface */
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

    @Override
    public void onServiceConnected() {
    }

    @Override
    public void onServiceDisconnected() {
    }

    @Override
    public void onAvatarSelected(ContentObject co) {
    }

    @Override
    public void onAttachmentSelected(ContentObject co) {
    }

    @Override
    public void onClientStateChanged(int state) throws RemoteException {
    }

    @Override
    public void onTokenPairingFailed(String token) throws RemoteException {
    }

    @Override
    public void onTokenPairingSucceeded(String token) throws RemoteException {
    }

    @Override
    public void onContactAdded(int contactId) throws RemoteException {
    }

    @Override
    public void onContactRemoved(int contactId) throws RemoteException {
    }

    @Override
    public void onClientPresenceChanged(int contactId) throws RemoteException {
    }

    @Override
    public void onClientRelationshipChanged(int contactId) throws RemoteException {
    }

    @Override
    public void onGroupCreationSucceeded(int contactId) throws RemoteException {
    }

    @Override
    public void onGroupCreationFailed() throws RemoteException {
    }

    @Override
    public void onGroupPresenceChanged(int contactId) throws RemoteException {
    }

    @Override
    public void onGroupMembershipChanged(int contactId) throws RemoteException {
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

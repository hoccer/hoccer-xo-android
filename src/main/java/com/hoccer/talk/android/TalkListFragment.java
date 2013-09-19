package com.hoccer.talk.android;

import android.app.Activity;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.ListView;
import com.actionbarsherlock.app.SherlockListFragment;
import com.hoccer.talk.android.content.ContentObject;
import com.hoccer.talk.android.service.ITalkClientService;
import com.hoccer.talk.android.service.ITalkClientServiceListener;
import com.hoccer.talk.client.TalkClientDatabase;
import org.apache.log4j.Logger;

import java.io.File;

public class TalkListFragment extends SherlockListFragment implements ITalkFragment {

    protected Logger LOG = null;

    private TalkActivity mActivity;

    public TalkListFragment() {
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
        LOG.debug("onAttach()");
        super.onAttach(activity);

        if(activity instanceof TalkActivity) {
            mActivity = (TalkActivity)activity;
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

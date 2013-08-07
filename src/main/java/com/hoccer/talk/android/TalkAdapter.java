package com.hoccer.talk.android;

import android.os.IBinder;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import com.hoccer.talk.android.service.ITalkClientServiceListener;
import com.hoccer.talk.client.TalkClientDatabase;

import java.io.File;

public abstract class TalkAdapter extends BaseAdapter implements ITalkClientServiceListener {

    protected TalkActivity mActivity;
    protected TalkClientDatabase mDatabase;

    protected LayoutInflater mInflater;


    public TalkAdapter(TalkActivity activity) {
        mActivity = activity;
        mDatabase = mActivity.getTalkClientDatabase();
        mInflater = mActivity.getLayoutInflater();
    }

    public void register() {
        mActivity.registerListener(this);
    }

    public void unregister() {
        mActivity.unregisterListener(this);
    }

    public void runOnUiThread(Runnable runnable) {
        mActivity.runOnUiThread(runnable);
    }

    abstract public void reload();

    @Override
    public IBinder asBinder() {
        return null;
    }

    public File getAvatarDirectory() {
        return new File(mActivity.getFilesDir(), "avatars");
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

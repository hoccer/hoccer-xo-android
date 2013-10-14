package com.hoccer.xo.android;

import android.content.res.Resources;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import com.hoccer.talk.client.TalkClientDatabase;
import com.hoccer.xo.android.service.IXoClientServiceListener;

import java.io.File;

public abstract class XoAdapter extends BaseAdapter implements IXoClientServiceListener {

    protected XoActivity mActivity;
    protected TalkClientDatabase mDatabase;

    protected Resources mResources;
    protected LayoutInflater mInflater;


    public XoAdapter(XoActivity activity) {
        mActivity = activity;
        mDatabase = mActivity.getXoDatabase();
        mInflater = mActivity.getLayoutInflater();
        mResources = mActivity.getResources();
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

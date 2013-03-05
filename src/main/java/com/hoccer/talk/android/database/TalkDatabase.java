package com.hoccer.talk.android.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.hoccer.talk.client.HoccerTalkDatabase;
import com.hoccer.talk.logging.HoccerLoggers;
import com.hoccer.talk.model.TalkClient;
import com.hoccer.talk.model.TalkMessage;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;

public class TalkDatabase extends OrmLiteSqliteOpenHelper implements HoccerTalkDatabase {

    private static final Logger LOG = HoccerLoggers.getLogger(TalkDatabase.class);

    private static final String DATABASE_NAME    = "hoccer-talk.db";

    private static final int    DATABASE_VERSION = 1;

    Dao<TalkClient, String> mClientDao;
    Dao<TalkMessage, String> mMessageDao;

    public TalkDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource cs) {
        LOG.info("Creating database at schema version " + DATABASE_VERSION);
        try {
            TableUtils.createTable(cs, TalkClient.class);
            TableUtils.createTable(cs, TalkMessage.class);
        } catch (SQLException e) {
            e.printStackTrace();
            // XXX app must fail or something
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource cs, int oldVersion, int newVersion) {
        LOG.info("Upgrading database from schema version "
                + oldVersion + " to schema version " + newVersion);
    }

    private Dao<TalkClient, String> getClientDao() {
        if(mClientDao == null) {
            try {
                mClientDao = getDao(TalkClient.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return mClientDao;
    }

    private Dao<TalkMessage, String> getMessageDao() {
        if(mMessageDao == null) {
            try {
                mMessageDao = getDao(TalkMessage.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return mMessageDao;
    }

    @Override
    public TalkClient getClient() {
        return new TalkClient(UUID.randomUUID().toString());
    }

}
package com.hoccer.talk.android.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.hoccer.talk.client.ITalkClientDatabase;
import com.hoccer.talk.logging.HoccerLoggers;
import com.hoccer.talk.model.TalkClient;
import com.hoccer.talk.model.TalkDelivery;
import com.hoccer.talk.model.TalkMessage;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class TalkDatabase extends OrmLiteSqliteOpenHelper implements ITalkClientDatabase {

    private static final Logger LOG = HoccerLoggers.getLogger(TalkDatabase.class);

    private static final String DATABASE_NAME    = "hoccer-talk.db";

    private static final int    DATABASE_VERSION = 1;

    Dao<TalkClient, String> mClientDao;
    Dao<TalkMessage, String> mMessageDao;
    Dao<TalkDelivery, String> mDeliveryDao;

    public TalkDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource cs) {
        LOG.info("creating database at schema version " + DATABASE_VERSION);
        try {
            TableUtils.createTable(cs, TalkClient.class);
            TableUtils.createTable(cs, TalkMessage.class);
            TableUtils.createTable(cs, TalkDelivery.class);
        } catch (SQLException e) {
            e.printStackTrace();
            // XXX app must fail or something
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource cs, int oldVersion, int newVersion) {
        LOG.info("upgrading database from schema version "
                + oldVersion + " to schema version " + newVersion);
    }

    public Dao<TalkClient, String> getClientDao() {
        if(mClientDao == null) {
            try {
                mClientDao = getDao(TalkClient.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return mClientDao;
    }

    public Dao<TalkMessage, String> getMessageDao() {
        if(mMessageDao == null) {
            try {
                mMessageDao = getDao(TalkMessage.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return mMessageDao;
    }

    public Dao<TalkDelivery, String> getDeliveryDao() {
        if(mDeliveryDao == null) {
            try {
                mDeliveryDao = getDao(TalkDelivery.class);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return mDeliveryDao;
    }

    @Override
    public TalkClient getClient() {
        return new TalkClient(UUID.randomUUID().toString());
    }

    @Override
    public TalkMessage getMessageByTag(String messageTag) throws SQLException {
        List<TalkMessage> result = getMessageDao()
                .queryForEq(TalkMessage.FIELD_MESSAGE_TAG, messageTag);

        if(result.size() > 1) {
            logger.info("BUG: multiple messages with same tag " + messageTag);
        }

        if(result.size() == 0) {
            return null;
        }

        return result.get(0);
    }

    @Override
    public TalkDelivery[] getDeliveriesByTag(String messageTag) throws SQLException {
        List<TalkDelivery> result = getDeliveryDao()
                .queryForEq(TalkDelivery.FIELD_MESSAGE_TAG, messageTag);

        TalkDelivery[] ret = new TalkDelivery[result.size()];

        int i = 0;
        for(TalkDelivery d: result) {
            ret[i++] = d;
        }

        return ret;
    }

}

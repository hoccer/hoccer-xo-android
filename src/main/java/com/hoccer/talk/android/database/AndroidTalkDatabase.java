package com.hoccer.talk.android.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.hoccer.talk.client.ITalkClientDatabaseBackend;
import com.hoccer.talk.client.model.*;
import com.hoccer.talk.model.*;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.apache.log4j.Logger;

import java.sql.SQLException;

public class AndroidTalkDatabase extends OrmLiteSqliteOpenHelper implements ITalkClientDatabaseBackend {

    private static final Logger LOG = Logger.getLogger(AndroidTalkDatabase.class);

    private static final String DATABASE_NAME    = "hoccer-talk.db";

    private static final int    DATABASE_VERSION = 5;

    private static AndroidTalkDatabase INSTANCE = null;

    public static AndroidTalkDatabase getInstance(Context applicationContext) {
        if(INSTANCE == null) {
            INSTANCE = new AndroidTalkDatabase(applicationContext);
        }
        return INSTANCE;
    }

    private AndroidTalkDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource cs) {
        LOG.info("creating database at schema version " + DATABASE_VERSION);
        try {
            TableUtils.createTable(cs, TalkClientContact.class);
            TableUtils.createTable(cs, TalkClientSelf.class);
            TableUtils.createTable(cs, TalkPresence.class);
            TableUtils.createTable(cs, TalkRelationship.class);
            TableUtils.createTable(cs, TalkGroup.class);
            TableUtils.createTable(cs, TalkGroupMember.class);

            TableUtils.createTable(cs, TalkClientMembership.class);

            TableUtils.createTable(cs, TalkClientMessage.class);
            TableUtils.createTable(cs, TalkMessage.class);
            TableUtils.createTable(cs, TalkDelivery.class);

            TableUtils.createTable(cs, TalkKey.class);
            TableUtils.createTable(cs, TalkPrivateKey.class);

            TableUtils.createTable(cs, TalkClientDownload.class);
            TableUtils.createTable(cs, TalkClientUpload.class);
        } catch (SQLException e) {
            LOG.error("sql error creating database", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource cs, int oldVersion, int newVersion) {
        LOG.info("upgrading database from schema version "
                + oldVersion + " to schema version " + newVersion);
        try {
            if(oldVersion < 2) {
                TableUtils.createTable(cs, TalkGroup.class);
                TableUtils.createTable(cs, TalkGroupMember.class);
            }
            if(oldVersion < 3) {
                TableUtils.createTable(cs, TalkClientMembership.class);
            }
            if(oldVersion < 4) {
                TableUtils.createTable(cs, TalkKey.class);
                TableUtils.createTable(cs, TalkPrivateKey.class);
            }
            if(oldVersion < 5) {
                TableUtils.createTable(cs, TalkClientDownload.class);
                TableUtils.createTable(cs, TalkClientUpload.class);
            }
        } catch (SQLException e) {
            LOG.error("sql error upgrading database", e);
        }
    }

}

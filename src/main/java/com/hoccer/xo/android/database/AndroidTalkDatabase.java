package com.hoccer.xo.android.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import com.hoccer.talk.client.IXoClientDatabaseBackend;
import com.hoccer.talk.client.XoClientDatabase;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.talk.client.model.TalkClientMembership;
import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.talk.client.model.TalkClientSelf;
import com.hoccer.talk.client.model.TalkClientSmsToken;
import com.hoccer.talk.client.model.TalkClientUpload;
import com.hoccer.talk.model.*;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.apache.log4j.Logger;

import java.sql.SQLException;

public class AndroidTalkDatabase extends OrmLiteSqliteOpenHelper implements IXoClientDatabaseBackend {

    private static final Logger LOG = Logger.getLogger(AndroidTalkDatabase.class);

    //private static final String DATABASE_NAME = "hoccer-talk.db";
    private static String DATABASE_NAME = "hoccer-talk.db";

    private static final int DATABASE_VERSION = 13;

    private static AndroidTalkDatabase INSTANCE = null;

    public static AndroidTalkDatabase getInstance(Context applicationContext) {
        if (INSTANCE == null) {
            INSTANCE = new AndroidTalkDatabase(applicationContext);
        }
        return INSTANCE;
    }

    private AndroidTalkDatabase(Context context) {
        //super(context, DATABASE_NAME, null, DATABASE_VERSION);
        super(context, PreferenceManager.getDefaultSharedPreferences(context).getString("preference_database", "hoccer-talk.db"), null, DATABASE_VERSION);
        DATABASE_NAME = PreferenceManager.getDefaultSharedPreferences(context).getString("preference_database", "hoccer-talk.db");
    }

    @Override
    public <D extends Dao<T, ?>, T> D getDao(Class<T> clazz) throws SQLException {
        D dao = super.getDao(clazz);
        dao.setObjectCache(true);
        return dao;
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource cs) {
        LOG.info("creating database at schema version " + DATABASE_VERSION);
        try {
            XoClientDatabase.createTables(cs);
        } catch (SQLException e) {
            LOG.error("sql error creating database", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource cs, int oldVersion, int newVersion) {
        LOG.info("upgrading database from schema version "
                + oldVersion + " to schema version " + newVersion);
        try {
            if (oldVersion < 2) {
                TableUtils.createTable(cs, TalkGroup.class);
                TableUtils.createTable(cs, TalkGroupMember.class);
            }
            if (oldVersion < 3) {
                TableUtils.createTable(cs, TalkClientMembership.class);
            }
            if (oldVersion < 4) {
                TableUtils.createTable(cs, TalkKey.class);
                TableUtils.createTable(cs, TalkPrivateKey.class);
            }
            if (oldVersion < 5) {
                TableUtils.createTable(cs, TalkClientDownload.class);
                TableUtils.createTable(cs, TalkClientUpload.class);
            }
            if (oldVersion < 6) {
                TableUtils.createTable(cs, TalkClientSmsToken.class);
            }
            if (oldVersion < 7) {
                if (oldVersion >= 5) {
                    Dao<TalkClientDownload, Integer> downloads = getDao(TalkClientDownload.class);
                    downloads.executeRaw("ALTER TABLE `clientDownload` ADD COLUMN `contentUrl` VARCHAR;");
                    downloads.executeRaw("ALTER TABLE `clientDownload` ADD COLUMN `dataFile` VARCHAR;");
                    Dao<TalkClientUpload, Integer> uploads = getDao(TalkClientUpload.class);
                    uploads.executeRaw("ALTER TABLE `clientUpload` ADD COLUMN `contentUrl` VARCHAR;");
                }
                Dao<TalkClientMessage, Integer> messages = getDao(TalkClientMessage.class);
                messages.executeRaw("ALTER TABLE `clientMessage` ADD COLUMN `deleted` BOOLEAN;");
                Dao<TalkClientSelf, Integer> selfs = getDao(TalkClientSelf.class);
                selfs.executeRaw("ALTER TABLE `clientSelf` ADD COLUMN `registrationConfirmed` BOOLEAN;");
                selfs.executeRaw("UPDATE `clientSelf` SET `registrationConfirmed` = 1;");
            }
            if (oldVersion < 8) {
                Dao<TalkClientSelf, Integer> selfs = getDao(TalkClientSelf.class);
                selfs.executeRaw("ALTER TABLE `clientSelf` ADD COLUMN `registrationName` VARCHAR;");
            }
            if (oldVersion < 9) {
                Dao<TalkClientMessage, Integer> messages = getDao(TalkClientMessage.class);
                messages.executeRaw("ALTER TABLE `clientMessage` ADD COLUMN `inProgress` BOOLEAN;");
            }
            if (oldVersion < 10) {
                Dao<TalkClientDownload, Integer> downloads = getDao(TalkClientDownload.class);
                downloads.executeRaw("ALTER TABLE `clientDownload` ADD COLUMN `transferFailures` INTEGER;");
                TableUtils.createTableIfNotExists(cs, TalkAttachment.class);
            }
            if (oldVersion < 11) {
                Dao<TalkClientDownload, Integer> downloads = getDao(TalkClientDownload.class);
                downloads.executeRaw("ALTER TABLE `clientDownload` ADD COLUMN `contentHmac` VARCHAR;");
                Dao<TalkClientUpload, Integer> uploads = getDao(TalkClientUpload.class);
                uploads.executeRaw("ALTER TABLE `clientUpload` ADD COLUMN `contentHmac` VARCHAR;");
                uploads.executeRaw("ALTER TABLE `clientUpload` ADD COLUMN `transferFailures` INTEGER;");
                TableUtils.createTableIfNotExists(cs, TalkAttachment.class);
                Dao<TalkClientMessage, Integer> messages = getDao(TalkClientMessage.class);
                messages.executeRaw("ALTER TABLE `clientMessage` ADD COLUMN `signature` VARCHAR;");
                messages.executeRaw("ALTER TABLE `clientMessage` ADD COLUMN `hmac` VARCHAR;");
            }
            if (oldVersion < 12) {
                Dao<TalkClientDownload, Integer> downloads = getDao(TalkClientDownload.class);
                downloads.executeRaw("ALTER TABLE `clientDownload` ADD COLUMN `fileName` VARCHAR;");
                Dao<TalkClientUpload, Integer> uploads = getDao(TalkClientUpload.class);
                uploads.executeRaw("ALTER TABLE `clientUpload` ADD COLUMN `fileName` VARCHAR;");
            }
            if (oldVersion < 13) {
                Dao<TalkGroup, Integer> talkGroups = getDao(TalkGroup.class);
                talkGroups.executeRaw("ALTER TABLE `group` ADD COLUMN `keyDate` DATE");
                talkGroups.executeRaw("ALTER TABLE `group` ADD COLUMN `groupType` VARCHAR");
                talkGroups.executeRaw("ALTER TABLE `group` ADD COLUMN `sharedKeyId` VARCHAR");
                talkGroups.executeRaw("ALTER TABLE `group` ADD COLUMN `sharedKeyIdSalt` VARCHAR");
                talkGroups.executeRaw("ALTER TABLE `group` ADD COLUMN `keySupplier` VARCHAR");
                talkGroups.executeRaw("ALTER TABLE `group` ADD COLUMN `groupKeyUpdateInProgress` DATE");

                Dao<TalkGroupMember, Integer> talkGroupMembers = getDao(TalkGroupMember.class);
                talkGroupMembers.executeRaw("ALTER TABLE `groupMember` ADD COLUMN `keySupplier` VARCHAR");
                talkGroupMembers.executeRaw("ALTER TABLE `groupMember` ADD COLUMN `sharedKeyId` VARCHAR");
                talkGroupMembers.executeRaw("ALTER TABLE `groupMember` ADD COLUMN `sharedKeyIdSalt` VARCHAR");
                talkGroupMembers.executeRaw("ALTER TABLE `groupMember` ADD COLUMN `sharedKeyDate` DATE");

                Dao<TalkMessage, Integer> talkMessages = getDao(TalkMessage.class);
                talkMessages.executeRaw("ALTER TABLE `message` ADD COLUMN `sharedKeyId` VARCHAR");
                talkMessages.executeRaw("ALTER TABLE `message` ADD COLUMN `sharedKeyIdSalt` VARCHAR");
                talkMessages.executeRaw("ALTER TABLE `message` ADD COLUMN `hmac` VARCHAR");
                talkMessages.executeRaw("ALTER TABLE `message` ADD COLUMN `signature` VARCHAR");
                talkMessages.executeRaw("ALTER TABLE `message` ADD COLUMN `system` VARCHAR");

                Dao<TalkPrivateKey, Integer> talkPrivateKeys = getDao(TalkPrivateKey.class);
                talkPrivateKeys.executeRaw("ALTER TABLE `privateKey` ADD COLUMN `groupKeyId` VARCHAR");
                talkPrivateKeys.executeRaw("ALTER TABLE `privateKey` ADD COLUMN `groupKeyIdSalt` VARCHAR");

                Dao<TalkClientContact, Integer> talkClientContacts = getDao(TalkClientContact.class);
                talkClientContacts.executeRaw("ALTER TABLE `clientContact` ADD COLUMN `isNearby` BOOLEAN");
            }
        } catch (SQLException e) {
            LOG.error("sql error upgrading database", e);
        }
    }

}

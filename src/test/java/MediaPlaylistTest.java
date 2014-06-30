import com.hoccer.talk.client.model.TalkClientDownload;
import com.hoccer.xo.android.content.AudioAttachmentItem;
import com.hoccer.xo.android.content.audio.MediaPlaylist;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

public class MediaPlaylistTest {

    private static final Logger LOG = Logger.getLogger(MediaPlaylistTest.class);

    private MediaPlaylist mp;
    List<AudioAttachmentItem> items = new ArrayList<AudioAttachmentItem>();
    private String databaseUrl = "jdbc:h2:mem:account";

    @Before
    public void testSetup() throws Exception {

        JdbcConnectionSource connectionSource = new JdbcConnectionSource(databaseUrl);
        Dao<TalkClientDownload, Integer> clientDownloadsDao = DaoManager.createDao(connectionSource, TalkClientDownload.class);
        TableUtils.createTable(connectionSource, TalkClientDownload.class);

        mp = new MediaPlaylist();
        items = new ArrayList<AudioAttachmentItem>();
        for (int i = 0; i < 4; i++) {
            TalkClientDownload tcd = new TalkClientDownload();
            clientDownloadsDao.create(tcd);
            AudioAttachmentItem item = AudioAttachmentItem.create("test_dummy_path", tcd, false);
            items.add(item);
        }
        mp.setTrackList(items);

        connectionSource.close();
    }

    @After
    public void testCleanup() throws SQLException {
        mp = null;

        JdbcConnectionSource connectionSource = new JdbcConnectionSource(databaseUrl);
        TableUtils.dropTable(connectionSource, TalkClientDownload.class, true);
        connectionSource.close();
    }

    @Test
    public void testRemoveAfterCurrent() {

        mp.setCurrentIndex(1);
        mp.remove(2);

        List<Integer> expectedOrder = new ArrayList<Integer>() {
            {
                add(0);
                add(1);
                add(2);
            }
        };

        items.remove(2);

        assertEquals(items, mp.getAudioAttachmentItems());
        assertEquals(1, mp.getCurrentIndex());
        assertEquals(expectedOrder, mp.getPlaylistOrder());
    }

    @Test
    public void testRemoveBeforeCurrent() {

        mp.setCurrentIndex(2);
        mp.remove(1);

        List<Integer> expectedOrder = new ArrayList<Integer>() {
            {
                add(0);
                add(1);
                add(2);
            }
        };

        items.remove(1);

        assertEquals(items, mp.getAudioAttachmentItems());
        assertEquals(1, mp.getCurrentIndex());
        assertEquals(expectedOrder, mp.getPlaylistOrder());
    }

    @Test(expected = IllegalStateException.class)
    public void testRemoveCurrent() {

        mp.setCurrentIndex(0);
        mp.remove(0);
    }

    @Test
    public void testRemoveBeforeCurrentFromShuffledList() {

        mp.setShuffleActive(true);
        mp.setPlaylistOrder(new ArrayList<Integer>() {
            {
                add(3);
                add(2);
                add(0);
                add(1);
            }
        });
        mp.setCurrentIndex(2);
        mp.remove(2);

        List<Integer> expectedOrder = new ArrayList<Integer>() {
            {
                add(2);
                add(0);
                add(1);
            }
        };

        items.remove(2);

        assertEquals(items, mp.getAudioAttachmentItems());
        assertEquals(1, mp.getCurrentIndex());
        assertEquals(expectedOrder, mp.getPlaylistOrder());
    }

    @Test
    public void testRemoveAfterCurrentFromShuffledList() {

        mp.setShuffleActive(true);
        mp.setPlaylistOrder(new ArrayList<Integer>() {
            {
                add(3);
                add(2);
                add(0);
                add(1);
            }
        });
        mp.setCurrentIndex(0);
        mp.remove(0);

        List<Integer> expectedOrder = new ArrayList<Integer>() {
            {
                add(2);
                add(1);
                add(0);
            }
        };

        items.remove(0);

        assertEquals(items, mp.getAudioAttachmentItems());
        assertEquals(0, mp.getCurrentIndex());
        assertEquals(expectedOrder, mp.getPlaylistOrder());
    }

    @Test(expected = IllegalStateException.class)
    public void testRemoveCurrentFromShuffledList() {

        mp.setShuffleActive(true);
        mp.setPlaylistOrder(new ArrayList<Integer>() {
            {
                add(3);
                add(2);
                add(0);
                add(1);
            }
        });
        mp.setCurrentIndex(2);
        mp.remove(0);
    }
}

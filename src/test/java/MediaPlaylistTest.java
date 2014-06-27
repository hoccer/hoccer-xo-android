import com.hoccer.xo.android.content.AudioAttachmentItem;
import com.hoccer.xo.android.content.audio.MediaPlaylist;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

public class MediaPlaylistTest {

    private static final Logger LOG = Logger.getLogger(MediaPlaylistTest.class);

    private MediaPlaylist mp;

    @Before
    public void testSetup() {
        mp = new MediaPlaylist();
        List<AudioAttachmentItem> items = new ArrayList<AudioAttachmentItem>();
        for (int i = 0; i < 4; i++) {
            AudioAttachmentItem item = new AudioAttachmentItem();
            item.setFilePath("attachment-" + i);
            items.add(item);
        }
        mp.setTrackList(items);
    }

    @After
    public void testCleanup() {
        mp = null;
    }

    @Test
    public void testRemoveAfterCurrent() {

        mp.setCurrentIndex(1);
        mp.remove(2);

        List<Integer> expected = new ArrayList<Integer>() {
            {
                add(0);
                add(1);
                add(2);
            }
        };

        assertEquals(1, mp.getCurrentIndex());
        assertEquals(expected, mp.getPlaylistOrder());
    }

    @Test
    public void testRemoveBeforeCurrent() {

        mp.setCurrentIndex(2);
        mp.remove(1);

        List<Integer> expected = new ArrayList<Integer>() {
            {
                add(0);
                add(1);
                add(2);
            }
        };

        assertEquals(1, mp.getCurrentIndex());
        assertEquals(expected, mp.getPlaylistOrder());
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

        List<Integer> expected = new ArrayList<Integer>() {
            {
                add(2);
                add(0);
                add(1);
            }
        };

        assertEquals(1, mp.getCurrentIndex());
        assertEquals(expected, mp.getPlaylistOrder());
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

        List<Integer> expected = new ArrayList<Integer>() {
            {
                add(2);
                add(1);
                add(0);
            }
        };

        assertEquals(0, mp.getCurrentIndex());
        assertEquals(expected, mp.getPlaylistOrder());
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

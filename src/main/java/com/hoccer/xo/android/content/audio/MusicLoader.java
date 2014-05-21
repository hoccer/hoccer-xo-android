package com.hoccer.xo.android.content.audio;

/**
 * Created by alexw on 06.05.14.
 */

import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;

public class MusicLoader {
        private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();

    private String mPath;

    public MusicLoader(String mediaPath){

        mPath = mediaPath;
    }

    public ArrayList<HashMap<String, String>> getPlayList(){

        try {
            File home = new File(mPath);

            if (home != null) {
                if (home.listFiles(new FileExtensionFilter()).length > 0) {
                    for (File file : home.listFiles(new FileExtensionFilter())) {
                        HashMap<String, String> song = new HashMap<String, String>();
                        song.put("songTitle", file.getName().substring(0, (file.getName().length() - 4)));
                        song.put("songPath", file.getPath());

                        // Adding each song to SongList
                        songsList.add(song);
                    }
                }
            } else {
                Log.e(MusicLoader.class.toString(), "Error loading songs in " + mPath);
            }
        }
        catch(Exception e){
            Log.e(MusicLoader.class.toString(), "getPlaylist mismatch: " + e);
        }

        // return songs list array
        return songsList;
    }

    /**
     * Class to filter files which are having .mp3 extension
     * */
    class FileExtensionFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return (name.endsWith(".mp3")
                    || name.endsWith(".MP3")
                    || name.endsWith(".MPGA")
                    || name.endsWith(".mpga")
                    || name.endsWith(".wma")
                    || name.endsWith(".oga")
                    || name.endsWith(".ogg"));
        }
    }
}

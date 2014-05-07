package com.hoccer.xo.android.activity;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import com.hoccer.xo.android.content.audio.MediaPlayerService;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

public class FullscreenPlayerActivity extends Activity implements OnCompletionListener, SeekBar.OnSeekBarChangeListener {

    private ImageButton btnPlay;
    private ImageButton btnForward;
    private ImageButton btnBackward;
    private ImageButton btnNext;
    private ImageButton btnPrevious;
    private ImageButton btnPlaylist;
    private ImageButton btnRepeat;
    private ImageButton btnShuffle;
    private SeekBar songProgressBar;
    private TextView songTitleLabel;
    private TextView songCurrentDurationLabel;
    private TextView songTotalDurationLabel;
    // Media Player
    private MediaPlayerService mMediaPlayerService;
    // Handler to update UI timer, progress bar etc,.
    private Handler mHandler = new Handler();

    private String mTempFilePath;
//    private MusicViewerActivity songManager;
//    private Utilities utils;
    private int seekForwardTime = 5000; // 5000 milliseconds
    private int seekBackwardTime = 5000; // 5000 milliseconds
    private int currentSongIndex = 0;
    private boolean isShuffle = false;
    private boolean isRepeat = false;
    private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();

    private final static Logger LOG = Logger.getLogger(FullscreenPlayerActivity.class);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_fullscreen_player);

        Intent intent = new Intent(this, MediaPlayerService.class);
        startService(intent);
        bindService(intent);

        LOG.debug("------------------------- onCreate");

        // All player buttons
        btnPlay = (ImageButton) findViewById(R.id.btnPlay);
        btnForward = (ImageButton) findViewById(R.id.btnForward);
        btnBackward = (ImageButton) findViewById(R.id.btnBackward);
//        btnNext = (ImageButton) findViewById(R.id.btnNext);
        btnPrevious = (ImageButton) findViewById(R.id.btnPrevious);
//        btnPlaylist = (ImageButton) findViewById(R.id.btnPlaylist);
//        btnRepeat = (ImageButton) findViewById(R.id.btnRepeat);
//        btnShuffle = (ImageButton) findViewById(R.id.btnShuffle);
        songProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
        songTitleLabel = (TextView) findViewById(R.id.songTitle);
//        songCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);
//        songTotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);

        // Mediaplayer
//        mAudioPlayer = MediaPlayerService.get(this);


//        songManager = new SongsManager();
//        utils = new Utilities();

        // Listeners
        songProgressBar.setOnSeekBarChangeListener(this); // Important
//        mp.setOnCompletionListener(this); // Important

        // Getting all songs list
//        songsList = songManager.getPlayList();

        // By default play first song
//        playSong(0);

        /**
         * Play button click event
         * plays a song and changes button to pause image
         * pauses a song and changes button to play image
         * */
        btnPlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                LOG.error("------------------------- Play onClick");
                // check for already playing
                if(!mMediaPlayerService.isPaused() && !mMediaPlayerService.isStopped()){
                    mMediaPlayerService.pause();
                        // Changing button image to play button
                        btnPlay.setImageResource(R.drawable.ic_dark_play);
                }else{
                    // Resume song
                    mMediaPlayerService.start(mTempFilePath);
                        // Changing button image to pause button
                        btnPlay.setImageResource(R.drawable.ic_dark_pause);
                }

            }
        });

        /**
         * Forward button click event
         * Forwards song specified seconds
         * */
//        btnForward.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View arg0) {
//
//                LOG.error("------------------------- Forward onClick");
////                // get current song position
////                int currentPosition = mp.getCurrentPosition();
////                // check if seekForward time is lesser than song duration
////                if(currentPosition + seekForwardTime <= mp.getDuration()){
////                    // forward song
////                    mp.seekTo(currentPosition + seekForwardTime);
////                }else{
////                    // forward to end position
////                    mp.seekTo(mp.getDuration());
////                }
//            }
//        });

        /**
         * Backward button click event
         * Backward song to specified seconds
         * */
//        btnBackward.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View arg0) {
//                LOG.error("------------------------- Back onClick");
////
////                // get current song position
////                int currentPosition = mp.getCurrentPosition();
////                // check if seekBackward time is greater than 0 sec
////                if(currentPosition - seekBackwardTime >= 0){
////                    // forward song
////                    mp.seekTo(currentPosition - seekBackwardTime);
////                }else{
////                    // backward to starting position
////                    mp.seekTo(0);
////                }
//
//            }
//        });

        /**
         * Next button click event
         * Plays next song by taking currentSongIndex + 1
         * */
//        btnNext.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View arg0) {
//
//                LOG.error("------------------------- Next onClick");
//                // check if next song is there or not
////                if(currentSongIndex < (songsList.size() - 1)){
////                    playSong(currentSongIndex + 1);
////                    currentSongIndex = currentSongIndex + 1;
////                }else{
////                    // play first song
////                    playSong(0);
////                    currentSongIndex = 0;
////                }
//
//            }
//        });

        /**
         * Back button click event
         * Plays previous song by currentSongIndex - 1
         * */
//        btnPrevious.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View arg0) {
//                LOG.error("------------------------- Prev onClick");
////                if(currentSongIndex > 0){
////                    playSong(currentSongIndex - 1);
////                    currentSongIndex = currentSongIndex - 1;
////                }else{
////                    // play last song
////                    playSong(songsList.size() - 1);
////                    currentSongIndex = songsList.size() - 1;
////                }
//
//            }
//        });

        /**
         * Button Click event for Repeat button
         * Enables repeat flag to true
         * */
//        btnRepeat.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View arg0) {
//
//                LOG.error("------------------------- Repeat onClick");
////                if(isRepeat){
////                    isRepeat = false;
////                    Toast.makeText(getApplicationContext(), "Repeat is OFF", Toast.LENGTH_SHORT).show();
////                    btnRepeat.setImageResource(R.drawable.btn_repeat);
////                }else{
////                    // make repeat to true
////                    isRepeat = true;
////                    Toast.makeText(getApplicationContext(), "Repeat is ON", Toast.LENGTH_SHORT).show();
////                    // make shuffle to false
////                    isShuffle = false;
////                    btnRepeat.setImageResource(R.drawable.btn_repeat_focused);
////                    btnShuffle.setImageResource(R.drawable.btn_shuffle);
////                }
//            }
//        });

        /**
         * Button Click event for Shuffle button
         * Enables shuffle flag to true
         * */
//        btnShuffle.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View arg0) {
//
//                LOG.error("------------------------- Shuffle onClick");
////                if(isShuffle){
////                    isShuffle = false;
////                    Toast.makeText(getApplicationContext(), "Shuffle is OFF", Toast.LENGTH_SHORT).show();
////                    btnShuffle.setImageResource(R.drawable.btn_shuffle);
////                }else{
////                    // make repeat to true
////                    isShuffle= true;
////                    Toast.makeText(getApplicationContext(), "Shuffle is ON", Toast.LENGTH_SHORT).show();
////                    // make shuffle to false
////                    isRepeat = false;
////                    btnShuffle.setImageResource(R.drawable.btn_shuffle_focused);
////                    btnRepeat.setImageResource(R.drawable.btn_repeat);
////                }
//            }
//        });

        /**
         * Button Click event for Play list click event
         * Launches list activity which displays list of songs
         * */
//        btnPlaylist.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View arg0) {
//                Intent i = new Intent(getApplicationContext(), PlayListActivity.class);
//                startActivityForResult(i, 100);
//            }
//        });

    }

    /**
     * Receiving song index from playlist view
     * and play the song
     * */
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 100){
            currentSongIndex = data.getExtras().getInt("songIndex");
            // play selected song
//            playSong(currentSongIndex);
        }
    //TODO: this method is not needed, but we could use it if we had to send some data
    }

    /**
     * Function to play a song
     * @param songIndex - index of song
     * */
//    public void  playSong(int songIndex){
//        // Play song
//        try {
//            mp.reset();
//            mp.setDataSource(songsList.get(songIndex).get("songPath"));
//            mp.prepare();
//            mp.start();
//            // Displaying Song title
//            String songTitle = songsList.get(songIndex).get("songTitle");
//            songTitleLabel.setText(songTitle);
//
//            // Changing Button Image to pause image
//            btnPlay.setImageResource(R.drawable.btn_pause);
//
//            // set Progress bar values
//            songProgressBar.setProgress(0);
//            songProgressBar.setMax(100);
//
//            // Updating progress bar
//            updateProgressBar();
//        } catch (IllegalArgumentException e) {
//            e.printStackTrace();
//        } catch (IllegalStateException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * Update timer on seekbar
     * */
    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    /**
     * Background Runnable thread
     * */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {

            LOG.error("------------------------- run");
//            long totalDuration = mp.getDuration();
//            long currentDuration = mp.getCurrentPosition();
//
//            // Displaying Total Duration time
//            songTotalDurationLabel.setText(""+utils.milliSecondsToTimer(totalDuration));
//            // Displaying time completed playing
//            songCurrentDurationLabel.setText(""+utils.milliSecondsToTimer(currentDuration));
//
//            // Updating progress bar
//            int progress = (int)(utils.getProgressPercentage(currentDuration, totalDuration));
//            //Log.d("Progress", ""+progress);
//            songProgressBar.setProgress(progress);
//
//            // Running this thread after 100 milliseconds
//            mHandler.postDelayed(this, 100);
        }
    };

    /**
     *
     * */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {

    }

    /**
     * When user starts moving the progress handler
     * */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    /**
     * When user stops moving the progress hanlder
     * */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
//        int totalDuration = mp.getDuration();
//        int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);
//
//        // forward or backward to certain seconds
//        mp.seekTo(currentPosition);

        // update timer progress again
        updateProgressBar();
    }

    /**
     * On Song Playing completed
     * if repeat is ON play same song again
     * if shuffle is ON play random song
     * */
    @Override
    public void onCompletion(MediaPlayer arg0) {

//        // check for repeat is ON or OFF
//        if(isRepeat){
//            // repeat is on play same song again
//            playSong(currentSongIndex);
//        } else if(isShuffle){
//            // shuffle is on - play a random song
//            Random rand = new Random();
//            currentSongIndex = rand.nextInt((songsList.size() - 1) - 0 + 1) + 0;
//            playSong(currentSongIndex);
//        } else{
//            // no repeat or shuffle ON - play next song
//            if(currentSongIndex < (songsList.size() - 1)){
//                playSong(currentSongIndex + 1);
//                currentSongIndex = currentSongIndex + 1;
//            }else{
//                // play first song
//                playSong(0);
//                currentSongIndex = 0;
//            }
//        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
//        mp.release();
    }

    private void bindService(Intent intent){

        ServiceConnection connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MediaPlayerService.MediaPlayerBinder binder = (MediaPlayerService.MediaPlayerBinder) service;
                mMediaPlayerService = binder.getService();

                mTempFilePath = mMediaPlayerService.getCurrentMediaFilePath();

                songTitleLabel.setText(mTempFilePath);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mMediaPlayerService = null;
            }
        };

        bindService(intent, connection, Context.BIND_AUTO_CREATE);

//        mPlayPauseButton = (ImageButton) findViewById(R.id.audio_play);
//        mPlayPauseButton.setOnClickListener(this);
    }

}
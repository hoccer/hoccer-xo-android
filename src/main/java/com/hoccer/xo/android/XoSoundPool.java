package com.hoccer.xo.android;


import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import com.hoccer.xo.test.R;

/**
 * This class manages all internal sounds of the application.
 */
public class XoSoundPool {

    SoundPool mSoundPool;

    private final int mThrowSoundId;
    private final int mCatchSoundId;

    private final Context mContext;

    public XoSoundPool(Context pContext) {
        mContext = pContext;

        mSoundPool = new SoundPool(1, AudioManager.STREAM_RING, 0);
        mThrowSoundId = mSoundPool.load(mContext, R.raw.throw_sound, 1);
        mCatchSoundId = mSoundPool.load(mContext, R.raw.catch_sound, 1);
    }

    @Override
    protected void finalize() throws Throwable {
        // mSoundPool.release();
        super.finalize();
    }

    public void playThrowSound() {
        playSound(mThrowSoundId);
    }

    public void playCatchSound() {
        playSound(mCatchSoundId);
    }

    /**
     * Plays a sound defined by a given resource id.
     *
     * @param pId The resource id of the sound to play
     */
    private void playSound(int pId) {
        float volume = getCurrentRingtoneVolume();
        mSoundPool.play(pId, volume, volume, 1, 0, 1f);
    }

    private float getCurrentRingtoneVolume() {
        AudioManager mgr = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        return mgr.getStreamVolume(AudioManager.STREAM_RING);
    }
}

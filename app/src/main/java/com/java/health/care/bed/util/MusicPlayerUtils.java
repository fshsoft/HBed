package com.java.health.care.bed.util;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;


/**
 * 音乐播放工具类
 */
public class MusicPlayerUtils {
    private final int MSG_PROGRESS = 100;
    private static MusicPlayerUtils instance;
    private MediaPlayer mMediaPlayer;
    private Handler mHandler;
    private OnProgressListener mListener;
    private int mDuration;

    public static MusicPlayerUtils get() {
        if (instance == null) {
            instance = new MusicPlayerUtils();
        }
        return instance;
    }

    private MusicPlayerUtils() {
        mMediaPlayer = new MediaPlayer();
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (mListener != null) {
                    mListener.onProgress(getCurrentPosition(), getCurrentPosition() * 100.0 / mDuration);
                }
                mHandler.sendEmptyMessageDelayed(MSG_PROGRESS, 300);
            }
        };
    }

    public void setListener(OnProgressListener mListener) {
        this.mListener = mListener;
    }

    public void play(String path) {
        play(path, false);
    }

    public void play(final String soundFilePath, boolean looping) {

        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(soundFilePath);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setLooping(looping);
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mDuration = mMediaPlayer.getDuration();
                    mMediaPlayer.start();
                    mHandler.sendEmptyMessage(MSG_PROGRESS);
                }
            });
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (mListener != null) {
                        mListener.onProgress(mDuration, 100);
                    }
                    mHandler.removeCallbacksAndMessages(null);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void play() {
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            try {
                mMediaPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mHandler.sendEmptyMessage(MSG_PROGRESS);
        }
    }

    public void seekTo(int progress) {
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.seekTo(progress);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void pause() {
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.pause();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mHandler.removeCallbacksAndMessages(null);
    }

    public void stop() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            try {
                mMediaPlayer.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mHandler.removeCallbacksAndMessages(null);
    }

    public int getCurrentPosition() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getCurrentPosition();
        } else {
            return 0;
        }
    }

    public int getDuration() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getDuration();
        } else {
            return 0;
        }
    }

    public boolean isPlaying() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.isPlaying();
        } else {
            return false;
        }
    }

    public void setPlayOnCompleteListener(MediaPlayer.OnCompletionListener playOnCompleteListener) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setOnCompletionListener(playOnCompleteListener);
        }
    }

    public void release() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (mListener != null) {
            mListener = null;
        }
    }

    public static int getDuration(String url) {
        MediaPlayer mediaPlayer = null;
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
            return mediaPlayer.getDuration();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
        }
        return 0;
    }

    public interface OnProgressListener {
        void onProgress(int currentTime, double percent);
    }
}

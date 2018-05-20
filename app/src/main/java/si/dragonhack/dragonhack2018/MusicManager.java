package si.dragonhack.dragonhack2018;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Andrej Hafner on 20. 05. 2018.
 */
public class MusicManager implements OnStepDetected {
    private static MusicManager mInstance;
    private final int UPDATE_BPM_INTERVAL = 5000;
    private final int MUSIC_CHANGE_THRESHOLD = 5; // +- 5 bpm to change song
    private final int CHANGE_AFTER_TIME = 30; // time after which the changing of the song starts working

    public enum MusicEvents
    {
        STARTED,
        PAUSED,
        STOPPED,
        SONG_CHANGED,
        SONG_SPEED_CHANGED
    }

    public enum ChangeSongMode
    {
        CHANGE_SONG,
        CHANGE_BPM
    }

    public enum PlaySongMode
    {
        FOLLOW_USER,
        MOTIVATE_USER
    }


    private List<OnMusicDecisionChanged> listeners = new ArrayList<>();

    private PlaySongMode playSongMode = PlaySongMode.FOLLOW_USER;
    private ChangeSongMode changeSongMode = ChangeSongMode.CHANGE_SONG;

    private int stepCount = 0;
    private int bpm = 100;
    private int songStartedBpm = 0;
    private boolean isPlaying;
    private long secondsLasted;
    private long secondsStarted;
    private boolean measuring = false;
    private final Handler handler = new Handler();
    Timer timer = new Timer(false);


    public static MusicManager getInstance()
    {
        if(mInstance == null)
        {
            mInstance = new MusicManager();
            return mInstance;
        }
        return mInstance;
    }

    public void addListener(OnMusicDecisionChanged listener)
    {
        this.listeners.add(listener);
    }

    private MusicManager()
    {
        StepDetector.getInstance().addListener(this);
    }

    void startSong()
    {
        isPlaying = true;
        // TODO set to current song bpm
        Uri uri = GetSongs.songs.get(0).getPathUri();
        String path = uri.getPath();
        audioPlayer("/storage/emulated/0/Samsung/Music/Over_the_Horizon.mp3","");
        publishSongChangedhanged(MusicEvents.STARTED);
    }

    void pauseSong()
    {
        isPlaying = false;

        publishSongChangedhanged(MusicEvents.PAUSED);

    }

    void stopSong()
    {
        isPlaying = false;

        publishSongChangedhanged(MusicEvents.STOPPED);

    }

    void changeSong()
    {
        // TODO set to current song bpm

        publishSongChangedhanged(MusicEvents.SONG_CHANGED);

    }


    void changeSongMetrics()
    {
        if(changeSongMode == ChangeSongMode.CHANGE_SONG && Math.abs(songStartedBpm - bpm) > MUSIC_CHANGE_THRESHOLD)
        {
            // todo FIND A new song with a required bpm and change to it
            publishSongChangedhanged(MusicEvents.SONG_SPEED_CHANGED);
        } else if (changeSongMode == ChangeSongMode.CHANGE_BPM && Math.abs(songStartedBpm - bpm) > MUSIC_CHANGE_THRESHOLD)
        {
            // change bpm
            publishSongChangedhanged(MusicEvents.SONG_SPEED_CHANGED);

        }


        if(playSongMode == PlaySongMode.FOLLOW_USER && Math.abs(songStartedBpm - bpm) > MUSIC_CHANGE_THRESHOLD)
        {
            publishSongChangedhanged(MusicEvents.SONG_SPEED_CHANGED);

        }
        else if (playSongMode == PlaySongMode.MOTIVATE_USER && Math.abs(songStartedBpm - bpm) > MUSIC_CHANGE_THRESHOLD)
        {
            publishSongChangedhanged(MusicEvents.SONG_SPEED_CHANGED);

        }

}

    public void startMeasuring()
    {
        measuring = true;
        secondsLasted = 0;
        stepCount = 0;
        secondsStarted = System.currentTimeMillis();
//        timer.cancel();
//        timer.purge();
        timer.scheduleAtFixedRate(timerTask, UPDATE_BPM_INTERVAL, UPDATE_BPM_INTERVAL);

    }

    public void stopMeasuring()
    {
        measuring = false;
        timer.cancel();
        timer.purge();
    }

    public void setChangeSongMode(ChangeSongMode mode)
    {
        this.changeSongMode = mode;
    }

    public void setPlaySongMode(PlaySongMode mode)
    {
        this.playSongMode = mode;
    }

    public void audioPlayer(String path, String fileName){
        //set up MediaPlayer
        MediaPlayer mp = new MediaPlayer();

        try {
//            mp.setDataSource(path + File.separator + fileName);
            mp.setDataSource(path);

            mp.prepare();
            mp.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void calculateBpm()
    {
        if(secondsLasted > 0)
        {
            bpm = (int)((float) stepCount / (((float) secondsLasted) / 60f));
            publishBpmChanged();

            if(secondsLasted > CHANGE_AFTER_TIME)
            {
                changeSongMetrics();
            }

        }

    }

    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    calculateBpm();
                }
            });
        }
    };

    @Override
    public void onStep(int arg) {
        if(measuring)
        {
            stepCount++;
            secondsLasted = (System.currentTimeMillis() - secondsStarted) / 1000;
        }

    }

    private void publishBpmChanged()
    {
        for (OnMusicDecisionChanged listener: listeners) {
            listener.onBpmChanged(bpm);
        }
    }

    private void publishSongChangedhanged(MusicEvents event)
    {
        for (OnMusicDecisionChanged listener: listeners) {
            listener.onMusicChanged(event);
        }

    }
}

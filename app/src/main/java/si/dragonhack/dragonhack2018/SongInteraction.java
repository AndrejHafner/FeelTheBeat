package si.dragonhack.dragonhack2018;

import android.media.MediaPlayer;
import android.os.CountDownTimer;

/**
 * Created by Jaka on 20. 05. 2018.
 */

public class SongInteraction {
    /**
     *
     * @param mp media player that is playing the song
     * @param songBpm original bpm of the song, MUST NOT BE 0!
     * @param newBpm new bpm of the song, MUST NOT BE 0!
     */
    public static void changeSpeed(final MediaPlayer mp, final int songBpm, int newBpm) {
        newBpm = newBpm > 0 ? newBpm : songBpm;
        final int currentBpm = Math.round(mp.getPlaybackParams().getSpeed() * songBpm);
        final int bpmStep = Math.round((currentBpm - newBpm) / 15);

        final float finalFactor = newBpm / songBpm;
        new CountDownTimer(3000, 200)
        {
            int songBeat = currentBpm + bpmStep;
            public void onTick(long millisUntilFinished)
            {
                // do something every 5 seconds...
                float modFactor = songBpm / songBeat;
                mp.setPlaybackParams(mp.getPlaybackParams().setSpeed(modFactor));
                songBeat += bpmStep;
            }

            public void onFinish()
            {
                // finish off when we're all dead !
                mp.setPlaybackParams(mp.getPlaybackParams().setSpeed(finalFactor));
            }
        }.start();
    }
}

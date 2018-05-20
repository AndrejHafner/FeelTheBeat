package si.dragonhack.dragonhack2018;

/**
 * Created by Andrej Hafner on 20. 05. 2018.
 */
public interface OnMusicDecisionChanged {
    void onMusicChanged(MusicManager.MusicEvents event);
    void onBpmChanged(int bpm);
}

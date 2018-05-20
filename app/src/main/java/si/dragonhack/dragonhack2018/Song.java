package si.dragonhack.dragonhack2018;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

public class Song {

    private String title;
    private String artist;
    private int bpm;
    private Uri pathUri;
    private long id;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Song(String title, String artist, int bpm, Uri pathUri, long id) {
        this.title = title;
        this.artist = artist;
        this.bpm = bpm;
        this.pathUri = pathUri;
        this.id = id;

    }

    public Uri getPathUri() {
        return pathUri;
    }

    public void setPathUri(Uri pathUri) {
        this.pathUri = pathUri;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setBpm(int bpm) {
        this.bpm = bpm;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public int getBpm() {
        return bpm;
    }

    public JSONObject toJSONobj() {
        JSONObject obj = new JSONObject();

        try {
            obj.put("title", this.getTitle());
            obj.put("artist", this.getArtist());
            obj.put("bpm", this.getBpm());
            obj.put("pathUri", this.getPathUri());
            obj.put("id", this.getId());

            return obj;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Song toSong(JSONObject obj) {
        try {
           return new Song(obj.getString("title"), obj.getString("artist"), obj.getInt("bpm"), Uri.parse(obj.getString(("pathUri"))),obj.getLong("id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  null;
    }

}

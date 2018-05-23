package si.dragonhack.dragonhack2018;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static si.dragonhack.dragonhack2018.MainActivity.getPath;
import static si.dragonhack.dragonhack2018.MainActivity.saveSongs;
import static si.dragonhack.dragonhack2018.MainActivity.songsCount;

public class GetSongs {

    public static List<Song> songs = new ArrayList<>();
    public static int maximum = 0;
    public static int maxBpm = -1;
    public static int currSongNum = 0;
    private static ProgressDialog pDialog;





    /*

    public static ArrayList<String> listmp3 = new ArrayList<String>();
    public static String[] extensions = { "mp3" };

    public static void loadMp3(String YourFolderPath) {

        File file = new File(YourFolderPath);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        loadMp3(f.getAbsolutePath());
                    } else {
                        for (int i = 0; i < extensions.length; i++) {
                            if (f.getAbsolutePath().endsWith(extensions[i])) {
                                listmp3.add(f.getAbsolutePath());
                            }
                        }
                    }
                }
            }
        }

        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        for(String path : listmp3) {

            mmr.setDataSource(path);
            System.out.println(mmr);


            String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

            System.out.println("TITLEE: " + title);




            Song newSong = MainActivity.getSongsBpm(title, artist);
            newSong.setPathUri(Uri.parse(path));

            songs.add(newSong);

        }
    }


    */



/*


    public static void getSongList(Context context) {

        TreeMap<Integer, Integer> frequencies = new TreeMap<Integer, Integer>();


        //retrieve song info
        ContentResolver musicResolver = context.getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        String[] proj = { MediaStore.Audio.Media.DATA };
        CursorLoader loader = new CursorLoader(context, musicUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();

        //iterate over results if valid
        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int albumId = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM_ID);
            int data= musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int albumkey=musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_KEY);
            //add songs to list

            MainActivity.songsCount = musicCursor.getCount();
            System.out.println(MainActivity.songsCount);

            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                long thisalbumId = musicCursor.getLong(albumId);
                String thisdata= musicCursor.getString(data);
                String AlbumKey = musicCursor.getString(albumkey);

                Song newSong = MainActivity.getSongsBpm(thisTitle, thisArtist,thisId,context);
                newSong.setPathUri(ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, thisId));
                newSong.setId(thisId);

//                if(!newSong.getArtist().equals("") && !newSong.getTitle().equals("")) {
//                    songs.add(newSong);
//
//                }


            }
            while (musicCursor.moveToNext());
        }
    }

*/
    public static void getSongList(final Context context) {

        pDialog = new ProgressDialog(context);
        pDialog.setMessage("Loading songs. Please wait.");
        pDialog.setCancelable(false);
        pDialog.show();

        TreeMap<Integer, Integer> frequencies = new TreeMap<Integer, Integer>();


        ContentResolver musicResolver = context.getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        final Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);



        String[] proj = { MediaStore.Audio.Media.DATA };
        CursorLoader loader = new CursorLoader(context, musicUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();

        //iterate over results if valid
        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
            int titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST);
            int albumId = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            int data = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int albumkey = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_KEY);
            int albumArt = musicCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART);
            songsCount = musicCursor.getCount();
            System.out.println(songsCount);

            final int songsNum = musicCursor.getCount();


            do {
                final long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisdata= musicCursor.getString(data);
                String AlbumKey = musicCursor.getString(albumkey);
                long thisalbumId = musicCursor.getLong(albumId);
                String thisAlbumArt = "";
                if(albumArt != -1) {
                    thisAlbumArt = musicCursor.getString(albumArt);
                }
                System.out.println("ne" + thisAlbumArt);




                String apiKey = "?api_key=6c362d30505f9a33fe5357c9b087bbc0";
                String type = "&type=both";
                String lookup = "&lookup=lookup=" + parseSong(thisTitle) + "%20" + parseArtist(thisArtist);
                String URL =  apiKey + type + lookup;

                final Song newSong = new Song("", "", 0, null, 0, thisAlbumArt);

                Call<Object> callGetSongsBpm = MainActivity.apiClient.getSongsBpm(URL);
                callGetSongsBpm.enqueue(new Callback<Object>() {

                    @Override
                    public void onResponse(Call<Object> call, Response<Object> response) {

                        Object obj = response.body();
                        Gson gson = new Gson();
                        String jsonInString = gson.toJson(obj);

                        try {

                            JSONObject jsonRes = new JSONObject(jsonInString);

                            String resTitle = jsonRes.getJSONArray("search").getJSONObject(0).getString("song_title");
                            String resArtist = jsonRes.getJSONArray("search").getJSONObject(0).getJSONObject("artist").getString("name");
                            String resBpm = jsonRes.getJSONArray("search").getJSONObject(0).getString("tempo");

                            newSong.setArtist(resArtist);
                            newSong.setTitle(resTitle);
                            newSong.setBpm(Integer.parseInt(resBpm));
                            newSong.setId(thisId);

                            Uri contentUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, thisId);
                            String fullpath = getPath(context, contentUri);
                            newSong.setPathUri(Uri.parse(fullpath));

                            currSongNum++;

                            if(!newSong.getArtist().equals("") && !newSong.getTitle().equals("")) {
                                GetSongs.songs.add(newSong);

                            }

                            if(currSongNum >= songsNum) {
                                System.out.println("DONEEEEEEEEEE");
                                saveSongs();
                                if (pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }

                            }


                        } catch (JSONException e) {
                            currSongNum++;
                            if(currSongNum >= songsNum) {
                                System.out.println("DONEEEEEEEEEE");
                                saveSongs();
                                if (pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }

                            }

                        }



                    }


                    @Override
                    public void onFailure(Call<Object> call, Throwable t) {

                        System.out.println(t.getMessage());
                        System.out.println(t);
                        System.out.println("FAILEEEEEEEEEEEEEEEEEED");

                        currSongNum++;

                        if(currSongNum >= songsNum) {
                            System.out.println("DONEEEEEEEEEE");
                            saveSongs();
                            if (pDialog.isShowing()) {
                                pDialog.dismiss();
                            }

                        }

                    }

                });

            }
            while (musicCursor.moveToNext());
        }
    }


    public static String parseArtist(String artistName) {
        artistName = artistName.toLowerCase();
        String []splitSong = artistName.split(" ");

        String parsedName = "";
        for(int i = 0; i < splitSong.length; i++) {
            if(i == splitSong.length-1) {
                parsedName += splitSong[i];
            } else {
                parsedName += splitSong[i] + "+";

            }
        }

        parsedName = "artist:" + parsedName;
        return parsedName;
    }


    public static String parseSong(String song) {

        song = song.toLowerCase();
        String []splitSong = song.split(" ");

        String parsedSong = "";
        for(int i = 0; i < splitSong.length; i++) {
            if(i == splitSong.length-1) {
                parsedSong += splitSong[i];
            } else {
                parsedSong += splitSong[i] + "+";

            }
        }

        parsedSong = "song:" + parsedSong;
        return parsedSong;
    }

}

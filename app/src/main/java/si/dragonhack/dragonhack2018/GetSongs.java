package si.dragonhack.dragonhack2018;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class GetSongs {

    public static List<Song> songs = new ArrayList<>();
    public static int maximum = 0;
    public static int maxBpm = -1;



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


}

package si.dragonhack.dragonhack2018;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements OnStepDetected, OnMusicDecisionChanged{

    public static final String INIT = "init";
    public static final String MY_PREFS = "myPrefs";
    public static final String SONGS = "songs";
    public static final String BPMNUMBER = "bpmNumber";
    public static final String BPM = "bpm";


    public static SharedPreferences sharedPreferences;
    public static ApiClient apiClient;
    SensorManager sensorManager;
    Sensor mSensor;
    TextView footCnt;
    Button resetBt;
    EditText setLimitEt;
    StepDetector stepDetector;
    TextView bpmCountTv;
    public static int songsCount = 0;
    int stepCount = 0;
    private final static String TAG = "StepDetector";
    private int   mLimit = 20;
    private float   mLastValues[] = new float[3*2];
    private float   mScale[] = new float[2];
    private float   mYOffset;

    private float   mLastDirections[] = new float[3*2];
    private float   mLastExtremes[][] = { new float[3*2], new float[3*2] };
    private float   mLastDiff[] = new float[3*2];
    private int     mLastMatch = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        footCnt = (TextView) findViewById(R.id.test_view);
        bpmCountTv = findViewById(R.id.bpm_cnt);
        resetBt = (Button) findViewById(R.id.reset_cnt);
        resetBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stepCount = 0;
                try {
                    footCnt.setText(String.valueOf(stepCount));
                } catch (Exception e)
                {
                }
            }
        });
        setLimitEt = (EditText) findViewById(R.id.limit);
        setLimitEt.setText(String.valueOf(mLimit));
        setLimitEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    mLimit = Integer.parseInt(charSequence.toString());
                } catch (Exception e)
                { }
                stepCount = 0;
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiClient.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiClient = retrofit.create(ApiClient.class);

        stepDetector = StepDetector.getInstance();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        stepDetector.addListener(this);
        sensorManager.registerListener(stepDetector,
                mSensor,
                SensorManager.SENSOR_DELAY_FASTEST);
        MusicManager.getInstance().addListener(this);
        MusicManager.getInstance().startMeasuring();
        sharedPreferences = getSharedPreferences(MY_PREFS, Context.MODE_PRIVATE);

        String res = sharedPreferences.getString(SONGS,"");
        try {
            JSONArray jsonArray = new JSONArray(res);
            for(int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject obj = jsonArray.getJSONObject(i);
                GetSongs.songs.add(Song.toSong(obj));
            }
        } catch (Exception e ) {
            e.printStackTrace();
        }
        /* Check for read files permission */
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

            }

            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    0);

            return;
        }
        if(sharedPreferences.getBoolean(INIT, true)) {
            GetSongs.getSongList(this);
        }

        //String fullPath = getRealPathFromURI(Uri.parse("content://external/audio/media/217"),getApplicationContext());



        //MusicManager.getInstance().startSong();

    }

    private String getRealPathFromURI(Uri contentUri,Context context) {
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        ContentResolver musicResolver = context.getContentResolver();

        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        String[] proj = { MediaStore.Audio.Media.DATA };
        CursorLoader loader = new CursorLoader(context, musicUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        while(cursor.moveToNext())
        {
            int index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            String res = cursor.getString(column_index);

            for (int i = 0; i < GetSongs.songs.size(); i++) {
                if(res.contains(GetSongs.songs.get(i).getTitle()))
                {
                    GetSongs.songs.get(i).setPathUri(Uri.parse(res));
                    break;
                }
            }

        }
        saveSongs();


        return result;
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

        //System.out.println("Parsed name: " + parsedName);
        return parsedName;
    }

    @Override
    protected void onResume() {
        super.onResume();
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


        //System.out.println("Parsed song " + parsedSong);
        return parsedSong;
    }





    public static void saveSongs() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(INIT, false);
        JSONArray arr = new JSONArray();

        TreeMap<Integer, Integer> frequencies = new TreeMap<Integer, Integer>();

        for(Song song : GetSongs.songs) {
            arr.put(song.toJSONobj());
            if (frequencies.get(new Integer(song.getBpm())) == null) {
                frequencies.put(new Integer(song.getBpm()), 1);
            } else {
                frequencies.put(new Integer(song.getBpm()), new Integer(song.getBpm() + 1));
            }
        }

        if(!frequencies.isEmpty()) {
            int max = 0;
            int bpm = -1;
            for(Map.Entry<Integer, Integer> entry : frequencies.entrySet()) {
                if(bpm == -1 || max < entry.getValue()) {
                    max = entry.getValue();
                    bpm = entry.getKey();
                }
            }
            editor.putInt(BPMNUMBER, max);
            editor.putInt(BPM, bpm);
            System.out.printf("Bpm %d se pojavi %d-krat!", bpm, max);
        }

        editor.putString(SONGS, arr.toString());
        editor.apply();
        editor.commit();


    }




    public static Song getSongsBpm(String song, String artist, final long thisId, final Context context) {

        String apiKey = "?api_key=8c9b6ffc8166265f75769afb21c71043";
        String type = "&type=both";
        String lookup = "&lookup=lookup=" + parseSong(song) + "%20" + parseArtist(artist);
        String URL =  apiKey + type + lookup;

        final Song newSong = new Song("", "", 0, null, 0);

        Call<Object> callGetSongsBpm = apiClient.getSongsBpm(URL);
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

                    /*
                    System.out.println(newSong.getArtist());
                    System.out.println(newSong.getTitle());
                    System.out.println(newSong.getBpm());
                    */

                    System.out.println(songsCount);
                    songsCount--;
                    if(songsCount < 10)
                        saveSongs();

//                    newSong.setPathUri(ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, thisId));
                    newSong.setId(thisId);
                    Uri contentUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, thisId);
                    String fullpath = getPath(context,contentUri);
                    newSong.setPathUri(Uri.parse(fullpath));

                    if(!newSong.getArtist().equals("") && !newSong.getTitle().equals("")) {
                        GetSongs.songs.add(newSong);

                    }


                } catch (JSONException e) {
                   // e.printStackTrace();
                    songsCount--;

                    if(songsCount < 10)
                        saveSongs();


                }



            }


            @Override
            public void onFailure(Call<Object> call, Throwable t) {

               // System.out.println(t.getMessage());
               // System.out.println(t);
                System.out.println(songsCount);
                songsCount--;

                if(songsCount < 10)
                    saveSongs();

            }

        });
        return newSong;
    }

    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    @Override
    public void onStep(int arg) {
        stepCount++;
        footCnt.setText(String.valueOf(stepCount));
    }


    @Override
    public void onBpmChanged(int bpm) {
        bpmCountTv.setText(String.valueOf(bpm));
    }

    @Override
    public void onMusicChanged(MusicManager.MusicEvents event) {

    }
}

package com.cooervo.ecoplayer;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A service to let MediaPlayer run in background even when other sound are playing
 */
public class MediaPlayerService extends Service {

    private final String TAG = MediaPlayerService.class.getSimpleName();

    private MediaPlayer player;
    private List<Song> songs = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        scanAllAudio();
    }

    /**
     * Scans ALL audio files in devices and adds the path of every Song to the list of songs
     */
    private void scanAllAudio() {
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String[] projection = {
                MediaStore.Audio.Media.DATA,
        };

        final String sortOrder = MediaStore.Audio.AudioColumns.TITLE + " COLLATE LOCALIZED ASC";

        Cursor cursor = null;
        try {
            Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            cursor = getApplicationContext().getContentResolver().query(uri, projection, selection, null, sortOrder);

            if (cursor != null) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {

                    String path = cursor.getString(0);
                    Song song = new Song();
                    song.path = path;
                    songs.add(song);

                    cursor.moveToNext();
                }

            }

        } catch (Exception e) {
            Log.d(TAG, e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /**
     * START_STICKY: This is suitable for media players (or similar services)
     * that are not executing commands, but running indefinitely and waiting
     * for a job.
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {

            Song randomSong = getRandomSong();
            String command = intent.getStringExtra("command");

            if (command.equals("playRandomSong")) {
                startPlayer(randomSong);

            } else {
                stopPlayer();
            }

        }
        return super.onStartCommand(intent, flags, START_STICKY);
    }

    private void stopPlayer() {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
    }

    private void startPlayer(Song randomSong) {
        try {
            player = new MediaPlayer();
            player.setDataSource(randomSong.path);
            player.prepareAsync();

        } catch (IOException e) {
            e.printStackTrace();
        }

        //Once prepareAsync() is finished start() media player
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
    }


    private Song getRandomSong() {
        Random r = new Random();
        int min = 0;
        int max = songs.size();
        int randomNum = r.nextInt(max - min) + min;

        return songs.get(randomNum);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        stopPlayer();
    }
}

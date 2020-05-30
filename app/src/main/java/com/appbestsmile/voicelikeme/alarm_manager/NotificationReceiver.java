package com.appbestsmile.voicelikeme.alarm_manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.nfc.Tag;
import android.util.Log;

import java.io.IOException;

public class NotificationReceiver extends BroadcastReceiver
{
    private final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction() == "notification")
        {
            // TODO: Grab extras from the Intent as needed.
            String voice_name = intent.getStringExtra("voice_name");
            String voice_path = intent.getStringExtra("voice_path");

            Log.d(TAG, voice_path);

            Boolean isPlayable = intent.getBooleanExtra("playable", false);

            Log.d(TAG, isPlayable.toString());

            if(isPlayable){

                int maxCount = Integer.parseInt(intent.getStringExtra("play_count"));

                Log.d(TAG, "maxCount : " + maxCount);

                MediaPlayer mediaPlayer = new MediaPlayer();

                try {

                    mediaPlayer.setDataSource(voice_path);
                    mediaPlayer.prepare();

                } catch (IOException e) {
                    e.printStackTrace();
                }

                final int[] count = {0}; // initialise outside listener to prevent looping

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){

                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        if(count[0] < maxCount) {
                            count[0]++;
                            mediaPlayer.seekTo(0);
                            mediaPlayer.start();

                            Log.d("Played Count : ", count[0] + "");
                        }
                    }});

                mediaPlayer.start();
            }
        }
    }
}
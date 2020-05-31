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

            int position = intent.getIntExtra("position", -1);

            Log.d(TAG, position + "");
        }
    }
}
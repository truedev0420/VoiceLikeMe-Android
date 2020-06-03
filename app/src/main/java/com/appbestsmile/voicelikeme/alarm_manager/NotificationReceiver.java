package com.appbestsmile.voicelikeme.alarm_manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.appbestsmile.voicelikeme.activities.MainActivity;
import com.appbestsmile.voicelikeme.activities.PlayListActivity;


public class NotificationReceiver extends BroadcastReceiver
{
    private final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction() == "notification")
        {
            // TODO: Grab extras from the Intent as needed.
            int position = intent.getIntExtra("position", -1);
            String voice_name = intent.getStringExtra("voice_name");
            String voice_path = intent.getStringExtra("voice_path");

            Log.d(TAG, position + "");

            Intent playListIntent = new Intent(context, PlayListActivity.class);
            playListIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            playListIntent.putExtra("position", position);
            playListIntent.putExtra("voice_name", voice_name);
            playListIntent.putExtra("voice_path", voice_path);

            context.startActivity(playListIntent);
        }
    }
}
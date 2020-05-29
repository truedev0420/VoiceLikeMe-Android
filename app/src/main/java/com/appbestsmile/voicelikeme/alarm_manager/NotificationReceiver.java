package com.appbestsmile.voicelikeme.alarm_manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == "notification")
        {
            // TODO: Grab extras from the Intent as needed.
            String voice_name = intent.getStringExtra("voice_name");

            Log.d("notification", voice_name);
        }
    }
}
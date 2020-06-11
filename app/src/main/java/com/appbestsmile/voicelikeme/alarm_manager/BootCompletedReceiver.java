package com.appbestsmile.voicelikeme.alarm_manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompletedReceiver extends BroadcastReceiver {

    private static final String TAG = BootCompletedReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

        if(intent.getAction() != null)
        {
            if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) ||
                    intent.getAction().equals(Intent.ACTION_USER_PRESENT))
            {
                WatchScheduleThread scheduleThread = new WatchScheduleThread(context);
                scheduleThread.start();
            }
        }
    }
}

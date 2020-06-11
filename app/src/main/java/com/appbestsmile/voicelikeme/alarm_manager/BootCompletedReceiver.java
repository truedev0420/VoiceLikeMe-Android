package com.appbestsmile.voicelikeme.alarm_manager;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.appbestsmile.voicelikeme.db.ScheduleItem;
import com.appbestsmile.voicelikeme.db.ScheduleItemDataSource;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class BootCompletedReceiver extends BroadcastReceiver {

    private static final String TAG = BootCompletedReceiver.class.getSimpleName();
    @Inject
    public ScheduleItemDataSource scheduleItemDataSource;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

        if(intent.getAction() != null)
        {
            if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) ||
                    intent.getAction().equals(Intent.ACTION_USER_PRESENT))
            {
                /*Handler handler = new Handler();

                Thread thread = new Thread() {
                    @SuppressLint("CheckResult")
                    @Override
                    public void run() {


                    }
                };

                thread.start();*/

//                WatchScheduleThread scheduleThread = new WatchScheduleThread();
//                scheduleThread.start();
            }
        }
    }
}

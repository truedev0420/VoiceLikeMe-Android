package com.appbestsmile.voicelikeme.chat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.appbestsmile.voicelikeme.R;
import com.appbestsmile.voicelikeme.activities.ChatTopicActivity;
import com.appbestsmile.voicelikeme.alarm_manager.NotificationReceiver;
import com.appbestsmile.voicelikeme.db.AppDataBase;
import com.appbestsmile.voicelikeme.db.RecordingItem;
import com.appbestsmile.voicelikeme.db.ScheduleItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class WatchTopicThread extends Thread {

    private static final String TAG = WatchTopicThread.class.getSimpleName();
    private static final int SLEEP_INTERVAL = 1000;

    private ChatTopicActivity context;
    Handler handler;

    public WatchTopicThread(ChatTopicActivity context){
        this.context = context;
        handler = new Handler();
    }

    @Override
    public void run() {
        super.run();

        try {

            while(true) {

                context.loadTopicsFirebase(null);
                Thread.sleep(SLEEP_INTERVAL);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

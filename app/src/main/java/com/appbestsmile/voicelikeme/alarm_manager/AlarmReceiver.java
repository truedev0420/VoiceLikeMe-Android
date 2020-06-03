package com.appbestsmile.voicelikeme.alarm_manager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.appbestsmile.voicelikeme.R;

import java.io.IOException;
import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {

    private final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "Alarm went off");

        Calendar currentCalendar = Calendar.getInstance();
        int notifyID = (int) currentCalendar.getTimeInMillis();
        String CHANNEL_ID = "my_channel_01";// The id of the channel.
        CharSequence name = "channel_name";// The user-visible name of the channel.
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
        }


//        RemoteViews notificationView = new RemoteViews(context.getPackageName(), R.layout.layout_notification);
//        notificationView.setTextViewText(R.id.textAlarmMessage, alarmMessage);


        String voice_name = intent.getStringExtra("voice_name");
        String voice_path = intent.getStringExtra("voice_path");

        Boolean isPlayable = intent.getBooleanExtra("playable", false);

        Intent deleteIntent = new Intent(context, NotificationReceiver.class);
        deleteIntent.setAction("notification");
        deleteIntent.putExtra("voice_name", voice_name);
        deleteIntent.putExtra("voice_path", voice_path);

        String alarmMessage;
        String play_count = "";

        if(isPlayable){

            // user receive push notification and play sound 10 times
            alarmMessage = String.format(context.getString(R.string.notification_msg_playing), voice_name, play_count);

            MediaPlayer mediaPlayer = new MediaPlayer();

            try {
                mediaPlayer.setDataSource(voice_path);
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            final int[] count = {0}; // initialise outside listener to prevent looping
            int maxCount = Integer.parseInt(intent.getStringExtra("play_count"));

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
        }else{

            // user receive push notification and if user click the notification, go
            // to the voice list or long tap the sound file
            alarmMessage = String.format("Go to PlayList View.");

            int position = intent.getIntExtra("position", -1);
            intent.putExtra("voice_name", voice_name);
            intent.putExtra("voice_path", voice_path);


            Log.d(TAG, position + "");

            deleteIntent.putExtra("position", position);
        }


        // TODO: Set extras as needed.
        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context, notifyID, deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentTitle(context.getString(R.string.notification_title))
                        .setContentText(alarmMessage)
                        .setDeleteIntent(deletePendingIntent)
                        .setChannelId(CHANNEL_ID);


        Notification notification = builder.build();


        NotificationManager mNotificationManager =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationManager.createNotificationChannel(mChannel);
        }
        mNotificationManager.notify(notifyID , notification);
    }
}
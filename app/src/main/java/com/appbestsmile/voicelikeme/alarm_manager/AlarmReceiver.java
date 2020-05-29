package com.appbestsmile.voicelikeme.alarm_manager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.appbestsmile.voicelikeme.R;

import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {

        Toast.makeText(context, "Alarm went off", Toast.LENGTH_SHORT).show();
        Log.d("MyAlarmReceiver", "Alarm went off");

        Calendar currentCalendar = Calendar.getInstance();
        int notifyID = (int) currentCalendar.getTimeInMillis();
        String CHANNEL_ID = "my_channel_01";// The id of the channel.
        CharSequence name = "channel_name";// The user-visible name of the channel.
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);


//        RemoteViews notificationView = new RemoteViews(context.getPackageName(), R.layout.layout_notification);
//        notificationView.setTextViewText(R.id.textAlarmMessage, alarmMessage);


        String voice_name = intent.getStringExtra("voice_name");
        Boolean isPlayable = intent.getBooleanExtra("playable", false);

        Intent deleteIntent = new Intent(context, NotificationReceiver.class);
        deleteIntent.setAction("notification");
        deleteIntent.putExtra("voice_name", voice_name);
        deleteIntent.putExtra("playable", isPlayable);


        String alarmMessage;
        String play_count = "";

        if(isPlayable){

            play_count = intent.getStringExtra("play_count");

            // user receive push notification and play sound 10 times
            alarmMessage = String.format("Playing %s, %s times.", voice_name, play_count);

        }else{

            // user receive push notification and if user click the notification, go
            // to the voice list or long tap the sound file
            alarmMessage = String.format("Go to PlayList View.");
        }


        // TODO: Set extras as needed.
        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context, notifyID, deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentTitle("Schedule")
                        .setContentText(alarmMessage)
                        .setDeleteIntent(deletePendingIntent)
                        .setChannelId(CHANNEL_ID);


        Notification notification = builder.build();


        NotificationManager mNotificationManager =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.createNotificationChannel(mChannel);
        mNotificationManager.notify(notifyID , notification);
    }
}
package com.appbestsmile.voicelikeme.alarm_manager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.appbestsmile.voicelikeme.R;
import com.appbestsmile.voicelikeme.db.AppDataBase;
import com.appbestsmile.voicelikeme.db.RecordingItem;
import com.appbestsmile.voicelikeme.db.ScheduleItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class WatchScheduleThread extends Thread {

    private static final String TAG = WatchScheduleThread.class.getSimpleName();
    private static final int SLEEP_INTERVAL = 5000;

    private Context context;
    Handler handler;

    public WatchScheduleThread(Context context){
        this.context = context;
        handler = new Handler();
    }

    @Override
    public void run() {
        super.run();

        try {

            while(true) {

                final List<ScheduleItem>[] listSchedules = new List[]{new ArrayList<ScheduleItem>()};
                listSchedules[0] =  AppDataBase.getInstance(context).scheduleItemDao().getAllSchedules();
                handler.post(this);

                Log.d(TAG, "----------------------");
                Log.d(TAG, listSchedules[0].size() + "");

                for (int i = 0; i < listSchedules[0].size(); i++) {

                    ScheduleItem scheduleItem = listSchedules[0].get(i);
                    long scheduleTime = Long.parseLong(scheduleItem.getTime());
                    long currentTime = System.currentTimeMillis();


                    if(scheduleItem.getIsRepeat() == 0 && scheduleTime < currentTime){
                        Log.d(TAG, "Sending push notification...");
                        sendPushNotification(scheduleItem);
                        AppDataBase.getInstance(context).scheduleItemDao().deleteScheduleItem(scheduleItem);
                    }


                    if(scheduleItem.getIsRepeat() == 1){

                        String weekday = scheduleItem.getWeekdays();
                        String[] scheduleTimes = weekday.split(":");
                        String weekdayUpdated = "";

                        for(int weekItem = 0; weekItem < scheduleTimes.length; weekItem++){

                            if(Long.parseLong(scheduleTimes[weekItem]) < System.currentTimeMillis()){

                                Log.d(TAG, "Sending push notification if repeat mode ...");
                                sendPushNotification(scheduleItem);

                                scheduleTimes[weekItem] = (Long.parseLong(scheduleTimes[weekItem]) + 7 * 24 * 60 * 60 * 1000) + "";
                            }

                            if(weekItem != scheduleTimes.length - 1)
                                weekdayUpdated += scheduleTimes[weekItem] + ":";
                            else
                                weekdayUpdated += scheduleTimes[weekItem];
                        }

                        scheduleItem.setWeekdays(weekdayUpdated);

                        AppDataBase.getInstance(context).scheduleItemDao().updateScheduleItem(scheduleItem);
                    }
                }

                Thread.sleep(SLEEP_INTERVAL);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendPushNotification(ScheduleItem scheduleItem){

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


        String voice_name = scheduleItem.getName();
        Boolean isPlayable = scheduleItem.getIsPlay() == 0 ? false : true;

        Intent deleteIntent = new Intent(context, NotificationReceiver.class);
        deleteIntent.setAction("notification");
        deleteIntent.putExtra("voice_name", voice_name);

        String alarmMessage;
        String play_count = scheduleItem.getIsPlay() + "";
        String voice_path = "";

        List<RecordingItem> recordingItemList = AppDataBase.getInstance(context).recordItemDao().getAllRecordings();
        for(RecordingItem recordingItem : recordingItemList){

            if(recordingItem.getFilePath().contains(voice_name)){

                voice_path = recordingItem.getFilePath();
                break;
            }
        }


        if(isPlayable && voice_path != ""){

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
            int maxCount = Integer.parseInt(scheduleItem.getIsPlay() + "");

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
            deleteIntent.putExtra("voice_name", voice_name);
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

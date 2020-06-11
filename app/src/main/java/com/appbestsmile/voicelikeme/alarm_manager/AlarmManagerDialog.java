package com.appbestsmile.voicelikeme.alarm_manager;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.util.Calendar;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import com.appbestsmile.voicelikeme.R;
import com.appbestsmile.voicelikeme.db.ScheduleItem;
import com.appbestsmile.voicelikeme.db.ScheduleItemDataSource;

import java.util.Date;

import javax.inject.Inject;


public class AlarmManagerDialog extends Dialog implements View.OnClickListener, TimePickerDialog.OnTimeSetListener {

    private String TAG = "AlarmManagerDialog";

    private Activity activity;
    private Button btnTime, btnOK, btnCancel;
    private LinearLayout layoutWeekday;
    private ToggleButton[] weekdays;
    private CheckBox checkRepeat;
    private TextView textVoiceName;
    private EditText editPlayCount;
    private CheckBox checkPlay;
    private LinearLayout layoutPlay;

    private int mHourOfDay, mMinute;

    private String mVoiceName;
    private String mVoicePath;
    private int mPosition;

    private ScheduleItemDataSource scheduleItemDataSource;


    public AlarmManagerDialog(Activity activity, ScheduleItemDataSource scheduleItemDataSource, String voiceName, String voicePath, int position) {

        super(activity);
        // TODO Auto-generated constructor stub

        this.scheduleItemDataSource = scheduleItemDataSource;
        this.activity = activity;
        mVoiceName = voiceName;
        mVoicePath = voicePath;
        mPosition = position;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.dialog_alarm_manager);

        Window window = getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,  ViewGroup.LayoutParams.WRAP_CONTENT);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        checkPlay           = (CheckBox) findViewById(R.id.checkPlay);
        checkRepeat         = (CheckBox) findViewById(R.id.checkRepeat);
        layoutPlay          = (LinearLayout) findViewById(R.id.layoutPlay);

        textVoiceName       = (TextView) findViewById(R.id.textVoiceName);
        textVoiceName.setText(mVoiceName);


        editPlayCount       = (EditText) findViewById(R.id.editPlays);

        btnTime             = (Button) findViewById(R.id.btnTime);
        btnOK               = (Button) findViewById(R.id.btnOK);
        btnCancel           = (Button) findViewById(R.id.btnCancel);


        btnTime.setOnClickListener(this);
        btnOK.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        checkRepeat.setOnClickListener(this);
        checkPlay.setOnClickListener(this);

        layoutWeekday = (LinearLayout) findViewById(R.id.layoutWeekday);

        weekdays = new ToggleButton[7];
        weekdays[0]     = (ToggleButton) findViewById(R.id.sunday);
        weekdays[1]     = (ToggleButton) findViewById(R.id.monday);
        weekdays[2]     = (ToggleButton) findViewById(R.id.tuesday);
        weekdays[3]     = (ToggleButton) findViewById(R.id.wednesday);
        weekdays[4]     = (ToggleButton) findViewById(R.id.thursday);
        weekdays[5]     = (ToggleButton) findViewById(R.id.friday);
        weekdays[6]     = (ToggleButton) findViewById(R.id.saturday);

        Log.d("AlarmManager", scheduleItemDataSource.toString());
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void init(){

        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        setTimeText(hour, minute);
    }

    private void setTimeText(int hourOfDay, int minute){

        /*SpannableString ss1=  new SpannableString(strTime);
        ss1.setSpan(new RelativeSizeSpan(2f), 0,2, 0); // set size*/

        btnTime.setText( hourOfDay + ":" + String.format("%02d", minute));
        this.mHourOfDay = hourOfDay;
        this.mMinute = minute;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View v) {

        int id = v.getId();

        switch (id) {
            case R.id.btnTime:

                // TODO Auto-generated method stub

                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(activity, this, mHourOfDay, mMinute, true);//Yes 24 hour time

                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
                break;

            case R.id.checkRepeat:

                if (checkRepeat.isChecked()) {
                    layoutWeekday.setVisibility(View.VISIBLE);
                } else {
                    layoutWeekday.setVisibility(View.GONE);
                }
                break;

            case R.id.checkPlay:

                if (checkPlay.isChecked())
                    layoutPlay.setVisibility(View.VISIBLE);
                else
                    layoutPlay.setVisibility(View.GONE);

                break;

            case R.id.btnOK:

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, mHourOfDay);
                calendar.set(Calendar.MINUTE, mMinute);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);


                ScheduleItem scheduleItem = new ScheduleItem();
                scheduleItem.setName(mVoiceName);
                scheduleItem.setIsPlay(checkPlay.isChecked() ? (byte) Integer.parseInt(editPlayCount.getText().toString()) : 0);
                scheduleItem.setIsRepeat(checkRepeat.isChecked() ? (byte) 1 : 0);

                long scheduleTime = calendar.getTimeInMillis();
                if (!checkRepeat.isChecked() && calendar.getTimeInMillis() < System.currentTimeMillis())
                    scheduleTime = calendar.getTimeInMillis() + 24 * 60 * 60 * 1000;

                scheduleItem.setTime(scheduleTime + "");

                Log.d(TAG, calendar.getTime().toString());

                String weekday = "";

                if(checkRepeat.isChecked()) {

                    for(int i = 0; i < 7; i++){
                        if(weekdays[i].isChecked()){

                            calendar.set(Calendar.DAY_OF_WEEK, i + 1);
                            if(calendar.getTimeInMillis() < System.currentTimeMillis())
                                weekday += (calendar.getTimeInMillis() + 7 * 24 * 60 * 60 * 1000);
                            else
                                weekday += calendar.getTimeInMillis();
                        }else
                            weekday += "0";

                        if(i < 6)
                            weekday += ":";
                    }
                }
                scheduleItem.setWeekdays(weekday);


                Handler handler = new Handler();

                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            scheduleItemDataSource.insertNewScheduleItem(scheduleItem);
                            handler.post(this);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };

                thread.start();

                try {

                }catch(Exception e){
                    Log.e(TAG, e.toString());
                }


                /*
                // Register alarm / notification to Android System AlarmManager.

                AlarmManager alarmMgr = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);

                Intent intent = new Intent(activity, AlarmReceiver.class);

                intent.putExtra("voice_name", mVoiceName);
                intent.putExtra("voice_path", mVoicePath);
                intent.putExtra("playable",  checkPlay.isChecked());
                intent.putExtra("position", mPosition);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(activity, (int) calendar.getTimeInMillis(), intent, 0);

                if(checkPlay.isChecked())
                    intent.putExtra("play_count", editPlayCount.getText().toString());


                if(!checkRepeat.isChecked()){

                    Log.d(TAG, alarmCalendar.getTime().toString());
                    alarmMgr.set(AlarmManager.RTC_WAKEUP, alarmCalendar.getTimeInMillis(), pendingIntent);

                }else{

                    for(int i = 0; i < 7; i++){

                        if(weekdays[i].isChecked()){

                            scheduleAlarm(i + 1);
                        }
                    }
                }*/

                dismiss();
                break;

            case R.id.btnCancel :
                dismiss();
                break;
        }
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

        setTimeText(hourOfDay, minute);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void scheduleAlarm(int dayOfWeek) {

        Calendar calendar = Calendar.getInstance();

        int todayWeekday = Calendar.DAY_OF_WEEK;

        calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        calendar.set(Calendar.HOUR_OF_DAY, mHourOfDay);
        calendar.set(Calendar.MINUTE, mMinute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Check we aren't setting it in the past which would trigger it to fire instantly
        if(calendar.getTimeInMillis() < System.currentTimeMillis() && dayOfWeek != todayWeekday) {
            calendar.add(Calendar.DAY_OF_YEAR, 7);
        }


        Log.d(TAG, calendar.getTime().toString());
        Intent intent = new Intent(activity, AlarmReceiver.class);

        intent.putExtra("position", mPosition);
        intent.putExtra("voice_name", mVoiceName);
        intent.putExtra("voice_path", mVoicePath);

        // Set this to whatever you were planning to do at the given time
        PendingIntent pendingIntent = PendingIntent.getBroadcast(activity, (int) calendar.getTimeInMillis(), intent, 0);

        AlarmManager alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pendingIntent);
    }
}

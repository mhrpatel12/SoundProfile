package com.appontherocks.soundprofile;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Created by Mihir on 4/30/2017.
 */

public class SleepyHours extends BroadcastReceiver {
    private static final String TAG = "SleepyHours";
    private AudioManager audioManager;

    @Override
    public void onReceive(Context mContext, Intent intent) {
        // Put here YOUR code.
        Log.e(TAG, "Alarm !!!!!!!!!!");
        Toast.makeText(mContext, "Alarm !!!!!!!!!!", Toast.LENGTH_LONG).show(); // For example
        audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_RING, 37, 0);
    }

    public void setSleepyHours(Context context) {
        // Set the alarm to start at approximately 2:00 p.m.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 39);

        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, SleepyHours.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);

        Log.e(TAG, "onSleepyHoursSet");
    }

    public void cancelSleepyHours(Context context) {
        Intent intent = new Intent(context, SleepyHours.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
        Log.e(TAG, "onSleepyHoursCancelled");
    }
}

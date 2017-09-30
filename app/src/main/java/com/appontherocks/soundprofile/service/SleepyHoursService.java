package com.appontherocks.soundprofile.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.appontherocks.soundprofile.R;
import com.appontherocks.soundprofile.event.SleepyHoursEnd;
import com.appontherocks.soundprofile.event.SleepyHoursStart;

public class SleepyHoursService extends Service {
    private static final String TAG = "SleepyHoursService";
    SleepyHoursStart sleepyHoursStart = new SleepyHoursStart();
    SleepyHoursEnd sleepyHoursEnd = new SleepyHoursEnd();

    public void onCreate() {
        Log.e(TAG, "onCreate");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        Bundle extras = intent.getExtras();
        if (extras == null)
            Log.d("Service", "null");
        else {
            Log.d("Service", "not null");
            sleepyHoursStart.setSleepyHoursStart(SleepyHoursService.this, (int) extras.get(getString(R.string.startHour)), (int) extras.get(getString(R.string.startMinute)));
            sleepyHoursEnd.setSleepyHoursEnd(SleepyHoursService.this, (int) extras.get(getString(R.string.endHour)), (int) extras.get(getString(R.string.endMinute)));
        }
        return START_STICKY;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.e(TAG, "onStart");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        sleepyHoursStart.cancelSleepyHoursStart(SleepyHoursService.this);
        sleepyHoursEnd.cancelSleepyHoursEnd(SleepyHoursService.this);
        super.onDestroy();
    }
}

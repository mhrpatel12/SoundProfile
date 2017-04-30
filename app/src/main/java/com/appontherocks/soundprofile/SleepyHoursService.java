package com.appontherocks.soundprofile;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class SleepyHoursService extends Service {
    private static final String TAG = "SleepyHoursService";
    SleepyHours sleepyHours = new SleepyHours();

    public void onCreate() {
        Log.e(TAG, "onCreate");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        sleepyHours.setSleepyHours(SleepyHoursService.this);
        return START_STICKY;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.e(TAG, "onStart");
        sleepyHours.setSleepyHours(SleepyHoursService.this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        sleepyHours.cancelSleepyHours(SleepyHoursService.this);
        super.onDestroy();
    }
}

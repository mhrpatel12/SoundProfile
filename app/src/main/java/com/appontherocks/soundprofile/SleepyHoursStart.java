package com.appontherocks.soundprofile;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.appontherocks.soundprofile.models.SoundProfile;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Mihir on 4/30/2017.
 */

public class SleepyHoursStart extends BroadcastReceiver implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "SleepyHoursStart";
    private final int STEP_ONE_COMPLETE = 0;
    List<String> mGeofenceList;
    private AudioManager audioManager;
    private ArrayList<SoundProfile> profileArrayList = new ArrayList<>();
    private GoogleApiClient mGoogleApiClient;
    private Context mContext;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STEP_ONE_COMPLETE:
                    if (mGeofenceList.size() > 0) {
                        LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, mGeofenceList);
                    }

                    break;
            }
        }
    };

    @Override
    public void onReceive(Context mContext, Intent intent) {
        // Put here YOUR code.
        mContext = mContext;
        Log.e(TAG, "Start Hours !!!!!!!!!!");
        Toast.makeText(mContext, "Alarm !!!!!!!!!!", Toast.LENGTH_LONG).show(); // For example
        audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);

        final WifiManager wifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        SharedPreferences prefs = mContext.getSharedPreferences(mContext.getString(R.string.advanced_settings), MODE_PRIVATE);
        boolean autoDisableWifi = prefs.getBoolean(mContext.getString(R.string.auto_disable_wifi), false); //false is the default value.

        if (autoDisableWifi) {
            wifiManager.setWifiEnabled(false);
        }

        mGeofenceList = new ArrayList<String>();
        Toast.makeText(mContext, "Booting Completed", Toast.LENGTH_LONG).show();
        fetchGeoFences();
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    private void fetchGeoFences() {
        Thread backgroundThread = new Thread() {
            @Override
            public void run() {
                try {
                    FirebaseDatabase.getInstance().getReference().child("profiles").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            profileArrayList.clear();
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                SoundProfile profile = ds.getValue(SoundProfile.class);
                                if ((profile.mKey != null) && (profile.latitude != null) && (profile.longitude != null) && !((profile.latitude + "").equals("")) && !((profile.longitude + "").equals(""))) {
                                    mGeofenceList.add(profile.mKey);
                                }
                            }
                            Message msg = Message.obtain();
                            msg.what = STEP_ONE_COMPLETE;
                            handler.sendMessage(msg);
                            FirebaseDatabase.getInstance().getReference().child("profiles").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } catch (Exception e) {
                }
            }
        };
        backgroundThread.start();

    }

    public void setSleepyHoursStart(Context context, int startHours, int startMinutes) {
        // Set the alarm to start at approximately 2:00 p.m.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, startHours);
        calendar.set(Calendar.MINUTE, startMinutes);

        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, SleepyHoursStart.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);

        Log.e(TAG, "onSleepyHoursStartSet");
    }

    public void cancelSleepyHoursStart(Context context) {
        Intent intent = new Intent(context, SleepyHoursStart.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
        Log.e(TAG, "onSleepyHoursStartCancelled");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}

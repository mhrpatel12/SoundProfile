package com.appontherocks.soundprofile.event;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.appontherocks.soundprofile.R;
import com.appontherocks.soundprofile.Utility.Constants;
import com.appontherocks.soundprofile.models.SoundProfile;
import com.appontherocks.soundprofile.service.GeofenceTransitionsIntentService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
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

public class SleepyHoursEnd extends BroadcastReceiver implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {
    private static final String TAG = "SleepyHoursEnd";
    private final int STEP_ONE_COMPLETE = 0;
    List<Geofence> mGeofenceList;
    private AudioManager audioManager;
    private ArrayList<SoundProfile> profileArrayList = new ArrayList<>();
    private GoogleApiClient mGoogleApiClient;
    private Context mContext;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STEP_ONE_COMPLETE:
                    LocationManager manager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
                    if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        if (mGeofenceList.size() > 0) {
                            new SleepyHoursEnd.saveGeoFerncesAyncTask().execute();
                        }
                    }
                    break;
            }
        }
    };

    @Override
    public void onReceive(Context mContext, Intent intent) {
        // Put here YOUR code.
        mContext = mContext;
        Log.e(TAG, "End Hours !!!!!!!!!!");
        Toast.makeText(mContext, "Alarm !!!!!!!!!!", Toast.LENGTH_LONG).show(); // For example
        audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

        final WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        SharedPreferences prefs = mContext.getSharedPreferences(mContext.getString(R.string.advanced_settings), MODE_PRIVATE);
        boolean autoDisableWifi = prefs.getBoolean(mContext.getString(R.string.auto_disable_wifi), false); //false is the default value.

        if (autoDisableWifi) {
            wifiManager.setWifiEnabled(true);
        }

        mGeofenceList = new ArrayList<Geofence>();
        Toast.makeText(mContext, "Booting Completed", Toast.LENGTH_LONG).show();
        LocationManager manager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            fetchGeoFences();
        }
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

    public void setSleepyHoursEnd(Context context, int endHours, int endMinutes) {
        // Set the alarm to start at approximately 2:00 p.m.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, endHours);
        calendar.set(Calendar.MINUTE, endMinutes);

        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, SleepyHoursEnd.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);

        Log.e(TAG, "onSleepyHoursEnd");
    }

    public void cancelSleepyHoursEnd(Context context) {
        Intent intent = new Intent(context, SleepyHoursEnd.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
        Log.e(TAG, "onSleepyHoursEndCancelled");
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
                                    mGeofenceList.add(new Geofence.Builder()
                                            // Set the request ID of the geofence. This is a string to identify this
                                            // geofence.
                                            .setRequestId(profile.mKey)
                                            .setCircularRegion(
                                                    Double.parseDouble(profile.latitude),
                                                    Double.parseDouble(profile.longitude),
                                                    50
                                            )
                                            .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_TIME)
                                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                                                    Geofence.GEOFENCE_TRANSITION_EXIT)
                                            .build());
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

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);


        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
/*        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }*/
        Intent intent = new Intent(mContext, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        return PendingIntent.getService(mContext, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
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

    @Override
    public void onResult(@NonNull Status status) {

    }

    public class saveGeoFerncesAyncTask extends AsyncTask<Void, String, String> {

        /**
         * Before starting background thread
         * Show Progress Bar Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(Void... f_url) {
            if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return null;
            }

            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent()
            ).setResultCallback(SleepyHoursEnd.this);

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
        }
    }
}

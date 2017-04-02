/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appontherocks.soundprofile;

import android.*;
import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.appontherocks.soundprofile.models.SoundProfile;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.appontherocks.soundprofile.Constants.CONNECTION_TIME_OUT_MS;
import static com.appontherocks.soundprofile.Constants.GEOFENCE_DATA_ITEM_PATH;
import static com.appontherocks.soundprofile.Constants.GEOFENCE_DATA_ITEM_URI;
import static com.appontherocks.soundprofile.Constants.KEY_GEOFENCE_ID;
import static com.appontherocks.soundprofile.Constants.TAG;

/**
 * Listens for geofence transition changes.
 */
public class GeofenceTransitionsIntentService extends IntentService
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    ArrayList mKeys;

    private final int STEP_ONE_COMPLETE = 0;
    private static final int STEP_TWO_COMPLETE = 1;

    int geofenceTransition;
    List triggeringGeofences;

    public GeofenceTransitionsIntentService() {
        super(GeofenceTransitionsIntentService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }


    /**
     * Handles incoming intents.
     *
     * @param intent The Intent sent by Location Services. This Intent is provided to Location
     *               Services (inside a PendingIntent) when addGeofences() is called.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            int errorMessage = geofencingEvent.getErrorCode();
            Log.e(TAG, errorMessage + "");
            return;
        }


        // Get the transition type.
        geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            fetchmKeys();
            // Get the transition details as a String.
            //getGeofenceTransitionDetails(geofenceTransition, triggeringGeofences);

        } else {
            // Log the error.
            /*Log.e(TAG, getString(R.string.geofence_transition_invalid_type,
                    geofenceTransition));*/
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STEP_ONE_COMPLETE:
                    getGeofenceTransitionDetails(geofenceTransition, triggeringGeofences);
                    break;
            }
        }
    };

    private void fetchmKeys() {
        Thread backgroundThread = new Thread() {
            @Override
            public void run() {
                FirebaseDatabase.getInstance().getReference().child("profiles").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mKeys = new ArrayList();
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            mKeys.add(ds.getValue(SoundProfile.class).mKey);
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
            }
        };
        backgroundThread.start();

    }

    private void getGeofenceTransitionDetails(
            final int geofenceTransition,
            List<Geofence> triggeringGeofences) {
        final AudioManager mobilemode = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

        // Get the Ids of each geofence that was triggered.
        for (final Geofence geofence : triggeringGeofences) {
            if (checkGeoFenseExists(geofence.getRequestId())) {
                FirebaseDatabase.getInstance().getReference().child("profiles").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                            if (dataSnapshot.child((geofence.getRequestId() + "")).child("notificationVolume").getValue() != null) {
                                mobilemode.setStreamVolume(AudioManager.STREAM_RING, Integer.parseInt(dataSnapshot.child((geofence.getRequestId() + "")).child("notificationVolume").getValue() + ""), 0);
                            }
                            if (dataSnapshot.child((geofence.getRequestId() + "")).child("musicVolume").getValue() != null) {
                                mobilemode.setStreamVolume(AudioManager.STREAM_MUSIC, Integer.parseInt(dataSnapshot.child((geofence.getRequestId() + "")).child("musicVolume").getValue() + ""), 0);
                            }
                            if (dataSnapshot.child((geofence.getRequestId() + "")).child("alarmVolume").getValue() != null) {
                                mobilemode.setStreamVolume(AudioManager.STREAM_ALARM, Integer.parseInt(dataSnapshot.child((geofence.getRequestId() + "")).child("alarmVolume").getValue() + ""), 0);
                            }
                            if (dataSnapshot.child((geofence.getRequestId() + "")).child("callVolume").getValue() != null) {
                                mobilemode.setStreamVolume(AudioManager.STREAM_VOICE_CALL, Integer.parseInt(dataSnapshot.child((geofence.getRequestId() + "")).child("alarmVolume").getValue() + ""), 0);
                            }
/*                            if (dataSnapshot.child((geofence.getRequestId() + "")).child("ringToneURI").getValue() != null)
                                RingtoneManager.setActualDefaultRingtoneUri(
                                        GeofenceTransitionsIntentService.this,
                                        RingtoneManager.TYPE_RINGTONE,
                                        Uri.parse(dataSnapshot.child((geofence.getRequestId() + "")).child("ringToneURI").getValue() + "")
                                );*/
                        } else {
                            if (dataSnapshot.child("default").child("notificationVolume").getValue() != null) {
                                mobilemode.setStreamVolume(AudioManager.STREAM_RING, Integer.parseInt(dataSnapshot.child("default").child("notificationVolume").getValue() + ""), 0);
                            }
                            if (dataSnapshot.child("default").child("musicVolume").getValue() != null) {
                                mobilemode.setStreamVolume(AudioManager.STREAM_MUSIC, Integer.parseInt(dataSnapshot.child("default").child("musicVolume").getValue() + ""), 0);
                            }
                            if (dataSnapshot.child("default").child("alarmVolume").getValue() != null) {
                                mobilemode.setStreamVolume(AudioManager.STREAM_ALARM, Integer.parseInt(dataSnapshot.child("default").child("alarmVolume").getValue() + ""), 0);
                            }
                            if (dataSnapshot.child("default").child("callVolume").getValue() != null) {
                                mobilemode.setStreamVolume(AudioManager.STREAM_VOICE_CALL, Integer.parseInt(dataSnapshot.child("default").child("alarmVolume").getValue() + ""), 0);
                            }
/*                            if (dataSnapshot.child("default").child("ringToneURI").getValue() != null)
                                RingtoneManager.setActualDefaultRingtoneUri(
                                        GeofenceTransitionsIntentService.this,
                                        RingtoneManager.TYPE_RINGTONE,
                                        Uri.parse(dataSnapshot.child((geofence.getRequestId() + "")).child("ringToneURI").getValue() + "")
                                );*/
                        }
                        // Send notification and log the transition details.
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
            ShowLocalNotification(getTransitionString(geofenceTransition));
        }

    }

    private boolean checkGeoFenseExists(String triggeredGeoFenceID) {

        boolean isExists = false;
        for (int i = 0; i < mKeys.size(); i++) {
            if ((mKeys.get(i) + "").equals((triggeredGeoFenceID + ""))) {
                isExists = true;
            }
        }
        return isExists;
    }

    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return getString(R.string.entering_geofence);
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return getString(R.string.exiting_geofence);
            default:
                return getString(R.string.unknown_geofence_transition);
        }
    }

    public void ShowLocalNotification(String msg) {


        Intent notificationIntent = new Intent();
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // stackBuilder.addParentStack(NotificationActivity.class);
        stackBuilder.addNextIntent(notificationIntent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.getNotification().flags |= Notification.FLAG_AUTO_CANCEL;

        Notification notification = builder.setContentTitle(this.getResources().getString(R.string.app_name))
                .setContentText(msg)
                .setTicker(msg)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent).build();



        /*final Notification.Builder builder = new Notification.Builder(this);
        builder.setStyle(new Notification.BigTextStyle(builder)
                .bigText(msg)
                .setBigContentTitle(this.getResources().getString(R.string.app_name))
                );
        builder.setSmallIcon(R.mipmap.ic_launcher);*/
        //.setSummaryText("Big summary"))
        //.setContentTitle("Title")
        //.setContentText("Summary")


        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }

    /**
     * Showing a toast message, using the Main thread
     */
    private void showToast(final Context context, final int resourceId) {
        Handler mainThread = new Handler(Looper.getMainLooper());
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, context.getString(resourceId), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onConnected(Bundle connectionHint) {
    }

    @Override
    public void onConnectionSuspended(int cause) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
    }

}

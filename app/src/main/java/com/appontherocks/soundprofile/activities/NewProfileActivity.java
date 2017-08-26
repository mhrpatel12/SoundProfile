package com.appontherocks.soundprofile.activities;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.appontherocks.soundprofile.R;
import com.appontherocks.soundprofile.Utility.Constants;
import com.appontherocks.soundprofile.service.GeofenceTransitionsIntentService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class NewProfileActivity extends BaseActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    final int RQS_RINGTONEPICKER = 1;
    final int REQUEST_CODE_MAP_ACTIVITY = 99;
    TextView textviewRingerVolume, textViewMediaVolume, textviewAlarmVolume, textviewCallVolume, textViewNotificationVolume, textViewSystemVolume;
    SeekBar seekbarRingerVolume, seekBarMediaVolume, seekBarAlarmVolume, seekBarCallVolume, seekBarNotificationVolume, seekBarSystenVolume;
    AppCompatCheckBox chkRingerVolume, chkMediaVolume, chkAlarmVolume, chkCallVolume, chkNotificationVolume, chkSystemVolume;
    EditText edtProfileName;
    List<Geofence> mGeofenceList;
    ImageButton btnChangeRingtone, btnChangeNotificationtone;
    Ringtone ringTone;
    LatLng latLng;
    Uri defaultRintoneUri;
    Ringtone defaultRingtone;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private String mKey;
    private ProgressDialog pDialog;
    private Uri uri;

    private TextView txtWifiSetting, txtBluetoothSetting;
    private TextView txtWifiSettingValue, txtBluetoothSettingValue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_profile);

        defaultRintoneUri = RingtoneManager.getActualDefaultRingtoneUri(this.getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
        defaultRingtone = RingtoneManager.getRingtone(this, defaultRintoneUri);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        mGeofenceList = new ArrayList<Geofence>();

        mKey = getIntent().getStringExtra("key");

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        seekbarRingerVolume = (SeekBar) findViewById(R.id.seekBarRingerVolume);
        textviewRingerVolume = (TextView) findViewById(R.id.txtViewRingerVolume);
        chkRingerVolume = (AppCompatCheckBox) findViewById(R.id.chkRingerVolume);
        chkRingerVolume.setOnCheckedChangeListener(new MyCheckedChangeListener(1));

        seekBarMediaVolume = (SeekBar) findViewById(R.id.seekBarMediaVolume);
        textViewMediaVolume = (TextView) findViewById(R.id.txtViewMediaVolume);
        chkMediaVolume = (AppCompatCheckBox) findViewById(R.id.chkMediaVolume);
        chkMediaVolume.setOnCheckedChangeListener(new MyCheckedChangeListener(2));

        seekBarAlarmVolume = (SeekBar) findViewById(R.id.seekBarAlarmVolume);
        textviewAlarmVolume = (TextView) findViewById(R.id.txtViewAlarmVolume);
        chkAlarmVolume = (AppCompatCheckBox) findViewById(R.id.chkAlarmVolume);
        chkAlarmVolume.setOnCheckedChangeListener(new MyCheckedChangeListener(3));

        seekBarCallVolume = (SeekBar) findViewById(R.id.seekBarCallVolume);
        textviewCallVolume = (TextView) findViewById(R.id.txtCallVolume);
        chkCallVolume = (AppCompatCheckBox) findViewById(R.id.chkCallVolume);
        chkCallVolume.setOnCheckedChangeListener(new MyCheckedChangeListener(4));

        seekBarNotificationVolume = (SeekBar) findViewById(R.id.seekBarNotificationVolume);
        textViewNotificationVolume = (TextView) findViewById(R.id.txtViewNotificationVolume);
        chkNotificationVolume = (AppCompatCheckBox) findViewById(R.id.chkNotificationVolume);
        chkNotificationVolume.setOnCheckedChangeListener(new MyCheckedChangeListener(5));

        seekBarSystenVolume = (SeekBar) findViewById(R.id.seekBarSystemVolume);
        textViewSystemVolume = (TextView) findViewById(R.id.txtViewSystemVolume);
        chkSystemVolume = (AppCompatCheckBox) findViewById(R.id.chkSystemVolume);
        chkSystemVolume.setOnCheckedChangeListener(new MyCheckedChangeListener(6));

        edtProfileName = (EditText) findViewById(R.id.edtProfileName);

        btnChangeRingtone = (ImageButton) findViewById(R.id.btnChangeRingTone);
        btnChangeNotificationtone = (ImageButton) findViewById(R.id.btnChangeNotificationTone);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    buildAlertMessageNoGps();
                } else {
                    Intent intent = new Intent(NewProfileActivity.this, MapActivity.class);
                    intent.putExtra("key", mKey);
                    startActivityForResult(intent, REQUEST_CODE_MAP_ACTIVITY);
                }
            }
        });

        findViewById(R.id.fabSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new saveGeoFerncesAyncTask().execute();
            }
        });

        AudioManager mobilemode = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

        seekbarRingerVolume.setMax(mobilemode.getStreamMaxVolume(AudioManager.STREAM_MUSIC));

        seekbarRingerVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                textviewRingerVolume.setText(i + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarMediaVolume.setMax(mobilemode.getStreamMaxVolume(AudioManager.STREAM_MUSIC));

        seekBarMediaVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                textViewMediaVolume.setText(i + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarAlarmVolume.setMax(mobilemode.getStreamMaxVolume(AudioManager.STREAM_MUSIC));

        seekBarAlarmVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                textviewAlarmVolume.setText(i + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarCallVolume.setMax(mobilemode.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL));

        seekBarCallVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                textviewCallVolume.setText(i + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarNotificationVolume.setMax(mobilemode.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION));

        seekBarNotificationVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                textViewNotificationVolume.setText(i + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarSystenVolume.setMax(mobilemode.getStreamMaxVolume(AudioManager.STREAM_SYSTEM));

        seekBarSystenVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                textViewSystemVolume.setText(i + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        btnChangeRingtone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
                startActivityForResult(intent, RQS_RINGTONEPICKER);
            }
        });

        btnChangeNotificationtone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                startActivityForResult(intent, RQS_RINGTONEPICKER);
            }
        });

        txtWifiSetting = (TextView) findViewById(R.id.txtWifiSetting);
        txtWifiSettingValue = (TextView) findViewById(R.id.txtWifiStatus);
        txtWifiSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(NewProfileActivity.this, v);
                popup.getMenuInflater().inflate(R.menu.menu_wifi_bluetooth, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        txtWifiSettingValue.setText(item.getTitle());
                        return true;
                    }
                });
                popup.show();
            }
        });

        txtBluetoothSetting = (TextView) findViewById(R.id.txtBluetoothSetting);
        txtBluetoothSettingValue = (TextView) findViewById(R.id.txtBluetoothStatus);
        txtBluetoothSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(NewProfileActivity.this, v);
                popup.getMenuInflater().inflate(R.menu.menu_wifi_bluetooth, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        txtBluetoothSettingValue.setText(item.getTitle());
                        return true;
                    }
                });
                popup.show();
            }
        });
    }

    @Override
    public void onResult(@NonNull Status status) {

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        showDismissWarning();
    }

    public void showDismissWarning() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.prompt_discard_profile_changes))
                .setCancelable(true)
                .setPositiveButton("DISCARD", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        FirebaseDatabase.getInstance().getReference().child("profiles").child(getUid()).child(mKey).removeValue();
                        NewProfileActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        SharedPreferences prefs = getSharedPreferences(getString(R.string.theme), MODE_PRIVATE);
        String selectedTheme = prefs.getString(getString(R.string.theme), getString(R.string.theme_dark));
        if (selectedTheme.equals(getString(R.string.theme_dark))) {
            MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(NewProfileActivity.this, R.raw.google_maps_night_mode);
            mMap.setMapStyle(style);
        }
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            //FirebaseDatabase.getInstance().getReference().child("profiles").child(getUid()).child(mKey).child("latitude").setValue((mLastLocation.getLatitude() + ""));
            //FirebaseDatabase.getInstance().getReference().child("profiles").child(getUid()).child(mKey).child("longitude").setValue((mLastLocation.getLongitude() + ""));

            LatLng ltLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            CameraUpdate center = CameraUpdateFactory.newLatLngZoom(ltLng, 15);
            latLng = ltLng;
            mMap.addMarker(new MarkerOptions().position(ltLng).title("Sydney").draggable(true));
            mMap.moveCamera(center);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RQS_RINGTONEPICKER:
                    uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    ringTone = RingtoneManager.getRingtone(getApplicationContext(), uri);
                    Toast.makeText(NewProfileActivity.this,
                            ringTone.getTitle(NewProfileActivity.this),
                            Toast.LENGTH_LONG).show();
                    break;
                case REQUEST_CODE_MAP_ACTIVITY:
                    if (data.getStringExtra("lat") != null && (data.getStringExtra("lng") != null)) {
                        try {
                            latLng = new LatLng(Double.parseDouble(data.getStringExtra("lat") + ""), Double.parseDouble(data.getStringExtra("lng") + ""));
                        } catch (NumberFormatException e) {
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
    }

    public class MyCheckedChangeListener implements CompoundButton.OnCheckedChangeListener {
        int position;

        public MyCheckedChangeListener(int position) {
            this.position = position;
        }

        private void changeVisibility(SeekBar seekBar, boolean isChecked) {
            if (isChecked) {
                seekBar.setEnabled(true);
            } else {
                seekBar.setEnabled(false);
            }

        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (position) {
                case 1:
                    FirebaseDatabase.getInstance().getReference().child("profiles").child(getUid()).child(mKey).child("chkRinger").setValue(isChecked);
                    changeVisibility(seekbarRingerVolume, isChecked);
                    break;
                case 2:
                    FirebaseDatabase.getInstance().getReference().child("profiles").child(getUid()).child(mKey).child("chkMedia").setValue(isChecked);
                    changeVisibility(seekBarMediaVolume, isChecked);
                    break;
                case 3:
                    FirebaseDatabase.getInstance().getReference().child("profiles").child(getUid()).child(mKey).child("chkAlarm").setValue(isChecked);
                    changeVisibility(seekBarAlarmVolume, isChecked);
                    break;
                case 4:
                    FirebaseDatabase.getInstance().getReference().child("profiles").child(getUid()).child(mKey).child("chkCall").setValue(isChecked);
                    changeVisibility(seekBarCallVolume, isChecked);
                    break;
            }
        }
    }

    public class saveGeoFerncesAyncTask extends AsyncTask<Void, String, String> {

        private String profileName;

        private String ringerVolume;
        private String mediaVolume;
        private String alarmVolume;
        private String callVolume;
        private String notificationVolume;
        private String systemVolume;

        private Boolean isRingToneChecked;
        private Boolean isMediaChecked;
        private Boolean isAlarmChecked;
        private Boolean isCallChecked;
        private Boolean isNotificationChecked;
        private Boolean isSystemChecked;

        private String wifiSetting;
        private String bluetoothSetting;

        private DatabaseReference mProfileReference;

        /**
         * Before starting background thread
         * Show Progress Bar Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(NewProfileActivity.this);
            pDialog.setMessage("Loading...");
            pDialog.setCancelable(false);
            pDialog.show();

            profileName = (edtProfileName.getText() + "").trim();

            ringerVolume = seekbarRingerVolume.getProgress() + "";
            mediaVolume = seekBarMediaVolume.getProgress() + "";
            alarmVolume = seekBarAlarmVolume.getProgress() + "";
            callVolume = seekBarCallVolume.getProgress() + "";
            notificationVolume = seekBarNotificationVolume.getProgress() + "";
            systemVolume = seekBarSystenVolume.getProgress() + "";

            isRingToneChecked = chkRingerVolume.isChecked();
            isMediaChecked = chkMediaVolume.isChecked();
            isAlarmChecked = chkAlarmVolume.isChecked();
            isCallChecked = chkCallVolume.isChecked();
            isNotificationChecked = chkNotificationVolume.isChecked();
            isSystemChecked = chkSystemVolume.isChecked();

            wifiSetting = txtWifiSettingValue.getText().toString();
            bluetoothSetting = txtBluetoothSettingValue.getText().toString();

            mProfileReference = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_profiles)).child(getUid()).child(mKey);
        }

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(Void... f_url) {
            if (ActivityCompat.checkSelfPermission(NewProfileActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(NewProfileActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return null;
            }

            mProfileReference.child(getString(R.string.firebase_profile_name)).setValue((profileName + ""));

            mProfileReference.child(getString(R.string.firebase_profile_latitude)).setValue((latLng.latitude + ""));
            mProfileReference.child(getString(R.string.firebase_profile_longitude)).setValue((latLng.longitude + ""));

            mProfileReference.child(getString(R.string.firebase_profile_ringtone_volume)).setValue(ringerVolume);
            mProfileReference.child(getString(R.string.firebase_profile_music_volume)).setValue(mediaVolume);
            mProfileReference.child(getString(R.string.firebase_profile_alarm_volume)).setValue(alarmVolume);
            mProfileReference.child(getString(R.string.firebase_profile_call_volume)).setValue(callVolume);
            mProfileReference.child(getString(R.string.firebase_profile_notification_volume)).setValue(notificationVolume);
            mProfileReference.child(getString(R.string.firebase_profile_system_volume)).setValue(systemVolume);

            mProfileReference.child(getString(R.string.firebase_profile_ringtone_chk)).setValue(isRingToneChecked);
            mProfileReference.child(getString(R.string.firebase_profile_music_chk)).setValue(isMediaChecked);
            mProfileReference.child(getString(R.string.firebase_profile_alarm_chk)).setValue(isAlarmChecked);
            mProfileReference.child(getString(R.string.firebase_profile_call_chl)).setValue(isCallChecked);
            mProfileReference.child(getString(R.string.firebase_profile_notification_chk)).setValue(isNotificationChecked);
            mProfileReference.child(getString(R.string.firebase_profile_system_chl)).setValue(isSystemChecked);

            mProfileReference.child(getString(R.string.firebase_profile_wifi_setting)).setValue(wifiSetting);
            mProfileReference.child(getString(R.string.firebase_profile_bluetooth_setting)).setValue(bluetoothSetting);

            mProfileReference.child(getString(R.string.firebase_profile_ringtone_uri)).setValue(uri + "");

            mGeofenceList.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(mKey)
                    .setCircularRegion(
                            latLng.latitude,
                            latLng.longitude,
                            50
                    )
                    .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_TIME)
                    .setNotificationResponsiveness(Constants.GEOFENCE_NOTIFICATION_RESPONSIVENESS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build());

            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent()
            ).setResultCallback(NewProfileActivity.this);

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            pDialog.dismiss();
            finish();
        }
    }

}

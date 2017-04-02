package com.appontherocks.soundprofile;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.appontherocks.soundprofile.models.SoundProfile;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private MapFragment mapFragment;

    TextView textviewRingerVolume, textViewMediaVolume, textviewAlarmVolume, textviewCallVolume;
    SeekBar seekbarRingerVolume, seekBarMediaVolume, seekBarAlarmVolume, seekBarCallVolume;
    AppCompatCheckBox chkRingerVolume, chkMediaVolume, chkAlarmVolume, chkCallVolume;

    EditText edtProfileName;

    ImageButton btnChangeRingtone, btnChangeNotificationtone;

    final int RQS_RINGTONEPICKER = 1;

    LatLng latLng;

    private ProgressDialog pDialog;

    private static final String TAG = "MainActivty";

    private String mKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_profile);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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
                    Intent intent = new Intent(MainActivity.this, MapActivity.class);
                    intent.putExtra("key", mKey);
                    intent.putExtra("lat", getIntent().getStringExtra("lat"));
                    intent.putExtra("lng", getIntent().getStringExtra("lng"));
                    startActivity(intent);
                }
            }
        });

        edtProfileName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                FirebaseDatabase.getInstance().getReference().child("profiles").child(getUid()).child(mKey).child("profileName").setValue(charSequence + "");
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mKey = getIntent().getStringExtra("key");
        if (getIntent().getStringExtra("lat") != null && (getIntent().getStringExtra("lng") != null)) {
            try {
                latLng = new LatLng(Double.parseDouble(getIntent().getStringExtra("lat") + ""), Double.parseDouble(getIntent().getStringExtra("lng") + ""));
            } catch (NumberFormatException e) {
            }
        }

        AudioManager mobilemode = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

        seekbarRingerVolume.setMax(mobilemode.getStreamMaxVolume(AudioManager.STREAM_MUSIC));

        seekbarRingerVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                textviewRingerVolume.setText(i + "");
                FirebaseDatabase.getInstance().getReference().child("profiles").child(getUid()).child(mKey).child("notificationVolume").setValue(seekbarRingerVolume.getProgress() + "");
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
                FirebaseDatabase.getInstance().getReference().child("profiles").child(getUid()).child(mKey).child("musicVolume").setValue(seekBarMediaVolume.getProgress() + "");
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
                FirebaseDatabase.getInstance().getReference().child("profiles").child(getUid()).child(mKey).child("alarmVolume").setValue(seekBarAlarmVolume.getProgress() + "");
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
                FirebaseDatabase.getInstance().getReference().child("profiles").child(getUid()).child(mKey).child("callVolume").setValue(seekBarCallVolume.getProgress() + "");
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

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        new loadDataAyncTask().execute();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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

        if (latLng != null) {
            CameraUpdate center = CameraUpdateFactory.newLatLngZoom(latLng, 15);
            mMap.addMarker(new MarkerOptions().position(latLng).draggable(true));
            mMap.moveCamera(center);
        } else if (mLastLocation != null) {
            latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            CameraUpdate center = CameraUpdateFactory.newLatLngZoom(latLng, 15);
            mMap.addMarker(new MarkerOptions().position(latLng).draggable(true));
            mMap.moveCamera(center);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mGoogleApiClient.connect();
    }

    public class loadDataAyncTask extends AsyncTask<Void, String, String> {

        /**
         * Before starting background thread
         * Show Progress Bar Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Loading...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(Void... f_url) {
            FirebaseDatabase.getInstance().getReference().child("profiles").child(getUid()).child(mKey).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    SoundProfile profile = dataSnapshot.getValue(SoundProfile.class);

                    if (profile != null) {
                        if (profile.profileName != null) {
                            edtProfileName.setText(profile.profileName + "");
                        }

                        if ((profile.notificationVolume != null)) {
                            seekbarRingerVolume.setProgress(Integer.parseInt(profile.notificationVolume));
                            seekBarMediaVolume.setProgress(Integer.parseInt(profile.musicVolume));
                            seekBarAlarmVolume.setProgress(Integer.parseInt(profile.alarmVolume));
                            seekBarCallVolume.setProgress(Integer.parseInt(profile.callVolume));
                        }

                        if (((profile.latitude != null) && !(profile.latitude.equals(""))) &&
                                ((profile.longitude != null) && !(profile.longitude.equals("")))) {
                            latLng = new LatLng(Double.parseDouble(profile.latitude + ""), Double.parseDouble(profile.longitude + ""));
                            mapFragment.getMapAsync(new OnMapReadyCallback() {
                                @Override
                                public void onMapReady(GoogleMap googleMap) {
                                    mMap = googleMap;
                                    mGoogleApiClient.connect();
                                }
                            });
                        }
                        chkRingerVolume.setEnabled(profile.chkRinger);
                        chkMediaVolume.setEnabled(profile.chkMedia);
                        chkAlarmVolume.setEnabled(profile.chkAlarm);
                        chkCallVolume.setEnabled(profile.chkCall);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(MainActivity.this, "Failed to load data.",
                            Toast.LENGTH_SHORT).show();
                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            pDialog.dismiss();
        }
    }
}

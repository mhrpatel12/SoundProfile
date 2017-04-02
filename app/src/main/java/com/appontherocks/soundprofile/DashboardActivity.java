package com.appontherocks.soundprofile;

import android.*;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.appontherocks.soundprofile.models.SoundProfile;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.URL;

public class DashboardActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    ImageView profilePicture;

    TextView txtDisplayName, txtEmail;

    TextView textviewRingerVolume, textViewMediaVolume, textviewAlarmVolume, textviewCallVolume;
    SeekBar seekbarRingerVolume, seekBarMediaVolume, seekBarAlarmVolume, seekBarCallVolume;
    AppCompatCheckBox chkRingerVolume, chkMediaVolume, chkAlarmVolume, chkCallVolume;

    ImageButton btnChangeRingtone, btnChangeNotificationtone;

    final int RQS_RINGTONEPICKER = 1;

    private static final String TAG = "Dashboard";

    private Uri defaultRintoneUri;

    private DatabaseReference mSoundProfileReference;

    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        profilePicture = (ImageView) headerView.findViewById(R.id.imgProfilePicture);
        txtDisplayName = (TextView) headerView.findViewById(R.id.txtUserName);
        txtEmail = (TextView) headerView.findViewById(R.id.txtEmail);

        defaultRintoneUri = RingtoneManager.getActualDefaultRingtoneUri(DashboardActivity.this.getApplicationContext(), RingtoneManager.TYPE_RINGTONE);

        if ((ContextCompat.checkSelfPermission(DashboardActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
                ||
                (ContextCompat.checkSelfPermission(DashboardActivity.this,
                        Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED)
                ||
                (ContextCompat.checkSelfPermission(DashboardActivity.this,
                        Manifest.permission.WRITE_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED)
                ||
                (ContextCompat.checkSelfPermission(DashboardActivity.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED)
                ||
                (ContextCompat.checkSelfPermission(DashboardActivity.this,
                        Manifest.permission.WRITE_SETTINGS)
                        != PackageManager.PERMISSION_GRANTED)
                ||
                (ContextCompat.checkSelfPermission(DashboardActivity.this,
                        Manifest.permission.CHANGE_CONFIGURATION)
                        != PackageManager.PERMISSION_GRANTED)
                ||
                (ContextCompat.checkSelfPermission(DashboardActivity.this,
                        Manifest.permission.MODIFY_AUDIO_SETTINGS)
                        != PackageManager.PERMISSION_GRANTED)
                ||
                (ContextCompat.checkSelfPermission(DashboardActivity.this,
                        Manifest.permission.RECEIVE_BOOT_COMPLETED)
                        != PackageManager.PERMISSION_GRANTED)) {

            // Should we show an explanation?
            if ((ActivityCompat.shouldShowRequestPermissionRationale(DashboardActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION))
                    &&
                    (ActivityCompat.shouldShowRequestPermissionRationale(DashboardActivity.this,
                            Manifest.permission.READ_CONTACTS))
                    &&
                    (ActivityCompat.shouldShowRequestPermissionRationale(DashboardActivity.this,
                            Manifest.permission.WRITE_CONTACTS))
                    &&
                    (ActivityCompat.shouldShowRequestPermissionRationale(DashboardActivity.this,
                            Manifest.permission.ACCESS_COARSE_LOCATION))
                    &&
                    (ActivityCompat.shouldShowRequestPermissionRationale(DashboardActivity.this,
                            Manifest.permission.WRITE_SETTINGS))
                    &&
                    (ActivityCompat.shouldShowRequestPermissionRationale(DashboardActivity.this,
                            Manifest.permission.CHANGE_CONFIGURATION))
                    &&
                    (ActivityCompat.shouldShowRequestPermissionRationale(DashboardActivity.this,
                            Manifest.permission.MODIFY_AUDIO_SETTINGS))
                    &&
                    (ActivityCompat.shouldShowRequestPermissionRationale(DashboardActivity.this,
                            Manifest.permission.RECEIVE_BOOT_COMPLETED))) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(DashboardActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_SETTINGS, Manifest.permission.CHANGE_CONFIGURATION, Manifest.permission.MODIFY_AUDIO_SETTINGS, Manifest.permission.RECEIVE_BOOT_COMPLETED},
                        6);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        if (getDP() != null) {
            try {
                new downloadProfilePicture().execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (getDisplayName() != null) {
            txtDisplayName.setText(getDisplayName() + "");
        }
        if (getEmail() != null) {
            txtEmail.setText(getEmail() + "");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // Initialize Database
        mSoundProfileReference = FirebaseDatabase.getInstance().getReference();

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
        btnChangeRingtone = (ImageButton) findViewById(R.id.btnChangeRingTone);
        btnChangeNotificationtone = (ImageButton) findViewById(R.id.btnChangeNotificationTone);

        findViewById(R.id.layout_profile_name).setVisibility(View.GONE);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    buildAlertMessageNoGps();
                } else {
                    SoundProfile profile = new SoundProfile("New Profile", true, true, true, true, "0", "0", "0", "0", "", "", defaultRintoneUri + "");
                    String key = mSoundProfileReference.child("profiles").child(getUid()).push().getKey();
                    mSoundProfileReference.child("profiles").child(getUid()).child(key).setValue(profile);
                    mSoundProfileReference.child("profiles").child(getUid()).child(key).child("mKey").setValue(key + "");
                    Intent intent = new Intent(DashboardActivity.this, NewProfileActivity.class);
                    intent.putExtra("key", key + "");
                    startActivity(intent);
                }
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
                    FirebaseDatabase.getInstance().getReference().child("profiles").child(getUid()).child("default").child("chkRinger").setValue(isChecked);
                    changeVisibility(seekbarRingerVolume, isChecked);
                    break;
                case 2:
                    FirebaseDatabase.getInstance().getReference().child("profiles").child(getUid()).child("default").child("chkMedia").setValue(isChecked);
                    changeVisibility(seekBarMediaVolume, isChecked);
                    break;
                case 3:
                    FirebaseDatabase.getInstance().getReference().child("profiles").child(getUid()).child("default").child("chkAlarm").setValue(isChecked);
                    changeVisibility(seekBarAlarmVolume, isChecked);
                    break;
                case 4:
                    FirebaseDatabase.getInstance().getReference().child("profiles").child(getUid()).child("default").child("chkCall").setValue(isChecked);
                    changeVisibility(seekBarCallVolume, isChecked);
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
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

    public class saveDataAyncTask extends AsyncTask<Void, String, String> {

        String ringerVolume = seekbarRingerVolume.getProgress() + "";
        String mediaVolume = seekBarMediaVolume.getProgress() + "";
        String alarmVolume = seekBarAlarmVolume.getProgress() + "";
        String callVolume = seekBarCallVolume.getProgress() + "";

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
            FirebaseDatabase.getInstance().getReference().child("profiles").child(getUid()).child("default").child("notificationVolume").setValue(ringerVolume);
            FirebaseDatabase.getInstance().getReference().child("profiles").child(getUid()).child("default").child("musicVolume").setValue(mediaVolume);
            FirebaseDatabase.getInstance().getReference().child("profiles").child(getUid()).child("default").child("alarmVolume").setValue(alarmVolume);
            FirebaseDatabase.getInstance().getReference().child("profiles").child(getUid()).child("default").child("callVolume").setValue(callVolume);
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Intent intent = new Intent(DashboardActivity.this, AuthenticationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("EXIT", true);
            startActivity(intent);
        }
    }

    public class loadDataAyncTask extends AsyncTask<Void, String, String> {

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
            FirebaseDatabase.getInstance().getReference().child("profiles").child(getUid()).child("default").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    SoundProfile profile = dataSnapshot.getValue(SoundProfile.class);

                    if (profile != null) {

                        if ((profile.notificationVolume != null)) {
                            seekbarRingerVolume.setProgress(Integer.parseInt(profile.notificationVolume));
                            seekBarMediaVolume.setProgress(Integer.parseInt(profile.musicVolume));
                            seekBarAlarmVolume.setProgress(Integer.parseInt(profile.alarmVolume));
                            seekBarCallVolume.setProgress(Integer.parseInt(profile.callVolume));
                        }

                        chkRingerVolume.setEnabled(profile.chkRinger);
                        chkMediaVolume.setEnabled(profile.chkMedia);
                        chkAlarmVolume.setEnabled(profile.chkAlarm);
                        chkCallVolume.setEnabled(profile.chkCall);
                    } else {
                        profile = new SoundProfile("Default Profile", true, true, true, true, "0", "0", "0", "0", "", "", defaultRintoneUri + "");
                        mSoundProfileReference.child("profiles").child(getUid()).child("default").setValue(profile);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(DashboardActivity.this, "Failed to load data.",
                            Toast.LENGTH_SHORT).show();
                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
        }
    }

    private class downloadProfilePicture extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String[] params) {
            URL url = null;
            Bitmap myImg = null;
            try {
                url = new URL(getDP() + "");
                myImg = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return myImg;
        }

        @Override
        protected void onPostExecute(Bitmap image) {
            profilePicture.setImageBitmap(image);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //super.onBackPressed();
            new saveDataAyncTask().execute();
/*
            Intent intent = new Intent(DashboardActivity.this, AuthenticationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("EXIT", true);
            startActivity(intent);
*/
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dashboard, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profiles) {
            Intent intent = new Intent(DashboardActivity.this, ProfilesActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}

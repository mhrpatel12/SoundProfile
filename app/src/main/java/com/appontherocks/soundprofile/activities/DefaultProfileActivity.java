package com.appontherocks.soundprofile.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.appontherocks.soundprofile.R;
import com.appontherocks.soundprofile.models.SoundProfile;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DefaultProfileActivity extends BaseActivity {

    private static final String TAG = "Default Profile Activity";
    final int RQS_RINGTONEPICKER = 1;
    TextView textviewRingerVolume, textViewMediaVolume, textviewAlarmVolume, textviewCallVolume;
    SeekBar seekbarRingerVolume, seekBarMediaVolume, seekBarAlarmVolume, seekBarCallVolume;
    AppCompatCheckBox chkRingerVolume, chkMediaVolume, chkAlarmVolume, chkCallVolume;
    AppCompatCheckBox chkDefaultProfile;
    ImageButton btnChangeRingtone, btnChangeNotificationtone;
    Ringtone ringTone;
    Uri uri;
    private Uri defaultRintoneUri;
    private Uri defaultNotificationToneUri;
    private DatabaseReference mSoundProfileReference;
    private ProgressDialog pDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        defaultRintoneUri = RingtoneManager.getActualDefaultRingtoneUri(DefaultProfileActivity.this.getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
        defaultNotificationToneUri = RingtoneManager.getActualDefaultRingtoneUri(DefaultProfileActivity.this.getApplicationContext(), RingtoneManager.TYPE_NOTIFICATION);

        initializeViews();
    }

    private void initializeViews() {
        // Initialize Database
        mSoundProfileReference = FirebaseDatabase.getInstance().getReference();

        findViewById(R.id.layout_default_profile).setVisibility(View.VISIBLE);

        chkDefaultProfile = (AppCompatCheckBox) findViewById(R.id.chkDefaultProfile);

        seekbarRingerVolume = (SeekBar) findViewById(R.id.seekBarRingerVolume);
        textviewRingerVolume = (TextView) findViewById(R.id.txtViewRingerVolume);
        chkRingerVolume = (AppCompatCheckBox) findViewById(R.id.chkRingerVolume);
        chkRingerVolume.setOnCheckedChangeListener(new DefaultProfileActivity.MyCheckedChangeListener(1));
        seekBarMediaVolume = (SeekBar) findViewById(R.id.seekBarMediaVolume);
        textViewMediaVolume = (TextView) findViewById(R.id.txtViewMediaVolume);
        chkMediaVolume = (AppCompatCheckBox) findViewById(R.id.chkMediaVolume);
        chkMediaVolume.setOnCheckedChangeListener(new DefaultProfileActivity.MyCheckedChangeListener(2));

        seekBarAlarmVolume = (SeekBar) findViewById(R.id.seekBarAlarmVolume);
        textviewAlarmVolume = (TextView) findViewById(R.id.txtViewAlarmVolume);
        chkAlarmVolume = (AppCompatCheckBox) findViewById(R.id.chkAlarmVolume);
        chkAlarmVolume.setOnCheckedChangeListener(new DefaultProfileActivity.MyCheckedChangeListener(3));

        seekBarCallVolume = (SeekBar) findViewById(R.id.seekBarCallVolume);
        textviewCallVolume = (TextView) findViewById(R.id.txtCallVolume);
        chkCallVolume = (AppCompatCheckBox) findViewById(R.id.chkCallVolume);
        chkCallVolume.setOnCheckedChangeListener(new DefaultProfileActivity.MyCheckedChangeListener(4));
        btnChangeRingtone = (ImageButton) findViewById(R.id.btnChangeRingTone);
        btnChangeNotificationtone = (ImageButton) findViewById(R.id.btnChangeNotificationTone);

        findViewById(R.id.layout_profile_name).setVisibility(View.GONE);

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
        builder.setMessage(getResources().getString(R.string.prompt_save_default_profile_changes))
                .setCancelable(true)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        new saveGeoFerncesAyncTask().execute();
                        DefaultProfileActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("Discard", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                        DefaultProfileActivity.super.onBackPressed();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RQS_RINGTONEPICKER && resultCode == RESULT_OK) {
            uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            ringTone = RingtoneManager.getRingtone(getApplicationContext(), uri);
            Toast.makeText(DefaultProfileActivity.this,
                    ringTone.getTitle(DefaultProfileActivity.this),
                    Toast.LENGTH_LONG).show();
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

    public class loadDataAyncTask extends AsyncTask<Void, String, String> {

        /**
         * Before starting background thread
         * Show Progress Bar Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(DefaultProfileActivity.this);
            pDialog.setMessage("Loading...");
            pDialog.setCancelable(false);
            pDialog.show();
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

                        if ((profile.ringtoneVolume != null)) {
                            seekbarRingerVolume.setProgress(Integer.parseInt(profile.ringtoneVolume));
                            seekBarMediaVolume.setProgress(Integer.parseInt(profile.musicVolume));
                            seekBarAlarmVolume.setProgress(Integer.parseInt(profile.alarmVolume));
                            seekBarCallVolume.setProgress(Integer.parseInt(profile.callVolume));
                        }

                        chkDefaultProfile.setChecked(profile.chkDefaultProfile);
                        chkRingerVolume.setChecked(profile.chkRinger);
                        chkMediaVolume.setChecked(profile.chkMedia);
                        chkAlarmVolume.setChecked(profile.chkAlarm);
                        chkCallVolume.setChecked(profile.chkCall);
                    } else {
                        profile = new SoundProfile("New Profile",//PROFILE NAME
                                false,//ACTIVE DEFAULT PROFILE ON UNKNOWN AREA ?
                                true, true, true, true, true, true, // DEFAULT VALUES FOR CHANGING SOUND SETTING
                                "0", "0", "0", "0", "0", "0", //DEFAULT VOLUME LEVEL
                                getString(R.string.title_no_change), getString(R.string.title_no_change), //DEFAULT STATE OF WIFI / BLUETOOTH
                                "", "", //BLANK LATITUDE & LONGITUDE
                                defaultRintoneUri + "", defaultNotificationToneUri + ""); //DEFAULT RINGTONE URI
                        mSoundProfileReference.child("profiles").child(getUid()).child("default").setValue(profile);
                    }
                    FirebaseDatabase.getInstance().getReference().child("profiles").child(getUid()).child("default").removeEventListener(this);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(DefaultProfileActivity.this, "Failed to load data.",
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

    public class saveGeoFerncesAyncTask extends AsyncTask<Void, String, String> {

        private String ringerVolume;
        private String mediaVolume;
        private String alarmVolume;
        private String callVolume;

        private Boolean isDefaultProfileChecked;
        private Boolean isRingToneChecked;
        private Boolean isMediaChecked;
        private Boolean isAlarmChecked;
        private Boolean isCallChecked;

        private DatabaseReference mProfileReference;

        /**
         * Before starting background thread
         * Show Progress Bar Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(DefaultProfileActivity.this);
            pDialog.setMessage("Loading...");
            pDialog.setCancelable(false);
            pDialog.show();

            ringerVolume = seekbarRingerVolume.getProgress() + "";
            mediaVolume = seekBarMediaVolume.getProgress() + "";
            alarmVolume = seekBarAlarmVolume.getProgress() + "";
            callVolume = seekBarCallVolume.getProgress() + "";

            isDefaultProfileChecked = chkDefaultProfile.isChecked();
            isRingToneChecked = chkRingerVolume.isChecked();
            isMediaChecked = chkMediaVolume.isChecked();
            isAlarmChecked = chkAlarmVolume.isChecked();
            isCallChecked = chkCallVolume.isChecked();

            mProfileReference = FirebaseDatabase.getInstance().getReference().child("profiles").child(getUid()).child("default");
        }

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(Void... f_url) {
            if (ActivityCompat.checkSelfPermission(DefaultProfileActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DefaultProfileActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return null;
            }

            mProfileReference.child("ringtoneVolume").setValue(ringerVolume);
            mProfileReference.child("musicVolume").setValue(mediaVolume);
            mProfileReference.child("alarmVolume").setValue(alarmVolume);
            mProfileReference.child("callVolume").setValue(callVolume);

            mProfileReference.child("chkDefaultProfile").setValue(isDefaultProfileChecked);
            mProfileReference.child("chkRinger").setValue(isRingToneChecked);
            mProfileReference.child("chkMedia").setValue(isMediaChecked);
            mProfileReference.child("chkAlarm").setValue(isAlarmChecked);
            mProfileReference.child("chkCall").setValue(isCallChecked);

            mProfileReference.child("ringToneURI").setValue(uri + "");

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            pDialog.dismiss();
            finish();
        }
    }
}

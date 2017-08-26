package com.appontherocks.soundprofile.fragments;


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
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.appontherocks.soundprofile.R;
import com.appontherocks.soundprofile.activities.DashboardActivity;
import com.appontherocks.soundprofile.interfaces.AlertForDiscardDefaultProfileChanges;
import com.appontherocks.soundprofile.models.SoundProfile;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class DefaultProfileFragment extends Fragment implements AlertForDiscardDefaultProfileChanges {

    private static final String TAG = "Default Profile Fragment";
    final int RQS_RINGTONEPICKER = 1;
    TextView textviewRingerVolume, textViewMediaVolume, textviewAlarmVolume, textviewCallVolume;
    SeekBar seekbarRingerVolume, seekBarMediaVolume, seekBarAlarmVolume, seekBarCallVolume;
    AppCompatCheckBox chkRingerVolume, chkMediaVolume, chkAlarmVolume, chkCallVolume;
    AppCompatCheckBox chkDefaultProfile;
    ImageButton btnChangeRingtone, btnChangeNotificationtone;
    Ringtone ringTone;
    Uri uri;
    private Uri defaultRintoneUri;
    private DatabaseReference mSoundProfileReference;
    private ProgressDialog pDialog;

    private Context mContext;
    private View view;


    public DefaultProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_default_profile, container, false);
        mContext = view.getContext();

        defaultRintoneUri = RingtoneManager.getActualDefaultRingtoneUri(mContext.getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
        initializeViews();

        return view;
    }

    private void initializeViews() {
        // Initialize Database
        mSoundProfileReference = FirebaseDatabase.getInstance().getReference();

        view.findViewById(R.id.layout_default_profile).setVisibility(View.VISIBLE);

        chkDefaultProfile = (AppCompatCheckBox) view.findViewById(R.id.chkDefaultProfile);

        seekbarRingerVolume = (SeekBar) view.findViewById(R.id.seekBarRingerVolume);
        textviewRingerVolume = (TextView) view.findViewById(R.id.txtViewRingerVolume);
        chkRingerVolume = (AppCompatCheckBox) view.findViewById(R.id.chkRingerVolume);
        chkRingerVolume.setOnCheckedChangeListener(new MyCheckedChangeListener(1));
        seekBarMediaVolume = (SeekBar) view.findViewById(R.id.seekBarMediaVolume);
        textViewMediaVolume = (TextView) view.findViewById(R.id.txtViewMediaVolume);
        chkMediaVolume = (AppCompatCheckBox) view.findViewById(R.id.chkMediaVolume);
        chkMediaVolume.setOnCheckedChangeListener(new MyCheckedChangeListener(2));

        seekBarAlarmVolume = (SeekBar) view.findViewById(R.id.seekBarAlarmVolume);
        textviewAlarmVolume = (TextView) view.findViewById(R.id.txtViewAlarmVolume);
        chkAlarmVolume = (AppCompatCheckBox) view.findViewById(R.id.chkAlarmVolume);
        chkAlarmVolume.setOnCheckedChangeListener(new MyCheckedChangeListener(3));

        seekBarCallVolume = (SeekBar) view.findViewById(R.id.seekBarCallVolume);
        textviewCallVolume = (TextView) view.findViewById(R.id.txtCallVolume);
        chkCallVolume = (AppCompatCheckBox) view.findViewById(R.id.chkCallVolume);
        chkCallVolume.setOnCheckedChangeListener(new MyCheckedChangeListener(4));
        btnChangeRingtone = (ImageButton) view.findViewById(R.id.btnChangeRingTone);
        btnChangeNotificationtone = (ImageButton) view.findViewById(R.id.btnChangeNotificationTone);

        view.findViewById(R.id.layout_profile_name).setVisibility(View.GONE);

        AudioManager mobilemode = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

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
    public void onResume() {
        super.onResume();
        new loadDataAyncTask().execute();
    }

    public void showDismissWarning(int navigationID) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getResources().getString(R.string.prompt_save_default_profile_changes))
                .setCancelable(true)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        new saveGeoFerncesAyncTask().execute();
                    }
                })
                .setNegativeButton("Discard", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RQS_RINGTONEPICKER && resultCode == RESULT_OK) {
            uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            ringTone = RingtoneManager.getRingtone(mContext.getApplicationContext(), uri);
            Toast.makeText(mContext,
                    ringTone.getTitle(mContext),
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void alertForDiscardDefaultProfileChanges(int navigationItemID) {
        showDismissWarning(navigationItemID);
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
                    FirebaseDatabase.getInstance().getReference().child("profiles").child(((DashboardActivity) getActivity()).getUid()).child("default").child("chkRinger").setValue(isChecked);
                    changeVisibility(seekbarRingerVolume, isChecked);
                    break;
                case 2:
                    FirebaseDatabase.getInstance().getReference().child("profiles").child(((DashboardActivity) getActivity()).getUid()).child("default").child("chkMedia").setValue(isChecked);
                    changeVisibility(seekBarMediaVolume, isChecked);
                    break;
                case 3:
                    FirebaseDatabase.getInstance().getReference().child("profiles").child(((DashboardActivity) getActivity()).getUid()).child("default").child("chkAlarm").setValue(isChecked);
                    changeVisibility(seekBarAlarmVolume, isChecked);
                    break;
                case 4:
                    FirebaseDatabase.getInstance().getReference().child("profiles").child(((DashboardActivity) getActivity()).getUid()).child("default").child("chkCall").setValue(isChecked);
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
            pDialog = new ProgressDialog(mContext);
            pDialog.setMessage("Loading...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(Void... f_url) {
            FirebaseDatabase.getInstance().getReference().child("profiles").child(((DashboardActivity) getActivity()).getUid()).child("default").addValueEventListener(new ValueEventListener() {
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
                                defaultRintoneUri + ""); //DEFAULT RINGTONE URI
                        mSoundProfileReference.child("profiles").child(((DashboardActivity) getActivity()).getUid()).child("default").setValue(profile);
                    }
                    FirebaseDatabase.getInstance().getReference().child("profiles").child(((DashboardActivity) getActivity()).getUid()).child("default").removeEventListener(this);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(mContext, "Failed to load data.",
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

            pDialog = new ProgressDialog(mContext);
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

            mProfileReference = FirebaseDatabase.getInstance().getReference().child("profiles").child(((DashboardActivity) getActivity()).getUid()).child("default");
        }

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(Void... f_url) {
            if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
            //finish();
        }
    }
}

package com.appontherocks.soundprofile.fragments;


import android.app.Activity;
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
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.appontherocks.soundprofile.R;
import com.appontherocks.soundprofile.activities.BaseActivity;
import com.appontherocks.soundprofile.activities.HomeActivity;
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
    final int RQS_NOTIFICATION_TONE_PICKER = 2;
    TextView textviewRingerVolume, textViewMediaVolume, textviewAlarmVolume, textviewCallVolume, textViewNotificationVolume, textViewSystemVolume;
    SeekBar seekbarRingerVolume, seekBarMediaVolume, seekBarAlarmVolume, seekBarCallVolume, seekBarNotificationVolume, seekBarSystenVolume;
    AppCompatCheckBox chkRingerVolume, chkMediaVolume, chkAlarmVolume, chkCallVolume, chkNotificationVolume, chkSystemVolume;
    AppCompatCheckBox chkDefaultProfile;
    LinearLayout layoutChangeNotificationtone;
    LinearLayout layoutChangeRingtone;
    Ringtone ringTone;
    Ringtone notificationTone;
    TextView txtRingTone, txtNotificationTone;
    private LinearLayout layoutWifiSetting, layoutBluetoothSetting;
    private TextView txtWifiSetting, txtBluetoothSetting;
    private TextView txtWifiSettingValue, txtBluetoothSettingValue;
    private Uri defaultRintoneUri;
    private Uri defaultNotificationToneUri;
    private DatabaseReference mSoundProfileReference;
    private Uri uriRingTone;
    private Uri uriNotificationTone;
    private Context mContext;
    private View view;

    private DatabaseReference mProfileReference;
    private boolean isViewShown = false;

    public DefaultProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_default_profile, container, false);
        mContext = view.getContext();

        defaultRintoneUri = RingtoneManager.getActualDefaultRingtoneUri(mContext.getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
        defaultNotificationToneUri = RingtoneManager.getActualDefaultRingtoneUri(mContext.getApplicationContext(), RingtoneManager.TYPE_NOTIFICATION);

        initializeViews();
        if (!isViewShown) {
            new loadDataAyncTask().execute();
        }

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

        seekBarNotificationVolume = (SeekBar) view.findViewById(R.id.seekBarNotificationVolume);
        textViewNotificationVolume = (TextView) view.findViewById(R.id.txtViewNotificationVolume);
        chkNotificationVolume = (AppCompatCheckBox) view.findViewById(R.id.chkNotificationVolume);
        chkNotificationVolume.setOnCheckedChangeListener(new MyCheckedChangeListener(5));

        seekBarSystenVolume = (SeekBar) view.findViewById(R.id.seekBarSystemVolume);
        textViewSystemVolume = (TextView) view.findViewById(R.id.txtViewSystemVolume);
        chkSystemVolume = (AppCompatCheckBox) view.findViewById(R.id.chkSystemVolume);
        chkSystemVolume.setOnCheckedChangeListener(new MyCheckedChangeListener(6));

        layoutChangeRingtone = (LinearLayout) view.findViewById(R.id.layoutChangeRingTone);
        layoutChangeNotificationtone = (LinearLayout) view.findViewById(R.id.layoutChangeNotificationTone);

        txtRingTone = (TextView) view.findViewById(R.id.txtRingTone);
        txtNotificationTone = (TextView) view.findViewById(R.id.txtNotificationTone);

        view.findViewById(R.id.layout_profile_name).setVisibility(View.GONE);

        AudioManager mobilemode = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        seekbarRingerVolume.setMax(mobilemode.getStreamMaxVolume(AudioManager.STREAM_MUSIC));

        seekbarRingerVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                textviewRingerVolume.setText(i + "");
                mProfileReference.child(getString(R.string.firebase_profile_ringtone_volume)).setValue(i + "");
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
                mProfileReference.child(getString(R.string.firebase_profile_music_volume)).setValue(i + "");
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
                mProfileReference.child(getString(R.string.firebase_profile_alarm_volume)).setValue(i + "");
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
                mProfileReference.child(getString(R.string.firebase_profile_call_volume)).setValue(i + "");
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
                mProfileReference.child(getString(R.string.firebase_profile_notification_volume)).setValue(i + "");
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
                mProfileReference.child(getString(R.string.firebase_profile_system_volume)).setValue(i + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        txtWifiSetting = (TextView) view.findViewById(R.id.txtWifiSetting);
        txtWifiSettingValue = (TextView) view.findViewById(R.id.txtWifiStatus);
        layoutWifiSetting = (LinearLayout) view.findViewById(R.id.layoutWifiSetting);
        layoutWifiSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(mContext, v);
                popup.getMenuInflater().inflate(R.menu.menu_wifi_bluetooth, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        txtWifiSettingValue.setText(item.getTitle());
                        mProfileReference.child(getString(R.string.firebase_profile_wifi_setting)).setValue(item.getTitle());
                        return true;
                    }
                });
                popup.show();
            }
        });

        txtBluetoothSetting = (TextView) view.findViewById(R.id.txtBluetoothSetting);
        txtBluetoothSettingValue = (TextView) view.findViewById(R.id.txtBluetoothStatus);
        layoutBluetoothSetting = (LinearLayout) view.findViewById(R.id.layoutBluetoothSetting);
        layoutBluetoothSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(mContext, v);
                popup.getMenuInflater().inflate(R.menu.menu_wifi_bluetooth, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        txtBluetoothSettingValue.setText(item.getTitle());
                        mProfileReference.child(getString(R.string.firebase_profile_bluetooth_setting)).setValue(item.getTitle());
                        return true;
                    }
                });
                popup.show();
            }
        });

        layoutChangeRingtone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
                startActivityForResult(intent, RQS_RINGTONEPICKER);
            }
        });

        layoutChangeNotificationtone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                startActivityForResult(intent, RQS_NOTIFICATION_TONE_PICKER);
            }
        });
    }

    @Override
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);

        if (getView() != null) {
            isViewShown = true;
            // fetchdata() contains logic to show data when page is selected mostly asynctask to fill the data
            new loadDataAyncTask().execute();
        } else {
            isViewShown = false;
        }

        if (visible) {
            //Only manually call onResume if fragment is already visible
            //Otherwise allow natural fragment lifecycle to call onResume
            if (mProfileReference == null) {
                Activity activity = getActivity();
                if (activity != null) {

                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void showDismissWarning(int navigationID) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getResources().getString(R.string.prompt_save_default_profile_changes))
                .setCancelable(true)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        new saveGeoFencesAsyncTask().execute();
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
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RQS_RINGTONEPICKER:
                    uriRingTone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    ringTone = RingtoneManager.getRingtone(mContext, uriRingTone);
                    txtRingTone.setText(ringTone.getTitle(mContext));
                    mProfileReference.child(getString(R.string.firebase_profile_ringtone_uri)).setValue(uriRingTone + "");
                    break;
                case RQS_NOTIFICATION_TONE_PICKER:
                    uriNotificationTone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    notificationTone = RingtoneManager.getRingtone(mContext, uriNotificationTone);
                    txtNotificationTone.setText(notificationTone.getTitle(mContext));
                    mProfileReference.child(getString(R.string.firebase_profile_notification_tone_uri)).setValue(uriNotificationTone + "");
                    break;
            }
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
                    mProfileReference.child(getString(R.string.firebase_profile_ringtone_chk)).setValue(isChecked);
                    changeVisibility(seekbarRingerVolume, isChecked);
                    break;
                case 2:
                    mProfileReference.child(getString(R.string.firebase_profile_music_chk)).setValue(isChecked);
                    changeVisibility(seekBarMediaVolume, isChecked);
                    break;
                case 3:
                    mProfileReference.child(getString(R.string.firebase_profile_alarm_chk)).setValue(isChecked);
                    changeVisibility(seekBarAlarmVolume, isChecked);
                    break;
                case 4:
                    mProfileReference.child(getString(R.string.firebase_profile_call_chl)).setValue(isChecked);
                    changeVisibility(seekBarCallVolume, isChecked);
                    break;
                case 5:
                    mProfileReference.child(getString(R.string.firebase_profile_notification_chk)).setValue(isChecked);
                    changeVisibility(seekBarNotificationVolume, isChecked);
                    break;
                case 6:
                    mProfileReference.child(getString(R.string.firebase_profile_system_chl)).setValue(isChecked);
                    changeVisibility(seekBarSystenVolume, isChecked);
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
            mProfileReference = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_profiles)).child(((HomeActivity) getActivity()).getUid()).child(getString(R.string.firebase_default_profiles));
        }

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(Void... f_url) {
            mProfileReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    SoundProfile profile = dataSnapshot.getValue(SoundProfile.class);

                    if (profile != null) {

                        if ((profile.ringtoneVolume != null)) {
                            seekbarRingerVolume.setProgress(Integer.parseInt(profile.ringtoneVolume));
                            seekBarMediaVolume.setProgress(Integer.parseInt(profile.musicVolume));
                            seekBarAlarmVolume.setProgress(Integer.parseInt(profile.alarmVolume));
                            seekBarCallVolume.setProgress(Integer.parseInt(profile.callVolume));
                            seekBarNotificationVolume.setProgress(Integer.parseInt(profile.notificationVolume));
                            seekBarSystenVolume.setProgress(Integer.parseInt(profile.systemVolume));
                        }

                        if ((profile.ringToneURI) != null) {
                            txtRingTone.setText(RingtoneManager.getRingtone(mContext, Uri.parse(profile.ringToneURI)).getTitle(mContext));
                        }
                        if ((profile.notificationToneURI) != null) {
                            txtNotificationTone.setText(RingtoneManager.getRingtone(mContext, Uri.parse(profile.notificationToneURI)).getTitle(mContext));
                        }

                        chkDefaultProfile.setChecked(profile.chkDefaultProfile);
                        chkRingerVolume.setChecked(profile.chkRinger);
                        chkMediaVolume.setChecked(profile.chkMedia);
                        chkAlarmVolume.setChecked(profile.chkAlarm);
                        chkCallVolume.setChecked(profile.chkCall);
                        chkSystemVolume.setChecked(profile.chkSystem);
                        chkNotificationVolume.setChecked(profile.chkNotification);

                        if (profile.wifiSetting != null) {
                            txtWifiSettingValue.setText(profile.wifiSetting);
                        }
                        if (profile.bluetoothSetting != null) {
                            txtBluetoothSettingValue.setText(profile.bluetoothSetting);
                        }

                    } else {
                        profile = new SoundProfile("New Profile",//PROFILE NAME
                                false,//ACTIVE DEFAULT PROFILE ON UNKNOWN AREA ?
                                true, true, true, true, true, true, // DEFAULT VALUES FOR CHANGING SOUND SETTING
                                "0", "0", "0", "0", "0", "0", //DEFAULT VOLUME LEVEL
                                getString(R.string.title_no_change), getString(R.string.title_no_change), //DEFAULT STATE OF WIFI / BLUETOOTH
                                "", "", //BLANK LATITUDE & LONGITUDE
                                defaultRintoneUri + "", defaultNotificationToneUri + ""); //DEFAULT RINGTONE URI
                        mSoundProfileReference.child("profiles").child(((HomeActivity) getActivity()).getUid()).child("default").setValue(profile);
                    }
                    FirebaseDatabase.getInstance().getReference().child("profiles").child(((HomeActivity) getActivity()).getUid()).child("default").removeEventListener(this);
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
            //((BaseActivity) getActivity()).hideProgressDialog();
        }
    }

    public class saveGeoFencesAsyncTask extends AsyncTask<Void, String, String> {

        private Boolean isDefaultProfileChecked;

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

        /**
         * Before starting background thread
         * Show Progress Bar Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            ((BaseActivity) getActivity()).showProgressDialog();
            isDefaultProfileChecked = chkDefaultProfile.isChecked();

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
        }

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(Void... f_url) {
            if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }


            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            ((BaseActivity) getActivity()).hideProgressDialog();
        }
    }
}

package com.appontherocks.soundprofile.fragments;


import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.appontherocks.soundprofile.R;
import com.appontherocks.soundprofile.activities.BaseActivity;
import com.appontherocks.soundprofile.activities.NewProfileActivity;
import com.appontherocks.soundprofile.models.SoundProfile;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * A simple {@link Fragment} subclass.
 */
public class DashboardFragment extends Fragment {

    SeekBar seekbarRingerVolume, seekBarMediaVolume, seekBarAlarmVolume;
    private View view;
    private Context mContext;
    private Uri defaultRintoneUri;
    private Uri defaultNotificationToneUri;
    private DatabaseReference mSoundProfileReference;
    private boolean isServiceRunning = false;

    public DashboardFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        mContext = view.getContext();

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
/*        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);*/

        defaultRintoneUri = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
        defaultNotificationToneUri = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_NOTIFICATION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(getApplicationContext())) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage(getResources().getString(R.string.prompt_system_write_permission))
                        .setCancelable(true)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + mContext.getPackageName()));
                                startActivityForResult(intent, 200);
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
        }

        mSoundProfileReference = FirebaseDatabase.getInstance().getReference();

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final LocationManager manager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

                if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    ((BaseActivity) getActivity()).buildAlertMessageNoGps();
                } else {
                    SoundProfile profile = new SoundProfile(getString(R.string.title_new_profile),//PROFILE NAME
                            false,//ACTIVE DEFAULT PROFILE ON UNKNOWN AREA ?
                            true, true, true, true, true, true, // DEFAULT VALUES FOR CHANGING SOUND SETTING
                            "0", "0", "0", "0", "0", "0", //DEFAULT VOLUME LEVEL
                            getString(R.string.title_no_change), getString(R.string.title_no_change), //DEFAULT STATE OF WIFI / BLUETOOTH
                            "", "", //BLANK LATITUDE & LONGITUDE
                            defaultRintoneUri + "", defaultNotificationToneUri + ""); //DEFAULT RINGTONE URI

                    String key = mSoundProfileReference.child(getString(R.string.firebase_profiles)).child(((BaseActivity) getActivity()).getUid()).push().getKey();
                    mSoundProfileReference.child(getString(R.string.firebase_profiles)).child(((BaseActivity) getActivity()).getUid()).child(key).setValue(profile);
                    mSoundProfileReference.child(getString(R.string.firebase_profiles)).child(((BaseActivity) getActivity()).getUid()).child(key).child(getString(R.string.mKey)).setValue(key + "");
                    Intent intent = new Intent(mContext, NewProfileActivity.class);
                    intent.putExtra(getString(R.string.key), key + "");
                    startActivity(intent);
                }
            }
        });

        seekbarRingerVolume = (SeekBar) view.findViewById(R.id.seekBarRingerVolume);
        seekBarMediaVolume = (SeekBar) view.findViewById(R.id.seekBarMediaVolume);
        seekBarAlarmVolume = (SeekBar) view.findViewById(R.id.seekBarAlarmVolume);

        final AudioManager mobilemode = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        seekbarRingerVolume.setMax(mobilemode.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        seekbarRingerVolume.setProgress(mobilemode.getStreamVolume(AudioManager.STREAM_RING));
        seekBarMediaVolume.setProgress(AudioManager.STREAM_MUSIC);
        seekBarAlarmVolume.setProgress(AudioManager.STREAM_ALARM);

        seekbarRingerVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mobilemode.setStreamVolume(AudioManager.STREAM_RING, i, 0);
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
                mobilemode.setStreamVolume(AudioManager.STREAM_MUSIC, i, 0);
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
                mobilemode.setStreamVolume(AudioManager.STREAM_ALARM, i, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        ((ToggleButton) view.findViewById(R.id.toggle_profile)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotificationManager n = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (n.isNotificationPolicyAccessGranted()) {
                        if (((ToggleButton) view.findViewById(R.id.toggle_profile)).getText().toString().equals(getString(R.string.text_on))) {
                            mobilemode.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                            Toast.makeText(mContext, "SILENT profile activated ", Toast.LENGTH_LONG).show();
                        } else if (((ToggleButton) view.findViewById(R.id.toggle_profile)).getText().toString().equals(getString(R.string.text_off))) {
                            mobilemode.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                            Toast.makeText(mContext, "LOUD profile activated !", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        // Ask the user to grant access
                        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                        startActivity(intent);
                    }
                }
            }
        });

        return view;
    }

}

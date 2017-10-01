package com.appontherocks.soundprofile.fragments;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

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
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class AdvancedSettingsFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, ResultCallback<Status>, GoogleApiClient.OnConnectionFailedListener {

    private final int STEP_ONE_COMPLETE = 0;
    SharedPreferences.Editor advancedSettingsEdit, themeSettingEdit;
    SharedPreferences advancedSettings;
    List<Geofence> mGeofenceList;
    private ArrayList<SoundProfile> profileArrayList = new ArrayList<>();
    private GoogleApiClient mGoogleApiClient;
    private View view;
    private Context mContext;
    private int radius;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STEP_ONE_COMPLETE:
                    LocationManager manager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
                    if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        if (mGeofenceList.size() > 0) {
                            new saveGeoFerncesAyncTask().execute();
                        }
                    }
                    break;
            }
        }
    };

    public AdvancedSettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_advanced_settings, container, false);
        mContext = view.getContext();

        advancedSettingsEdit = mContext.getSharedPreferences(getString(R.string.advanced_settings), MODE_PRIVATE).edit();
        themeSettingEdit = mContext.getSharedPreferences(getString(R.string.theme), MODE_PRIVATE).edit();
        advancedSettings = mContext.getSharedPreferences(getString(R.string.advanced_settings), MODE_PRIVATE);

        mGeofenceList = new ArrayList<Geofence>();
        advancedSettings = mContext.getSharedPreferences(mContext.getString(R.string.advanced_settings), MODE_PRIVATE);
        radius = advancedSettings.getInt(mContext.getString(R.string.geofence_radius), 50);
        final LocationManager manager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();

        ((AppCompatCheckBox) view.findViewById(R.id.chkAutoWifi)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                advancedSettingsEdit.putBoolean(getString(R.string.auto_disable_wifi), isChecked);
                advancedSettingsEdit.commit();
            }
        });

        ((AppCompatCheckBox) view.findViewById(R.id.chkGeofenceRadius)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        fetchGeoFences();
                    }
                }
            }
        });

        ((TextInputEditText) view.findViewById(R.id.edtGeofenceRadius)).addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    advancedSettingsEdit.putInt(getString(R.string.geofence_radius), Integer.parseInt(s.toString()));
                    advancedSettingsEdit.commit();
                } catch (Exception e) {
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        ((TextInputEditText) view.findViewById(R.id.edtGeofenceRadius)).setText(advancedSettings.getInt(getString(R.string.geofence_radius), 50) + "");

        ((AppCompatCheckBox) view.findViewById(R.id.chkAutoWifi)).setChecked(advancedSettings.getBoolean(getString(R.string.auto_disable_wifi), false));

        ((TextView) view.findViewById(R.id.txtChangeTheme)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showThemeSelectorPopup(v);
            }
        });

        return view;
    }

    // Display anchored popup menu based on view selected
    private void showThemeSelectorPopup(View v) {
        PopupMenu popup = new PopupMenu(mContext, v);
        // Inflate the menu from xml
        popup.getMenuInflater().inflate(R.menu.popup_themes, popup.getMenu());
        // Setup menu item selection
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.theme_dark:
                        themeSettingEdit.putString(getString(R.string.theme), getString(R.string.theme_dark)).commit();
                        mContext.setTheme(R.style.DarkTheme);
                        getActivity().recreate();
                        return true;
                    case R.id.theme_green:
                        themeSettingEdit.putString(getString(R.string.theme), getString(R.string.theme_green)).commit();
                        mContext.setTheme(R.style.PurpleTheme);
                        getActivity().recreate();
                        return true;
                    case R.id.theme_purple:
                        themeSettingEdit.putString(getString(R.string.theme), getString(R.string.theme_purple)).commit();
                        mContext.setTheme(R.style.GreenTheme);
                        getActivity().recreate();
                        return true;
                    default:
                        return false;
                }
            }
        });
        // Handle dismissal with: popup.setOnDismissListener(...);
        // Show the menu
        popup.show();
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
                                                    radius
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
            ).setResultCallback((ResultCallback<? super com.google.android.gms.common.api.Status>) mContext);

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
        }
    }
}

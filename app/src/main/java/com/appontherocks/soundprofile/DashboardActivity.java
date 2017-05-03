package com.appontherocks.soundprofile;

import android.app.ActivityManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;

import com.appontherocks.soundprofile.models.SoundProfile;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.net.URL;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class DashboardActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "Dashboard";
    CircleImageView profilePicture;
    TextView txtDisplayName, txtEmail;
    private Uri defaultRintoneUri;

    private DatabaseReference mSoundProfileReference;
    private boolean isServiceRunning = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        super.onCreate(savedInstanceState);
        //setTheme(R.style.AppThemeLight);
        setContentView(R.layout.activity_dashboard);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);

        profilePicture = (de.hdodenhof.circleimageview.CircleImageView) headerView.findViewById(R.id.imgProfilePicture);
        txtDisplayName = (TextView) headerView.findViewById(R.id.txtUserName);
        txtEmail = (TextView) headerView.findViewById(R.id.txtEmail);

        defaultRintoneUri = RingtoneManager.getActualDefaultRingtoneUri(DashboardActivity.this.getApplicationContext(), RingtoneManager.TYPE_RINGTONE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(getApplicationContext())) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getResources().getString(R.string.prompt_system_write_permission))
                        .setCancelable(true)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    buildAlertMessageNoGps();
                } else {
                    SoundProfile profile = new SoundProfile("New Profile", false, true, true, true, true, "0", "0", "0", "0", "", "", defaultRintoneUri + "");
                    String key = mSoundProfileReference.child("profiles").child(getUid()).push().getKey();
                    mSoundProfileReference.child("profiles").child(getUid()).child(key).setValue(profile);
                    mSoundProfileReference.child("profiles").child(getUid()).child(key).child("mKey").setValue(key + "");
                    Intent intent = new Intent(DashboardActivity.this, NewProfileActivity.class);
                    intent.putExtra("key", key + "");
                    startActivity(intent);
                }
            }
        });
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
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Intent intent = new Intent(DashboardActivity.this, AuthenticationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("EXIT", true);
            startActivity(intent);
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
        } else if (id == R.id.nav_default_profile) {
            Intent intent = new Intent(DashboardActivity.this, DefaultProfileActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_sleep_hours) {
            ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            isServiceRunning = false;
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (SleepyHoursService.class.getName().equals(service.service.getClassName())) {
                    isServiceRunning = true;
                }
            }
            if (isServiceRunning) {
                stopService(new Intent(DashboardActivity.this, SleepyHoursService.class));
            } else {
                Calendar mcurrentTime = Calendar.getInstance();
                final int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                final int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(DashboardActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        TimePickerDialog mTimePicker;
                        final int selectedStartHour = selectedHour;
                        final int selectedStartMinute = selectedMinute;
                        mTimePicker = new TimePickerDialog(DashboardActivity.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                                Intent intent = new Intent(DashboardActivity.this, SleepyHoursService.class);
                                intent.putExtra("startHour", selectedStartHour);
                                intent.putExtra("startMinute", selectedStartMinute);
                                intent.putExtra("endHour", selectedHour);
                                intent.putExtra("endMinute", selectedMinute);
                                startService(intent);
                            }
                        }, hour, minute, true);//Yes 24 hour time
                        mTimePicker.setMessage("End Time");
                        mTimePicker.show();
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setMessage("Start Time");
                mTimePicker.show();
            }
        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
}

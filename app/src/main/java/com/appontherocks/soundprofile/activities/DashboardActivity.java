package com.appontherocks.soundprofile.activities;

import android.app.ActivityManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.appontherocks.soundprofile.R;
import com.appontherocks.soundprofile.fragments.AdvancedSettingsFragment;
import com.appontherocks.soundprofile.fragments.DashboardFragment;
import com.appontherocks.soundprofile.fragments.DefaultProfileFragment;
import com.appontherocks.soundprofile.fragments.ProfilesFragment;
import com.appontherocks.soundprofile.service.SleepyHoursService;
import com.google.firebase.database.DatabaseReference;

import java.net.URL;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class DashboardActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "Dashboard";
    CircleImageView profilePicture;
    TextView txtDisplayName, txtEmail;
    SeekBar seekbarRingerVolume, seekBarMediaVolume, seekBarAlarmVolume;
    DefaultProfileFragment mDefaultProfileFragment;
    private Uri defaultRintoneUri;
    private DatabaseReference mSoundProfileReference;
    private boolean isServiceRunning = false;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(DashboardActivity.this);
        View headerView = mNavigationView.getHeaderView(0);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        mNavigationView.setNavigationItemSelectedListener(this);

        profilePicture = (de.hdodenhof.circleimageview.CircleImageView) headerView.findViewById(R.id.imgProfilePicture);
        txtDisplayName = (TextView) headerView.findViewById(R.id.txtUserName);
        txtEmail = (TextView) headerView.findViewById(R.id.txtEmail);

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

        setInitialFragment();
    }

    public void setInitialFragment() {
        DashboardFragment dashboardFragment = new DashboardFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_Content, dashboardFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        //setUpToolbar();
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
        switch (item.getItemId()) {
            // THIS IS YOUR DRAWER/HAMBURGER BUTTON
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);  // OPEN DRAWER
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent;

        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frame_Content);
        if ((currentFragment instanceof DefaultProfileFragment) && !(id == R.id.nav_default_profile)) {
            Toast.makeText(this, "Default profile", Toast.LENGTH_LONG).show();
            mDefaultProfileFragment = new DefaultProfileFragment();
            ((DefaultProfileFragment) currentFragment).alertForDiscardDefaultProfileChanges(id);
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return false;
        }

        switch (id) {
            case R.id.nav_dashboard:
                setInitialFragment();
                break;
            case R.id.nav_profiles:
                ProfilesFragment profilesFragment = new ProfilesFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.frame_Content, profilesFragment);
                fragmentTransaction.addToBackStack(null);
                //fragmentTransaction.commit();
                intent = new Intent(DashboardActivity.this, ProfilesActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_default_profile:
                DefaultProfileFragment defaultProfileFragment = new DefaultProfileFragment();
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.frame_Content, defaultProfileFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                /*intent = new Intent(DashboardActivity.this, DefaultProfileActivity.class);
                startActivity(intent);*/
                break;
            case R.id.nav_sleep_hours:
                ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                isServiceRunning = false;
                for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                    if (SleepyHoursService.class.getName().equals(service.service.getClassName())) {
                        isServiceRunning = true;
                    }
                }
                if (isServiceRunning) {
                    SharedPreferences prefs = getSharedPreferences(getString(R.string.sleep_hours), MODE_PRIVATE);
                    int startHour = prefs.getInt("startHour", 0); //0 is the default value.
                    int startMinute = prefs.getInt("startMinute", 0); //0 is the default value.
                    int endHour = prefs.getInt("endHour", 0); //0 is the default value.
                    int endMinute = prefs.getInt("endMinute", 0); //0 is the default value.

                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(getResources().getString(R.string.sleep_hours))
                            .setMessage(getResources().getString(R.string.sleep_hours_warning_start) + " " + startHour + ":" + startMinute + " to " + endHour + ":" + endMinute
                                    + getResources().getString(R.string.sleep_hours_warning_end))
                            .setCancelable(true)
                            .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                                public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                    stopService(new Intent(DashboardActivity.this, SleepyHoursService.class));
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                    dialog.cancel();
                                }
                            });
                    final AlertDialog alert = builder.create();
                    alert.show();
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
                                    SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.sleep_hours), MODE_PRIVATE).edit();
                                    editor.putInt("startHour", selectedStartHour);
                                    editor.putInt("startMinute", selectedStartMinute);
                                    editor.putInt("endHour", selectedHour);
                                    editor.putInt("endMinute", selectedMinute);
                                    editor.commit();
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
                break;
            case R.id.nav_advanced_settings:
                AdvancedSettingsFragment advancedSettingsFragment = new AdvancedSettingsFragment();
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.frame_Content, advancedSettingsFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

                /*intent = new Intent(DashboardActivity.this, AdvancedSettingsActivity.class);
                startActivity(intent);*/
                break;
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
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

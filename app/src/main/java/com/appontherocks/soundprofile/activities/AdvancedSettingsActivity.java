package com.appontherocks.soundprofile.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.CompoundButton;

import com.appontherocks.soundprofile.R;

public class AdvancedSettingsActivity extends AppCompatActivity {

    SharedPreferences.Editor advancedSettingsEdit;
    SharedPreferences advancedSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        advancedSettingsEdit = getSharedPreferences(getString(R.string.advanced_settings), MODE_PRIVATE).edit();
        advancedSettings = AdvancedSettingsActivity.this.getSharedPreferences(AdvancedSettingsActivity.this.getString(R.string.advanced_settings), MODE_PRIVATE);

        ((AppCompatCheckBox) findViewById(R.id.chkAutoWifi)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                advancedSettingsEdit.putBoolean(getString(R.string.auto_disable_wifi), isChecked);
                advancedSettingsEdit.commit();
            }
        });

        ((AppCompatCheckBox) findViewById(R.id.chkGeofenceRadius)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Intent intent = new Intent("android.intent.action.BOOT_COMPLETED");
                sendBroadcast(intent);
            }
        });

        ((TextInputEditText) findViewById(R.id.edtGeofenceRadius)).addTextChangedListener(new TextWatcher() {
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

        ((TextInputEditText) findViewById(R.id.edtGeofenceRadius)).setText(advancedSettings.getInt(getString(R.string.geofence_radius), 50) + "");

        ((AppCompatCheckBox) findViewById(R.id.chkAutoWifi)).setChecked(advancedSettings.getBoolean(getString(R.string.auto_disable_wifi), false));
    }
}

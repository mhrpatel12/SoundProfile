package com.appontherocks.soundprofile.fragments;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatDrawableManager;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;

import com.appontherocks.soundprofile.R;
import com.appontherocks.soundprofile.service.SleepyHoursService;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class SleepyHoursFragment extends Fragment {

    private Context mContext;
    private View view;

    private TimePicker startTime;
    private TimePicker endTime;
    private AppCompatTextView selectedTextView;

    private boolean isServiceRunning = false;

    private AppCompatButton btnEnableSleepyHours;
    private SharedPreferences prefs;

    public SleepyHoursFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_sleepy_hours, container, false);
        mContext = view.getContext();
        prefs = mContext.getSharedPreferences(getString(R.string.sleep_hours), MODE_PRIVATE);

        btnEnableSleepyHours = (AppCompatButton) view.findViewById(R.id.btnEnableSleepyHours);
        Drawable somevectordrable = AppCompatDrawableManager.get().getDrawable(mContext, R.drawable.ic_brightness_3_black_24dp);
        btnEnableSleepyHours.setCompoundDrawables(somevectordrable, null, null, null);

        startTime = (TimePicker) view.findViewById(R.id.startTime);
        endTime = (TimePicker) view.findViewById(R.id.endTime);
        startTime.setCurrentHour(prefs.getInt(getString(R.string.startHour), 0));
        startTime.setCurrentMinute(prefs.getInt(getString(R.string.startMinute), 0));
        endTime.setCurrentHour(prefs.getInt(getString(R.string.endHour), 0));
        endTime.setCurrentMinute(prefs.getInt(getString(R.string.endMinute), 0));
        btnEnableSleepyHours.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
                isServiceRunning = false;
                for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                    if (SleepyHoursService.class.getName().equals(service.service.getClassName())) {
                        isServiceRunning = true;
                    }
                }

                if (isServiceRunning) {
                    int startHour = prefs.getInt(getString(R.string.startHour), 0); //0 is the default value.
                    int startMinute = prefs.getInt(getString(R.string.startMinute), 0); //0 is the default value.
                    int endHour = prefs.getInt(getString(R.string.endHour), 0); //0 is the default value.
                    int endMinute = prefs.getInt(getString(R.string.endMinute), 0); //0 is the default value.

                    final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(getResources().getString(R.string.sleep_hours))
                            .setMessage(getResources().getString(R.string.sleep_hours_warning_start) + " " + startHour + ":" + startMinute + " to " + endHour + ":" + endMinute
                                    + getResources().getString(R.string.sleep_hours_warning_end))
                            .setCancelable(true)
                            .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                                public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                    mContext.stopService(new Intent(mContext, SleepyHoursService.class));
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
                    SharedPreferences.Editor editor = mContext.getSharedPreferences(getString(R.string.sleep_hours), MODE_PRIVATE).edit();
                    editor.putInt(getString(R.string.startHour), startTime.getCurrentHour());
                    editor.putInt(getString(R.string.startMinute), startTime.getCurrentMinute());
                    editor.putInt(getString(R.string.endHour), endTime.getCurrentHour());
                    editor.putInt(getString(R.string.endMinute), endTime.getCurrentMinute());
                    editor.apply();
                    Intent intent = new Intent(mContext, SleepyHoursService.class);
                    intent.putExtra(getString(R.string.startHour), startTime.getCurrentHour());
                    intent.putExtra(getString(R.string.startMinute), startTime.getCurrentMinute());
                    intent.putExtra(getString(R.string.endHour), endTime.getCurrentHour());
                    intent.putExtra(getString(R.string.endMinute), endTime.getCurrentMinute());
                    mContext.startService(intent);
                }
            }
        });

        return view;
    }
}

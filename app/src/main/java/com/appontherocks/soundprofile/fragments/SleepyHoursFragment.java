package com.appontherocks.soundprofile.fragments;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatDrawableManager;
import android.support.v7.widget.AppCompatTextView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.appontherocks.soundprofile.R;
import com.appontherocks.soundprofile.service.SleepyHoursService;
import com.philliphsu.bottomsheetpickers.time.BottomSheetTimePickerDialog;
import com.philliphsu.bottomsheetpickers.time.grid.GridTimePickerDialog;

import java.util.Calendar;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class SleepyHoursFragment extends Fragment implements BottomSheetTimePickerDialog.OnTimeSetListener {

    private Context mContext;
    private View view;

    private LinearLayout layoutStartTime;
    private LinearLayout layoutEndTime;

    private AppCompatTextView txtStartTime;
    private AppCompatTextView txtEndTime;
    private AppCompatTextView selectedTextView;

    private boolean isServiceRunning = false;

    private AppCompatButton btnEnableSleepyHours;
    private int startHour, startMinute = 0;
    private int endHour, endMinute = 0;

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

        btnEnableSleepyHours = (AppCompatButton) view.findViewById(R.id.btnEnableSleepyHours);
        Drawable somevectordrable = AppCompatDrawableManager.get().getDrawable(mContext, R.drawable.ic_brightness_3_black_24dp);
        btnEnableSleepyHours.setCompoundDrawables(somevectordrable, null, null, null);

        layoutStartTime = (LinearLayout) view.findViewById(R.id.layoutStartHours);
        layoutStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimeDialog(txtStartTime, mContext.getString(R.string.text_start_time));
            }
        });
        layoutEndTime = (LinearLayout) view.findViewById(R.id.layoutEndHours);
        layoutEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimeDialog(txtEndTime, mContext.getString(R.string.text_end_time));
            }
        });

        txtStartTime = (AppCompatTextView) view.findViewById(R.id.txtStartTime);
        txtEndTime = (AppCompatTextView) view.findViewById(R.id.txtEndTime);

        btnEnableSleepyHours.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if ((startHour == 0)
                        || (startMinute) == 0
                        || (endHour) == 0
                        || (endMinute) == 0) {
                    return;
                }

                ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
                isServiceRunning = false;
                for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                    if (SleepyHoursService.class.getName().equals(service.service.getClassName())) {
                        isServiceRunning = true;
                    }
                }

                if (isServiceRunning) {
                    SharedPreferences prefs = mContext.getSharedPreferences(getString(R.string.sleep_hours), MODE_PRIVATE);
                    int startHour = prefs.getInt("startHour", 0); //0 is the default value.
                    int startMinute = prefs.getInt("startMinute", 0); //0 is the default value.
                    int endHour = prefs.getInt("endHour", 0); //0 is the default value.
                    int endMinute = prefs.getInt("endMinute", 0); //0 is the default value.

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
                    editor.putInt("startHour", startHour);
                    editor.putInt("startMinute", startMinute);
                    editor.putInt("endHour", endHour);
                    editor.putInt("endMinute", endMinute);
                    editor.apply();
                    Intent intent = new Intent(mContext, SleepyHoursService.class);
                    intent.putExtra("startHour", startHour);
                    intent.putExtra("startMinute", startMinute);
                    intent.putExtra("endHour", endHour);
                    intent.putExtra("endMinute", endMinute);
                    mContext.startService(intent);
                }
            }
        });

        return view;
    }

    private void showTimeDialog(AppCompatTextView textView, String time) {
        selectedTextView = textView;
        Calendar now = Calendar.getInstance();

        GridTimePickerDialog grid = new GridTimePickerDialog.Builder(
                this,
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(mContext))
                .build();
        int[] attrs = new int[]{R.attr.drawable_color_inverse};
        TypedArray ta = mContext.obtainStyledAttributes(attrs);

        grid.setAccentColor(ta.getColor(0, 0));
        grid.show(getActivity().getSupportFragmentManager(), "SleepyHoursFragment");
    }

    @Override
    public void onTimeSet(ViewGroup viewGroup, int hourOfDay, int minute) {
        if (selectedTextView != null) {
            if (selectedTextView == txtStartTime) {
                startHour = hourOfDay;
                startMinute = minute;
            } else if (selectedTextView == txtEndTime) {
                endHour = hourOfDay;
                endMinute = minute;
            }
            selectedTextView.setText(hourOfDay + " : " + minute);
        }
    }
}

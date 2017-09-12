package com.appontherocks.soundprofile.fragments;

import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TimePicker;

import com.appontherocks.soundprofile.R;

import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 */
public class SleepyHoursFragment extends Fragment {

    private Context mContext;
    private View view;

    private LinearLayout layoutStartTime;
    private LinearLayout layoutEndTime;

    private AppCompatTextView txtStartTime;
    private AppCompatTextView txtEndTime;

    public SleepyHoursFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_sleepy_hours, container, false);
        mContext = view.getContext();

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

        return view;
    }

    Calendar mcurrentTime = Calendar.getInstance();
    final int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
    final int minute = mcurrentTime.get(Calendar.MINUTE);

    private void showTimeDialog(AppCompatTextView textView, String time) {
        final AppCompatTextView mTextView = textView;

        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(mContext, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                mTextView.setText(selectedHour + " : " + selectedMinute);
            }
        }, hour, minute, true);//Yes 24 hour time
        mTimePicker.setMessage(time);
        mTimePicker.show();
    }
}

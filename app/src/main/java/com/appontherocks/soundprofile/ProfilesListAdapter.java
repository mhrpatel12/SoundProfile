package com.appontherocks.soundprofile;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.appontherocks.soundprofile.models.SoundProfile;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * Created by Mihir on 3/15/2017.
 */

public class ProfilesListAdapter extends BaseAdapter {

    private Context mContext;
    private Activity activity;
    private ArrayList<SoundProfile> profileArrayList = new ArrayList<>();
    private GoogleApiClient mGoogleApiClient;

    public ProfilesListAdapter(Activity activity, ArrayList<SoundProfile> soundProfiles) {
        this.activity = activity;
        this.mContext = activity.getApplicationContext();
        this.profileArrayList = soundProfiles;

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(activity)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();

    }

    private class ViewHolder {
        TextView txtProfileName, txtNotificationVolume, txtMediaVolume, txtAlarmVolume;
        CardView cardView;
    }

    @Override
    public int getCount() {
        return profileArrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getViewTypeCount() {
        if (getCount() != 0)
            return getCount();

        else return 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    ViewHolder holder = null;

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {

        if (view == null) {
            LayoutInflater vi = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.list_item_profile, null);

            holder = new ViewHolder();
            holder.cardView = (CardView) view.findViewById(R.id.cv);
            holder.txtProfileName = (TextView) view.findViewById(R.id.txtProfileName);
            holder.txtNotificationVolume = (TextView) view.findViewById(R.id.txtViewRingerVolume);
            holder.txtMediaVolume = (TextView) view.findViewById(R.id.txtViewMediaVolume);
            holder.txtAlarmVolume = (TextView) view.findViewById(R.id.txtViewAlarmVolume);

            holder.txtProfileName.setText(profileArrayList.get(position).profileName);
            holder.txtNotificationVolume.setText(profileArrayList.get(position).notificationVolume);
            holder.txtMediaVolume.setText(profileArrayList.get(position).musicVolume);
            holder.txtAlarmVolume.setText(profileArrayList.get(position).alarmVolume);

            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    final LocationManager manager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

                    if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        buildAlertMessageNoGps();
                    } else {
                        Intent intent = new Intent(mContext, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("key", profileArrayList.get(position).mKey);
                        intent.putExtra("lat", profileArrayList.get(position).latitude);
                        intent.putExtra("lng", profileArrayList.get(position).longitude);
                        mContext.startActivity(intent);
                    }

                }
            });

            holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setMessage(mContext.getResources().getString(R.string.prompt_discard_profile))
                            .setCancelable(true)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                    ArrayList<String> list = new ArrayList<String>();
                                    list.add(profileArrayList.get(position).mKey);
                                    LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, list);
                                    FirebaseDatabase.getInstance().getReference().child("profiles").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(profileArrayList.get(position).mKey).removeValue();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                    dialog.cancel();
                                }
                            });
                    final AlertDialog alert = builder.create();
                    alert.show();
                    return false;
                }
            });

        }

        return view;
    }

    public void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        mContext.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
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

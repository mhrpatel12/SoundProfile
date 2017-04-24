package com.appontherocks.soundprofile;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.appontherocks.soundprofile.models.SoundProfile;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * Created by Mihir on 4/23/2017.
 */

public class ProfilesSwipableAdapter extends ArrayAdapter<SoundProfile> {
    private final ArrayList<SoundProfile> cards;
    private final LayoutInflater layoutInflater;
    private GoogleApiClient mGoogleApiClient;
    private Context mContext;

    public ProfilesSwipableAdapter(Context context, ArrayList<SoundProfile> cards) {
        super(context, -1);
        this.mContext = context;
        this.cards = cards;
        this.layoutInflater = LayoutInflater.from(context);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final SoundProfile card = cards.get(position);
        View view = layoutInflater.inflate(R.layout.list_item_profile, parent, false);
        String getMapURL = "http://maps.googleapis.com/maps/api/staticmap?zoom=16&size=300x600&scale=2&markers=size:mid|color:red|"
                + card.latitude
                + ","
                + card.longitude
                + "&sensor=false";
        Glide.with(mContext)
                .load(getMapURL)
                .into(((ImageView) view.findViewById(R.id.map_lite)));

        ((TextView) view.findViewById(R.id.txtProfileName)).setText(card.profileName);

        view.findViewById(R.id.imgEdit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final LocationManager manager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

                if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    buildAlertMessageNoGps();
                } else {
                    Intent intent = new Intent(mContext, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("name", card.profileName);
                    intent.putExtra("key", card.mKey);
                    intent.putExtra("lat", card.latitude);
                    intent.putExtra("lng", card.longitude);
                    mContext.startActivity(intent);
                }
            }
        });

        view.findViewById(R.id.btnDelete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage(mContext.getResources().getString(R.string.prompt_discard_profile))
                        .setCancelable(true)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                ArrayList<String> list = new ArrayList<String>();
                                list.add(card.mKey);
                                LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, list);
                                FirebaseDatabase.getInstance().getReference().child("profiles").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(card.mKey).removeValue();
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
        });

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

    @Override
    public SoundProfile getItem(int position) {
        return cards.get(position);
    }

    @Override
    public int getCount() {
        return cards.size();
    }

}

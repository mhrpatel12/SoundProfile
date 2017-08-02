package com.appontherocks.soundprofile.adapter;

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

import com.appontherocks.soundprofile.R;
import com.appontherocks.soundprofile.activities.MainActivity;
import com.appontherocks.soundprofile.event.ProfileDeletedEvent;
import com.appontherocks.soundprofile.models.SoundProfile;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

/**
 * Created by Mihir on 4/23/2017.
 */

public class ProfilesSwipableAdapter extends ArrayAdapter<SoundProfile> {
    private final ArrayList<SoundProfile> cards;
    private final LayoutInflater layoutInflater;
    private GoogleApiClient mGoogleApiClient;
    private Context mContext;

    public ProfilesSwipableAdapter(Context context, int resource, ArrayList<SoundProfile> cards) {
        super(context, 0, cards);
        this.mContext = context;
        this.cards = cards;
        //this.layoutInflater = LayoutInflater.from(context);
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final SoundProfile card = cards.get(position);
        View view = layoutInflater.inflate(R.layout.list_item_profile, parent, false);
        String getMapURL = "http://maps.googleapis.com/maps/api/staticmap?zoom=16&size=300x600&scale=2&markers=size:mid|color:red|"
                + card.latitude
                + ","
                + card.longitude
                + "&sensor=false";

        String getMapURLBlack = "https://maps.googleapis.com/maps/api/staticmap"
                + "?zoom=16&size=300x600&scale=2&markers=size:mid|color:red|"
                + card.latitude
                + ","
                + card.longitude
                + "&sensor=false"
                + "&style=element:geometry%7Ccolor:0x242f3e&style=element:labels.text.fill%7Ccolor:0x746855&style=element:labels.text.stroke%7Ccolor:0x242f3e&style=feature:administrative.locality%7Celement:labels.text.fill%7Ccolor:0xd59563&style=feature:poi%7Celement:labels.text.fill%7Ccolor:0xd59563&style=feature:poi.park%7Celement:geometry%7Ccolor:0x263c3f&style=feature:poi.park%7Celement:labels.text.fill%7Ccolor:0x6b9a76&style=feature:road%7Celement:geometry%7Ccolor:0x38414e&style=feature:road%7Celement:geometry.stroke%7Ccolor:0x212a37&style=feature:road%7Celement:labels.text.fill%7Ccolor:0x9ca5b3&style=feature:road.highway%7Celement:geometry%7Ccolor:0x746855&style=feature:road.highway%7Celement:geometry.stroke%7Ccolor:0x1f2835&style=feature:road.highway%7Celement:labels.text.fill%7Ccolor:0xf3d19c&style=feature:transit%7Celement:geometry%7Ccolor:0x2f3948&style=feature:transit.station%7Celement:labels.text.fill%7Ccolor:0xd59563&style=feature:water%7Celement:geometry%7Ccolor:0x17263c&style=feature:water%7Celement:labels.text.fill%7Ccolor:0x515c6d&style=feature:water%7Celement:labels.text.stroke%7Ccolor:0x17263c";

        Glide.with(mContext)
                .load(getMapURLBlack)
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
                                EventBus.getDefault().post(new ProfileDeletedEvent(position));
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
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getCount() {
        return cards.size();
    }

}

package com.appontherocks.soundprofile;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.appontherocks.soundprofile.models.SoundProfile;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

/**
 * Created by Mihir on 3/15/2017.
 */

public class ProfilesListAdapter extends RecyclerView.Adapter<ProfilesListAdapter.MyViewHolder> {
    ImageLoader imageLoader;
    private ArrayList<SoundProfile> profileArrayList;
    private Context mContext;
    private Activity activity;
    private GoogleApiClient mGoogleApiClient;

    public ProfilesListAdapter(Activity activity, ArrayList<SoundProfile> data) {
        this.profileArrayList = data;
        this.activity = activity;
        this.mContext = activity.getApplicationContext();
        this.imageLoader = new ImageLoader(mContext);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(activity)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_profile, parent, false);

        //view.setOnClickListener(MainActivity.myOnClickListener);

        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {

        TextView txtProfileName = holder.txtProfileName;
        CardView cardView = holder.cardView;
        AppCompatImageView imageView = holder.imageView;
        AppCompatImageView btnDelete = holder.btnDelete;

        String getMapURL = "";
        txtProfileName.setText(profileArrayList.get(listPosition).profileName);

        if (((profileArrayList.get(listPosition).latitude != null) && (!(profileArrayList.get(listPosition).latitude + "").equals(""))) && ((profileArrayList.get(listPosition).longitude != null) && (!(profileArrayList.get(listPosition).longitude + "").equals("")))) {

            getMapURL = "http://maps.googleapis.com/maps/api/staticmap?zoom=16&size=300x600&scale=2&markers=size:mid|color:red|"
                    + profileArrayList.get(listPosition).latitude
                    + ","
                    + profileArrayList.get(listPosition).longitude
                    + "&sensor=false";
            Log.e("PROFILES ADAPTER", getMapURL);
            Glide.with(mContext)
                    .load(getMapURL)
                    .into(imageView);
            //new downloadMapImage().execute(getMapURL);
            //imageLoader.DisplayImage(getMapURL, holder.imageView);
        }

        if ((profileArrayList.get(listPosition).profileName + "").equals("Default Profile")) {
            cardView.setVisibility(View.GONE);
        }

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final LocationManager manager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

                if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    buildAlertMessageNoGps();
                } else {
                    Intent intent = new Intent(mContext, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("name", profileArrayList.get(listPosition).profileName);
                    intent.putExtra("key", profileArrayList.get(listPosition).mKey);
                    intent.putExtra("lat", profileArrayList.get(listPosition).latitude);
                    intent.putExtra("lng", profileArrayList.get(listPosition).longitude);
                    mContext.startActivity(intent);
                }

            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((profileArrayList.get(listPosition).profileName + "").equals("Default Profile")) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setMessage(mContext.getResources().getString(R.string.prompt_delete_default_profile))
                            .setCancelable(true)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                    dialog.cancel();
                                }
                            });
                    final AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setMessage(mContext.getResources().getString(R.string.prompt_discard_profile))
                            .setCancelable(true)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                    ArrayList<String> list = new ArrayList<String>();
                                    list.add(profileArrayList.get(listPosition).mKey);
                                    LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient, list);
                                    FirebaseDatabase.getInstance().getReference().child("profiles").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(profileArrayList.get(listPosition).mKey).removeValue();
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
        });
    }

    @Override
    public int getItemCount() {
        return profileArrayList.size();
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

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtProfileName;
        CardView cardView;
        AppCompatImageView imageView;
        AppCompatImageView btnDelete;


        public MyViewHolder(View itemView) {
            super(itemView);
            this.cardView = (CardView) itemView.findViewById(R.id.cv);
            this.txtProfileName = (TextView) itemView.findViewById(R.id.txtProfileName);
            this.imageView = (AppCompatImageView) itemView.findViewById(R.id.map_lite);
            this.btnDelete = (AppCompatImageView) itemView.findViewById(R.id.btnDelete);
        }
    }
}

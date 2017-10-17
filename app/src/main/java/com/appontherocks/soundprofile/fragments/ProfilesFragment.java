package com.appontherocks.soundprofile.fragments;


import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.appontherocks.soundprofile.R;
import com.appontherocks.soundprofile.activities.BaseActivity;
import com.appontherocks.soundprofile.activities.NewProfileActivity;
import com.appontherocks.soundprofile.adapter.ProfilesListAdapter;
import com.appontherocks.soundprofile.event.ProfileDeletedEvent;
import com.appontherocks.soundprofile.models.SoundProfile;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfilesFragment extends Fragment {

    private RecyclerView recyclerViewProfiles;
    private ProfilesListAdapter profilesListAdapter;
    private ArrayList<SoundProfile> profileArrayList = new ArrayList<>();

    private LinearLayout layoutNoProfiles;
    private AppCompatButton buttonNewProfile;

    private View view;

    private Context mContext;

    private Uri defaultRintoneUri;
    private Uri defaultNotificationToneUri;
    private DatabaseReference mSoundProfileReference;

    public ProfilesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profiles, container, false);

        mContext = getContext();

        recyclerViewProfiles = (RecyclerView) view.findViewById(R.id.recyclerViewProfiles);
        recyclerViewProfiles.setLayoutManager(new LinearLayoutManager(mContext));

        profilesListAdapter = new ProfilesListAdapter(getActivity(), profileArrayList);
        recyclerViewProfiles.setAdapter(profilesListAdapter);

        defaultRintoneUri = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
        defaultNotificationToneUri = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_NOTIFICATION);

        mSoundProfileReference = FirebaseDatabase.getInstance().getReference();

        layoutNoProfiles = (LinearLayout) view.findViewById(R.id.layoutNoProfiles);
        buttonNewProfile = (AppCompatButton) view.findViewById(R.id.btnNewProfile);
        buttonNewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final LocationManager manager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

                if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    ((BaseActivity) getActivity()).buildAlertMessageNoGps();
                } else {
                    SoundProfile profile = new SoundProfile(getString(R.string.title_new_profile),//PROFILE NAME
                            false,//ACTIVE DEFAULT PROFILE ON UNKNOWN AREA ?
                            true, true, true, true, true, true, // DEFAULT VALUES FOR CHANGING SOUND SETTING
                            "0", "0", "0", "0", "0", "0", //DEFAULT VOLUME LEVEL
                            getString(R.string.title_no_change), getString(R.string.title_no_change), //DEFAULT STATE OF WIFI / BLUETOOTH
                            "", "", //BLANK LATITUDE & LONGITUDE
                            defaultRintoneUri + "", defaultNotificationToneUri + ""); //DEFAULT RINGTONE URI

                    String key = mSoundProfileReference.child(getString(R.string.firebase_profiles)).child(((BaseActivity) getActivity()).getUid()).push().getKey();
                    mSoundProfileReference.child(getString(R.string.firebase_profiles)).child(((BaseActivity) getActivity()).getUid()).child(key).setValue(profile);
                    mSoundProfileReference.child(getString(R.string.firebase_profiles)).child(((BaseActivity) getActivity()).getUid()).child(key).child(getString(R.string.mKey)).setValue(key + "");
                    Intent intent = new Intent(mContext, NewProfileActivity.class);
                    intent.putExtra(getString(R.string.key), key + "");
                    startActivity(intent);
                }
            }
        });

        return view;
    }

    // This method will be called when a MessageEvent is posted (in the UI thread)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ProfileDeletedEvent profileDeletedEvent) {
        if (profileDeletedEvent != null) {
            int profileDeleted = profileDeletedEvent.profileDeleted;
            profileArrayList.remove(profileDeleted);
            profilesListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStart() {
        FirebaseDatabase.getInstance().getReference().child("profiles").child(((BaseActivity) getActivity()).getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                profileArrayList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    SoundProfile profile = ds.getValue(SoundProfile.class);
                    profileArrayList.add(profile);
                }

                if (profileArrayList.size() > 0) {
                    profileArrayList.remove(profileArrayList.size() - 1);
                }

                if (profileArrayList.size() == 0) {
                    layoutNoProfiles.setVisibility(View.VISIBLE);
                    recyclerViewProfiles.setVisibility(View.GONE);
                } else {
                    layoutNoProfiles.setVisibility(View.GONE);
                    recyclerViewProfiles.setVisibility(View.VISIBLE);
                }
                profilesListAdapter.notifyDataSetChanged();
                //FirebaseDatabase.getInstance().getReference().child("profiles").child(((BaseActivity) getActivity()).getUid()).removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onDestroy();
    }
}

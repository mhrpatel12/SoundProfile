package com.appontherocks.soundprofile.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.appontherocks.soundprofile.R;
import com.appontherocks.soundprofile.activities.BaseActivity;
import com.appontherocks.soundprofile.adapter.ProfilesListAdapter;
import com.appontherocks.soundprofile.event.ProfileDeletedEvent;
import com.appontherocks.soundprofile.models.SoundProfile;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfilesFragment extends Fragment {

    private RecyclerView recyclerViewProfiles;
    private ProfilesListAdapter profilesListAdapter;
    private ArrayList<SoundProfile> profileArrayList = new ArrayList<>();

    private View view;

    private Context mContext;

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

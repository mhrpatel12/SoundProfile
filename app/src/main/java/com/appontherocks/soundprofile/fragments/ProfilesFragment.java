package com.appontherocks.soundprofile.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.appontherocks.soundprofile.R;
import com.appontherocks.soundprofile.activities.BaseActivity;
import com.appontherocks.soundprofile.adapter.ProfilesSwipableAdapter;
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

import in.arjsna.swipecardlib.SwipeCardView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfilesFragment extends Fragment {

    public SwipeCardView swipeCardView;
    private ProfilesSwipableAdapter profilesSwipableAdapter;
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

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        swipeCardView = (SwipeCardView) view.findViewById(R.id.card_stack_view);
        swipeCardView.setVisibility(View.VISIBLE);
        swipeCardView.setFlingListener(new SwipeCardView.OnCardFlingListener() {
            @Override
            public void onCardExitLeft(Object dataObject) {
            }

            @Override
            public void onCardExitRight(Object dataObject) {
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                if (itemsInAdapter == 0)
                    swipeCardView.restart();
            }

            @Override
            public void onScroll(float scrollProgressPercent) {
            }

            @Override
            public void onCardExitTop(Object dataObject) {
            }

            @Override
            public void onCardExitBottom(Object dataObject) {
            }
        });

        // Optionally add an OnItemClickListener
        swipeCardView.setOnItemClickListener(new SwipeCardView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {

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
            profilesSwipableAdapter.notifyDataSetChanged();
            swipeCardView.throwBottom();
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
                profilesSwipableAdapter = new ProfilesSwipableAdapter(getActivity(), R.layout.list_item_profile, profileArrayList);
                swipeCardView.setAdapter(profilesSwipableAdapter);
                FirebaseDatabase.getInstance().getReference().child("profiles").child(((BaseActivity) getActivity()).getUid()).removeEventListener(this);
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

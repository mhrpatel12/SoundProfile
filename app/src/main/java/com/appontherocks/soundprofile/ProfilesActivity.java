package com.appontherocks.soundprofile;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.appontherocks.soundprofile.models.SoundProfile;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import in.arjsna.swipecardlib.SwipeCardView;

public class ProfilesActivity extends BaseActivity {

    public SwipeCardView swipeCardView;
    private ProfilesSwipableAdapter profilesSwipableAdapter;
    private ArrayList<SoundProfile> profileArrayList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profiles);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        swipeCardView = (SwipeCardView) findViewById(R.id.card_stack_view);

        swipeCardView.setFlingListener(new SwipeCardView.OnCardFlingListener() {
            @Override
            public void onCardExitLeft(Object dataObject) {
            }

            @Override
            public void onCardExitRight(Object dataObject) {

            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                if (itemsInAdapter == 0) {
                    swipeCardView.requestLayout();
                }
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
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        FirebaseDatabase.getInstance().getReference().child("profiles").child(getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                profileArrayList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    SoundProfile profile = ds.getValue(SoundProfile.class);
                    profileArrayList.add(profile);
                }

                profilesSwipableAdapter = new ProfilesSwipableAdapter(ProfilesActivity.this, profileArrayList);
                swipeCardView.setAdapter(profilesSwipableAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        super.onStart();
    }
}

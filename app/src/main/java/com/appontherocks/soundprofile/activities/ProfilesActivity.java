package com.appontherocks.soundprofile.activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.appontherocks.soundprofile.R;
import com.appontherocks.soundprofile.adapter.ProfilesFoldingCellListAdapter;
import com.appontherocks.soundprofile.adapter.ProfilesSwipableAdapter;
import com.appontherocks.soundprofile.event.ProfileDeletedEvent;
import com.appontherocks.soundprofile.models.SoundProfile;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ramotion.foldingcell.FoldingCell;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class ProfilesActivity extends BaseActivity {

    //public SwipeCardView swipeCardView;
    private ListView recyclerViewProfiles;
    private ProfilesSwipableAdapter profilesSwipableAdapter;
    private ProfilesFoldingCellListAdapter profilesListAdapter;
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

        recyclerViewProfiles = (ListView) findViewById(R.id.mainListView);

 /*       swipeCardView = (SwipeCardView) findViewById(R.id.card_stack_view);
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
        });*/
    }

    // This method will be called when a MessageEvent is posted (in the UI thread)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ProfileDeletedEvent profileDeletedEvent) {
        if (profileDeletedEvent != null) {
            int profileDeleted = profileDeletedEvent.profileDeleted;
            profileArrayList.remove(profileDeleted);
            //profilesSwipableAdapter.notifyDataSetChanged();
            //swipeCardView.throwBottom();
            profilesListAdapter.notifyDataSetChanged();
        }
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

                if (profileArrayList.size() > 0) {
                    profileArrayList.remove(profileArrayList.size() - 1);
                }
                /*profilesSwipableAdapter = new ProfilesSwipableAdapter(ProfilesActivity.this, R.layout.list_item_profile, profileArrayList);
                swipeCardView.setAdapter(profilesSwipableAdapter);*/
                profilesListAdapter = new ProfilesFoldingCellListAdapter(ProfilesActivity.this, profileArrayList);
                profilesListAdapter.setDefaultRequestBtnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getApplicationContext(), "DEFAULT HANDLER FOR ALL BUTTONS", Toast.LENGTH_SHORT).show();
                    }
                });
                recyclerViewProfiles.setAdapter(profilesListAdapter);
                // set on click event listener to list view
                recyclerViewProfiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                        // toggle clicked cell state
                        ((FoldingCell) view).toggle(false);
                        // register in adapter that state for selected cell is toggled
                        profilesListAdapter.registerToggle(pos);
                    }
                });
                FirebaseDatabase.getInstance().getReference().child("profiles").child(getUid()).removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onDestroy();
    }
}

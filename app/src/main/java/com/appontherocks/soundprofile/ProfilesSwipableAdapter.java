package com.appontherocks.soundprofile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.appontherocks.soundprofile.models.SoundProfile;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * Created by Mihir on 4/23/2017.
 */

public class ProfilesSwipableAdapter extends ArrayAdapter<SoundProfile> {
    private final ArrayList<SoundProfile> cards;
    private final LayoutInflater layoutInflater;
    private Context mContext;

    public ProfilesSwipableAdapter(Context context, ArrayList<SoundProfile> cards) {
        super(context, -1);
        this.mContext = context;
        this.cards = cards;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SoundProfile card = cards.get(position);
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
        return view;
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

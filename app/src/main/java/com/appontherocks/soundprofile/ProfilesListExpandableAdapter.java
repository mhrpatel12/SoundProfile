package com.appontherocks.soundprofile;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.appontherocks.soundprofile.models.SoundProfile;
import com.bumptech.glide.Glide;
import com.ramotion.expandingcollection.ECCardContentListItemAdapter;

import java.util.List;

/**
 * Created by Mihir on 4/23/2017.
 */

public class ProfilesListExpandableAdapter extends ECCardContentListItemAdapter<SoundProfile> {

    private Context mContext;

    public ProfilesListExpandableAdapter(@NonNull Context context, List<SoundProfile> data) {
        super(context, R.layout.list_profiles_expanding, data);
        mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull ViewGroup viewGroup) {
        ViewHolder viewHolder;
        View rowView = view;

        if (rowView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            rowView = inflater.inflate(R.layout.list_profiles_expanding, null);
            viewHolder = new ViewHolder();
            viewHolder.txtProfileName = (TextView) rowView.findViewById(R.id.txtProfileName);
            viewHolder.imageView = (AppCompatImageView) rowView.findViewById(R.id.map_lite);
            rowView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) rowView.getTag();
        }

        SoundProfile item = getItem(position);
        if (item != null) {
            viewHolder.txtProfileName.setText(item.profileName + "");
            String getMapURL = "http://maps.googleapis.com/maps/api/staticmap?zoom=16&size=300x600&scale=2&markers=size:mid|color:red|"
                    + item.latitude
                    + ","
                    + item.longitude
                    + "&sensor=false";
            Glide.with(mContext)
                    .load(getMapURL)
                    .into(viewHolder.imageView);
        }

        return rowView;
    }

    public static class ViewHolder {
        TextView txtProfileName;

        AppCompatImageView imageView;
        AppCompatImageView btnDelete;
    }

}

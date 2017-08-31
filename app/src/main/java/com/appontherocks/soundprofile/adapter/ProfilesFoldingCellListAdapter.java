package com.appontherocks.soundprofile.adapter;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.appontherocks.soundprofile.R;
import com.appontherocks.soundprofile.models.SoundProfile;
import com.bumptech.glide.Glide;
import com.ramotion.foldingcell.FoldingCell;

import java.util.HashSet;
import java.util.List;

/**
 * Simple example of ListAdapter for using with Folding Cell
 * Adapter holds indexes of unfolded elements for correct work with default reusable views behavior
 */
public class ProfilesFoldingCellListAdapter extends ArrayAdapter<SoundProfile> {

    private HashSet<Integer> unfoldedIndexes = new HashSet<>();
    private View.OnClickListener defaultRequestBtnClickListener;

    public ProfilesFoldingCellListAdapter(Context context, List<SoundProfile> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // get item for selected view
        SoundProfile item = getItem(position);
        // if cell is exists - reuse it, if not - create the new one from resource
        FoldingCell cell = (FoldingCell) convertView;
        ViewHolder viewHolder;
        if (cell == null) {
            viewHolder = new ViewHolder();
            LayoutInflater vi = LayoutInflater.from(getContext());
            cell = (FoldingCell) vi.inflate(R.layout.cell, parent, false);
            // binding view parts to view holder
            viewHolder.cardView = (CardView) cell.findViewById(R.id.cv);
            viewHolder.txtProfileName = (TextView) cell.findViewById(R.id.txtProfileName);
            viewHolder.imageView = (AppCompatImageView) cell.findViewById(R.id.map_lite);
            viewHolder.btnDelete = (AppCompatImageView) cell.findViewById(R.id.btnDelete);

            cell.setTag(viewHolder);
        } else {
            // for existing cell set valid valid state(without animation)
            if (unfoldedIndexes.contains(position)) {
                cell.unfold(true);
            } else {
                cell.fold(true);
            }
            viewHolder = (ViewHolder) cell.getTag();
        }

        // bind data from selected element to view through view holder
        String getMapURL = "";
        viewHolder.txtProfileName.setText(item.profileName);

        if (((item.latitude != null) && (!(item.latitude + "").equals(""))) && ((item.longitude != null) && (!(item.longitude + "").equals("")))) {

            getMapURL = "http://maps.googleapis.com/maps/api/staticmap?zoom=16&size=450x900&scale=2&markers=size:mid|color:red|"
                    + item.latitude
                    + ","
                    + item.longitude
                    + "&sensor=false";
            Log.e("PROFILES ADAPTER", getMapURL);
            Glide.with(getContext())
                    .load(getMapURL)
                    .into(viewHolder.imageView);
            //new downloadMapImage().execute(getMapURL);
            //imageLoader.DisplayImage(getMapURL, holder.imageView);
        }

/*
        if ((profileArrayList.get(position).profileName + "").equals("Default Profile")) {
            cardView.setVisibility(View.GONE);
        }
*/


        return cell;
    }

    // simple methods for register cell state changes
    public void registerToggle(int position) {
        if (unfoldedIndexes.contains(position))
            registerFold(position);
        else
            registerUnfold(position);
    }

    public void registerFold(int position) {
        unfoldedIndexes.remove(position);
    }

    public void registerUnfold(int position) {
        unfoldedIndexes.add(position);
    }

    public View.OnClickListener getDefaultRequestBtnClickListener() {
        return defaultRequestBtnClickListener;
    }

    public void setDefaultRequestBtnClickListener(View.OnClickListener defaultRequestBtnClickListener) {
        this.defaultRequestBtnClickListener = defaultRequestBtnClickListener;
    }

    // View lookup cache
    private static class ViewHolder {
        TextView txtProfileName;
        CardView cardView;
        AppCompatImageView imageView;
        AppCompatImageView btnDelete;
    }
}

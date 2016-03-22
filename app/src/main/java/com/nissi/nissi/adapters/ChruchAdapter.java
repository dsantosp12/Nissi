package com.nissi.nissi.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nissi.nissi.R;
import com.nissi.nissi.model.Place;

import java.util.List;

public class ChruchAdapter extends BaseAdapter {

    private Context mContext;
    private List<Place> mPlaces;

    public ChruchAdapter(Context context, List<Place> places) {
        mContext = context;
        mPlaces = places;
    }

    @Override
    public int getCount() {
        return mPlaces.size();
    }

    @Override
    public Object getItem(int position) {
        return mPlaces.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.church_list_item, null);
            holder = new ViewHolder();
            holder.mTitle = (TextView) convertView.findViewById(R.id.itemTitleTextView);
            holder.mDistance = (TextView) convertView.findViewById(R.id.itemDistanceTextView);
            holder.mPlaceID = (TextView) convertView.findViewById(R.id.itemPlaceIDTextView);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Place place = mPlaces.get(position);
        holder.mTitle.setText(place.getName());
        holder.mDistance.setText(place.getDistance() + " mi");
        holder.mPlaceID.setText(place.getId());

        return convertView;
    }

    private static class ViewHolder {
        TextView mTitle;
        TextView mDistance;
        TextView mPlaceID;
    }
}

package com.juliazluo.www.simpleweather;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by julia on 2017-03-10.
 */

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.CustomViewHolder> {

    ArrayList<DayOfWeek> days;

    public ListAdapter(ArrayList<DayOfWeek> days) {
        this.days = days;
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_item, parent, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
        // Display the day of week information
        DayOfWeek dayOfWeek = days.get(position);
        holder.dayTxt.setText(dayOfWeek.getDay());
        holder.minTxt.setText("Min: " + dayOfWeek.getMinTemp());
        holder.maxTxt.setText("Max: " + dayOfWeek.getMaxTemp());
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        TextView dayTxt, minTxt, maxTxt;

        public CustomViewHolder(View view) {
            // Initialize the components in row view
            super(view);
            this.dayTxt = (TextView) view.findViewById(R.id.day_of_week);
            this.minTxt = (TextView) view.findViewById(R.id.min_temp_txt);
            this.maxTxt = (TextView) view.findViewById(R.id.max_temp_txt);
        }
    }
}
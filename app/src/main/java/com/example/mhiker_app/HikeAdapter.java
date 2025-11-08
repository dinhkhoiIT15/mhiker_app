package com.example.mhiker_app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HikeAdapter extends RecyclerView.Adapter<HikeAdapter.HikeViewHolder> {

    private List<Hike> hikeList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Hike hike);
    }

    public HikeAdapter(List<Hike> hikeList, OnItemClickListener listener) {
        this.hikeList = hikeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HikeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.hike_item, parent, false);
        return new HikeViewHolder(itemView, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull HikeViewHolder holder, int position) {
        Hike currentHike = hikeList.get(position);
        holder.tvHikeName.setText(currentHike.getName());
        holder.tvHikeLocation.setText("Location: " + currentHike.getLocation());
        holder.tvHikeDate.setText("Date: " + currentHike.getDateOfHike());
        holder.bind(currentHike);
    }

    @Override
    public int getItemCount() {
        return hikeList.size();
    }

    public void setData(List<Hike> newHikeList) {
        this.hikeList = newHikeList;
        notifyDataSetChanged();
    }

    public static class HikeViewHolder extends RecyclerView.ViewHolder {
        public TextView tvHikeName, tvHikeLocation, tvHikeDate;
        private Hike currentHike;

        public HikeViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            tvHikeName = itemView.findViewById(R.id.tvHikeName);
            tvHikeLocation = itemView.findViewById(R.id.tvHikeLocation);
            tvHikeDate = itemView.findViewById(R.id.tvHikeDate);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(currentHike);
                }
            });
        }

        public void bind(Hike hike) {
            this.currentHike = hike;
        }
    }
}
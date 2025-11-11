package com.example.mhiker_app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ObservationAdapter extends RecyclerView.Adapter<ObservationAdapter.ObservationViewHolder> {

    private List<Observation> observationList;
    private OnObservationListener listener;

    public interface OnObservationListener {
        void onEditClick(Observation observation);
        void onDeleteClick(Observation observation);
    }

    public ObservationAdapter(List<Observation> observationList, OnObservationListener listener) {
        this.observationList = observationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ObservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.observation_item, parent, false);
        return new ObservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ObservationViewHolder holder, int position) {
        Observation currentObservation = observationList.get(position);
        holder.tvObservationText.setText(currentObservation.getObservationText());
        holder.tvObservationTime.setText("Time: " + currentObservation.getTimeOfObservation());

        if (currentObservation.getAdditionalComments() != null && !currentObservation.getAdditionalComments().isEmpty()) {
            holder.tvObservationComments.setText(currentObservation.getAdditionalComments());
            holder.tvObservationComments.setVisibility(View.VISIBLE);
        } else {
            holder.tvObservationComments.setVisibility(View.GONE);
        }

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(currentObservation);
            }
        });
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(currentObservation);
            }
        });
    }

    @Override
    public int getItemCount() {
        return observationList.size();
    }

    public void setData(List<Observation> newObservations) {
        this.observationList = newObservations;
        notifyDataSetChanged();
    }

    public static class ObservationViewHolder extends RecyclerView.ViewHolder {
        TextView tvObservationText, tvObservationTime, tvObservationComments;
        ImageButton btnEdit, btnDelete;

        public ObservationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvObservationText = itemView.findViewById(R.id.tvObservationText);
            tvObservationTime = itemView.findViewById(R.id.tvObservationTime);
            tvObservationComments = itemView.findViewById(R.id.tvObservationComments);
            btnEdit = itemView.findViewById(R.id.btnEditObservation);
            btnDelete = itemView.findViewById(R.id.btnDeleteObservation);
        }
    }
}
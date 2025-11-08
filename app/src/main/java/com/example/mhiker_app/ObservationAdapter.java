package com.example.mhiker_app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton; // THAY ĐỔI: Import mới
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

        // THAY ĐỔI: Hiển thị hoặc ẩn phần comment
        if (currentObservation.getAdditionalComments() != null && !currentObservation.getAdditionalComments().isEmpty()) {
            holder.tvObservationComments.setText(currentObservation.getAdditionalComments());
            holder.tvObservationComments.setVisibility(View.VISIBLE);
        } else {
            holder.tvObservationComments.setVisibility(View.GONE);
        }

        // Gán listener cho nút Edit và Delete
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

    // Cập nhật dữ liệu cho Adapter
    public void setData(List<Observation> newObservations) {
        this.observationList = newObservations;
        notifyDataSetChanged(); // Thông báo cho RecyclerView cập nhật
    }

    // ViewHolder
    public static class ObservationViewHolder extends RecyclerView.ViewHolder {
        TextView tvObservationText, tvObservationTime, tvObservationComments;
        // THAY ĐỔI: Sử dụng ImageButton
        ImageButton btnEdit, btnDelete;

        public ObservationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvObservationText = itemView.findViewById(R.id.tvObservationText);
            tvObservationTime = itemView.findViewById(R.id.tvObservationTime);
            tvObservationComments = itemView.findViewById(R.id.tvObservationComments);
            // THAY ĐỔI: Cập nhật findViewById
            btnEdit = itemView.findViewById(R.id.btnEditObservation);
            btnDelete = itemView.findViewById(R.id.btnDeleteObservation);
        }
    }
}
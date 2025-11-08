package com.example.mhiker_app;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;

import java.text.DateFormat;
import java.util.Calendar;

public class AdvancedSearchFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private TextInputEditText etSearchName, etSearchLocation, etSearchLength, etSearchDate;
    private Button btnPerformSearch, btnResetSearch;
    private AdvancedSearchListener listener;

    // Interface để gửi dữ liệu về MainActivity
    public interface AdvancedSearchListener {
        void onSearchClicked(String name, String location, String length, String date);
        void onResetSearchClicked();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            // Ép kiểu MainActivity thành listener
            listener = (AdvancedSearchListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement AdvancedSearchListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_advanced_search, null);

        etSearchName = view.findViewById(R.id.etSearchName);
        etSearchLocation = view.findViewById(R.id.etSearchLocation);
        etSearchLength = view.findViewById(R.id.etSearchLength);
        etSearchDate = view.findViewById(R.id.etSearchDate);
        btnPerformSearch = view.findViewById(R.id.btnPerformSearch);
        btnResetSearch = view.findViewById(R.id.btnResetSearch);

        etSearchDate.setOnClickListener(v -> {
            // Sử dụng getParentFragmentManager() để hiển thị DatePicker
            DatePickerFragment datePicker = new DatePickerFragment();
            // Đặt Fragment này làm mục tiêu để nhận callback onDateSet
            datePicker.setTargetFragment(this, 0);
            datePicker.show(getParentFragmentManager(), "datePickerSearch");
        });

        btnPerformSearch.setOnClickListener(v -> {
            String name = etSearchName.getText().toString().trim();
            String location = etSearchLocation.getText().toString().trim();
            String length = etSearchLength.getText().toString().trim();
            String date = etSearchDate.getText().toString().trim();
            // Gọi interface về MainActivity
            listener.onSearchClicked(name, location, length, date);
            dismiss(); // Đóng dialog
        });

        btnResetSearch.setOnClickListener(v -> {
            etSearchName.setText("");
            etSearchLocation.setText("");
            etSearchLength.setText("");
            etSearchDate.setText("");
            // Gọi interface về MainActivity
            listener.onResetSearchClicked();
            dismiss(); // Đóng dialog
        });

        builder.setView(view);
        return builder.create();
    }

    // Callback từ DatePickerFragment
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String currentDateString = DateFormat.getDateInstance(DateFormat.MEDIUM).format(c.getTime());
        // Hiển thị ngày đã chọn lên etSearchDate
        etSearchDate.setText(currentDateString);
    }
}
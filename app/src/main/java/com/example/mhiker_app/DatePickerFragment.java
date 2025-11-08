package com.example.mhiker_app;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // THAY ĐỔI: Kiểm tra xem target fragment (Fragment gọi) có được set hay không
        DatePickerDialog.OnDateSetListener listener = (DatePickerDialog.OnDateSetListener) getTargetFragment();

        // Nếu không (ví dụ: được gọi từ AddHikeActivity), thì dùng Activity
        if (listener == null) {
            listener = (DatePickerDialog.OnDateSetListener) getActivity();
        }

        // Trả về DatePickerDialog cho listener đã xác định
        return new DatePickerDialog(requireActivity(), listener, year, month, day);
    }
}
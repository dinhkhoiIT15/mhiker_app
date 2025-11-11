// app/src/main/java/com/example/mhiker_app/ResetPasswordActivity.java
package com.example.mhiker_app;

import android.os.Bundle;
import android.widget.TextView;
// XÓA: import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ResetPasswordActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPhone, etNewPassword;
    private MaterialButton btnReset;
    private TextView tvBackToLogin;
    private DatabaseHelper dbHelper;
    private static final int SNACKBAR_DURATION = 2500; // 2.5 giây

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        dbHelper = new DatabaseHelper(this);
        // Tên biến này khớp với mã nguồn mới của bạn
        etUsername = findViewById(R.id.etResetUsername);
        etPhone = findViewById(R.id.etResetPhone);
        etNewPassword = findViewById(R.id.etResetNewPassword);
        btnReset = findViewById(R.id.btnResetPassword);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        btnReset.setOnClickListener(v -> handleResetPassword());
        tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void handleResetPassword() {
        String username = etUsername.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();

        if (username.isEmpty() || phone.isEmpty() || newPassword.isEmpty()) {
            // THAY THẾ TOAST
            SnackbarHelper.showCustomSnackbar(
                    btnReset,
                    "Please fill all fields",
                    SnackbarHelper.TYPE_ERROR,
                    SNACKBAR_DURATION
            );
            return;
        }

        // Logic kiểm tra từ mã nguồn mới của bạn
        boolean userExists = dbHelper.checkUserForReset(username, phone);

        if (userExists) {
            int rowsAffected = dbHelper.updatePassword(username, newPassword);
            if (rowsAffected > 0) {
                // THAY THẾ TOAST
                SnackbarHelper.showCustomSnackbar(
                        btnReset,
                        "Password reset successful! You can login now.",
                        SnackbarHelper.TYPE_SUCCESS,
                        SNACKBAR_DURATION
                );
                finish();
            } else {
                // THAY THẾ TOAST
                SnackbarHelper.showCustomSnackbar(
                        btnReset,
                        "Error updating password. Please try again.",
                        SnackbarHelper.TYPE_ERROR,
                        SNACKBAR_DURATION
                );
            }
        } else {
            // THAY THẾ TOAST
            SnackbarHelper.showCustomSnackbar(
                    btnReset,
                    "Username or Phone Number is incorrect",
                    SnackbarHelper.TYPE_ERROR,
                    SNACKBAR_DURATION
            );
        }
    }
}
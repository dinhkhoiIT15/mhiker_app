// app/src/main/java/com/example/mhiker_app/ResetPasswordActivity.java

package com.example.mhiker_app;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ResetPasswordActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPhone, etNewPassword;
    private MaterialButton btnReset;
    private TextView tvBackToLogin;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        dbHelper = new DatabaseHelper(this);
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
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Kiểm tra xem username và SĐT có khớp không
        boolean userExists = dbHelper.checkUserForReset(username, phone);

        if (userExists) {
            // 2. Nếu khớp, cập nhật mật khẩu
            int rowsAffected = dbHelper.updatePassword(username, newPassword);
            if (rowsAffected > 0) {
                Toast.makeText(this, "Password reset successful! You can login now.", Toast.LENGTH_LONG).show();
                finish(); // Quay lại Login
            } else {
                Toast.makeText(this, "Error updating password. Please try again.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // 3. Nếu không, thông báo lỗi
            Toast.makeText(this, "Username or Phone Number is incorrect", Toast.LENGTH_SHORT).show();
        }
    }
}
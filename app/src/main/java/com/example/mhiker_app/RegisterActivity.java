// app/src/main/java/com/example/mhiker_app/RegisterActivity.java
package com.example.mhiker_app;

import android.os.Bundle;
import android.widget.TextView;
// XÓA: import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etName, etUsername, etPhone, etPassword, etConfirmPassword;
    private MaterialButton btnRegister;
    private TextView tvGoToLogin;
    private DatabaseHelper dbHelper;
    private static final int SNACKBAR_DURATION = 2500; // 2.5 giây

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);
        // Tên biến này khớp với mã nguồn mới của bạn
        etName = findViewById(R.id.etRegName);
        etUsername = findViewById(R.id.etRegUsername);
        etPhone = findViewById(R.id.etRegPhone);
        etPassword = findViewById(R.id.etRegPassword);
        etConfirmPassword = findViewById(R.id.etRegConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        btnRegister.setOnClickListener(v -> handleRegister());
        tvGoToLogin.setOnClickListener(v -> finish());
    }

    private void handleRegister() {
        String name = etName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (name.isEmpty() || username.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            // THAY THẾ TOAST
            SnackbarHelper.showCustomSnackbar(
                    btnRegister,
                    "Please fill all required fields",
                    SnackbarHelper.TYPE_ERROR,
                    SNACKBAR_DURATION
            );
            return;
        }

        if (!password.equals(confirmPassword)) {
            // THAY THẾ TOAST
            SnackbarHelper.showCustomSnackbar(
                    btnRegister,
                    "Passwords do not match",
                    SnackbarHelper.TYPE_ERROR,
                    SNACKBAR_DURATION
            );
            return;
        }

        if (dbHelper.checkUsernameExists(username)) {
            // THAY THẾ TOAST
            SnackbarHelper.showCustomSnackbar(
                    btnRegister,
                    "Username already exists",
                    SnackbarHelper.TYPE_ERROR,
                    SNACKBAR_DURATION
            );
            return;
        }

        // Logic tạo User từ mã nguồn mới của bạn
        User newUser = new User();
        newUser.setName(name);
        newUser.setUsername(username);
        newUser.setPhoneNumber(phone);
        newUser.setPassword(password);

        long newUserId = dbHelper.addUser(newUser);

        if (newUserId != -1) {
            // THAY THẾ TOAST
            SnackbarHelper.showCustomSnackbar(
                    btnRegister,
                    "Registration Successful! Please login.",
                    SnackbarHelper.TYPE_SUCCESS,
                    SNACKBAR_DURATION
            );
            finish();
        } else {
            // THAY THẾ TOAST
            SnackbarHelper.showCustomSnackbar(
                    btnRegister,
                    "Registration Failed. Please try again.",
                    SnackbarHelper.TYPE_ERROR,
                    SNACKBAR_DURATION
            );
        }
    }
}
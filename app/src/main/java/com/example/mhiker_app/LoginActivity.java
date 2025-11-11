// app/src/main/java/com/example/mhiker_app/LoginActivity.java
package com.example.mhiker_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
// XÓA: import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword;
    private MaterialButton btnLogin;
    private TextView tvGoToRegister, tvForgotPassword;
    private DatabaseHelper dbHelper;
    private static final int SNACKBAR_DURATION = 2500; // 2.5 giây

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);
        etUsername = findViewById(R.id.etLoginUsername);
        etPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoToRegister = findViewById(R.id.tvGoToRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        btnLogin.setOnClickListener(v -> handleLogin());

        tvGoToRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );

        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class))
        );
    }

    private void handleLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            // THAY THẾ TOAST
            SnackbarHelper.showCustomSnackbar(
                    btnLogin,
                    "Please enter username and password",
                    SnackbarHelper.TYPE_ERROR,
                    SNACKBAR_DURATION
            );
            return;
        }

        // Logic kiểm tra từ mã nguồn mới của bạn
        boolean loginSuccess = dbHelper.checkUser(username, password);

        if (loginSuccess) {
            SharedPreferences prefs = getSharedPreferences(SplashActivity.PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(SplashActivity.KEY_IS_LOGGED_IN, true);
            editor.putString("LOGGED_IN_USERNAME", username);
            editor.apply();

            // THAY THẾ TOAST
            SnackbarHelper.showCustomSnackbar(
                    btnLogin,
                    "Login Successful!",
                    SnackbarHelper.TYPE_SUCCESS,
                    SNACKBAR_DURATION
            );

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            // THAY THẾ TOAST
            SnackbarHelper.showCustomSnackbar(
                    btnLogin,
                    "Invalid username or password",
                    SnackbarHelper.TYPE_ERROR,
                    SNACKBAR_DURATION
            );
        }
    }
}
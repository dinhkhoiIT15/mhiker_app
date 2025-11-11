// app/src/main/java/com/example/mhiker_app/LoginActivity.java

package com.example.mhiker_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword;
    private MaterialButton btnLogin;
    private TextView tvGoToRegister, tvForgotPassword;
    private DatabaseHelper dbHelper;

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
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra thông tin đăng nhập với DB
        boolean loginSuccess = dbHelper.checkUser(username, password);

        if (loginSuccess) {
            // Lưu trạng thái đăng nhập vào SharedPreferences [cite: 973-977]
            SharedPreferences prefs = getSharedPreferences(SplashActivity.PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(SplashActivity.KEY_IS_LOGGED_IN, true);
            editor.putString("LOGGED_IN_USERNAME", username);
            editor.apply(); // Sử dụng apply() thay vì commit() để chạy bất đồng bộ

            // Chuyển đến MainActivity
            Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            // Xóa tất cả các Activity trước đó khỏi back stack [cite: 338, 339]
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            // Hiển thị thông báo lỗi
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
        }
    }
}
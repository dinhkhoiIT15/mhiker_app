// app/src/main/java/com/example/mhiker_app/RegisterActivity.java

package com.example.mhiker_app;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etName, etUsername, etPhone, etPassword, etConfirmPassword;
    private MaterialButton btnRegister;
    private TextView tvGoToLogin;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);
        etName = findViewById(R.id.etRegName);
        etUsername = findViewById(R.id.etRegUsername);
        etPhone = findViewById(R.id.etRegPhone);
        etPassword = findViewById(R.id.etRegPassword);
        etConfirmPassword = findViewById(R.id.etRegConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        btnRegister.setOnClickListener(v -> handleRegister());

        tvGoToLogin.setOnClickListener(v -> finish()); // Quay lại màn hình Login
    }

    private void handleRegister() {
        String name = etName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // 1. Kiểm tra validation
        if (name.isEmpty() || username.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Kiểm tra username đã tồn tại chưa
        if (dbHelper.checkUsernameExists(username)) {
            Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Tạo User và thêm vào DB
        User newUser = new User();
        newUser.setName(name);
        newUser.setUsername(username);
        newUser.setPhoneNumber(phone);
        newUser.setPassword(password); // Nhắc lại: Nên mã hóa mật khẩu này!

        long newUserId = dbHelper.addUser(newUser);

        if (newUserId != -1) {
            Toast.makeText(this, "Registration Successful! Please login.", Toast.LENGTH_LONG).show();
            finish(); // Quay lại màn hình Login
        } else {
            Toast.makeText(this, "Registration Failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}
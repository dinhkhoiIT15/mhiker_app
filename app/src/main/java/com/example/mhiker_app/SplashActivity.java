// app/src/main/java/com/example/mhiker_app/SplashActivity.java

package com.example.mhiker_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "M_HIKER_PREFS";
    public static final String KEY_IS_LOGGED_IN = "IS_LOGGED_IN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Sử dụng Handler để trì hoãn việc chuyển Activity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Đọc SharedPreferences để kiểm tra trạng thái đăng nhập
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            boolean isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);

            Intent intent;
            if (isLoggedIn) {
                // Nếu đã đăng nhập, chuyển đến MainActivity
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                // Nếu chưa, chuyển đến LoginActivity
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            finish(); // Đóng SplashActivity để người dùng không thể quay lại
        }, 1500); // Trì hoãn 1.5 giây
    }
}
// app/src/main/java/com/example/mhiker_app/SnackbarHelper.java
package com.example.mhiker_app;

import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.google.android.material.snackbar.Snackbar;

public class SnackbarHelper {

    // Định nghĩa các loại thông báo
    public static final String TYPE_SUCCESS = "SUCCESS";
    public static final String TYPE_ERROR = "ERROR";
    public static final String TYPE_INFO = "INFO";

    /**
     * Hiển thị một Snackbar tùy chỉnh trượt từ trên xuống.
     *
     * @param view     Một View bất kỳ trong layout hiện tại (dùng để tìm root view).
     * @param message  Tin nhắn cần hiển thị.
     * @param type     Loại tin nhắn (TYPE_SUCCESS, TYPE_ERROR, TYPE_INFO).
     * @param duration Thời lượng hiển thị (tính bằng mili giây).
     */
    public static void showCustomSnackbar(View view, String message, String type, int duration) {
        if (view == null) {
            return; // Không thể hiển thị nếu view là null
        }

        // Tìm view gốc (root view) của Activity
        View rootView = view.getRootView().findViewById(android.R.id.content);
        if (rootView == null) {
            rootView = view; // Dự phòng
        }

        // 1. Tạo Snackbar
        // Dùng LENGTH_INDEFINITE và setDuration để có thể đặt thời gian tùy chỉnh
        Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setDuration(duration); // Đặt thời lượng tùy chỉnh

        // 2. Lấy View của Snackbar
        View snackbarView = snackbar.getView();

        // 3. Thay đổi màu sắc dựa trên loại thông báo
        String backgroundColor;
        switch (type) {
            case TYPE_SUCCESS:
                backgroundColor = "#4CAF50"; // Màu xanh lá
                break;
            case TYPE_ERROR:
                backgroundColor = "#D32F2F"; // Màu đỏ
                break;
            case TYPE_INFO:
            default:
                backgroundColor = "#2196F3"; // Màu xanh dương
                break;
        }
        snackbarView.setBackgroundColor(Color.parseColor(backgroundColor));

        // Đặt màu chữ là TRẮNG
        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);

        // 4. Di chuyển Snackbar lên trên cùng
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) snackbarView.getLayoutParams();
        params.gravity = Gravity.TOP;
        snackbarView.setLayoutParams(params);

        // 5. Hiển thị Snackbar
        snackbar.show();
    }
}
// app/src/main/java/com/example/mhiker_app/AddObservationActivity.java
package com.example.mhiker_app;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView; // THÊM MỚI

import java.io.File; // THÊM MỚI
import java.io.FileOutputStream; // THÊM MỚI
import java.io.InputStream; // THÊM MỚI
import java.io.OutputStream; // THÊM MỚI
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID; // THÊM MỚI

import coil.Coil; // THÊM MỚI
import coil.ImageLoader; // THÊM MỚI
import coil.request.ImageRequest; // THÊM MỚI

public class AddObservationActivity extends AppCompatActivity {

    private EditText etObservation, etTime, etComments;
    private Button btnSaveObservation, btnAddImage; // CẬP NHẬT
    private ImageView imgObservationPreview; // THÊM MỚI
    private DatabaseHelper dbHelper;

    private long hikeId;
    private Observation observationToEdit = null;
    private String currentImagePath = null; // THÊM MỚI
    private static final int SNACKBAR_DURATION = 2500;

    // THÊM MỚI: Trình khởi chạy Photo Picker
    private final ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    // Người dùng đã chọn ảnh, sao chép nó vào bộ nhớ trong
                    currentImagePath = saveImageToInternalStorage(uri);
                    if (currentImagePath != null) {
                        loadImage(currentImagePath);
                        imgObservationPreview.setVisibility(View.VISIBLE);
                    } else {
                        SnackbarHelper.showCustomSnackbar(
                                btnSaveObservation,
                                "Failed to save image.",
                                SnackbarHelper.TYPE_ERROR,
                                SNACKBAR_DURATION
                        );
                    }
                } else {
                    // Người dùng đã hủy
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_observation);

        dbHelper = new DatabaseHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbar_add_observation);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        etObservation = findViewById(R.id.etObservation);
        etTime = findViewById(R.id.etTimeOfObservation);
        etComments = findViewById(R.id.etAdditionalComments);
        btnSaveObservation = findViewById(R.id.btnSaveObservation);
        // THÊM MỚI
        btnAddImage = findViewById(R.id.btnAddImage);
        imgObservationPreview = findViewById(R.id.imgObservationPreview);

        if (getIntent().hasExtra("EDIT_OBSERVATION")) {
            observationToEdit = (Observation) getIntent().getSerializableExtra("EDIT_OBSERVATION");
            hikeId = observationToEdit.getHikeId();
            getSupportActionBar().setTitle("Edit Observation");
            btnSaveObservation.setText("Update Observation");
            populateFields();
        } else {
            hikeId = getIntent().getLongExtra("HIKE_ID", -1);
            getSupportActionBar().setTitle("Add Observation");
            etTime.setText(getCurrentTimestamp());
        }

        if (hikeId == -1 && observationToEdit == null) {
            SnackbarHelper.showCustomSnackbar(
                    btnSaveObservation,
                    "Error: Hike ID is missing.",
                    SnackbarHelper.TYPE_ERROR,
                    SNACKBAR_DURATION
            );
            finish();
            return;
        }

        btnSaveObservation.setOnClickListener(v -> saveObservation());
        // THÊM MỚI: Xử lý nhấp nút thêm ảnh
        btnAddImage.setOnClickListener(v -> launchPhotoPicker());
    }

    private void populateFields() {
        etObservation.setText(observationToEdit.getObservationText());
        etTime.setText(observationToEdit.getTimeOfObservation());
        etComments.setText(observationToEdit.getAdditionalComments());
        // THÊM MỚI: Tải ảnh đã lưu
        currentImagePath = observationToEdit.getImagePath();
        if (currentImagePath != null && !currentImagePath.isEmpty()) {
            loadImage(currentImagePath);
            imgObservationPreview.setVisibility(View.VISIBLE);
        }
    }

    // THÊM MỚI: Khởi chạy Photo Picker
    private void launchPhotoPicker() {
        // Chỉ chấp nhận hình ảnh
        pickMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    // THÊM MỚI: Sao chép URI đã chọn vào bộ nhớ trong
    private String saveImageToInternalStorage(Uri uri) {
        try {
            // Mở luồng đọc từ URI
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            // Tạo tệp đích trong thư mục tệp riêng tư của ứng dụng
            String fileName = "IMG_" + UUID.randomUUID().toString() + ".jpg";
            File file = new File(getFilesDir(), fileName);
            OutputStream outputStream = new FileOutputStream(file);

            // Sao chép dữ liệu
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            // Đóng luồng
            outputStream.close();
            inputStream.close();

            // Trả về đường dẫn tuyệt đối đến tệp đã lưu
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // THÊM MỚI: Tải ảnh bằng Coil
    private void loadImage(String path) {
        ImageLoader imageLoader = Coil.imageLoader(this);
        ImageRequest request = new ImageRequest.Builder(this)
                .data(new File(path))
                .target(imgObservationPreview)
                .build();
        imageLoader.enqueue(request);
    }

    private void saveObservation() {
        String observationText = etObservation.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String comments = etComments.getText().toString().trim();

        if (observationText.isEmpty() || time.isEmpty()) {
            SnackbarHelper.showCustomSnackbar(
                    btnSaveObservation,
                    "Observation and Time are required.",
                    SnackbarHelper.TYPE_ERROR,
                    SNACKBAR_DURATION
            );
            return;
        }

        if (observationToEdit == null) {
            Observation newObservation = new Observation();
            newObservation.setObservationText(observationText);
            newObservation.setTimeOfObservation(time);
            newObservation.setAdditionalComments(comments);
            newObservation.setHikeId(hikeId);
            newObservation.setImagePath(currentImagePath); // CẬP NHẬT
            dbHelper.addObservation(newObservation);
            SnackbarHelper.showCustomSnackbar(
                    btnSaveObservation,
                    "Observation saved.",
                    SnackbarHelper.TYPE_SUCCESS,
                    SNACKBAR_DURATION
            );
        } else {
            observationToEdit.setObservationText(observationText);
            observationToEdit.setTimeOfObservation(time);
            observationToEdit.setAdditionalComments(comments);
            observationToEdit.setImagePath(currentImagePath); // CẬP NHẬT
            dbHelper.updateObservation(observationToEdit);
            SnackbarHelper.showCustomSnackbar(
                    btnSaveObservation,
                    "Observation updated.",
                    SnackbarHelper.TYPE_SUCCESS,
                    SNACKBAR_DURATION
            );
        }

        setResult(Activity.RESULT_OK);
        finish();
    }

    private String getCurrentTimestamp() {
        return new SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
    }
}
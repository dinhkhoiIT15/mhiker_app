// app/src/main/java/com/example/mhiker_app/AddObservationActivity.java
package com.example.mhiker_app;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts; // CẬP NHẬT
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat; // THÊM
import androidx.core.content.FileProvider; // THÊM

import android.Manifest; // THÊM
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager; // THÊM
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.File;
// Xóa các import không cần thiết: FileOutputStream, InputStream, OutputStream, UUID
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import coil.Coil;
import coil.ImageLoader;
import coil.request.ImageRequest;

public class AddObservationActivity extends AppCompatActivity {

    private EditText etObservation, etTime, etComments;
    private Button btnSaveObservation, btnAddImage;
    private ImageView imgObservationPreview;
    private DatabaseHelper dbHelper;

    private long hikeId;
    private Observation observationToEdit = null;

    // CẬP NHẬT: Đây là đường dẫn Uri trỏ đến nơi ảnh SẼ ĐƯỢC LƯU
    private Uri currentImageUri = null;
    // CẬP NHẬT: Đây là đường dẫn (dưới dạng String) để lưu vào DB
    private String currentImagePathString = null;

    private static final int SNACKBAR_DURATION = 2500;

    // THÊM MỚI: Trình khởi chạy yêu cầu quyền Camera
    private final ActivityResultLauncher<String> requestCameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Quyền được cấp, khởi chạy camera
                    launchCamera();
                } else {
                    // Quyền bị từ chối
                    SnackbarHelper.showCustomSnackbar(
                            btnSaveObservation,
                            "Camera permission denied.",
                            SnackbarHelper.TYPE_ERROR,
                            SNACKBAR_DURATION
                    );
                }
            });

    // CẬP NHẬT: Trình khởi chạy Camera (thay thế cho Photo Picker)
    private final ActivityResultLauncher<Uri> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success) {
                    // Ảnh đã được chụp và lưu thành công vào currentImageUri
                    // Lưu Uri này (dưới dạng String) để lưu vào DB
                    currentImagePathString = currentImageUri.toString();
                    loadImage(currentImagePathString); // Tải ảnh vào ImageView
                    imgObservationPreview.setVisibility(View.VISIBLE);
                } else {
                    // Người dùng đã hủy chụp ảnh
                    currentImageUri = null; // Reset Uri
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
        btnAddImage = findViewById(R.id.btnAddImage);
        imgObservationPreview = findViewById(R.id.imgObservationPreview);

        // CẬP NHẬT: Đổi văn bản nút
        btnAddImage.setText("Take Photo");

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

        // CẬP NHẬT: Xử lý nhấp nút camera
        btnAddImage.setOnClickListener(v -> checkCameraPermissionAndLaunch());
    }

    private void populateFields() {
        etObservation.setText(observationToEdit.getObservationText());
        etTime.setText(observationToEdit.getTimeOfObservation());
        etComments.setText(observationToEdit.getAdditionalComments());

        // CẬP NHẬT: Tải ảnh đã lưu (giờ là Uri string)
        currentImagePathString = observationToEdit.getImagePath();
        if (currentImagePathString != null && !currentImagePathString.isEmpty()) {
            loadImage(currentImagePathString);
            imgObservationPreview.setVisibility(View.VISIBLE);
        }
    }

    // THÊM MỚI: 1. Kiểm tra quyền Camera
    private void checkCameraPermissionAndLaunch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Quyền đã được cấp
            launchCamera();
        } else {
            // Yêu cầu quyền
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    // THÊM MỚI: 2. Tạo Uri cho tệp ảnh mới
    private Uri createTempImageUri() {
        // Tạo một thư mục con "images" trong getFilesDir()
        File imagePath = new File(getFilesDir(), "images");
        if (!imagePath.exists()) {
            imagePath.mkdirs();
        }

        // Tạo một tệp ảnh trống
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File newFile = new File(imagePath, "IMG_" + timeStamp + ".jpg");

        // Lấy Uri cho tệp đó bằng FileProvider
        return FileProvider.getUriForFile(
                this,
                getApplicationContext().getPackageName() + ".fileprovider",
                newFile
        );
    }

    // CẬP NHẬT: 3. Khởi chạy Camera
    private void launchCamera() {
        // Tạo một Uri mới cho ảnh sắp chụp
        currentImageUri = createTempImageUri();
        if (currentImageUri != null) {
            // Khởi chạy camera và truyền Uri đích
            takePictureLauncher.launch(currentImageUri);
        } else {
            SnackbarHelper.showCustomSnackbar(
                    btnSaveObservation,
                    "Failed to create image file.",
                    SnackbarHelper.TYPE_ERROR,
                    SNACKBAR_DURATION
            );
        }
    }

    // XÓA: Hàm saveImageToInternalStorage(Uri uri) đã bị xóa

    // CẬP NHẬT: Tải ảnh bằng Coil từ Uri (dưới dạng String)
    private void loadImage(String path) {
        if (path == null || path.isEmpty()) return;

        ImageLoader imageLoader = Coil.imageLoader(this);
        ImageRequest request = new ImageRequest.Builder(this)
                // Phân tích chuỗi path trở lại thành Uri
                .data(Uri.parse(path))
                .target(imgObservationPreview)
                .error(android.R.drawable.ic_menu_report_image) // Ảnh lỗi
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
            // CẬP NHẬT: Lưu chuỗi Uri
            newObservation.setImagePath(currentImagePathString);
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
            // CẬP NHẬT: Lưu chuỗi Uri
            observationToEdit.setImagePath(currentImagePathString);
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
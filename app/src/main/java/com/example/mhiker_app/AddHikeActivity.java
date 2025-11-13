// app/src/main/java/com/example/mhiker_app/AddHikeActivity.java
package com.example.mhiker_app;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.ImageButton; // THÊM MỚI

import com.google.android.gms.location.FusedLocationProviderClient; // THÊM MỚI
import com.google.android.gms.location.LocationServices; // THÊM MỚI
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddHikeActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private TextInputEditText etName, etLocation, etLength;
    private TextInputEditText etHikerCount, etEquipment, etDate;
    private TextInputEditText etDescription;
    private MaterialButton btnSave;
    private AutoCompleteTextView autoCompleteDifficulty, autoCompleteParking;
    private DatabaseHelper dbHelper;

    // THÊM MỚI: Cho GPS
    private ImageButton btnGetLocation;
    private FusedLocationProviderClient fusedLocationClient;
    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;

    private Hike hikeToEdit = null;
    private static final int SNACKBAR_DURATION = 2500; // 2.5 giây

    // THÊM MỚI: Trình khởi chạy yêu cầu quyền
    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                if (fineLocationGranted != null && fineLocationGranted) {
                    // Quyền chính xác được cấp
                    fetchLastLocation();
                } else if (coarseLocationGranted != null && coarseLocationGranted) {
                    // Quyền tương đối được cấp
                    fetchLastLocation();
                } else {
                    // Không có quyền nào được cấp
                    SnackbarHelper.showCustomSnackbar(
                            btnSave,
                            "Location permission denied.",
                            SnackbarHelper.TYPE_ERROR,
                            SNACKBAR_DURATION
                    );
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_hike);

        dbHelper = new DatabaseHelper(this);
        // THÊM MỚI: Khởi tạo FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Toolbar toolbar = findViewById(R.id.toolbar_add_hike);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        etName = findViewById(R.id.etName);
        etLocation = findViewById(R.id.etLocation);
        etLength = findViewById(R.id.etLength);
        etDate = findViewById(R.id.etDate);
        btnSave = findViewById(R.id.btnSave);
        autoCompleteParking = findViewById(R.id.autoCompleteParking);
        autoCompleteDifficulty = findViewById(R.id.autoCompleteDifficulty);
        etHikerCount = findViewById(R.id.etHikerCount);
        etEquipment = findViewById(R.id.etEquipment);
        etDescription = findViewById(R.id.etDescription);
        // THÊM MỚI: Ánh xạ nút GPS
        btnGetLocation = findViewById(R.id.btnGetLocation);

        setupDifficultyMenu();
        setupParkingMenu();

        if (getIntent().hasExtra("EDIT_HIKE")) {
            hikeToEdit = (Hike) getIntent().getSerializableExtra("EDIT_HIKE");
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Edit Hike");
            }
            btnSave.setText("Update");
            populateDataForEdit();
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Add New Hike");
            }
            btnSave.setText("Save");
        }

        etDate.setOnClickListener(v -> {
            DialogFragment datePicker = new DatePickerFragment();
            datePicker.show(getSupportFragmentManager(), "date picker");
        });

        // THÊM MỚI: Xử lý sự kiện nhấp nút GPS
        btnGetLocation.setOnClickListener(v -> checkLocationPermissionAndFetch());

        btnSave.setOnClickListener(v -> showConfirmationDialog());
    }

    private void setupDifficultyMenu() {
        String[] difficultyLevels = getResources().getStringArray(R.array.difficulty_levels);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, difficultyLevels);
        autoCompleteDifficulty.setAdapter(adapter);
    }

    private void setupParkingMenu() {
        String[] parkingOptions = getResources().getStringArray(R.array.parking_options);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, parkingOptions);
        autoCompleteParking.setAdapter(adapter);
    }

    private void populateDataForEdit() {
        if (hikeToEdit == null) return;

        etName.setText(hikeToEdit.getName());
        etLocation.setText(hikeToEdit.getLocation());
        etDate.setText(hikeToEdit.getDateOfHike());
        etLength.setText(hikeToEdit.getLengthOfHike());
        etHikerCount.setText(hikeToEdit.getHikerCount());
        etEquipment.setText(hikeToEdit.getEquipment());
        etDescription.setText(hikeToEdit.getDescription());
        autoCompleteDifficulty.setText(hikeToEdit.getDifficultyLevel(), false);

        String parkingText = hikeToEdit.isParkingAvailable() ? "Yes" : "No";
        autoCompleteParking.setText(parkingText, false);

        // THÊM MỚI: Tải tọa độ đã lưu
        currentLatitude = hikeToEdit.getLatitude();
        currentLongitude = hikeToEdit.getLongitude();
    }


    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        String currentDateString = DateFormat.getDateInstance(DateFormat.MEDIUM).format(c.getTime());

        etDate.setText(currentDateString);
    }

    // THÊM MỚI: Kiểm tra quyền và lấy vị trí
    private void checkLocationPermissionAndFetch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Quyền đã được cấp, lấy vị trí
            fetchLastLocation();
        } else {
            // Yêu cầu quyền
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    // THÊM MỚI: Lấy vị trí cuối cùng
    @SuppressLint("MissingPermission") // Đã kiểm tra trong checkLocationPermissionAndFetch
    private void fetchLastLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        currentLatitude = location.getLatitude();
                        currentLongitude = location.getLongitude();

                        SnackbarHelper.showCustomSnackbar(
                                btnSave,
                                "Location acquired: " + currentLatitude + ", " + currentLongitude,
                                SnackbarHelper.TYPE_SUCCESS,
                                SNACKBAR_DURATION
                        );
                        // Cố gắng tự động điền tên vị trí
                        geocodeLocation(currentLatitude, currentLongitude);
                    } else {
                        SnackbarHelper.showCustomSnackbar(
                                btnSave,
                                "Unable to fetch location. Try again later.",
                                SnackbarHelper.TYPE_ERROR,
                                SNACKBAR_DURATION
                        );
                    }
                })
                .addOnFailureListener(this, e ->
                        SnackbarHelper.showCustomSnackbar(
                                btnSave,
                                "Error fetching location: " + e.getMessage(),
                                SnackbarHelper.TYPE_ERROR,
                                SNACKBAR_DURATION
                        )
                );
    }

    // THÊM MỚI: (Tùy chọn) Chuyển tọa độ thành tên địa điểm
    private void geocodeLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String locationName = address.getLocality(); // Tên thành phố
                if (locationName == null || locationName.isEmpty()) {
                    locationName = address.getSubAdminArea(); // Tên quận/huyện
                }
                if (locationName != null && !locationName.isEmpty()) {
                    etLocation.setText(locationName);
                }
            }
        } catch (Exception e) {
            // Bỏ qua lỗi geocoding
        }
    }

    private void showConfirmationDialog() {
        String name = etName.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String length = etLength.getText().toString().trim();
        String difficulty = autoCompleteDifficulty.getText().toString().trim();
        String parkingString = autoCompleteParking.getText().toString().trim();
        String hikerCount = etHikerCount.getText().toString().trim();
        String equipment = etEquipment.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (name.isEmpty() || location.isEmpty() || date.isEmpty() || length.isEmpty() || difficulty.isEmpty() || parkingString.isEmpty()) {
            SnackbarHelper.showCustomSnackbar(
                    btnSave,
                    "Please fill all required fields",
                    SnackbarHelper.TYPE_ERROR,
                    SNACKBAR_DURATION
            );
            return;
        }

        boolean parking = parkingString.equals("Yes");

        // CẬP NHẬT: Thêm thông tin GPS vào dialog
        String locationInfo = (currentLatitude != 0.0) ?
                location + " (GPS: " + String.format("%.4f", currentLatitude) + ", " + String.format("%.4f", currentLongitude) + ")" :
                location;

        String confirmationMessage = "Please confirm the details below:\n\n" +
                "Name: " + name + "\n" +
                "Location: " + locationInfo + "\n" + // Cập nhật
                "Date: " + date + "\n" +
                "Parking Available: " + parkingString + "\n" +
                "Length of Hike: " + length + " km\n" +
                "Difficulty: " + difficulty + "\n" +
                "Hiker Count: " + (hikerCount.isEmpty() ? "N/A" : hikerCount) + "\n" +
                "Equipment: " + (equipment.isEmpty() ? "N/A" : equipment) + "\n" +
                "Description: " + (description.isEmpty() ? "N/A" : description);

        String title = (hikeToEdit == null) ? "Confirm Hike Details" : "Confirm Update";
        String positiveButtonText = (hikeToEdit == null) ? "Confirm" : "Update";

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(confirmationMessage)
                .setPositiveButton(positiveButtonText, (dialog, which) -> {
                    saveHikeToDatabase(name, location, date, parking, length, difficulty, hikerCount, equipment, description);
                })
                .setNegativeButton("Edit", (dialog, which) -> dialog.dismiss())
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    private void saveHikeToDatabase(String name, String location, String date, boolean parking, String length, String difficulty, String hikerCount, String equipment, String description) {

        Hike hike = (hikeToEdit == null) ? new Hike() : hikeToEdit;

        hike.setName(name);
        hike.setLocation(location);
        hike.setDateOfHike(date);
        hike.setLengthOfHike(length);
        hike.setDifficultyLevel(difficulty);
        hike.setParkingAvailable(parking);
        hike.setHikerCount(hikerCount);
        hike.setEquipment(equipment);
        hike.setDescription(description);
        // THÊM MỚI: Lưu tọa độ GPS
        hike.setLatitude(currentLatitude);
        hike.setLongitude(currentLongitude);

        if (hikeToEdit == null) {
            long id = dbHelper.addHike(hike);
            if (id != -1) {
                SnackbarHelper.showCustomSnackbar(
                        btnSave,
                        "Hike saved successfully!",
                        SnackbarHelper.TYPE_SUCCESS,
                        SNACKBAR_DURATION
                );
                setResult(Activity.RESULT_OK);
                finish();
            } else {
                SnackbarHelper.showCustomSnackbar(
                        btnSave,
                        "Error saving hike.",
                        SnackbarHelper.TYPE_ERROR,
                        SNACKBAR_DURATION
                );
            }
        } else {
            int rowsAffected = dbHelper.updateHike(hike);
            if (rowsAffected > 0) {
                SnackbarHelper.showCustomSnackbar(
                        btnSave,
                        "Hike updated successfully!",
                        SnackbarHelper.TYPE_SUCCESS,
                        SNACKBAR_DURATION
                );
                setResult(Activity.RESULT_OK);
                finish();
            } else {
                SnackbarHelper.showCustomSnackbar(
                        btnSave,
                        "Error updating hike.",
                        SnackbarHelper.TYPE_ERROR,
                        SNACKBAR_DURATION
                );
            }
        }
    }
}
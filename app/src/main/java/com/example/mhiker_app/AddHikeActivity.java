// app/src/main/java/com/example/mhiker_app/AddHikeActivity.java
package com.example.mhiker_app;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
// XÓA: import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.DateFormat;
import java.util.Calendar;

public class AddHikeActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private TextInputEditText etName, etLocation, etLength;
    private TextInputEditText etHikerCount, etEquipment, etDate;
    private TextInputEditText etDescription;
    private MaterialButton btnSave;
    private AutoCompleteTextView autoCompleteDifficulty, autoCompleteParking;
    private DatabaseHelper dbHelper;

    private Hike hikeToEdit = null;
    private static final int SNACKBAR_DURATION = 2500; // 2.5 giây

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_hike);

        dbHelper = new DatabaseHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbar_add_hike);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Ánh xạ các view - Tên biến này khớp với mã nguồn mới nhất của bạn
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

        // Sử dụng các phương thức getter/setter chính xác từ mã nguồn mới của bạn
        etName.setText(hikeToEdit.getName());
        etLocation.setText(hikeToEdit.getLocation());
        etDate.setText(hikeToEdit.getDateOfHike());
        etLength.setText(hikeToEdit.getLengthOfHike());
        etHikerCount.setText(hikeToEdit.getHikerCount());
        etEquipment.setText(hikeToEdit.getEquipment());
        etDescription.setText(hikeToEdit.getDescription());
        autoCompleteDifficulty.setText(hikeToEdit.getDifficultyLevel(), false);

        // Xử lý boolean cho parking
        String parkingText = hikeToEdit.isParkingAvailable() ? "Yes" : "No";
        autoCompleteParking.setText(parkingText, false);
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
            // THAY THẾ TOAST
            SnackbarHelper.showCustomSnackbar(
                    btnSave, // View neo
                    "Please fill all required fields",
                    SnackbarHelper.TYPE_ERROR,
                    SNACKBAR_DURATION
            );
            return;
        }

        // Chuyển đổi parking String sang boolean
        boolean parking = parkingString.equals("Yes");

        String confirmationMessage = "Please confirm the details below:\n\n" +
                "Name: " + name + "\n" +
                "Location: " + location + "\n" +
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

        // Sử dụng các phương thức setter chính xác
        hike.setName(name);
        hike.setLocation(location);
        hike.setDateOfHike(date);
        hike.setLengthOfHike(length);
        hike.setDifficultyLevel(difficulty);
        hike.setParkingAvailable(parking); // Gửi boolean
        hike.setHikerCount(hikerCount);
        hike.setEquipment(equipment);
        hike.setDescription(description);

        if (hikeToEdit == null) {
            long id = dbHelper.addHike(hike);
            if (id != -1) {
                // THAY THẾ TOAST
                SnackbarHelper.showCustomSnackbar(
                        btnSave,
                        "Hike saved successfully!",
                        SnackbarHelper.TYPE_SUCCESS,
                        SNACKBAR_DURATION
                );
                setResult(Activity.RESULT_OK);
                finish();
            } else {
                // THAY THẾ TOAST
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
                // THAY THẾ TOAST
                SnackbarHelper.showCustomSnackbar(
                        btnSave,
                        "Hike updated successfully!",
                        SnackbarHelper.TYPE_SUCCESS,
                        SNACKBAR_DURATION
                );
                setResult(Activity.RESULT_OK);
                finish();
            } else {
                // THAY THẾ TOAST
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
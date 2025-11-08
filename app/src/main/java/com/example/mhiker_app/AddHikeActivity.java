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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial; // Import này không còn cần thiết
import com.google.android.material.textfield.TextInputEditText;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;

public class AddHikeActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private TextInputEditText etName, etLocation, etLength;
    private TextInputEditText etHikerCount, etEquipment, etDate;
    private MaterialButton btnSave;
    // THAY ĐỔI: Đã thay thế switchParking
    private AutoCompleteTextView autoCompleteDifficulty, autoCompleteParking;
    private DatabaseHelper dbHelper;

    private Hike hikeToEdit = null;

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

        etName = findViewById(R.id.etName);
        etLocation = findViewById(R.id.etLocation);
        etLength = findViewById(R.id.etLength);
        etDate = findViewById(R.id.etDate);
        btnSave = findViewById(R.id.btnSave);
        // THAY ĐỔI: Cập nhật ID
        autoCompleteParking = findViewById(R.id.autoCompleteParking);
        autoCompleteDifficulty = findViewById(R.id.autoCompleteDifficulty);
        etHikerCount = findViewById(R.id.etHikerCount);
        etEquipment = findViewById(R.id.etEquipment);

        setupDifficultyMenu();
        setupParkingMenu(); // THÊM MỚI: Gọi hàm setup cho Parking

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

    // THÊM MỚI: Hàm setup cho menu Parking
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
        autoCompleteDifficulty.setText(hikeToEdit.getDifficultyLevel(), false);

        // THAY ĐỔI: Cập nhật logic để set giá trị Yes/No
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
        // THAY ĐỔI: Lấy dữ liệu từ autoCompleteParking
        String parkingString = autoCompleteParking.getText().toString().trim();
        String hikerCount = etHikerCount.getText().toString().trim();
        String equipment = etEquipment.getText().toString().trim();

        // THAY ĐỔI: Thêm parkingString vào kiểm tra
        if (name.isEmpty() || location.isEmpty() || date.isEmpty() || length.isEmpty() || difficulty.isEmpty() || parkingString.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // THAY ĐỔI: Chuyển đổi parkingString sang boolean
        boolean parking = parkingString.equals("Yes");

        String confirmationMessage = "Please confirm the details below:\n\n" +
                "Name: " + name + "\n" +
                "Location: " + location + "\n" +
                "Date: " + date + "\n" +
                // THAY ĐỔI: Hiển thị giá trị Yes/No
                "Parking Available: " + parkingString + "\n" +
                "Length of Hike: " + length + " km\n" +
                "Difficulty: " + difficulty + "\n" +
                "Hiker Count: " + (hikerCount.isEmpty() ? "N/A" : hikerCount) + "\n" +
                "Equipment: " + (equipment.isEmpty() ? "N/A" : equipment);

        String title = (hikeToEdit == null) ? "Confirm Hike Details" : "Confirm Update";
        String positiveButtonText = (hikeToEdit == null) ? "Confirm" : "Update";

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(confirmationMessage)
                .setPositiveButton(positiveButtonText, (dialog, which) -> {
                    // Truyền giá trị boolean 'parking' đã chuyển đổi
                    saveHikeToDatabase(name, location, date, parking, length, difficulty, hikerCount, equipment);
                })
                .setNegativeButton("Edit", (dialog, which) -> dialog.dismiss())
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    private void saveHikeToDatabase(String name, String location, String date, boolean parking, String length, String difficulty, String hikerCount, String equipment) {

        Hike hike = (hikeToEdit == null) ? new Hike() : hikeToEdit;

        hike.setName(name);
        hike.setLocation(location);
        hike.setDateOfHike(date);
        hike.setLengthOfHike(length);
        hike.setDifficultyLevel(difficulty);
        hike.setParkingAvailable(parking); // Giá trị boolean đã được xử lý
        hike.setHikerCount(hikerCount);
        hike.setEquipment(equipment);

        if (hikeToEdit == null) {
            long id = dbHelper.addHike(hike);
            if (id != -1) {
                Toast.makeText(this, "Hike saved successfully!", Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Error saving hike.", Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = dbHelper.updateHike(hike);
            if (rowsAffected > 0) {
                Toast.makeText(this, "Hike updated successfully!", Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Error updating hike.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
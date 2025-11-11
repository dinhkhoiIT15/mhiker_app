// app/src/main/java/com/example/mhiker_app/AddObservationActivity.java
package com.example.mhiker_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
// XÓA: import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddObservationActivity extends AppCompatActivity {

    // Tên biến này (etObservation, etTimeOfObservation, etAdditionalComments)
    // khớp với tệp layout 'activity_add_observation.xml' mới của bạn
    private EditText etObservation, etTime, etComments;
    private Button btnSaveObservation;
    private DatabaseHelper dbHelper;

    private long hikeId;
    private Observation observationToEdit = null;
    private static final int SNACKBAR_DURATION = 2500; // 2.5 giây

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_observation);

        dbHelper = new DatabaseHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbar_add_observation);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        // SỬA LỖI BIÊN DỊCH: Sử dụng đúng ID từ layout của bạn
        etObservation = findViewById(R.id.etObservation);
        etTime = findViewById(R.id.etTimeOfObservation);
        etComments = findViewById(R.id.etAdditionalComments);
        btnSaveObservation = findViewById(R.id.btnSaveObservation);

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
            // THAY THẾ TOAST:
            // Phải hiển thị snackbar TRƯỚC KHI gọi finish()
            SnackbarHelper.showCustomSnackbar(
                    btnSaveObservation, // View neo
                    "Error: Hike ID is missing.",
                    SnackbarHelper.TYPE_ERROR,
                    SNACKBAR_DURATION
            );
            finish();
            return;
        }

        btnSaveObservation.setOnClickListener(v -> saveObservation());
    }

    private void populateFields() {
        // SỬA LỖI BIÊN DỊCH: Sử dụng đúng tên phương thức từ 'Observation.java'
        etObservation.setText(observationToEdit.getObservationText());
        etTime.setText(observationToEdit.getTimeOfObservation());
        etComments.setText(observationToEdit.getAdditionalComments());
    }

    private void saveObservation() {
        // SỬA LỖI BIÊN DỊCH: Sử dụng đúng tên biến
        String observationText = etObservation.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String comments = etComments.getText().toString().trim();

        if (observationText.isEmpty() || time.isEmpty()) {
            // THAY THẾ TOAST
            SnackbarHelper.showCustomSnackbar(
                    btnSaveObservation,
                    "Observation and Time are required.",
                    SnackbarHelper.TYPE_ERROR,
                    SNACKBAR_DURATION
            );
            return;
        }

        // SỬA LỖI BIÊN DỊCH: Mã của bạn đã đúng (dùng đối tượng Observation)
        // Chúng ta chỉ thay thế Toast
        if (observationToEdit == null) {
            Observation newObservation = new Observation();
            newObservation.setObservationText(observationText);
            newObservation.setTimeOfObservation(time);
            newObservation.setAdditionalComments(comments);
            newObservation.setHikeId(hikeId);
            dbHelper.addObservation(newObservation);
            // THAY THẾ TOAST
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
            dbHelper.updateObservation(observationToEdit);
            // THAY THẾ TOAST
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
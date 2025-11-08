package com.example.mhiker_app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddObservationActivity extends AppCompatActivity {

    private EditText etObservation, etTime, etComments;
    private Button btnSaveObservation;
    private DatabaseHelper dbHelper;

    private long hikeId;
    private Observation observationToEdit = null;

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
            Toast.makeText(this, "Error: Hike ID is missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnSaveObservation.setOnClickListener(v -> saveObservation());
    }

    private void populateFields() {
        etObservation.setText(observationToEdit.getObservationText());
        etTime.setText(observationToEdit.getTimeOfObservation());
        etComments.setText(observationToEdit.getAdditionalComments());
    }

    private void saveObservation() {
        String observationText = etObservation.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String comments = etComments.getText().toString().trim();

        if (observationText.isEmpty() || time.isEmpty()) {
            Toast.makeText(this, "Observation and Time are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (observationToEdit == null) {
            Observation newObservation = new Observation();
            newObservation.setObservationText(observationText);
            newObservation.setTimeOfObservation(time);
            newObservation.setAdditionalComments(comments);
            newObservation.setHikeId(hikeId);
            dbHelper.addObservation(newObservation);
            Toast.makeText(this, "Observation saved.", Toast.LENGTH_SHORT).show();
        } else {
            observationToEdit.setObservationText(observationText);
            observationToEdit.setTimeOfObservation(time);
            observationToEdit.setAdditionalComments(comments);
            dbHelper.updateObservation(observationToEdit);
            Toast.makeText(this, "Observation updated.", Toast.LENGTH_SHORT).show();
        }

        setResult(Activity.RESULT_OK);
        finish();
    }

    private String getCurrentTimestamp() {
        return new SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
    }
}

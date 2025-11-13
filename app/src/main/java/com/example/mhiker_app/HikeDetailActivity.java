// app/src/main/java/com/example/mhiker_app/HikeDetailActivity.java
package com.example.mhiker_app;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri; // THÊM MỚI
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton; // THÊM MỚI
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale; // THÊM MỚI

public class HikeDetailActivity extends AppCompatActivity implements ObservationAdapter.OnObservationListener {

    private TextView tvName, tvLocation, tvDate, tvParking, tvLength, tvDifficulty, tvHikerCount, tvEquipment;
    private TextView tvDetailDescription;
    private Hike hike;
    private DatabaseHelper dbHelper;

    private RecyclerView rvObservations;
    private ObservationAdapter observationAdapter;
    private List<Observation> observationList;
    private MaterialButton btnAddObservation;
    private ImageButton btnShowOnMap; // THÊM MỚI
    private static final int SNACKBAR_DURATION = 2500;

    private final ActivityResultLauncher<Intent> editHikeLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    loadHikeDetails();
                }
            });

    private final ActivityResultLauncher<Intent> observationLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    loadObservations();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hike_detail);

        dbHelper = new DatabaseHelper(this);

        Toolbar toolbar = findViewById(R.id.toolbar_hike_detail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        tvName = findViewById(R.id.tvDetailName);
        tvLocation = findViewById(R.id.tvDetailLocation);
        tvDate = findViewById(R.id.tvDetailDate);
        tvParking = findViewById(R.id.tvDetailParking);
        tvLength = findViewById(R.id.tvDetailLength);
        tvDifficulty = findViewById(R.id.tvDetailDifficulty);
        tvHikerCount = findViewById(R.id.tvDetailHikerCount);
        tvEquipment = findViewById(R.id.tvDetailEquipment);
        tvDetailDescription = findViewById(R.id.tvDetailDescription);
        btnShowOnMap = findViewById(R.id.btnShowOnMap); // THÊM MỚI

        rvObservations = findViewById(R.id.rvObservations);
        btnAddObservation = findViewById(R.id.btnAddObservation);
        setupObservationRecyclerView();

        loadHikeDetails();

        btnAddObservation.setOnClickListener(v -> {
            if (hike != null) {
                Intent intent = new Intent(HikeDetailActivity.this, AddObservationActivity.class);
                intent.putExtra("HIKE_ID", hike.getId());
                observationLauncher.launch(intent);
            } else {
                SnackbarHelper.showCustomSnackbar(
                        btnAddObservation,
                        "Cannot add observation, hike data is missing.",
                        SnackbarHelper.TYPE_ERROR,
                        SNACKBAR_DURATION
                );
            }
        });

        // THÊM MỚI: Xử lý nhấp nút bản đồ
        btnShowOnMap.setOnClickListener(v -> showOnMap());
    }

    // ... (onCreateOptionsMenu và onOptionsItemSelected giữ nguyên) ...
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.hike_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_edit_hike) {
            if (hike != null) {
                Intent intent = new Intent(HikeDetailActivity.this, AddHikeActivity.class);
                intent.putExtra("EDIT_HIKE", hike);
                editHikeLauncher.launch(intent);
            } else {
                SnackbarHelper.showCustomSnackbar(
                        btnAddObservation,
                        "Cannot edit, hike data is missing.",
                        SnackbarHelper.TYPE_ERROR,
                        SNACKBAR_DURATION
                );
            }
            return true;
        } else if (id == R.id.action_delete_hike) {
            if (hike != null) {
                showDeleteConfirmationDialog();
            } else {
                SnackbarHelper.showCustomSnackbar(
                        btnAddObservation,
                        "Cannot delete, hike data is missing.",
                        SnackbarHelper.TYPE_ERROR,
                        SNACKBAR_DURATION
                );
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void setupObservationRecyclerView() {
        observationList = new ArrayList<>();
        rvObservations.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        observationAdapter = new ObservationAdapter(observationList, this);
        rvObservations.setAdapter(observationAdapter);
        rvObservations.setNestedScrollingEnabled(false);
    }


    private void loadHikeDetails() {
        long hikeId;
        if (getIntent().hasExtra("HIKE_ID")) {
            hikeId = getIntent().getLongExtra("HIKE_ID", -1);
            getIntent().removeExtra("HIKE_ID");
        }
        else if (hike != null) {
            hikeId = hike.getId();
        }
        else {
            View rootView = findViewById(android.R.id.content);
            SnackbarHelper.showCustomSnackbar(
                    rootView,
                    "Error: Hike ID not found.",
                    SnackbarHelper.TYPE_ERROR,
                    SNACKBAR_DURATION
            );
            finish();
            return;
        }

        if (hikeId == -1) {
            View rootView = findViewById(android.R.id.content);
            SnackbarHelper.showCustomSnackbar(
                    rootView,
                    "Error: Invalid Hike ID.",
                    SnackbarHelper.TYPE_ERROR,
                    SNACKBAR_DURATION
            );
            finish();
            return;
        }

        hike = dbHelper.getHikeById(hikeId);

        if (hike == null) {
            View rootView = findViewById(android.R.id.content);
            SnackbarHelper.showCustomSnackbar(
                    rootView,
                    "Hike not found. It might have been deleted.",
                    SnackbarHelper.TYPE_INFO,
                    SNACKBAR_DURATION
            );
            finish();
            return;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(hike.getName());
        }
        tvName.setText(hike.getName());
        tvLocation.setText(hike.getLocation());
        tvDate.setText(hike.getDateOfHike());
        tvParking.setText(hike.isParkingAvailable() ? "Yes" : "No");
        tvLength.setText(hike.getLengthOfHike() + " km");
        tvDifficulty.setText(hike.getDifficultyLevel());
        tvHikerCount.setText(hike.getHikerCount().isEmpty() ? "N/A" : hike.getHikerCount());
        tvEquipment.setText(hike.getEquipment().isEmpty() ? "N/A" : hike.getEquipment());

        String description = hike.getDescription();
        tvDetailDescription.setText(description == null || description.isEmpty() ? "N/A" : description);

        // THÊM MỚI: Hiển thị nút bản đồ nếu có tọa độ
        if (hike.getLatitude() != 0.0 || hike.getLongitude() != 0.0) {
            btnShowOnMap.setVisibility(View.VISIBLE);
        } else {
            btnShowOnMap.setVisibility(View.GONE);
        }

        loadObservations();
    }

    private void loadObservations() {
        if (hike != null) {
            observationList = dbHelper.getAllObservationsForHike(hike.getId());
            observationAdapter.setData(observationList);
        }
    }

    // THÊM MỚI: Hàm mở Intent bản đồ
    private void showOnMap() {
        if (hike == null || (hike.getLatitude() == 0.0 && hike.getLongitude() == 0.0)) {
            SnackbarHelper.showCustomSnackbar(
                    btnAddObservation,
                    "No GPS location saved for this hike.",
                    SnackbarHelper.TYPE_INFO,
                    SNACKBAR_DURATION
            );
            return;
        }

        // Tạo URI geo
        String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f(%s)",
                hike.getLatitude(),
                hike.getLongitude(),
                hike.getLatitude(),
                hike.getLongitude(),
                Uri.encode(hike.getName()) // Mã hóa tên cho URI
        );

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        mapIntent.setPackage("com.google.android.apps.maps"); // Cố gắng mở Google Maps

        // Kiểm tra xem Google Maps có được cài đặt không
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            // Nếu không, hãy để hệ thống chọn bất kỳ ứng dụng bản đồ nào
            Intent genericMapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            if (genericMapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(genericMapIntent);
            } else {
                SnackbarHelper.showCustomSnackbar(
                        btnAddObservation,
                        "No map application found.",
                        SnackbarHelper.TYPE_ERROR,
                        SNACKBAR_DURATION
                );
            }
        }
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Hike")
                .setMessage("Are you sure you want to delete '" + hike.getName() + "'? All its observations will also be deleted. This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteHike(hike.getId());
                    Toast.makeText(HikeDetailActivity.this, "'" + hike.getName() + "' deleted.", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public void onEditClick(Observation observation) {
        Intent intent = new Intent(this, AddObservationActivity.class);
        intent.putExtra("EDIT_OBSERVATION", observation);
        observationLauncher.launch(intent);
    }

    @Override
    public void onDeleteClick(Observation observation) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Observation")
                .setMessage("Are you sure you want to delete this observation?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteObservation(observation.getId());
                    SnackbarHelper.showCustomSnackbar(
                            btnAddObservation,
                            "Observation deleted.",
                            SnackbarHelper.TYPE_INFO,
                            SNACKBAR_DURATION
                    );
                    loadObservations();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
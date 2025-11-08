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
import android.os.Bundle;
// THAY ĐỔI: Import Menu
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button; // Giữ lại cho nút Add Observation mới
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton; // THAY ĐỔI: Import mới
import com.google.android.material.floatingactionbutton.FloatingActionButton; // Import này không còn cần thiết

import java.util.ArrayList;
import java.util.List;

public class HikeDetailActivity extends AppCompatActivity implements ObservationAdapter.OnObservationListener {

    private TextView tvName, tvLocation, tvDate, tvParking, tvLength, tvDifficulty, tvHikerCount, tvEquipment;
    // ĐÃ XÓA: private Button btnEdit, btnDelete;
    private Hike hike;
    private DatabaseHelper dbHelper;

    private RecyclerView rvObservations;
    private ObservationAdapter observationAdapter;
    private List<Observation> observationList;
    // ĐÃ XÓA: private FloatingActionButton fabAddObservation;
    private MaterialButton btnAddObservation; // THÊM MỚI: Button Add Observation

    private final ActivityResultLauncher<Intent> editHikeLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    loadHikeDetails(); // Tải lại chi tiết sau khi chỉnh sửa
                }
            });

    private final ActivityResultLauncher<Intent> observationLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    loadObservations(); // Chỉ cần tải lại danh sách observation
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
            // Tiêu đề sẽ được đặt trong loadHikeDetails()
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
        // ĐÃ XÓA: findViewById cho btnEdit, btnDelete

        rvObservations = findViewById(R.id.rvObservations);
        btnAddObservation = findViewById(R.id.btnAddObservation); // THÊM MỚI: findViewById cho nút mới
        setupObservationRecyclerView();

        loadHikeDetails(); // Tải dữ liệu hike lần đầu

        // ĐÃ XÓA: setOnClickListener cho btnEdit, btnDelete

        // THÊM MỚI: setOnClickListener cho nút Add Observation mới
        btnAddObservation.setOnClickListener(v -> {
            if (hike != null) {
                Intent intent = new Intent(HikeDetailActivity.this, AddObservationActivity.class);
                intent.putExtra("HIKE_ID", hike.getId());
                observationLauncher.launch(intent);
            } else {
                Toast.makeText(this, "Cannot add observation, hike data is missing.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // THÊM MỚI: Inflate menu trên Toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.hike_detail_menu, menu);
        return true;
    }

    // THÊM MỚI: Xử lý sự kiện click item trên menu Toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_edit_hike) {
            if (hike != null) {
                Intent intent = new Intent(HikeDetailActivity.this, AddHikeActivity.class);
                intent.putExtra("EDIT_HIKE", hike);
                editHikeLauncher.launch(intent);
            } else {
                Toast.makeText(this, "Cannot edit, hike data is missing.", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (id == R.id.action_delete_hike) {
            if (hike != null) {
                showDeleteConfirmationDialog();
            } else {
                Toast.makeText(this, "Cannot delete, hike data is missing.", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void setupObservationRecyclerView() {
        observationList = new ArrayList<>();
        // Đặt nestedScrollingEnabled=false để tránh xung đột cuộn
        rvObservations.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false; // Ngăn RecyclerView cuộn độc lập
            }
        });
        observationAdapter = new ObservationAdapter(observationList, this);
        rvObservations.setAdapter(observationAdapter);
        rvObservations.setNestedScrollingEnabled(false);
    }


    private void loadHikeDetails() {
        long hikeId;
        // Ưu tiên ID từ Intent nếu có (khi mở lần đầu)
        if (getIntent().hasExtra("HIKE_ID")) {
            hikeId = getIntent().getLongExtra("HIKE_ID", -1);
            getIntent().removeExtra("HIKE_ID"); // Xóa ID khỏi intent để lần sau không dùng lại nhầm
        }
        // Nếu không có từ Intent (ví dụ sau khi edit quay lại), dùng ID từ object 'hike' hiện tại
        else if (hike != null) {
            hikeId = hike.getId();
        }
        // Trường hợp không có ID nào
        else {
            Toast.makeText(this, "Error: Hike ID not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (hikeId == -1) {
            Toast.makeText(this, "Error: Invalid Hike ID.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Tải lại hike từ CSDL bằng ID mới nhất
        hike = dbHelper.getHikeById(hikeId);

        if (hike == null) {
            Toast.makeText(this, "Hike not found. It might have been deleted.", Toast.LENGTH_SHORT).show();
            finish(); // Đóng activity nếu hike không tồn tại
            return;
        }

        // Cập nhật giao diện với dữ liệu mới
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(hike.getName()); // Đặt tiêu đề Toolbar
        }
        tvName.setText(hike.getName());
        tvLocation.setText(hike.getLocation()); // Bỏ chữ "Location: " để dùng icon
        tvDate.setText(hike.getDateOfHike()); // Bỏ chữ "Date: "
        tvParking.setText(hike.isParkingAvailable() ? "Yes" : "No"); // Bỏ chữ "Parking: "
        tvLength.setText(hike.getLengthOfHike() + " km"); // Bỏ chữ "Length: "
        tvDifficulty.setText(hike.getDifficultyLevel()); // Bỏ chữ "Difficulty: "
        tvHikerCount.setText(hike.getHikerCount().isEmpty() ? "N/A" : hike.getHikerCount()); // Bỏ chữ "Hiker Count: "
        tvEquipment.setText(hike.getEquipment().isEmpty() ? "N/A" : hike.getEquipment()); // Bỏ chữ "Equipment: "

        loadObservations(); // Tải danh sách observations cho hike này
    }

    private void loadObservations() {
        if (hike != null) {
            observationList = dbHelper.getAllObservationsForHike(hike.getId());
            observationAdapter.setData(observationList); // Cập nhật dữ liệu cho Adapter
        }
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Hike")
                .setMessage("Are you sure you want to delete '" + hike.getName() + "'? All its observations will also be deleted. This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteHike(hike.getId());
                    Toast.makeText(HikeDetailActivity.this, "'" + hike.getName() + "' deleted.", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK); // Đặt kết quả để MainActivity có thể cập nhật list
                    finish(); // Đóng màn hình chi tiết sau khi xóa
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // Các phương thức xử lý click trên item Observation (giữ nguyên)
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
                    Toast.makeText(this, "Observation deleted.", Toast.LENGTH_SHORT).show();
                    loadObservations(); // Tải lại danh sách sau khi xóa
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
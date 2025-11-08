package com.example.mhiker_app;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
// Đã xóa import: androidx.appcompat.widget.SearchView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

// THAY ĐỔI: Implement interface mới của DialogFragment
public class MainActivity extends AppCompatActivity implements HikeAdapter.OnItemClickListener, AdvancedSearchFragment.AdvancedSearchListener {

    private RecyclerView recyclerView;
    private HikeAdapter hikeAdapter;
    private List<Hike> hikeList;
    private DatabaseHelper dbHelper;
    private FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("M-Hiker");

        dbHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerViewHikes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        hikeList = new ArrayList<>();
        hikeAdapter = new HikeAdapter(hikeList, this);
        recyclerView.setAdapter(hikeAdapter);

        fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddHikeActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHikesFromDb();
    }

    private void loadHikesFromDb() {
        hikeList = dbHelper.getAllHikes();
        hikeAdapter.setData(hikeList);
    }

    @Override
    public void onItemClick(Hike hike) {
        Intent intent = new Intent(MainActivity.this, HikeDetailActivity.class);
        intent.putExtra("HIKE_ID", hike.getId());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        // ĐÃ XÓA: Toàn bộ logic của SearchView đã được loại bỏ
        return true;
    }

    // THAY ĐỔI: Tên hàm và tham số
    private void performAdvancedSearch(String name, String location, String length, String date) {
        List<Hike> searchResults = dbHelper.searchHikesAdvanced(name, location, length, date);
        hikeAdapter.setData(searchResults);
        if (searchResults.isEmpty()) {
            Toast.makeText(this, "No hikes found matching criteria.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_delete_all) {
            showDeleteAllConfirmationDialog();
            return true;
        } else if (id == R.id.action_search) {
            // THAY ĐỔI: Mở DialogFragment thay vì SearchView
            AdvancedSearchFragment dialog = new AdvancedSearchFragment();
            dialog.show(getSupportFragmentManager(), "AdvancedSearchFragment");
            return true;
        } else if (id == R.id.action_show_all) {
            // THÊM MỚI: Tải lại tất cả các chuyến đi
            loadHikesFromDb();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDeleteAllConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete All Hikes")
                .setMessage("Are you sure you want to delete ALL hikes? This action cannot be undone.")
                .setPositiveButton("Delete All", (dialog, which) -> {
                    dbHelper.deleteAllHikes();
                    loadHikesFromDb();
                    Toast.makeText(MainActivity.this, "All hikes deleted.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // THÊM MỚI: Phương thức callback từ AdvancedSearchFragment
    @Override
    public void onSearchClicked(String name, String location, String length, String date) {
        // Khi người dùng nhấn "Search" trong dialog
        performAdvancedSearch(name, location, length, date);
    }

    // THÊM MỚI: Phương thức callback từ AdvancedSearchFragment
    @Override
    public void onResetSearchClicked() {
        // Khi người dùng nhấn "Reset" trong dialog
        loadHikesFromDb();
    }
}
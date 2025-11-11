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
// Đảm bảo import này tồn tại
import androidx.appcompat.widget.SearchView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

// Implement interface mới của DialogFragment
public class MainActivity extends AppCompatActivity implements HikeAdapter.OnItemClickListener, AdvancedSearchFragment.AdvancedSearchListener {

    private RecyclerView recyclerView;
    private HikeAdapter hikeAdapter;
    private List<Hike> hikeList;
    private DatabaseHelper dbHelper;
    private FloatingActionButton fabAdd;
    private SearchView searchView;

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
        // Thu gọn search view nếu nó đang mở khi quay lại
        if (searchView != null && !searchView.isIconified()) {
            searchView.setIconified(true);
        }
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

        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();

        searchView.setQueryHint(getString(R.string.search_hint)); //

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Không cần làm gì ở đây vì đã xử lý real-time
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // THAY ĐỔI: Chuyển logic tìm kiếm vào đây
                // Khi người dùng gõ, thực hiện tìm kiếm ngay lập tức
                performFastSearch(newText);
                return true;
            }
        });

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                // Khi đóng search view, tải lại danh sách đầy đủ
                loadHikesFromDb();
                return true;
            }
        });

        return true;
    }

    // Hàm này (tìm kiếm nhanh) giờ sẽ được gọi mỗi khi gõ phím
    private void performFastSearch(String nameQuery) {
        // THAY ĐỔI: Nếu query rỗng, tải lại tất cả. Nếu không, thực hiện tìm kiếm.
        if (nameQuery == null || nameQuery.trim().isEmpty()) {
            loadHikesFromDb();
        } else {
            // Hàm này gọi searchHikesAdvanced chỉ với tên
            List<Hike> searchResults = dbHelper.searchHikesByName(nameQuery);
            hikeAdapter.setData(searchResults);
            // Không cần hiển thị Toast nữa để tránh làm phiền khi gõ
        }
    }

    // Hàm này (tìm kiếm nâng cao) được giữ nguyên, gọi từ dialog
    private void performAdvancedSearch(String name, String location, String length, String date) {
        List<Hike> searchResults = dbHelper.searchHikesAdvanced(name, location, length, date); //
        hikeAdapter.setData(searchResults);
        if (searchResults.isEmpty()) {
            Toast.makeText(this, "No hikes found matching criteria.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_delete_all) { //
            showDeleteAllConfirmationDialog();
            return true;
        }
        else if (id == R.id.action_advanced_search) { //
            AdvancedSearchFragment dialog = new AdvancedSearchFragment();
            dialog.show(getSupportFragmentManager(), "AdvancedSearchFragment");
            return true;
        } else if (id == R.id.action_show_all) { //
            loadHikesFromDb();
            // Đóng/reset search view nếu đang mở
            if (searchView != null && !searchView.isIconified()) {
                searchView.setIconified(true);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDeleteAllConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete All Hikes")
                .setMessage("Are you sure you want to delete ALL hikes? This action cannot be undone.")
                .setPositiveButton("Delete All", (dialog, which) -> {
                    dbHelper.deleteAllHikes(); //
                    loadHikesFromDb();
                    Toast.makeText(MainActivity.this, "All hikes deleted.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // Callback từ AdvancedSearchFragment
    @Override
    public void onSearchClicked(String name, String location, String length, String date) {
        performAdvancedSearch(name, location, length, date);
    }

    // Callback từ AdvancedSearchFragment
    @Override
    public void onResetSearchClicked() {
        loadHikesFromDb();
    }
}
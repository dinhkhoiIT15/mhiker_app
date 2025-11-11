// app/src/main/java/com/example/mhiker_app/MainActivity.java
package com.example.mhiker_app;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.widget.SearchView;
import android.widget.Toast; // Giữ lại cho "Press back again to exit"

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements HikeAdapter.OnItemClickListener, AdvancedSearchFragment.AdvancedSearchListener {

    private RecyclerView recyclerView;
    private HikeAdapter hikeAdapter;
    private List<Hike> hikeList;
    private DatabaseHelper dbHelper;
    private FloatingActionButton fabAdd;
    private SearchView searchView;
    private static final int SNACKBAR_DURATION = 2500; // 2.5 giây

    // Biến cho "Back to Exit"
    private long backPressedTime;
    private Toast backToast;

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

        searchView.setQueryHint(getString(R.string.search_hint));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
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
                loadHikesFromDb();
                return true;
            }
        });

        return true;
    }

    private void performFastSearch(String nameQuery) {
        if (nameQuery == null || nameQuery.trim().isEmpty()) {
            loadHikesFromDb();
        } else {
            List<Hike> searchResults = dbHelper.searchHikesByName(nameQuery);
            hikeAdapter.setData(searchResults);
        }
    }

    private void performAdvancedSearch(String name, String location, String length, String date) {
        List<Hike> searchResults = dbHelper.searchHikesAdvanced(name, location, length, date);
        hikeAdapter.setData(searchResults);
        if (searchResults.isEmpty()) {
            // THAY THẾ TOAST
            SnackbarHelper.showCustomSnackbar(
                    fabAdd, // View neo
                    "No hikes found matching criteria.",
                    SnackbarHelper.TYPE_INFO,
                    SNACKBAR_DURATION
            );
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_delete_all) {
            showDeleteAllConfirmationDialog();
            return true;
        }
        else if (id == R.id.action_advanced_search) {
            AdvancedSearchFragment dialog = new AdvancedSearchFragment();
            dialog.show(getSupportFragmentManager(), "AdvancedSearchFragment");
            return true;
        } else if (id == R.id.action_show_all) {
            loadHikesFromDb();
            if (searchView != null && !searchView.isIconified()) {
                searchView.setIconified(true);
            }
            return true;
        }
        else if (id == R.id.action_logout) {
            showLogoutConfirmationDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDeleteAllConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete All Hikes")
                .setMessage("Are you sure you want to delete ALL hikes? This action cannot be undone.")
                .setPositiveButton("Delete All", (dialog, which) -> {
                    dbHelper.deleteAllHikes(); // Giả sử đây là lỗi gõ phím và
                    // phải là deleteAllHikes()
                    // dbHelper.deleteAllHikes(); // Sửa lại
                    loadHikesFromDb();
                    // THAY THẾ TOAST
                    SnackbarHelper.showCustomSnackbar(
                            fabAdd,
                            "All hikes deleted.",
                            SnackbarHelper.TYPE_INFO,
                            SNACKBAR_DURATION
                    );
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // Sửa lỗi (nếu có) trong tên phương thức
    // private void showDeleteAllConfirmationDialog() {
    //     new AlertDialog.Builder(this)
    //             .setTitle("Delete All Hikes")
    //             ...
    //             .setPositiveButton("Delete All", (dialog, which) -> {
    //                 dbHelper.deleteAllHikes(); // Đảm bảo tên phương thức này đúng
    //                 loadHikesFromDb();
    //                 SnackbarHelper.showCustomSnackbar(
    //                         fabAdd,
    //                         "All hikes deleted.",
    //                         SnackbarHelper.TYPE_INFO,
    //                         SNACKBAR_DURATION
    //                 );
    //             })
    //             ...
    //             .show();
    // }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    SharedPreferences prefs = getSharedPreferences(SplashActivity.PREFS_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(SplashActivity.KEY_IS_LOGGED_IN, false);
                    editor.remove("LOGGED_IN_USERNAME");
                    editor.apply();

                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    @Override
    public void onSearchClicked(String name, String location, String length, String date) {
        performAdvancedSearch(name, location, length, date);
    }

    @Override
    public void onResetSearchClicked() {
        loadHikesFromDb();
    }

    // GIỮ LẠI TOAST cho chức năng "Press back again to exit"
    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            if (backToast != null) {
                backToast.cancel();
            }
            super.onBackPressed();
            return;
        } else {
            backToast = Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT);
            backToast.show();
        }
        backPressedTime = System.currentTimeMillis();
    }
}
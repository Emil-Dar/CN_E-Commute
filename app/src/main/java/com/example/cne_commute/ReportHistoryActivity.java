package com.example.cne_commute;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.io.IOException;

public class ReportHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ReportHistoryAdapter adapter;
    private ProgressBar progressBar;
    private TextView noReportsText;
    private Spinner sortSpinner;

    private FirebaseAuth mAuth;
    private List<ReportData> originalList = new ArrayList<>();

    private SharedPreferences scanPrefs;
    private static final String SCAN_PREFS = "scan_tracker";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_history);

        mAuth = FirebaseAuth.getInstance();
        scanPrefs = getSharedPreferences(SCAN_PREFS, MODE_PRIVATE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Report History");
        }

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReportHistoryAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        progressBar = findViewById(R.id.progress_bar);
        noReportsText = findViewById(R.id.no_reports_text);
        sortSpinner = findViewById(R.id.sort_spinner);

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                this, R.array.sort_options, R.layout.spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(spinnerAdapter);

        setupSortSpinner();
        fetchReports();
    }

    private void setupSortSpinner() {
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                List<ReportData> sorted = new ArrayList<>(originalList);
                if (position == 0) {
                    Collections.sort(sorted, (r1, r2) -> safe(r2.getTimestamp()).compareTo(safe(r1.getTimestamp())));
                } else {
                    Collections.sort(sorted, Comparator.comparing(r -> safe(r.getDriverName()), String.CASE_INSENSITIVE_ORDER));
                }
                adapter.updateList(sorted);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void fetchReports() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        noReportsText.setVisibility(View.GONE);

        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        if (userId == null) {
            Log.w("ReportHistoryActivity", "User not logged in. Loading local history.");
            Toast.makeText(this, "User not logged in. Showing local history.", Toast.LENGTH_SHORT).show();
            loadLocalReportHistory();
            return;
        }

        ReportApiService apiService = ApiClient.getClient().create(ReportApiService.class);

        Call<List<ReportData>> call = apiService.getReports("eq." + userId);

        call.enqueue(new Callback<List<ReportData>>() {
            @Override
            public void onResponse(@NonNull Call<List<ReportData>> call, @NonNull Response<List<ReportData>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<ReportData> reports = response.body();
                    if (reports.isEmpty()) {
                        noReportsText.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        noReportsText.setText("No reports found.");
                    } else {
                        originalList = reports;
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter.updateList(new ArrayList<>(originalList));
                    }
                } else {
                    handleError(response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ReportData>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                noReportsText.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                Toast.makeText(ReportHistoryActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleError(Response<?> response) {
        String errorBody = "Unknown error";
        try {
            if (response.errorBody() != null) {
                errorBody = response.errorBody().string();
            }
        } catch (IOException e) {
            Log.e("ReportHistoryActivity", "Error parsing error body: " + e.getMessage());
        }

        noReportsText.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        Toast.makeText(this, "Failed to load reports.", Toast.LENGTH_SHORT).show();
    }

    private void loadLocalReportHistory() {
        SharedPreferences prefs = getSharedPreferences("report_history", MODE_PRIVATE);
        String json = prefs.getString("reports", "[]");

        Gson gson = new Gson();
        Type type = new TypeToken<List<Report>>() {}.getType();
        List<Report> reportList = gson.fromJson(json, type);

        progressBar.setVisibility(View.GONE);

        if (reportList.isEmpty()) {
            noReportsText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            noReportsText.setText("No local reports found.");
        } else {
            originalList = ReportMapper.toReportDataList(reportList);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.updateList(new ArrayList<>(originalList));
        }
    }

    // 🧠 Scan tracking logic
    public boolean hasScannedToday(String driverId) {
        String todayKey = driverId + "_" + getTodayDate();
        return scanPrefs.getBoolean(todayKey, false);
    }

    public void markScannedToday(String driverId) {
        String todayKey = driverId + "_" + getTodayDate();
        scanPrefs.edit().putBoolean(todayKey, true).apply();
    }

    public void promptRepeatScan(String driverId, Runnable onConfirm) {
        if (hasScannedToday(driverId)) {
            new AlertDialog.Builder(this)
                    .setTitle("Already Scanned")
                    .setMessage("You've already scanned this driver today. Log another ride?")
                    .setPositiveButton("Yes", (dialog, which) -> onConfirm.run())
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            onConfirm.run();
        }
    }

    private String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private String safe(String value) {
        return value != null ? value : "";
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.fade_in, 0);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, 0);
    }
}

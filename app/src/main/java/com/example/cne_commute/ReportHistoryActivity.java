package com.example.cne_commute;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ReportHistoryAdapter adapter; // Ensure this is ReportHistoryAdapter
    private ProgressBar progressBar;
    private TextView noReportsText; // To show when no reports are found

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_history); // Using your existing layout

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);  // Show back arrow
            actionBar.setTitle("Report History");
        }

        // Initialize RecyclerView and its components
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReportHistoryAdapter(Collections.emptyList()); // Initialize with empty list
        recyclerView.setAdapter(adapter);

        // Initialize ProgressBar and TextView for no reports
        // Assuming these IDs exist in activity_report_history.xml or will be added.
        // If they don't exist, you'll need to add them to your activity_report_history.xml
        // For now, I'll assume they are there or will be added.
        progressBar = findViewById(R.id.progress_bar); // Add this ID to your XML
        noReportsText = findViewById(R.id.no_reports_text); // Add this ID to your XML

        // Fetch reports from the backend
        fetchReports();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.fade_in, 0);  // Optional animation
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, 0);
    }

    /**
     * Fetches reports from the backend API for the current user.
     */
    private void fetchReports() {
        // Show loading indicator
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        noReportsText.setVisibility(View.GONE);

        String userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        if (userId == null) {
            // If user is not logged in, hide progress bar and show message
            progressBar.setVisibility(View.GONE);
            noReportsText.setVisibility(View.VISIBLE);
            noReportsText.setText("Please log in to view your reports.");
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the API service instance
        ReportApiService apiService = ApiClient.getRetrofitInstance().create(ReportApiService.class);
        Call<List<ReportData>> call = apiService.getReports(userId);

        call.enqueue(new Callback<List<ReportData>>() {
            @Override
            public void onResponse(@NonNull Call<List<ReportData>> call, @NonNull Response<List<ReportData>> response) {
                // Hide loading indicator
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<ReportData> reports = response.body();
                    if (reports.isEmpty()) {
                        // Show "no reports" message if the list is empty
                        noReportsText.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        // Update RecyclerView with fetched reports
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter = new ReportHistoryAdapter(reports);
                        recyclerView.setAdapter(adapter);
                    }
                } else {
                    // Handle API error response (e.g., 404, 500)
                    noReportsText.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    Log.e("ReportHistoryActivity", "Failed to fetch reports. Code: " + response.code());
                    Toast.makeText(ReportHistoryActivity.this, "Failed to load reports.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ReportData>> call, @NonNull Throwable t) {
                // Handle network errors (e.g., no internet, timeout)
                progressBar.setVisibility(View.GONE);
                noReportsText.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                Log.e("ReportHistoryActivity", "API call failed: " + t.getMessage(), t);
                Toast.makeText(ReportHistoryActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

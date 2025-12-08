package com.example.cne_commute;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OperatorHomeActivity extends AppCompatActivity {

    private String operatorId;
    private SupabaseService apiService;

    private RecyclerView recyclerDrivers;
    private OperatorHomeDriverAdapter adapter;

    private final List<OperatorDriverSummary> driverSummaryList = new ArrayList<>();

    private final String API_KEY = BuildConfig.SUPABASE_API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_home);

        operatorId = getIntent().getStringExtra("operatorId");

        apiService = SupabaseApiClient.getRetrofitInstance().create(SupabaseService.class);

        recyclerDrivers = findViewById(R.id.recycler_drivers);
        recyclerDrivers.setLayoutManager(new LinearLayoutManager(this));

        adapter = new OperatorHomeDriverAdapter(driverSummaryList);
        recyclerDrivers.setAdapter(adapter);

        setupBottomNavigation();
        fetchOperatorDrivers();
    }

    // ---------------------------------------------------------
    // FETCH ALL DRIVERS REQUESTED BY THIS OPERATOR
    // ---------------------------------------------------------
    private void fetchOperatorDrivers() {

        apiService.getDriversByOperator(
                API_KEY,
                "Bearer " + API_KEY,
                "eq." + operatorId
        ).enqueue(new Callback<List<Driver>>() {
            @Override
            public void onResponse(Call<List<Driver>> call, Response<List<Driver>> response) {

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(OperatorHomeActivity.this, "Unable to load drivers", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (Driver d : response.body()) {
                    loadFranchiseAndAssignment(d);
                }
            }

            @Override
            public void onFailure(Call<List<Driver>> call, Throwable t) {
                Toast.makeText(OperatorHomeActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ---------------------------------------------------------
    // GET OPERATOR’S FRANCHISE, THEN ASSIGNMENT
    // ---------------------------------------------------------
    private void loadFranchiseAndAssignment(Driver driver) {

        apiService.getFranchisesByOperatorId(
                API_KEY,
                "Bearer " + API_KEY,
                "eq." + operatorId
        ).enqueue(new Callback<List<Franchise>>() {
            @Override
            public void onResponse(Call<List<Franchise>> call, Response<List<Franchise>> response) {

                if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                    return;
                }

                Franchise franchise = response.body().get(0);

                getLatestAssignment(driver, franchise);
            }

            @Override
            public void onFailure(Call<List<Franchise>> call, Throwable t) {}
        });
    }

    // ---------------------------------------------------------
    // GET LATEST ASSIGNMENT DATE
    // ---------------------------------------------------------
    private void getLatestAssignment(Driver driver, Franchise franchise) {

        apiService.getLatestAssignmentForFranchise(
                API_KEY,
                "Bearer " + API_KEY,
                "eq." + franchise.getFranchiseId(),
                "assigned_at.desc",
                1
        ).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {

                String assignedDate = "N/A";

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Object dt = response.body().get(0).get("assigned_at");
                    assignedDate = dt != null ? formatDate(dt.toString()) : "N/A";
                }

                fetchReports(driver, franchise, assignedDate);
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {}
        });
    }

    // ---------------------------------------------------------
    // GET REPORT COUNTS FOR DRIVER
    // ---------------------------------------------------------
    private void fetchReports(Driver driver, Franchise franchise, String assignedDate) {

        apiService.getReportsByDriverId(
                API_KEY,
                "Bearer " + API_KEY,
                "eq." + driver.getDriverId(),
                "report_id.desc",
                "*"
        ).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {

                int pending = 0, scheduled = 0, resolved = 0, unresolved = 0;

                if (response.isSuccessful() && response.body() != null) {
                    for (Map<String, Object> r : response.body()) {

                        String status = String.valueOf(r.get("status")).toLowerCase();

                        switch (status) {
                            case "pending":    pending++; break;
                            case "scheduled":  scheduled++; break;
                            case "resolved":   resolved++; break;
                            case "unresolved": unresolved++; break;
                        }
                    }
                }

                driverSummaryList.add(new OperatorDriverSummary(
                        driver.getDriverId(),
                        driver.getFirstName() + " " + driver.getLastName(),
                        franchise.getFranchiseId(),
                        assignedDate,
                        pending, scheduled, resolved, unresolved
                ));

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {}
        });
    }

    // ---------------------------------------------------------
    // UTILS
    // ---------------------------------------------------------
    private String formatDate(String raw) {
        try { return raw.substring(0, 10); }
        catch (Exception e) { return "N/A"; }
    }

    // ---------------------------------------------------------
    // RESTORED FULL BOTTOM NAVIGATION
    // ---------------------------------------------------------
    private void setupBottomNavigation() {

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {

            Intent intent = null;

            switch (item.getItemId()) {

                case R.id.nav_home:
                    return true;

                case R.id.nav_franchise:
                    intent = new Intent(this, OperatorFranchiseActivity.class);
                    break;

                case R.id.nav_notification:
                    intent = new Intent(this, OperatorNotificationActivity.class);
                    break;

                case R.id.nav_account:
                    intent = new Intent(this, OperatorAccountActivity.class);
                    break;
            }

            if (intent != null) {
                intent.putExtra("operatorId", operatorId);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });
    }
}

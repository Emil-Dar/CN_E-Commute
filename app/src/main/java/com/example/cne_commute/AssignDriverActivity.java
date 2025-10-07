package com.example.cne_commute;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AssignDriverActivity extends AppCompatActivity {

    private TextView assignDriverText;
    private AutoCompleteTextView driverNameInput;
    private Button cancelBtn, saveBtn;

    private Map<String, String> driverMap = new HashMap<>();
    private String franchiseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_driver);

        assignDriverText = findViewById(R.id.headerText);
        driverNameInput = findViewById(R.id.driverNameInput);
        cancelBtn = findViewById(R.id.cancelBtn);
        saveBtn = findViewById(R.id.saveBtn);

        franchiseId = getIntent().getStringExtra("franchiseId");
        if (franchiseId == null || franchiseId.isEmpty()) {
            Toast.makeText(this, "Franchise ID is missing", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        assignDriverText.setText("Assign a driver to Franchise ID: " + franchiseId);

        fetchDrivers();

        cancelBtn.setOnClickListener(v -> finish());
        saveBtn.setOnClickListener(v -> generateAssignmentIdAndAssign());
    }

    private void fetchDrivers() {
        SupabaseService service = SupabaseApiClient.getRetrofitInstance().create(SupabaseService.class);
        service.getDrivers().enqueue(new Callback<List<Driver>>() {
            @Override
            public void onResponse(Call<List<Driver>> call, Response<List<Driver>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    driverMap.clear();
                    for (Driver driver : response.body()) {
                        if (driver.getFullName() != null && driver.getDriverId() != null) {
                            driverMap.put(driver.getFullName(), driver.getDriverId());
                        }
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(AssignDriverActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            driverMap.keySet().toArray(new String[0]));
                    driverNameInput.setAdapter(adapter);
                } else {
                    Toast.makeText(AssignDriverActivity.this, "Failed to load drivers", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Driver>> call, Throwable t) {
                Toast.makeText(AssignDriverActivity.this, "Error fetching drivers: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generateAssignmentIdAndAssign() {
        String driverName = driverNameInput.getText().toString().trim();
        if (driverName.isEmpty() || !driverMap.containsKey(driverName)) {
            Toast.makeText(this, "Please select a valid driver", Toast.LENGTH_SHORT).show();
            return;
        }

        String driverId = driverMap.get(driverName);
        SupabaseService service = SupabaseApiClient.getRetrofitInstance().create(SupabaseService.class);

        // 1️⃣ fetch latest assignment for this franchise with proper filter
        service.getLatestAssignmentForFranchise(
                BuildConfig.SUPABASE_API_KEY,
                "Bearer " + BuildConfig.SUPABASE_API_KEY,
                "eq." + franchiseId,       // ⚠ must use "eq.<franchiseId>"
                "assigned_at.desc",
                1
        ).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    String latestDriverId = String.valueOf(response.body().get(0).get("driver_id")); // safe comparison
                    if (driverId.equals(latestDriverId)) {
                        Toast.makeText(AssignDriverActivity.this,
                                "This driver is already assigned to this franchise",
                                Toast.LENGTH_SHORT).show();
                        return; // stop assignment
                    }
                }

                // 2️⃣ fetch last assignment ID to generate next sequential ID
                service.getLastAssignmentId(
                        BuildConfig.SUPABASE_API_KEY,
                        "Bearer " + BuildConfig.SUPABASE_API_KEY,
                        "assignment_id",
                        "assignment_id.desc",
                        1
                ).enqueue(new Callback<List<Map<String, Object>>>() {
                    @Override
                    public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                        String nextId;
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            String lastId = (String) response.body().get(0).get("assignment_id");
                            nextId = getNextAssignmentId(lastId);
                        } else {
                            nextId = "AS00001";
                        }
                        // 3️⃣ assign the driver
                        assignDriverWithId(nextId, driverId, franchiseId);
                    }

                    @Override
                    public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                        Toast.makeText(AssignDriverActivity.this, "Error fetching last ID: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(AssignDriverActivity.this, "Error fetching latest assignment: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getNextAssignmentId(String lastId) {
        if (lastId == null || lastId.isEmpty()) return "AS00001";
        try {
            int num = Integer.parseInt(lastId.substring(2)) + 1;
            return String.format("AS%05d", num);
        } catch (Exception e) {
            e.printStackTrace();
            return "AS00001";
        }
    }

    private void assignDriverWithId(String assignmentId, String driverId, String franchiseId) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        String assignedAt = sdf.format(new Date());

        Assignment assignment = new Assignment(assignmentId, driverId, franchiseId, assignedAt);

        SupabaseService service = SupabaseApiClient.getRetrofitInstance().create(SupabaseService.class);

        service.assignDriver(assignment).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AssignDriverActivity.this,
                            "Driver assigned successfully with ID " + assignmentId,
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AssignDriverActivity.this, "Failed to assign driver (server error)", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(AssignDriverActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

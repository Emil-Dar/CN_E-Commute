package com.example.cne_commute;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AssignmentHistoryActivity extends AppCompatActivity {

    private RecyclerView assignmentRecyclerView;
    private AssignmentAdapter adapter;
    private List<Assignment> assignmentList = new ArrayList<>();

    private String franchiseId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment_history);

        assignmentRecyclerView = findViewById(R.id.assignmentRecyclerView);
        assignmentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AssignmentAdapter(assignmentList);
        assignmentRecyclerView.setAdapter(adapter);

        franchiseId = getIntent().getStringExtra("franchiseId");

        fetchAssignmentHistory();
    }

    private void fetchAssignmentHistory() {
        SupabaseService service = SupabaseApiClient.getRetrofitInstance().create(SupabaseService.class);
        service.getLatestAssignmentForFranchise(
                BuildConfig.SUPABASE_API_KEY,
                "Bearer " + BuildConfig.SUPABASE_API_KEY,
                "eq." + franchiseId,
                "assigned_at.desc",
                50 // fetch last 50 assignments
        ).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    assignmentList.clear();
                    for (Map<String, Object> map : response.body()) {
                        String assignmentId = String.valueOf(map.get("assignment_id"));
                        String driverId = String.valueOf(map.get("driver_id")); // optionally fetch driver name
                        String assignedAt = String.valueOf(map.get("assigned_at"));
                        assignmentList.add(new Assignment(assignmentId, driverId, franchiseId, assignedAt));
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(AssignmentHistoryActivity.this, "Failed to load history", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(AssignmentHistoryActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

package com.example.cne_commute;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationActivity extends AppCompatActivity {

    private static final String TAG = "NotificationActivity";
    private RecyclerView recyclerView;
    private TextView emptyMessage;
    private AcceptedReportAdapter adapter;
    private SupabaseRealtimeListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate triggered — setting layout");
        setContentView(R.layout.activity_notification);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            Log.d(TAG, "Toolbar configured — setting title and back button");
            getSupportActionBar().setTitle("Notifications");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.recycler_notifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Log.d(TAG, "RecyclerView initialized and layout manager set");

        emptyMessage = findViewById(R.id.empty_message);
        Log.d(TAG, "Empty message TextView bound");

        fetchAcceptedReports();
        startRealtimeListener();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listener != null) {
            listener.stopListening();
            Log.d(TAG, "Realtime listener stopped");
        }
    }

    private void fetchAcceptedReports() {
        String userId = getCurrentUserId();
        Log.d(TAG, "Fetching accepted reports for userId: " + userId);

        ReportApiService apiService = ApiClient.getClient().create(ReportApiService.class);
        Call<List<ReportData>> call = apiService.getAcceptedReports("eq." + userId, "eq.accepted");

        call.enqueue(new Callback<List<ReportData>>() {
            @Override
            public void onResponse(Call<List<ReportData>> call, Response<List<ReportData>> response) {
                Log.d(TAG, "API response received");
                if (response.isSuccessful() && response.body() != null) {
                    List<ReportData> acceptedReports = response.body();
                    Log.d(TAG, "Accepted reports count: " + acceptedReports.size());

                    for (ReportData report : acceptedReports) {
                        Log.d(TAG, "Report object: " + report.toString());
                        Log.d(TAG, "→ reportId: " + report.getReportId());
                        Log.d(TAG, "→ reportCode: " + report.getReportCode());
                        Log.d(TAG, "→ timestamp: " + report.getTimestamp());
                        Log.d(TAG, "→ status: " + report.getStatus());
                        Log.d(TAG, "→ remarks: " + report.getRemarks());

                        markReportAsRead(report.getReportId());
                    }

                    if (!acceptedReports.isEmpty()) {
                        Log.d(TAG, "Accepted reports found — updating adapter");
                        if (adapter == null) {
                            adapter = new AcceptedReportAdapter(acceptedReports);
                            recyclerView.setAdapter(adapter);
                            Log.d(TAG, "Adapter created and set to RecyclerView");
                        } else {
                            adapter.setReports(acceptedReports);
                            adapter.notifyDataSetChanged();
                            Log.d(TAG, "Adapter updated with new report list");
                        }
                        emptyMessage.setVisibility(TextView.GONE);
                        Log.d(TAG, "Empty message hidden");
                    } else {
                        emptyMessage.setVisibility(TextView.VISIBLE);
                        Log.d(TAG, "No reports found — showing empty message");
                    }
                } else {
                    Log.w(TAG, "No accepted reports or failed response");
                    emptyMessage.setVisibility(TextView.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<ReportData>> call, Throwable t) {
                Log.e(TAG, "Failed to fetch accepted reports: " + t.getMessage());
                emptyMessage.setVisibility(TextView.VISIBLE);
            }
        });
    }

    private void markReportAsRead(String reportId) {
        ReportApiService apiService = ApiClient.getClient().create(ReportApiService.class);
        Map<String, Object> body = new HashMap<>();
        body.put("is_read", true);

        Log.d(TAG, "Marking report as read — reportId: " + reportId);
        Call<Void> call = apiService.markReportAsRead("eq." + reportId, body);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d(TAG, "Marked report as read: " + reportId + " — success: " + response.isSuccessful());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Failed to mark report as read: " + reportId + " — " + t.getMessage());
            }
        });
    }

    private String getCurrentUserId() {
        Log.d(TAG, "getCurrentUserId() called — retrieving from SupabaseAuthManager");
        return SupabaseAuthManager.getInstance().getCurrentUserId();
    }

    private void startRealtimeListener() {
        listener = new SupabaseRealtimeListener();
        listener.startListening(reportId -> {
            Log.d(TAG, "Realtime update — report accepted: " + reportId);
            runOnUiThread(this::fetchAcceptedReports);
        });
    }
}

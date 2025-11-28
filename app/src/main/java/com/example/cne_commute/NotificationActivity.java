package com.example.cne_commute;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.widget.Toast;


public class NotificationActivity extends AppCompatActivity {

    private static final String TAG = "NotificationActivity";

    private RecyclerView recyclerView;
    private TextView emptyMessage;
    private NotificationAdapter adapter;
    private SupabaseRealtimeListener listener;
    private final List<NotificationItem> notificationItems = new ArrayList<>();

    private boolean reportsFetched = false;
    private boolean appointmentsFetched = false;

    private boolean selectionMode = false; // track selection mode


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called");

        setContentView(R.layout.activity_notification);
        setupToolbar();
        bindViews();

        adapter = new NotificationAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(adapter);

        refreshNotifications();
        startRealtimeListener();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Notifications");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void bindViews() {
        recyclerView = findViewById(R.id.recycler_notifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        emptyMessage = findViewById(R.id.empty_message);
    }

    private void refreshNotifications() {
        Log.d(TAG, "Refreshing notifications...");
        notificationItems.clear();
        reportsFetched = false;
        appointmentsFetched = false;

        fetchReportNotifications();
        fetchAppointments();
    }

    // -------------------- REPORT NOTIFICATIONS --------------------

    private void fetchReportNotifications() {
        String userId = getCurrentUserId();
        Log.d(TAG, "Fetching reports for userId: " + userId);

        ReportApiService apiService = ApiClient.getClient().create(ReportApiService.class);
        Call<List<ReportData>> call = apiService.getReportsByStatuses(
                "eq." + userId,
                "eq.Accepted" // ✅ Only accepted reports
        );

        call.enqueue(new Callback<List<ReportData>>() {
            @Override
            public void onResponse(Call<List<ReportData>> call, Response<List<ReportData>> response) {
                Log.d(TAG, "Report fetch success: " + response.isSuccessful());

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    for (ReportData report : response.body()) {
                        NotificationItem item = new NotificationItem(
                                "report",
                                "Report Accepted",
                                report.getRemarks() != null ? report.getRemarks() : "No remarks available.",
                                report.getTimestamp(),
                                report.getReportId()
                        );
                        notificationItems.add(item);
                        markReportAsRead(report.getReportId());
                    }
                }

                reportsFetched = true;
                checkAndUpdateRecyclerView();
            }

            @Override
            public void onFailure(Call<List<ReportData>> call, Throwable t) {
                Log.e(TAG, "Report fetch failed: " + t.getMessage());
                reportsFetched = true;
                checkAndUpdateRecyclerView();
            }
        });
    }

    // -------------------- APPOINTMENT NOTIFICATIONS --------------------

    private void fetchAppointments() {
        String userId = getCurrentUserId();
        Log.d(TAG, "Fetching appointments for userId: " + userId);

        AppointmentApiService apiService = ApiClient.getClient().create(AppointmentApiService.class);
        Map<String, String> filters = new HashMap<>();
        filters.put("select", "*");
        filters.put("commuter_id", "eq." + userId);
        filters.put("status", "eq.Scheduled"); // ✅ Only scheduled appointments

        Call<List<Appointment>> call = apiService.getAppointmentsForUser(filters);

        call.enqueue(new Callback<List<Appointment>>() {
            @Override
            public void onResponse(Call<List<Appointment>> call, Response<List<Appointment>> response) {
                Log.d(TAG, "Appointment fetch success: " + response.isSuccessful());

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    for (Appointment appt : response.body()) {
                        String dateTime = appt.getScheduledDate() + " " + appt.getScheduledTime();

                        NotificationItem item = new NotificationItem(
                                "appointment",
                                "Appointment Scheduled",
                                "With Driver on " + dateTime,
                                dateTime,
                                appt.getAppointmentId()
                        );
                        notificationItems.add(item);
                    }
                }

                appointmentsFetched = true;
                checkAndUpdateRecyclerView();
            }

            @Override
            public void onFailure(Call<List<Appointment>> call, Throwable t) {
                Log.e(TAG, "Appointment fetch failed: " + t.getMessage());
                appointmentsFetched = true;
                checkAndUpdateRecyclerView();
            }
        });
    }

    // -------------------- UPDATE UI --------------------

    private void checkAndUpdateRecyclerView() {
        if (reportsFetched && appointmentsFetched) {
            runOnUiThread(this::updateRecyclerView);
        }
    }

    private void updateRecyclerView() {
        Log.d(TAG, "Updating RecyclerView — Total items: " + notificationItems.size());

        if (notificationItems.isEmpty()) {
            emptyMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyMessage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.setItems(notificationItems);

            // 🔑 Apply sorting here
            adapter.sortByDateDescending();
        }
    }

    // -------------------- HELPERS --------------------

    private void markReportAsRead(String reportId) {
        ReportApiService apiService = ApiClient.getClient().create(ReportApiService.class);
        Map<String, Object> body = new HashMap<>();
        body.put("is_read", true);

        Call<Void> call = apiService.markReportAsRead("eq." + reportId, body);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d(TAG, "Marked report as read: " + reportId);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Failed to mark report as read: " + t.getMessage());
            }
        });
    }

    private String getCurrentUserId() {
        return SupabaseAuthManager.getInstance().getCurrentUserId();
    }

    private void startRealtimeListener() {
        listener = new SupabaseRealtimeListener();
        listener.startListening(reportId -> runOnUiThread(this::refreshNotifications));
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
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshNotifications();
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_notifications, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sort_date:
                adapter.sortByDateDescending();
                return true;
            case R.id.action_sort_report:
                adapter.sortByReport();
                return true;
            case R.id.action_sort_appointment:
                adapter.sortByAppointment();
                return true;
            case R.id.action_clear_all:   // 🔑 now toggles selection mode
                toggleSelectionMode();
                return true;
            case R.id.action_delete_selected: // 🔑 new delete action
                deleteSelectedNotifications();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void toggleSelectionMode() {
        selectionMode = !selectionMode;
        adapter.setSelectionMode(selectionMode);

        if (selectionMode) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Select to Delete");
            }
            Toast.makeText(this, "Select notifications to delete", Toast.LENGTH_SHORT).show();
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Notifications");
            }
        }
    }

    private void deleteSelectedNotifications() {
        List<NotificationItem> selected = adapter.getSelectedItems();
        if (selected.isEmpty()) {
            Toast.makeText(this, "No items selected", Toast.LENGTH_SHORT).show();
            return;
        }

        notificationItems.removeAll(selected);
        adapter.setItems(notificationItems);
        adapter.setSelectionMode(false);
        selectionMode = false;

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Notifications");
        }

        checkAndUpdateRecyclerView();
        Toast.makeText(this, selected.size() + " notifications deleted", Toast.LENGTH_SHORT).show();
    }



    // -------------------- CLEAR ALL --------------------

    private void confirmClearAll() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Clear All Notifications")
                .setMessage("Are you sure you want to delete all notifications?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    notificationItems.clear();
                    adapter.setItems(notificationItems);
                    emptyMessage.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    Log.d(TAG, "All notifications cleared.");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}

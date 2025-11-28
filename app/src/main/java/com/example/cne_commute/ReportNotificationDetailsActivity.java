package com.example.cne_commute;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportNotificationDetailsActivity extends AppCompatActivity {

    private static final String TAG = "ReportNotifDetails";

    private TextView driverNameView, statusView, remarksView, timestampView;
    private String reportId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_notification_details);

        setupToolbar();

        // Get reportId passed from NotificationAdapter
        reportId = getIntent().getStringExtra("report_id");
        Log.d(TAG, "Opened with reportId: " + reportId);

        bindViews();
        loadReportDetails(reportId);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Report Notification");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void bindViews() {
        driverNameView = findViewById(R.id.text_driver_name);
        statusView = findViewById(R.id.text_status);
        remarksView = findViewById(R.id.text_remarks);
        timestampView = findViewById(R.id.text_timestamp);
    }

    private void loadReportDetails(String reportId) {
        ReportApiService apiService = ApiClient.getClient().create(ReportApiService.class);

        // Endpoint returns a list; fetch and use the first item
        Call<List<ReportData>> call = apiService.getReportById("eq." + reportId);

        call.enqueue(new Callback<List<ReportData>>() {
            @Override
            public void onResponse(Call<List<ReportData>> call, Response<List<ReportData>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    ReportData report = response.body().get(0);
                    driverNameView.setText(report.getDriverName());
                    statusView.setText(report.getStatus());
                    remarksView.setText(report.getRemarks() != null ? report.getRemarks() : "No remarks");
                    timestampView.setText(formatTimestamp(report.getTimestamp()));
                } else {
                    Log.e(TAG, "Failed to load report details or empty response");
                }
            }

            @Override
            public void onFailure(Call<List<ReportData>> call, Throwable t) {
                Log.e(TAG, "Error fetching report details: " + t.getMessage());
            }
        });
    }

    /**
     * Format timestamps into "Nov 01, 2025 10:30 pm"
     */
    private String formatTimestamp(String raw) {
        if (raw == null || raw.trim().isEmpty()) return "";

        try {
            // Normalize: strip fractional seconds if present
            String normalized = raw.replaceFirst("\\.(\\d+)", "");

            Date date;

            // Case 1: ISO with 'Z' (UTC)
            if (normalized.endsWith("Z")) {
                SimpleDateFormat inUtc = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                inUtc.setTimeZone(TimeZone.getTimeZone("UTC"));
                date = inUtc.parse(normalized);

                // Case 2: ISO without timezone
            } else if (normalized.contains("T")) {
                SimpleDateFormat inLocal = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                date = inLocal.parse(normalized);

                // Case 3: SQL style (fallback)
            } else {
                SimpleDateFormat inLocalSql = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                date = inLocalSql.parse(normalized);
            }

            SimpleDateFormat out = new SimpleDateFormat("MMMM dd, yyyy hh:mm a", Locale.getDefault());
            return out.format(date); // don’t call .toLowerCase()

        } catch (Exception e) {
            Log.e(TAG, "Timestamp parsing error: " + e.getMessage());
            return raw;
        }
    }
}

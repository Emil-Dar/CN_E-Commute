package com.example.cne_commute;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;



import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.media.MediaPlayer;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OperatorNotificationActivity extends AppCompatActivity {

    private String operatorId;

    // Containers
    private LinearLayout statusContainer;
    private LinearLayout violationContainer;

    private ProgressBar progressBar;
    private EditText searchBar;
    private Spinner dateFilter;

    private SupabaseService supabaseService;
    private String selectedFilter = "All"; // default

    private android.os.Handler handler;
    private Runnable pollRunnable;
    private MediaPlayer notificationPlayer;
    private int lastNotificationCount = 0;



    private final SimpleDateFormat isoNoMs = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    private final SimpleDateFormat isoWithMs = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
    private final SimpleDateFormat human = new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_notification);

        operatorId = getIntent().getStringExtra("operatorId");

        statusContainer = findViewById(R.id.status_notif_container);
        violationContainer = findViewById(R.id.violation_notif_container);
        progressBar = findViewById(R.id.progress_bar);
        searchBar = findViewById(R.id.searchBar);
        dateFilter = findViewById(R.id.dateFilter);

        supabaseService = SupabaseApiClient.getRetrofitInstance().create(SupabaseService.class);

        setupBottomNav();
        setupDateFilter();

        // 🔔 prepare sound
        notificationPlayer = MediaPlayer.create(this, R.raw.notification); // put notification.mp3 in res/raw

        // 🕐 initial load
        loadAll(searchBar.getText().toString().trim(), selectedFilter);

        // 🔄 realtime polling every 10 seconds
        handler = new android.os.Handler();
        pollRunnable = new Runnable() {
            @Override public void run() {
                loadAll(searchBar.getText().toString().trim(), selectedFilter);
                handler.postDelayed(this, 10000);
            }
        };
        handler.postDelayed(pollRunnable, 10000);

        // 🔍 search listener
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadAll(s.toString().trim(), selectedFilter);
            }
        });
    }



    private boolean isLoading = false;

    private void loadAll(String searchTerm, String filterType) {
        if (isLoading) return;
        isLoading = true;
        progressBar.setVisibility(View.VISIBLE);

        fetchDriverStatusNotifications(searchTerm, filterType, () -> {
            fetchViolationNotifications(searchTerm, filterType, () -> {
                progressBar.setVisibility(View.GONE);
                isLoading = false;

                int totalNow = statusContainer.getChildCount() + violationContainer.getChildCount();
                playNotificationSoundIfNew(totalNow);

                if (totalNow == 0) {
                    showEmpty(statusContainer, "No notifications found.");
                }
            });
        });
    }



    // =========================
    // 🔽 DATE FILTER FUNCTION
    // =========================
    private void setupDateFilter() {
        dateFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedFilter = parent.getItemAtPosition(position).toString();
                loadAll(searchBar.getText().toString().trim(), selectedFilter);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // =========================
    // 🔽 BOTTOM NAVIGATION
    // =========================
    private void setupBottomNav() {
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_notification);

        bottomNav.setOnItemSelectedListener(item -> {
            Intent intent = null;
            switch (item.getItemId()) {
                case R.id.nav_home:
                    intent = new Intent(this, OperatorHomeActivity.class);
                    break;
                case R.id.nav_franchise:
                    intent = new Intent(this, OperatorFranchiseActivity.class);
                    break;
                case R.id.nav_account:
                    intent = new Intent(this, OperatorAccountActivity.class);
                    break;
                case R.id.nav_notification:
                    return true;
            }
            if (intent != null) {
                intent.putExtra("operatorId", operatorId);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }

    // =========================
    // 🔽 DRIVER STATUS NOTIFICATIONS
    // =========================
    // =======================================
// ✅ FIXED DRIVER STATUS FETCH
// =======================================
    private void fetchDriverStatusNotifications(String searchTerm, String filterType, Runnable done) {
        supabaseService.getDrivers(
                SupabaseApiClient.getApiKey(),
                "Bearer " + SupabaseApiClient.getApiKey()
        ).enqueue(new Callback<List<Driver>>() {
            @Override
            public void onResponse(Call<List<Driver>> call, Response<List<Driver>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    done.run();
                    return;
                }

                List<Driver> allDrivers = response.body();
                List<Driver> filtered = new ArrayList<>();

                for (Driver d : allDrivers) {
                    if (d == null) continue;

                    // Only verified drivers for this operator
                    if (!safe(d.getRequestedBy()).equals(operatorId)) continue;
                    if (!safe(d.getStatus()).equalsIgnoreCase("Verified")) continue;

                    if (!matchesDateFilter(safe(d.getCreatedAt()), filterType)) continue;

                    String hay = (safe(d.getDriverId()) + " " + joinName(d.getFirstName(), d.getMiddleName(), d.getLastName()) + " " + safe(d.getStatus())).toLowerCase(Locale.ROOT);
                    if (!searchTerm.isEmpty() && !hay.contains(searchTerm.toLowerCase(Locale.ROOT))) continue;

                    filtered.add(d);
                }

                // Sort newest first
                filtered.sort((a, b) -> {
                    Date da = parseAnyDate(safe(a.getCreatedAt()));
                    Date db = parseAnyDate(safe(b.getCreatedAt()));
                    if (da != null && db != null) return db.compareTo(da);
                    return 0;
                });

                runOnUiThread(() -> {
                    // ✅ clear all existing views first
                    statusContainer.removeAllViews();

                    for (Driver d : filtered) {
                        createStatusNotificationCard(
                                safe(d.getDriverId()),
                                joinName(d.getFirstName(), d.getMiddleName(), d.getLastName()),
                                safe(d.getStatus()),
                                safe(d.getCreatedAt())
                        );
                    }

                    done.run();
                });
            }

            @Override
            public void onFailure(Call<List<Driver>> call, Throwable t) {
                done.run();
            }
        });
    }




    // =========================
    // 🔽 VIOLATION NOTIFICATIONS
    // =========================
    private void fetchViolationNotifications(String searchTerm, String filterType, Runnable done) {
        Call<List<Map<String, Object>>> call = supabaseService.getReports(
                SupabaseApiClient.getApiKey(),
                "Bearer " + SupabaseApiClient.getApiKey()
        );

        call.enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    done.run();
                    return;
                }

                List<Map<String, Object>> reports = response.body();
                List<Map<String, Object>> mine = new ArrayList<>();

                for (Map<String, Object> report : reports) {
                    String createdAt = str(report.get("created_at"));
                    if (!filterType.equals("All") && !matchesDateFilter(createdAt, filterType))
                        continue;

                    mine.add(report);
                }

                // ✅ Sort newest first
                Collections.sort(mine, (a, b) -> {
                    Date da = parseAnyDate(str(a.get("created_at")));
                    Date db = parseAnyDate(str(b.get("created_at")));
                    if (da == null || db == null) return 0;
                    return db.compareTo(da);
                });

                for (Map<String, Object> report : mine) {
                    String franchiseId = str(report.get("franchise_id"));
                    String driverName = str(report.get("driver_name"));
                    String violations = collectViolations(report);
                    String remarks = str(report.get("remarks"));
                    String status = str(report.get("status"));
                    String createdAt = str(report.get("created_at"));
                    String reportId = str(report.get("report_id"));

                    // ✅ Persist viewed state locally
                    boolean viewed = isViolationViewed(reportId)
                            || "true".equalsIgnoreCase(str(report.get("viewed_by_operator")));

                    // ✅ Apply search filter
                    if (!searchTerm.isEmpty()
                            && !violations.toLowerCase().contains(searchTerm.toLowerCase())
                            && !driverName.toLowerCase().contains(searchTerm.toLowerCase()))
                        continue;

                    createViolationItem(franchiseId, driverName, status, violations, remarks, createdAt, viewed, reportId);
                }

                done.run();
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                done.run();
            }
        });
    }




    private void showEmpty(LinearLayout target, String message) {
        TextView empty = new TextView(this);
        empty.setText(message);
        empty.setTextSize(16);
        empty.setPadding(30, 50, 30, 30);
        target.addView(empty);
    }

    // =========================
    // 🔽 DATE FILTER LOGIC
    // =========================
    private boolean matchesDateFilter(String createdAt, String filterType) {
        if (createdAt == null || createdAt.isEmpty()) return true; // don't over-filter when missing
        try {
            Date reportDate = parseAnyDate(createdAt);
            if (reportDate == null) return true;

            Date now = new Date();
            long diff = now.getTime() - reportDate.getTime();
            long days = diff / (1000 * 60 * 60 * 24);

            switch (filterType) {
                case "Today": return days < 1;
                case "This Week": return days < 7;
                case "This Month": return days < 30;
                default: return true;
            }
        } catch (Exception e) {
            return true;
        }
    }

    private Date parseAnyDate(String s) {
        if (s == null || s.isEmpty()) return null;
        try { return isoWithMs.parse(s); } catch (ParseException ignored) {}
        try { return isoNoMs.parse(s); } catch (ParseException ignored) {}
        return null;
    }

    private String formatDate(String dateStr) {
        try {
            Date d = parseAnyDate(dateStr);
            if (d == null) return dateStr;
            return human.format(d);
        } catch (Exception e) {
            return dateStr;
        }
    }

    // =========================
    // 🔽 BUILD: DRIVER STATUS CARD
    // =========================
    @SuppressLint("SetTextI18n")
    private void createStatusNotificationCard(String driverId, String driverName, String status, String createdAt) {

        View itemView = getLayoutInflater().inflate(R.layout.item_operator_status_notif, statusContainer, false);

        TextView tvMessage = itemView.findViewById(R.id.tv_message);
        TextView tvBadge = itemView.findViewById(R.id.tv_status_badge);
        TextView tvDriverId = itemView.findViewById(R.id.tv_driver_id);
        TextView tvDriverName = itemView.findViewById(R.id.tv_driver_name);
        TextView tvStatus = itemView.findViewById(R.id.tv_status);
        TextView tvDate = itemView.findViewById(R.id.tv_date);
        LinearLayout detailsLayout = itemView.findViewById(R.id.details_layout);
        ImageView ivExpand = itemView.findViewById(R.id.iv_expand);

        // ✅ Generate message
        String normalized = safe(status).toLowerCase(Locale.ROOT);
        String message;

        if (normalized.contains("verified") || normalized.contains("approved")) {
            message = "The driver you registered has been verified.";
        } else if (normalized.contains("rejected") || normalized.contains("denied")) {
            message = "The driver you registered has been rejected.";
        } else {
            message = "The driver you registered is pending review.";
        }

        tvMessage.setText(message);
        tvBadge.setText(cap(status));
        applyStatusPill(tvBadge, status);

        tvDriverId.setText("Driver ID: " + driverId);
        tvDriverName.setText("Driver Name: " + driverName);
        tvStatus.setText("Status: " + cap(status));
        tvDate.setText("Updated: " + (createdAt.isEmpty() ? "—" : formatDate(createdAt)));

        // ✅ NEW — CHECK IF THIS DRIVER STATUS WAS ALREADY VIEWED
        boolean viewed = isDriverStatusViewed(driverId);

        // ✅ NEW — Set background based on viewed state
        itemView.setBackgroundResource(
                viewed ? R.drawable.bg_notification_viewed : R.drawable.bg_notification_unviewed
        );

        // ✅ Expand and mark as viewed
        ivExpand.setOnClickListener(v -> {
            boolean visible = detailsLayout.getVisibility() == View.VISIBLE;
            detailsLayout.setVisibility(visible ? View.GONE : View.VISIBLE);
            ivExpand.setRotation(visible ? 0 : 180);

            if (!viewed) {
                setDriverStatusViewed(driverId);
                itemView.setBackgroundResource(R.drawable.bg_notification_viewed);
            }
        });

        statusContainer.addView(itemView);
    }

    private boolean isDriverStatusViewed(String driverId) {
        return getSharedPreferences("driver_status_viewed", MODE_PRIVATE)
                .getBoolean(driverId, false);
    }

    private void setDriverStatusViewed(String driverId) {
        getSharedPreferences("driver_status_viewed", MODE_PRIVATE)
                .edit()
                .putBoolean(driverId, true)
                .apply();
    }

    // ✅ LOCAL VIEWED PERSISTENCE FOR VIOLATIONS
    private boolean isViolationViewed(String reportId) {
        return getSharedPreferences("violation_viewed", MODE_PRIVATE)
                .getBoolean(reportId, false);
    }

    private void setViolationViewed(String reportId) {
        getSharedPreferences("violation_viewed", MODE_PRIVATE)
                .edit()
                .putBoolean(reportId, true)
                .apply();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && pollRunnable != null) handler.removeCallbacks(pollRunnable);
        if (notificationPlayer != null) {
            notificationPlayer.release();
            notificationPlayer = null;
        }
        isLoading = false;
    }



    // =========================
    // 🔽 BUILD: VIOLATION CARD
    // =========================
    @SuppressLint("SetTextI18n")
    private void createViolationItem(String franchiseId, String driverName, String status, String violations,
                                     String remarks, String createdAt, boolean viewed, String reportId) {

        View itemView = getLayoutInflater().inflate(R.layout.item_operator_notif, violationContainer, false);

        TextView tvMessage = itemView.findViewById(R.id.tv_message);
        TextView tvStatus = itemView.findViewById(R.id.tv_status);
        TextView tvViolation = itemView.findViewById(R.id.violations);
        TextView tvRemarks = itemView.findViewById(R.id.remarks);
        TextView tvDate = itemView.findViewById(R.id.date);
        LinearLayout detailsLayout = itemView.findViewById(R.id.details_layout);
        ImageView ivExpand = itemView.findViewById(R.id.iv_expand);

        tvMessage.setText("Your driver " + driverName +
                " of franchise #" + franchiseId + " has received a complaint.");
        tvStatus.setText("Status: " + status);
        tvViolation.setText("Violations: " + (violations.isEmpty() ? "None" : violations));
        tvRemarks.setText("Remarks: " + (remarks.isEmpty() ? "None" : remarks));
        tvRemarks.setVisibility(remarks.isEmpty() ? View.GONE : View.VISIBLE);
        tvDate.setText("Filed on: " + formatDate(createdAt));

        applyStatusBadge(tvStatus, status, remarks);

        itemView.setBackgroundResource(
                viewed ? R.drawable.bg_notification_viewed : R.drawable.bg_notification_unviewed
        );

        ivExpand.setOnClickListener(v -> {
            boolean visible = detailsLayout.getVisibility() == View.VISIBLE;
            detailsLayout.setVisibility(visible ? View.GONE : View.VISIBLE);
            ivExpand.setRotation(visible ? 0 : 180);

            if (!viewed) {
                markAsViewed(reportId);
                setViolationViewed(reportId);
                itemView.setBackgroundResource(R.drawable.bg_notification_viewed);
            }
        });

        violationContainer.addView(itemView);
    }


    private void applyStatusBadge(TextView statusView, String status, String remarks) {
        int badgeColor = ContextCompat.getColor(this, R.color.dark_gray);
        String displayStatus = status != null ? status : "Pending";

        try {
            String s = status.toLowerCase(Locale.ROOT);
            String r = remarks.toLowerCase(Locale.ROOT);

            if (r.contains("resolved") && !r.contains("unresolved")) {
                displayStatus = "Resolved";
                badgeColor = ContextCompat.getColor(this, R.color.status_resolved_green);

            } else {
                switch (s) {
                    case "pending":
                        badgeColor = ContextCompat.getColor(this, R.color.status_pending_red);
                        break;

                    case "accepted":
                        badgeColor = ContextCompat.getColor(this, R.color.status_accepted_yellow);
                        break;

                    case "scheduled":
                        badgeColor = ContextCompat.getColor(this, R.color.status_scheduled_blue);
                        break;

                    case "resolved":
                        badgeColor = ContextCompat.getColor(this, R.color.status_resolved_green);
                        break;

                    case "unresolved":
                        badgeColor = ContextCompat.getColor(this, R.color.status_unresolved_gray);
                        break;
                }
            }
        } catch (Exception ignored) { }

        statusView.setText("Status: " + displayStatus);

        GradientDrawable bgShape = (GradientDrawable)
                ContextCompat.getDrawable(this, R.drawable.status_badge_bg).mutate();
        bgShape.setColor(badgeColor);
        statusView.setBackground(bgShape);
    }



    // =========================
    // 🔽 VIOLATIONS CONCAT
    // =========================
    private String collectViolations(Map<String, Object> report) {
        StringBuilder vio = new StringBuilder();
        for (String field : Arrays.asList(
                "parking_obstruction_violations",
                "traffic_movement_violations",
                "driver_behavior_violations",
                "licensing_documentation_violations",
                "attire_fare_violations")) {
            if (report.containsKey(field) && report.get(field) != null) {
                String val = String.valueOf(report.get(field));
                if (!"none".equalsIgnoreCase(val) && !val.trim().isEmpty()) {
                    if (vio.length() > 0) vio.append("; ");
                    vio.append(val.trim());
                }
            }
        }
        return vio.toString();
    }

    // =========================
    // 🔽 STATUS BADGES
    // =========================
    private void applyStatusPill(TextView badgeView, String status) {
        int color = ContextCompat.getColor(this, R.color.dark_gray);
        String s = safe(status).toLowerCase(Locale.ROOT);
        if (s.contains("verify") || s.contains("approve") || s.contains("accept")) {
            color = ContextCompat.getColor(this, R.color.status_resolved_green);
        } else if (s.contains("reject") || s.contains("deny")) {
            color = ContextCompat.getColor(this, R.color.status_pending_red);
        } else if (s.contains("pending")) {
            color = ContextCompat.getColor(this, R.color.status_accepted_yellow);
        }
        GradientDrawable bg = (GradientDrawable) ContextCompat.getDrawable(this, R.drawable.status_badge_bg).mutate();
        bg.setColor(color);
        badgeView.setBackground(bg);
    }

    private void applyComplaintStatusBadge(TextView statusView, String status, String remarks) {
        int badgeColor = ContextCompat.getColor(this, R.color.dark_gray);
        String displayStatus = status != null && !status.isEmpty() ? cap(status) : "Pending";

        try {
            String s = safe(status).toLowerCase(Locale.ROOT);
            String r = safe(remarks).toLowerCase(Locale.ROOT);

            if (r.contains("resolved") && !r.contains("unresolved")) {
                displayStatus = "Resolved";
                badgeColor = ContextCompat.getColor(this, R.color.status_resolved_green);
            } else {
                if (s.contains("pending")) badgeColor = ContextCompat.getColor(this, R.color.status_pending_red);
                else if (s.contains("accept")) badgeColor = ContextCompat.getColor(this, R.color.status_accepted_yellow);
                else if (s.contains("schedule")) badgeColor = ContextCompat.getColor(this, R.color.status_scheduled_blue);
                else if (s.contains("resolved")) badgeColor = ContextCompat.getColor(this, R.color.status_resolved_green);
                else if (s.contains("unresolved")) badgeColor = ContextCompat.getColor(this, R.color.status_unresolved_gray);
            }
        } catch (Exception ignored) {}

        statusView.setText("Status: " + displayStatus);
        GradientDrawable bgShape = (GradientDrawable) ContextCompat.getDrawable(this, R.drawable.status_badge_bg).mutate();
        bgShape.setColor(badgeColor);
        statusView.setBackground(bgShape);
    }

    // =========================
    // 🔽 MARK REPORT AS VIEWED
    // =========================
    private void markAsViewed(String reportId) {
        if (reportId == null || reportId.isEmpty()) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("viewed_by_operator", true);

        Call<List<Map<String, Object>>> call = supabaseService.updateReportViewed(
                SupabaseApiClient.getApiKey(),
                "Bearer " + SupabaseApiClient.getApiKey(),
                "eq." + reportId,
                updates
        );
        call.enqueue(new Callback<List<Map<String, Object>>>() {
            @Override public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {}
            @Override public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {}
        });
    }

    // =========================
    // 🔽 HELPERS
    // =========================
    private String safe(String s) { return s == null ? "" : s; }
    private String str(Object o) { return o == null ? "" : o.toString(); }

    private String cap(String s) {
        s = safe(s).trim();
        if (s.isEmpty()) return "—";
        return s.substring(0,1).toUpperCase(Locale.ROOT) + s.substring(1);
    }

    private String joinName(String first, String middle, String last) {
        String f = safe(first).trim();
        String m = safe(middle).trim();
        String l = safe(last).trim();
        String full = (f + " " + (m.isEmpty() ? "" : (m + " ")) + l).replaceAll("\\s+", " ").trim();
        return full.isEmpty() ? "—" : full;
    }

    private void playNotificationSoundIfNew(int currentCount) {
        if (currentCount > lastNotificationCount && notificationPlayer != null) {
            notificationPlayer.start();
        }
        lastNotificationCount = currentCount;
    }

}



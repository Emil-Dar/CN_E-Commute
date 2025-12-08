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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

    private List<String> operatorDriverIds = new ArrayList<>();



    private boolean isLoading = false;

    // Date formats
    private final SimpleDateFormat isoNoMs = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    private final SimpleDateFormat isoWithMs = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
    private final SimpleDateFormat human = new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_notification);

        operatorId = getIntent().getStringExtra("operatorId");
        if (operatorId == null) operatorId = "";

        statusContainer = findViewById(R.id.status_notif_container);
        violationContainer = findViewById(R.id.violation_notif_container);
        progressBar = findViewById(R.id.progress_bar);
        searchBar = findViewById(R.id.searchBar);
        dateFilter = findViewById(R.id.dateFilter);

        supabaseService = SupabaseApiClient.getRetrofitInstance().create(SupabaseService.class);

        setupBottomNav();
        setupDateFilter();

        // Initial load
        loadAll("", "All");

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadAll(s.toString().trim(), selectedFilter);
            }
        });
    }

    private void loadAll(String searchTerm, String filterType) {
        if (isLoading) return;
        isLoading = true;

        progressBar.setVisibility(View.VISIBLE);
        statusContainer.removeAllViews();
        violationContainer.removeAllViews();

        // Fetch status notifications -> then violation notifications
        fetchDriverStatusNotifications(searchTerm, filterType, () ->
                fetchViolationNotifications(searchTerm, filterType, () -> {
                    progressBar.setVisibility(View.GONE);
                    isLoading = false;

                    int total = statusContainer.getChildCount() + violationContainer.getChildCount();
                    if (total == 0) {
                        showEmpty(statusContainer, "No notifications found.");
                    }
                }));

    }

    // =========================
    // 🔽 DATE FILTER
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
    // 🔽 BOTTOM NAV
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

                List<Driver> drivers = response.body();
                List<Driver> filtered = new ArrayList<>();

                // 🔥 RESET then re-collect all operator-owned drivers
                operatorDriverIds.clear();

                for (Driver driver : drivers) {
                    if (driver == null) continue;

                    // Always collect operator-owned drivers
                    if (safe(driver.getRequestedBy()).equals(operatorId)) {
                        operatorDriverIds.add(safe(driver.getDriverId()));
                    }

                    // Only verified drivers get status notifications — but this should NOT affect violation notifications
                    if (!safe(driver.getRequestedBy()).equals(operatorId)) continue;
                    if (!"Verified".equalsIgnoreCase(safe(driver.getStatus()))) continue;

                    if (!matchesDateFilter(safe(driver.getCreatedAt()), filterType)) continue;

                    String text =
                            (safe(driver.getDriverId()) + " " +
                                    joinName(driver.getFirstName(),
                                            driver.getMiddleName(),
                                            driver.getLastName()) + " " +
                                    safe(driver.getStatus()))
                                    .toLowerCase(Locale.ROOT);

                    if (!searchTerm.isEmpty() &&
                            !text.contains(searchTerm.toLowerCase(Locale.ROOT)))
                        continue;

                    filtered.add(driver);
                }

                // Sort newest first
                filtered.sort((a, b) -> {
                    Date da = parseAnyDate(safe(a.getCreatedAt()));
                    Date db = parseAnyDate(safe(b.getCreatedAt()));
                    if (da != null && db != null) return db.compareTo(da);
                    return 0;
                });

                runOnUiThread(() -> {
                    statusContainer.removeAllViews();

                    for (Driver d : filtered) {
                        createStatusNotificationCard(
                                safe(d.getDriverId()),
                                joinName(d.getFirstName(),
                                        d.getMiddleName(),
                                        d.getLastName()),
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


    private void fetchViolationNotifications(String searchTerm, String filterType, Runnable done) {

        supabaseService.getReportsByOperator(
                SupabaseApiClient.getApiKey(),
                "Bearer " + SupabaseApiClient.getApiKey(),
                "eq." + operatorId
        ).enqueue(new Callback<List<Map<String, Object>>>() {

            @Override
            public void onResponse(Call<List<Map<String, Object>>> call,
                                   Response<List<Map<String, Object>>> response) {

                if (!response.isSuccessful() || response.body() == null) {
                    done.run();
                    return;
                }

                List<Map<String, Object>> filtered =
                        filterViolationNotifications(response.body(), searchTerm, filterType);

                filtered.sort((a, b) -> {
                    Date da = parseAnyDate(str(a.get("created_at")));
                    if (da == null) da = parseAnyDate(str(a.get("timestamp")));

                    Date db = parseAnyDate(str(b.get("created_at")));
                    if (db == null) db = parseAnyDate(str(b.get("timestamp")));

                    return (da == null || db == null) ? 0 : db.compareTo(da);
                });

                runOnUiThread(() -> {

                    violationContainer.removeAllViews();

                    for (Map<String, Object> r : filtered) {

                        String created = str(r.get("created_at"));
                        if (created.isEmpty()) created = str(r.get("timestamp"));

                        createViolationItem(
                                str(r.get("franchise_id")),
                                str(r.get("driver_name")),
                                str(r.get("status")),
                                collectViolations(r),
                                str(r.get("remarks")),
                                created,
                                isViolationViewed(str(r.get("report_id"))),
                                str(r.get("report_id"))
                        );
                    }

                    done.run();
                });
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                done.run();
            }
        });
    }

    private List<Map<String, Object>> filterViolationNotifications(
            List<Map<String, Object>> reports,
            String search,
            String filterType
    ) {
        List<Map<String, Object>> out = new ArrayList<>();

        for (Map<String, Object> r : reports) {
            if (r == null) continue;

            // operator must match
            String op = safe(str(r.get("operator_id")));
            if (!op.equalsIgnoreCase(operatorId)) continue;

            // 🔒 driver must belong to this operator
            String driverId = safe(str(r.get("driver_id")));
            if (!operatorDriverIds.contains(driverId)) continue;

            // date filter
            String created = str(r.get("created_at"));
            if (created.isEmpty()) created = str(r.get("timestamp"));
            if (!matchesDateFilter(created, filterType)) continue;

            // search filter
            String hay = (
                    str(r.get("driver_name")) + " " +
                            collectViolations(r) + " " +
                            str(r.get("remarks"))
            ).toLowerCase(Locale.ROOT);

            if (!search.isEmpty() &&
                    !hay.contains(search.toLowerCase(Locale.ROOT)))
                continue;

            out.add(r);
        }

        return out;
    }

    // =========================
    // 🔽 UI BUILDERS
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

        boolean viewed = isDriverStatusViewed(driverId);
        itemView.setBackgroundResource(viewed ? R.drawable.bg_notification_viewed : R.drawable.bg_notification_unviewed);

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
                (franchiseId.isEmpty() ? "" : " of franchise #" + franchiseId) + " has received a complaint.");
        tvStatus.setText("Status: " + cap(status));
        tvViolation.setText("Violations: " + (violations.isEmpty() ? "None" : violations));
        tvRemarks.setText("Remarks: " + (remarks.isEmpty() ? "None" : remarks));
        tvRemarks.setVisibility(remarks.isEmpty() ? View.GONE : View.VISIBLE);
        tvDate.setText("Filed on: " + (createdAt.isEmpty() ? "—" : formatDate(createdAt)));

        applyStatusBadge(tvStatus, status, remarks);

        itemView.setBackgroundResource(viewed ? R.drawable.bg_notification_viewed : R.drawable.bg_notification_unviewed);

        ivExpand.setOnClickListener(v -> {
            boolean visible = detailsLayout.getVisibility() == View.VISIBLE;
            detailsLayout.setVisibility(visible ? View.GONE : View.VISIBLE);
            ivExpand.setRotation(visible ? 0 : 180);

            if (!viewed) {
                // Update server (mark as read) and mark locally
                markReportAsViewed(reportId);
                setViolationViewed(reportId);
                itemView.setBackgroundResource(R.drawable.bg_notification_viewed);
            }
        });

        violationContainer.addView(itemView);
    }

    // =========================
    // 🔽 STYLING HELPERS
    // =========================
    private void applyStatusBadge(TextView statusView, String status, String remarks) {
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
    // 🔽 MARK REPORT AS VIEWED (server)
    // =========================
    private void markReportAsViewed(String reportId) {
        if (reportId == null || reportId.isEmpty()) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("is_read", true);

        // Assuming your Supabase route accepts a filter such as "eq.<report_id>"
        Call<List<Map<String, Object>>> call = supabaseService.updateReportViewed(
                SupabaseApiClient.getApiKey(),
                "Bearer " + SupabaseApiClient.getApiKey(),
                "eq." + reportId,
                updates
        );

        call.enqueue(new Callback<List<Map<String, Object>>>() {
            @Override public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) { }
            @Override public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) { }
        });
    }

    // =========================
    // 🔽 LOCAL VIEWED PERSISTENCE
    // =========================
    private boolean isDriverStatusViewed(String driverId) {
        if (driverId == null || driverId.isEmpty()) return false;
        return getSharedPreferences("driver_status_viewed", MODE_PRIVATE)
                .getBoolean(driverId, false);
    }

    private void setDriverStatusViewed(String driverId) {
        if (driverId == null || driverId.isEmpty()) return;
        getSharedPreferences("driver_status_viewed", MODE_PRIVATE)
                .edit()
                .putBoolean(driverId, true)
                .apply();
    }

    private boolean isViolationViewed(String reportId) {
        if (reportId == null || reportId.isEmpty()) return false;
        return getSharedPreferences("violation_viewed", MODE_PRIVATE)
                .getBoolean(reportId, false);
    }

    private void setViolationViewed(String reportId) {
        if (reportId == null || reportId.isEmpty()) return;
        getSharedPreferences("violation_viewed", MODE_PRIVATE)
                .edit()
                .putBoolean(reportId, true)
                .apply();
    }

    // =========================
    // 🔽 DATE HELPERS
    // =========================
    private boolean matchesDateFilter(String createdAt, String filterType) {
        if (createdAt == null || createdAt.isEmpty()) return true;
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
        try {
            // Some timestamps come as "yyyy-MM-dd HH:mm:ss" -- try a fallback
            SimpleDateFormat fallback = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            return fallback.parse(s);
        } catch (Exception ignored) {}
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

    private void showEmpty(LinearLayout target, String message) {
        TextView empty = new TextView(this);
        empty.setText(message);
        empty.setTextSize(16);
        empty.setPadding(30, 50, 30, 30);
        target.addView(empty);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isLoading = false;
    }
}

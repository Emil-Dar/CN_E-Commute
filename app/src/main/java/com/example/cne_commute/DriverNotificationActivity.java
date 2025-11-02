package com.example.cne_commute;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cne_commute.driver_fragments.DriverComplaints;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverNotificationActivity extends AppCompatActivity {

    private static final String TAG = "DriverNotificationAct";

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;
    private NotificationAdapter adapter;
    private SupabaseRealtimeListener realtimeListener;

    private final Set<String> locallyViewedIds = new HashSet<>();
    private String driverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_notification);

        driverId = getIntent().getStringExtra("driverID");

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        recyclerView = findViewById(R.id.notificationList);
        progressBar = findViewById(R.id.progressBar);
        emptyText = findViewById(R.id.emptyText);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NotificationAdapter((report, position) -> {
            String reportId = safeGet(report, "report_id");

            // Mark viewed locally
            adapter.markAsViewed(position);
            locallyViewedIds.add(reportId);

            // Update backend
            markReportAsViewed(reportId, () -> {
                // Optionally update badge in parent activity (if implemented)
                if (this instanceof BadgeUpdater) {
                    ((BadgeUpdater) this).decrementBadge();
                }

                // Navigate to complaint details fragment
                getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content,
                                DriverComplaints.newInstance(driverId, reportId))
                        .addToBackStack(null)
                        .commit();
            });
        });

        recyclerView.setAdapter(adapter);

        fetchNotifications();
        listenForRealtimeUpdates();
    }

    // -------------------------------------------------------------------
    // Fetch notifications
    // -------------------------------------------------------------------
    private void fetchNotifications() {
        progressBar.setVisibility(View.VISIBLE);

        SupabaseService service = SupabaseApiClient
                .getRetrofitInstance()
                .create(SupabaseService.class);

        service.getReports(
                SupabaseApiClient.SUPABASE_API_KEY,
                "Bearer " + SupabaseApiClient.SUPABASE_API_KEY
        ).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call,
                                   @NonNull Response<List<Map<String, Object>>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, Object>> reports = response.body();

                    SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                    reports.sort((a, b) -> {
                        try {
                            Date da = fmt.parse(safeGet(a, "timestamp").replace("Z", ""));
                            Date db = fmt.parse(safeGet(b, "timestamp").replace("Z", ""));
                            return db.compareTo(da);
                        } catch (Exception e) {
                            return 0;
                        }
                    });

                    for (Map<String, Object> r : reports) {
                        String id = safeGet(r, "report_id");
                        if (locallyViewedIds.contains(id)) {
                            r.put("viewed", true);
                        }
                    }

                    adapter.setData(reports);
                    toggleEmpty(reports.isEmpty(), "No notifications yet.");
                } else {
                    toggleEmpty(true, "Failed to load notifications.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Map<String, Object>>> call,
                                  @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                toggleEmpty(true, "Connection error. Please try again.");
            }
        });
    }

    // -------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------
    private void toggleEmpty(boolean show, String message) {
        emptyText.setVisibility(show ? View.VISIBLE : View.GONE);
        emptyText.setText(show ? message : "");
    }

    private void markReportAsViewed(String reportId, Runnable onSuccess) {
        if (reportId == null || reportId.isEmpty()) return;

        SupabaseService service = SupabaseApiClient
                .getRetrofitInstance()
                .create(SupabaseService.class);

        Map<String, Object> updates = new HashMap<>();
        updates.put("viewed", true);

        service.updateReportViewed(
                SupabaseApiClient.SUPABASE_API_KEY,
                "Bearer " + SupabaseApiClient.SUPABASE_API_KEY,
                "eq." + reportId,
                updates
        ).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<List<Map<String, Object>>> call,
                                   @NonNull Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "✅ Report " + reportId + " marked viewed");
                    locallyViewedIds.add(reportId);
                    if (onSuccess != null) onSuccess.run();
                } else {
                    Log.e(TAG, "❌ Failed to mark viewed: HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Map<String, Object>>> call,
                                  @NonNull Throwable t) {
                Log.e(TAG, "⚠ Network error marking viewed", t);
            }
        });
    }

    private void listenForRealtimeUpdates() {
        realtimeListener = new SupabaseRealtimeListener();
        realtimeListener.startListening(reportId -> {
            Log.d(TAG, "🔄 Realtime update for: " + reportId);
            runOnUiThread(this::fetchNotifications);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realtimeListener != null) realtimeListener.stopListening();
    }

    private String safeGet(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }

    // -------------------------------------------------------------------
    // RecyclerView Adapter
    // -------------------------------------------------------------------
    private static class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

        interface OnNotificationClickListener {
            void onNotificationClick(Map<String, Object> report, int position);
        }

        private final List<Map<String, Object>> notifications = new ArrayList<>();
        private final OnNotificationClickListener listener;

        private final SimpleDateFormat inFmt =
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        private final SimpleDateFormat outFmt =
                new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());

        NotificationAdapter(OnNotificationClickListener listener) {
            this.listener = listener;
        }

        void setData(List<Map<String, Object>> data) {
            notifications.clear();
            notifications.addAll(data);
            notifyDataSetChanged();
        }

        void markAsViewed(int position) {
            if (position >= 0 && position < notifications.size()) {
                notifications.get(position).put("viewed", true);
                notifyItemChanged(position);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_notification, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Map<String, Object> report = notifications.get(position);
            boolean viewed = report.get("viewed") instanceof Boolean && (Boolean) report.get("viewed");

            holder.message.setText("A commuter filed a report on you.");
            holder.time.setText(formatDate(safeGet(report, "timestamp")));
            holder.container.setBackgroundResource(viewed ?
                    R.drawable.bg_notification_viewed :
                    R.drawable.bg_notification_unviewed);

            holder.itemView.setOnClickListener(v ->
                    listener.onNotificationClick(report, holder.getAdapterPosition()));
        }

        private String safeGet(Map<String, Object> map, String key) {
            Object val = map.get(key);
            return val != null ? val.toString() : "";
        }

        private String formatDate(String isoDate) {
            if (isoDate == null) return "Unknown time";
            try {
                Date d = inFmt.parse(isoDate.replace("Z", ""));
                return outFmt.format(d);
            } catch (ParseException e) {
                return isoDate.replace("T", " ").replace("Z", "");
            }
        }

        @Override
        public int getItemCount() {
            return notifications.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            View container;
            TextView message, time;
            CardView card;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                container = itemView.findViewById(R.id.notificationContainer);
                message = itemView.findViewById(R.id.notificationMessage);
                time = itemView.findViewById(R.id.notificationTime);
                card = itemView.findViewById(R.id.notificationCard);
            }
        }
    }

    // -------------------------------------------------------------------
    // Optional interface for badge updates
    // -------------------------------------------------------------------
    public interface BadgeUpdater {
        void decrementBadge();
    }
}

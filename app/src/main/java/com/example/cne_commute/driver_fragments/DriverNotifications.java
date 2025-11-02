package com.example.cne_commute.driver_fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cne_commute.DriverHomeActivity;
import com.example.cne_commute.R;
import com.example.cne_commute.SupabaseApiClient;
import com.example.cne_commute.SupabaseRealtimeListener;
import com.example.cne_commute.SupabaseService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverNotifications extends Fragment {

    private static final String TAG = "DriverNotifications";
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;
    private NotificationAdapter adapter;
    private SupabaseRealtimeListener realtimeListener;
    private final List<Map<String, Object>> localReports = new ArrayList<>();
    private boolean suppressRealtimeFetch = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_driver_notifications, container, false);

        recyclerView = view.findViewById(R.id.notificationList);
        progressBar = view.findViewById(R.id.progressBar);
        emptyText = view.findViewById(R.id.emptyText);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new NotificationAdapter((report, position) -> {
            String reportId = safeGet(report, "report_id");
            String driverId = safeGet(report, "driver_id");

            // Open complaints fragment
            Fragment complaintsFragment = DriverComplaints.newInstance(driverId, reportId);
            ((AppCompatActivity) requireContext()).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, complaintsFragment)
                    .addToBackStack(null)
                    .commit();

            // Mark locally and remotely
            markAsViewed(reportId, position);
        });
        recyclerView.setAdapter(adapter);
        applyRecyclerAnimation();

        fetchNotifications();
        listenForRealtimeUpdates();

        return view;
    }

    private void applyRecyclerAnimation() {
        LayoutAnimationController animation =
                AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_fall_down);
        recyclerView.setLayoutAnimation(animation);
    }

    // ---------- FETCH ----------
    private void fetchNotifications() {
        progressBar.setVisibility(View.VISIBLE);
        SupabaseService service = SupabaseApiClient.getRetrofitInstance().create(SupabaseService.class);

        service.getReports(
                SupabaseApiClient.SUPABASE_API_KEY,
                "Bearer " + SupabaseApiClient.SUPABASE_API_KEY
        ).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    localReports.clear();
                    localReports.addAll(response.body());

                    // sort newest first
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                    localReports.sort((a, b) -> {
                        try {
                            Date da = inputFormat.parse(safeGet(a, "timestamp").replace("Z", ""));
                            Date db = inputFormat.parse(safeGet(b, "timestamp").replace("Z", ""));
                            return db != null && da != null ? db.compareTo(da) : 0;
                        } catch (Exception e) {
                            return 0;
                        }
                    });

                    adapter.setData(new ArrayList<>(localReports));
                    recyclerView.scheduleLayoutAnimation();
                    toggleEmpty(localReports.isEmpty(), "No notifications yet.");
                } else {
                    toggleEmpty(true, "Failed to load notifications.");
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                toggleEmpty(true, "Connection error. Please try again.");
            }
        });
    }

    private void toggleEmpty(boolean show, String msg) {
        emptyText.setVisibility(show ? View.VISIBLE : View.GONE);
        emptyText.setText(show ? msg : "");
    }

    // ---------- MARK VIEWED ----------
    private void markAsViewed(String reportId, int position) {
        if (reportId == null || reportId.isEmpty()) return;

        // prevent realtime refresh overriding our update
        suppressRealtimeFetch = true;

        adapter.markAsViewed(position);
        updateLocalViewed(reportId);

        SupabaseService service = SupabaseApiClient.getRetrofitInstance().create(SupabaseService.class);
        Map<String, Object> updates = new HashMap<>();
        updates.put("viewed", true);

        service.updateReportViewed(
                SupabaseApiClient.SUPABASE_API_KEY,
                "Bearer " + SupabaseApiClient.SUPABASE_API_KEY,
                "eq." + reportId,
                updates
        ).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call,
                                   Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "✅ Marked viewed: " + reportId);
                    DriverHomeActivity.refreshBadgeFromAnywhere();

                    // delay realtime re-fetch to avoid reverting
                    recyclerView.postDelayed(() -> suppressRealtimeFetch = false, 1200);
                } else {
                    Log.e(TAG, "❌ Failed mark viewed " + response.code());
                    suppressRealtimeFetch = false;
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Log.e(TAG, "⚠ Network error", t);
                suppressRealtimeFetch = false;
            }
        });
    }

    private void updateLocalViewed(String reportId) {
        for (Map<String, Object> r : localReports) {
            if (reportId.equals(safeGet(r, "report_id"))) {
                r.put("viewed", true);
                break;
            }
        }
    }

    // ---------- REALTIME ----------
    private void listenForRealtimeUpdates() {
        realtimeListener = new SupabaseRealtimeListener();
        realtimeListener.startListening(reportId -> {
            if (suppressRealtimeFetch) {
                Log.d(TAG, "⏳ Suppressing realtime fetch temporarily...");
                return;
            }
            Log.d(TAG, "🔄 Realtime update detected: " + reportId);
            requireActivity().runOnUiThread(this::fetchNotifications);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (realtimeListener != null) realtimeListener.stopListening();

        View bottomNav = requireActivity().findViewById(R.id.driver_bottom_nav);
        if (bottomNav != null) bottomNav.setVisibility(View.VISIBLE);
    }

    private String safeGet(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }

    // ---------- ADAPTER ----------
    private static class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
        interface OnNotificationClickListener {
            void onNotificationClick(Map<String, Object> report, int position);
        }

        private final List<Map<String, Object>> notifications = new ArrayList<>();
        private final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        private final SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
        private final OnNotificationClickListener listener;

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
                Map<String, Object> item = notifications.get(position);
                item.put("viewed", true);
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

            holder.container.setBackgroundResource(
                    viewed ? R.drawable.bg_notification_viewed : R.drawable.bg_notification_unviewed
            );

            holder.itemView.setOnClickListener(v -> listener.onNotificationClick(report, holder.getAdapterPosition()));
        }

        private String safeGet(Map<String, Object> map, String key) {
            Object val = map.get(key);
            return val != null ? val.toString() : "";
        }

        private String formatDate(String iso) {
            if (iso == null) return "Unknown time";
            try {
                Date d = inputFormat.parse(iso.replace("Z", ""));
                return outputFormat.format(d);
            } catch (ParseException e) {
                return iso.replace("T", " ").replace("Z", "");
            }
        }

        @Override
        public int getItemCount() { return notifications.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            CardView card;
            View container;
            TextView message, time;

            ViewHolder(View itemView) {
                super(itemView);
                card = itemView.findViewById(R.id.notificationCard);
                container = itemView.findViewById(R.id.notificationContainer);
                message = itemView.findViewById(R.id.notificationMessage);
                time = itemView.findViewById(R.id.notificationTime);
            }
        }
    }
}

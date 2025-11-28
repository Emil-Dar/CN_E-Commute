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

    // Cache viewed reports locally (to persist UI state)
    private static final Set<String> viewedCache = new HashSet<>();

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
            String reportId = safeGet(report, "report_id").trim();
            String driverId = safeGet(report, "driver_id").trim();

            boolean alreadyViewed = report.get("viewed") instanceof Boolean && (Boolean) report.get("viewed");

            if (!alreadyViewed) {
                adapter.markAsViewed(position);
                updateLocalViewed(reportId);
                DriverHomeActivity.decrementBadgeCount();
                markAsViewed(reportId); // ✅ update remotely (async)
            }

            recyclerView.postDelayed(() -> {
                Fragment complaintsFragment = DriverComplaints.newInstance(driverId, reportId);
                ((AppCompatActivity) requireContext()).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, complaintsFragment)
                        .addToBackStack(null)
                        .commit();
            }, 150);
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

                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                    localReports.sort((a, b) -> {
                        try {
                            Date da = inputFormat.parse(safeGet(a, "created_at").replace("Z", ""));
                            Date db = inputFormat.parse(safeGet(b, "created_at").replace("Z", ""));
                            return db != null && da != null ? db.compareTo(da) : 0;
                        } catch (Exception e) {
                            return 0;
                        }
                    });

                    for (Map<String, Object> report : localReports) {
                        String rid = safeGet(report, "report_id");
                        if (viewedCache.contains(rid)) {
                            report.put("viewed", true);
                        }
                    }

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

    // ---------- MARK AS VIEWED ----------
    private void markAsViewed(String reportId) {
        if (reportId == null || reportId.isEmpty()) return;

        suppressRealtimeFetch = true;
        viewedCache.add(reportId);

        SupabaseService service = SupabaseApiClient.getRetrofitInstance().create(SupabaseService.class);
        Map<String, Object> updates = new HashMap<>();
        updates.put("viewed", true);

        service.updateReportViewed(
                SupabaseApiClient.SUPABASE_API_KEY,
                "Bearer " + SupabaseApiClient.SUPABASE_API_KEY,
                "eq." + reportId,
                updates
        ).enqueue(new Callback<List<Map<String, Object>>>() { // ✅ aligned with SupabaseService

            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Marked viewed remotely: " + reportId);
                } else {
                    Log.e(TAG, "Failed to mark viewed remotely, code: " + response.code());
                }
                recyclerView.postDelayed(() -> suppressRealtimeFetch = false, 1200);
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Log.e(TAG, "⚠ Network error", t);
                recyclerView.postDelayed(() -> suppressRealtimeFetch = false, 1200);
            }
        });
    }

    private void updateLocalViewed(String reportId) {
        if (reportId == null || reportId.isEmpty()) return;
        for (Map<String, Object> r : localReports) {
            if (reportId.equals(safeGet(r, "report_id"))) {
                r.put("viewed", true);
                break;
            }
        }
        viewedCache.add(reportId);
    }

    // ---------- REALTIME ----------
    private void listenForRealtimeUpdates() {
        realtimeListener = new SupabaseRealtimeListener();
        realtimeListener.startListening(reportId -> {
            if (suppressRealtimeFetch || viewedCache.contains(reportId)) {
                Log.d(TAG, "Ignoring realtime update for viewed report: " + reportId);
                return;
            }
            Log.d(TAG, "Realtime update detected: " + reportId);
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
                boolean alreadyViewed = item.get("viewed") instanceof Boolean && (Boolean) item.get("viewed");
                if (!alreadyViewed) {
                    item.put("viewed", true);
                    notifyItemChanged(position, "viewed_update");
                }
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_driver_notification, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            onBindViewHolder(holder, position, Collections.emptyList());
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull List<Object> payloads) {
            Map<String, Object> report = notifications.get(position);
            boolean viewed = report.get("viewed") instanceof Boolean && Boolean.TRUE.equals(report.get("viewed"));

            if (payloads.contains("viewed_update")) {
                holder.container.animate().alpha(0f).setDuration(120).withEndAction(() -> {
                    holder.container.setBackgroundResource(R.drawable.bg_notification_viewed);
                    holder.container.animate().alpha(1f).setDuration(120).start();
                }).start();
                return;
            }

            holder.message.setText("A commuter filed a report on you.");
            holder.time.setText(formatDate(safeGet(report, "created_at")));

            holder.container.setBackgroundResource(
                    viewed ? R.drawable.bg_notification_viewed : R.drawable.bg_notification_unviewed
            );

            holder.itemView.setOnClickListener(v ->
                    listener.onNotificationClick(report, holder.getAdapterPosition()));
        }

        private String safeGet(Map<String, Object> map, String key) {
            Object val = map.get(key);
            return val != null ? val.toString() : "";
        }

        private String formatDate(String iso) {
            if (iso == null || iso.isEmpty()) return "Unknown time";
            try {
                Date d = inputFormat.parse(iso.replace("Z", ""));
                return outputFormat.format(d);
            } catch (ParseException e) {
                return iso.replace("T", " ").replace("Z", "");
            }
        }

        @Override
        public int getItemCount() {
            return notifications.size();
        }

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

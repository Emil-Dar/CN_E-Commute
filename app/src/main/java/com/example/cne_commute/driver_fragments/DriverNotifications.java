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

    // Cache which report IDs have been viewed
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
            String reportId = getSafeString(report, "report_id");
            String driverId = getSafeString(report, "driver_id");

            boolean viewed = Boolean.TRUE.equals(report.get("viewed"));

            if (!viewed) {
                adapter.markAsViewed(position);
                updateLocalViewed(reportId);
                DriverHomeActivity.decrementBadgeCount();
                markAsViewed(reportId);
            }

            recyclerView.postDelayed(() -> {
                Fragment fragment = DriverComplaints.newInstance(driverId, reportId);
                ((AppCompatActivity) requireContext())
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }, 150);
        });

        recyclerView.setAdapter(adapter);
        applyRecyclerAnimation();

        fetchNotifications();
        setupRealtime();

        return view;
    }

    private void applyRecyclerAnimation() {
        LayoutAnimationController anim =
                AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_fall_down);
        recyclerView.setLayoutAnimation(anim);
    }

    // ============================================================
    // FETCH NOTIFICATIONS
    // ============================================================
    private void fetchNotifications() {
        progressBar.setVisibility(View.VISIBLE);

        String driverId = DriverHomeActivity.getLoggedInDriverId();
        if (driverId == null || driverId.trim().isEmpty()) {
            toggleEmpty(true, "Driver ID missing.");
            progressBar.setVisibility(View.GONE);
            return;
        }

        Log.e("DRIVER_ID_DEBUG", "Driver ID sent to Supabase = [" + driverId + "]");


        SupabaseService service = SupabaseApiClient.getRetrofitInstance().create(SupabaseService.class);

        service.getReportsByDriverId(
                        SupabaseApiClient.SUPABASE_API_KEY,
                        "Bearer " + SupabaseApiClient.SUPABASE_API_KEY,
                        "eq." + driverId,
                        "created_at.desc",
                        "*"   // select all columns
                )
                .enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                progressBar.setVisibility(View.GONE);

                if (!response.isSuccessful() || response.body() == null) {
                    toggleEmpty(true, "Failed to load notifications.");
                    return;
                }

                localReports.clear();
                localReports.addAll(response.body());

                sortReportsByDate();
                applyViewedCache();

                adapter.setData(new ArrayList<>(localReports));
                recyclerView.scheduleLayoutAnimation();
                toggleEmpty(localReports.isEmpty(), "No notifications yet.");
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

    // Sort latest-first
    private void sortReportsByDate() {
        SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        localReports.sort((a, b) -> {
            try {
                Date da = input.parse(getSafeString(a, "created_at").replace("Z", ""));
                Date db = input.parse(getSafeString(b, "created_at").replace("Z", ""));
                return db != null && da != null ? db.compareTo(da) : 0;
            } catch (Exception e) {
                return 0;
            }
        });
    }

    private void applyViewedCache() {
        for (Map<String, Object> r : localReports) {
            String id = getSafeString(r, "report_id");
            if (viewedCache.contains(id)) {
                r.put("viewed", true);
            }
        }
    }

    // ============================================================
    // MARK REPORT AS VIEWED
    // ============================================================
    private void markAsViewed(String reportId) {
        if (reportId == null || reportId.isEmpty()) return;

        viewedCache.add(reportId);
        suppressRealtimeFetch = true;

        SupabaseService service = SupabaseApiClient.getRetrofitInstance().create(SupabaseService.class);

        Map<String, Object> payload = new HashMap<>();
        payload.put("viewed", true);

        service.updateReportViewed(
                SupabaseApiClient.SUPABASE_API_KEY,
                "Bearer " + SupabaseApiClient.SUPABASE_API_KEY,
                "eq." + reportId,
                payload
        ).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                Log.d(TAG, response.isSuccessful()
                        ? "Viewed updated remotely: " + reportId
                        : "Failed to update viewed");
                resetRealtimeSuppression();
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Log.e(TAG, "Network error updating viewed", t);
                resetRealtimeSuppression();
            }
        });
    }

    private void resetRealtimeSuppression() {
        recyclerView.postDelayed(() -> suppressRealtimeFetch = false, 1200);
    }

    private void updateLocalViewed(String id) {
        for (Map<String, Object> r : localReports) {
            if (id.equals(getSafeString(r, "report_id"))) {
                r.put("viewed", true);
                break;
            }
        }
        viewedCache.add(id);
    }

    // ============================================================
    // REALTIME LISTENING
    // ============================================================
    private void setupRealtime() {
        realtimeListener = new SupabaseRealtimeListener();
        realtimeListener.startListening(reportId -> {

            if (suppressRealtimeFetch || viewedCache.contains(reportId)) {
                Log.d(TAG, "Ignoring realtime update for viewed report: " + reportId);
                return;
            }

            requireActivity().runOnUiThread(this::fetchNotifications);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (realtimeListener != null) realtimeListener.stopListening();
    }

    // Utility
    private String getSafeString(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v != null ? v.toString() : "";
    }

    // ============================================================
    // ADAPTER
    // ============================================================
    private static class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

        interface OnNotificationClickListener {
            void onNotificationClick(Map<String, Object> report, int position);
        }

        private final List<Map<String, Object>> items = new ArrayList<>();
        private final OnNotificationClickListener listener;

        private final SimpleDateFormat inFmt =
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        private final SimpleDateFormat outFmt =
                new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());

        NotificationAdapter(OnNotificationClickListener listener) {
            this.listener = listener;
        }

        void setData(List<Map<String, Object>> data) {
            items.clear();
            items.addAll(data);
            notifyDataSetChanged();
        }

        void markAsViewed(int pos) {
            if (pos < 0 || pos >= items.size()) return;
            items.get(pos).put("viewed", true);
            notifyItemChanged(pos, "update_viewed");
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int v) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_driver_notification, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
            onBindViewHolder(h, pos, Collections.emptyList());
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder h, int pos,
                                     @NonNull List<Object> payloads) {

            Map<String, Object> report = items.get(pos);
            boolean viewed = Boolean.TRUE.equals(report.get("viewed"));

            if (payloads.contains("update_viewed")) {
                h.container.animate().alpha(0f).setDuration(120).withEndAction(() -> {
                    h.container.setBackgroundResource(R.drawable.bg_notification_viewed);
                    h.container.animate().alpha(1f).setDuration(120).start();
                }).start();
                return;
            }

            h.message.setText("A commuter filed a report on you.");
            h.time.setText(formatDate(getSafe(report, "created_at")));

            int bg = viewed
                    ? R.drawable.bg_notification_viewed
                    : R.drawable.bg_notification_unviewed;

            h.container.setBackgroundResource(bg);

            h.itemView.setOnClickListener(v ->
                    listener.onNotificationClick(report, h.getAdapterPosition()));
        }

        private String getSafe(Map<String, Object> map, String key) {
            Object v = map.get(key);
            return v != null ? v.toString() : "";
        }

        private String formatDate(String iso) {
            try {
                Date d = inFmt.parse(iso.replace("Z", ""));
                return outFmt.format(d);
            } catch (Exception e) {
                return iso;
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            CardView card;
            View container;
            TextView message, time;

            ViewHolder(View v) {
                super(v);
                card = v.findViewById(R.id.notificationCard);
                container = v.findViewById(R.id.notificationContainer);
                message = v.findViewById(R.id.notificationMessage);
                time = v.findViewById(R.id.notificationTime);
            }
        }
    }
}

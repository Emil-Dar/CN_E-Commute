package com.example.cne_commute;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.cne_commute.driver_fragments.DriverAccount;
import com.example.cne_commute.driver_fragments.DriverComplaints;
import com.example.cne_commute.driver_fragments.DriverHome;
import com.example.cne_commute.driver_fragments.DriverNotifications;
import com.example.cne_commute.driver_fragments.DriverRevenue;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverHomeActivity extends AppCompatActivity {

    private static DriverHomeActivity instance;
    private TextView badge;
    private BottomNavigationView bottomNav;
    private String driverId;
    private SupabaseRealtimeListener realtimeListener;

    // 🟢 Global unviewed cache
    public static int cachedUnviewedCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home);

        instance = this;
        driverId = getIntent().getStringExtra("driverID");
        if (driverId == null || driverId.isEmpty()) driverId = "0";

        bottomNav = findViewById(R.id.driver_bottom_nav);
        badge = findViewById(R.id.notification_badge);

        setupBottomNav();

        ImageView btnNotifications = findViewById(R.id.btn_notifications);
        btnNotifications.setOnClickListener(v -> {
            bottomNav.setVisibility(View.GONE);
            loadFragment(new DriverNotifications());
        });

        if (savedInstanceState == null) loadFragment(new DriverHome());

        startRealtimeBadgeListener();
        updateNotificationBadge();
    }

    // 🔵 Called from DriverNotifications after a report is viewed
    public static void decrementBadgeCount() {
        if (instance != null) {
            instance.runOnUiThread(() -> {
                if (cachedUnviewedCount > 0) cachedUnviewedCount--;
                instance.showBadge(cachedUnviewedCount);
            });
        }
    }

    public static void refreshBadgeFromAnywhere() {
        if (instance != null) {
            instance.runOnUiThread(instance::updateNotificationBadge);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNotificationBadge();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realtimeListener != null) realtimeListener.stopListening();
    }

    private void updateNotificationBadge() {
        SupabaseService service = SupabaseApiClient.getRetrofitInstance().create(SupabaseService.class);

        service.getReports(
                SupabaseApiClient.SUPABASE_API_KEY,
                "Bearer " + SupabaseApiClient.SUPABASE_API_KEY
        ).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call,
                                   Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int unviewedCount = 0;
                    for (Map<String, Object> r : response.body()) {
                        Object viewed = r.get("viewed");
                        if (!(viewed instanceof Boolean && (Boolean) viewed)) unviewedCount++;
                    }
                    cachedUnviewedCount = unviewedCount;
                    showBadge(unviewedCount);
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                showBadge(cachedUnviewedCount);
            }
        });
    }

    private void startRealtimeBadgeListener() {
        realtimeListener = new SupabaseRealtimeListener();
        realtimeListener.startListening(reportId -> runOnUiThread(this::updateNotificationBadge));
    }

    private void setupBottomNav() {
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            switch (item.getItemId()) {
                case R.id.driver_home:
                    selected = new DriverHome();
                    break;
                case R.id.driver_revenue:
                    selected = new DriverRevenue();
                    break;
                case R.id.driver_complaints:
                    selected = DriverComplaints.newInstance(driverId, null);
                    break;
                case R.id.driver_account:
                    selected = new DriverAccount();
                    break;
            }
            if (selected != null) {
                loadFragment(selected);
                bottomNav.setVisibility(View.VISIBLE);
            }
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void showBadge(int count) {
        if (badge == null) return;
        if (count > 0) {
            badge.setText(String.valueOf(count));
            badge.setVisibility(View.VISIBLE);
        } else {
            badge.setVisibility(View.GONE);
        }
    }
}

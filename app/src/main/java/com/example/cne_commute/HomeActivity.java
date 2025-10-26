package com.example.cne_commute;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private static final String CHANNEL_ID = "report_channel";
    private FloatingActionButton fabQRCode;
    private ImageButton btnNotifications;
    private TextView notificationCount;
    private boolean notificationsViewed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate triggered — setting up UI");
        setContentView(R.layout.activity_home);

        fabQRCode = findViewById(R.id.fab_qr_code);
        fabQRCode.bringToFront();
        fabQRCode.setOnClickListener(v -> {
            Log.d(TAG, "FAB QR clicked — launching QRScannerActivity");
            startActivity(new Intent(HomeActivity.this, QRScannerActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        btnNotifications = findViewById(R.id.btn_notifications);
        notificationCount = findViewById(R.id.notification_count);

        btnNotifications.setOnClickListener(v -> {
            Log.d(TAG, "Notification icon clicked — launching NotificationActivity");
            notificationsViewed = true;
            startActivity(new Intent(HomeActivity.this, NotificationActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Log.d(TAG, "Bottom nav item selected: " + itemId);
            switch (itemId) {
                case R.id.nav_home:
                    return true;
                case R.id.nav_calculator:
                    startActivity(new Intent(this, FareCalculatorActivity.class));
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    return true;
                case R.id.nav_history:
                    startActivity(new Intent(this, HistoryActivity.class));
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    return true;
                case R.id.nav_account:
                    startActivity(new Intent(this, AccountActivity.class));
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    return true;
                default:
                    return false;
            }
        });

        setupCardActions(); // Cards are now passive only
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume triggered");

        if (notificationsViewed) {
            Log.d(TAG, "Notifications were viewed — hiding badge");
            hideNotificationBadge();
            notificationsViewed = false;
        } else {
            Log.d(TAG, "Notifications not yet viewed — checking for accepted reports");
            checkAcceptedReports();
        }
    }

    private void checkAcceptedReports() {
        Log.d(TAG, "checkAcceptedReports() started");

        String userId = getCurrentUserId();
        Log.d(TAG, "Using userId: " + userId);

        ReportApiService apiService = ApiClient.getClient().create(ReportApiService.class);
        Call<List<ReportData>> call = apiService.getAcceptedReports("eq." + userId, null);

        call.enqueue(new Callback<List<ReportData>>() {
            @Override
            public void onResponse(Call<List<ReportData>> call, Response<List<ReportData>> response) {
                Log.d(TAG, "API response received");
                Log.d(TAG, "Response successful: " + response.isSuccessful());

                if (response.body() != null) {
                    Log.d(TAG, "Response body not null");
                    List<ReportData> acceptedReports = response.body();
                    Log.d(TAG, "Accepted reports count: " + acceptedReports.size());

                    for (ReportData report : acceptedReports) {
                        Log.d(TAG, "Report ID: " + report.getReportId() + ", Status: " + report.getStatus());
                        Log.d(TAG, "Report object: " + report.toString());
                    }

                    if (!acceptedReports.isEmpty()) {
                        showNotificationBadge(acceptedReports.size());
                        showNotification("Report Accepted", "You have " + acceptedReports.size() + " accepted report(s).");
                    } else {
                        hideNotificationBadge();
                    }
                } else {
                    Log.w(TAG, "Response body is null");
                }
            }

            @Override
            public void onFailure(Call<List<ReportData>> call, Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage());
            }
        });
    }

    private void showNotificationBadge(int count) {
        Log.d(TAG, "Showing badge with count: " + count);
        if (btnNotifications != null && notificationCount != null) {
            btnNotifications.setColorFilter(ContextCompat.getColor(this, R.color.alert_red));
            notificationCount.setText(String.valueOf(count));
            notificationCount.setVisibility(TextView.VISIBLE);
        }
    }

    private void hideNotificationBadge() {
        Log.d(TAG, "Hiding notification badge");
        if (notificationCount != null) {
            notificationCount.setVisibility(TextView.GONE);
        }
    }

    private void showNotification(String title, String message) {
        Log.d(TAG, "showNotification() called with title: " + title);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "Notification permission not granted — requesting");
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
                return;
            } else {
                Log.d(TAG, "Notification permission already granted");
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "Creating notification channel");
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Report Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        Log.d(TAG, "Building and showing notification");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notify)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify((int) System.currentTimeMillis(), builder.build());
    }

    private String getCurrentUserId() {
        Log.d(TAG, "getCurrentUserId() called — returning static ID");
        return "user123"; // Replace with actual user ID logic
    }

    private void setupCardActions() {
        Log.d(TAG, "Card actions disabled — cards are non-clickable");
        // Cards are now passive visual elements only
    }

    private void showToast(String message) {
        Log.d(TAG, "Showing toast: " + message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

package com.example.cne_commute;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeActivity extends AppCompatActivity {

    private FloatingActionButton fabQRCode;
    private ImageButton btnNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Setup Floating Action Button
        fabQRCode = findViewById(R.id.fab_qr_code);
        fabQRCode.bringToFront(); // Ensure it's clickable on top of other views
        fabQRCode.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, QRScannerActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        // Setup Notification Button
        btnNotifications = findViewById(R.id.btn_notifications);
        btnNotifications.setOnClickListener(v -> {
            // Change this to your actual notification screen
            Intent intent = new Intent(HomeActivity.this, NotificationActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        // Setup Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
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

        setupCardActions();
    }

    private void setupCardActions() {
        findViewById(R.id.card_scan_qr).setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, QRScannerActivity.class));
            overridePendingTransition(R.anim.fade_in, 0);
        });

        findViewById(R.id.card_fare_calculator).setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, FareCalculatorActivity.class));
            overridePendingTransition(R.anim.fade_in, 0);
        });

        findViewById(R.id.card_report_issues).setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ReportActivity.class));
            overridePendingTransition(R.anim.fade_in, 0);
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

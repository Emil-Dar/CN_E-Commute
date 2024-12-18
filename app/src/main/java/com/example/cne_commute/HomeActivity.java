package com.example.cne_commute;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize the "Report" button
        Button reportButton = findViewById(R.id.report_button);
        reportButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ReportActivity.class);
            startActivity(intent);
        });

        // Initialize Floating Action Button (FAB) for QR Code Scanner
        FloatingActionButton fabQRCode = findViewById(R.id.fab_qr_code);
        fabQRCode.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, QRScannerActivity.class);
            startActivity(intent);
        });

        // Initialize Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Navigate to HomeActivity
                startActivity(new Intent(HomeActivity.this, HomeActivity.class));
                return true;
            } else if (itemId == R.id.nav_calculator) {
                // Navigate to FareCalculatorActivity
                startActivity(new Intent(HomeActivity.this, FareCalculatorActivity.class));
                return true;
            } else if (itemId == R.id.nav_history) {
                // Navigate to HistoryActivity
                startActivity(new Intent(HomeActivity.this, HistoryActivity.class));
                return true;
            } else if (itemId == R.id.nav_account) {
                // Navigate to AccountActivity
                startActivity(new Intent(HomeActivity.this, AccountActivity.class));
                return true;
            } else {
                return false;
            }
        });

        // Show toast for card actions
        setupCardActions();
    }

    // Helper method for CardView click listeners
    private void setupCardActions() {
        // "Scan QR Code" Card
        findViewById(R.id.card_scan_qr).setOnClickListener(v ->
                showToast("Scan QR Code Clicked"));

        // "Fare Calculator" Card
        findViewById(R.id.card_fare_calculator).setOnClickListener(v -> {
            // Navigate to FareCalculatorActivity
            Intent intent = new Intent(HomeActivity.this, FareCalculatorActivity.class);
            startActivity(intent);
        });

        // "Report Issues" Card
        findViewById(R.id.card_report_issues).setOnClickListener(v ->
                showToast("Report Issues Clicked"));
    }

    // Helper method to display toast messages
    private void showToast(String message) {
        Toast.makeText(HomeActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}

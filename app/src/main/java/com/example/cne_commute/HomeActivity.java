package com.example.cne_commute;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Floating Action Button for QR Code Scanner
        FloatingActionButton fabQRCode = findViewById(R.id.fab_qr_code);
        fabQRCode.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, QRScannerActivity.class));
        });

        // Setup Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home); // Mark home as selected

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Do nothing or refresh if needed
                return true;
            } else if (itemId == R.id.nav_calculator) {
                startActivity(new Intent(HomeActivity.this, FareCalculatorActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_history) {
                startActivity(new Intent(HomeActivity.this, HistoryActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_account) {
                startActivity(new Intent(HomeActivity.this, AccountActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });



        // Card click actions
        setupCardActions();
    }



    private void setupCardActions() {
        findViewById(R.id.card_scan_qr).setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, QRScannerActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.card_fare_calculator).setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, FareCalculatorActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.card_report_issues).setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ReportActivity.class);
            startActivity(intent);
        });
    }

    private void showToast(String message) {
        Toast.makeText(HomeActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}

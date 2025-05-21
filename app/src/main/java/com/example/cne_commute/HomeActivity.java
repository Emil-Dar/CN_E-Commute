package com.example.cne_commute;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;
import androidx.cardview.widget.CardView;

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
                startActivity(new Intent(HomeActivity.this, HomeActivity.class));
                return true;
            } else if (itemId == R.id.nav_calculator) {
                startActivity(new Intent(HomeActivity.this, FareCalculatorActivity.class));
                return true;
            } else if (itemId == R.id.nav_history) {
                startActivity(new Intent(HomeActivity.this, HistoryActivity.class));
                return true;
            } else if (itemId == R.id.nav_account) {
                startActivity(new Intent(HomeActivity.this, AccountActivity.class));
                return true;
            } else {
                return false;
            }
        });

        // Apply animations to cards
        applyAnimations();

        // Set up card actions
        setupCardActions();
    }

    // Apply both fade-in and slide-in animations to CardViews
    private void applyAnimations() {
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);

        CardView cardScanQr = findViewById(R.id.card_scan_qr);
        CardView cardFareCalculator = findViewById(R.id.card_fare_calculator);
        CardView cardReportIssues = findViewById(R.id.card_report_issues);

        cardScanQr.startAnimation(slideIn);
        cardFareCalculator.startAnimation(slideIn);
        cardReportIssues.startAnimation(slideIn);

        // Optional: Also apply fade-in simultaneously
        cardScanQr.startAnimation(fadeIn);
        cardFareCalculator.startAnimation(fadeIn);
        cardReportIssues.startAnimation(fadeIn);
    }

    // Helper method for CardView click listeners
    private void setupCardActions() {
        findViewById(R.id.card_scan_qr).setOnClickListener(v ->
                showToast("Scan QR Code Clicked"));

        findViewById(R.id.card_fare_calculator).setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, FareCalculatorActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.card_report_issues).setOnClickListener(v ->
                showToast("Report Issues Clicked"));
    }

    // Helper method to display toast messages
    private void showToast(String message) {
        Toast.makeText(HomeActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}

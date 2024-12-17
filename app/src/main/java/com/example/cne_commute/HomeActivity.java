package com.example.cne_commute;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

        // Show toast for card actions
        setupCardActions();
    }

    // Helper method for CardView click listeners
    private void setupCardActions() {
        // "Scan QR Code" Card
        findViewById(R.id.card_container).setOnClickListener(v ->
                showToast("Scan QR Code Clicked"));

        // "Fare Calculator" Card
        findViewById(R.id.card_container).setOnClickListener(v ->
                showToast("Fare Calculator Clicked"));

        // "Report Issues" Card
        findViewById(R.id.card_container).setOnClickListener(v ->
                showToast("Report Issues Clicked"));
    }

    // Helper method to display toast messages
    private void showToast(String message) {
        Toast.makeText(HomeActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}

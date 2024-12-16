package com.example.cne_commute;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeActivity extends AppCompatActivity {

    private Button homeButton, mapButton, historyButton, accountButton, reportButton;
    private FloatingActionButton fabQRCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize buttons and floating action button
        homeButton = findViewById(R.id.home_button);
        mapButton = findViewById(R.id.map_button);
        historyButton = findViewById(R.id.history_button);
        accountButton = findViewById(R.id.account_button);
        reportButton = findViewById(R.id.report_button);
        fabQRCode = findViewById(R.id.fab_qr_code);

        // Button click listeners
        homeButton.setOnClickListener(v -> showToast("Home Button Clicked"));
        mapButton.setOnClickListener(v -> showToast("Fare Calculator Button Clicked"));
        historyButton.setOnClickListener(v -> showToast("History Button Clicked"));
        accountButton.setOnClickListener(v -> showToast("Account Button Clicked"));

        // Navigate to ReportActivity when the "Report" button is clicked
        reportButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ReportActivity.class);
            startActivity(intent);
        });

        // Navigate to QRScannerActivity when the QR Code button is clicked
        fabQRCode.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, QRScannerActivity.class);
            startActivity(intent);
        });


    }

    // Helper method to display a toast message
    private void showToast(String message) {
        Toast.makeText(HomeActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    // Helper method to open Google Maps with a query

}

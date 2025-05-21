package com.example.cne_commute;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;

public class DriverHomeActivity extends AppCompatActivity {

    private Button homeButton, mapButton, historyButton, accountButton;
    private FloatingActionButton fabQRCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home);

        // Initialize Buttons and FloatingActionButton
        homeButton = findViewById(R.id.home_button);
        mapButton = findViewById(R.id.map_button);
        historyButton = findViewById(R.id.history_button);
        accountButton = findViewById(R.id.account_button);
        fabQRCode = findViewById(R.id.fab_qr_code);

        // Set click listeners for each button
        homeButton.setOnClickListener(v -> navigateToActivity(DriverHomeActivity.class));
        mapButton.setOnClickListener(v -> navigateToActivity(FareCalculatorActivity.class));  // Corrected mapButton navigation
        historyButton.setOnClickListener(v -> navigateToActivity(DriverNotificationActivity.class));
        accountButton.setOnClickListener(v -> navigateToActivity(DriverAccountActivity.class));
        fabQRCode.setOnClickListener(v -> showToast("QR Code Button Clicked"));
    }

    // Helper function to navigate between activities
    private void navigateToActivity(Class<?> targetActivity) {
        Intent intent = new Intent(DriverHomeActivity.this, targetActivity);
        startActivity(intent);
    }

    // Helper function to show a toast message
    private void showToast(String message) {
        Toast.makeText(DriverHomeActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
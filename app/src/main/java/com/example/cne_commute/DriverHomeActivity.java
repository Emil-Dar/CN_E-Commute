package com.example.cne_commute;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DriverHomeActivity extends AppCompatActivity {

    private Button homeButton, mapButton, historyButton, accountButton;
    private FloatingActionButton fabQRCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home);

        homeButton = findViewById(R.id.home_button);
        mapButton = findViewById(R.id.map_button);
        historyButton = findViewById(R.id.history_button);
        accountButton = findViewById(R.id.account_button);
        fabQRCode = findViewById(R.id.fab_qr_code);

        homeButton.setOnClickListener(v -> navigateToActivity(DriverHomeActivity.class));
        mapButton.setOnClickListener(v -> navigateToActivity(WalletActivity.class));
        historyButton.setOnClickListener(v -> navigateToActivity(DriverNotificationActivity.class));
        accountButton.setOnClickListener(v -> navigateToActivity(DriverAccountActivity.class));
        fabQRCode.setOnClickListener(v -> showToast("Driver Map Button Clicked"));

    }

    private void navigateToActivity(Class<?> targetActivity) {
        Intent intent = new Intent(DriverHomeActivity.this, targetActivity);
        startActivity(intent);
    }

    private void showToast(String message) {
        Toast.makeText(DriverHomeActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}

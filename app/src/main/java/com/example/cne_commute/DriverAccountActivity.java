package com.example.cne_commute;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DriverAccountActivity extends AppCompatActivity {

    private Button homeButton, mapButton, historyButton, accountButton;
    private FloatingActionButton fabDriverMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_account);

        fabDriverMap = findViewById(R.id.fab_qr_code);
        fabDriverMap.setOnClickListener(v -> showToast("Driver Map Button Clicked"));

        homeButton = findViewById(R.id.home_button);
        mapButton = findViewById(R.id.map_button);
        historyButton = findViewById(R.id.history_button);
        accountButton = findViewById(R.id.account_button);

        homeButton.setOnClickListener(v -> navigateToActivity(DriverHomeActivity.class));
        mapButton.setOnClickListener(v -> navigateToActivity(WalletActivity.class));
        historyButton.setOnClickListener(v -> navigateToActivity(DriverNotificationActivity.class));
        accountButton.setOnClickListener(v -> navigateToActivity(DriverAccountActivity.class));
    }

    private void navigateToActivity(Class<?> targetActivity) {
        Intent intent = new Intent(DriverAccountActivity.this, targetActivity);
        startActivity(intent);
    }

    private void showToast(String message) {
        Toast.makeText(DriverAccountActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}

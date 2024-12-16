package com.example.cne_commute;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class WalletActivity extends AppCompatActivity {

    private Button homeButton, mapButton, historyButton, accountButton;
    private FloatingActionButton fabDriverMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        fabDriverMap = findViewById(R.id.fab_qr_code);
        fabDriverMap.setOnClickListener(v -> navigateTo(MapActivity.class));

        homeButton = findViewById(R.id.home_button);
        mapButton = findViewById(R.id.map_button);
        historyButton = findViewById(R.id.history_button);
        accountButton = findViewById(R.id.account_button);

        homeButton.setOnClickListener(v -> navigateTo(DriverHomeActivity.class));
        mapButton.setOnClickListener(v -> navigateTo(WalletActivity.class));
        historyButton.setOnClickListener(v -> navigateTo(DriverNotificationActivity.class));
        accountButton.setOnClickListener(v -> navigateTo(DriverAccountActivity.class));
    }

    private void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(WalletActivity.this, targetActivity);
        startActivity(intent);
    }

    private void showToast(String message) {
        Toast.makeText(WalletActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}

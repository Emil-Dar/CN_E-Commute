package com.example.cne_commute;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeActivity extends AppCompatActivity {

    private Button homeButton, mapButton, historyButton, accountButton;
    private FloatingActionButton fabQRCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        homeButton = findViewById(R.id.home_button);
        mapButton = findViewById(R.id.map_button);
        historyButton = findViewById(R.id.history_button);
        accountButton = findViewById(R.id.account_button);
        fabQRCode = findViewById(R.id.fab_qr_code);

        homeButton.setOnClickListener(v -> showToast("Home Button Clicked"));
        mapButton.setOnClickListener(v -> showToast("Map Button Clicked"));
        historyButton.setOnClickListener(v -> showToast("History Button Clicked"));
        accountButton.setOnClickListener(v -> showToast("Account Button Clicked"));

        fabQRCode.setOnClickListener(v -> showToast("QR Code Button Clicked"));
    }

    private void showToast(String message) {
        Toast.makeText(HomeActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}

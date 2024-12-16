package com.example.cne_commute;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Navigate to ReportActivity when the "Report" button is clicked
        findViewById(R.id.report_button).setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ReportActivity.class);
            startActivity(intent);
        });

        // Navigate to QRScannerActivity when the QR Code button is clicked
        FloatingActionButton fabQRCode = findViewById(R.id.fab_qr_code);
        fabQRCode.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, QRScannerActivity.class);
            startActivity(intent);
        });

        // Set up BottomNavigationView item selection listener
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                showToast("Home selected");
                return true;
            } else if (id == R.id.nav_map) {
                showToast("Map selected");
                return true;
            } else if (id == R.id.nav_history) {
                showToast("History selected");
                return true;
            } else if (id == R.id.nav_account) {
                showToast("Account selected");
                return true;
            }
            return false;
        });
    }

    // Helper method to display a toast message
    private void showToast(String message) {
        Toast.makeText(HomeActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}

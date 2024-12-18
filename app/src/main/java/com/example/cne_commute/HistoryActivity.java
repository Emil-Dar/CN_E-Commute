package com.example.cne_commute;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HistoryActivity extends AppCompatActivity {

    private Button reportHistoryButton, scannedQrCodesButton;
    private FrameLayout contentFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Initialize views
        reportHistoryButton = findViewById(R.id.report_history_button);
        scannedQrCodesButton = findViewById(R.id.scanned_qr_codes_button);
        contentFrame = findViewById(R.id.content_frame);

        // Set button click listeners
        reportHistoryButton.setOnClickListener(v -> showFragment(new ReportHistoryFragment()));
        scannedQrCodesButton.setOnClickListener(v -> showFragment(new ScannedQrCodesFragment()));

        // Show default fragment (e.g., ReportHistoryFragment)
        showFragment(new ReportHistoryFragment());

        // Initialize Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                // Navigate to HomeActivity
                startActivity(new Intent(HistoryActivity.this, HomeActivity.class));
                return true;
            } else if (itemId == R.id.nav_calculator) {
                // Navigate to FareCalculatorActivity
                startActivity(new Intent(HistoryActivity.this, FareCalculatorActivity.class));
                return true;
            } else if (itemId == R.id.nav_history) {
                // Navigate to HistoryActivity
                startActivity(new Intent(HistoryActivity.this, HistoryActivity.class));
                return true;
            } else if (itemId == R.id.nav_account) {
                // Navigate to AccountActivity
                startActivity(new Intent(HistoryActivity.this, AccountActivity.class));
                return true;
            } else {
                return false;
            }
        });
    }

    private void showFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, fragment);
        transaction.commit();
    }
}

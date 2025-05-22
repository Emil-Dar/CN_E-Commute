package com.example.cne_commute;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
    private SharedPreferences sharedPreferences, reportPreferences;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        initViews();
        setupListeners();
        setupBottomNavigation();

        // Load default fragment
        showReportHistoryFragment();
    }

    private void initViews() {
        reportHistoryButton = findViewById(R.id.report_history_button);
        scannedQrCodesButton = findViewById(R.id.scanned_qr_codes_button);
        contentFrame = findViewById(R.id.content_frame);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        sharedPreferences = getSharedPreferences("ScannedQrCodes", Context.MODE_PRIVATE);
        reportPreferences = getSharedPreferences("ReportData", Context.MODE_PRIVATE);
    }

    private void setupListeners() {
        reportHistoryButton.setOnClickListener(v -> {
            Log.d("HistoryActivity", "Report History Button Clicked");
            showReportHistoryFragment();
        });

        scannedQrCodesButton.setOnClickListener(v -> {
            Log.d("HistoryActivity", "Scanned QR Codes Button Clicked");
            showScannedQrCodesFragment();
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_history); // Highlight current tab

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                navigateTo(HomeActivity.class);
                return true;
            } else if (itemId == R.id.nav_calculator) {
                navigateTo(FareCalculatorActivity.class);
                return true;
            } else if (itemId == R.id.nav_history) {
                return true; // Already here
            } else if (itemId == R.id.nav_account) {
                navigateTo(AccountActivity.class);
                return true;
            }
            return false;
        });
    }

    private void navigateTo(@NonNull Class<?> targetActivity) {
        Intent intent = new Intent(HistoryActivity.this, targetActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    private void showFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, fragment);
        transaction.commit();
    }

    private void showScannedQrCodesFragment() {
        String scannedData = sharedPreferences != null
                ? sharedPreferences.getString("scannedData", null)
                : null;

        ScannedQrCodesFragment fragment = new ScannedQrCodesFragment();
        if (scannedData != null && !scannedData.trim().isEmpty()) {
            Bundle bundle = new Bundle();
            bundle.putString("scannedData", scannedData);
            fragment.setArguments(bundle);
        }

        showFragment(fragment);
    }

    private void showReportHistoryFragment() {
        String description = reportPreferences.getString("description", "");
        String violation = reportPreferences.getString("violation", "");
        String imagePath = reportPreferences.getString("imagePath", "");

        ReportHistoryFragment fragment = new ReportHistoryFragment();
        Bundle bundle = new Bundle();
        bundle.putString("description", description);
        bundle.putString("violation", violation);
        bundle.putString("imagePath", imagePath);
        fragment.setArguments(bundle);

        showFragment(fragment);
    }
}

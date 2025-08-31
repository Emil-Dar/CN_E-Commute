package com.example.cne_commute;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;



public class HistoryActivity extends AppCompatActivity {

    private Button reportHistoryButton, scannedQrCodesButton;
    private FrameLayout contentFrame;
    private SharedPreferences sharedPreferences, reportPreferences;
    private BottomNavigationView bottomNavigationView;
    private boolean isFragmentShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        initViews();
        setupListeners();
        setupBottomNavigation();

        // Set up the default ActionBar without the back arrow
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setTitle("History");
        }
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

            String reportListJson = reportPreferences.getString("reportList", "[]");
            Intent intent = new Intent(HistoryActivity.this, ReportHistoryActivity.class);
            intent.putExtra("reportList", reportListJson);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, 0);
        });

        scannedQrCodesButton.setOnClickListener(v -> {
            Log.d("HistoryActivity", "Scanned QR Codes Button Clicked");

            ScannedQrCodesFragment fragment = new ScannedQrCodesFragment();
            String scannedData = sharedPreferences.getString("scannedData", "");
            if (!scannedData.trim().isEmpty()) {
                Bundle bundle = new Bundle();
                bundle.putString("scannedData", scannedData);
                fragment.setArguments(bundle);
            }

            // Show back arrow when fragment is loaded
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setTitle("Scanned QR Codes");
            }

            isFragmentShown = true;

            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, 0)
                    .replace(R.id.content_frame, fragment)
                    .addToBackStack(null)  // Allows back arrow to pop fragment
                    .commit();
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_history);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                navigateTo(HomeActivity.class);
                return true;
            } else if (itemId == R.id.nav_calculator) {
                navigateTo(FareCalculatorActivity.class);
                return true;
            } else if (itemId == R.id.nav_history) {
                return true;
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
        overridePendingTransition(R.anim.fade_in, 0);
    }

    @Override
    public void onBackPressed() {
        if (isFragmentShown) {
            getSupportFragmentManager().popBackStack();
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(false);
                actionBar.setTitle("History");
            }
            isFragmentShown = false;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home && isFragmentShown) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

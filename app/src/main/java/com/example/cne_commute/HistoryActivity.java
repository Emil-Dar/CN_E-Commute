package com.example.cne_commute;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HistoryActivity extends AppCompatActivity {

    private Button reportHistoryButton, scannedQrCodesButton;
    private FrameLayout contentFrame;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        reportHistoryButton = findViewById(R.id.report_history_button);
        scannedQrCodesButton = findViewById(R.id.scanned_qr_codes_button);
        contentFrame = findViewById(R.id.content_frame);
        sharedPreferences = getSharedPreferences("ScannedQrCodes", Context.MODE_PRIVATE);

        reportHistoryButton.setOnClickListener(v -> {
            Log.d("HistoryActivity", "Report History Button Clicked");
            showFragment(new ReportHistoryFragment());
        });

        scannedQrCodesButton.setOnClickListener(v -> {
            Log.d("HistoryActivity", "Scanned QR Codes Button Clicked");
            showFragment(new ScannedQrCodesFragment());
        });

        // Show the initial fragment
        showFragment(new ReportHistoryFragment());

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            switch (itemId) {
                case R.id.nav_home:
                    startActivity(new Intent(HistoryActivity.this, HomeActivity.class));
                    return true;
                case R.id.nav_calculator:
                    startActivity(new Intent(HistoryActivity.this, FareCalculatorActivity.class));
                    return true;
                case R.id.nav_history:
                    return true;
                case R.id.nav_account:
                    startActivity(new Intent(HistoryActivity.this, AccountActivity.class));
                    return true;
                default:
                    return false;
            }
        });
    }

    private void showFragment(Fragment fragment) {
        // Pass scanned data to fragment if available
        String scannedData = sharedPreferences.getString("scannedData", null);
        if (scannedData != null && fragment instanceof ScannedQrCodesFragment) {
            Bundle bundle = new Bundle();
            bundle.putString("scannedData", scannedData);
            fragment.setArguments(bundle);
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, fragment);
        transaction.commit();
    }
}

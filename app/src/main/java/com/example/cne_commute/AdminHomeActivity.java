package com.example.cne_commute;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class AdminHomeActivity extends AppCompatActivity {

    private Button homeButton, mapButton, historyButton, accountButton, reportButton;
    private FloatingActionButton fabQRCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        homeButton = findViewById(R.id.admin_home_button);
        mapButton = findViewById(R.id.admin_map_button);
        historyButton = findViewById(R.id.admin_history_button);
        accountButton = findViewById(R.id.admin_account_button);
        reportButton = findViewById(R.id.admin_report_button);
        fabQRCode = findViewById(R.id.admin_fab_qr_code);

        homeButton.setOnClickListener(v -> showToast("Home Button Clicked"));
        mapButton.setOnClickListener(v -> openGoogleMaps());
        historyButton.setOnClickListener(v -> showToast("History Button Clicked"));
        accountButton.setOnClickListener(v -> showToast("Account Button Clicked"));
        reportButton.setOnClickListener(v -> showToast("View Reports Clicked"));
        fabQRCode.setOnClickListener(v -> showToast("QR Code Button Clicked"));
    }

    private void showToast(String message) {
        Toast.makeText(AdminHomeActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void openGoogleMaps() {
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=Tricycles");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            showToast("Google Maps app is not installed");
        }
    }
}

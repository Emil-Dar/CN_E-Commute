package com.example.cne_commute;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReportDetailsActivity extends AppCompatActivity {

    private TextView descriptionText, violationText, statusText, remarksText, timestampText;
    private ImageView reportImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_details);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Report Details");
        }

        // Bind views
        timestampText = findViewById(R.id.timestamp_text);
        descriptionText = findViewById(R.id.description_text);
        violationText = findViewById(R.id.violation_text);
        statusText = findViewById(R.id.status_text);
        remarksText = findViewById(R.id.remarks_text);
        reportImage = findViewById(R.id.report_image);

        // Get data from intent
        String driverId = getIntent().getStringExtra("driverId");
        String driverName = getIntent().getStringExtra("driverName");
        String franchiseId = getIntent().getStringExtra("franchiseId");
        String operatorName = getIntent().getStringExtra("operatorName");
        String toda = getIntent().getStringExtra("toda");
        String violations = getIntent().getStringExtra("violations");
        String status = getIntent().getStringExtra("status");
        String remarks = getIntent().getStringExtra("remarks");
        String imageUrl = getIntent().getStringExtra("imageUrl");
        String rawTimestamp = getIntent().getStringExtra("timestamp");

        // Populate views
        timestampText.setText("Reported on: " + formatTimestamp(rawTimestamp));

        descriptionText.setText(
                "Driver Name: " + safe(driverName) + "\n" +
                        "Driver ID: " + safe(driverId) + "\n" +
                        "Franchise ID: " + safe(franchiseId) + "\n" +
                        "Operator Name: " + safe(operatorName) + "\n" +
                        "TODA: " + safe(toda)
        );

        violationText.setText(safe(violations));
        statusText.setText("Status: " + safe(status));
        remarksText.setText("Remarks: " + safe(remarks));

        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            Glide.with(this).load(imageUrl).into(reportImage);
            reportImage.setVisibility(ImageView.VISIBLE);
        } else {
            reportImage.setVisibility(ImageView.GONE);
        }
    }

    private String formatTimestamp(String raw) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
            Date date = inputFormat.parse(raw);
            return outputFormat.format(date);
        } catch (Exception e) {
            return "Unknown time";
        }
    }

    private String safe(String value) {
        return value != null && !value.trim().isEmpty() ? value : "N/A";
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

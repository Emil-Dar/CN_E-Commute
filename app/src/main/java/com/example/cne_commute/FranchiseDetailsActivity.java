package com.example.cne_commute;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class FranchiseDetailsActivity extends AppCompatActivity {

    private TextView detailsFranchiseId, detailsRegDate, detailsToda, detailsPlate, detailsRenewalStatus, detailsRenewalDate;
    private Button assignDriverButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_franchise_details);

        detailsFranchiseId = findViewById(R.id.detailsFranchiseId);
        detailsRegDate = findViewById(R.id.detailsRegDate);
        detailsToda = findViewById(R.id.detailsToda);

        detailsRenewalStatus = findViewById(R.id.detailsRenewalStatus);
        detailsRenewalDate = findViewById(R.id.detailsRenewalDate);
        assignDriverButton = findViewById(R.id.assignDriverButton);

        // get data from intent
        String franchiseId = getIntent().getStringExtra("franchiseId");
        String regDate = getIntent().getStringExtra("registrationDate");
        String toda = getIntent().getStringExtra("toda");
        String renewalStatus = getIntent().getStringExtra("renewalStatus");
        String renewalDate = getIntent().getStringExtra("renewalDate");

        // bind values
        detailsFranchiseId.setText("Franchise ID: " + franchiseId);
        detailsRegDate.setText("Registration Date: " + regDate);
        detailsToda.setText("TODA: " + toda);
        detailsRenewalStatus.setText("Renewal Status: " + renewalStatus);
        detailsRenewalDate.setText("Renewal Date: " + renewalDate);

        // button click → open assign driver screen
        assignDriverButton.setOnClickListener(v -> {
            Intent intent = new Intent(FranchiseDetailsActivity.this, AssignDriverActivity.class);
            intent.putExtra("franchiseId", franchiseId); // pass franchise id for reference
            startActivity(intent);
        });
    }
}

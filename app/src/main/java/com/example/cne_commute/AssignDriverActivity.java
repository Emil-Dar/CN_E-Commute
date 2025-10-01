package com.example.cne_commute;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AssignDriverActivity extends AppCompatActivity {

    private TextView assignDriverText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_driver);

        assignDriverText = findViewById(R.id.assignDriverText);

        // get franchise id from intent
        String franchiseId = getIntent().getStringExtra("franchiseId");
        assignDriverText.setText("Assign a driver to Franchise ID: " + franchiseId);
    }
}

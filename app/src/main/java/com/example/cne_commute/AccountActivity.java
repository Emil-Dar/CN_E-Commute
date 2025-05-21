package com.example.cne_commute;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        // Initialize UI components
        ImageView profilePicture = findViewById(R.id.profile_picture);
        TextView accountText = findViewById(R.id.account_text);
        TextView driverName = findViewById(R.id.driver_name);
        TextView driverLicense = findViewById(R.id.driver_license);
        EditText contactNumber = findViewById(R.id.contact_number);
        EditText shortDescription = findViewById(R.id.short_description);
        Button editButton = findViewById(R.id.edit_button);
        Button saveButton = findViewById(R.id.save_button);

        // Initially, fields are not editable
        contactNumber.setEnabled(false);
        shortDescription.setEnabled(false);

        // Handle "Edit" button click
        editButton.setOnClickListener(v -> {
            // Enable editing for fields
            contactNumber.setEnabled(true);
            shortDescription.setEnabled(true);
            editButton.setVisibility(Button.GONE);
            saveButton.setVisibility(Button.VISIBLE);
        });

        // Handle "Save" button click
        saveButton.setOnClickListener(v -> {
            // Get updated values
            String updatedContactNumber = contactNumber.getText().toString();
            String updatedShortDescription = shortDescription.getText().toString();

            // TODO: Add logic to save updated values to backend or local database

            // Disable editing and switch button visibility
            contactNumber.setEnabled(false);
            shortDescription.setEnabled(false);
            saveButton.setVisibility(Button.GONE);
            editButton.setVisibility(Button.VISIBLE);
        });

        // Set up profile picture click listener
        profilePicture.setOnClickListener(v -> {
            // Handle profile picture click logic
        });

        // Initialize Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    startActivity(new Intent(AccountActivity.this, HomeActivity.class));
                    return true;
                } else if (itemId == R.id.nav_calculator) {
                    startActivity(new Intent(AccountActivity.this, FareCalculatorActivity.class));
                    return true;
                } else if (itemId == R.id.nav_history) {
                    startActivity(new Intent(AccountActivity.this, HistoryActivity.class));
                    return true;
                } else if (itemId == R.id.nav_account) {
                    return true;
                } else {
                    return false;
                }
            }
        });

        // Set the selected item
        bottomNavigationView.setSelectedItemId(R.id.nav_account);
    }
}

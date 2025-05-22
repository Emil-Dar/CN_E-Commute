package com.example.cne_commute;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AccountActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "AccountPrefs";
    private static final String KEY_CONTACT_NUMBER = "contact_number";
    private static final String KEY_COMMUTER_NAME = "commuter_name";
    private static final String KEY_EMAIL_ADDRESS = "email_address";

    private TextView contactNumberTextView, commuterNameTextView, emailAddressTextView;
    private EditText contactNumberEditText, commuterNameEditText, emailAddressEditText;
    private Button editButton, saveButton;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        ImageView profilePicture = findViewById(R.id.profile_picture);
        TextView accountText = findViewById(R.id.account_text);

        // TextViews
        commuterNameTextView = findViewById(R.id.commuter_name_textview);
        emailAddressTextView = findViewById(R.id.email_address_textview);
        contactNumberTextView = findViewById(R.id.contact_number_textview);

        // EditTexts
        commuterNameEditText = findViewById(R.id.commuter_name_edittext);
        emailAddressEditText = findViewById(R.id.email_address_edittext);
        contactNumberEditText = findViewById(R.id.contact_number_edittext);

        editButton = findViewById(R.id.edit_button);
        saveButton = findViewById(R.id.save_button);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Load saved data or default
        String savedName = prefs.getString(KEY_COMMUTER_NAME, "Krizza");
        String savedEmail = prefs.getString(KEY_EMAIL_ADDRESS, "krizzaheart.esperas@gmail.com");
        String savedContact = prefs.getString(KEY_CONTACT_NUMBER, "09917809416");

        // Set text for TextViews and EditTexts
        commuterNameTextView.setText("Commuter Name: " + savedName);
        emailAddressTextView.setText("Email Address: " + savedEmail);
        contactNumberTextView.setText("Contact Number: " + savedContact);

        commuterNameEditText.setText(savedName);
        emailAddressEditText.setText(savedEmail);
        contactNumberEditText.setText(savedContact);

        // Show TextViews, hide EditTexts initially
        commuterNameTextView.setVisibility(TextView.VISIBLE);
        emailAddressTextView.setVisibility(TextView.VISIBLE);
        contactNumberTextView.setVisibility(TextView.VISIBLE);

        commuterNameEditText.setVisibility(EditText.GONE);
        emailAddressEditText.setVisibility(EditText.GONE);
        contactNumberEditText.setVisibility(EditText.GONE);

        // Edit button click listener
        editButton.setOnClickListener(v -> {
            // Hide TextViews, show EditTexts for editing
            commuterNameTextView.setVisibility(TextView.GONE);
            emailAddressTextView.setVisibility(TextView.GONE);
            contactNumberTextView.setVisibility(TextView.GONE);

            commuterNameEditText.setVisibility(EditText.VISIBLE);
            emailAddressEditText.setVisibility(EditText.VISIBLE);
            contactNumberEditText.setVisibility(EditText.VISIBLE);

            // Switch buttons visibility
            editButton.setVisibility(Button.GONE);
            saveButton.setVisibility(Button.VISIBLE);
        });

        // Save button click listener
        saveButton.setOnClickListener(v -> {
            // Get input values
            String newName = commuterNameEditText.getText().toString().trim();
            String newEmail = emailAddressEditText.getText().toString().trim();
            String newContact = contactNumberEditText.getText().toString().trim();

            // Save to SharedPreferences
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_COMMUTER_NAME, newName);
            editor.putString(KEY_EMAIL_ADDRESS, newEmail);
            editor.putString(KEY_CONTACT_NUMBER, newContact);
            editor.apply();

            // Update TextViews
            commuterNameTextView.setText("Commuter Name: " + newName);
            emailAddressTextView.setText("Email Address: " + newEmail);
            contactNumberTextView.setText("Contact Number: " + newContact);

            // Show TextViews, hide EditTexts
            commuterNameTextView.setVisibility(TextView.VISIBLE);
            emailAddressTextView.setVisibility(TextView.VISIBLE);
            contactNumberTextView.setVisibility(TextView.VISIBLE);

            commuterNameEditText.setVisibility(EditText.GONE);
            emailAddressEditText.setVisibility(EditText.GONE);
            contactNumberEditText.setVisibility(EditText.GONE);

            // Switch buttons visibility
            saveButton.setVisibility(Button.GONE);
            editButton.setVisibility(Button.VISIBLE);
        });

        profilePicture.setOnClickListener(v -> {
            // TODO: Handle profile picture update if needed
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(AccountActivity.this, HomeActivity.class));
                return true;
            } else if (id == R.id.nav_calculator) {
                startActivity(new Intent(AccountActivity.this, FareCalculatorActivity.class));
                return true;
            } else if (id == R.id.nav_history) {
                startActivity(new Intent(AccountActivity.this, HistoryActivity.class));
                return true;
            } else if (id == R.id.nav_account) {
                return true;
            }
            return false;
        });
        bottomNavigationView.setSelectedItemId(R.id.nav_account);
    }
}

package com.example.cne_commute;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AccountActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "AccountPrefs";
    private static final String KEY_CONTACT_NUMBER = "contact_number";
    private static final String KEY_COMMUTER_NAME = "commuter_name";
    private static final String KEY_EMAIL_ADDRESS = "email_address";

    private TextView contactTextView, nameTextView, emailTextView;
    private EditText contactEditText, nameEditText, emailEditText;
    private Button editButton, saveButton;
    private Switch themeSwitch;
    private TextView logoutText;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        // Views
        ImageView profilePicture = findViewById(R.id.profile_picture);
        nameTextView = findViewById(R.id.name_textview);
        emailTextView = findViewById(R.id.email_textview);
        contactTextView = findViewById(R.id.contact_textview);

        nameEditText = findViewById(R.id.name_edittext);
        emailEditText = findViewById(R.id.email_edittext);
        contactEditText = findViewById(R.id.contact_edittext);

        editButton = findViewById(R.id.edit_button);
        saveButton = findViewById(R.id.save_button);
        themeSwitch = findViewById(R.id.theme_switch);
        logoutText = findViewById(R.id.logout_text);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Load saved data or defaults
        String savedName = prefs.getString(KEY_COMMUTER_NAME, "Krizza Heart");
        String savedEmail = prefs.getString(KEY_EMAIL_ADDRESS, "krizzaheart.esperas@gmail.com");
        String savedContact = prefs.getString(KEY_CONTACT_NUMBER, "09917809416");

        // Set values to views
        nameTextView.setText(savedName);
        emailTextView.setText(savedEmail);
        contactTextView.setText(savedContact);

        nameEditText.setText(savedName);
        emailEditText.setText(savedEmail);
        contactEditText.setText(savedContact);

        // Default to view mode
        setEditMode(false);

        // Edit button logic
        editButton.setOnClickListener(v -> setEditMode(true));

        // Save button logic
        saveButton.setOnClickListener(v -> {
            String newName = nameEditText.getText().toString().trim();
            String newEmail = emailEditText.getText().toString().trim();
            String newContact = contactEditText.getText().toString().trim();

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_COMMUTER_NAME, newName);
            editor.putString(KEY_EMAIL_ADDRESS, newEmail);
            editor.putString(KEY_CONTACT_NUMBER, newContact);
            editor.apply();

            nameTextView.setText(newName);
            emailTextView.setText(newEmail);
            contactTextView.setText(newContact);

            setEditMode(false);
            Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
        });

        // Logout logic
        logoutText.setOnClickListener(v -> {
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AccountActivity.this, LoginActivity.class));
            finish();
        });

        // Theme toggle logic
        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Toast.makeText(this, "Dark Mode (placeholder)", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Light Mode (placeholder)", Toast.LENGTH_SHORT).show();
            }
        });

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_account); // Mark account as active

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                navigateTo(HomeActivity.class);
                return true;
            } else if (id == R.id.nav_calculator) {
                navigateTo(FareCalculatorActivity.class);
                return true;
            } else if (id == R.id.nav_history) {
                navigateTo(HistoryActivity.class);
                return true;
            } else if (id == R.id.nav_account) {
                return true; // Already here
            }
            return false;
        });
    }

    private void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(this, targetActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, 0); // Apply fade-in only
    }

    private void setEditMode(boolean enabled) {
        nameTextView.setVisibility(enabled ? TextView.GONE : TextView.VISIBLE);
        emailTextView.setVisibility(enabled ? TextView.GONE : TextView.VISIBLE);
        contactTextView.setVisibility(enabled ? TextView.GONE : TextView.VISIBLE);

        nameEditText.setVisibility(enabled ? EditText.VISIBLE : EditText.GONE);
        emailEditText.setVisibility(enabled ? EditText.VISIBLE : EditText.GONE);
        contactEditText.setVisibility(enabled ? EditText.VISIBLE : EditText.GONE);

        editButton.setVisibility(enabled ? Button.GONE : Button.VISIBLE);
        saveButton.setVisibility(enabled ? Button.VISIBLE : Button.GONE);
    }
}

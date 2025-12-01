package com.example.cne_commute;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class CommuterAccountActivity extends AppCompatActivity {

    private static final String TAG = "CommuterAccountActivity";

    private EditText firstNameEditText, middleNameEditText, lastNameEditText, suffixEditText;
    private EditText contactEditText, addressEditText;
    private TextView contactTextView, nameTextView, emailTextView;
    private Button editButton, saveButton, changePasswordButton;
    private TextView logoutText;
    private Switch themeSwitch;
    private BottomNavigationView bottomNavigationView;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "CommuterAccountActivity launched — onCreate triggered");

        setContentView(R.layout.activity_commuter_account);
        prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        // Assign views
        firstNameEditText = findViewById(R.id.first_name_edittext);
        middleNameEditText = findViewById(R.id.middle_name_edittext);
        lastNameEditText = findViewById(R.id.last_name_edittext);
        suffixEditText = findViewById(R.id.suffix_edittext);
        contactEditText = findViewById(R.id.contact_edittext);
        addressEditText = findViewById(R.id.address_edittext);
        contactTextView = findViewById(R.id.contact_textview);
        nameTextView = findViewById(R.id.name_textview);
        emailTextView = findViewById(R.id.email_textview);

        editButton = findViewById(R.id.edit_button);
        saveButton = findViewById(R.id.save_button);
        changePasswordButton = findViewById(R.id.change_password_button);
        logoutText = findViewById(R.id.logout_text);
        themeSwitch = findViewById(R.id.theme_switch);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Load saved data
        String firstName = prefs.getString("first_name", "");
        String middleName = prefs.getString("middle_name", "");
        String lastName = prefs.getString("last_name", "");
        String suffix = prefs.getString("suffix", "");
        String contact = prefs.getString("commuter_contact", "");
        String address = prefs.getString("address", "");
        String email = prefs.getString("email_address", "");
        String fallbackName = prefs.getString("commuter_name", "Commuter");

        Log.d(TAG, "Loaded: " + firstName + " " + middleName + " " + lastName + " " + suffix + ", " + contact + ", " + address + ", " + email);

        // Display full name with fallback
        String fullName;
        if (!firstName.isEmpty() || !lastName.isEmpty()) {
            fullName = (firstName + " " + middleName + " " + lastName).trim();
            if (!suffix.isEmpty()) fullName += ", " + suffix;
        } else {
            fullName = fallbackName;
        }
        Log.d(TAG, "Final full name displayed: " + fullName);
        nameTextView.setText(fullName);
        contactTextView.setText(contact);
        emailTextView.setText(email.isEmpty() ? "commuter@email.com" : email);

        // Populate edit fields
        firstNameEditText.setText(firstName);
        middleNameEditText.setText(middleName);
        lastNameEditText.setText(lastName);
        suffixEditText.setText(suffix);
        contactEditText.setText(contact);
        addressEditText.setText(address);

        setEditMode(false);
        setupFocusListeners();

        editButton.setOnClickListener(v -> {
            Log.d(TAG, "Edit button clicked");
            setEditMode(true);
        });

        saveButton.setOnClickListener(v -> {
            Log.d(TAG, "Save button clicked");

            String newFirstName = firstNameEditText.getText().toString().trim();
            String newMiddleName = middleNameEditText.getText().toString().trim();
            String newLastName = lastNameEditText.getText().toString().trim();
            String newSuffix = suffixEditText.getText().toString().trim();
            String newContact = contactEditText.getText().toString().trim();
            String newAddress = addressEditText.getText().toString().trim();

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("first_name", newFirstName);
            editor.putString("middle_name", newMiddleName);
            editor.putString("last_name", newLastName);
            editor.putString("suffix", newSuffix);
            editor.putString("commuter_contact", newContact);
            editor.putString("address", newAddress);
            editor.apply();

            String updatedFullName = (newFirstName + " " + newMiddleName + " " + newLastName).trim();
            if (!newSuffix.isEmpty()) updatedFullName += ", " + newSuffix;
            nameTextView.setText(updatedFullName);
            contactTextView.setText(newContact);

            setEditMode(false);
            Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
        });

        changePasswordButton.setOnClickListener(v -> {
            Log.d(TAG, "Change Password button clicked");
            Intent intent = new Intent(CommuterAccountActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        logoutText.setOnClickListener(v -> {
            Log.d(TAG, "Logout clicked — clearing SharedPreferences and launching UserRoleSelectionActivity");
            Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();

            // Firebase logout
            FirebaseAuth.getInstance().signOut();

            // Google logout
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, gso);

            googleSignInClient.signOut().addOnCompleteListener(task -> {
                // Clear SharedPreferences
                SharedPreferences.Editor editor = getSharedPreferences("UserPrefs", MODE_PRIVATE).edit();
                editor.clear();
                editor.apply();

                // Redirect to user role selection screen
                Intent intent = new Intent(CommuterAccountActivity.this, UserRoleSelectionActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                finish();
            });
        });


        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(this, isChecked ? "Dark Mode (placeholder)" : "Light Mode (placeholder)", Toast.LENGTH_SHORT).show();
        });

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_account);

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
                return true;
            }
            return false;
        });
    }

    private void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(this, targetActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, 0);
    }

    private void setEditMode(boolean enabled) {
        int visible = enabled ? View.VISIBLE : View.GONE;
        int hidden = enabled ? View.GONE : View.VISIBLE;

        firstNameEditText.setVisibility(visible);
        middleNameEditText.setVisibility(visible);
        lastNameEditText.setVisibility(visible);
        suffixEditText.setVisibility(visible);
        contactEditText.setVisibility(visible);
        addressEditText.setVisibility(visible);

        contactTextView.setVisibility(hidden);
        nameTextView.setVisibility(hidden);

        editButton.setVisibility(hidden);
        saveButton.setVisibility(visible);
    }

    private void setupFocusListeners() {
        View.OnFocusChangeListener hideNavOnFocus = (v, hasFocus) -> {
            if (hasFocus) {
                bottomNavigationView.setVisibility(View.GONE);
            } else {
                bottomNavigationView.setVisibility(View.VISIBLE);
            }
        };

        firstNameEditText.setOnFocusChangeListener(hideNavOnFocus);
        middleNameEditText.setOnFocusChangeListener(hideNavOnFocus);
        lastNameEditText.setOnFocusChangeListener(hideNavOnFocus);
        suffixEditText.setOnFocusChangeListener(hideNavOnFocus);
        contactEditText.setOnFocusChangeListener(hideNavOnFocus);
        addressEditText.setOnFocusChangeListener(hideNavOnFocus);
    }
}

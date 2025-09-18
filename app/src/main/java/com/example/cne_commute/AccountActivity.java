package com.example.cne_commute;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AccountActivity extends AppCompatActivity {

    private static final String TAG = "AccountActivity";

    private TextView nameTextView, emailTextView, contactTextView, logoutText;
    private EditText nameEditText, emailEditText, contactEditText;
    private Button editButton, saveButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        nameTextView = findViewById(R.id.name_textview);
        emailTextView = findViewById(R.id.email_textview);
        contactTextView = findViewById(R.id.contact_textview);

        nameEditText = findViewById(R.id.name_edittext);
        emailEditText = findViewById(R.id.email_edittext);
        contactEditText = findViewById(R.id.contact_edittext);

        editButton = findViewById(R.id.edit_button);
        saveButton = findViewById(R.id.save_button);
        logoutText = findViewById(R.id.logout_text);

        loadCommuterData();

        editButton.setOnClickListener(v -> setEditMode(true));
        saveButton.setOnClickListener(v -> saveCommuterData());

        logoutText.setOnClickListener(v -> logoutUser());

        setupBottomNavigation();
    }

    private void loadCommuterData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Log.d(TAG, "No logged-in user!");
            return;
        }

        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    Log.d(TAG, "Firestore document data: " + document.getData());

                    if (document.exists()) {
                        String userType = document.getString("userType");
                        if (!"Commuter".equals(userType)) {
                            Toast.makeText(this, "Not a commuter account", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "User is not a commuter");
                            return;
                        }

                        String name = document.getString("username");
                        String email = document.getString("email");
                        String contact = document.getString("contact");

                        Log.d(TAG, "Loaded commuter info - name: " + name + ", email: " + email + ", contact: " + contact);

                        nameTextView.setText(name != null ? name : "");
                        emailTextView.setText(email != null ? email : "");
                        contactTextView.setText(contact != null ? contact : "");

                        nameEditText.setText(name != null ? name : "");
                        emailEditText.setText(email != null ? email : "");
                        contactEditText.setText(contact != null ? contact : "");
                    } else {
                        Toast.makeText(this, "Profile not found", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Document does not exist!");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Error fetching document", e);
                });
    }

    private void saveCommuterData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String newName = nameEditText.getText().toString().trim();
        String newEmail = emailEditText.getText().toString().trim();
        String newContact = contactEditText.getText().toString().trim();

        Map<String, Object> updates = new HashMap<>();
        updates.put("username", newName);
        updates.put("email", newEmail);
        updates.put("contact", newContact);

        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists() && "Commuter".equals(document.getString("userType"))) {
                        db.collection("users").document(user.getUid())
                                .update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Commuter profile updated successfully");
                                    nameTextView.setText(newName);
                                    emailTextView.setText(newEmail);
                                    contactTextView.setText(newContact);
                                    setEditMode(false);
                                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Log.d(TAG, "Failed to update commuter profile", e);
                                    Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "Not a commuter account", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "User is not a commuter");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "Error verifying commuter userType", e);
                    Toast.makeText(this, "Failed to verify commuter", Toast.LENGTH_SHORT).show();
                });
    }

    private void setEditMode(boolean editable) {
        nameTextView.setVisibility(editable ? View.GONE : View.VISIBLE);
        emailTextView.setVisibility(editable ? View.GONE : View.VISIBLE);
        contactTextView.setVisibility(editable ? View.GONE : View.VISIBLE);

        nameEditText.setVisibility(editable ? View.VISIBLE : View.GONE);
        emailEditText.setVisibility(editable ? View.VISIBLE : View.GONE);
        contactEditText.setVisibility(editable ? View.VISIBLE : View.GONE);

        editButton.setVisibility(editable ? View.GONE : View.VISIBLE);
        saveButton.setVisibility(editable ? View.VISIBLE : View.GONE);
    }

    private void logoutUser() {
        mAuth.signOut();
        Intent intent = new Intent(AccountActivity.this, UserRoleSelectionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_account);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = null;
            if (id == R.id.nav_home) intent = new Intent(this, HomeActivity.class);
            else if (id == R.id.nav_calculator) intent = new Intent(this, FareCalculatorActivity.class);
            else if (id == R.id.nav_history) intent = new Intent(this, HistoryActivity.class);
            else if (id == R.id.nav_account) return true;

            if (intent != null) {
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, 0);
            }
            return true;
        });
    }
}

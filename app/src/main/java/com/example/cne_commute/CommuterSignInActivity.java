package com.example.cne_commute;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class CommuterSignInActivity extends AppCompatActivity {

    private static final String TAG = "CommuterSignInActivity";

    private EditText emailEditText, passwordEditText;
    private ImageView passwordEyeIcon;
    private boolean isPasswordVisible = false;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        Log.d(TAG, "onCreate: CommuterSignInActivity launched");

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        Button signInButton = findViewById(R.id.sign_in_button);
        TextView signupLink = findViewById(R.id.signin_link);
        passwordEyeIcon = findViewById(R.id.password_eye_icon);

        signInButton.setOnClickListener(v -> {
            Log.d(TAG, "Sign-in button clicked");
            signInCommuter();
        });

        signupLink.setOnClickListener(v -> {
            Log.d(TAG, "Sign-up link clicked");
            Intent intent = new Intent(CommuterSignInActivity.this, CommuterSignUpActivity.class);
            startActivity(intent);
            finish();
        });

        passwordEyeIcon.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            Log.d(TAG, "Password visibility toggled: " + isPasswordVisible);
            if (isPasswordVisible) {
                passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                passwordEyeIcon.setImageResource(R.drawable.ic_eye_on);
            } else {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                passwordEyeIcon.setImageResource(R.drawable.ic_eye_off);
            }
            passwordEditText.setSelection(passwordEditText.getText().length());
        });

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String savedEmail = prefs.getString("email_address", "");
        if (!savedEmail.isEmpty()) {
            emailEditText.setText(savedEmail);
        }
    }

    private void signInCommuter() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Enter a valid email");
            emailEditText.requestFocus();
            return;
        }

        if (password.isEmpty() || password.length() < 6) {
            passwordEditText.setError("Enter a valid password (at least 6 characters)");
            passwordEditText.requestFocus();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            fetchCommuterProfile(user.getUid());
                        }
                    } else {
                        String errorMessage = (task.getException() != null) ? task.getException().getMessage() : "Unknown error";
                        Log.e(TAG, "Authentication Failed: " + errorMessage);
                        Toast.makeText(this, "Authentication Failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchCommuterProfile(@NonNull String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("fullName");
                        String contact = documentSnapshot.getString("contact");
                        String userType = documentSnapshot.getString("userType");

                        if ("Commuter".equalsIgnoreCase(userType)) {
                            saveToSharedPreferences(fullName, contact);
                            Log.d(TAG, "Commuter profile loaded and saved: " + fullName + ", " + contact);
                            startActivity(new Intent(this, HomeActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "This account is not a commuter.", Toast.LENGTH_SHORT).show();
                            FirebaseAuth.getInstance().signOut();
                        }
                    } else {
                        Toast.makeText(this, "User profile not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching profile: " + e.getMessage());
                    Toast.makeText(this, "Failed to load profile. Try again later.", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveToSharedPreferences(String fullName, String contact) {
        SharedPreferences.Editor editor = getSharedPreferences("UserPrefs", MODE_PRIVATE).edit();

        // Split full name into parts
        String[] nameParts = fullName.trim().split(" ");
        String firstName = nameParts.length > 0 ? nameParts[0] : "";
        String lastName = nameParts.length > 1 ? nameParts[nameParts.length - 1] : "";
        String middleName = "";
        if (nameParts.length > 2) {
            StringBuilder middle = new StringBuilder();
            for (int i = 1; i < nameParts.length - 1; i++) {
                middle.append(nameParts[i]).append(" ");
            }
            middleName = middle.toString().trim();
        }

        editor.putString("first_name", firstName);
        editor.putString("middle_name", middleName);
        editor.putString("last_name", lastName);
        editor.putString("suffix", ""); // Optional
        editor.putString("commuter_contact", contact);
        editor.putString("commuter_name", fullName);
        editor.putString("email_address", emailEditText.getText().toString().trim());

        editor.apply();
    }
}

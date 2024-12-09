package com.example.cne_commute;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class DriverSignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText nameEditText, licenseNumberEditText, passwordEditText;
    private Button signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_sign_up);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        nameEditText = findViewById(R.id.name);
        licenseNumberEditText = findViewById(R.id.license_number);
        passwordEditText = findViewById(R.id.password);
        signUpButton = findViewById(R.id.sign_up_button);

        signUpButton.setOnClickListener(v -> signUpDriver());
    }

    private void signUpDriver() {
        final String name = nameEditText.getText().toString().trim();
        final String licenseNumber = licenseNumberEditText.getText().toString().trim();
        final String password = passwordEditText.getText().toString().trim();

        if (name.isEmpty()) {
            nameEditText.setError("Enter your name");
            nameEditText.requestFocus();
            return;
        }

        if (licenseNumber.isEmpty()) {
            licenseNumberEditText.setError("Enter your license number");
            licenseNumberEditText.requestFocus();
            return;
        }

        if (password.isEmpty() || password.length() < 6) {
            passwordEditText.setError("Enter a valid password (at least 6 characters)");
            passwordEditText.requestFocus();
            return;
        }

        // Create a new user with email and password based on the license number
        mAuth.createUserWithEmailAndPassword(licenseNumber + "@example.com", password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("name", name);
                            userData.put("licenseNumber", licenseNumber);

                            db.collection("drivers").document(user.getUid()).set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(DriverSignUpActivity.this, "User Registered.", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(DriverSignUpActivity.this, DriverSignInActivity.class);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(DriverSignUpActivity.this, "Registration Failed.", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(DriverSignUpActivity.this, "Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

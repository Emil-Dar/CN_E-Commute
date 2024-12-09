package com.example.cne_commute;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DriverSignInActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText licenseNumberEditText, passwordEditText;
    private Button signInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_sign_in);

        mAuth = FirebaseAuth.getInstance();
        licenseNumberEditText = findViewById(R.id.license_number);
        passwordEditText = findViewById(R.id.password);
        signInButton = findViewById(R.id.sign_in_button);

        signInButton.setOnClickListener(v -> signInDriver());
    }

    private void signInDriver() {
        String licenseNumber = licenseNumberEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

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

        // Simulate Firebase Authentication for demonstration purposes
        mAuth.signInWithEmailAndPassword(licenseNumber + "@example.com", password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Toast.makeText(DriverSignInActivity.this, "Sign-in Successful.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(DriverSignInActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Toast.makeText(DriverSignInActivity.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

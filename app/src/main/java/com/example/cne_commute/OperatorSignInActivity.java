package com.example.cne_commute;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class OperatorSignInActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button signInButton;
    private ImageView passwordEyeIcon;
    private boolean isPasswordVisible = false;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_sign_in);

        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        signInButton = findViewById(R.id.sign_in_button);
        TextView signupLink = findViewById(R.id.signin_link);
        passwordEyeIcon = findViewById(R.id.password_eye_icon);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // sign in
        signInButton.setOnClickListener(v -> signInOperator());

        // go to signup (don’t finish, allow back navigation)
        signupLink.setOnClickListener(v -> {
            Intent intent = new Intent(OperatorSignInActivity.this, OperatorSignUpActivity.class);
            startActivity(intent);
        });

        // toggle password visibility
        passwordEyeIcon.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                passwordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                passwordEyeIcon.setImageResource(R.drawable.ic_eye_on);
            } else {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                passwordEyeIcon.setImageResource(R.drawable.ic_eye_off);
            }
            passwordEditText.setSelection(passwordEditText.getText().length());
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();

            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String role = documentSnapshot.getString("userType");
                            if ("Operator".equalsIgnoreCase(role)) {
                                // already signed in as operator → go home
                                Intent intent = new Intent(OperatorSignInActivity.this, OperatorHomeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                // logged in but not operator → sign out
                                mAuth.signOut();
                            }
                        }
                    });
        }
    }

    private void signInOperator() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        signInButton.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    if (mAuth.getCurrentUser() == null) {
                        Toast.makeText(this, "Session error. Try again.", Toast.LENGTH_SHORT).show();
                        signInButton.setEnabled(true);
                        return;
                    }

                    String uid = mAuth.getCurrentUser().getUid();

                    db.collection("users").document(uid).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    String role = documentSnapshot.getString("userType");
                                    if ("operator".equalsIgnoreCase(role)) {
                                        Intent intent = new Intent(OperatorSignInActivity.this, OperatorHomeActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        mAuth.signOut();
                                        Toast.makeText(this, "Access denied: Not an operator", Toast.LENGTH_SHORT).show();
                                        signInButton.setEnabled(true);
                                    }
                                } else {
                                    mAuth.signOut();
                                    Toast.makeText(this, "No operator record found", Toast.LENGTH_SHORT).show();
                                    signInButton.setEnabled(true);
                                }
                            })
                            .addOnFailureListener(e -> {
                                mAuth.signOut();
                                Toast.makeText(this, "Error accessing user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                signInButton.setEnabled(true);
                            });

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Authentication failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    signInButton.setEnabled(true);
                });
    }
}

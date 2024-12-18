package com.example.cne_commute;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";
    private EditText emailEditText, passwordEditText;
    private ImageView passwordEyeIcon;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        Button signInButton = findViewById(R.id.sign_in_button);
        TextView signupLink = findViewById(R.id.signin_link); // Local variable
        passwordEyeIcon = findViewById(R.id.password_eye_icon);

        signInButton.setOnClickListener(v -> signInUser(mAuth));

        signupLink.setOnClickListener(v -> {
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(intent);
            finish();
        });

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

    private void signInUser(FirebaseAuth mAuth) {
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
                            Toast.makeText(SignInActivity.this, "Sign-in Successful.", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Sign-in Successful: " + email);
                            Intent intent = new Intent(SignInActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        String errorMessage = (task.getException() != null) ? task.getException().getMessage() : "Unknown error";
                        Log.e(TAG, "Authentication Failed: " + errorMessage);
                        Toast.makeText(SignInActivity.this, "Authentication Failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

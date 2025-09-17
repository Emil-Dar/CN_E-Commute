package com.example.cne_commute;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // ✅ Already signed in — go directly to Home
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
            finish(); // Prevent returning to this screen
        } else {
            // 👇 Not signed in — show login/signup options
            setContentView(R.layout.activity_main);

            Button signInButton = findViewById(R.id.sign_in_button);
            Button signUpButton = findViewById(R.id.sign_up_button);

            signInButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                startActivity(intent);
            });

            signUpButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(intent);
            });
        }
    }
}

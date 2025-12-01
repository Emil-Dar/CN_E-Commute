package com.example.cne_commute;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashScreenActivity extends AppCompatActivity {

    private ImageView logoImageView;
    private TextView taglineTextView;
    private LinearLayout buttonLayout;
    private Button signInButton, signUpButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        logoImageView = findViewById(R.id.logo);
        taglineTextView = findViewById(R.id.tagline);

        buttonLayout.setVisibility(View.GONE);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {

            // 🔹 User is signed in — fetch their role first
            new Handler().postDelayed(() -> {
                db.collection("users").document(currentUser.getUid())
                        .get()
                        .addOnSuccessListener(doc -> {
                            if (doc.exists() && doc.getString("userType") != null) {
                                String role = doc.getString("userType");

                                if ("Operator".equalsIgnoreCase(role)) {
                                    goToActivity(OperatorHomeActivity.class);
                                } else if ("Driver".equalsIgnoreCase(role)) {
                                    goToActivity(DriverHomeActivity.class);
                                } else if ("Commuter".equalsIgnoreCase(role)) {
                                    goToActivity(HomeActivity.class);
                                } else {
                                    goToRoleSelection();
                                }
                            } else {
                                goToRoleSelection();
                            }
                        })
                        .addOnFailureListener(e -> goToRoleSelection());
            }, 1500); // Krizza's short splash delay

        } else {

            // 🔹 Not logged in — show sign-in/sign-up buttons after animation
            signInButton.setOnClickListener(v -> {
                Intent intent = new Intent(SplashScreenActivity.this, UserRoleSelectionActivity.class);
                intent.putExtra("user_type", R.id.sign_in_button);
                startActivity(intent);
            });

            signUpButton.setOnClickListener(v -> {
                Intent intent = new Intent(SplashScreenActivity.this, UserRoleSelectionActivity.class);
                intent.putExtra("user_type", R.id.sign_up_button);
                startActivity(intent);
            });

            new Handler().postDelayed(() -> {
                buttonLayout.setVisibility(View.VISIBLE);
                Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
                buttonLayout.startAnimation(fadeIn);
            }, 3000); // Krizza's 3 sec delay
        }
    }

    private void goToActivity(Class<?> target) {
        Intent intent = new Intent(SplashScreenActivity.this, target);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void goToRoleSelection() {
        Intent intent = new Intent(SplashScreenActivity.this, UserRoleSelectionActivity.class);
        startActivity(intent);
        finish();
    }
}

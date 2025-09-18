package com.example.cne_commute;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashScreenActivity extends AppCompatActivity {

    private ImageView logoImageView;
    private TextView taglineTextView;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        logoImageView = findViewById(R.id.logo);
        taglineTextView = findViewById(R.id.tagline);
        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            showProgressBar();

            db.collection("users").document(uid).get()
                    .addOnSuccessListener(doc -> {
                        hideProgressBar();
                        if (doc.exists()) {
                            String role = doc.getString("userType");
                            if ("Operator".equalsIgnoreCase(role)) {
                                goToActivity(OperatorHomeActivity.class);
                            } else if ("Driver".equalsIgnoreCase(role)) {
                                goToActivity(DriverHomeActivity.class);
                            } else if ("Commuter".equalsIgnoreCase(role)) {
                                goToActivity(HomeActivity.class);
                            } else {
                                goToRoleSelectionWithAnimation();
                            }
                        } else {
                            goToRoleSelectionWithAnimation();
                        }
                    })
                    .addOnFailureListener(e -> {
                        hideProgressBar();
                        goToRoleSelectionWithAnimation();
                    });
        } else {
            goToRoleSelectionWithAnimation(); // not logged in
        }
    }

    private void goToActivity(Class<?> target) {
        Intent intent = new Intent(SplashScreenActivity.this, target);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showProgressBar() {
        progressBar.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        progressBar.startAnimation(fadeOut);
        progressBar.setVisibility(View.GONE);
    }

    private void goToRoleSelectionWithAnimation() {
        new Handler().postDelayed(() -> {
            Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
            logoImageView.startAnimation(slideUp);
            taglineTextView.startAnimation(slideUp);

            slideUp.setAnimationListener(new Animation.AnimationListener() {
                @Override public void onAnimationStart(Animation animation) {}
                @Override public void onAnimationEnd(Animation animation) {
                    Intent intent = new Intent(SplashScreenActivity.this, UserRoleSelectionActivity.class);
                    startActivity(intent);
                    finish();
                }
                @Override public void onAnimationRepeat(Animation animation) {}
            });
        }, 3000);
    }
}

package com.example.cne_commute;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashScreenActivityNew extends AppCompatActivity {

    private ImageView logoImageView;
    private TextView taglineTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen_new);

        logoImageView = findViewById(R.id.logo);
        taglineTextView = findViewById(R.id.tagline);

        // Animate logo + tagline
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        logoImageView.startAnimation(fadeIn);
        taglineTextView.startAnimation(fadeIn);

        // Splash delay
        new Handler().postDelayed(() -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            Intent intent;

            if (currentUser != null) {
                // User already logged in → go straight to HomeActivity
                intent = new Intent(SplashScreenActivityNew.this, HomeActivity.class);
            } else {
                // Not logged in → show UserRoleSelectionActivity
                intent = new Intent(SplashScreenActivityNew.this, UserRoleSelectionActivity.class);
            }

            startActivity(intent);
            finish();

        }, 2000); // 2 seconds
    }
}

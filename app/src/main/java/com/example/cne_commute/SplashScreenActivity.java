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

public class SplashScreenActivity extends AppCompatActivity {

    private ImageView logoImageView;
    private TextView taglineTextView;
    private LinearLayout buttonLayout;
    private Button signInButton, signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        logoImageView = findViewById(R.id.logo);
        taglineTextView = findViewById(R.id.tagline);
        buttonLayout = findViewById(R.id.button_layout);
        signInButton = findViewById(R.id.sign_in_button);
        signUpButton = findViewById(R.id.sign_up_button);

        buttonLayout.setVisibility(View.GONE); // Hide buttons initially

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            //  Already signed in — go to HomeActivity after short splash delay
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(SplashScreenActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }, 1500); // Optional splash delay
        } else {
            //  Not signed in — show splash buttons after animation
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
                Animation fadeIn = AnimationUtils.loadAnimation(SplashScreenActivity.this, R.anim.fade_in);
                buttonLayout.startAnimation(fadeIn);
            }, 3000); // 3 seconds delay
        }
    }
}

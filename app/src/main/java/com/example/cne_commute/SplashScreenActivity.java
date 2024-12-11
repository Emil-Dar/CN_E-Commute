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

        buttonLayout.setVisibility(View.GONE); // Hide the buttons initially

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

        // Delay for 3 seconds before starting the animation and showing buttons
        new Handler().postDelayed(() -> {
            Animation moveUp = AnimationUtils.loadAnimation(SplashScreenActivity.this, R.anim.move_up);
            logoImageView.startAnimation(moveUp);
            taglineTextView.startAnimation(moveUp);
            moveUp.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    // No action needed here
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    buttonLayout.setVisibility(View.VISIBLE); // Show the buttons after the animation ends
                    Animation fadeIn = AnimationUtils.loadAnimation(SplashScreenActivity.this, R.anim.fade_in);
                    buttonLayout.startAnimation(fadeIn);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    // No action needed here
                }
            });
        }, 3000); // 3 seconds delay
    }
}

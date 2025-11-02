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

        // Optional: animate logo and tagline
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        logoImageView.startAnimation(fadeIn);
        taglineTextView.startAnimation(fadeIn);

        // Delay before routing
        new Handler().postDelayed(() -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            Intent intent;
            if (currentUser != null) {
                //  Already signed in — go to HomeActivity
                intent = new Intent(SplashScreenActivityNew.this, HomeActivity.class);
            } else {
                //  Not signed in — go to CommuterSignInActivity
                intent = new Intent(SplashScreenActivityNew.this, CommuterSignInActivity.class);
            }

            startActivity(intent);
            finish();
        }, 2000); // 2 seconds splash delay
    }
}

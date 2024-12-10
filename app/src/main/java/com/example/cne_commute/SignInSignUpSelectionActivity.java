package com.example.cne_commute;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class SignInSignUpSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_sign_up_selection);

        Button signInButton = findViewById(R.id.sign_in_button);
        Button signUpButton = findViewById(R.id.sign_up_button);

        int userType = getIntent().getIntExtra("user_type", -1);

        View.OnClickListener onClickListener = v -> {
            Intent intent;
            if (v.getId() == R.id.sign_in_button) {
                intent = new Intent(SignInSignUpSelectionActivity.this, SignInActivity.class);
            } else {
                intent = new Intent(SignInSignUpSelectionActivity.this, SignUpActivity.class);
            }
            intent.putExtra("user_type", userType);
            startActivity(intent);
        };

        signInButton.setOnClickListener(onClickListener);
        signUpButton.setOnClickListener(onClickListener);
    }
}

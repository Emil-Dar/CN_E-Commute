package com.example.cne_commute;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

public class UserRoleSelectionActivity extends AppCompatActivity {

    private int userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_role_selection);

        // Initialize role buttons
        LinearLayout commuterButton = findViewById(R.id.commuter_button);
        LinearLayout driverButton = findViewById(R.id.driver_button);
        LinearLayout operatorButton = findViewById(R.id.operator_button);

        // Retrieve the user type (sign in or sign up)
        userType = getIntent().getIntExtra("user_type", -1);

        // Assign shared click listener
        View.OnClickListener roleButtonClickListener = this::handleRoleSelection;
        commuterButton.setOnClickListener(roleButtonClickListener);
        driverButton.setOnClickListener(roleButtonClickListener);
        operatorButton.setOnClickListener(roleButtonClickListener);
    }

    private void handleRoleSelection(View view) {
        Intent intent;

        if (userType == R.id.sign_in_button) {
            intent = getSignInIntent(view.getId());
        } else if (userType == R.id.sign_up_button) {
            intent = getSignUpIntent(view.getId());
        } else {
            return; // Invalid user type
        }

        intent.putExtra("user_role", view.getId());
        startActivity(intent);
    }

    private Intent getSignInIntent(int roleId) {
        if (roleId == R.id.driver_button) {
            return new Intent(this, DriverSignInActivity.class);
        } else if (roleId == R.id.commuter_button) {
            return new Intent(this, CommuterSignInActivity.class);
        } else {
            return new Intent(this, SignInActivity.class); // fallback for others
        }
    }

    private Intent getSignUpIntent(int roleId) {
        if (roleId == R.id.driver_button) {
            return new Intent(this, DriverSignUpActivity.class);
        } else if (roleId == R.id.commuter_button) {
            return new Intent(this, CommuterSignUpActivity.class);
        } else {
            return new Intent(this, SignUpActivity.class); // fallback for others
        }
    }
}

package com.example.cne_commute;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class UserRoleSelectionActivity extends AppCompatActivity {

    private int userType; // sign-in or sign-up

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_role_selection);

        LinearLayout commuterButton = findViewById(R.id.commuter_button);
        LinearLayout driverButton = findViewById(R.id.driver_button);
        LinearLayout operatorButton = findViewById(R.id.operator_button);

        // Get type (sign in / sign up)
        userType = getIntent().getIntExtra("user_type", -1);

        // Attach shared listener
        View.OnClickListener listener = this::handleRoleSelection;
        commuterButton.setOnClickListener(listener);
        driverButton.setOnClickListener(listener);
        operatorButton.setOnClickListener(listener);
    }

    private void handleRoleSelection(View view) {
        Intent intent;

        // Determine if user clicked sign-in or sign-up
        if (userType == R.id.sign_in_button) {
            intent = getSignInIntent(view.getId());
        } else if (userType == R.id.sign_up_button) {
            intent = getSignUpIntent(view.getId());
        } else {
            return; // invalid state
        }

        intent.putExtra("user_role", view.getId());
        startActivity(intent);
    }

    // SIGN-IN ROLES — all three allowed
    private Intent getSignInIntent(int roleId) {
        if (roleId == R.id.driver_button) {
            return new Intent(this, DriverSignInActivity.class);
        } else if (roleId == R.id.operator_button) {
            return new Intent(this, OperatorSignInActivity.class);
        } else { // commuter
            return new Intent(this, SignInActivity.class);
        }
    }

    // SIGN-UP ROLES — ONLY COMMUTER
    private Intent getSignUpIntent(int roleId) {
        if (roleId == R.id.commuter_button) {
            return new Intent(this, SignUpActivity.class);
        } else {
            // ❗ Drivers & Operators CANNOT sign up
            // Redirect them to sign-in instead
            return getSignInIntent(roleId);
        }
    }
}

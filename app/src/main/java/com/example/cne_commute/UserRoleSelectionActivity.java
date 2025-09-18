package com.example.cne_commute;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class UserRoleSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_role_selection);

        // Initialize role buttons
        LinearLayout commuterButton = findViewById(R.id.commuter_button);
        LinearLayout driverButton = findViewById(R.id.driver_button);
        LinearLayout operatorButton = findViewById(R.id.operator_button);

        // Set individual click listeners for sign-in redirection
        commuterButton.setOnClickListener(v ->
                startActivity(new Intent(this, SignInActivity.class)));

        driverButton.setOnClickListener(v ->
                startActivity(new Intent(this, DriverSignInActivity.class)));

        operatorButton.setOnClickListener(v ->
                startActivity(new Intent(this, OperatorSignInActivity.class)));
    }
}

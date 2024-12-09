package com.example.cne_commute;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class UserRoleSelectionActivity extends AppCompatActivity {

    private int userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_role_selection);

        Button commuterButton = findViewById(R.id.commuter_button);
        Button driverButton = findViewById(R.id.driver_button);
        Button operatorButton = findViewById(R.id.operator_button);
        Button adminButton = findViewById(R.id.admin_button);

        // Retrieve user type from intent
        userType = getIntent().getIntExtra("user_type", -1);

        View.OnClickListener roleButtonClickListener = v -> {
            Intent intent;
            if (userType == R.id.sign_in_button) {
                intent = new Intent(UserRoleSelectionActivity.this, SignInActivity.class);
            } else if (userType == R.id.sign_up_button) {
                intent = new Intent(UserRoleSelectionActivity.this, SignUpActivity.class);
            } else {
                return;
            }
            // Pass the selected user role to the next activity
            intent.putExtra("user_role", v.getId());
            startActivity(intent);
        };

        commuterButton.setOnClickListener(roleButtonClickListener);
        driverButton.setOnClickListener(roleButtonClickListener);
        operatorButton.setOnClickListener(roleButtonClickListener);
        adminButton.setOnClickListener(roleButtonClickListener);
    }
}

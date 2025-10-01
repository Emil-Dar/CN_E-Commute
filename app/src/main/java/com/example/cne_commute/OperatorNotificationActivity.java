package com.example.cne_commute;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;

public class OperatorNotificationActivity extends AppCompatActivity {

    private String operatorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_notification);

        // ✅ get operatorId from intent
        operatorId = getIntent().getStringExtra("operatorId");

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_notification);

        bottomNav.setOnItemSelectedListener(item -> {
            Intent intent = null;
            switch (item.getItemId()) {
                case R.id.nav_home:
                    intent = new Intent(getApplicationContext(), OperatorHomeActivity.class);
                    break;
                case R.id.nav_franchise:
                    intent = new Intent(getApplicationContext(), OperatorFranchiseActivity.class);
                    break;
                case R.id.nav_notification:
                    return true; // already here
                case R.id.nav_account:
                    intent = new Intent(getApplicationContext(), OperatorAccountActivity.class);
                    break;
            }

            if (intent != null) {
                // ✅ pass operatorId to target activity
                intent.putExtra("operatorId", operatorId);
                startActivity(intent);
                finish();
                overridePendingTransition(0,0);
                return true;
            }
            return false;
        });
    }
}

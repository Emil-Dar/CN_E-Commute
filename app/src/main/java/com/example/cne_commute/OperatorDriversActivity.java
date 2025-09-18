package com.example.cne_commute;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;

public class OperatorDriversActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_drivers);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_drivers);

        bottomNav.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.nav_home:
                    startActivity(new Intent(getApplicationContext(), OperatorHomeActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.nav_drivers:
                    return true;
                case R.id.nav_notifications:
                    startActivity(new Intent(getApplicationContext(), OperatorNotificationsActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                case R.id.nav_account:
                    startActivity(new Intent(getApplicationContext(), OperatorAccountActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
            }
            return false;
        });
    }
}

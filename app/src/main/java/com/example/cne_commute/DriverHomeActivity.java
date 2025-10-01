package com.example.cne_commute;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.cne_commute.driver_fragments.DriverHome;
import com.example.cne_commute.driver_fragments.DriverRevenue;
import com.example.cne_commute.driver_fragments.DriverComplaints;
import com.example.cne_commute.driver_fragments.DriverAccount;

public class DriverHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home);

        BottomNavigationView bottomNav = findViewById(R.id.driver_bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            switch (item.getItemId()) {
                case R.id.driver_home:
                    selectedFragment = new DriverHome();
                    break;
                case R.id.driver_revenue:
                    selectedFragment = new DriverRevenue();
                    break;
                case R.id.driver_complaints:
                    selectedFragment = new DriverComplaints();
                    break;
                case R.id.driver_account:
                    selectedFragment = new DriverAccount();
                    break;
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            return true;
        });

        // Default fragment on launch
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DriverHome())
                    .commit();
        }
    }
}

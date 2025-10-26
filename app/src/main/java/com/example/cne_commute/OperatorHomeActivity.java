package com.example.cne_commute;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class OperatorHomeActivity extends AppCompatActivity {

    private String operatorId; // store operator id
    private SupabaseService apiService; // your Retrofit service
    private final String SUPABASE_API_KEY = BuildConfig.SUPABASE_API_KEY; // your API key

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_home);

        operatorId = getIntent().getStringExtra("operatorId");

        apiService = SupabaseApiClient.getRetrofitInstance().create(SupabaseService.class);

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_home); // since this is home screen

        bottomNav.setOnItemSelectedListener(item -> {
            Intent intent = null;
            switch (item.getItemId()) {
                case R.id.nav_home:
                    return true; // already on home
                case R.id.nav_franchise:
                    intent = new Intent(this, OperatorFranchiseActivity.class);
                    break;
                case R.id.nav_notification:
                    intent = new Intent(this, OperatorNotificationActivity.class);
                    break;
                case R.id.nav_account:
                    intent = new Intent(this, OperatorAccountActivity.class);
                    break;
            }
            if (intent != null) {
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

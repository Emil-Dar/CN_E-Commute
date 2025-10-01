package com.example.cne_commute;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OperatorFranchiseActivity extends AppCompatActivity {

    private RecyclerView franchiseRecyclerView;
    private FranchiseAdapter adapter;
    private SupabaseService supabaseService;
    private String operatorId; // keep reference

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_franchise);

        // === recycler view setup ===
        franchiseRecyclerView = findViewById(R.id.franchiseRecyclerView);
        franchiseRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        supabaseService = SupabaseApiClient.getRetrofitInstance().create(SupabaseService.class);

        // get operatorId from intent
        operatorId = getIntent().getStringExtra("operatorId");
        if (operatorId != null) {
            loadFranchises(operatorId);
        } else {
            Toast.makeText(this, "Operator ID not found", Toast.LENGTH_SHORT).show();
        }

        // === bottom navigation setup ===
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_franchise);

        bottomNav.setOnItemSelectedListener(item -> {
            Intent intent = null;
            switch (item.getItemId()) {
                case R.id.nav_home:
                    intent = new Intent(getApplicationContext(), OperatorHomeActivity.class);
                    break;
                case R.id.nav_franchise:
                    return true; // already here
                case R.id.nav_notification:
                    intent = new Intent(getApplicationContext(), OperatorNotificationActivity.class);
                    break;
                case R.id.nav_account:
                    intent = new Intent(getApplicationContext(), OperatorAccountActivity.class);
                    break;
            }
            if (intent != null) {
                // ✅ pass operatorId to other activities
                intent.putExtra("operatorId", operatorId);
                startActivity(intent);
                finish();
                overridePendingTransition(0,0);
                return true;
            }
            return false;
        });
    }

    private void loadFranchises(String operatorId) {
        String apiKey = BuildConfig.SUPABASE_API_KEY;
        String authHeader = "Bearer " + apiKey;

        // supabase expects eq.<id>
        String filter = "eq." + operatorId;

        supabaseService.getFranchisesByOperatorId(apiKey, authHeader, filter)
                .enqueue(new Callback<List<Franchise>>() {
                    @Override
                    public void onResponse(Call<List<Franchise>> call, Response<List<Franchise>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            List<Franchise> franchiseList = response.body();
                            adapter = new FranchiseAdapter(franchiseList, OperatorFranchiseActivity.this);
                            franchiseRecyclerView.setAdapter(adapter);
                        } else {
                            Toast.makeText(OperatorFranchiseActivity.this, "No franchises found for this operator", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Franchise>> call, Throwable t) {
                        Toast.makeText(OperatorFranchiseActivity.this, "Failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}

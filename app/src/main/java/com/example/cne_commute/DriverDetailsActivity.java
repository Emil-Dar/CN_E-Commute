package com.example.cne_commute;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverDetailsActivity extends AppCompatActivity {

    private TextView firstNameText, middleNameText, lastNameText, suffixText;
    private TextView addressText, contactNumText, licenseNumText, licenseExpirationText, licenseRestrictionText;
    private ScrollView contentLayout;
    private ProgressBar loadingSpinner;

    private String supabaseKey = BuildConfig.SUPABASE_API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_details);

        firstNameText = findViewById(R.id.firstNameText);
        middleNameText = findViewById(R.id.middleNameText);
        lastNameText = findViewById(R.id.lastNameText);
        suffixText = findViewById(R.id.suffixText);
        addressText = findViewById(R.id.addressText);
        contactNumText = findViewById(R.id.contactNumText);
        licenseNumText = findViewById(R.id.licenseNumText);
        licenseExpirationText = findViewById(R.id.licenseExpirationText);
        licenseRestrictionText = findViewById(R.id.licenseRestrictionText);

        contentLayout = findViewById(R.id.scrollContent); // wrap LinearLayout in ScrollView with id scrollContent
        loadingSpinner = findViewById(R.id.loadingSpinner); // add a ProgressBar in xml

        String driverId = getIntent().getStringExtra("driverId");
        if (driverId == null || driverId.isEmpty()) {
            Toast.makeText(this, "driver id missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showIntentData();
        fetchDriverFromSupabase(driverId);
    }

    private void showIntentData() {
        firstNameText.setText("First Name: " + safe(getIntent().getStringExtra("first_name")));
        middleNameText.setText("Middle Name: " + safe(getIntent().getStringExtra("middle_name")));
        lastNameText.setText("Last Name: " + safe(getIntent().getStringExtra("last_name")));
        suffixText.setText("Suffix: " + safe(getIntent().getStringExtra("suffix")));
        addressText.setText("Address: " + safe(getIntent().getStringExtra("address")));
        contactNumText.setText("Contact Number: " + safe(getIntent().getStringExtra("contact_num")));
        licenseNumText.setText("License Number: " + safe(getIntent().getStringExtra("license_num")));
        licenseExpirationText.setText("License Expiration: " + safe(getIntent().getStringExtra("license_expiration")));
        licenseRestrictionText.setText("License Restriction: " + safe(getIntent().getStringExtra("license_restriction")));
    }

    private void fetchDriverFromSupabase(String driverId) {
        contentLayout.setVisibility(View.GONE);
        loadingSpinner.setVisibility(View.VISIBLE);

        SupabaseService service = SupabaseApiClient.getRetrofitInstance().create(SupabaseService.class);
        String driverIdFilter = "eq." + driverId;

        Call<List<Driver>> call = service.getDriverById(supabaseKey, "Bearer " + supabaseKey, driverIdFilter);
        call.enqueue(new Callback<List<Driver>>() {
            @Override
            public void onResponse(Call<List<Driver>> call, Response<List<Driver>> response) {
                loadingSpinner.setVisibility(View.GONE);
                contentLayout.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Driver driver = response.body().get(0);

                    firstNameText.setText("First Name: " + safe(driver.firstName));
                    middleNameText.setText("Middle Name: " + safe(driver.middleName));
                    lastNameText.setText("Last Name: " + safe(driver.lastName));
                    suffixText.setText("Suffix: " + safe(driver.suffix));
                    addressText.setText("Address: " + safe(driver.address));
                    contactNumText.setText("Contact Number: " + safe(driver.contactNum));
                    licenseNumText.setText("License Number: " + safe(driver.licenseNum));
                    licenseExpirationText.setText("License Expiration: " + safe(driver.licenseExpiration));
                    licenseRestrictionText.setText("License Restriction: " + safe(driver.licenseRestriction));
                } else {
                    Toast.makeText(DriverDetailsActivity.this, "failed to fetch driver info", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Driver>> call, Throwable t) {
                loadingSpinner.setVisibility(View.GONE);
                contentLayout.setVisibility(View.VISIBLE);
                Toast.makeText(DriverDetailsActivity.this, "error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String safe(String value) {
        return value != null && !value.isEmpty() ? value : "--";
    }
}

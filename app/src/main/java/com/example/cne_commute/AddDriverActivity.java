package com.example.cne_commute;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class AddDriverActivity extends AppCompatActivity {

    private EditText firstNameInput, middleNameInput, lastNameInput, suffixInput;
    private EditText addressInput, contactNumInput, licenseNumInput, licenseExpInput, licenseRestrictionInput;
    private Button cancelBtn, saveBtn;

    private String supabaseKey = BuildConfig.SUPABASE_API_KEY; // stored in gradle ♡

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_driver);

        firstNameInput = findViewById(R.id.firstNameInput);
        middleNameInput = findViewById(R.id.middleNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        suffixInput = findViewById(R.id.suffixInput);
        addressInput = findViewById(R.id.addressInput);
        contactNumInput = findViewById(R.id.contactNumInput);
        licenseNumInput = findViewById(R.id.licenseNumInput);
        licenseExpInput = findViewById(R.id.licenseExpInput);
        licenseRestrictionInput = findViewById(R.id.licenseRestrictionInput);

        cancelBtn = findViewById(R.id.cancelBtn);
        saveBtn = findViewById(R.id.saveBtn);

        cancelBtn.setOnClickListener(v -> finish());
        saveBtn.setOnClickListener(v -> validateAndFetch());
    }

    private void validateAndFetch() {
        // collect and sanitize ♡
        String firstName = sanitize(firstNameInput.getText().toString());
        String middleName = sanitize(middleNameInput.getText().toString());
        String lastName = sanitize(lastNameInput.getText().toString());
        String suffix = sanitize(suffixInput.getText().toString());
        String address = sanitize(addressInput.getText().toString());
        String contactNum = sanitize(contactNumInput.getText().toString());
        String licenseNum = licenseNumInput.getText().toString().trim().toUpperCase();
        String licenseExp = sanitize(licenseExpInput.getText().toString());
        String licenseRestriction = licenseRestrictionInput.getText().toString().trim().toUpperCase();

        // check required fields (suffix is optional)
        if (firstName.isEmpty()) {
            firstNameInput.requestFocus();
            firstNameInput.setError("required ♡");
            return;
        }
        if (lastName.isEmpty()) {
            lastNameInput.requestFocus();
            lastNameInput.setError("required ♡");
            return;
        }
        if (address.isEmpty()) {
            addressInput.requestFocus();
            addressInput.setError("required ♡");
            return;
        }
        if (contactNum.isEmpty()) {
            contactNumInput.requestFocus();
            contactNumInput.setError("required ♡");
            return;
        }
        if (licenseNum.isEmpty()) {
            licenseNumInput.requestFocus();
            licenseNumInput.setError("required ♡");
            return;
        }
        if (licenseExp.isEmpty()) {
            licenseExpInput.requestFocus();
            licenseExpInput.setError("required ♡");
            return;
        }
        if (licenseRestriction.isEmpty()) {
            licenseRestrictionInput.requestFocus();
            licenseRestrictionInput.setError("required ♡");
            return;
        }

        // if all filled, proceed ♡
        fetchAndSaveDriver(firstName, middleName, lastName, suffix, address, contactNum, licenseNum, licenseExp, licenseRestriction);
    }

    private void fetchAndSaveDriver(String firstName, String middleName, String lastName, String suffix,
                                    String address, String contactNum, String licenseNum, String licenseExp, String licenseRestriction) {

        SupabaseService service = SupabaseApiClient.getRetrofitInstance().create(SupabaseService.class);

        Call<List<Map<String, Object>>> call = service.getLastDriverId(
                supabaseKey,
                "Bearer " + supabaseKey,
                "driver_id",
                "driver_id.desc",
                1
        );

        saveBtn.setEnabled(false);
        saveBtn.setText("checking...");

        call.enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    String lastId = response.body().get(0).get("driver_id").toString();
                    String newDriverId = generateNextDriverId(lastId);
                    saveDriver(newDriverId, firstName, middleName, lastName, suffix, address, contactNum, licenseNum, licenseExp, licenseRestriction);
                } else {
                    saveDriver("DR0001", firstName, middleName, lastName, suffix, address, contactNum, licenseNum, licenseExp, licenseRestriction);
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(AddDriverActivity.this, "failed to fetch latest id", Toast.LENGTH_SHORT).show();
                saveBtn.setEnabled(true);
                saveBtn.setText("save");
            }
        });
    }

    private String generateNextDriverId(String lastId) {
        try {
            int num = Integer.parseInt(lastId.replaceAll("[^0-9]", "")) + 1;
            return String.format("DR%04d", num);
        } catch (Exception e) {
            return "DR0001";
        }
    }

    private void saveDriver(String driverId, String firstName, String middleName, String lastName, String suffix,
                            String address, String contactNum, String licenseNum, String licenseExp, String licenseRestriction) {

        // generate password (same as driver id)
        String plainPassword = driverId;

        // hash password
        String hashedPassword = BCrypt.withDefaults().hashToString(12, plainPassword.toCharArray());

        Map<String, Object> driverData = new HashMap<>();
        driverData.put("driver_id", driverId);
        driverData.put("first_name", firstName);
        driverData.put("middle_name", middleName);
        driverData.put("last_name", lastName);
        driverData.put("suffix", suffix);
        driverData.put("address", address);
        driverData.put("contact_num", contactNum);
        driverData.put("license_num", licenseNum);
        driverData.put("license_expiration", licenseExp);
        driverData.put("license_restriction", licenseRestriction);
        driverData.put("password", hashedPassword); // save hashed password ♡

        SupabaseService service = SupabaseApiClient.getRetrofitInstance().create(SupabaseService.class);
        Call<Void> call = service.addDriver(driverData);

        saveBtn.setText("saving...");

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                saveBtn.setEnabled(true);
                saveBtn.setText("save");

                if (response.isSuccessful()) {
                    Toast.makeText(AddDriverActivity.this, "driver added successfully ♡", Toast.LENGTH_SHORT).show();
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("driver_added", true);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    Toast.makeText(AddDriverActivity.this, "failed to save driver (server error)", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                saveBtn.setEnabled(true);
                saveBtn.setText("save");
                Toast.makeText(AddDriverActivity.this, "error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String sanitize(String value) {
        if (value == null || value.trim().isEmpty()) return "";
        return capitalizeWords(value.trim());
    }

    // helper: capitalize each word
    private String capitalizeWords(String input) {
        if (input.isEmpty()) return "";
        String[] words = input.toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            sb.append(Character.toUpperCase(w.charAt(0)))
                    .append(w.substring(1))
                    .append(" ");
        }
        return sb.toString().trim();
    }
}

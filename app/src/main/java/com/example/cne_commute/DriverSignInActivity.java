package com.example.cne_commute;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import at.favre.lib.crypto.bcrypt.BCrypt;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverSignInActivity extends AppCompatActivity {

    private EditText driverIDInput, passwordInput;
    private ImageView passwordEyeIcon;
    private Button signInButton;
    private boolean isPasswordVisible = false;

    private SupabaseService supabaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_sign_in);

        driverIDInput = findViewById(R.id.driverID);
        passwordInput = findViewById(R.id.passwordInput);
        passwordEyeIcon = findViewById(R.id.password_eye_icon);
        signInButton = findViewById(R.id.signInButton);

        supabaseService = SupabaseApiClient.getRetrofitInstance().create(SupabaseService.class);

        passwordEyeIcon.setOnClickListener(v -> togglePasswordVisibility());

        signInButton.setOnClickListener(v -> {
            String driverId = driverIDInput.getText().toString().trim();
            String password = passwordInput.getText().toString();

            if (driverId.isEmpty()) {
                Toast.makeText(this, "Please enter Driver ID", Toast.LENGTH_SHORT).show();
            } else if (password.isEmpty()) {
                Toast.makeText(this, "Please enter Password", Toast.LENGTH_SHORT).show();
            } else {
                loginDriver(driverId, password);
            }
        });
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordEyeIcon.setImageResource(R.drawable.ic_eye_off);
        } else {
            passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            passwordEyeIcon.setImageResource(R.drawable.ic_eye_on);
        }
        passwordInput.setSelection(passwordInput.getText().length());
        isPasswordVisible = !isPasswordVisible;
    }

    private void loginDriver(String driverId, String password) {
        String apiKey = BuildConfig.SUPABASE_API_KEY;
        String authHeader = "Bearer " + apiKey;

        String filter = "eq." + driverId; // matches @Query("driver_id")

        supabaseService.getDriverById(apiKey, authHeader, filter)
                .enqueue(new Callback<List<Driver>>() {
                    @Override
                    public void onResponse(Call<List<Driver>> call, Response<List<Driver>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            Driver driver = response.body().get(0);

                            boolean isVerified = false;

                            // strict password enforcement
                            if (driver.password != null && !driver.password.isEmpty()) {
                                isVerified = BCrypt.verifyer()
                                        .verify(password.toCharArray(), driver.password)
                                        .verified;
                            }

                            if (isVerified) {
                                Intent intent = new Intent(DriverSignInActivity.this, DriverHomeActivity.class);
                                intent.putExtra("driverID", driverId);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                Toast.makeText(DriverSignInActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(DriverSignInActivity.this, "Driver not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Driver>> call, Throwable t) {
                        Toast.makeText(DriverSignInActivity.this, "Login failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}

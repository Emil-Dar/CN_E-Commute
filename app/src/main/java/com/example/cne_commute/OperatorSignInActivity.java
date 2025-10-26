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

public class OperatorSignInActivity extends AppCompatActivity {

    private EditText operatorIdInput, passwordInput;
    private ImageView passwordEyeIcon;
    private Button signInButton;
    private boolean isPasswordVisible = false;

    private SupabaseService supabaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_sign_in);

        operatorIdInput = findViewById(R.id.operatorIdInput);
        passwordInput = findViewById(R.id.passwordInput);
        passwordEyeIcon = findViewById(R.id.password_eye_icon);
        signInButton = findViewById(R.id.signInButton);

        supabaseService = SupabaseApiClient.getRetrofitInstance().create(SupabaseService.class);

        passwordEyeIcon.setOnClickListener(v -> togglePasswordVisibility());

        signInButton.setOnClickListener(v -> {
            String operatorId = operatorIdInput.getText().toString().trim();
            String password = passwordInput.getText().toString();

            if (operatorId.isEmpty()) {
                Toast.makeText(this, "Please enter Operator ID", Toast.LENGTH_SHORT).show();
            } else if (password.isEmpty()) {
                Toast.makeText(this, "Please enter Password", Toast.LENGTH_SHORT).show();
            } else {
                loginOperator(operatorId, password);
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

    private void loginOperator(String operatorId, String password) {
        String apiKey = BuildConfig.SUPABASE_API_KEY;
        String authHeader = "Bearer " + apiKey;

        String filter = "eq." + operatorId; // matches @Query("operator_id")

        supabaseService.getOperatorById(apiKey, authHeader, filter)
                .enqueue(new Callback<List<Operator>>() {
                    @Override
                    public void onResponse(Call<List<Operator>> call, Response<List<Operator>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            Operator operator = response.body().get(0);

                            boolean isVerified = false;

                            // strict password enforcement
                            if (operator.getPassword() != null && !operator.getPassword().isEmpty()) {
                                isVerified = BCrypt.verifyer()
                                        .verify(password.toCharArray(), operator.getPassword())
                                        .verified;
                            }

                            if (isVerified) {
                                Intent intent = new Intent(OperatorSignInActivity.this, OperatorHomeActivity.class);
                                intent.putExtra("operatorId", operatorId);
                                finish();
                                startActivity(intent);
                            } else {
                                Toast.makeText(OperatorSignInActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(OperatorSignInActivity.this, "Operator not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Operator>> call, Throwable t) {
                        Toast.makeText(OperatorSignInActivity.this, "Login failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}

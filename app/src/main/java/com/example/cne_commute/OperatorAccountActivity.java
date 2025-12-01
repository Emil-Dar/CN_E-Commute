package com.example.cne_commute;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OperatorAccountActivity extends AppCompatActivity {

    private TextView nameTextView, contactTextView, logoutText;
    private EditText firstNameEditText, lastNameEditText, suffixEditText,
            contactEditText, addressEditText;
    private Button editButton, saveButton, changePasswordButton;

    private String operatorId;
    private SupabaseService apiService;
    private final String SUPABASE_API_KEY = BuildConfig.SUPABASE_API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_account);

        operatorId = getIntent().getStringExtra("operatorId");
        apiService = SupabaseApiClient.getRetrofitInstance().create(SupabaseService.class);

        // init views
        nameTextView = findViewById(R.id.name_textview);
        contactTextView = findViewById(R.id.contact_textview);
        firstNameEditText = findViewById(R.id.first_name_edittext);
        lastNameEditText = findViewById(R.id.last_name_edittext);
        suffixEditText = findViewById(R.id.suffix_edittext);
        contactEditText = findViewById(R.id.contact_edittext);
        addressEditText = findViewById(R.id.address_edittext);
        editButton = findViewById(R.id.edit_button);
        saveButton = findViewById(R.id.save_button);
        changePasswordButton = findViewById(R.id.change_password_button);
        logoutText = findViewById(R.id.logout_text);

        loadOperatorData();

        editButton.setOnClickListener(v -> setEditMode(true));
        saveButton.setOnClickListener(v -> saveOperatorData());
        changePasswordButton.setOnClickListener(v -> showChangePasswordDialog());
        logoutText.setOnClickListener(v -> logoutUser());

        setupBottomNavigation();
    }

    // helper method to get full name
    private String getFullName(Operator operator) {
        String first = operator.getFirstName() != null ? operator.getFirstName() : "";
        String last = operator.getLastName() != null ? operator.getLastName() : "";
        String fullName = first + " " + last;
        if (operator.getSuffix() != null && !operator.getSuffix().isEmpty()) {
            fullName += " " + operator.getSuffix();
        }
        return fullName.trim();
    }

    private void loadOperatorData() {
        apiService.getOperatorById(
                SUPABASE_API_KEY,
                "Bearer " + SUPABASE_API_KEY,
                "eq." + operatorId
        ).enqueue(new Callback<List<Operator>>() {
            @Override
            public void onResponse(Call<List<Operator>> call, Response<List<Operator>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Operator operator = response.body().get(0);

                    nameTextView.setText(getFullName(operator));
                    contactTextView.setText(operator.getContactNum());

                    firstNameEditText.setText(operator.getFirstName());
                    lastNameEditText.setText(operator.getLastName());
                    suffixEditText.setText(operator.getSuffix() != null ? operator.getSuffix() : "");
                    contactEditText.setText(operator.getContactNum());
                    addressEditText.setText(operator.getAddress());
                } else {
                    Toast.makeText(OperatorAccountActivity.this, "Operator not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Operator>> call, Throwable t) {
                Toast.makeText(OperatorAccountActivity.this, "Failed to load operator profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveOperatorData() {
        Map<String, Object> updates = new HashMap<>();

        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String suffix = suffixEditText.getText().toString().trim();
        String contactNum = contactEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();

        // use JsonNull.INSTANCE for empty fields to delete in Supabase
        updates.put("first_name", firstName.isEmpty() ? com.google.gson.JsonNull.INSTANCE : firstName);
        updates.put("last_name", lastName.isEmpty() ? com.google.gson.JsonNull.INSTANCE : lastName);
        updates.put("suffix", suffix.isEmpty() ? com.google.gson.JsonNull.INSTANCE : suffix);
        updates.put("contact_num", contactNum.isEmpty() ? com.google.gson.JsonNull.INSTANCE : contactNum);
        updates.put("address", address.isEmpty() ? com.google.gson.JsonNull.INSTANCE : address);

        apiService.updateOperator(
                        SUPABASE_API_KEY,
                        "Bearer " + SUPABASE_API_KEY,
                        "eq." + operatorId,
                        updates
                )
                .enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // update local UI
                    Operator updatedOperator = new Operator();
                    updatedOperator.setFirstName(firstName.isEmpty() ? null : firstName);
                    updatedOperator.setLastName(lastName.isEmpty() ? null : lastName);
                    updatedOperator.setSuffix(suffix.isEmpty() ? null : suffix);
                    updatedOperator.setContactNum(contactNum.isEmpty() ? null : contactNum);
                    updatedOperator.setAddress(address.isEmpty() ? null : address);

                    nameTextView.setText(getFullName(updatedOperator));
                    contactTextView.setText(contactNum.isEmpty() ? "" : contactNum);
                    setEditMode(false);
                    Toast.makeText(OperatorAccountActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(OperatorAccountActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(OperatorAccountActivity.this, "Error updating profile", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showChangePasswordDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.password_dialog, null);
        EditText currentPasswordEdit = dialogView.findViewById(R.id.dialog_current_password);
        EditText newPasswordEdit = dialogView.findViewById(R.id.dialog_password);
        EditText confirmPasswordEdit = dialogView.findViewById(R.id.dialog_confirm_password);
        Button saveButton = dialogView.findViewById(R.id.dialog_save_button);
        Button cancelButton = dialogView.findViewById(R.id.dialog_cancel_button);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        saveButton.setOnClickListener(v -> {
            String currentPassword = currentPasswordEdit.getText().toString().trim();
            String newPassword = newPasswordEdit.getText().toString().trim();
            String confirm = confirmPasswordEdit.getText().toString().trim();

            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirm)) {
                Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // first load operator from db to check current password
            apiService.getOperatorById(
                    SUPABASE_API_KEY,
                    "Bearer " + SUPABASE_API_KEY,
                    "eq." + operatorId
            ).enqueue(new Callback<List<Operator>>() {
                @Override
                public void onResponse(Call<List<Operator>> call, Response<List<Operator>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        Operator operator = response.body().get(0);

                        String storedHashed = operator.getPassword();
                        boolean isVerified = false;

                        if (storedHashed != null && !storedHashed.isEmpty()) {
                            isVerified = at.favre.lib.crypto.bcrypt.BCrypt.verifyer()
                                    .verify(currentPassword.toCharArray(), storedHashed)
                                    .verified;
                        }

                        if (!isVerified) {
                            Toast.makeText(OperatorAccountActivity.this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // secure hash new password
                        String hashedNewPassword = at.favre.lib.crypto.bcrypt.BCrypt.withDefaults()
                                .hashToString(12, newPassword.toCharArray());

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("password", hashedNewPassword);

                        apiService.updateOperator(
                                SUPABASE_API_KEY,
                                "Bearer " + SUPABASE_API_KEY,
                                "eq." + operatorId,
                                updates
                        ).enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(OperatorAccountActivity.this, "Password updated", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(OperatorAccountActivity.this, "Failed to update password", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Toast.makeText(OperatorAccountActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(OperatorAccountActivity.this, "Failed to verify current password", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<Operator>> call, Throwable t) {
                    Toast.makeText(OperatorAccountActivity.this, "Error verifying password", Toast.LENGTH_SHORT).show();
                }
            });
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void setEditMode(boolean editable) {
        firstNameEditText.setVisibility(editable ? View.VISIBLE : View.GONE);
        lastNameEditText.setVisibility(editable ? View.VISIBLE : View.GONE);
        suffixEditText.setVisibility(editable ? View.VISIBLE : View.GONE);
        contactEditText.setVisibility(editable ? View.VISIBLE : View.GONE);
        addressEditText.setVisibility(editable ? View.VISIBLE : View.GONE);

        nameTextView.setVisibility(editable ? View.GONE : View.VISIBLE);
        contactTextView.setVisibility(editable ? View.GONE : View.VISIBLE);

        editButton.setVisibility(editable ? View.GONE : View.VISIBLE);
        saveButton.setVisibility(editable ? View.VISIBLE : View.GONE);
    }

    private void logoutUser() {
        // clear saved login
        getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                .edit()
                .clear()
                .apply();

        // go back to role selection
        Intent intent = new Intent(OperatorAccountActivity.this, UserRoleSelectionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_account);

        bottomNav.setOnItemSelectedListener(item -> {
            Intent intent = null;
            switch (item.getItemId()) {
                case R.id.nav_home:
                    intent = new Intent(this, OperatorHomeActivity.class);
                    break;
                case R.id.nav_franchise:
                    intent = new Intent(this, OperatorFranchiseActivity.class);
                    break;
                case R.id.nav_notification:
                    intent = new Intent(this, OperatorNotificationActivity.class);
                    break;
                case R.id.nav_account:
                    return true;
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

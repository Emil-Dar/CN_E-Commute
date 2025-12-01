package com.example.cne_commute.driver_fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.cne_commute.Driver;
import com.example.cne_commute.R;
import com.example.cne_commute.SupabaseApiClient;
import com.example.cne_commute.SupabaseService;
import com.example.cne_commute.UserRoleSelectionActivity;

import java.util.*;

import at.favre.lib.crypto.bcrypt.BCrypt;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverAccount extends Fragment {

    // DISPLAY TEXTS
    private TextView driverIdText, nameTextView, contactTextView, logoutText;
    private Button editButton, changePasswordButton;

    private String driverId;
    private SupabaseService apiService;
    private final String SUPABASE_API_KEY = SupabaseApiClient.SUPABASE_API_KEY;

    // MUNICIPALITY → BARANGAYS MAP
    private final Map<String, List<String>> municipalityBarangays = new HashMap<String, List<String>>() {{
        put("Daet", Arrays.asList("Awitan", "Bagasbas", "Barangay I (Pob.)", "Barangay II (Pob.)", "Barangay III (Pob.)"));
        put("JosePanganiban", Arrays.asList("Calero", "Larap", "Motherlode", "North Poblacion", "South Poblacion"));
        put("Labo", Arrays.asList("Anahaw", "Bagacay", "Exciban", "Guisican", "Malasugui"));
        put("Mercedes", Arrays.asList("Apuao", "Caringo", "Catandunganon", "Pambuhan", "San Roque"));
        put("Paracale", Arrays.asList("Bagumbayan", "Calabasa", "Dalnac", "Mabini", "Poblacion Norte"));
    }};

    public DriverAccount() {}

    public static DriverAccount newInstance(String driverId) {
        DriverAccount fragment = new DriverAccount();
        Bundle args = new Bundle();
        args.putString("driverId", driverId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_driver_account, container, false);

        // Load driverId
        if (getArguments() != null)
            driverId = getArguments().getString("driverId");

        if (driverId == null) {
            driverId = requireContext()
                    .getSharedPreferences("MyAppPrefs", android.content.Context.MODE_PRIVATE)
                    .getString("driverId", null);
        }

        if (driverId != null) {
            requireContext()
                    .getSharedPreferences("MyAppPrefs", android.content.Context.MODE_PRIVATE)
                    .edit().putString("driverId", driverId).apply();
        }

        apiService = SupabaseApiClient.getRetrofitInstance().create(SupabaseService.class);

        // BIND DISPLAY VIEWS
        driverIdText = view.findViewById(R.id.driver_ID);
        nameTextView = view.findViewById(R.id.driver_account);
        contactTextView = view.findViewById(R.id.contact_textview);

        editButton = view.findViewById(R.id.edit_button);
        changePasswordButton = view.findViewById(R.id.change_password_button);
        logoutText = view.findViewById(R.id.logout_text);

        driverIdText.setText(driverId != null ? driverId : "N/A");

        loadDriverData();

        editButton.setOnClickListener(v -> showEditDialog());
        changePasswordButton.setOnClickListener(v -> showChangePasswordDialog());
        logoutText.setOnClickListener(v -> logoutUser());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDriverData();
    }

    // ===============================================================
    //  LOAD DRIVER PROFILE (TEXTVIEWS ONLY)
    // ===============================================================
    private void loadDriverData() {

        apiService.getDriverById(
                SUPABASE_API_KEY,
                "Bearer " + SUPABASE_API_KEY,
                "eq." + driverId
        ).enqueue(new Callback<List<Driver>>() {
            @Override
            public void onResponse(Call<List<Driver>> call, Response<List<Driver>> response) {

                if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                    Toast.makeText(requireContext(), "Driver not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                Driver driver = response.body().get(0);

                nameTextView.setText(getFullName(driver));
                contactTextView.setText(driver.getContactNum());
            }

            @Override
            public void onFailure(Call<List<Driver>> call, Throwable t) {
                Toast.makeText(requireContext(), "Load failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ===============================================================
    //  EDIT PROFILE DIALOG
    // ===============================================================
    private void showEditDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.edit_profile_dialog, null);

        // DIALOG FIELDS
        EditText firstNameInput = dialogView.findViewById(R.id.dialog_first_name);
        EditText middleNameInput = dialogView.findViewById(R.id.dialog_middle_name);
        EditText lastNameInput = dialogView.findViewById(R.id.dialog_last_name);
        EditText contactInput = dialogView.findViewById(R.id.dialog_contact);

        EditText houseNoInput = dialogView.findViewById(R.id.dialog_house_no);
        EditText streetInput = dialogView.findViewById(R.id.dialog_street);
        Spinner barangaySpinner = dialogView.findViewById(R.id.dialog_barangay);
        Spinner municipalitySpinner = dialogView.findViewById(R.id.dialog_municipality);

        TextView provinceText = dialogView.findViewById(R.id.dialog_province);
        provinceText.setText("Camarines Norte");

        Button saveBtn = dialogView.findViewById(R.id.dialog_save_button);
        Button cancelBtn = dialogView.findViewById(R.id.dialog_cancel_button);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        // ------------------ Load municipalities ------------------
        ArrayAdapter<String> municipalityAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new ArrayList<>(municipalityBarangays.keySet())
        );
        municipalityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        municipalitySpinner.setAdapter(municipalityAdapter);

// ------------------ Get driver data ------------------
        apiService.getDriverById(
                SUPABASE_API_KEY,
                "Bearer " + SUPABASE_API_KEY,
                "eq." + driverId
        ).enqueue(new Callback<List<Driver>>() {
            @Override
            public void onResponse(Call<List<Driver>> call, Response<List<Driver>> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) return;

                Driver d = response.body().get(0);

                firstNameInput.setText(d.getFirstName());
                middleNameInput.setText(d.getMiddleName());
                lastNameInput.setText(d.getLastName());
                contactInput.setText(d.getContactNum());

                // Parse Address
                String address = d.getAddress();
                if (address != null && !address.isEmpty()) {
                    String[] parts = address.split(",");
                    houseNoInput.setText(parts.length > 0 ? parts[0].trim() : "");
                    streetInput.setText(parts.length > 1 ? parts[1].trim() : "");

                    String brgy = parts.length > 2 ? parts[2].trim() : "";
                    String muni = parts.length > 3 ? parts[3].trim() : "";

                    // ✅ Set Municipality first
                    selectSpinnerValue(municipalitySpinner, muni);

                    // ✅ Then populate Barangay spinner based on Municipality
                    List<String> brgys = municipalityBarangays.getOrDefault(muni, new ArrayList<>());
                    ArrayAdapter<String> brgyAdapter = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            brgys
                    );
                    brgyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    barangaySpinner.setAdapter(brgyAdapter);

                    // ✅ Set Barangay after adapter is ready
                    selectSpinnerValue(barangaySpinner, brgy);
                }
            }

            @Override
            public void onFailure(Call<List<Driver>> call, Throwable t) {}
        });


        // SAVE BUTTON
        saveBtn.setOnClickListener(v -> {

            String fullAddress =
                    houseNoInput.getText().toString().trim() + "," +
                            streetInput.getText().toString().trim() + "," +
                            barangaySpinner.getSelectedItem().toString() + "," +
                            municipalitySpinner.getSelectedItem().toString() + ",Camarines Norte";

            Map<String, Object> updates = new HashMap<>();
            updates.put("first_name", firstNameInput.getText().toString());
            updates.put("middle_name", middleNameInput.getText().toString());
            updates.put("last_name", lastNameInput.getText().toString());
            updates.put("contact_num", contactInput.getText().toString());
            updates.put("address", fullAddress);

            apiService.updateDriver(
                    SUPABASE_API_KEY,
                    "Bearer " + SUPABASE_API_KEY,
                    "eq." + driverId,
                    updates
            ).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                    loadDriverData();
                    dialog.dismiss();
                }

                @Override public void onFailure(Call<Void> call, Throwable t) {}
            });

        });

        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void selectSpinnerValue(Spinner spinner, String value) {
        if (value == null) return;

        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    // ===============================================================
    //  PASSWORD CHANGE DIALOG (unchanged)
    // ===============================================================
    private void showChangePasswordDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.password_dialog, null);

        EditText currentPasswordEdit = dialogView.findViewById(R.id.dialog_current_password);
        EditText newPasswordEdit = dialogView.findViewById(R.id.dialog_password);
        EditText confirmPasswordEdit = dialogView.findViewById(R.id.dialog_confirm_password);

        Button dialogSave = dialogView.findViewById(R.id.dialog_save_button);
        Button dialogCancel = dialogView.findViewById(R.id.dialog_cancel_button);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        dialogSave.setOnClickListener(v -> {

            String currentPass = currentPasswordEdit.getText().toString().trim();
            String newPass = newPasswordEdit.getText().toString().trim();
            String confirmPass = confirmPasswordEdit.getText().toString().trim();

            if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(requireContext(), "Complete all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPass.equals(confirmPass)) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            verifyAndChangePassword(currentPass, newPass, dialog);
        });

        dialogCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void verifyAndChangePassword(String currentPassword, String newPassword, AlertDialog dialog) {

        apiService.getDriverById(
                SUPABASE_API_KEY,
                "Bearer " + SUPABASE_API_KEY,
                "eq." + driverId
        ).enqueue(new Callback<List<Driver>>() {
            @Override
            public void onResponse(Call<List<Driver>> call, Response<List<Driver>> response) {

                if (!response.isSuccessful() || response.body() == null || response.body().isEmpty()) {
                    Toast.makeText(requireContext(), "Driver not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                Driver driver = response.body().get(0);
                String storedHash = driver.getPassword();

                boolean verified = storedHash != null &&
                        BCrypt.verifyer().verify(currentPassword.toCharArray(), storedHash).verified;

                if (!verified) {
                    Toast.makeText(requireContext(), "Incorrect current password", Toast.LENGTH_SHORT).show();
                    return;
                }

                String newHash = BCrypt.withDefaults().hashToString(12, newPassword.toCharArray());

                Map<String, Object> update = new HashMap<>();
                update.put("password", newHash);

                apiService.updateDriver(
                        SUPABASE_API_KEY,
                        "Bearer " + SUPABASE_API_KEY,
                        "eq." + driverId,
                        update
                ).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), "Password updated", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(requireContext(), "Failed to update password", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override public void onFailure(Call<Void> call, Throwable t) {}
                });

            }

            @Override public void onFailure(Call<List<Driver>> call, Throwable t) {}
        });
    }

    // ===============================================================
    //  LOGOUT
    // ===============================================================
    private void logoutUser() {
        requireContext().getSharedPreferences("MyAppPrefs", android.content.Context.MODE_PRIVATE)
                .edit().clear().apply();

        Intent intent = new Intent(requireContext(), UserRoleSelectionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    // ===============================================================
    //  FULL NAME FORMATTER
    // ===============================================================
    private String getFullName(Driver driver) {
        return (driver.getFirstName() + " " +
                (driver.getMiddleName() == null ? "" : driver.getMiddleName()) + " " +
                driver.getLastName())
                .trim().replaceAll("\\s+", " ");
    }
}

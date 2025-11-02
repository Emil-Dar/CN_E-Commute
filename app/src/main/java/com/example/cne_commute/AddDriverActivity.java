package com.example.cne_commute;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import at.favre.lib.crypto.bcrypt.BCrypt;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.*;

public class AddDriverActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1001;
    private static final int CAMERA_REQUEST_CODE = 1002;
    private static final int PERMISSION_CODE = 1003;

    private TextInputEditText firstNameInput, middleNameInput, lastNameInput, suffixInput;
    private TextInputEditText birthdateInput, houseNoInput, streetInput, contactNumInput;
    private TextInputEditText licenseNumInput, licenseExpInput, licenseRestrictionInput;
    private Spinner barangayDropdown, municipalityDropdown;
    private Button cancelBtn, saveBtn, uploadLicenseBtn, openCameraBtn;
    private ImageButton removeImageBtn;
    private ImageView licensePreview;

    private String currentOperatorId;

    private Uri selectedImageUri;
    private String uploadedLicenseUrl = null;

    private final String supabaseUrl = SupabaseApiClient.getProjectUrl();
    private final String supabaseKey = SupabaseApiClient.getApiKey();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_driver);

        // Initialize views
        firstNameInput = findViewById(R.id.firstNameInput);
        middleNameInput = findViewById(R.id.middleNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        suffixInput = findViewById(R.id.suffixInput);
        birthdateInput = findViewById(R.id.birthdateInput);
        houseNoInput = findViewById(R.id.houseNoInput);
        streetInput = findViewById(R.id.streetInput);
        contactNumInput = findViewById(R.id.contactNumInput);
        licenseNumInput = findViewById(R.id.licenseNumInput);
        licenseExpInput = findViewById(R.id.licenseExpInput);
        licenseRestrictionInput = findViewById(R.id.licenseRestrictionInput);
        barangayDropdown = findViewById(R.id.barangayDropdown);
        municipalityDropdown = findViewById(R.id.municipalityDropdown);
        cancelBtn = findViewById(R.id.cancelBtn);
        saveBtn = findViewById(R.id.saveBtn);
        uploadLicenseBtn = findViewById(R.id.uploadLicenseBtn);
        openCameraBtn = findViewById(R.id.openCameraBtn);
        licensePreview = findViewById(R.id.licensePreview);
        removeImageBtn = findViewById(R.id.removeImageBtn);

        // Retrieve operator_id from SharedPreferences (or session)
        currentOperatorId = getSharedPreferences("UserSession", MODE_PRIVATE)
                .getString("operator_id", null);


        // Dropdown setup
        ArrayAdapter<String> barangayAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                new String[]{"Select Barangay", "Barangay I", "Barangay II", "Barangay III", "Barangay IV", "Barangay V", "Barangay VI", "Barangay VII"});
        barangayDropdown.setAdapter(barangayAdapter);

        ArrayAdapter<String> municipalityAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                new String[]{"Select Municipality", "Daet", "Basud", "Labo"});
        municipalityDropdown.setAdapter(municipalityAdapter);

        // Birthdate picker
        birthdateInput.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            android.app.DatePickerDialog datePicker = new android.app.DatePickerDialog(
                    AddDriverActivity.this,
                    (view, y, m, d) -> birthdateInput.setText(String.format("%04d-%02d-%02d", y, m + 1, d)),
                    year, month, day
            );
            datePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePicker.show();
        });

        licenseExpInput.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            android.app.DatePickerDialog datePicker = new android.app.DatePickerDialog(
                    AddDriverActivity.this,
                    (view, y, m, d) -> licenseExpInput.setText(
                            String.format("%04d-%02d-%02d", y, m + 1, d)
                    ),
                    year, month, day
            );

            Calendar maxCalendar = Calendar.getInstance();
            maxCalendar.add(Calendar.YEAR, 10);
            datePicker.getDatePicker().setMinDate(System.currentTimeMillis()); // can’t expire before today
            datePicker.getDatePicker().setMaxDate(maxCalendar.getTimeInMillis()); // up to 10 years ahead

            datePicker.show();
        });




        cancelBtn.setOnClickListener(v -> finish());
        uploadLicenseBtn.setOnClickListener(v -> openImagePicker());
        openCameraBtn.setOnClickListener(v -> openCamera());
        removeImageBtn.setOnClickListener(v -> removeImage());
        saveBtn.setOnClickListener(v -> validateAndFetch());
    }

    // region 📸 CAMERA + GALLERY
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, PERMISSION_CODE);
        } else {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "New Picture");
            values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
            selectedImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImageUri);
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        }
    }

    private void removeImage() {
        licensePreview.setImageDrawable(null);
        removeImageBtn.setVisibility(ImageButton.GONE);
        uploadedLicenseUrl = null;
        selectedImageUri = null;
        Toast.makeText(this, "Image removed", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null) {
                selectedImageUri = data.getData();
            }
            if (selectedImageUri != null) {
                licensePreview.setImageURI(selectedImageUri);
                removeImageBtn.setVisibility(ImageButton.VISIBLE);
                uploadLicenseToSupabase(selectedImageUri);
            }
        }
    }

    // endregion

    // region ☁️ SUPABASE UPLOAD
    private void uploadLicenseToSupabase(Uri uri) {
        if (uri == null) return;

        String fileName = "license_" + System.currentTimeMillis() + ".jpg";
        String bucket = "driver_licenses";
        String uploadUrl = "https://rtwrbkrroilftdhggxjc.supabase.co/storage/v1/object/" + bucket + "/" + fileName;

        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            byte[] bytes = readBytes(inputStream);

            RequestBody body = RequestBody.create(bytes, MediaType.parse("image/jpeg"));
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(uploadUrl)
                    .header("Authorization", "Bearer " + supabaseKey)
                    .header("Content-Type", "image/jpeg")
                    .put(body)
                    .build();

            okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();

            uploadLicenseBtn.setText("Uploading...");
            uploadLicenseBtn.setEnabled(false);

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                    runOnUiThread(() -> {
                        uploadLicenseBtn.setEnabled(true);
                        uploadLicenseBtn.setText("Upload License Photo");
                    });

                    if (response.isSuccessful()) {
                        uploadedLicenseUrl = "https://rtwrbkrroilftdhggxjc.supabase.co/storage/v1/object/public/"
                                + bucket + "/" + fileName;
                        runOnUiThread(() -> Toast.makeText(AddDriverActivity.this,
                                "License uploaded successfully ♡", Toast.LENGTH_SHORT).show());
                    } else {
                        runOnUiThread(() -> Toast.makeText(AddDriverActivity.this,
                                "Upload failed: " + response.code(), Toast.LENGTH_SHORT).show());
                    }
                }

                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    runOnUiThread(() -> {
                        uploadLicenseBtn.setEnabled(true);
                        uploadLicenseBtn.setText("Upload License Photo");
                        Toast.makeText(AddDriverActivity.this,
                                "Upload error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to read image file", Toast.LENGTH_SHORT).show();
        }
    }




    private byte[] readBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[4096];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

    private String getFileExtension(Uri uri) {
        ContentResolver resolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(resolver.getType(uri));
    }

    // endregion

    // region 🧹 Permissions
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
        }
    }


    // endregion

    // region 💾 VALIDATION + SAVE FLOW
    private void validateAndFetch() {
        String firstName = sanitize(firstNameInput.getText().toString());
        String middleName = sanitize(middleNameInput.getText().toString());
        String lastName = sanitize(lastNameInput.getText().toString());
        String suffix = sanitize(suffixInput.getText().toString());
        String birthdate = sanitize(birthdateInput.getText().toString());
        String houseNo = sanitize(houseNoInput.getText().toString());
        String street = sanitize(streetInput.getText().toString());
        String barangay = barangayDropdown.getSelectedItem() != null ? barangayDropdown.getSelectedItem().toString() : "";
        String municipality = municipalityDropdown.getSelectedItem() != null ? municipalityDropdown.getSelectedItem().toString() : "";
        String contactNum = sanitize(contactNumInput.getText().toString());
        String licenseNum = licenseNumInput.getText().toString().trim().toUpperCase();
        String licenseExp = sanitize(licenseExpInput.getText().toString());
        String licenseRestriction = licenseRestrictionInput.getText().toString().trim().toUpperCase();

        // Required field validation
        if (firstName.isEmpty()) { firstNameInput.setError("Required ♡"); return; }
        if (lastName.isEmpty()) { lastNameInput.setError("Required ♡"); return; }
        if (birthdate.isEmpty()) { birthdateInput.setError("Required ♡"); return; }
        if (houseNo.isEmpty()) { houseNoInput.setError("Required ♡"); return; }
        if (street.isEmpty()) { streetInput.setError("Required ♡"); return; }
        if (barangay.isEmpty()) { Toast.makeText(this, "Please select a barangay ♡", Toast.LENGTH_SHORT).show(); return; }
        if (municipality.isEmpty()) { Toast.makeText(this, "Please select a municipality ♡", Toast.LENGTH_SHORT).show(); return; }
        if (contactNum.isEmpty()) { contactNumInput.setError("Required ♡"); return; }
        if (licenseNum.isEmpty()) { licenseNumInput.setError("Required ♡"); return; }
        if (licenseExp.isEmpty()) { licenseExpInput.setError("Required ♡"); return; }
        if (licenseRestriction.isEmpty()) { licenseRestrictionInput.setError("Required ♡"); return; }
        if (uploadedLicenseUrl == null) {
            Toast.makeText(this, "Please upload a proof of license ♡", Toast.LENGTH_SHORT).show();
            uploadLicenseBtn.requestFocus();
            return;
        }

        String fullAddress = houseNo + ", " + street + ", " + barangay + ", " + municipality + ", Camarines Norte";

        fetchAndSaveDriver(firstName, middleName, lastName, suffix, birthdate,
                fullAddress, contactNum, licenseNum, licenseExp, licenseRestriction);
    }

    private void fetchAndSaveDriver(String firstName, String middleName, String lastName, String suffix, String birthdate,
                                    String address, String contactNum, String licenseNum, String licenseExp, String licenseRestriction) {

        SupabaseService service = SupabaseApiClient.getRetrofitInstance().create(SupabaseService.class);
        Call<List<Map<String, Object>>> call = service.getLastDriverId(
                supabaseKey, "Bearer " + supabaseKey, "driver_id", "driver_id.desc", 1
        );

        saveBtn.setEnabled(false);
        saveBtn.setText("Checking...");

        call.enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    String lastId = response.body().get(0).get("driver_id").toString();
                    String newDriverId = generateNextDriverId(lastId);
                    saveDriver(newDriverId, firstName, middleName, lastName, suffix, birthdate,
                            address, contactNum, licenseNum, licenseExp, licenseRestriction);
                } else {
                    saveDriver("DR0001", firstName, middleName, lastName, suffix, birthdate,
                            address, contactNum, licenseNum, licenseExp, licenseRestriction);
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Toast.makeText(AddDriverActivity.this, "Failed to fetch latest ID", Toast.LENGTH_SHORT).show();
                saveBtn.setEnabled(true);
                saveBtn.setText("Save");
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
                            String birthdate, String address, String contactNum, String licenseNum,
                            String licenseExp, String licenseRestriction) {

        String plainPassword = driverId;
        String hashedPassword = BCrypt.withDefaults().hashToString(12, plainPassword.toCharArray());

        // 🕒 Generate current timestamp in Asia/Manila timezone (non-ISO format)
        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Manila"));
        String createdAt = dateFormat.format(new java.util.Date());

        Map<String, Object> driverData = new HashMap<>();
        driverData.put("driver_id", driverId);
        driverData.put("first_name", firstName);
        driverData.put("middle_name", middleName);
        driverData.put("last_name", lastName);
        driverData.put("suffix", suffix);
        driverData.put("birthdate", birthdate);
        driverData.put("address", address);
        driverData.put("contact_num", contactNum);
        driverData.put("license_num", licenseNum);
        driverData.put("license_expiration", licenseExp);
        driverData.put("license_restriction", licenseRestriction);
        driverData.put("password", hashedPassword);
        driverData.put("status", "Pending");
        driverData.put("proof_of_license", uploadedLicenseUrl);
        driverData.put("assigned_by", currentOperatorId);


        driverData.put("created_at", createdAt);

        SupabaseService service = SupabaseApiClient.getRetrofitInstance().create(SupabaseService.class);
        Call<Void> call = service.addDriver(driverData);

        saveBtn.setText("Saving...");

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                saveBtn.setEnabled(true);
                saveBtn.setText("Save");

                if (response.isSuccessful()) {
                    Toast.makeText(AddDriverActivity.this, "Driver added successfully ♡", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK, new Intent().putExtra("driver_added", true));
                    finish();
                } else {
                    Toast.makeText(AddDriverActivity.this, "Failed to save driver (server error)", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                saveBtn.setEnabled(true);
                saveBtn.setText("Save");
                Toast.makeText(AddDriverActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // endregion

    // region ✨ UTILITIES
    private String sanitize(String value) {
        if (value == null || value.trim().isEmpty()) return "";
        return capitalizeWords(value.trim());
    }

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
    // endregion
}

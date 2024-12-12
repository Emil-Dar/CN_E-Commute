package com.example.cne_commute;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.widget.ImageButton;

public class ReportActivity extends AppCompatActivity {

    private ImageView imagePreview;
    private ImageButton removePhotoButton;
    private LinearLayout speedingOptions, operationalOptions, trafficRulesOptions, passengerSafetyViolationsOptions, driverConductOptions;

    // Activity Result Launchers
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ActivityResultLauncher<Intent> takePhotoLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        // Initialize views
        imagePreview = findViewById(R.id.image_preview);
        removePhotoButton = findViewById(R.id.remove_image_button);
        speedingOptions = findViewById(R.id.speeding_violations_options);
        operationalOptions = findViewById(R.id.operational_violations_options);
        trafficRulesOptions = findViewById(R.id.traffic_rule_violations_options);
        passengerSafetyViolationsOptions = findViewById(R.id.passenger_safety_violations_options);
        driverConductOptions = findViewById(R.id.driver_conduct_violations_options);

        ImageView selectFileButton = findViewById(R.id.select_file_button);
        Button openCameraButton = findViewById(R.id.open_camera_button);
        Button submitButton = findViewById(R.id.submit_button);

        // Set up result launchers
        setupActivityResultLaunchers();

        // Set click listeners
        selectFileButton.setOnClickListener(v -> openFileChooser());
        openCameraButton.setOnClickListener(v -> checkCameraPermissionAndOpen());
        submitButton.setOnClickListener(v -> confirmAndSubmitReport());
        removePhotoButton.setOnClickListener(v -> clearImagePreview());

        setupToggleVisibility(findViewById(R.id.speeding_violations_title), speedingOptions);
        setupToggleVisibility(findViewById(R.id.operational_violations_title), operationalOptions);
        setupToggleVisibility(findViewById(R.id.traffic_rule_violations_title), trafficRulesOptions);
        setupToggleVisibility(findViewById(R.id.passenger_safety_violations_title), passengerSafetyViolationsOptions);
        setupToggleVisibility(findViewById(R.id.driver_conduct_violations_title), driverConductOptions);
    }

    private void setupActivityResultLaunchers() {
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri selectedImageUri = result.getData().getData();
                if (selectedImageUri != null) {
                    imagePreview.setImageURI(selectedImageUri);
                    removePhotoButton.setVisibility(View.VISIBLE);
                    showToast("Selected Image: " + selectedImageUri);

                }
            }
        });

        takePhotoLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                // Check if getExtras() is null to avoid NullPointerException
                if (result.getData().getExtras() != null) {
                    Bitmap capturedImage = (Bitmap) result.getData().getExtras().get("data");
                    if (capturedImage != null) {
                        imagePreview.setImageBitmap(capturedImage);
                        removePhotoButton.setVisibility(View.VISIBLE);
                        showToast("Photo captured successfully!");
                    } else {
                        showToast("Failed to capture image.");
                    }
                } else {
                    showToast("No data found in the captured image.");
                }
            }
        });

    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        pickImageLauncher.launch(Intent.createChooser(intent, "Select an image"));
    }

    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0);
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePhotoLauncher.launch(intent);
    }

    private void confirmAndSubmitReport() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Submission")
                .setMessage("Are you sure you want to submit this report?")
                .setPositiveButton("Yes", (dialog, which) -> submitReport())
                .setNegativeButton("No", (dialog, which) -> showToast("Submission canceled."))
                .show();
    }

    private void submitReport() {
        String speedingViolations = extractInputFromSection(speedingOptions);
        String passengerSafetyViolations = extractInputFromSection(passengerSafetyViolationsOptions);
        String operationalViolations = extractInputFromSection(operationalOptions);
        String trafficRuleViolations = extractInputFromSection(trafficRulesOptions);
        String driverConductViolations = extractInputFromSection(driverConductOptions);

        if (isAllEmpty(speedingViolations, passengerSafetyViolations, operationalViolations, trafficRuleViolations, driverConductViolations)) {
            new AlertDialog.Builder(this)
                    .setTitle("Incomplete Report")
                    .setMessage("Please select or input at least one violation before submitting.")
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
            return;
        }

        String reportMessage = String.format("Submitted violations:\nSpeeding: %s\nOperational: %s\nTraffic Rules: %s\nPassenger Safety: %s\nDriver Conduct: %s",
                speedingViolations, operationalViolations, trafficRuleViolations, passengerSafetyViolations, driverConductViolations);

        showToast(reportMessage);
    }

    private boolean isAllEmpty(String... inputs) {
        for (String input : inputs) {
            if (!isEmpty(input)) return false;
        }
        return true;
    }

    private boolean isEmpty(String input) {
        return input == null || input.trim().isEmpty() || input.equalsIgnoreCase("None");
    }

    private String extractInputFromSection(LinearLayout section) {
        EditText editText = section.findViewWithTag("other_input");
        return (editText != null && !editText.getText().toString().trim().isEmpty()) ? editText.getText().toString().trim() : "None";
    }

    private void clearImagePreview() {
        imagePreview.setImageURI(null);
        imagePreview.setImageBitmap(null);
        removePhotoButton.setVisibility(View.GONE);
        showToast("Image removed.");
    }

    private void setupToggleVisibility(View titleView, LinearLayout sectionOptions) {
        titleView.setOnClickListener(v -> {
            int visibility = (sectionOptions.getVisibility() == View.GONE) ? View.VISIBLE : View.GONE;
            sectionOptions.setVisibility(visibility);
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);  // Call the parent class method

        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                showToast("Camera permission is required to use this feature.");
            }
        }
    }

}

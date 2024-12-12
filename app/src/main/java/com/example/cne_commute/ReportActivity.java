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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class ReportActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int TAKE_PHOTO_REQUEST = 2;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    private Button selectFileButton, openCameraButton, submitButton;
    private ImageView imagePreview, removePhotoButton;
    private LinearLayout speedingOptions, operationalOptions, trafficRulesOptions, passengerSafetyViolationsOptions, driverConductOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        // Initialize views
        selectFileButton = findViewById(R.id.select_file_button);
        openCameraButton = findViewById(R.id.open_camera_button);
        submitButton = findViewById(R.id.submit_button);
        imagePreview = findViewById(R.id.image_preview);
        removePhotoButton = findViewById(R.id.remove_photo_button);

        // Initialize sections
        speedingOptions = findViewById(R.id.speeding_violations_options);
        operationalOptions = findViewById(R.id.operational_violations_options);
        trafficRulesOptions = findViewById(R.id.traffic_rule_violations_options);
        passengerSafetyViolationsOptions = findViewById(R.id.passenger_safety_violations_options);
        driverConductOptions = findViewById(R.id.driver_conduct_violations_options);

        // Setup click listeners for toggling section visibility
        setupToggleVisibility(findViewById(R.id.speeding_violations_title), speedingOptions);
        setupToggleVisibility(findViewById(R.id.operational_violations_title), operationalOptions);
        setupToggleVisibility(findViewById(R.id.traffic_rule_violations_title), trafficRulesOptions);
        setupToggleVisibility(findViewById(R.id.passenger_safety_violations_title), passengerSafetyViolationsOptions);
        setupToggleVisibility(findViewById(R.id.driver_conduct_violations_title), driverConductOptions);

        // Set listeners for file selection and photo capture
        selectFileButton.setOnClickListener(v -> openFileChooser());
        openCameraButton.setOnClickListener(v -> checkCameraPermissionAndOpen());
        submitButton.setOnClickListener(v -> confirmAndSubmitReport());

        // Remove photo button click listener
        removePhotoButton.setOnClickListener(v -> clearImagePreview());
    }

    // Setup click listener to toggle section visibility
    private void setupToggleVisibility(View titleView, LinearLayout sectionOptions) {
        titleView.setOnClickListener(v -> {
            sectionOptions.setVisibility(sectionOptions.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
        });
    }

    // Open file chooser for image selection
    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select an image"), PICK_IMAGE_REQUEST);
    }

    // Check camera permissions before opening the camera
    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    // Open camera to take a photo
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, TAKE_PHOTO_REQUEST);
        } else {
            showToast("Camera app is not available.");
        }
    }

    // Confirm and submit the report
    private void confirmAndSubmitReport() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Submission")
                .setMessage("Are you sure you want to submit this report?")
                .setPositiveButton("Yes", (dialog, which) -> submitReport())
                .setNegativeButton("No", (dialog, which) -> showToast("Submission canceled."))
                .show();
    }

    // Submit the report with selected violations
    private void submitReport() {
        String speedingViolations = extractInputFromSection(speedingOptions);
        String passengerSafetyViolations = extractInputFromSection(passengerSafetyViolationsOptions);
        String operationalViolations = extractInputFromSection(operationalOptions);
        String trafficRuleViolations = extractInputFromSection(trafficRulesOptions);
        String driverConductViolations = extractInputFromSection(driverConductOptions);

        // Format and display the report message
        String reportMessage = String.format("Submitted violations:\nSpeeding: %s\nOperational: %s\nTraffic Rules: %s\nPassenger Safety: %s\nDriver Conduct: %s",
                speedingViolations, operationalViolations, trafficRuleViolations, passengerSafetyViolations, driverConductViolations);

        showToast(reportMessage);
    }

    // Extract user input from EditText in a section
    private String extractInputFromSection(LinearLayout section) {
        EditText editText = section.findViewWithTag("other_input");
        return (editText != null && !editText.getText().toString().isEmpty()) ? editText.getText().toString() : "None";
    }

    // Handle results from file chooser or camera
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
                Uri selectedImageUri = data.getData();
                imagePreview.setImageURI(selectedImageUri);
                removePhotoButton.setVisibility(View.VISIBLE);
                showToast("Selected Image: " + selectedImageUri.toString());
            } else if (requestCode == TAKE_PHOTO_REQUEST && data != null) {
                Bitmap capturedImage = (Bitmap) data.getExtras().get("data");
                if (capturedImage != null) {
                    imagePreview.setImageBitmap(capturedImage);
                    removePhotoButton.setVisibility(View.VISIBLE);
                    showToast("Photo captured successfully!");
                } else {
                    showToast("Failed to capture image.");
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                showToast("Camera permission is required to use this feature.");
            }
        }
    }

    // Clear the image preview and hide the remove button
    private void clearImagePreview() {
        imagePreview.setImageURI(null);
        imagePreview.setImageBitmap(null);
        removePhotoButton.setVisibility(View.GONE);
        showToast("Image removed.");
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

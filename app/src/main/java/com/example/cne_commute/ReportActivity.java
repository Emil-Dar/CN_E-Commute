package com.example.cne_commute;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.CheckBox;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReportActivity extends AppCompatActivity {

    private ImageView imagePreview;
    private ImageButton removePhotoButton;
    private LinearLayout speedingOptions, operationalOptions, trafficRulesOptions, passengerSafetyViolationsOptions, driverConductOptions;

    private Uri photoUri;

    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ActivityResultLauncher<Intent> takePhotoLauncher;

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

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

        setupActivityResultLaunchers();

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
                    imagePreview.setTag(selectedImageUri);
                    removePhotoButton.setVisibility(View.VISIBLE);
                    showToast("Selected Image: " + selectedImageUri);
                }
            }
        });

        takePhotoLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                if (photoUri != null) {
                    imagePreview.setImageURI(photoUri);
                    imagePreview.setTag(photoUri);
                    removePhotoButton.setVisibility(View.VISIBLE);
                    showToast("Photo captured successfully!");
                }
            } else {
                showToast("Failed to capture image.");
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
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    private void openCamera() {
        try {
            File photoFile = createImageFile();
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this, "com.example.cne_commute.fileprovider", photoFile);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                takePhotoLauncher.launch(intent);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showToast("Error creating image file.");
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "CNCommute_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
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
        Uri imageUri = (imagePreview.getDrawable() != null) ? (Uri) imagePreview.getTag() : null;

        if (isAllEmpty(speedingViolations, passengerSafetyViolations, operationalViolations, trafficRuleViolations, driverConductViolations) && imageUri == null) {
            new AlertDialog.Builder(this)
                    .setTitle("Incomplete Report")
                    .setMessage("Please select or input at least one violation or add an image before submitting.")
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
            return;
        }


        Intent intent = new Intent(this, HistoryActivity.class);
        intent.putExtra("speedingViolations", speedingViolations);
        intent.putExtra("passengerSafetyViolations", passengerSafetyViolations);
        intent.putExtra("operationalViolations", operationalViolations);
        intent.putExtra("trafficRuleViolations", trafficRuleViolations);
        intent.putExtra("driverConductViolations", driverConductViolations);
        if (imageUri != null) {
            intent.putExtra("imageUri", imageUri.toString());
        }
        startActivity(intent);
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
        StringBuilder result = new StringBuilder();

        // Get all checked checkboxes with tag "violation_option"
        for (int i = 0; i < section.getChildCount(); i++) {
            View child = section.getChildAt(i);
            if (child instanceof CheckBox && ((CheckBox) child).isChecked()) {
                result.append(((CheckBox) child).getText().toString()).append(", ");
            }
        }

        // Include the EditText with tag "other_input"
        EditText editText = section.findViewWithTag("other_input");
        if (editText != null && !editText.getText().toString().trim().isEmpty()) {
            result.append(editText.getText().toString().trim());
        }

        // Remove trailing comma and space
        String finalResult = result.toString().trim();
        if (finalResult.endsWith(",")) {
            finalResult = finalResult.substring(0, finalResult.length() - 1);
        }

        return finalResult.isEmpty() ? "None" : finalResult;
    }


    private void clearImagePreview() {
        imagePreview.setImageURI(null);
        imagePreview.setImageBitmap(null);
        imagePreview.setTag(null);
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                showToast("Camera permission is required to use this feature.");
            }
        }
    }
}

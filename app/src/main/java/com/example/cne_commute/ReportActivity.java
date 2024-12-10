package com.example.cne_commute;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class ReportActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int TAKE_PHOTO_REQUEST = 2;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    private Button selectFileButton, openCameraButton, submitButton;
    private Spinner spinner1, spinner2, spinner3, spinner4;
    private ImageView imagePreview; // ImageView to display selected or captured image

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_report);

        // Initialize views
        selectFileButton = findViewById(R.id.select_file_button);
        openCameraButton = findViewById(R.id.open_camera_button);
        submitButton = findViewById(R.id.submit_button);
        imagePreview = findViewById(R.id.image_preview); // Initialize ImageView for preview

        // Initialize spinners
        spinner1 = findViewById(R.id.spinner_passenger_safety);
        spinner2 = findViewById(R.id.spinner_operational);
        spinner3 = findViewById(R.id.spinner_traffic_rules);
        spinner4 = findViewById(R.id.spinner_vehicle_condition);

        // Set listeners for file selection and photo capture
        selectFileButton.setOnClickListener(v -> openFileChooser());
        openCameraButton.setOnClickListener(v -> checkCameraPermissionAndOpen());
        submitButton.setOnClickListener(v -> submitReport());
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
            // Request camera permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    // Open camera to take a photo
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, TAKE_PHOTO_REQUEST);
        } else {
            showToast("Camera app is not available");
        }
    }

    // Submit the report with selected violations
    private void submitReport() {
        String selectedViolation1 = spinner1.getSelectedItem().toString();
        String selectedViolation2 = spinner2.getSelectedItem().toString();
        String selectedViolation3 = spinner3.getSelectedItem().toString();
        String selectedViolation4 = spinner4.getSelectedItem().toString();

        showToast("Selected Violations:\n1. " + selectedViolation1 +
                "\n2. " + selectedViolation2 +
                "\n3. " + selectedViolation3 +
                "\n4. " + selectedViolation4);

        // Add functionality to send this data to a server or database
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
                Uri selectedImageUri = data.getData();
                // Show the selected image in the ImageView
                imagePreview.setImageURI(selectedImageUri);
                showToast("Selected Image: " + selectedImageUri.toString());
            } else if (requestCode == TAKE_PHOTO_REQUEST && data != null) {
                // Extract the thumbnail image from the returned Intent
                Bitmap capturedImage = (Bitmap) data.getExtras().get("data");
                if (capturedImage != null) {
                    imagePreview.setImageBitmap(capturedImage);
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

    private void showToast(String message) {
        Toast.makeText(ReportActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}

package com.example.cne_commute;



import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;



import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;


import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;
import java.util.concurrent.TimeUnit;
import android.content.ContentResolver;
import android.net.Uri;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.example.cne_commute.BuildConfig;

import com.google.gson.Gson;





public class ReportActivity extends AppCompatActivity {

    // These constants are used to pass data from the QR code adapter to this activity.
    public static final String EXTRA_DRIVER_ID = "driverId";
    public static final String EXTRA_OPERATOR_NAME = "EXTRA_OPERATOR_NAME";
    public static final String EXTRA_PLATE_NUMBER = "EXTRA_PLATE_NUMBER";
    public static final String EXTRA_CONTACT_NO = "EXTRA_CONTACT_NO";

    public static final String EXTRA_DRIVER_NAME = "EXTRA_DRIVER_NAME";
    public static final String EXTRA_DRIVER_CONTACT = "EXTRA_DRIVER_CONTACT";



    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final String TAG = "ReportActivity";

    // Firebase instances
    private FirebaseFirestore db;


    // UI elements
    private ImageView imagePreview;
    private ImageButton removePhotoButton;
    private EditText imageDescriptionInput;
    private TextInputEditText commuterNameEditText;
    private TextInputEditText commuterContactEditText;
    private LinearLayout parkingObstructionOptions,
            trafficMovementOptions,
            driverBehaviorOptions,
            licensingDocumentationOptions,
            attireFareOptions;
    private TextView driverInfoText;
    private Button submitButton;
    private Button selectFileButton;
    private Button openCameraButton;

    // Uri to store the photo taken by the camera
    private Uri photoUri;

    // Activity result launchers for handling image selection and camera
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ActivityResultLauncher<Intent> takePhotoLauncher;

    // Data from the scanned QR code
    private String driverId;
    private String operatorName;
    private String plateNumber;
    private String contactNo;



    private EditText franchiseIdEditText;

    private String driverName;
    private String driverContact;

    private TextView driverNameTextView;
    private TextView driverContactTextView;


    private SupabaseService supabaseService;
    private OkHttpClient client;

    private Retrofit retrofit;






    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        driverName = getIntent().getStringExtra("EXTRA_DRIVER_NAME");
        driverContact = getIntent().getStringExtra("EXTRA_DRIVER_CONTACT");



        setContentView(R.layout.activity_report);

        // Confirm activity launch
        Toast.makeText(this, "ReportActivity launched", Toast.LENGTH_SHORT).show();

        // Link XML views for scanned driver details
        TextView driverNameTextView = findViewById(R.id.driver_name_text_view);
        TextView driverContactTextView = findViewById(R.id.driver_contact_text_view);

        // Retrieve and display operator data from Intent
        retrieveAndDisplayOperatorData();

        // Initialize other UI elements (e.g., franchise ID, image picker, etc.)
        initViews();

        // Set up activity result launchers (camera/gallery)
        setupActivityResultLaunchers();

        // Initialize violation headers (TextViews)
        TextView parkingObstructionHeader = findViewById(R.id.parking_obstruction_violations);
        TextView trafficMovementHeader = findViewById(R.id.traffic_movement_violations);
        TextView driverBehaviorHeader = findViewById(R.id.driver_behavior_violations);
        TextView licensingDocumentationHeader = findViewById(R.id.licensing_documentation_violations);
        TextView attireFareHeader = findViewById(R.id.attire_fare_violations);

// Initialize violation groups (LinearLayouts)
        LinearLayout parkingObstructionViolations = findViewById(R.id.parking_obstruction_violations_options);
        LinearLayout trafficMovementViolations = findViewById(R.id.traffic_movement_violations_options);
        LinearLayout driverBehaviorViolations = findViewById(R.id.driver_behavior_violations_options);
        LinearLayout licensingDocumentationViolations = findViewById(R.id.licensing_documentation_violations_options);
        LinearLayout attireFareViolations = findViewById(R.id.attire_fare_violations_options);

// Set up click listeners and toggle logic
        setupToggle(parkingObstructionHeader, parkingObstructionViolations);
        setupToggle(trafficMovementHeader, trafficMovementViolations);
        setupToggle(driverBehaviorHeader, driverBehaviorViolations);
        setupToggle(licensingDocumentationHeader, licensingDocumentationViolations);
        setupToggle(attireFareHeader, attireFareViolations);


        // Initialize Supabase Retrofit client with auth interceptor
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                            .header("Authorization", "Bearer " + BuildConfig.SUPABASE_API_KEY)
                            .method(original.method(), original.body())
                            .build();
                    return chain.proceed(request);
                })
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl("https://rtwrbkrroilftdhggxjc.supabase.co/rest/v1/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        setupSupabaseClient();
        supabaseService = retrofit.create(SupabaseService.class);

        setupImageButtonListeners();
        setupSubmitButtonListener();

        showDriverInfo();

    }


    private void setupSupabaseClient() {
        String supabaseKey = BuildConfig.SUPABASE_API_KEY;



        // Logging interceptor for debugging
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Auth interceptor for Supabase headers
        Interceptor authInterceptor = chain -> {
            Request request = chain.request().newBuilder()
                    .addHeader("apikey", supabaseKey)
                    .addHeader("Authorization", "Bearer " + supabaseKey)
                    .addHeader("Content-Type", "application/json")
                    .build();
            return chain.proceed(request);
        };

        // Build OkHttpClient
        client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(authInterceptor)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)

                .build();

        // Build Retrofit instance
        retrofit = new Retrofit.Builder()
                .baseUrl("https://rtwrbkrroilftdhggxjc.supabase.co/rest/v1/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Create Supabase service interface
        supabaseService = retrofit.create(SupabaseService.class);
    }



    private void retrieveAndDisplayOperatorData() {
        // Retrieve intent extras with fallback
        String name = getSafeExtra("EXTRA_DRIVER_NAME", "Unknown");
        String contact = getSafeExtra("EXTRA_DRIVER_CONTACT", "Unknown");
        String franchiseId = getSafeExtra("EXTRA_FRANCHISE_ID", "");

        // Display driver name
        TextView driverNameTextView = findViewById(R.id.driver_name_text_view);
        if (driverNameTextView != null) {
            driverNameTextView.setText(getString(R.string.label_driver_name) + ": " + driverName);
        }

        // Display driver contact
        TextView driverContactTextView = findViewById(R.id.driver_contact_text_view);
        if (driverContactTextView != null) {
            driverContactTextView.setText(getString(R.string.label_driver_contact, contact));
        }

        // Display franchise ID in input field
        if (franchiseIdEditText != null) {
            franchiseIdEditText.setText(franchiseId);
        }

        // Log for debugging
        Log.d("ReportActivity", "Driver: " + name + ", Contact: " + contact + ", Franchise ID: " + franchiseId);
    }

    private String getSafeExtra(String key, String fallback) {
        String value = getIntent().getStringExtra(key);
        return (value != null && !value.trim().isEmpty()) ? value : fallback;
    }





    private void initViews() {
        // Customize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            androidx.appcompat.app.ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle("Report Violation");
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }


        // Commuter details input
        commuterNameEditText = findViewById(R.id.commuter_name_edit_text);
        if (commuterNameEditText != null) {
            ;
        }

        commuterContactEditText = findViewById(R.id.commuter_contact_edit_text);
        if (commuterContactEditText != null) {
            ;
        }

        // Franchise ID input
        franchiseIdEditText = findViewById(R.id.franchise_id_edit_text);
        if (franchiseIdEditText != null) {
            ;
        }

        parkingObstructionOptions = findViewById(R.id.parking_obstruction_violations_options);
        trafficMovementOptions = findViewById(R.id.traffic_movement_violations_options);
        driverBehaviorOptions = findViewById(R.id.driver_behavior_violations_options);
        licensingDocumentationOptions = findViewById(R.id.licensing_documentation_violations_options);
        attireFareOptions = findViewById(R.id.attire_fare_violations_options);

        imagePreview = findViewById(R.id.image_preview);
        removePhotoButton = findViewById(R.id.remove_image_button);
        imageDescriptionInput = findViewById(R.id.image_description);

        // Buttons
        selectFileButton = findViewById(R.id.select_file_button); // must be a class-level field
        openCameraButton = findViewById(R.id.open_camera_button);

        submitButton = findViewById(R.id.submit_button);


        // Driver info display
        driverInfoText = findViewById(R.id.driver_info_text);
        if (driverInfoText != null) {
            driverInfoText.setContentDescription("Driver information display");
        }

    }

    private void setupImageButtonListeners() {
        setupClickListener(selectFileButton, this::openFileChooser);
        setupClickListener(openCameraButton, this::checkCameraPermissionAndOpen);
        setupClickListener(removePhotoButton, () -> {
            imagePreview.setImageDrawable(null);
            imagePreview.setTag(null);
            imagePreview.setContentDescription("No image selected");
            removePhotoButton.setVisibility(View.GONE);
        });
    }


    private void setupSubmitButtonListener() {
        if (submitButton != null) {
            submitButton.setOnClickListener(v -> confirmAndSubmitReport());
        }
    }




    private void setupListeners() {
        // Click listeners for file selection and camera
        findViewById(R.id.select_file_button).setOnClickListener(v -> openFileChooser());
        findViewById(R.id.open_camera_button).setOnClickListener(v -> checkCameraPermissionAndOpen());
        removePhotoButton.setOnClickListener(v -> clearImagePreview());
        submitButton.setOnClickListener(v -> confirmAndSubmitReport());

        // Click listeners for collapsible sections
        setupToggleVisibility(findViewById(R.id.parking_obstruction_violations), parkingObstructionOptions);
        setupToggleVisibility(findViewById(R.id.traffic_movement_violations), trafficMovementOptions);
        setupToggleVisibility(findViewById(R.id.driver_behavior_violations), driverBehaviorOptions);
        setupToggleVisibility(findViewById(R.id.licensing_documentation_violations), licensingDocumentationOptions);
        setupToggleVisibility(findViewById(R.id.attire_fare_violations), attireFareOptions);
    }


    private void setupClickListener(View view, Runnable action) {
        if (view != null) {
            view.setOnClickListener(v -> action.run());
        }
    }

    private void setupToggle(View toggleAnchor, View targetGroup) {
        if (toggleAnchor != null && targetGroup != null) {
            toggleAnchor.setOnClickListener(v -> {
                boolean isVisible = targetGroup.getVisibility() == View.VISIBLE;
                targetGroup.setVisibility(isVisible ? View.GONE : View.VISIBLE);

                // Optional: Add rotation or icon feedback
                toggleAnchor.setRotation(isVisible ? 0f : 0f);
            });
        }
    }


    private void showDriverInfo() {
        if (driverInfoText != null) {
            // Apply fallback values for safety
            String name = (driverName != null && !driverName.trim().isEmpty()) ? driverName : "Unknown";
            String contact = (driverContact != null && !driverContact.trim().isEmpty()) ? driverContact : "Unknown";

            // Format using string resource (recommended for localization)
            String info = String.format("Driver Name: %s\nDriver Contact No.: %s", name, contact);
            driverInfoText.setText(info);

            // Accessibility: Add content description for screen readers
            driverInfoText.setContentDescription("Driver information displayed: " + info);

            // Optional: Add line spacing or styling for readability
            driverInfoText.setLineSpacing(1.2f, 1.2f);
        }
    }



    private void setupActivityResultLaunchers() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        Log.d("ReportActivity", "Selected image URI: " + uri);
                        if (isValidImage(uri)) {
                            previewImage(uri);
                        } else {
                            showToast("Unsupported image format.");
                        }
                    }
                });

        takePhotoLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && photoUri != null) {
                        Log.d("ReportActivity", "Captured photo URI: " + photoUri);
                        if (isValidImage(photoUri)) {
                            previewImage(photoUri);
                            showToast("Photo captured successfully!");
                        } else {
                            showToast("Captured photo is not a valid image.");
                        }
                    }
                });
    }


    private void previewImage(Uri uri) {
        if (imagePreview != null) {
            imagePreview.setImageURI(uri);
            imagePreview.setTag(uri);
            imagePreview.setContentDescription("Selected violation photo");
            if (removePhotoButton != null) {
                removePhotoButton.setVisibility(View.VISIBLE);
            }
        }
    }

    private boolean isValidImage(Uri uri) {
        ContentResolver resolver = getContentResolver();
        String mimeType = resolver.getType(uri);
        return mimeType != null && mimeType.startsWith("image/");
    }



    private void openFileChooser() {
        Log.d("ReportActivity", "openFileChooser triggered");
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        pickImageLauncher.launch(Intent.createChooser(i, "Select an image"));
    }


    private void checkCameraPermissionAndOpen() {
        Log.d("ReportActivity", "checkCameraPermissionAndOpen triggered");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES},
                    CAMERA_PERMISSION_REQUEST_CODE);
        }
    }


    private void openCamera() {
        try {
            File file = createImageFile();
            photoUri = FileProvider.getUriForFile(
                    this,
                    "com.example.cne_commute.fileprovider",
                    file
            );

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            takePhotoLauncher.launch(intent);
        } catch (IOException e) {
            Log.e(TAG, "Failed to open camera", e);
            showToast("Unable to open camera.");
        }
    }


    private File createImageFile() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile("CNCommute_" + timestamp, ".jpg", dir);
    }

    private void confirmAndSubmitReport() {
        new MaterialAlertDialogBuilder(this, R.style.CNECommuteDialogTheme)
                .setTitle("Confirm Submission")
                .setMessage("Are you sure you want to submit this report?")
                .setPositiveButton("Submit Report", (dialog, which) -> submitReport())
                .setNegativeButton("Cancel", null)
                .setCancelable(false)
                .show();
    }

    private void submitReport() {
        String commuterName = commuterNameEditText != null ? commuterNameEditText.getText().toString().trim() : "";
        String commuterContact = commuterContactEditText != null ? commuterContactEditText.getText().toString().trim() : "";
        String franchiseId = franchiseIdEditText != null ? franchiseIdEditText.getText().toString().trim() : "";

        if (commuterName.isEmpty() || commuterName.matches(".*\\d.*")) {
            commuterNameEditText.setError("Please enter a valid name.");
            showToast("Name is required and must not contain numbers.");
            return;
        }

        if (commuterContact.isEmpty() || !commuterContact.matches("^09\\d{9}$")) {
            commuterContactEditText.setError("Invalid Philippine mobile number.");
            showToast("Please enter a valid contact number.");
            return;
        }

        if (franchiseId.isEmpty() || !franchiseId.matches("^\\d{4}$")) {
            franchiseIdEditText.setError("Franchise ID must be a 4-digit number.");
            showToast("Please enter a valid Franchise ID.");
            return;
        }

        String parking = extractInput(parkingObstructionOptions);
        String movement = extractInput(trafficMovementOptions);
        String behavior = extractInput(driverBehaviorOptions);
        String licensing = extractInput(licensingDocumentationOptions);
        String attire = extractInput(attireFareOptions);
        String imgDesc = imageDescriptionInput != null ? imageDescriptionInput.getText().toString().trim() : "";
        Uri uri = (imagePreview != null && imagePreview.getTag() instanceof Uri) ? (Uri) imagePreview.getTag() : null;

        if (isAllEmpty(parking, movement, behavior, licensing, attire) && uri == null) {
            showToast("Provide at least one violation or an image.");
            return;
        }

        String userId = "Anonymous"; // Replace with actual user ID if available
        String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(new Date());

        Report report = new Report(
                userId,
                driverId,
                driverName,
                driverContact,
                franchiseId,
                commuterName,
                commuterContact,
                parking,
                movement,
                behavior,
                licensing,
                attire,
                imgDesc,
                null, // imageUrl will be set after upload
                timestamp
        );

        if (uri != null) {
            uploadImageThenSubmit(uri, report);
        } else {
            submitToSupabase(report);
        }
    }

    private void submitToSupabase(Report report) {
        supabaseService.submitReport(report.toMap()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    showToast("Report submitted successfully!");
                    Log.d("ReportActivity", "Report submitted: " + new Gson().toJson(report));
                    finish(); // or reset form
                } else {
                    showToast("Failed to submit report. Code: " + response.code());
                    Log.e("ReportActivity", "Submit failed: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showToast("Error submitting report.");
                Log.e("ReportActivity", "Submit error", t);
            }
        });
    }






    private void uploadImageThenSubmit(Uri uri, Report report) {
        String filename = report.getUserId() + "_" + System.currentTimeMillis() + ".jpg";

        try {
            InputStream stream = getContentResolver().openInputStream(uri);
            byte[] imageBytes = readBytes(stream); // Your existing helper method

            RequestBody body = RequestBody.create(imageBytes, MediaType.parse("image/jpeg"));
            Request request = new Request.Builder()
                    .url("https://rtwrbkrroilftdhggxjc.supabase.co/storage/v1/s3/uploads_debug_0/" + filename)
                    .put(body)
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String imageUrl = "https://rtwrbkrroilftdhggxjc.supabase.co/storage/v1/s3/uploads_debug_0/" + filename;
                        report.setImageUrl(imageUrl);


                        runOnUiThread(() -> submitToSupabase(report));
                    } else {
                        Log.e(TAG, "Upload failed with code: " + response.code());
                        runOnUiThread(() -> {
                            showToast("Upload failed. Submitting without image.");
                            submitToSupabase(report);
                        });
                    }
                }

                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    Log.e(TAG, "Upload error: " + e.getMessage(), e);
                    runOnUiThread(() -> {
                        showToast("Upload error: " + e.getMessage());
                        submitToSupabase(report);
                    });
                }
            });

        } catch (IOException e) {
            Log.e(TAG, "Failed to read image: " + e.getMessage(), e);
            showToast("Failed to read image.");
            submitToSupabase(report);
        }
    }


    // Utility method for reading image bytes from InputStream
    private byte[] readBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    private void submitToSupabase(String reportId, Map<String, Object> data) {
        if (supabaseService == null) {
            showToast("Supabase client not initialized.");
            return;
        }

        Call<Void> call = supabaseService.submitReport(data);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "✅ Report submitted successfully to backend.");
                } else {
                    String errorBody = "Unknown error";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to parse error body: " + e.getMessage());
                    }
                    Log.e(TAG, "❌ Failed to submit to backend. Code: " + response.code() + ", Error: " + errorBody);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ API call to backend failed: " + t.getMessage(), t);
            }
        });

    }






    private void saveReport(String reportId, Map<String, Object> data) {
        db.collection("Reports").document(reportId).set(data)
                .addOnSuccessListener(u -> {
                    Log.d(TAG, "Report saved to Firestore successfully.");
                    sendReportToBackend(data);
                    showToast("Report submitted.");
                    startActivity(new Intent(this, HistoryActivity.class)
                            .putExtra("reportId", reportId));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save report to Firestore: " + e.getMessage(), e);
                    showToast("Failed to save: " + e.getMessage());
                });
    }

    private void sendReportToBackend(Map<String, Object> data) {
        Log.d(TAG, "Preparing to send report to backend...");

        // Send to backend via Retrofit
        SupabaseService apiService = retrofit.create(SupabaseService.class); // Use your configured Retrofit instance

        Call<Void> call = apiService.submitReport(data); // Directly pass the map

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "✅ Report submitted successfully to backend.");
                } else {
                    String errorBody = "Unknown error";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to parse error body: " + e.getMessage());
                    }
                    Log.e(TAG, "❌ Failed to submit to backend. Code: " + response.code() + ", Error: " + errorBody);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ API call to backend failed: " + t.getMessage(), t);
            }
        });
    }



    private String extractInput(LinearLayout section) {
        if (section == null) return "None";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < section.getChildCount(); i++) {
            View c = section.getChildAt(i);
            if (c instanceof CheckBox && ((CheckBox) c).isChecked()) {
                sb.append(((CheckBox) c).getText()).append(", ");
            }
        }
        EditText other = section.findViewWithTag("other_input");
        if (other != null && !other.getText().toString().trim().isEmpty()) {
            sb.append(other.getText().toString().trim());
        }
        String result = sb.toString().replaceAll(",\\s*$", "").trim();
        return result.isEmpty() ? "None" : result;
    }

    private boolean isAllEmpty(String... vals) {
        for (String s : vals) {
            if (!"None".equalsIgnoreCase(s) && !s.trim().isEmpty()) return false;
        }
        return true;
    }

    private void clearImagePreview() {
        if (imagePreview != null) {
            imagePreview.setImageDrawable(null);
            imagePreview.setTag(null);
        }
        if (removePhotoButton != null) {
            removePhotoButton.setVisibility(View.GONE);
        }
    }

    private void setupToggleVisibility(TextView titleView, LinearLayout options) {
        if (titleView == null || options == null) return;

        titleView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0);
        titleView.setOnClickListener(v -> {
            boolean visible = options.getVisibility() == View.VISIBLE;
            options.setVisibility(visible ? View.GONE : View.VISIBLE);
            titleView.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, visible ? R.drawable.ic_arrow_down : R.drawable.ic_arrow_up, 0);
        });
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int rc, @NonNull String[] perms,
                                           @NonNull int[] grants) {
        super.onRequestPermissionsResult(rc, perms, grants);
        if (rc == CAMERA_PERMISSION_REQUEST_CODE && grants.length > 0
                && grants[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        }
    }


}
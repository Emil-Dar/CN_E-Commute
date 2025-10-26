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

import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.ArrayList;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;


public class ReportActivity extends AppCompatActivity {

    // These constants are used to pass data from the QR code adapter to this activity.



    public static final String EXTRA_PLATE_NUMBER = "EXTRA_PLATE_NUMBER";
    public static final String EXTRA_DRIVER_ID = "EXTRA_DRIVER_ID";
    public static final String EXTRA_OPERATOR_NAME = "EXTRA_OPERATOR_NAME";


    public static final String EXTRA_FRANCHISE_ID = "EXTRA_FRANCHISE_ID";
    public static final String EXTRA_OPERATOR_ID = "EXTRA_OPERATOR_ID";
    public static final String EXTRA_TODA = "EXTRA_TODA";

    public static final String EXTRA_DRIVER_NAME = "EXTRA_DRIVER_NAME";



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


    private String franchiseId;
    private String operatorId;
    private String toda;


    private EditText franchiseIdEditText;

    private String driverName;


    private TextView driverNameTextView;


    private SupabaseService supabaseService;
    private OkHttpClient client;

    private Retrofit retrofit;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        //  Retrieve scanned QR data from intent and assign to class-level fields
        driverId = getIntent().getStringExtra(EXTRA_DRIVER_ID);
        driverName = getIntent().getStringExtra(EXTRA_DRIVER_NAME);
        franchiseId = getIntent().getStringExtra(EXTRA_FRANCHISE_ID);
        operatorName = getIntent().getStringExtra(EXTRA_OPERATOR_NAME);
        toda = getIntent().getStringExtra(EXTRA_TODA);

        //  Initialize all UI elements and display scanned data
        initViews();

        //  Optional: Log scanned data for debugging
        Log.d("ReportActivity", "Scanned QR Data — Driver ID: " + driverId +
                ", Driver Name: " + driverName +
                ", Franchise ID: " + franchiseId +
                ", Operator Name: " + operatorName +
                ", TODA: " + toda);

        //  Set up activity result launchers (camera/gallery)
        setupActivityResultLaunchers();

        //  Initialize violation headers
        TextView parkingObstructionHeader = findViewById(R.id.parking_obstruction_violations);
        TextView trafficMovementHeader = findViewById(R.id.traffic_movement_violations);
        TextView driverBehaviorHeader = findViewById(R.id.driver_behavior_violations);
        TextView licensingDocumentationHeader = findViewById(R.id.licensing_documentation_violations);
        TextView attireFareHeader = findViewById(R.id.attire_fare_violations);

        //  Initialize violation groups
        LinearLayout parkingObstructionViolations = findViewById(R.id.parking_obstruction_violations_options);
        LinearLayout trafficMovementViolations = findViewById(R.id.traffic_movement_violations_options);
        LinearLayout driverBehaviorViolations = findViewById(R.id.driver_behavior_violations_options);
        LinearLayout licensingDocumentationViolations = findViewById(R.id.licensing_documentation_violations_options);
        LinearLayout attireFareViolations = findViewById(R.id.attire_fare_violations_options);

        //  Set up toggle logic
        setupToggle(parkingObstructionHeader, parkingObstructionViolations);
        setupToggle(trafficMovementHeader, trafficMovementViolations);
        setupToggle(driverBehaviorHeader, driverBehaviorViolations);
        setupToggle(licensingDocumentationHeader, licensingDocumentationViolations);
        setupToggle(attireFareHeader, attireFareViolations);

        //  Initialize Supabase Retrofit client
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

        //  Set up image and submission logic
        setupImageButtonListeners();
        setupSubmitButtonListener();

        //  Display full driver info block
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
        // Log scanned data for debugging
        Log.d("ReportActivity", "Driver ID: " + driverId + ", Name: " + driverName +
                ", Franchise ID: " + franchiseId +
                ", Operator Name: " + operatorName + ", TODA: " + toda);
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
        commuterContactEditText = findViewById(R.id.commuter_contact_edit_text);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String name = currentUser.getDisplayName();
            String phone = currentUser.getPhoneNumber();

            if (name != null && commuterNameEditText != null) {
                commuterNameEditText.setText(name);
                commuterNameEditText.setEnabled(false);
            }

            if (phone != null && commuterContactEditText != null) {
                commuterContactEditText.setText(phone);
                commuterContactEditText.setEnabled(false);
            }
        }

        // Bind QR fields for display
        TextView driverIdTextView = findViewById(R.id.driver_id_text_view);
        TextView driverNameTextView = findViewById(R.id.driver_name_text_view);
        TextView franchiseIdTextView = findViewById(R.id.franchise_id_text_view);
        TextView operatorIdTextView = findViewById(R.id.operator_id_text_view);
        TextView todaTextView = findViewById(R.id.toda_text_view);

        //  Display scanned QR data
        driverIdTextView.setText("Driver ID: " + (driverId != null ? driverId : "Unknown"));
        driverNameTextView.setText("Driver Name: " + (driverName != null ? driverName : "Unknown"));
        franchiseIdTextView.setText("Franchise ID: " + (franchiseId != null ? franchiseId : "Unknown"));
        operatorIdTextView.setText("Operator Name: " + (operatorName != null ? operatorName : "Unknown"));
        todaTextView.setText("TODA: " + (toda != null ? toda : "Unknown"));

        // Violation groups
        parkingObstructionOptions = findViewById(R.id.parking_obstruction_violations_options);
        trafficMovementOptions = findViewById(R.id.traffic_movement_violations_options);
        driverBehaviorOptions = findViewById(R.id.driver_behavior_violations_options);
        licensingDocumentationOptions = findViewById(R.id.licensing_documentation_violations_options);
        attireFareOptions = findViewById(R.id.attire_fare_violations_options);

        // Image handling
        imagePreview = findViewById(R.id.image_preview);
        removePhotoButton = findViewById(R.id.remove_image_button);
        imageDescriptionInput = findViewById(R.id.image_description);

        // Buttons
        selectFileButton = findViewById(R.id.select_file_button);
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
            submitButton.setOnClickListener(v -> {
                if (driverId == null || driverName == null || franchiseId == null) {
                    Toast.makeText(this, "Missing driver info. Please rescan.", Toast.LENGTH_SHORT).show();
                    return;
                }

                showReportSummaryDialog(); //  Show preview before submitting
            });
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
            // Apply fallback values using helper
            String id = safeText(driverId);
            String name = safeText(driverName);
            String franchise = safeText(franchiseId);
            String operatorNameValue = safeText(operatorName);
            String todaValue = safeText(toda);

            // Format display block
            String info = String.format(
                    "Driver ID: %s\nDriver Name: %s\nFranchise ID: %s\nOperator Name: %s\nTODA: %s",
                    id, name, franchise, operatorNameValue, todaValue
            );

            driverInfoText.setText(info);
            driverInfoText.setContentDescription("Driver information displayed: " + info);
            driverInfoText.setLineSpacing(1.2f, 1.2f);
        }
    }

    private String safeText(String value) {
        return (value != null && !value.trim().isEmpty()) ? value : "Unknown";
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

        // Detect if fields were auto-filled and disabled
        boolean isAutoFilledName = commuterNameEditText != null && !commuterNameEditText.isEnabled();
        boolean isAutoFilledContact = commuterContactEditText != null && !commuterContactEditText.isEnabled();

        // Validate only if fields are editable
        if (!isAutoFilledName && (commuterName.isEmpty() || commuterName.matches(".*\\d.*"))) {
            commuterNameEditText.setError("Please enter a valid name.");
            showToast("Name is required and must not contain numbers.");
            return;
        }

        if (!isAutoFilledContact && (commuterContact.isEmpty() || !commuterContact.matches("^09\\d{9}$"))) {
            commuterContactEditText.setError("Invalid Philippine mobile number.");
            showToast("Please enter a valid contact number.");
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

        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "Anonymous";

        String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(new Date());

        Report report = new Report(
                userId,
                driverId,
                driverName,
                franchiseId,
                operatorName, //  updated
                toda,
                commuterName,
                commuterContact,
                parking,
                movement,
                behavior,
                licensing,
                attire,
                imgDesc,
                null,       // imageUrl will be set after upload
                timestamp,
                "Pending",  // default status
                null        // remarks (can be null at submission)
        );

        if (uri != null) {
            uploadImageThenSubmit(uri, report);
        } else {
            submitToSupabase(report);
        }
    }


    private void showReportSummaryDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_report_confirmation, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        Button cancelButton = dialogView.findViewById(R.id.button_cancel);
        Button submitButton = dialogView.findViewById(R.id.button_submit);

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        submitButton.setOnClickListener(v -> {
            dialog.dismiss();
            submitReport();
        });

        dialog.show();
    }




    private void submitToSupabase(Report report) {
        supabaseService.submitReport(report.toMap()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    showToast("Report submitted successfully!");
                    Log.d("ReportActivity", "Report submitted: " + new Gson().toJson(report));

                    if ("Anonymous".equals(report.getUserId())) {
                        saveReportToHistory(report);
                    }

                    startActivity(new Intent(ReportActivity.this, ReportHistoryActivity.class));
                    finish();
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

                        Log.d(TAG, "Image URL: " + imageUrl);
                        Log.d(TAG, "Report data: " + new Gson().toJson(report));

                        runOnUiThread(() -> submitToSupabase(report));
                    } else {
                        Log.e(TAG, "Upload failed with code: " + response.code());
                        runOnUiThread(() -> {
                            showToast("Upload failed. Submitting without image.");
                            Log.d(TAG, "Report data (no image): " + new Gson().toJson(report));
                            submitToSupabase(report);
                        });
                    }
                }

                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    Log.e(TAG, "Upload error: " + e.getMessage(), e);
                    runOnUiThread(() -> {
                        showToast("Upload error. Submitting without image.");
                        Log.d(TAG, "Report data (upload failed): " + new Gson().toJson(report));
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

    private void submitToSupabase(final String reportId, final Map<String, Object> data) {
        if (supabaseService == null) {
            showToast("Supabase client not initialized.");
            return;
        }

        // ✅ Retrieve and log Firebase UID
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "null";

        Log.d("SubmitReport", "Firebase UID: [" + userId + "]");

        //  Inject UID into the data map
        data.put("user_id", userId);

        //  Log the full data map before submission
        Log.d("SubmitReport", "Final data map: " + new Gson().toJson(data));

        Call<Void> call = supabaseService.submitReport(data);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    showToast("Report submitted successfully!");
                    finish(); // or redirect to ReportHistoryActivity
                } else {
                    showToast("Failed to submit report. Code: " + response.code());
                    Log.e("SubmitReport", "Error body: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showToast("Error submitting report.");
                Log.e("ReportActivity", "Submit error", t);
            }
        });
    }

    private void saveReportToHistory(Report report) {
        SharedPreferences prefs = getSharedPreferences("report_history", MODE_PRIVATE);
        String existing = prefs.getString("reports", "[]");

        Gson gson = new Gson();
        Type type = new TypeToken<List<Report>>() {}.getType();
        List<Report> reportList = gson.fromJson(existing, type);

        reportList.add(report); // Add the new report

        String updated = gson.toJson(reportList);
        prefs.edit().putString("reports", updated).apply();

        Log.d(TAG, " Report saved to local history.");
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

        SupabaseService apiService = retrofit.create(SupabaseService.class);

        Call<Void> reportCall = apiService.submitReport(data);

        reportCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, " Report submitted successfully to backend.");
                    Log.d(TAG, " Submitted report: " + new Gson().toJson(data));

                    if ("Anonymous".equals(data.get("user_id"))) {
                        Report report = Report.fromMap("local", data); // helper method to reconstruct Report
                        saveReportToHistory(report);
                    }

                } else {
                    String errorBody = "Unknown error";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to parse error body: " + e.getMessage());
                    }
                    Log.e(TAG, " Failed to submit to backend. Code: " + response.code() + ", Error: " + errorBody);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e(TAG, " API call to backend failed: " + t.getMessage(), t);
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
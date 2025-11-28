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
import android.widget.SearchView;
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

import android.os.Build;
import java.util.Arrays;
import android.graphics.Typeface;
import androidx.core.content.ContextCompat;



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

    private CheckBox cbCorner, cbCrosswalk, cbDoubleParking, cbNoParkingSign,
            cbOvertimeParking, cbObstruction, cbIllegalTerminal;

    private List<CheckBox> parkingCheckboxes = new ArrayList<>();

    private List<CheckBox> trafficCheckboxes;

    private List<CheckBox> driverBehaviorCheckboxes;

    private List<CheckBox> licensingCheckboxes;

    private List<CheckBox> attireFareCheckboxes;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        // Retrieve scanned QR data from intent and assign to class-level fields
        driverId = getIntent().getStringExtra(EXTRA_DRIVER_ID);
        driverName = getIntent().getStringExtra(EXTRA_DRIVER_NAME);
        franchiseId = getIntent().getStringExtra(EXTRA_FRANCHISE_ID);
        operatorName = getIntent().getStringExtra(EXTRA_OPERATOR_NAME);
        toda = getIntent().getStringExtra(EXTRA_TODA);

        // Initialize all UI elements and display scanned data
        initViews();

        // Autofill commuter name and contact from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String commuterName = prefs.getString("commuter_name", "");
        String commuterContact = prefs.getString("commuter_contact", "");

        commuterNameEditText = findViewById(R.id.edit_commuter_name);
        commuterContactEditText = findViewById(R.id.edit_commuter_contact);

        commuterNameEditText.setText(commuterName);
        commuterContactEditText.setText(commuterContact);

        commuterNameEditText.setEnabled(false);
        commuterContactEditText.setEnabled(false);

        Log.d("ReportActivity", "Scanned QR Data — Driver ID: " + driverId +
                ", Driver Name: " + driverName +
                ", Franchise ID: " + franchiseId +
                ", Operator Name: " + operatorName +
                ", TODA: " + toda);

        setupActivityResultLaunchers();

        TextView parkingObstructionHeader = findViewById(R.id.parking_obstruction_violations);
        TextView trafficMovementHeader = findViewById(R.id.traffic_movement_violations);
        TextView driverBehaviorHeader = findViewById(R.id.driver_behavior_violations);
        TextView licensingDocumentationHeader = findViewById(R.id.licensing_documentation_violations);
        TextView attireFareHeader = findViewById(R.id.attire_fare_violations);

        LinearLayout parkingObstructionViolations = findViewById(R.id.parking_obstruction_violations_options);
        LinearLayout trafficMovementViolations = findViewById(R.id.traffic_movement_violations_options);
        LinearLayout driverBehaviorViolations = findViewById(R.id.driver_behavior_violations_options);
        LinearLayout licensingDocumentationViolations = findViewById(R.id.licensing_documentation_violations_options);
        LinearLayout attireFareViolations = findViewById(R.id.attire_fare_violations_options);

        setupToggle(parkingObstructionHeader, parkingObstructionViolations);
        setupToggle(trafficMovementHeader, trafficMovementViolations);
        setupToggle(driverBehaviorHeader, driverBehaviorViolations);
        setupToggle(licensingDocumentationHeader, licensingDocumentationViolations);
        setupToggle(attireFareHeader, attireFareViolations);

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

        //  Parking Search Setup
        SearchView parkingSearch = findViewById(R.id.parking_violations_search);

        CheckBox cbCorner = findViewById(R.id.checkbox_corner);
        CheckBox cbCrosswalk = findViewById(R.id.checkbox_crosswalk);
        CheckBox cbDoubleParking = findViewById(R.id.checkbox_double_parking);
        CheckBox cbNoParkingSign = findViewById(R.id.checkbox_no_parking_sign);
        CheckBox cbOvertimeParking = findViewById(R.id.checkbox_overtime_parking);
        CheckBox cbObstruction = findViewById(R.id.checkbox_obstruction);
        CheckBox cbIllegalTerminal = findViewById(R.id.checkbox_illegal_terminal);

        parkingCheckboxes = Arrays.asList(
                cbCorner, cbCrosswalk, cbDoubleParking,
                cbNoParkingSign, cbOvertimeParking,
                cbObstruction, cbIllegalTerminal
        );

        parkingSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("ParkingSearch", "Query submitted: " + query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("ParkingSearch", "Query changed: " + newText);
                filterParkingViolations(newText);
                return true;
            }
        });
        int parkingSearchTextId = parkingSearch.getContext().getResources()
                .getIdentifier("android:id/search_src_text", null, null);
        TextView parkingSearchText = parkingSearch.findViewById(parkingSearchTextId);
        if (parkingSearchText != null) {
            parkingSearchText.setTextColor(ContextCompat.getColor(this, R.color.black));
            parkingSearchText.setTextSize(16);
            parkingSearchText.setTypeface(Typeface.DEFAULT);
            parkingSearchText.setPadding(
                    parkingSearchText.getPaddingLeft(),
                    parkingSearchText.getPaddingTop(),
                    parkingSearchText.getPaddingRight(),
                    parkingSearchText.getPaddingBottom() + 10
            );
        }




        //  Traffic Movement Search Setup
        SearchView trafficSearch = findViewById(R.id.traffic_movement_violations_search);

        // Bind each CheckBox
        CheckBox cbLeftTurnVinzonsJlukban = findViewById(R.id.checkbox_left_turn_vinzons_jlukban);
        CheckBox cbLeftTurnJlukban = findViewById(R.id.checkbox_left_turn_jlukban);
        CheckBox cbLeftTurnVinzonsFelipe = findViewById(R.id.checkbox_left_turn_vinzons_felipe);
        CheckBox cbUTurnVinzons = findViewById(R.id.checkbox_u_turn_vinzons);
        CheckBox cbLeftTurnFPimentelVBasit = findViewById(R.id.checkbox_left_turn_fpimentel_vbasit);
        CheckBox cbLeftTurnVBasitFPimentel = findViewById(R.id.checkbox_left_turn_vbasit_fpimentel);
        CheckBox cbOutOfLine = findViewById(R.id.checkbox_out_of_line);
        CheckBox cbDisregardingSign = findViewById(R.id.checkbox_disregarding_traffic_sign);
        CheckBox cbTruckBan = findViewById(R.id.checkbox_truck_ban);
        CheckBox cbNumberCoding = findViewById(R.id.checkbox_number_color_coding);
        CheckBox cbLoadingUnloading = findViewById(R.id.checkbox_loading_unloading_vinzons);
        CheckBox cbRightOfWay = findViewById(R.id.checkbox_right_of_way_emergency);
        CheckBox cbJaywalking = findViewById(R.id.checkbox_jaywalking);

        // Group into list
        trafficCheckboxes = Arrays.asList(
                cbLeftTurnVinzonsJlukban, cbLeftTurnJlukban, cbLeftTurnVinzonsFelipe,
                cbUTurnVinzons, cbLeftTurnFPimentelVBasit, cbLeftTurnVBasitFPimentel,
                cbOutOfLine, cbDisregardingSign, cbTruckBan,
                cbNumberCoding, cbLoadingUnloading, cbRightOfWay, cbJaywalking
        );

        // Search listener
        trafficSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("TrafficSearch", "Query submitted: " + query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("TrafficSearch", "Query changed: " + newText);
                filterTrafficViolations(newText);
                return true;
            }
        });
        int trafficSearchTextId = trafficSearch.getContext().getResources()
                .getIdentifier("android:id/search_src_text", null, null);
        TextView trafficSearchText = trafficSearch.findViewById(trafficSearchTextId);
        if (trafficSearchText != null) {
            trafficSearchText.setTextColor(ContextCompat.getColor(this, R.color.black));
            trafficSearchText.setTextSize(16);
            trafficSearchText.setTypeface(Typeface.DEFAULT);
            trafficSearchText.setPadding(
                    trafficSearchText.getPaddingLeft(),
                    trafficSearchText.getPaddingTop(),
                    trafficSearchText.getPaddingRight(),
                    trafficSearchText.getPaddingBottom() + 10
            );
        }


        //  Driver Behavior Search Setup
        SearchView driverBehaviorSearch = findViewById(R.id.driver_behavior_violations_search);

        // Bind each CheckBox
        CheckBox cbRefuseToConvey = findViewById(R.id.checkbox_refuse_to_convey);
        CheckBox cbAbusiveDriver = findViewById(R.id.checkbox_abusive_driver);
        CheckBox cbRecklessDriving = findViewById(R.id.checkbox_reckless_driving);
        CheckBox cbDuiLiquor = findViewById(R.id.checkbox_dui_liquor);
        CheckBox cbHitching = findViewById(R.id.checkbox_hitching);

        // Group into list
        driverBehaviorCheckboxes = Arrays.asList(
                cbRefuseToConvey, cbAbusiveDriver, cbRecklessDriving,
                cbDuiLiquor, cbHitching
        );

        // Search listener
        driverBehaviorSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("DriverBehaviorSearch", "Query submitted: " + query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("DriverBehaviorSearch", "Query changed: " + newText);
                filterDriverBehaviorViolations(newText);
                return true;
            }
        });
        int driverBehaviorSearchTextId = driverBehaviorSearch.getContext().getResources()
                .getIdentifier("android:id/search_src_text", null, null);
        TextView searchText = parkingSearch.findViewById(driverBehaviorSearchTextId);
        if (searchText != null) {
            searchText.setTextColor(ContextCompat.getColor(this, R.color.black));
            searchText.setTextSize(16); // Same as header
            searchText.setTypeface(Typeface.DEFAULT); // Not bold

            // Add bottom padding to push the underline lower
            searchText.setPadding(
                    searchText.getPaddingLeft(),
                    searchText.getPaddingTop(),
                    searchText.getPaddingRight(),
                    searchText.getPaddingBottom() + 10 // Adjust this value as needed
            );
        }

        //  Licensing Documentation Search Setup
        SearchView licensingSearch = findViewById(R.id.licensing_documentation_violations_search);

        // Bind each CheckBox
        CheckBox cbNoDriversLicense = findViewById(R.id.checkbox_no_drivers_license);
        CheckBox cbStudentPermitUse = findViewById(R.id.checkbox_student_permit_use);
        CheckBox cbUnregisteredFranchise = findViewById(R.id.checkbox_unregistered_franchise);

        // Group into list
        licensingCheckboxes = Arrays.asList(
                cbNoDriversLicense, cbStudentPermitUse, cbUnregisteredFranchise
        );

        // Search listener
        licensingSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("LicensingSearch", "Query submitted: " + query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("LicensingSearch", "Query changed: " + newText);
                filterLicensingViolations(newText);
                return true;
            }
        });
        int licensingSearchTextId = licensingSearch.getContext().getResources()
                .getIdentifier("android:id/search_src_text", null, null);
        TextView licensingSearchText = licensingSearch.findViewById(licensingSearchTextId);
        if (licensingSearchText != null) {
            licensingSearchText.setTextColor(ContextCompat.getColor(this, R.color.black));
            licensingSearchText.setTextSize(16); // Match header font size
            licensingSearchText.setTypeface(Typeface.DEFAULT); // Not bold
            licensingSearchText.setPadding(
                    licensingSearchText.getPaddingLeft(),
                    licensingSearchText.getPaddingTop(),
                    licensingSearchText.getPaddingRight(),
                    licensingSearchText.getPaddingBottom() + 10 // Push underline lower
            );
        }


        //  Attire Fare Search Setup
        SearchView attireFareSearch = findViewById(R.id.attire_fare_violations_search);

        // Bind each CheckBox
        CheckBox cbImproperAttire = findViewById(R.id.checkbox_improper_attire);
        CheckBox cbTricycleFareMatrix = findViewById(R.id.checkbox_tricycle_fare_matrix);
        CheckBox cbNoHelmet = findViewById(R.id.checkbox_no_helmet);
        CheckBox cbOvercharging = findViewById(R.id.checkbox_overcharging);


        // Group into list
        attireFareCheckboxes = Arrays.asList(
                cbImproperAttire, cbTricycleFareMatrix, cbNoHelmet, cbOvercharging
        );

        // Search listener
        attireFareSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("AttireFareSearch", "Query submitted: " + query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("AttireFareSearch", "Query changed: " + newText);
                filterAttireFareViolations(newText);
                return true;
            }
        });
        int attireFareSearchTextId = attireFareSearch.getContext().getResources()
                .getIdentifier("android:id/search_src_text", null, null);
        TextView attireFareSearchText = attireFareSearch.findViewById(attireFareSearchTextId);
        if (attireFareSearchText != null) {
            attireFareSearchText.setTextColor(ContextCompat.getColor(this, R.color.black));
            attireFareSearchText.setTextSize(16); // Match header font size
            attireFareSearchText.setTypeface(Typeface.DEFAULT); // Not bold
            attireFareSearchText.setPadding(
                    attireFareSearchText.getPaddingLeft(),
                    attireFareSearchText.getPaddingTop(),
                    attireFareSearchText.getPaddingRight(),
                    attireFareSearchText.getPaddingBottom() + 10 // Push underline lower
            );
        }


    }


    private void filterParkingViolations(String query) {
        String normalizedQuery = query.toLowerCase().trim();

        for (CheckBox checkbox : parkingCheckboxes) {
            if (checkbox == null) {
                Log.e("ParkingSearch", "Checkbox is null — skipping");
                continue;
            }

            String label = checkbox.getText().toString().toLowerCase().trim();
            Log.d("ParkingSearch", "Comparing label: '" + label + "' with query: '" + normalizedQuery + "'");
            checkbox.setVisibility(label.contains(normalizedQuery) ? View.VISIBLE : View.GONE);
        }
    }

    private void filterTrafficViolations(String query) {
        String normalizedQuery = query.toLowerCase().trim();

        for (CheckBox checkbox : trafficCheckboxes) {
            if (checkbox == null) {
                Log.e("TrafficSearch", "Checkbox is null — skipping");
                continue;
            }

            String label = checkbox.getText().toString().toLowerCase().trim();
            Log.d("TrafficSearch", "Comparing label: '" + label + "' with query: '" + normalizedQuery + "'");
            checkbox.setVisibility(label.contains(normalizedQuery) ? View.VISIBLE : View.GONE);
        }
    }

    private void filterDriverBehaviorViolations(String query) {
        String normalizedQuery = query.toLowerCase().trim();

        for (CheckBox checkbox : driverBehaviorCheckboxes) {
            if (checkbox == null) {
                Log.e("DriverBehaviorSearch", "Checkbox is null — skipping");
                continue;
            }

            String label = checkbox.getText().toString().toLowerCase().trim();
            Log.d("DriverBehaviorSearch", "Comparing label: '" + label + "' with query: '" + normalizedQuery + "'");
            checkbox.setVisibility(label.contains(normalizedQuery) ? View.VISIBLE : View.GONE);
        }
    }

    private void filterLicensingViolations(String query) {
        String normalizedQuery = query.toLowerCase().trim();

        for (CheckBox checkbox : licensingCheckboxes) {
            if (checkbox == null) {
                Log.e("LicensingSearch", "Checkbox is null — skipping");
                continue;
            }

            String label = checkbox.getText().toString().toLowerCase().trim();
            Log.d("LicensingSearch", "Comparing label: '" + label + "' with query: '" + normalizedQuery + "'");
            checkbox.setVisibility(label.contains(normalizedQuery) ? View.VISIBLE : View.GONE);
        }
    }

    private void filterAttireFareViolations(String query) {
        String normalizedQuery = query.toLowerCase().trim();

        for (CheckBox checkbox : attireFareCheckboxes) {
            if (checkbox == null) {
                Log.e("AttireFareSearch", "Checkbox is null — skipping");
                continue;
            }

            String label = checkbox.getText().toString().toLowerCase().trim();
            Log.d("AttireFareSearch", "Comparing label: '" + label + "' with query: '" + normalizedQuery + "'");
            checkbox.setVisibility(label.contains(normalizedQuery) ? View.VISIBLE : View.GONE);
        }
    }


    private void toggleCheckboxVisibility(CheckBox checkbox, String query) {
        if (checkbox == null) {
            Log.e("ParkingSearch", "Checkbox is null — skipping");
            return;
        }

        String label = checkbox.getText().toString().toLowerCase().trim();
        String normalizedQuery = query.toLowerCase().trim();

        Log.d("ParkingSearch", "Comparing label: '" + label + "' with query: '" + normalizedQuery + "'");

        checkbox.setVisibility(label.contains(normalizedQuery) ? View.VISIBLE : View.GONE);
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

        // Display scanned data in the driver info block
        driverInfoText = findViewById(R.id.driver_info_text); // Make sure this TextView exists in your XML

        String info = "Driver Name: " + driverName + "\n" +
                "Driver ID: " + driverId + "\n" +
                "Franchise ID: " + franchiseId + "\n" +
                "Operator: " + operatorName + "\n" +
                "TODA: " + toda;

        driverInfoText.setText(info);
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
        commuterNameEditText = findViewById(R.id.edit_commuter_name);
        commuterContactEditText = findViewById(R.id.edit_commuter_contact);

        // Fallback: If SharedPreferences didn't populate fields, try Firebase
        if ((commuterNameEditText.getText() == null || commuterNameEditText.getText().toString().isEmpty()) ||
                (commuterContactEditText.getText() == null || commuterContactEditText.getText().toString().isEmpty())) {

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
        }

        // Bind QR fields for display
        TextView driverIdTextView = findViewById(R.id.driver_id_text_view);
        TextView driverNameTextView = findViewById(R.id.driver_name_text_view);
        TextView franchiseIdTextView = findViewById(R.id.franchise_id_text_view);
        TextView operatorIdTextView = findViewById(R.id.operator_id_text_view);
        TextView todaTextView = findViewById(R.id.toda_text_view);

        // Display scanned QR data
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
                // Validate driver info from QR
                if (driverId == null || driverName == null || franchiseId == null) {
                    Toast.makeText(this, "Missing driver info. Please rescan the QR code.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate commuter info
                String commuterName = commuterNameEditText.getText().toString().trim();
                String commuterContact = commuterContactEditText.getText().toString().trim();

                if (commuterName.isEmpty() || commuterContact.isEmpty()) {
                    Toast.makeText(this, "Please ensure your name and contact number are filled in.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Optional: Validate image presence
                if (imagePreview.getDrawable() == null) {
                    Toast.makeText(this, "Please attach a photo before submitting.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // All good — show preview
                showReportSummaryDialog();
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

                // Rotate toggle icon for feedback
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
                            photoUri = uri; // Store for consistency
                            previewImage(uri);
                            imagePreview.setContentDescription("Selected image preview");
                            submitButton.setEnabled(true);
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
                            imagePreview.setContentDescription("Captured photo preview");
                            showToast("Photo captured successfully!");
                            submitButton.setEnabled(true);
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

            // Animate image appearance
            imagePreview.setAlpha(0f);
            imagePreview.animate().alpha(1f).setDuration(300).start();

            // Enable submit button
            submitButton.setEnabled(true);

            // Show remove button
            if (removePhotoButton != null) {
                removePhotoButton.setVisibility(View.VISIBLE);
            }

            // Log dimensions
            imagePreview.post(() -> {
                int width = imagePreview.getWidth();
                int height = imagePreview.getHeight();
                Log.d("ReportActivity", "Preview dimensions: " + width + "x" + height);
            });
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
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/jpeg", "image/png", "image/webp"});
        pickImageLauncher.launch(Intent.createChooser(i, "Select an image"));
    }


    private void checkCameraPermissionAndOpen() {
        Log.d("ReportActivity", "checkCameraPermissionAndOpen triggered");

        // Determine required permissions based on Android version
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES};
        } else {
            permissions = new String[]{Manifest.permission.CAMERA};
        }

        // Check if camera permission is already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            // Show rationale if needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                new AlertDialog.Builder(this)
                        .setTitle("Camera Permission Required")
                        .setMessage("To capture a violation photo, the app needs access to your camera.")
                        .setPositiveButton("Allow", (dialog, which) -> {
                            ActivityCompat.requestPermissions(this, permissions, CAMERA_PERMISSION_REQUEST_CODE);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                // Directly request permissions
                ActivityCompat.requestPermissions(this, permissions, CAMERA_PERMISSION_REQUEST_CODE);
            }
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
        Log.d("ReportActivity", "submitReport() triggered");

        String commuterName = commuterNameEditText != null ? commuterNameEditText.getText().toString().trim() : "";
        String commuterContact = commuterContactEditText != null ? commuterContactEditText.getText().toString().trim() : "";

        Log.d("ReportActivity", "Commuter name: " + commuterName);
        Log.d("ReportActivity", "Commuter contact: " + commuterContact);

        // Normalize contact number
        commuterContact = commuterContact.replaceAll("[\\s-]", "");

        boolean isAutoFilledName = commuterNameEditText != null && !commuterNameEditText.isEnabled();
        boolean isAutoFilledContact = commuterContactEditText != null && !commuterContactEditText.isEnabled();

        if (!isAutoFilledName && (commuterName.isEmpty() || commuterName.matches(".*\\d.*"))) {
            commuterNameEditText.setError("Please enter a valid name.");
            showToast("Name is required and must not contain numbers.");
            Log.d("ReportActivity", "Invalid commuter name");
            return;
        }

        if (!isAutoFilledContact && (commuterContact.isEmpty() || !commuterContact.matches("^09\\d{9}$"))) {
            commuterContactEditText.setError("Invalid Philippine mobile number.");
            showToast("Please enter a valid contact number.");
            Log.d("ReportActivity", "Invalid commuter contact");
            return;
        }

        String parking = extractInput(parkingObstructionOptions);
        String movement = extractInput(trafficMovementOptions);
        String behavior = extractInput(driverBehaviorOptions);
        String licensing = extractInput(licensingDocumentationOptions);
        String attire = extractInput(attireFareOptions);
        String imgDesc = imageDescriptionInput != null ? imageDescriptionInput.getText().toString().trim() : "";
        Uri uri = (imagePreview != null && imagePreview.getTag() instanceof Uri) ? (Uri) imagePreview.getTag() : null;

        Log.d("ReportActivity", "Image URI: " + uri);

        if (isAllEmpty(parking, movement, behavior, licensing, attire) && uri == null) {
            showToast("Provide at least one violation or an image.");
            Log.d("ReportActivity", "No violations or image provided");
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
                operatorName,
                toda,
                commuterName,
                commuterContact,
                parking,
                movement,
                behavior,
                licensing,
                attire,
                imgDesc,
                null,
                timestamp,
                "Pending",
                null
        );

        Log.d("ReportActivity", "Report object created: " + new Gson().toJson(report));

        if (uri != null) {
            Log.d("ReportActivity", "Uploading image before submitting report");
            uploadImageThenSubmit(uri, report);
        } else {
            Log.d("ReportActivity", "No image attached. Submitting report directly");
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

        try (InputStream stream = getContentResolver().openInputStream(uri)) {
            byte[] imageBytes = readBytes(stream);

            RequestBody body = RequestBody.create(imageBytes, MediaType.parse("image/jpeg"));
            String uploadUrl = buildImageUrl(filename);

            Request request = new Request.Builder()
                    .url(uploadUrl)
                    .put(body)
                    .build();

            // Optional: show loading indicator here

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                    // Optional: hide loading indicator here

                    if (response.isSuccessful()) {
                        String imageUrl = buildImageUrl(filename);
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
                    // Optional: hide loading indicator here
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

    private String buildImageUrl(String filename) {
        return "https://rtwrbkrroilftdhggxjc.supabase.co/storage/v1/object/report-images/" + filename;
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

        //  Retrieve and log Firebase UID
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
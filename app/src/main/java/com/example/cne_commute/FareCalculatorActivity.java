package com.example.cne_commute;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FareCalculatorActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final Logger logger = Logger.getLogger(FareCalculatorActivity.class.getName());

    private FusedLocationProviderClient fusedLocationClient;
    private CancellationTokenSource cancellationTokenSource;
    private Location startLocation;
    private Location destinationLocation;

    private TextView startingLocationText, destinationLocationText, totalKmText, totalFareText;
    private Spinner userAgeSpinner, companionAgeSpinner;
    private CheckBox userDiscountCheckbox, companionDiscountCheckbox;
    private Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fare_calculator);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        startingLocationText = findViewById(R.id.startingLocation1);
        destinationLocationText = findViewById(R.id.chosenDestination1);
        totalKmText = findViewById(R.id.totalkm1);
        totalFareText = findViewById(R.id.totalFare1);

        userAgeSpinner = findViewById(R.id.user_age_spinner);
        companionAgeSpinner = findViewById(R.id.companion_age_spinner);

        userDiscountCheckbox = findViewById(R.id.user_discount_checkbox);
        companionDiscountCheckbox = findViewById(R.id.companion_discount_checkbox);

        startButton = findViewById(R.id.startButton1);
        Button stopButton = findViewById(R.id.stopButton1);
        Button resetButton = findViewById(R.id.resetButton1);

        // Custom adapter with grayed-out first item
        ArrayAdapter<String> customAgeAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.age_group_options)) {

            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        customAgeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        userAgeSpinner.setAdapter(customAgeAdapter);
        companionAgeSpinner.setAdapter(customAgeAdapter);

        // Disable Start button initially
        startButton.setEnabled(false);

        userAgeSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selected = userAgeSpinner.getSelectedItem().toString();
                startButton.setEnabled(!selected.equals("Choose your age group"));
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                startButton.setEnabled(false);
            }
        });

        // Prevent "Start" if Spinner 1 is not valid
        startButton.setOnClickListener(v -> {
            String selected = userAgeSpinner.getSelectedItem().toString();
            if (selected.equals("Choose your age group")) {
                Toast.makeText(FareCalculatorActivity.this, "Please select your age group", Toast.LENGTH_SHORT).show();
            } else {
                resetCommuterState();
            }
        });

        stopButton.setOnClickListener(v -> setDestinationLocationAndCalculateFare());
        resetButton.setOnClickListener(v -> resetFieldsOnly());

        requestLocationPermission();
        initializeBottomNavigation();
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Location permission is required to use this feature", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetCommuterState() {
        startLocation = null;
        destinationLocation = null;

        startingLocationText.setText("");
        destinationLocationText.setText("");
        totalKmText.setText("0.00 km");
        totalFareText.setText("₱ 0.00");

        getCurrentLocation(location -> {
            startLocation = location;
            String address = getAddressFromLocation(location);
            if (address == null) address = getCoordinatesString(location);
            startingLocationText.setText(address);
        });
    }

    private void resetFieldsOnly() {
        startLocation = null;
        destinationLocation = null;

        startingLocationText.setText("");
        destinationLocationText.setText("");
        totalKmText.setText("0.00 km");
        totalFareText.setText("₱ 0.00");

        Toast.makeText(this, "Fields have been reset", Toast.LENGTH_SHORT).show();
    }

    private void setDestinationLocationAndCalculateFare() {
        getCurrentLocation(location -> {
            destinationLocation = location;
            String address = getAddressFromLocation(location);
            if (address == null) address = getCoordinatesString(location);
            destinationLocationText.setText(address);
            calculateFare();
        });
    }

    private void calculateFare() {
        if (startLocation == null || destinationLocation == null) {
            totalFareText.setText("₱ 0.00");
            totalKmText.setText("0.00 km");
            return;
        }

        String userAgeGroup = userAgeSpinner.getSelectedItem().toString();
        String companionAgeGroup = companionAgeSpinner.getSelectedItem().toString();

        if (userAgeGroup.equals("Choose your age group")) {
            Toast.makeText(this, "Please select your age group", Toast.LENGTH_SHORT).show();
            totalFareText.setText("₱ 0.00");
            return;
        }

        float[] results = new float[1];
        Location.distanceBetween(
                startLocation.getLatitude(), startLocation.getLongitude(),
                destinationLocation.getLatitude(), destinationLocation.getLongitude(),
                results);

        float distanceInKm = results[0] / 1000f;
        totalKmText.setText(String.format(Locale.getDefault(), "%.2f km", distanceInKm));

        double fare1 = calculateIndividualFare(userAgeGroup, userDiscountCheckbox.isChecked(), distanceInKm);
        double fare2 = 0.0;

        if (!companionAgeGroup.equals("Choose your age group")) {
            fare2 = calculateIndividualFare(companionAgeGroup, companionDiscountCheckbox.isChecked(), distanceInKm);
        }

        double totalFare = fare1 + fare2;
        totalFareText.setText(String.format(Locale.getDefault(), "₱ %.2f", totalFare));
    }

    private double calculateIndividualFare(String ageGroup, boolean hasDiscount, float distanceInKm) {
        double baseFare = 0.0;
        double perKmFare = 0.0;

        // Assign base fare and per km fare depending on age group and discount
        switch (ageGroup) {
            case "0-3":
                baseFare = hasDiscount ? 12.00 : 15.00;
                perKmFare = hasDiscount ? 1.60 : 2.00;
                break;
            case "3-5":
                baseFare = hasDiscount ? 5.60 : 7.00;
                perKmFare = hasDiscount ? 1.60 : 2.00;
                break;
            case "6+":
            default:
                baseFare = hasDiscount ? 12.00 : 15.00;
                perKmFare = hasDiscount ? 1.60 : 2.00;
                break;
        }

        // Fare calculation
        if (distanceInKm <= 2.0) {
            return baseFare;
        } else {
            int extraKm = (int) Math.ceil(distanceInKm - 2.0);
            return baseFare + (extraKm * perKmFare);
        }
    }



    private void getCurrentLocation(OnSuccessListener<Location> callback) {
        cancellationTokenSource = new CancellationTokenSource();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        callback.onSuccess(location);
                    } else {
                        Toast.makeText(this, "Unable to fetch location", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    logger.log(Level.SEVERE, "Failed to fetch location", e);
                    Toast.makeText(this, "Failed to fetch location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String getAddressFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address addr = addresses.get(0);
                String province = addr.getAdminArea();
                String subAdmin = addr.getSubAdminArea();

                if ((province != null && province.contains("Camarines Norte")) ||
                        (subAdmin != null && subAdmin.contains("Camarines Norte"))) {

                    StringBuilder address = new StringBuilder();
                    if (addr.getFeatureName() != null) address.append(addr.getFeatureName()).append(", ");
                    if (addr.getThoroughfare() != null) address.append(addr.getThoroughfare()).append(", ");
                    if (addr.getSubLocality() != null) address.append(addr.getSubLocality()).append(", ");
                    if (addr.getLocality() != null) address.append(addr.getLocality()).append(", ");
                    if (addr.getSubAdminArea() != null) address.append(addr.getSubAdminArea());

                    return address.toString().replaceAll(", $", "");
                } else {
                    return "Location not within Camarines Norte";
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to retrieve address", e);
        }
        return "Unable to determine location";
    }

    private String getCoordinatesString(Location location) {
        return String.format(Locale.getDefault(), "%.4f, %.4f", location.getLatitude(), location.getLongitude());
    }

    private void initializeBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_calculator) {
                return true;
            } else if (itemId == R.id.nav_home) {
                startActivity(new Intent(FareCalculatorActivity.this, HomeActivity.class));
                overridePendingTransition(R.anim.fade_in, 0);
                return true;
            } else if (itemId == R.id.nav_history) {
                startActivity(new Intent(FareCalculatorActivity.this, HistoryActivity.class));
                overridePendingTransition(R.anim.fade_in, 0);
                return true;
            } else if (itemId == R.id.nav_account) {
                startActivity(new Intent(FareCalculatorActivity.this, AccountActivity.class));
                overridePendingTransition(R.anim.fade_in, 0);
                return true;
            } else {
                return false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cancellationTokenSource != null) {
            cancellationTokenSource.cancel();
        }
    }
}

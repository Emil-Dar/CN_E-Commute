package com.example.cne_commute;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
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

    private static final double BASE_FARE = 50.0; // Initial fare
    private static final double FARE_PER_KM = 10.0; // Fare per kilometer
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private TextView startingLocation, destinationLocation, totalFareText;
    private Button startButton, stopButton;

    private Location startLocation, destinationLocationObj;
    private FusedLocationProviderClient fusedLocationClient;

    private static final Logger logger = Logger.getLogger(FareCalculatorActivity.class.getName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fare_calculator);

        // Initialize UI components and location client
        initializeUI();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Request location permissions if not already granted
        requestLocationPermission();

        // Set listeners for the buttons
        setListeners();

        // Initialize Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_calculator) {
                // Navigate to FareCalculatorActivity
                startActivity(new Intent(FareCalculatorActivity.this, FareCalculatorActivity.class));
                return true;
            } else if (itemId == R.id.nav_home) {
                // Navigate to FareCalculatorActivity HomeActivity
                startActivity(new Intent(FareCalculatorActivity.this, HomeActivity.class));
                return true;
            } else if (itemId == R.id.nav_history) {
                // Navigate to HistoryActivity
                startActivity(new Intent(FareCalculatorActivity.this, HistoryActivity.class));
                return true;
            } else if (itemId == R.id.nav_account) {
                // Navigate to AccountActivity
                startActivity(new Intent(FareCalculatorActivity.this, AccountActivity.class));
                return true;
            } else {
                return false;
            }
        });
    }

    private void initializeUI() {
        startingLocation = findViewById(R.id.startingLocation1);
        destinationLocation = findViewById(R.id.chosenDestination1);
        totalFareText = findViewById(R.id.totalFare1);
        startButton = findViewById(R.id.startButton1);
        stopButton = findViewById(R.id.stopButton1);
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location permission is required to use this feature", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setListeners() {
        startButton.setOnClickListener(v -> setStartingLocation());
        stopButton.setOnClickListener(v -> setDestinationLocationAndCalculateFare());
    }

    private void setStartingLocation() {
        getCurrentLocation(location -> {
            startLocation = location;
            // Use a ternary operator to handle null addresses
            String address = (getAddressFromLocation(location) != null) ? getAddressFromLocation(location) : getCoordinatesString(location);
            startingLocation.setText(address);
        });
    }

    private void setDestinationLocationAndCalculateFare() {
        getCurrentLocation(location -> {
            destinationLocationObj = location;
            // Use a ternary operator to handle null addresses
            String address = (getAddressFromLocation(location) != null) ? getAddressFromLocation(location) : getCoordinatesString(location);
            destinationLocation.setText(address);
            calculateFare();
        });
    }

    private void getCurrentLocation(OnSuccessListener<Location> callback) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    callback.onSuccess(location);
                } else {
                    // Request fresh location update
                    requestFreshLocation(callback);
                }
            });
        } else {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestFreshLocation(OnSuccessListener<Location> callback) {
        // Create a CancellationTokenSource to handle potential cancellations
        CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        callback.onSuccess(location);
                    } else {
                        Toast.makeText(this, "Unable to fetch location. Please try again.", Toast.LENGTH_SHORT).show();
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
                return addresses.get(0).getAddressLine(0);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to retrieve address", e);
            Toast.makeText(this, "Failed to retrieve address", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    private String getCoordinatesString(Location location) {
        return String.format(Locale.getDefault(), "%.4f, %.4f", location.getLatitude(), location.getLongitude());
    }

    private void calculateFare() {
        if (startLocation == null || destinationLocationObj == null) {
            totalFareText.setText(getString(R.string.fare_error));
            return;
        }

        // Compute the distance in meters
        float[] results = new float[1];
        Location.distanceBetween(
                startLocation.getLatitude(), startLocation.getLongitude(),
                destinationLocationObj.getLatitude(), destinationLocationObj.getLongitude(),
                results);

        // Convert distance to kilometers
        float distanceInKm = results[0] / 1000;

        // Calculate the total fare
        double totalFare = BASE_FARE + (distanceInKm * FARE_PER_KM);

        // Display the fare
        totalFareText.setText(String.format(Locale.getDefault(), "₱ %.2f", totalFare));
    }
}

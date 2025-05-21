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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FareCalculatorActivity extends AppCompatActivity {

    private static final double BASE_FARE = 50.0; // Initial fare
    private static final double FARE_PER_KM = 10.0; // Fare per kilometer
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private FusedLocationProviderClient fusedLocationClient;
    private static final Logger logger = Logger.getLogger(FareCalculatorActivity.class.getName());

    // HashMaps to track state for each commuter
    private final HashMap<Integer, Location> startLocations = new HashMap<>();
    private final HashMap<Integer, Location> destinationLocations = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fare_calculator);

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Request location permissions if not already granted
        requestLocationPermission();

        // Set listeners for each commuter
        setListenersForCommuters();

        // Initialize Bottom Navigation
        initializeBottomNavigation();
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

    private void setListenersForCommuters() {
        for (int i = 1; i <= 4; i++) {
            int commuterId = i;

            Button startButton = findViewById(getResources().getIdentifier("startButton" + commuterId, "id", getPackageName()));
            Button stopButton = findViewById(getResources().getIdentifier("stopButton" + commuterId, "id", getPackageName()));

            // Start button resets the state for the commuter and sets the starting location
            startButton.setOnClickListener(v -> resetCommuterState(commuterId));

            // Stop button records the destination and calculates the fare
            stopButton.setOnClickListener(v -> setDestinationLocationAndCalculateFare(commuterId));
        }
    }

    private void resetCommuterState(int commuterId) {
        // Reset the starting location and destination for the commuter
        startLocations.put(commuterId, null);
        destinationLocations.put(commuterId, null);

        // Clear the UI elements (text views)
        TextView startingLocationText = findViewById(getResources().getIdentifier("startingLocation" + commuterId, "id", getPackageName()));
        TextView destinationLocationText = findViewById(getResources().getIdentifier("chosenDestination" + commuterId, "id", getPackageName()));
        TextView totalKmText = findViewById(getResources().getIdentifier("totalkm" + commuterId, "id", getPackageName()));
        TextView totalFareText = findViewById(getResources().getIdentifier("totalFare" + commuterId, "id", getPackageName()));

        // Set default text instead of clearing them
        startingLocationText.setText("");
        destinationLocationText.setText("");

        // Set default values for Total KM and Total Fare so the layout doesn't shrink
        totalKmText.setText("0.00 km");
        totalFareText.setText("₱ 0.00");

        // Set the starting location for the commuter
        getCurrentLocation(location -> {
            startLocations.put(commuterId, location);

            String address = (getAddressFromLocation(location) != null) ? getAddressFromLocation(location) : getCoordinatesString(location);
            startingLocationText.setText(address);
        });
    }

    private void setDestinationLocationAndCalculateFare(int commuterId) {
        getCurrentLocation(location -> {
            destinationLocations.put(commuterId, location);

            String address = (getAddressFromLocation(location) != null) ? getAddressFromLocation(location) : getCoordinatesString(location);
            TextView destinationLocationText = findViewById(getResources().getIdentifier("chosenDestination" + commuterId, "id", getPackageName()));
            destinationLocationText.setText(address);

            // Calculate the fare once the destination is set
            calculateFare(commuterId);
        });
    }

    private void calculateFare(int commuterId) {
        Location startLocation = startLocations.get(commuterId);
        Location destinationLocation = destinationLocations.get(commuterId);

        if (startLocation == null || destinationLocation == null) {
            TextView totalFareText = findViewById(getResources().getIdentifier("totalFare" + commuterId, "id", getPackageName()));
            totalFareText.setText("₱ 0.00");  // Keep the text instead of leaving it empty
            return;
        }

        float[] results = new float[1];
        Location.distanceBetween(
                startLocation.getLatitude(), startLocation.getLongitude(),
                destinationLocation.getLatitude(), destinationLocation.getLongitude(),
                results);

        float distanceInKm = results[0] / 1000;

        double totalFare;
        if (distanceInKm <= 2) {
            totalFare = 15.0;
        } else {
            int additionalKmRanges = (int) Math.ceil(distanceInKm - 2);
            totalFare = 15.0 + (additionalKmRanges * 2.0);
        }

        TextView totalKmText = findViewById(getResources().getIdentifier("totalkm" + commuterId, "id", getPackageName()));
        TextView totalFareText = findViewById(getResources().getIdentifier("totalFare" + commuterId, "id", getPackageName()));

        totalKmText.setText(String.format(Locale.getDefault(), "%.2f km", distanceInKm));
        totalFareText.setText(String.format(Locale.getDefault(), "₱ %.2f", totalFare));
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



    private void initializeBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_calculator) {
                startActivity(new Intent(FareCalculatorActivity.this, FareCalculatorActivity.class));
                return true;
            } else if (itemId == R.id.nav_home) {
                startActivity(new Intent(FareCalculatorActivity.this, HomeActivity.class));
                return true;
            } else if (itemId == R.id.nav_history) {
                startActivity(new Intent(FareCalculatorActivity.this, HistoryActivity.class));
                return true;
            } else if (itemId == R.id.nav_account) {
                startActivity(new Intent(FareCalculatorActivity.this, AccountActivity.class));
                return true;
            } else {
                return false;
            }
        });
    }
}
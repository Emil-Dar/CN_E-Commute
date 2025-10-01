package com.example.cne_commute.driver_fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.cne_commute.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DriverHome extends Fragment {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final Logger logger = Logger.getLogger(DriverHome.class.getName());

    private FusedLocationProviderClient fusedLocationClient;
    private CancellationTokenSource cancellationTokenSource;
    private Location startLocation;
    private Location destinationLocation;

    private TextView startLocationText, destinationText, totalKmText, totalFareText;
    private Spinner userAgeSpinner, companionAgeSpinner;
    private CheckBox userDiscountCheck, companionDiscountCheck;
    private Button startBtn, stopBtn, payBtn, rentBtn;
    private ImageButton resetBtn;

    public DriverHome() {
        // Required empty public constructor
    }

    public static DriverHome newInstance() {
        return new DriverHome();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_driver_home, container, false);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // UI References
        startLocationText = view.findViewById(R.id.startingLocation1);
        destinationText = view.findViewById(R.id.chosenDestination1);
        totalKmText = view.findViewById(R.id.totalkm1);
        totalFareText = view.findViewById(R.id.totalFare1);

        userAgeSpinner = view.findViewById(R.id.user_age_spinner);
        companionAgeSpinner = view.findViewById(R.id.companion_age_spinner);
        userDiscountCheck = view.findViewById(R.id.user_discount_checkbox);
        companionDiscountCheck = view.findViewById(R.id.companion_discount_checkbox);

        startBtn = view.findViewById(R.id.btnStart);
        stopBtn = view.findViewById(R.id.btnStop);
        resetBtn = view.findViewById(R.id.btnReset);
        payBtn = view.findViewById(R.id.btnPay);
        rentBtn = view.findViewById(R.id.btnRent);

        setupAgeSpinners();
        setupButtonListeners();

        requestLocationPermission();

        return view;
    }

    private void setupAgeSpinners() {
        ArrayAdapter<String> customAgeAdapter = new ArrayAdapter<String>(
                requireContext(),
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
                if (position == 0) tv.setTextColor(Color.GRAY);
                else tv.setTextColor(Color.BLACK);
                return view;
            }
        };
        customAgeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userAgeSpinner.setAdapter(customAgeAdapter);
        companionAgeSpinner.setAdapter(customAgeAdapter);

        startBtn.setEnabled(false);

        userAgeSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int pos, long id) {
                startBtn.setEnabled(!userAgeSpinner.getSelectedItem().toString().equals("Choose your age group"));
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void setupButtonListeners() {
        startBtn.setOnClickListener(v -> {
            if (userAgeSpinner.getSelectedItem().toString().equals("Choose your age group")) {
                Toast.makeText(requireContext(), "Please select your age group", Toast.LENGTH_SHORT).show();
            } else {
                resetCommuterState();
            }
        });

        stopBtn.setOnClickListener(v -> setDestinationLocationAndCalculateFare());
        resetBtn.setOnClickListener(v -> resetFieldsOnly());

        payBtn.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Payment processed successfully!", Toast.LENGTH_SHORT).show()
        );

        rentBtn.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Vehicle marked as For Rent", Toast.LENGTH_SHORT).show()
        );
    }

    // ---------------- LOCATION + FARE LOGIC ---------------- //

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        }
    }

    private void resetCommuterState() {
        startLocation = null;
        destinationLocation = null;

        startLocationText.setText("");
        destinationText.setText("");
        totalKmText.setText("0.00 km");
        totalFareText.setText("₱ 0.00");

        getCurrentLocation(location -> {
            startLocation = location;
            String address = getAddressFromLocation(location);
            if (address == null) address = getCoordinatesString(location);
            startLocationText.setText(address);
        });
    }

    private void resetFieldsOnly() {
        startLocation = null;
        destinationLocation = null;

        startLocationText.setText("");
        destinationText.setText("");
        totalKmText.setText("0.00 km");
        totalFareText.setText("₱ 0.00");
        Toast.makeText(requireContext(), "Fields reset", Toast.LENGTH_SHORT).show();
    }

    private void setDestinationLocationAndCalculateFare() {
        getCurrentLocation(location -> {
            destinationLocation = location;
            String address = getAddressFromLocation(location);
            if (address == null) address = getCoordinatesString(location);
            destinationText.setText(address);
            calculateFare();
        });
    }

    private void calculateFare() {
        if (startLocation == null || destinationLocation == null) return;

        String userAge = userAgeSpinner.getSelectedItem().toString();
        String companionAge = companionAgeSpinner.getSelectedItem().toString();

        float[] results = new float[1];
        Location.distanceBetween(
                startLocation.getLatitude(), startLocation.getLongitude(),
                destinationLocation.getLatitude(), destinationLocation.getLongitude(),
                results);

        float distanceKm = results[0] / 1000f;
        totalKmText.setText(String.format(Locale.getDefault(), "%.2f km", distanceKm));

        double fare1 = calculateIndividualFare(userAge, userDiscountCheck.isChecked(), distanceKm);
        double fare2 = (!companionAge.equals("Choose your age group"))
                ? calculateIndividualFare(companionAge, companionDiscountCheck.isChecked(), distanceKm)
                : 0.0;

        double total = fare1 + fare2;
        totalFareText.setText(String.format(Locale.getDefault(), "₱ %.2f", total));
    }

    private double calculateIndividualFare(String ageGroup, boolean discounted, float distanceKm) {
        double baseFare, perKm;
        switch (ageGroup) {
            case "0-3":
                baseFare = discounted ? 12.00 : 15.00;
                perKm = discounted ? 1.60 : 2.00;
                break;
            case "3-5":
                baseFare = discounted ? 5.60 : 7.00;
                perKm = discounted ? 1.60 : 2.00;
                break;
            default:
                baseFare = discounted ? 12.00 : 15.00;
                perKm = discounted ? 1.60 : 2.00;
        }
        if (distanceKm <= 2.0) return baseFare;
        int extra = (int) Math.ceil(distanceKm - 2.0);
        return baseFare + (extra * perKm);
    }

    private void getCurrentLocation(OnSuccessListener<Location> callback) {
        cancellationTokenSource = new CancellationTokenSource();
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) callback.onSuccess(location);
                    else Toast.makeText(requireContext(), "Unable to fetch location", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    logger.log(Level.SEVERE, "Failed to fetch location", e);
                    Toast.makeText(requireContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String getAddressFromLocation(Location location) {
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address addr = addresses.get(0);
                return addr.getAddressLine(0);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Geocoder failed", e);
        }
        return "Unknown location";
    }

    private String getCoordinatesString(Location loc) {
        return String.format(Locale.getDefault(), "%.4f, %.4f", loc.getLatitude(), loc.getLongitude());
    }
}

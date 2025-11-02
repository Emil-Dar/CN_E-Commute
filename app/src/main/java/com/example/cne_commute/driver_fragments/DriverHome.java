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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DriverHome extends Fragment {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final Logger logger = Logger.getLogger(DriverHome.class.getName());

    private FusedLocationProviderClient fusedLocationClient;
    private CancellationTokenSource cancellationTokenSource;

    // List to hold all fare boxes
    private final List<FareBox> fareBoxes = new ArrayList<>();

    public DriverHome() {}

    public static DriverHome newInstance() {
        return new DriverHome();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_driver_home, container, false);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Initialize all fare boxes dynamically
        initFareBox(view, R.id.fareBox1);
        initFareBox(view, R.id.fareBox2);
        initFareBox(view, R.id.fareBox3);
        initFareBox(view, R.id.fareBox4);

        requestLocationPermission();
        return view;
    }

    /** Initialize each fare box layout **/
    private void initFareBox(View parentView, int includeId) {
        View fareBoxView = parentView.findViewById(includeId);
        if (fareBoxView == null) return;

        FareBox box = new FareBox(fareBoxView);
        fareBoxes.add(box);
        box.setupAgeSpinners();
        box.setupButtonListeners();
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        }
    }

    /** Get location helper **/
    private void getCurrentLocation(OnSuccessListener<Location> callback) {
        cancellationTokenSource = new CancellationTokenSource();
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
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

    // ==============================
    // Inner class for each fare box
    // ==============================
    private class FareBox {
        private final Spinner spinnerAge, spinnerCompanionAge;
        private final CheckBox discountCheck, companionDiscountCheck;
        private final TextView txtStart, txtEnd, txtKm, txtFare;
        private final Button btnStart, btnStop, btnPay, btnRent;
        private final ImageButton btnReset;

        private Location startLocation, destinationLocation;

        FareBox(View root) {
            spinnerAge = root.findViewById(R.id.spinnerAge);
            spinnerCompanionAge = root.findViewById(R.id.spinnerCompanionAge);
            discountCheck = root.findViewById(R.id.checkboxDiscount);
            companionDiscountCheck = root.findViewById(R.id.checkboxCompanionDiscount);
            txtStart = root.findViewById(R.id.startLocation);
            txtEnd = root.findViewById(R.id.endLocation);
            txtKm = root.findViewById(R.id.txtKm);
            txtFare = root.findViewById(R.id.txtFare);
            btnStart = root.findViewById(R.id.btnStart);
            btnStop = root.findViewById(R.id.btnStop);
            btnPay = root.findViewById(R.id.btnPay);
            btnRent = root.findViewById(R.id.btnRent);
            btnReset = root.findViewById(R.id.btnReset);
        }

        void setupAgeSpinners() {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
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
                    tv.setTextColor(position == 0 ? Color.GRAY : Color.BLACK);
                    return view;
                }
            };
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerAge.setAdapter(adapter);
            spinnerCompanionAge.setAdapter(adapter);

            btnStart.setEnabled(false);
            spinnerAge.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(android.widget.AdapterView<?> parent, View view, int pos, long id) {
                    btnStart.setEnabled(!spinnerAge.getSelectedItem().toString().equals("Choose your age group"));
                }
                @Override
                public void onNothingSelected(android.widget.AdapterView<?> parent) {}
            });
        }

        void setupButtonListeners() {
            btnStart.setOnClickListener(v -> {
                if (spinnerAge.getSelectedItem().toString().equals("Choose your age group")) {
                    Toast.makeText(requireContext(), "Please select your age group", Toast.LENGTH_SHORT).show();
                } else {
                    resetCommuterState();
                }
            });

            btnStop.setOnClickListener(v -> setDestinationLocationAndCalculateFare());
            btnReset.setOnClickListener(v -> resetFieldsOnly());
            btnPay.setOnClickListener(v -> Toast.makeText(requireContext(), "Payment processed!", Toast.LENGTH_SHORT).show());
            btnRent.setOnClickListener(v -> Toast.makeText(requireContext(), "Vehicle marked as For Rent", Toast.LENGTH_SHORT).show());
        }

        private void resetCommuterState() {
            startLocation = null;
            destinationLocation = null;
            txtStart.setText("");
            txtEnd.setText("");
            txtKm.setText("0.00");
            txtFare.setText("₱0.00");

            getCurrentLocation(location -> {
                startLocation = location;
                String address = getAddressFromLocation(location);
                if (address == null) address = getCoordinatesString(location);
                txtStart.setText(address);
            });
        }

        private void resetFieldsOnly() {
            startLocation = null;
            destinationLocation = null;
            txtStart.setText("");
            txtEnd.setText("");
            txtKm.setText("0.00");
            txtFare.setText("₱0.00");
        }

        private void setDestinationLocationAndCalculateFare() {
            getCurrentLocation(location -> {
                destinationLocation = location;
                String address = getAddressFromLocation(location);
                if (address == null) address = getCoordinatesString(location);
                txtEnd.setText(address);
                calculateFare();
            });
        }

        private void calculateFare() {
            if (startLocation == null || destinationLocation == null) return;

            String userAge = spinnerAge.getSelectedItem().toString();
            String companionAge = spinnerCompanionAge.getSelectedItem().toString();

            float[] results = new float[1];
            Location.distanceBetween(
                    startLocation.getLatitude(), startLocation.getLongitude(),
                    destinationLocation.getLatitude(), destinationLocation.getLongitude(),
                    results);

            float distanceKm = results[0] / 1000f;
            txtKm.setText(String.format(Locale.getDefault(), "%.2f", distanceKm));

            double fare1 = calculateIndividualFare(userAge, discountCheck.isChecked(), distanceKm);
            double fare2 = (!companionAge.equals("Choose your age group"))
                    ? calculateIndividualFare(companionAge, companionDiscountCheck.isChecked(), distanceKm)
                    : 0.0;

            double total = fare1 + fare2;
            txtFare.setText(String.format(Locale.getDefault(), "₱%.2f", total));
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
    }
}

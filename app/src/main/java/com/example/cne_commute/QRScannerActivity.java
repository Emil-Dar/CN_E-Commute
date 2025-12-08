package com.example.cne_commute;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONObject;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class QRScannerActivity extends AppCompatActivity {

    private static final String TAG = "QRScannerActivity";
    private static final int CAMERA_PERMISSION_REQUEST = 101;

    private DecoratedBarcodeView barcodeScannerView;
    private Button rescanButton;
    private View subtitleText;

    private boolean hasScanned = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);

        Log.d(TAG, "onCreate: QRScannerActivity started");

        setupToolbar();
        subtitleText = findViewById(R.id.subtitle_text);
        rescanButton = findViewById(R.id.scan_qr_button);
        barcodeScannerView = findViewById(R.id.barcode_scanner_view);

        if (checkCameraPermission()) {
            setupScanner();
        } else {
            requestCameraPermission();
        }

        setupRescanButton();
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Camera permission granted");
                setupScanner();
            } else {
                Toast.makeText(this, "Camera permission is required to scan QR codes.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("QR Scanner");
        }

        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void setupScanner() {
        barcodeScannerView.setTorchOff();
        barcodeScannerView.setStatusText("");

        Log.d(TAG, "setupScanner: Scanner initialized");

        barcodeScannerView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                Log.d(TAG, "barcodeResult triggered");

                if (result == null || result.getText() == null) return;

                if (hasScanned) return;   // IMPORTANT FIX
                hasScanned = true;

                String scannedData = result.getText();
                Log.d(TAG, "Scanned Raw Data → " + scannedData);

                ScannedQrCode scannedQrCode = parseQrData(scannedData);

                if (scannedQrCode == null) {
                    hasScanned = false; // allow retry
                    Toast.makeText(QRScannerActivity.this, "Invalid QR", Toast.LENGTH_SHORT).show();
                    return;
                }

                barcodeScannerView.pause();
                handleScan(scannedQrCode);
            }
        });


        barcodeScannerView.resume();
    }

    private void handleScan(ScannedQrCode scannedQrCode) {
        String timestamp = getCurrentTimestamp();
        scannedQrCode.setScanTimestamp(timestamp);
        scannedQrCode.addScanTimestamp(timestamp);

        Log.d(TAG, "handleScan: Timestamp added → " + timestamp);
        saveScan(scannedQrCode);
    }

    private void saveScan(ScannedQrCode scannedQrCode) {
        Log.d(TAG, "saveScan: Saving scanned QR code → " + scannedQrCode.getDriverName());

        QrCodeStorageHelper.saveQrCode(QRScannerActivity.this, scannedQrCode);

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        prefs.edit().putString("franchise_id", scannedQrCode.getFranchiseId()).apply();

        Toast.makeText(QRScannerActivity.this, "QR scanned successfully", Toast.LENGTH_SHORT).show();

        barcodeScannerView.setVisibility(View.GONE);
        rescanButton.setVisibility(View.GONE);
        subtitleText.setVisibility(View.GONE);

        Log.d(TAG, "saveScan: Redirecting to HistoryActivity");

        Intent intent = new Intent(QRScannerActivity.this, HistoryActivity.class);
        startActivity(intent);
        finish();
    }

    private void setupRescanButton() {
        if (rescanButton != null) {
            rescanButton.setOnClickListener(view -> {
                hasScanned = false;
                barcodeScannerView.resume();
            });
        }
    }
    private ScannedQrCode parseQrData(String raw) {
        Log.d(TAG, "parseQrData: Parsing JSON QR data");
        try {
            JSONObject obj = new JSONObject(raw);

            String driverId = obj.optString("driver_id", "");
            String franchiseId = obj.optString("franchise_id", "");
            String driverName = obj.optString("driver_name", "");
            String operatorName = obj.optString("operator_name", "");
            String toda = obj.optString("accredited_toda", "");

            if (driverName.isEmpty()) {
                Log.w(TAG, "parseQrData: Missing required field → driver_name");
                return null;
            }

            return new ScannedQrCode(
                    driverId,
                    franchiseId,
                    driverName,
                    "", "", "",
                    operatorName,
                    toda,
                    ""
            );

        } catch (Exception e) {
            Log.e(TAG, "parseQrData: Invalid JSON → " + raw, e);
            return null;
        }
    }



    private String getCurrentTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Scanner resumed");
        if (barcodeScannerView != null) barcodeScannerView.resume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart: Scanner resumed");
        if (barcodeScannerView != null) barcodeScannerView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: Scanner paused");
        if (barcodeScannerView != null) barcodeScannerView.pause();
    }
}

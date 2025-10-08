package com.example.cne_commute;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class QRScannerActivity extends AppCompatActivity {

    private static final String TAG = "QRScannerActivity";

    private DecoratedBarcodeView barcodeScannerView;
    private Button rescanButton;
    private View subtitleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);

        Log.d(TAG, "onCreate: QRScannerActivity started");

        setupToolbar();
        setupScanner();
        setupRescanButton();
        subtitleText = findViewById(R.id.subtitle_text);
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
        barcodeScannerView = findViewById(R.id.barcode_scanner_view);
        barcodeScannerView.setTorchOff();
        barcodeScannerView.setStatusText("");

        Log.d(TAG, "setupScanner: Scanner initialized");

        barcodeScannerView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result == null || result.getText() == null) {
                    Log.w(TAG, "barcodeResult: No barcode detected");
                    Toast.makeText(QRScannerActivity.this, "No barcode detected", Toast.LENGTH_LONG).show();
                    return;
                }

                String scannedData = result.getText();
                Log.d(TAG, "barcodeResult: Scanned Raw Data → " + scannedData);

                ScannedQrCode scannedQrCode = parseQrData(scannedData);

                if (scannedQrCode == null) {
                    Log.w(TAG, "barcodeResult: QR data parsing failed");
                    Toast.makeText(QRScannerActivity.this, "Invalid or incomplete QR data", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.d(TAG, "barcodeResult: QR data parsed successfully");
                barcodeScannerView.pause();
                handleScan(scannedQrCode);
            }
        });
    }

    private void handleScan(ScannedQrCode scannedQrCode) {
        String timestamp = getCurrentTimestamp();

        // ✅ Only add timestamp once
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
        rescanButton = findViewById(R.id.scan_qr_button);
        if (rescanButton != null) {
            rescanButton.setOnClickListener(view -> {
                Log.d(TAG, "Rescan button clicked");
                Toast.makeText(this, "Rescanning...", Toast.LENGTH_SHORT).show();
                barcodeScannerView.resume();
            });
        }
    }

    private ScannedQrCode parseQrData(String raw) {
        Log.d(TAG, "parseQrData: Parsing raw QR data");

        String[] lines = raw.split("\\r?\\n");

        String driverId = "", franchiseId = "", driverName = "";
        String vehiclePlate = "", route = "", operatorName = "", toda = "";

        for (String line : lines) {
            String normalized = line.toLowerCase().replaceAll("[^a-z0-9]", "");

            if (normalized.contains("driverid")) {
                driverId = line.substring(line.indexOf(":") + 1).trim();
            } else if (normalized.contains("drivername")) {
                driverName = line.substring(line.indexOf(":") + 1).trim();
            } else if (normalized.contains("franchiseid")) {
                franchiseId = line.substring(line.indexOf(":") + 1).trim();
            } else if (normalized.contains("operatorname")) {
                operatorName = line.substring(line.indexOf(":") + 1).trim();
            } else if (normalized.contains("toda")) {
                toda = line.substring(line.indexOf(":") + 1).trim();
            }
        }

        if (driverName.isEmpty()) {
            Log.w(TAG, "parseQrData: Missing required field → driverName");
            return null;
        }

        Log.d(TAG, "parseQrData: Parsed driver → " + driverName);

        // ✅ Leave timestamp blank — will be set in handleScan()
        return new ScannedQrCode(
                driverId,
                franchiseId,
                driverName,
                "", // contact removed
                vehiclePlate,
                route,
                operatorName,
                toda,
                ""
        );
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

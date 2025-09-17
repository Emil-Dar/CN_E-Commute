package com.example.cne_commute;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.UUID;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class QRScannerActivity extends AppCompatActivity {

    private DecoratedBarcodeView barcodeScannerView;
    private Button scanQrButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);

        setupToolbar();

        barcodeScannerView = findViewById(R.id.barcode_scanner_view);
        barcodeScannerView.setTorchOff();
        barcodeScannerView.setStatusText("");

        scanQrButton = findViewById(R.id.scan_qr_button);
        scanQrButton.setOnClickListener(view -> barcodeScannerView.resume());

        barcodeScannerView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result != null && result.getText() != null) {
                    String scannedData = result.getText();
                    Log.d("QRScannerActivity", "Scanned Raw Data: [" + scannedData + "]");

                    ScannedQrCode scannedQrCode = parseQrData(scannedData);

                    if (scannedQrCode == null) {
                        Toast.makeText(QRScannerActivity.this, "Invalid or incomplete QR data", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    QrCodeStorageHelper.saveQrCode(QRScannerActivity.this, scannedQrCode);

                    SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                    prefs.edit().putString("franchise_id", scannedQrCode.getFranchiseId()).apply();

                    Toast.makeText(QRScannerActivity.this, "QR scanned successfully", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(QRScannerActivity.this, ScannedQrCodesHistory.class);
                    startActivity(intent);

                    barcodeScannerView.pause();
                } else {
                    Toast.makeText(QRScannerActivity.this, "No barcode detected", Toast.LENGTH_LONG).show();
                    Log.e("QRScannerActivity", "No barcode detected");
                }
            }
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("QR Scanner");
        }

        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private ScannedQrCode parseQrData(String raw) {
        String[] lines = raw.split("\\r?\\n");
        String franchiseId = "";
        String driverName = "", driverContact = "";

        for (String line : lines) {
            String normalized = line.toLowerCase().replaceAll("\\s+", "").replaceAll("\\.", "");

            if (normalized.startsWith("drivername:")) {
                driverName = line.substring(line.indexOf(":") + 1).trim();
            } else if (normalized.startsWith("drivercontactno:")) {
                driverContact = line.substring(line.indexOf(":") + 1).trim();
            } else if (normalized.startsWith("franchiseid:")) {
                franchiseId = line.substring(line.indexOf(":") + 1).trim();
            }
        }

        if (driverName.isEmpty() || driverContact.isEmpty()) {
            Log.w("QRScannerActivity", "Missing required fields → Name: " + driverName + ", Contact: " + driverContact);
            return null;
        }

        String uniqueId = UUID.randomUUID().toString();

        // 🕒 Generate timestamp at scan time
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
        String timestamp = dateFormat.format(new Date());

        Log.d("QRScannerActivity", "Parsed → Name: " + driverName + ", Contact: " + driverContact + ", Franchise ID: " + franchiseId);

        // ✅ Use updated constructor with timestamp
        return new ScannedQrCode(uniqueId, franchiseId, driverName, driverContact, timestamp);
    }


    @Override
    protected void onResume() {
        super.onResume();
        barcodeScannerView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeScannerView.pause();
    }
}

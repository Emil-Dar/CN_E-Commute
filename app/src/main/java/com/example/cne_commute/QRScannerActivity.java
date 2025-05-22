package com.example.cne_commute;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

public class QRScannerActivity extends AppCompatActivity {

    private DecoratedBarcodeView barcodeScannerView;
    private Button scanQrButton;
    private TextView promptText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);

        barcodeScannerView = findViewById(R.id.barcode_scanner_view);
        barcodeScannerView.setTorchOff();
        barcodeScannerView.setStatusText("");

        barcodeScannerView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result != null) {
                    String scannedData = result.getText();
                    Log.d("QRScannerActivity", "Scanned Data: " + scannedData);

                    String extractedData = extractInformation(scannedData);

                    // Generate a unique ID (e.g., timestamp)
                    String uniqueId = String.valueOf(System.currentTimeMillis());

                    // Create a ScannedQrCode object with extracted data and empty placeholders
                    ScannedQrCode scannedQrCode = new ScannedQrCode(uniqueId, extractedData, "", "", "", "");

                    // Save the scanned QR code
                    QrCodeStorageHelper.saveQrCode(QRScannerActivity.this, scannedQrCode);

                    Toast.makeText(QRScannerActivity.this, "Scanned: " + extractedData, Toast.LENGTH_LONG).show();

                    // Launch history display
                    Intent intent = new Intent(QRScannerActivity.this, HistoryActivity.class);
                    startActivity(intent);

                    // Pause scanner after success
                    barcodeScannerView.pause();
                } else {
                    Toast.makeText(QRScannerActivity.this, "No barcode detected", Toast.LENGTH_LONG).show();
                    Log.e("QRScannerActivity", "No barcode detected");
                }
            }
        });

        promptText = findViewById(R.id.prompt_text);
        promptText.setText("Place a barcode inside the viewfinder rectangle to scan it");

        scanQrButton = findViewById(R.id.scan_qr_button);
        scanQrButton.setOnClickListener(view -> barcodeScannerView.resume());
    }

    private String extractInformation(String raw) {
        StringBuilder extractedData = new StringBuilder();
        String[] keyValues = raw.split("&");
        for (String keyValue : keyValues) {
            extractedData.append(keyValue.replace("=", ": ")).append("\n");
        }
        return extractedData.toString().trim();
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

package com.example.cne_commute;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);

        barcodeScannerView = findViewById(R.id.barcode_scanner_view);
        barcodeScannerView.setTorchOff();
        barcodeScannerView.setStatusText("");

        sharedPreferences = getSharedPreferences("ScannedQrCodes", Context.MODE_PRIVATE);

        barcodeScannerView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result != null) {
                    String scannedData = result.getText();
                    Log.d("QRScannerActivity", "Scanned Data: " + scannedData);

                    // Extract information from the link (assuming it's a simple key-value query string)
                    String extractedData = extractInformation(scannedData);

                    // Save the extracted data
                    saveScannedData(extractedData);

                    Intent intent = new Intent(QRScannerActivity.this, HistoryActivity.class);
                    startActivity(intent);

                    Toast.makeText(QRScannerActivity.this, "Scanned: " + extractedData, Toast.LENGTH_LONG).show();
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

    private String extractInformation(String url) {
        // Example: Extract key-value pairs from a query string
        StringBuilder extractedData = new StringBuilder();
        String[] parts = url.split("\\?");
        if (parts.length > 1) {
            String query = parts[1];
            String[] keyValues = query.split("&");
            for (String keyValue : keyValues) {
                extractedData.append(keyValue).append("\n");
            }
        }
        return extractedData.toString().trim();
    }

    private void saveScannedData(String data) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("scannedData", data);
        editor.apply();
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

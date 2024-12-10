package com.example.cne_commute;

import android.os.Bundle;
import android.widget.Button;
import android.widget.FrameLayout;  // Add this import for FrameLayout
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;

public class QRScannerActivity extends AppCompatActivity {

    private DecoratedBarcodeView barcodeScannerView;
    private Button scanQrButton;
    private TextView promptText;  // Reference for the prompt text

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);

        // Initialize the scanner view
        barcodeScannerView = new DecoratedBarcodeView(this);
        barcodeScannerView.setTorchOff(); // Disable torch by default

        // Disable the default prompt inside the scanner view
        barcodeScannerView.setStatusText("");  // Remove the default prompt message

        barcodeScannerView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                // Handle the scanned result
                if (result != null) {
                    Toast.makeText(QRScannerActivity.this, "Scanned: " + result.getText(), Toast.LENGTH_LONG).show();
                }
            }
        });

        // Add the scanner to the QR frame
        FrameLayout qrFrame = findViewById(R.id.qr_frame);
        qrFrame.addView(barcodeScannerView);

        // Initialize the prompt text and set the custom direction message
        promptText = findViewById(R.id.prompt_text);  // Find the prompt TextView
        promptText.setText("Place a barcode inside the viewfinder rectangle to scan it");

        // Start scanning when the button is clicked
        scanQrButton = findViewById(R.id.scan_qr_button);
        scanQrButton.setOnClickListener(view -> barcodeScannerView.resume());
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

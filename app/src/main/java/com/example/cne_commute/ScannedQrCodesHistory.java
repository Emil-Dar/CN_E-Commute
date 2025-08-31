package com.example.cne_commute;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ScannedQrCodesHistory extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ScannedQrCodeAdapter adapter;
    private TextView emptyStateText;
    private List<ScannedQrCode> scannedQrCodeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanned_qr_codes_history);

        // Set up the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Scanned QR Codes");
        }

        // Initialize views
        recyclerView = findViewById(R.id.recycler_view);
        emptyStateText = findViewById(R.id.empty_state_text);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load scanned QR code data
        scannedQrCodeList = QrCodeStorageHelper.loadQrCodes(this);

        if (scannedQrCodeList == null || scannedQrCodeList.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter = new ScannedQrCodeAdapter(scannedQrCodeList);
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

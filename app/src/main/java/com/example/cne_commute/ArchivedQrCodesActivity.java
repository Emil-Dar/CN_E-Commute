package com.example.cne_commute;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ArchivedQrCodesActivity extends AppCompatActivity {

    private static final String TAG = "ArchivedQrCodesActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archived_qr_codes);

        View rootView = getWindow().getDecorView();
        applyFadeIn(rootView); // ✅ Match fragment-style animation

        setupToolbar();

        RecyclerView recyclerView = findViewById(R.id.recycler_archived_qr_codes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<ScannedQrCode> archivedList = QrCodeStorageHelper.loadArchivedQrCodeList(this);

        if (archivedList != null && !archivedList.isEmpty()) {
            ScannedQrCodeAdapter adapter = new ScannedQrCodeAdapter(archivedList);
            recyclerView.setAdapter(adapter);
            Log.d(TAG, "Archived list loaded with " + archivedList.size() + " items");
        } else {
            Log.w(TAG, "No archived QR codes found");
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            int topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(0, topInset, 0, 0);
            return insets;
        });

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Archived QR Codes");
        }

        toolbar.setNavigationOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.fade_in, 0); // ✅ Same animation as fragment
        });

        View buttonsContainer = findViewById(R.id.buttons_container);
        if (buttonsContainer != null) {
            buttonsContainer.setVisibility(View.GONE);
        }
    }

    private void applyFadeIn(View view) {
        Animation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(300);
        view.startAnimation(fadeIn);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        overridePendingTransition(R.anim.fade_in, 0);
        return true;
    }
}

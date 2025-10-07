package com.example.cne_commute;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.text.TextPaint;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FranchiseDetailsActivity extends AppCompatActivity {

    private TextView detailsFranchiseId, detailsRegDate, detailsToda, detailsRenewalStatus, detailsRenewalDate;
    private TextView assignedDriverName, assignedAtDate, assignmentHistoryLink;
    private Button assignDriverButton, addDriverButton;
    private CardView assignedDriverCard;

    private String franchiseId;

    private static final int REQUEST_ADD_DRIVER = 100;
    private static final int REQUEST_ASSIGN_DRIVER = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_franchise_details);

        detailsFranchiseId = findViewById(R.id.detailsFranchiseId);
        detailsRegDate = findViewById(R.id.detailsRegDate);
        detailsToda = findViewById(R.id.detailsToda);
        detailsRenewalStatus = findViewById(R.id.detailsRenewalStatus);
        detailsRenewalDate = findViewById(R.id.detailsRenewalDate);

        assignDriverButton = findViewById(R.id.assignDriverButton);
        addDriverButton = findViewById(R.id.addDriverButton);

        assignedDriverName = findViewById(R.id.assignedDriverName);
        assignedAtDate = findViewById(R.id.assignedAtDate);
        assignedDriverCard = findViewById(R.id.assignedDriverCard);
        assignmentHistoryLink = findViewById(R.id.assignmentHistoryLink);

        franchiseId = sanitize(getIntent().getStringExtra("franchiseId"));
        String regDate = sanitize(getIntent().getStringExtra("registrationDate"));
        String toda = sanitize(getIntent().getStringExtra("toda"));
        String renewalStatus = sanitize(getIntent().getStringExtra("renewalStatus"));
        String renewalDate = sanitize(getIntent().getStringExtra("renewalDate"));

        detailsFranchiseId.setText("Franchise ID: " + franchiseId);
        detailsRegDate.setText("Registration Date: " + formatDateTime(regDate));
        detailsToda.setText("TODA: " + toda);
        detailsRenewalStatus.setText("Renewal Status: " + renewalStatus);
        detailsRenewalDate.setText("Renewal Date: " + formatDateTime(renewalDate));

        fetchCurrentAssignedDriver();

        assignDriverButton.setOnClickListener(v -> {
            Intent intent = new Intent(FranchiseDetailsActivity.this, AssignDriverActivity.class);
            intent.putExtra("franchiseId", franchiseId);
            startActivityForResult(intent, REQUEST_ASSIGN_DRIVER);
        });

        addDriverButton.setOnClickListener(v -> {
            Intent intent = new Intent(FranchiseDetailsActivity.this, AddDriverActivity.class);
            intent.putExtra("franchiseId", franchiseId);
            startActivityForResult(intent, REQUEST_ADD_DRIVER);
        });

        // Assignment History Link
        String linkText = "View Assignment History";
        SpannableString spannable = new SpannableString(linkText);
        spannable.setSpan(new UnderlineSpan(), 0, linkText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, linkText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent intent = new Intent(FranchiseDetailsActivity.this, AssignmentHistoryActivity.class);
                intent.putExtra("franchiseId", franchiseId);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(getResources().getColor(R.color.blue));
                ds.setUnderlineText(true);
                ds.setTypeface(Typeface.DEFAULT_BOLD);
            }
        }, 0, linkText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        assignmentHistoryLink.setText(spannable);
        assignmentHistoryLink.setMovementMethod(LinkMovementMethod.getInstance());
        assignmentHistoryLink.setHighlightColor(Color.TRANSPARENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == REQUEST_ADD_DRIVER || requestCode == REQUEST_ASSIGN_DRIVER)
                && resultCode == RESULT_OK) {
            fetchCurrentAssignedDriver();
        }
    }

    private String sanitize(String value) {
        if (value == null || value.trim().isEmpty() || value.equalsIgnoreCase("null")) {
            return "";
        }
        return value.trim();
    }

    private String formatDateTime(String isoDateTime) {
        if (isoDateTime == null || isoDateTime.isEmpty()) return "";
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = isoFormat.parse(isoDateTime);
            SimpleDateFormat displayFormat = new SimpleDateFormat("MMM d, yyyy hh:mm a", Locale.getDefault());
            return displayFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return isoDateTime;
        }
    }

    private void fetchCurrentAssignedDriver() {
        SupabaseService service = SupabaseApiClient.getRetrofitInstance().create(SupabaseService.class);

        service.getLatestAssignmentForFranchise(
                BuildConfig.SUPABASE_API_KEY,
                "Bearer " + BuildConfig.SUPABASE_API_KEY,
                "eq." + franchiseId,
                "assigned_at.desc",
                1
        ).enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Map<String, Object> latestAssignment = response.body().get(0);
                    String driverId = String.valueOf(latestAssignment.get("driver_id"));
                    String assignedAt = String.valueOf(latestAssignment.get("assigned_at"));

                    service.getDriverById(
                            BuildConfig.SUPABASE_API_KEY,
                            "Bearer " + BuildConfig.SUPABASE_API_KEY,
                            "eq." + driverId
                    ).enqueue(new Callback<List<Driver>>() {
                        @Override
                        public void onResponse(Call<List<Driver>> call, Response<List<Driver>> response) {
                            if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                                String driverName = response.body().get(0).getFullName();
                                assignedDriverName.setText(driverName);
                                assignedAtDate.setText(formatDateTime(assignedAt));
                                assignedAtDate.setVisibility(View.VISIBLE);

                                // enable card when driver exists
                                assignedDriverCard.setAlpha(1f);
                                assignedDriverCard.setClickable(true);
                                assignedDriverCard.setFocusable(true);
                                assignedDriverCard.setOnClickListener(v -> {
                                    Intent intent = new Intent(FranchiseDetailsActivity.this, DriverDetailsActivity.class);
                                    intent.putExtra("driverId", driverId);
                                    startActivity(intent);
                                });
                            } else {
                                disableDriverCard();
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Driver>> call, Throwable t) {
                            disableDriverCard();
                        }
                    });
                } else {
                    disableDriverCard();
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                disableDriverCard();
            }
        });
    }

    private void disableDriverCard() {
        assignedDriverName.setText("No driver assigned");
        assignedAtDate.setVisibility(View.GONE);
        assignedDriverCard.setAlpha(0.5f);
        assignedDriverCard.setClickable(false);
        assignedDriverCard.setFocusable(false);
        assignedDriverCard.setOnClickListener(null);
    }
}

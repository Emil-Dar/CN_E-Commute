package com.example.cne_commute;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class AppointmentNotificationDetailsActivity extends AppCompatActivity {

    private static final String TAG = "ApptNotifDetails";

    private TextView driverNameView, dateView, timeView, statusView;
    private String appointmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_notification_details);

        setupToolbar();

        // Get appointmentId passed from NotificationAdapter
        appointmentId = getIntent().getStringExtra("appointment_id");
        Log.d(TAG, "Opened with appointmentId: " + appointmentId);

        bindViews();
        loadAppointmentDetails(appointmentId);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Appointment Notification");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void bindViews() {
        driverNameView = findViewById(R.id.text_driver_name);
        dateView = findViewById(R.id.text_date);
        timeView = findViewById(R.id.text_time);
        statusView = findViewById(R.id.text_status);
    }

    private void loadAppointmentDetails(String appointmentId) {
        AppointmentApiService apiService = ApiClient.getClient().create(AppointmentApiService.class);

        // Build Supabase-style filters
        Map<String, String> filters = new HashMap<>();
        // Use the correct column name for your table:
        // If your column is "id":
        filters.put("id", "eq." + appointmentId);
        // If it's "appointment_id", use this instead:
        // filters.put("appointment_id", "eq." + appointmentId);

        filters.put("select", "*");

        Call<List<Appointment>> call = apiService.getAppointmentsForUser(filters);

        call.enqueue(new Callback<List<Appointment>>() {
            @Override
            public void onResponse(Call<List<Appointment>> call, Response<List<Appointment>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Appointment appt = response.body().get(0);
                    driverNameView.setText(appt.getDriverName());
                    dateView.setText(appt.getScheduledDate());
                    timeView.setText(appt.getScheduledTime());
                    statusView.setText(appt.getStatus());
                } else {
                    // Handle empty or failed responses gracefully
                    statusView.setText("Not found");
                }
            }

            @Override
            public void onFailure(Call<List<Appointment>> call, Throwable t) {
                statusView.setText("Error loading details");
            }
        });
    }

}

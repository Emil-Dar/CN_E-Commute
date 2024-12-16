package com.example.cne_commute;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class DriverNotificationActivity extends AppCompatActivity {

    private Button homeButton, mapButton, historyButton, accountButton;
    private FloatingActionButton fabDriverMap;
    private RecyclerView notificationRecyclerView;
    private DriverNotificationAdapter notificationAdapter;
    private List<String> notificationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_notification);

        homeButton = findViewById(R.id.home_button);
        mapButton = findViewById(R.id.map_button);
        historyButton = findViewById(R.id.history_button);
        accountButton = findViewById(R.id.account_button);
        fabDriverMap = findViewById(R.id.fab_qr_code);
        notificationRecyclerView = findViewById(R.id.notification_recycler_view);

        homeButton.setOnClickListener(v -> navigateToActivity(DriverHomeActivity.class));
        mapButton.setOnClickListener(v -> navigateToActivity(WalletActivity.class));
        historyButton.setOnClickListener(v -> navigateToActivity(DriverNotificationActivity.class));
        accountButton.setOnClickListener(v -> navigateToActivity(DriverAccountActivity.class));

        setupNotificationRecyclerView();

        // Example data
        notificationList.add("Notification 1: Your ride has been confirmed.");
        notificationList.add("Notification 2: Your ride is arriving soon.");
        notificationList.add("Notification 3: Your ride has been cancelled.");
        notificationAdapter.notifyDataSetChanged();
    }

    private void setupNotificationRecyclerView() {
        notificationList = new ArrayList<>();
        notificationAdapter = new DriverNotificationAdapter(notificationList);
        notificationRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notificationRecyclerView.setAdapter(notificationAdapter);
    }

    private void navigateToActivity(Class<?> targetActivity) {
        Intent intent = new Intent(DriverNotificationActivity.this, targetActivity);
        startActivity(intent);
    }


    private void showToast(String message) {
        Toast.makeText(DriverNotificationActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}

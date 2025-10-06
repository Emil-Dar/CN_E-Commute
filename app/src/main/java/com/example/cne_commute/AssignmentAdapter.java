package com.example.cne_commute;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AssignmentAdapter extends RecyclerView.Adapter<AssignmentAdapter.AssignmentViewHolder> {

    private final List<Assignment> assignmentList;
    private final Map<String, String> driverNameCache = new HashMap<>(); // cache driverId -> name

    public AssignmentAdapter(List<Assignment> assignmentList) {
        this.assignmentList = assignmentList;
    }

    @NonNull
    @Override
    public AssignmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_assignment, parent, false);
        return new AssignmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AssignmentViewHolder holder, int position) {
        Assignment assignment = assignmentList.get(position);
        String driverId = assignment.getDriverId();

        // format and set assigned at datetime
        holder.assignedAt.setText(formatDateTime(assignment.getAssignedAt()));

        // highlight first item as current driver
        if (position == 0) {
            holder.itemView.setBackgroundColor(Color.parseColor("#DFF0D8")); // light green
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
        }

        // fetch or use cached driver name
        if (driverNameCache.containsKey(driverId)) {
            String name = driverNameCache.get(driverId);
            holder.driverName.setText(name + (position == 0 ? " (current)" : ""));
        } else {
            holder.driverName.setText("Driver: loading..." + (position == 0 ? " (current)" : ""));
            fetchDriverName(driverId, holder, position == 0);
        }

        // make card clickable to open DriverDetailsActivity
        holder.rootView.setOnClickListener(v -> {
            if (driverId != null && !driverId.isEmpty()) {
                Intent intent = new Intent(v.getContext(), DriverDetailsActivity.class);
                intent.putExtra("driverId", driverId); // match FranchiseDetailsActivity key
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return assignmentList.size();
    }

    private void fetchDriverName(String driverId, AssignmentViewHolder holder, boolean isCurrent) {
        SupabaseService service = SupabaseApiClient.getRetrofitInstance().create(SupabaseService.class);
        service.getDriverById(
                BuildConfig.SUPABASE_API_KEY,
                "Bearer " + BuildConfig.SUPABASE_API_KEY,
                "eq." + driverId
        ).enqueue(new Callback<List<Driver>>() {
            @Override
            public void onResponse(Call<List<Driver>> call, Response<List<Driver>> response) {
                String name = "unknown";
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    name = response.body().get(0).getFullName();
                }
                driverNameCache.put(driverId, name);
                holder.driverName.setText(name + (isCurrent ? " (current)" : ""));
            }

            @Override
            public void onFailure(Call<List<Driver>> call, Throwable t) {
                driverNameCache.put(driverId, "unknown");
                holder.driverName.setText("Driver: unknown" + (isCurrent ? " (current)" : ""));
            }
        });
    }

    static class AssignmentViewHolder extends RecyclerView.ViewHolder {
        TextView driverName, assignedAt;
        View rootView;

        public AssignmentViewHolder(@NonNull View itemView) {
            super(itemView);
            rootView = itemView;
            driverName = itemView.findViewById(R.id.driverName);
            assignedAt = itemView.findViewById(R.id.assignedAt);
        }
    }

    // convert ISO 8601 datetime string to friendly format
    private String formatDateTime(String isoDateTime) {
        if (isoDateTime == null || isoDateTime.isEmpty()) return "--";
        try {
            LocalDateTime dateTime = LocalDateTime.parse(isoDateTime);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy hh:mm a", Locale.getDefault());
            return dateTime.format(formatter);
        } catch (Exception e) {
            e.printStackTrace();
            return isoDateTime;
        }
    }
}

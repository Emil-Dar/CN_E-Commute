package com.example.cne_commute;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ReportHistoryAdapter extends RecyclerView.Adapter<ReportHistoryAdapter.ReportViewHolder> {

    private final List<ReportData> reportList;

    public ReportHistoryAdapter(List<ReportData> reportList) {
        this.reportList = reportList;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report_card, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        ReportData report = reportList.get(position);

        // Driver info
        String driverName = report.getDriverName() != null ? report.getDriverName() : "N/A";
        String driverContact = report.getDriverContact() != null ? report.getDriverContact() : "N/A";
        String franchiseId = report.getFranchiseId() != null ? report.getFranchiseId() : "N/A";

        holder.description.setText("Driver: " + driverName +
                " | Contact: " + driverContact +
                " | Franchise ID: " + franchiseId);

        // Violations
        StringBuilder violations = new StringBuilder();
        if (!isEmpty(report.getParkingObstructionViolations())) {
            violations.append("Parking: ").append(report.getParkingObstructionViolations()).append("\n");
        }
        if (!isEmpty(report.getTrafficMovementViolations())) {
            violations.append("Movement: ").append(report.getTrafficMovementViolations()).append("\n");
        }
        if (!isEmpty(report.getDriverBehaviorViolations())) {
            violations.append("Behavior: ").append(report.getDriverBehaviorViolations()).append("\n");
        }
        if (!isEmpty(report.getLicensingDocumentationViolations())) {
            violations.append("Licensing: ").append(report.getLicensingDocumentationViolations()).append("\n");
        }
        if (!isEmpty(report.getAttireFareViolations())) {
            violations.append("Attire/Fare: ").append(report.getAttireFareViolations()).append("\n");
        }

        holder.violation.setText(violations.length() > 0 ? violations.toString().trim() : "Violations: None");

        // Image
        if (!isEmpty(report.getImageUrl())) {
            Glide.with(holder.itemView.getContext())
                    .load(report.getImageUrl())
                    .into(holder.reportImage);
            holder.reportImage.setVisibility(View.VISIBLE);
        } else {
            holder.reportImage.setVisibility(View.GONE);
        }

        // Report button click
        holder.reportButton.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            Intent intent = new Intent(context, ReportActivity.class);
            intent.putExtra("driverId", report.getDriverId());
            intent.putExtra("driverName", report.getDriverName());
            intent.putExtra("driverContact", report.getDriverContact());
            intent.putExtra("franchiseId", report.getFranchiseId());
            context.startActivity(intent);

            // Optional feedback
            Toast.makeText(context, "Opening report form...", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty() || value.equalsIgnoreCase("None");
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        public TextView description;
        public TextView violation;
        public ImageView reportImage;
        public View reportButton;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            description = itemView.findViewById(R.id.description_text);
            violation = itemView.findViewById(R.id.violation_text);
            reportImage = itemView.findViewById(R.id.report_image);
            reportButton = itemView.findViewById(R.id.report_button);
        }
    }
}

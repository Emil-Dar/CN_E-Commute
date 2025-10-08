package com.example.cne_commute;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportHistoryAdapter extends RecyclerView.Adapter<ReportHistoryAdapter.ReportViewHolder> {

    private List<ReportData> reportList;

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

        String driverId = safe(report.getDriverId());
        String toda = safe(report.getToda());
        String status = safe(report.getStatus());
        int violationCount = countViolations(report);
        String rawTimestamp = safe(report.getTimestamp());

        holder.timestamp.setText("Reported on: " + formatTimestamp(rawTimestamp));
        holder.description.setText("Driver ID: " + driverId);
        holder.toda.setText("TODA: " + toda);
        holder.status.setText("Status: " + status);
        holder.violation.setText("Violations: " + violationCount + " reported");

        if ("Resolved".equalsIgnoreCase(status)) {
            holder.status.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_green_dark));
        } else {
            holder.status.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.holo_orange_dark));
        }

        holder.viewReportText.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            Intent intent = new Intent(context, ReportDetailsActivity.class);
            intent.putExtra("driverId", report.getDriverId());
            intent.putExtra("driverName", report.getDriverName());
            intent.putExtra("franchiseId", report.getFranchiseId());
            intent.putExtra("operatorName", report.getOperatorName());
            intent.putExtra("toda", report.getToda());
            intent.putExtra("violations", buildViolationDetails(report));
            intent.putExtra("status", report.getStatus());
            intent.putExtra("remarks", report.getRemarks());
            intent.putExtra("imageUrl", report.getImageUrl());
            intent.putExtra("timestamp", report.getTimestamp());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    public void updateList(List<ReportData> newList) {
        reportList.clear();
        reportList.addAll(newList);
        notifyDataSetChanged();
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty() || value.equalsIgnoreCase("None");
    }

    private String safe(String value) {
        return value != null && !value.trim().isEmpty() ? value : "N/A";
    }

    private int countViolations(ReportData report) {
        int count = 0;
        if (!isEmpty(report.getParkingObstructionViolations())) count++;
        if (!isEmpty(report.getTrafficMovementViolations())) count++;
        if (!isEmpty(report.getDriverBehaviorViolations())) count++;
        if (!isEmpty(report.getLicensingDocumentationViolations())) count++;
        if (!isEmpty(report.getAttireFareViolations())) count++;
        return count;
    }

    private String buildViolationDetails(ReportData report) {
        StringBuilder violations = new StringBuilder();
        if (!isEmpty(report.getParkingObstructionViolations())) {
            violations.append("🚧 Parking: ").append(report.getParkingObstructionViolations()).append("\n");
        }
        if (!isEmpty(report.getTrafficMovementViolations())) {
            violations.append("🚦 Movement: ").append(report.getTrafficMovementViolations()).append("\n");
        }
        if (!isEmpty(report.getDriverBehaviorViolations())) {
            violations.append("🧍 Behavior: ").append(report.getDriverBehaviorViolations()).append("\n");
        }
        if (!isEmpty(report.getLicensingDocumentationViolations())) {
            violations.append("📄 Licensing: ").append(report.getLicensingDocumentationViolations()).append("\n");
        }
        if (!isEmpty(report.getAttireFareViolations())) {
            violations.append("🎽 Attire/Fare: ").append(report.getAttireFareViolations()).append("\n");
        }
        return violations.toString().trim();
    }

    private String formatTimestamp(String raw) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
            Date date = inputFormat.parse(raw);
            return outputFormat.format(date);
        } catch (Exception e) {
            return "Unknown time";
        }
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        public TextView description;
        public TextView toda;
        public TextView status;
        public TextView violation;
        public TextView timestamp;
        public TextView viewReportText;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            description = itemView.findViewById(R.id.description_text);
            toda = itemView.findViewById(R.id.text_toda);
            status = itemView.findViewById(R.id.text_status);
            violation = itemView.findViewById(R.id.violation_text);
            timestamp = itemView.findViewById(R.id.text_timestamp);
            viewReportText = itemView.findViewById(R.id.text_view_report); // updated ID
        }
    }
}

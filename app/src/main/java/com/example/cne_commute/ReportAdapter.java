package com.example.cne_commute;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private final List<Report> reportList;
    private final boolean isHistoryView;

    public ReportAdapter(List<Report> reportList, boolean isHistoryView) {
        this.reportList = reportList;
        this.isHistoryView = isHistoryView;
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView violationTextView, descriptionTextView;
        ImageView reportImageView;

        public ReportViewHolder(View itemView, boolean isHistoryView) {
            super(itemView);
            violationTextView = itemView.findViewById(
                    isHistoryView ? R.id.violation_text : R.id.violation_text_view);
            descriptionTextView = itemView.findViewById(
                    isHistoryView ? R.id.description_text : R.id.description_text_view);
            reportImageView = itemView.findViewById(
                    isHistoryView ? R.id.report_image : R.id.report_image_view);
        }
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(isHistoryView ? R.layout.item_report_card : R.layout.report_item, parent, false);
        return new ReportViewHolder(view, isHistoryView);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Report report = reportList.get(position);

        // Build violation summary
        String violations = buildViolationSummary(report);
        holder.violationTextView.setText("Violations: " + (violations.isEmpty() ? "None" : violations));

        // Set description
        String description = report.getImageDescription();
        holder.descriptionTextView.setText("Description: " + (description != null ? description : "No description"));

        // Load image or fallback
        String imageUrl = report.getImageUrl();
        if (!isEmpty(imageUrl)) {
            holder.reportImageView.setImageURI(Uri.parse(imageUrl));
        } else {
            holder.reportImageView.setImageResource(R.drawable.ic_placeholder_image);
        }
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    private String buildViolationSummary(Report report) {
        List<String> violations = new ArrayList<>();

        if (!isEmpty(report.getParkingObstruction())) {
            violations.add(report.getParkingObstruction());
        }
        if (!isEmpty(report.getTrafficMovement())) {
            violations.add(report.getTrafficMovement());
        }
        if (!isEmpty(report.getDriverBehavior())) {
            violations.add(report.getDriverBehavior());
        }
        if (!isEmpty(report.getLicensingDocumentation())) {
            violations.add(report.getLicensingDocumentation());
        }
        if (!isEmpty(report.getAttireFare())) {
            violations.add(report.getAttireFare());
        }

        return String.join(", ", violations);
    }

    private boolean isEmpty(String text) {
        return text == null || text.trim().isEmpty() || text.equalsIgnoreCase("None");
    }
}

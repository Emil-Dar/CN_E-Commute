package com.example.cne_commute;



import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;




public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private List<Report> reportList;

    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView violationTextView, descriptionTextView;
        ImageView reportImageView;

        public ReportViewHolder(View itemView) {
            super(itemView);
            violationTextView = itemView.findViewById(R.id.violation_text_view);
            descriptionTextView = itemView.findViewById(R.id.description_text_view);
            reportImageView = itemView.findViewById(R.id.report_image_view);
        }
    }

    public ReportAdapter(List<Report> reportList) {
        this.reportList = reportList;
    }

    @Override
    public ReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.report_item, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReportViewHolder holder, int position) {
        Report report = reportList.get(position);
        holder.violationTextView.setText("Violation: " + report.getViolation());
        holder.descriptionTextView.setText("Description: " + report.getDescription());

        if (!report.getImagePath().isEmpty()) {
            holder.reportImageView.setImageURI(Uri.parse(report.getImagePath()));
        }
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }
}

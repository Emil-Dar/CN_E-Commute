package com.example.cne_commute;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class AcceptedReportAdapter extends RecyclerView.Adapter<AcceptedReportAdapter.ReportViewHolder> {

    private List<ReportData> reportList;

    public AcceptedReportAdapter(List<ReportData> reportList) {
        this.reportList = reportList;
    }

    public void setReports(List<ReportData> newReports) {
        this.reportList = newReports;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_accepted_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        ReportData report = reportList.get(position);

        // Display Report ID using reportCode
        holder.reportTitle.setText("Report ID: " + report.getReportCode());

        // Display Status and Remarks
        holder.reportStatus.setText("Status: " + report.getStatus());
        holder.reportMessage.setText("Remarks: " + report.getRemarks());

        // Format timestamp to readable date
        if (report.getTimestamp() != null) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault());
                inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM d, yyyy – h:mm a", Locale.getDefault());
                String formattedDate = outputFormat.format(inputFormat.parse(report.getTimestamp()));
                holder.reportDate.setText("Date: " + formattedDate);
            } catch (ParseException e) {
                Log.e("AcceptedReportAdapter", "Failed to parse timestamp: " + report.getTimestamp(), e);
                holder.reportDate.setText("Date: Invalid");
            }
        } else {
            holder.reportDate.setText("Date: Unknown");
        }
    }

    @Override
    public int getItemCount() {
        return reportList != null ? reportList.size() : 0;
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView reportTitle, reportStatus, reportMessage, reportDate;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            reportTitle = itemView.findViewById(R.id.report_title);
            reportStatus = itemView.findViewById(R.id.report_status);
            reportMessage = itemView.findViewById(R.id.report_message);
            reportDate = itemView.findViewById(R.id.report_date);
        }
    }
}

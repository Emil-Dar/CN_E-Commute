package com.example.cne_commute;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OperatorHomeDriverAdapter extends RecyclerView.Adapter<OperatorHomeDriverAdapter.ViewHolder> {

    private List<OperatorDriverSummary> driverList;

    public OperatorHomeDriverAdapter(List<OperatorDriverSummary> driverList) {
        this.driverList = driverList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_operator_driver_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OperatorDriverSummary item = driverList.get(position);

        holder.tvDriverName.setText(item.getDriverId() + " • " + item.getDriverName());
        holder.tvFranchise.setText("Franchise: " + item.getFranchiseId());
        holder.tvAssignedDate.setText("Assigned Date: " + item.getAssignedDate());

        String reports = "Pending: " + item.getPending() +
                "    Scheduled: " + item.getScheduled() +
                "\nResolved: " + item.getResolved() +
                "    Unresolved: " + item.getUnresolved();

        holder.tvReportCounts.setText(reports);
    }

    @Override
    public int getItemCount() {
        return driverList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvDriverName, tvFranchise, tvAssignedDate, tvReportCounts;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDriverName = itemView.findViewById(R.id.tv_driver_name);
            tvFranchise = itemView.findViewById(R.id.tv_franchise);
            tvAssignedDate = itemView.findViewById(R.id.tv_assigned_date);
            tvReportCounts = itemView.findViewById(R.id.tv_report_counts);
        }
    }
}

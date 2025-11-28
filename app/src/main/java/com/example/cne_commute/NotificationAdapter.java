package com.example.cne_commute;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private final Context context;
    private final List<NotificationItem> items;

    private boolean selectionMode = false;

    public void setSelectionMode(boolean enabled) {
        this.selectionMode = enabled;
        notifyDataSetChanged();
    }

    public boolean isSelectionMode() {
        return selectionMode;
    }

    public NotificationAdapter(Context context, List<NotificationItem> items) {
        this.context = context;
        this.items = items;
    }

    // Replace all existing items with new ones
    public void setItems(List<NotificationItem> updatedItems) {
        items.clear();
        items.addAll(updatedItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationItem item = items.get(position);

        // Bind notification data
        holder.title.setText(item.getTitle());
        holder.message.setText(item.getMessage());
        holder.timestamp.setText(formatTimestamp(item.getTimestamp()));

        // ---------------- REPORT NOTIFICATIONS ----------------
        if ("report".equals(item.getType()) && item.getId() != null) {
            holder.viewReport.setVisibility(View.VISIBLE);
            holder.viewReport.setOnClickListener(v -> {
                if (!selectionMode) {
                    Intent intent = new Intent(context, ReportNotificationDetailsActivity.class);
                    intent.putExtra("report_id", item.getId());
                    context.startActivity(intent);
                }
            });
        } else {
            holder.viewReport.setVisibility(View.GONE);
        }

        // ---------------- APPOINTMENT NOTIFICATIONS ----------------
        if ("appointment".equals(item.getType()) && item.getId() != null) {
            holder.itemView.setOnClickListener(v -> {
                if (!selectionMode) {
                    Intent intent = new Intent(context, AppointmentNotificationDetailsActivity.class);
                    intent.putExtra("appointment_id", item.getId());
                    context.startActivity(intent);
                }
            });
        } else {
            if (!selectionMode) {
                holder.itemView.setOnClickListener(null);
            }
        }

        // ---------------- SELECTION MODE ----------------
        holder.checkBox.setVisibility(selectionMode ? View.VISIBLE : View.GONE);
        holder.checkBox.setOnCheckedChangeListener(null); // reset listener
        holder.checkBox.setChecked(item.isSelected());
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setSelected(isChecked);
        });

        // Fade-in animation
        holder.itemView.setAlpha(0f);
        holder.itemView.animate().alpha(1f).setDuration(300).start();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // -------------------- VIEW HOLDER --------------------
    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        final TextView title, message, timestamp, viewReport;
        final CheckBox checkBox;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.notification_title);
            message = itemView.findViewById(R.id.notification_message);
            timestamp = itemView.findViewById(R.id.notification_timestamp);
            viewReport = itemView.findViewById(R.id.text_view_report);
            checkBox = itemView.findViewById(R.id.notification_checkbox);
        }
    }

    // -------------------- HELPER METHODS --------------------
    private String formatTimestamp(String isoString) {
        if (isoString == null || isoString.trim().isEmpty()) {
            return "";
        }

        try {
            SimpleDateFormat inputFormat =
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(isoString);

            SimpleDateFormat outputFormat =
                    new SimpleDateFormat("MMMM dd, yyyy hh:mm a", Locale.getDefault());

            String formatted = outputFormat.format(date);
            return formatted.replace("AM", "am").replace("PM", "pm");
        } catch (Exception e) {
            return isoString;
        }
    }

    private Date parseDate(String isoString) {
        try {
            SimpleDateFormat inputFormat =
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            return inputFormat.parse(isoString);
        } catch (ParseException e) {
            return null;
        }
    }

    // -------------------- SORTERS --------------------
    public void sortByDateDescending() {
        Collections.sort(items, (a, b) -> {
            Date da = parseDate(a.getTimestamp());
            Date db = parseDate(b.getTimestamp());
            if (da == null || db == null) return 0;
            return db.compareTo(da); // newest first
        });
        notifyDataSetChanged();
    }

    public void sortByReport() {
        Collections.sort(items, Comparator.comparing(
                item -> !"report".equals(item.getType())));
        notifyDataSetChanged();
    }

    public void sortByAppointment() {
        Collections.sort(items, Comparator.comparing(
                item -> !"appointment".equals(item.getType())));
        notifyDataSetChanged();
    }

    // -------------------- MULTI-SELECTION SUPPORT --------------------
    public List<NotificationItem> getSelectedItems() {
        List<NotificationItem> selected = new ArrayList<>();
        for (NotificationItem item : items) {
            if (item.isSelected()) {
                selected.add(item);
            }
        }
        return selected;
    }

    // -------------------- LEGACY SUPPORT --------------------
    private void openReportDetails(NotificationItem item) {
        try {
            Intent intent = new Intent(context, ReportDetailsActivity.class);
            intent.putExtra("driverId", item.getDriverId());
            intent.putExtra("driverName", item.getDriverName());
            intent.putExtra("franchiseId", item.getFranchiseId());
            intent.putExtra("operatorName", item.getOperatorName());
            intent.putExtra("toda", item.getToda());
            intent.putExtra("violations", item.getViolation());
            intent.putExtra("status", item.getStatus());
            intent.putExtra("remarks", item.getMessage());
            intent.putExtra("imageUrl", item.getImageUrl());
            intent.putExtra("timestamp", item.getTimestamp());
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "Unable to open report details.", Toast.LENGTH_SHORT).show();
        }
    }
}

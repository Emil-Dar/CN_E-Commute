package com.example.cne_commute;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScannedQrCodeAdapter extends RecyclerView.Adapter<ScannedQrCodeAdapter.ViewHolder> {

    private static final String TAG = "ScannedQrCodeAdapter";
    private List<ScannedQrCode> qrCodeList;

    public ScannedQrCodeAdapter(List<ScannedQrCode> qrCodeList) {
        this.qrCodeList = qrCodeList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_scanned_qr_code, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScannedQrCode qrCode = qrCodeList.get(position);
        Context context = holder.itemView.getContext();

        Log.d(TAG, "Binding QR code at position " + position + ", Driver ID: " + qrCode.getDriverId());

        holder.driverIdTextView.setText("Driver ID: " + qrCode.getDriverId());
        holder.driverNameTextView.setText("Driver Name: " + qrCode.getDriverName());
        holder.franchiseIdTextView.setText("Franchise ID: " + qrCode.getFranchiseId());
        holder.operatorNameTextView.setText("Operator Name: " + qrCode.getOperatorName());
        holder.todaTextView.setText("TODA: " + qrCode.getToda());

        String formattedTimestamp = formatTimestamp(qrCode.getScanTimestamp());
        holder.scanDatetimeTextView.setText(formattedTimestamp.isEmpty() ? "Not yet scanned" : formattedTimestamp);

        int count = qrCode.getScanCountToday();
        holder.scanCountTextView.setText("Scanned: " + count + " time" + (count > 1 ? "s" : "") );

        holder.scanTimestampsTextView.setText(renderTimestampList(qrCode.getScanTimestamps()));

        holder.deleteButton.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_confirm_delete, null);
            AlertDialog dialog = new AlertDialog.Builder(context).setView(dialogView).create();

            dialogView.findViewById(R.id.button_cancel).setOnClickListener(v1 -> dialog.dismiss());

            dialogView.findViewById(R.id.button_confirm).setOnClickListener(v1 -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    ScannedQrCode toDelete = qrCodeList.get(pos);
                    qrCodeList.remove(pos);
                    notifyItemRemoved(pos);

                    List<ScannedQrCode> fullList = QrCodeStorageHelper.loadQrCodes(context);
                    fullList.removeIf(code -> code.getDriverId().equals(toDelete.getDriverId()));
                    QrCodeStorageHelper.saveQrCodeList(context, fullList);
                }
                dialog.dismiss();
            });

            dialog.show();
        });

        holder.reportButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReportActivity.class);
            intent.putExtra("EXTRA_DRIVER_ID", qrCode.getDriverId());
            intent.putExtra("EXTRA_DRIVER_NAME", qrCode.getDriverName());
            intent.putExtra("EXTRA_FRANCHISE_ID", qrCode.getFranchiseId());
            intent.putExtra("EXTRA_OPERATOR_NAME", qrCode.getOperatorName());
            intent.putExtra("EXTRA_TODA", qrCode.getToda());
            context.startActivity(intent);
        });

        if (holder.archiveIcon != null) {
            if (qrCode.isArchived()) {
                holder.archiveIcon.setImageResource(R.drawable.ic_restore);
                holder.archiveIcon.setContentDescription("Restore QR code");

                holder.archiveIcon.setOnClickListener(v -> {
                    int pos = holder.getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        ScannedQrCode toRestore = qrCodeList.get(pos);
                        toRestore.setArchived(false);
                        qrCodeList.remove(pos);
                        notifyItemRemoved(pos);

                        List<ScannedQrCode> archiveList = QrCodeStorageHelper.loadArchivedQrCodeList(context);
                        archiveList.removeIf(code -> code.getDriverId().equals(toRestore.getDriverId()));
                        QrCodeStorageHelper.saveArchivedQrCodeList(context, archiveList);

                        List<ScannedQrCode> scannedList = QrCodeStorageHelper.loadQrCodes(context);
                        scannedList.add(toRestore);
                        QrCodeStorageHelper.saveQrCodeList(context, scannedList);

                        Toast.makeText(context, "Restored successfully", Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                holder.archiveIcon.setImageResource(R.drawable.ic_archive_box); // Use valid drawable
                holder.archiveIcon.setContentDescription("Archive QR code");

                holder.archiveIcon.setOnClickListener(v -> {
                    View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_confirm_archive, null);
                    AlertDialog dialog = new AlertDialog.Builder(context).setView(dialogView).create();

                    dialogView.findViewById(R.id.button_cancel).setOnClickListener(v1 -> dialog.dismiss());

                    dialogView.findViewById(R.id.button_confirm).setOnClickListener(v1 -> {
                        int pos = holder.getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            ScannedQrCode archivedItem = qrCodeList.get(pos);
                            archivedItem.setArchived(true);
                            qrCodeList.remove(pos);
                            notifyItemRemoved(pos);

                            List<ScannedQrCode> fullList = QrCodeStorageHelper.loadQrCodes(context);
                            fullList.removeIf(code -> code.getDriverId().equals(archivedItem.getDriverId()));
                            QrCodeStorageHelper.saveQrCodeList(context, fullList);

                            List<ScannedQrCode> archiveList = QrCodeStorageHelper.loadArchivedQrCodeList(context);
                            archiveList.add(archivedItem);
                            QrCodeStorageHelper.saveArchivedQrCodeList(context, archiveList);

                            Toast.makeText(context, "Archived successfully", Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    });

                    dialog.show();
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return qrCodeList.size();
    }

    private String formatTimestamp(String raw) {
        if (raw == null || raw.trim().isEmpty() || raw.equals("1970-01-01 00:00:00")) {
            return "";
        }

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
            Date date = inputFormat.parse(raw);
            return outputFormat.format(date);
        } catch (Exception e) {
            Log.e(TAG, "Timestamp parsing error: " + e.getMessage());
            return "";
        }
    }

    private String renderTimestampList(List<String> timestamps) {
        if (timestamps == null || timestamps.isEmpty()) return "• No scans recorded";

        StringBuilder builder = new StringBuilder();
        for (String time : timestamps) {
            String formatted = formatTimestamp(time);
            if (!formatted.isEmpty()) {
                builder.append("• ").append(formatted).append("\n");
            }
        }
        return builder.length() == 0 ? "• No valid timestamps" : builder.toString().trim();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView driverIdTextView, driverNameTextView, franchiseIdTextView,
                operatorNameTextView, todaTextView, scanDatetimeTextView,
                scanCountTextView, scanTimestampsTextView;
        public MaterialButton deleteButton, reportButton;
        public ImageView archiveIcon;

        public ViewHolder(View view) {
            super(view);
            driverIdTextView = view.findViewById(R.id.driver_id_text_view);
            driverNameTextView = view.findViewById(R.id.driver_name_text_view);
            franchiseIdTextView = view.findViewById(R.id.franchise_id_text_view);
            operatorNameTextView = view.findViewById(R.id.operator_id_text_view);
            todaTextView = view.findViewById(R.id.toda_text_view);
            scanDatetimeTextView = view.findViewById(R.id.scan_timestamp_text_view);
            scanCountTextView = view.findViewById(R.id.text_scan_count);
            scanTimestampsTextView = view.findViewById(R.id.text_scan_timestamps);
            deleteButton = view.findViewById(R.id.button_delete);
            reportButton = view.findViewById(R.id.button_report);
            archiveIcon = view.findViewById(R.id.icon_action); // Make sure your XML uses this ID


        }
    }
}

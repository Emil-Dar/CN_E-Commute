package com.example.cne_commute;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class ScannedQrCodeAdapter extends RecyclerView.Adapter<ScannedQrCodeAdapter.ViewHolder> {

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

        // Bind commuter-facing fields
        holder.driverNameTextView.setText("Driver Name: " + qrCode.getDriverName());
        holder.driverContactTextView.setText("Driver Contact No: " + qrCode.getDriverContactNo());
        holder.scanDateTimeTextView.setText("Date and Time: " + qrCode.getScanTimestamp());

        Context context = holder.itemView.getContext();

        // Delete logic
        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Confirm Delete")
                    .setMessage("Are you sure you want to delete this QR code entry?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        int pos = holder.getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            qrCodeList.remove(pos);
                            notifyItemRemoved(pos);
                            QrCodeStorageHelper.saveQrCodeList(context, qrCodeList);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Report logic
        holder.reportButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReportActivity.class);
            intent.putExtra("EXTRA_DRIVER_NAME", qrCode.getDriverName());
            intent.putExtra("EXTRA_DRIVER_CONTACT", qrCode.getDriverContactNo());
            intent.putExtra("EXTRA_FRANCHISE_ID", qrCode.getFranchiseId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return qrCodeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView driverNameTextView, driverContactTextView, scanDateTimeTextView;
        public MaterialButton deleteButton, reportButton;

        public ViewHolder(View view) {
            super(view);
            driverNameTextView = view.findViewById(R.id.driver_name_text_view);
            driverContactTextView = view.findViewById(R.id.driver_contact_text_view);
            scanDateTimeTextView = view.findViewById(R.id.scan_datetime_text_view);
            deleteButton = view.findViewById(R.id.button_delete);
            reportButton = view.findViewById(R.id.button_report);
        }
    }
}

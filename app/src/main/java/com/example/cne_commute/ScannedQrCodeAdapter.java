package com.example.cne_commute;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
                .inflate(R.layout.item_scanned_qr_code_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScannedQrCode qrCode = qrCodeList.get(position);

        // Bind data to views
        holder.operatorNameTextView.setText(qrCode.getOperatorName());
        holder.ageTextView.setText(qrCode.getAge());
        holder.homeAddressTextView.setText(qrCode.getHomeAddress());
        holder.trPlateNumberTextView.setText(qrCode.getTrPlateNumber());
        holder.contactNoTextView.setText(qrCode.getContactNo());

        Context context = holder.itemView.getContext();

        // Delete button logic
        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(context, R.style.CustomAlertDialogTheme)
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

        // Report button logic - pass relevant info to ReportActivity
        holder.reportButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReportActivity.class);
            intent.putExtra("operator_name", qrCode.getOperatorName());
            intent.putExtra("plate_number", qrCode.getTrPlateNumber());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return qrCodeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView operatorNameTextView, ageTextView, homeAddressTextView,
                trPlateNumberTextView, contactNoTextView;
        public Button deleteButton, reportButton;

        public ViewHolder(View view) {
            super(view);
            operatorNameTextView = view.findViewById(R.id.operator_name_text_view);
            ageTextView = view.findViewById(R.id.age_text_view);
            homeAddressTextView = view.findViewById(R.id.home_address_text_view);
            trPlateNumberTextView = view.findViewById(R.id.tr_plate_number_text_view);
            contactNoTextView = view.findViewById(R.id.contact_no_text_view);
            deleteButton = view.findViewById(R.id.button_delete);
            reportButton = view.findViewById(R.id.button_report);
        }
    }
}

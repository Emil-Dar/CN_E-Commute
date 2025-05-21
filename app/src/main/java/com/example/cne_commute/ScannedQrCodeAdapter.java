package com.example.cne_commute;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_scanned_qr_code_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScannedQrCode qrCode = qrCodeList.get(position);
        holder.operatorNameTextView.setText(qrCode.getOperatorName());
        holder.ageTextView.setText(qrCode.getAge());
        holder.homeAddressTextView.setText(qrCode.getHomeAddress());
        holder.trPlateNumberTextView.setText(qrCode.getTrPlateNumber());
        holder.contactNoTextView.setText(qrCode.getContactNo());
    }

    @Override
    public int getItemCount() {
        return qrCodeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView operatorNameTextView, ageTextView, homeAddressTextView, trPlateNumberTextView, contactNoTextView;

        public ViewHolder(View view) {
            super(view);
            operatorNameTextView = view.findViewById(R.id.operator_name_text_view);
            ageTextView = view.findViewById(R.id.age_text_view);
            homeAddressTextView = view.findViewById(R.id.home_address_text_view);
            trPlateNumberTextView = view.findViewById(R.id.tr_plate_number_text_view);
            contactNoTextView = view.findViewById(R.id.contact_no_text_view);
        }
    }
}

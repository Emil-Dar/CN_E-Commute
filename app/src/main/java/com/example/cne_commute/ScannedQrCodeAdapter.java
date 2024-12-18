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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_scanned_qr_code, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScannedQrCode qrCode = qrCodeList.get(position);
        holder.dateTextView.setText(qrCode.getDate());
        holder.timeTextView.setText(qrCode.getTime());
        holder.transactionNumberTextView.setText(qrCode.getTransactionNumber());
    }

    @Override
    public int getItemCount() {
        return qrCodeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView dateTextView, timeTextView, transactionNumberTextView;

        public ViewHolder(View view) {
            super(view);
            dateTextView = view.findViewById(R.id.date_text_view);
            timeTextView = view.findViewById(R.id.time_text_view);
            transactionNumberTextView = view.findViewById(R.id.transaction_number_text_view);
        }
    }
}

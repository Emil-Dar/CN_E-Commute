package com.example.cne_commute;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FranchiseAdapter extends RecyclerView.Adapter<FranchiseAdapter.FranchiseViewHolder> {

    private List<Franchise> franchiseList;
    private Context context;

    public FranchiseAdapter(List<Franchise> franchiseList, Context context) {
        this.franchiseList = franchiseList;
        this.context = context;
    }

    @NonNull
    @Override
    public FranchiseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.franchise_card, parent, false);
        return new FranchiseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FranchiseViewHolder holder, int position) {
        Franchise franchise = franchiseList.get(position);

        holder.textFranchiseId.setText("Franchise ID: " + franchise.getFranchiseId());
        holder.textRegDate.setText("Registered: " + franchise.getRegistrationDate());
        holder.textToda.setText("TODA: " + franchise.getToda());


        // pass data to details activity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, FranchiseDetailsActivity.class);
            intent.putExtra("franchiseId", franchise.getFranchiseId());
            intent.putExtra("registrationDate", franchise.getRegistrationDate());
            intent.putExtra("toda", franchise.getToda());

            intent.putExtra("renewalStatus", franchise.getRenewalStatus());
            intent.putExtra("renewalDate", franchise.getRenewalDate());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return franchiseList.size();
    }

    static class FranchiseViewHolder extends RecyclerView.ViewHolder {
        TextView textFranchiseId, textRegDate, textToda;

        FranchiseViewHolder(@NonNull View itemView) {
            super(itemView);
            textFranchiseId = itemView.findViewById(R.id.textFranchiseId);
            textRegDate = itemView.findViewById(R.id.textRegDate);
            textToda = itemView.findViewById(R.id.textToda);

        }
    }
}

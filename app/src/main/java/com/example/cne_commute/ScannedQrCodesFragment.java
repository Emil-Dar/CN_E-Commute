package com.example.cne_commute;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ScannedQrCodesFragment extends Fragment {

    private RecyclerView recyclerView;
    private ScannedQrCodeAdapter adapter;
    private List<ScannedQrCode> qrCodeList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scanned_qr_codes, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_qr_codes);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        qrCodeList = new ArrayList<>();
        // Add sample data
        qrCodeList.add(new ScannedQrCode("June 12, 2023", "2:00 PM", "TR NUM: 1012"));
        qrCodeList.add(new ScannedQrCode("September 17, 2023", "1:25 PM", "TR NUM: 2561"));
        qrCodeList.add(new ScannedQrCode("October 24, 2023", "4:30 PM", "TR NUM: 0921"));

        adapter = new ScannedQrCodeAdapter(qrCodeList);
        recyclerView.setAdapter(adapter);

        return view;
    }
}

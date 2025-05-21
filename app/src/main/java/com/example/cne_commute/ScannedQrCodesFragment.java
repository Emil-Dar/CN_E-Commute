package com.example.cne_commute;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
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

        if (getActivity() != null) {
            Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
            View buttonsContainer = getActivity().findViewById(R.id.buttons_container);
            if (toolbar != null) {
                toolbar.setVisibility(View.VISIBLE);
                toolbar.setNavigationIcon(R.drawable.ic_arrow);
                toolbar.setNavigationOnClickListener(v -> getActivity().onBackPressed());
            }
            if (buttonsContainer != null) {
                buttonsContainer.setVisibility(View.GONE);
            }
        }

        recyclerView = view.findViewById(R.id.recycler_view_qr_codes);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Retrieve scanned data from arguments
        Bundle bundle = getArguments();
        if (bundle != null) {
            String scannedData = bundle.getString("scannedData");
            qrCodeList = new ArrayList<>();
            qrCodeList.add(new ScannedQrCode(scannedData, "", "", "", ""));
        } else {
            qrCodeList = new ArrayList<>();
        }

        adapter = new ScannedQrCodeAdapter(qrCodeList);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() != null) {
            Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
            View buttonsContainer = getActivity().findViewById(R.id.buttons_container);
            if (toolbar != null) {
                toolbar.setVisibility(View.GONE);
            }
            if (buttonsContainer != null) {
                buttonsContainer.setVisibility(View.VISIBLE);
            }
        }
    }
}

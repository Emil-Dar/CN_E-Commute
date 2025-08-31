package com.example.cne_commute;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class ScannedQrCodesFragment extends Fragment {

    private RecyclerView recyclerView;
    private ScannedQrCodeAdapter adapter;
    private List<ScannedQrCode> qrCodeList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // ✅ Enables back button handling
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_scanned_qr_codes, container, false);

        // ✅ Apply fade-in animation
        Animation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(300);
        view.startAnimation(fadeIn);

        setupToolbar(view);

        // Setup RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Load QR codes from storage
        qrCodeList = QrCodeStorageHelper.loadQrCodes(requireContext());

        // Show placeholder if no data
        if (qrCodeList == null || qrCodeList.isEmpty()) {
            qrCodeList = new ArrayList<>();

            // 🕒 Generate timestamp for placeholder
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
            String timestamp = dateFormat.format(new Date());

            qrCodeList.add(new ScannedQrCode(
                    "no-data-id",
                    "No scanned QR code found.",
                    "",
                    "",
                    timestamp
            ));
        }

        adapter = new ScannedQrCodeAdapter(qrCodeList);
        recyclerView.setAdapter(adapter);

        return view;
    }

    private void setupToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();

        if (activity != null && toolbar != null) {
            activity.setSupportActionBar(toolbar);

            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
                activity.getSupportActionBar().setTitle("Scanned QR Codes");
            }

            // ✅ Back arrow press: pop back stack with fade-in animation
            toolbar.setNavigationOnClickListener(v -> {
                requireActivity().getSupportFragmentManager().popBackStack();
                requireActivity().overridePendingTransition(R.anim.fade_in, 0);
            });
        }

        // Optional: Hide bottom buttons if present
        if (activity != null) {
            View buttonsContainer = activity.findViewById(R.id.buttons_container);
            if (buttonsContainer != null) {
                buttonsContainer.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            requireActivity().getSupportFragmentManager().popBackStack();
            requireActivity().overridePendingTransition(R.anim.fade_in, 0);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null && activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            activity.getSupportActionBar().setDisplayShowHomeEnabled(false);
            activity.getSupportActionBar().setTitle("");
        }

        // Show buttons again when exiting fragment
        if (activity != null) {
            View buttonsContainer = activity.findViewById(R.id.buttons_container);
            if (buttonsContainer != null) {
                buttonsContainer.setVisibility(View.VISIBLE);
            }
        }
    }
}

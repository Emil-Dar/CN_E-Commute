package com.example.cne_commute;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ScannedQrCodesFragment extends Fragment {

    private RecyclerView recyclerView;
    private ScannedQrCodeAdapter adapter;
    private List<ScannedQrCode> qrCodeList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // Enables back button handling
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        Log.d("ScannedQrCodesFragment", "Fragment loaded successfully");

        View view = inflater.inflate(R.layout.fragment_scanned_qr_codes, container, false);

        applyFadeIn(view);
        setupToolbar(view);
        setupRecyclerView(view);
        loadQrCodes(view); // ✅ Pass view safely

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Reload fresh data when returning
        if (getView() != null) {
            loadQrCodes(getView()); // ✅ Safe reload
        }

        // Ensure system navigation bar remains visible
        View decorView = requireActivity().getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

        // Hide bottom navigation bar (app-specific)
        View bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setVisibility(View.GONE);
        }
    }

    private void applyFadeIn(View view) {
        Animation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(300);
        view.startAnimation(fadeIn);
    }

    private void setupToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();

        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            v.setPadding(0, insets.getSystemWindowInsetTop(), 0, 0);
            return insets;
        });

        if (activity != null && toolbar != null) {
            activity.setSupportActionBar(toolbar);

            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
                activity.getSupportActionBar().setTitle("Scanned QR Codes");
            }

            toolbar.setNavigationOnClickListener(v -> {
                requireActivity().getSupportFragmentManager().popBackStack();
                requireActivity().overridePendingTransition(R.anim.fade_in, 0);
            });
        }

        // Optional: Hide other button containers if present
        if (activity != null) {
            View buttonsContainer = activity.findViewById(R.id.buttons_container);
            if (buttonsContainer != null) {
                buttonsContainer.setVisibility(View.GONE);
            }
        }
    }

    private void setupRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void loadQrCodes(View rootView) {
        List<ScannedQrCode> allCodes = QrCodeStorageHelper.loadQrCodes(requireContext());
        qrCodeList = new ArrayList<>();

        if (allCodes != null) {
            for (ScannedQrCode code : allCodes) {
                if (!code.isArchived()) {
                    qrCodeList.add(code);
                }
            }
        }

        adapter = new ScannedQrCodeAdapter(qrCodeList);
        recyclerView.setAdapter(adapter);

        // ✅ Use passed-in view safely
        TextView emptyMessage = rootView.findViewById(R.id.empty_message);
        if (emptyMessage != null) {
            emptyMessage.setVisibility(qrCodeList.isEmpty() ? View.VISIBLE : View.GONE);
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

        // Restore bottom navigation bar
        View bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setVisibility(View.VISIBLE);
        }

        // Restore other buttons if needed
        View buttonsContainer = activity.findViewById(R.id.buttons_container);
        if (buttonsContainer != null) {
            buttonsContainer.setVisibility(View.VISIBLE);
        }
    }
}

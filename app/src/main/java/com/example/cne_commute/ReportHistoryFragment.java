package com.example.cne_commute;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ReportHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private ReportAdapter adapter;
    private List<Report> reportList;

    private static final String PREF_NAME = "ReportData";
    private static final String KEY_REPORT_LIST = "reportList";

    public ReportHistoryFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_report_history, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Load reports
        reportList = loadReportsFromSharedPreferences();
        adapter = new ReportAdapter(reportList, true);
        recyclerView.setAdapter(adapter);

        return view;
    }

    private List<Report> loadReportsFromSharedPreferences() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_REPORT_LIST, null);

        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Report>>() {}.getType();
            return gson.fromJson(json, type);
        } else {
            return new ArrayList<>(); // Return empty list if none stored
        }
    }
}

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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ReportHistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private ReportAdapter adapter;
    private List<Report> reportList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report_history, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        reportList = loadReportsFromArguments();
        adapter = new ReportAdapter(reportList);
        recyclerView.setAdapter(adapter);

        return view;
    }

    private List<Report> loadReportsFromArguments() {
        Bundle args = getArguments();
        if (args != null && args.containsKey("reportList")) {
            return (ArrayList<Report>) args.getSerializable("reportList");
        }
        return new ArrayList<>();
    }
}

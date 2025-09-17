package com.example.cne_commute;

import com.google.gson.annotations.SerializedName;
import java.util.List;

// Represents the response from Supabase after inserting a report
public class ReportResponse {

    @SerializedName("data")
    private List<Report> data;

    public List<Report> getData() {
        return data;
    }

    public void setData(List<Report> data) {
        this.data = data;
    }
}

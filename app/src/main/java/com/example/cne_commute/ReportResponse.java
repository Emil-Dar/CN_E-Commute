package com.example.cne_commute;

import com.google.gson.annotations.SerializedName;
import java.util.List;

// Wraps Supabase response after inserting or fetching reports
public class ReportResponse {

    @SerializedName("data")
    private List<Report> data;

    // Optional: Supabase sometimes returns metadata or status
    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    public List<Report> getData() {
        return data != null ? data : List.of(); // Null-safe fallback
    }

    public void setData(List<Report> data) {
        this.data = data;
    }

    public String getStatus() {
        return status != null ? status : "";
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message != null ? message : "";
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

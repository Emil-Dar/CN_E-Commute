package com.example.cne_commute;

import com.google.gson.annotations.SerializedName;

public class ReportIdOnly {

    @SerializedName("report_id")
    private String reportId;

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }
}

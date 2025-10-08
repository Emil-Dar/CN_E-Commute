package com.example.cne_commute;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface SupabaseService {

    // Submits a report and receives the inserted row(s) from Supabase
    @POST("reports")
    Call<Void> submitReport(@Body Map<String, Object> reportData);





    // Optional: Fetches the latest report ID (if needed for tracking)
    @GET("reports?select=report_id&order=report_id.desc&limit=1")
    Call<List<ReportIdOnly>> getLatestReportId();
}

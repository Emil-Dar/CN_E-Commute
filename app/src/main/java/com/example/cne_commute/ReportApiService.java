package com.example.cne_commute;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ReportApiService {

    // Submit a new report
    @POST("reports")
    Call<Void> submitReport(@Body ReportData reportData);

    // Get all reports for a specific user
    @GET("reports?select=*")
    Call<List<ReportData>> getReports(@Query("user_id") String userId);

    // Get only accepted reports for a specific user using Supabase-style filters
    @GET("reports?select=*")
    Call<List<ReportData>> getAcceptedReports(
            @Query("user_id") String userId,
            @Query("status") String status
    );

    // ✅ Fixed: Mark a report as read using @Query
    @PATCH("reports")
    Call<Void> markReportAsRead(
            @Query("id") String idQuery,
            @Body Map<String, Object> body
    );
}

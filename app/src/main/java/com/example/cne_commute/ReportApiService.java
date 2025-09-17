package com.example.cne_commute;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ReportApiService {

    // Insert a new report into the "reports" table
    @POST("reports")
    Call<Void> submitReport(@Body ReportData reportData);

    // Fetch reports filtered by userId
    @GET("reports")
    Call<List<ReportData>> getReports(@Query("userId") String userId);
}


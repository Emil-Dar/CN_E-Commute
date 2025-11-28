package com.example.cne_commute;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface ReportApiService {

    // Submit a new report
    @POST("reports")
    Call<Void> submitReport(@Body ReportData reportData);

    // Flexible base: use any filters (user_id, status, id, etc.)
    @GET("reports?select=*")
    Call<List<ReportData>> getReports(@QueryMap Map<String, String> filters);

    // Mark a report as read
    @PATCH("reports")
    Call<Void> markReportAsRead(
            @retrofit2.http.Query("id") String idQuery,
            @Body Map<String, Object> body
    );

    // ---------------- BACKWARD-COMPAT METHODS ----------------
    // These signatures match your existing code, but delegate to getReports(@QueryMap)

    // Get all reports for a specific user
    @GET("reports?select=*")
    default Call<List<ReportData>> getReports(@Query("user_id") String userIdEq) {
        Map<String, String> filters = new HashMap<>();
        if (userIdEq != null && !userIdEq.isEmpty()) {
            filters.put("user_id", userIdEq); // e.g., "eq.user123"
        }
        filters.put("select", "*");
        return getReports(filters);
    }

    // Get only accepted reports for a specific user
    @GET("reports?select=*")
    default Call<List<ReportData>> getAcceptedReports(
            @Query("user_id") String userIdEq,
            @Query("status") String statusEq // can be null in your older calls
    ) {
        Map<String, String> filters = new HashMap<>();
        if (userIdEq != null && !userIdEq.isEmpty()) {
            filters.put("user_id", userIdEq);
        }
        filters.put("status", statusEq != null ? statusEq : "eq.Accepted");
        filters.put("select", "*");
        return getReports(filters);
    }

    // Get reports for a user with multiple statuses (your previous pattern)
    @GET("reports?select=*")
    default Call<List<ReportData>> getReportsByStatuses(
            @Query("user_id") String userIdFilter,
            @Query("status") String statusFilter // e.g., "in.(accepted,resolved,follow-up)" or "eq.Accepted"
    ) {
        Map<String, String> filters = new HashMap<>();
        if (userIdFilter != null && !userIdFilter.isEmpty()) {
            filters.put("user_id", userIdFilter);
        }
        if (statusFilter != null && !statusFilter.isEmpty()) {
            filters.put("status", statusFilter);
        }
        filters.put("select", "*");
        return getReports(filters);
    }

    // Get a single report by id (still returns a list; take first item)
    @GET("reports?select=*")
    default Call<List<ReportData>> getReportById(@Query("report_id") String reportIdEq) {
        Map<String, String> filters = new HashMap<>();
        if (reportIdEq != null && !reportIdEq.isEmpty()) {
            filters.put("report_id", reportIdEq); // or use "id" if your column is 'id'
        }
        filters.put("select", "*");
        return getReports(filters);
    }
}

package com.example.cne_commute;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SupabaseService {

    // === reports table ===
    @POST("reports")
    Call<Void> submitReport(@Body Map<String, Object> reportData);

    @GET("reports")
    Call<List<Map<String, Object>>> getReportsByDriverId(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authHeader,
            @Query("driver_id") String driverIdFilter, // e.g. "eq.123"
            @Query("order") String orderBy,            // e.g. "created_at.desc"
            @Query("select") String selectFields       // e.g. "report_id,status,created_at"
    );

    @GET("reports")
    Call<List<Map<String, Object>>> getReports(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authHeader
    );

    // ✅ NEW: mark report as viewed
    // ✅ Mark specific report as viewed
    @PATCH("reports")
    Call<List<Map<String, Object>>> updateReportViewed(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authHeader,
            @Query("report_id") String reportIdFilter, // e.g., "eq.123"
            @Body Map<String, Object> updates
    );



    // === operators table ===
    @GET("operators")
    Call<List<Operator>> getOperatorById(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authHeader,
            @Query("operator_id") String operatorIdFilter
    );

    @PATCH("operators")
    Call<Void> updateOperator(
            @Query("operator_id") String operatorIdFilter,
            @Body Map<String, Object> updates,
            @Header("apikey") String apiKey,
            @Header("Authorization") String authHeader
    );

    // === franchises table ===
    @GET("franchises")
    Call<List<Franchise>> getFranchisesByOperatorId(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authHeader,
            @Query("operator_id") String operatorIdFilter
    );

    // === drivers table ===
    @GET("drivers")
    Call<List<Driver>> getDriverById(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authHeader,
            @Query("driver_id") String driverIdFilter
    );

    @POST("drivers")
    Call<Void> addDriver(@Body Map<String, Object> driverData);

    @GET("drivers")
    Call<List<Driver>> getDrivers();

    @GET("drivers")
    Call<List<Map<String, Object>>> getLastDriverId(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authHeader,
            @Query("select") String select,
            @Query("order") String order,
            @Query("limit") int limit
    );

    // === assignments table ===
    @POST("assignments")
    Call<Void> assignDriver(@Body Assignment assignment);

    @GET("assignments")
    Call<List<Map<String, Object>>> getLastAssignmentId(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authHeader,
            @Query("select") String select,
            @Query("order") String order,
            @Query("limit") int limit
    );

    @GET("assignments")
    Call<List<Map<String, Object>>> getLatestAssignmentForFranchise(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authHeader,
            @Query("franchise_id") String franchiseIdFilter,
            @Query("order") String orderBy,
            @Query("limit") int limit
    );

    // === reports management ===
    @PATCH("reports")
    Call<Void> updateReportStatus(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authHeader,
            @Query("report_id") String reportIdFilter,
            @Body Map<String, Object> updates
    );

    // === appointments table ===
    @POST("appointments")
    Call<Void> createAppointment(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authHeader,
            @Body Map<String, Object> appointmentData
    );

    @GET("appointments")
    Call<List<Map<String, Object>>> getAppointmentsByReportId(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authHeader,
            @Query("report_id") String filter
    );

    @GET("reports?driver_id={driverId}")
    Call<List<Map<String, Object>>> getReportsByDriver(
            @Header("apikey") String apiKey,
            @Header("Authorization") String bearerToken,
            @Query("driver_id") String driverId
    );



}

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
    Call<Void> submitReport(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authHeader,
            @Body Map<String, Object> reportData
    );

    @GET("reports")
    Call<List<Map<String, Object>>> getReportsByDriverId(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authHeader,
            @Query("driver_id") String driverIdFilter,
            @Query("order") String orderBy,
            @Query("select") String selectFields
    );

    @GET("reports")
    Call<List<Map<String, Object>>> getReports(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authHeader
    );

    @PATCH("reports")
    Call<List<Map<String, Object>>> updateReportViewed(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authHeader,
            @Query("report_id") String reportIdFilter,
            @Body Map<String, Object> updates
    );

    // === operators ===
    @GET("operators")
    Call<List<Operator>> getOperatorById(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authHeader,
            @Query("operator_id") String operatorIdFilter
    );

    @PATCH("operators")
    Call<Void> updateOperator(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authHeader,
            @Query("operator_id") String operatorIdFilter,
            @Body Map<String, Object> updates
    );

    // === franchises ===
    @GET("franchises")
    Call<List<Franchise>> getFranchisesByOperatorId(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authHeader,
            @Query("operator_id") String operatorIdFilter
    );

    // === drivers ===
    @GET("drivers")
    Call<List<Driver>> getDriverById(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authHeader,
            @Query("driver_id") String driverIdFilter
    );

    @POST("drivers")
    Call<Void> addDriver(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authHeader,
            @Body Map<String, Object> driverData
    );

    @GET("drivers")
    Call<List<Driver>> getDrivers(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authHeader
    );

    @GET("drivers")
    Call<List<Map<String, Object>>> getLastDriverId(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authHeader,
            @Query("select") String select,
            @Query("order") String order,
            @Query("limit") int limit
    );

    @GET("drivers")
    Call<List<Driver>> getVerifiedDrivers(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authHeader,
            @Query("status") String statusFilter
    );

    @PATCH("drivers")
    Call<Void> updateDriver(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authHeader,
            @Query("driver_id") String driverIdFilter,
            @Body Map<String, Object> updates
    );



    // === assignments ===
    @POST("assignments")
    Call<Void> assignDriver(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authHeader,
            @Body Assignment assignment
    );

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

    // reports
    @PATCH("reports")
    Call<Void> updateReportStatus(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authHeader,
            @Query("report_id") String reportIdFilter,
            @Body Map<String, Object> updates
    );

    // appointments
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

    // FIXED - Removed invalid {driverId}
    @GET("reports")
    Call<List<Map<String, Object>>> getReportsByDriver(
            @Header("apikey") String apiKey,
            @Header("Authorization") String bearerToken,
            @Query("driver_id") String driverId
    );

    @GET("reports")
    Call<List<ReportData>> getReportDetails(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authHeader,
            @Query("report_id") String reportIdFilter
    );

    @GET("reports")
    Call<List<ReportIdOnly>> getLatestReportId(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authHeader,
            @Query("select") String select,
            @Query("order") String order,
            @Query("limit") int limit
    );
}

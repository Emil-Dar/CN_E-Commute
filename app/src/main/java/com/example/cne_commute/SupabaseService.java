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

    // === operators table ===
    @GET("operators")
    Call<List<Operator>> getOperatorById(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authHeader,
            @Query("operator_id") String operatorIdFilter // e.g. "eq.123"
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
            @Header("Authorization") String auth,
            @Query("select") String select,
            @Query("order") String order,
            @Query("limit") int limit
    );

    // === assignments table ===
    @POST("assignments")
    Call<Void> assignDriver(@Body Assignment assignment);

    // GET the last assignment_id to generate the next sequential ID
    @GET("assignments")
    Call<List<Map<String, Object>>> getLastAssignmentId(
            @Header("apikey") String apiKey,
            @Header("Authorization") String auth,
            @Query("select") String select,  // "assignment_id"
            @Query("order") String order,    // "desc"
            @Query("limit") int limit        // 1
    );
    @GET("assignments")
    Call<List<Map<String, Object>>> getLatestAssignmentForFranchise(
            @Header("apikey") String apiKey,
            @Header("Authorization") String auth,
            @Query("franchise_id") String franchiseIdFilter, // must be "eq.<franchiseId>"
            @Query("order") String orderBy,                  // "assigned_at.desc"
            @Query("limit") int limit
    );

}

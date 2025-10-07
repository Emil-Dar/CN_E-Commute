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
            @Body Map<String, Object> reportData
    );

    // === operators table ===
    // GET operator by ID
    @GET("operators")
    Call<List<Operator>> getOperatorById(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authHeader,
            @Query("operator_id") String operatorIdFilter // e.g. "eq.123"
    );

    // PATCH operator (update fields)
    @PATCH("operators")
    Call<Void> updateOperator(
            @Query("operator_id") String operatorIdFilter, // e.g. "eq.123"
            @Body Map<String, Object> updates,             // fields to update
            @Header("apikey") String apiKey,
            @Header("Authorization") String authHeader
    );

    // === franchises table ===
    @GET("franchises")
    Call<List<Franchise>> getFranchisesByOperatorId(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authHeader,
            @Query("operator_id") String operatorIdFilter // e.g. "eq.123"
    );

    // === drivers table ===
    @GET("drivers")
    Call<List<Driver>> getDriverById(
            @Header("apikey") String apiKey,
            @Header("Authorization") String authHeader,
            @Query("driver_id") String driverIdFilter // e.g. "eq.ABC123"
    );
}

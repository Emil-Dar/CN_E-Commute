package com.example.cne_commute;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface SupabaseService {
    @POST("reports")
    Call<Void> submitReport(@Body Map<String, Object> reportData);
}

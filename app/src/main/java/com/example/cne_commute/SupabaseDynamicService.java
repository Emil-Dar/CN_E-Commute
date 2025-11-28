package com.example.cne_commute;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface SupabaseDynamicService {

    // This allows dynamic GET URLs (for example: "reports?select=*&id=eq.123")
    @GET
    Call<List<Map<String, Object>>> getReportDetails(@Url String url);
}

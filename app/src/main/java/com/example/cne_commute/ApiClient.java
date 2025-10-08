package com.example.cne_commute;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String TAG = "ApiClient";
    private static Retrofit retrofit = null;

    private static final String BASE_URL = "https://rtwrbkrroilftdhggxjc.supabase.co/rest/v1/";
    private static final String SUPABASE_API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJ0d3Jia3Jyb2lsZnRkaGdneGpjIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTQ4MDg1OTksImV4cCI6MjA3MDM4NDU5OX0.eiCQTeLh9IG4mX3cNqoIe6-cq33pzeO_qSTtONuMnKA";

    public static Retrofit getClient() {
        if (retrofit == null) {
            Log.d(TAG, "Initializing Retrofit client");

            // Logging interceptor
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Supabase headers interceptor
            Interceptor supabaseInterceptor = chain -> {
                Request request = chain.request().newBuilder()
                        .addHeader("apikey", SUPABASE_API_KEY)
                        .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                        .build();
                return chain.proceed(request);
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(supabaseInterceptor)
                    .addInterceptor(loggingInterceptor)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();

            Log.d(TAG, "Retrofit client initialized");
        }

        return retrofit;
    }
}

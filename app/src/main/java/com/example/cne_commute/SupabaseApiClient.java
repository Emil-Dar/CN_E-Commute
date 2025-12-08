package com.example.cne_commute;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SupabaseApiClient {

    // Base REST endpoint
    private static final String SUPABASE_REST_URL = "https://rtwrbkrroilftdhggxjc.supabase.co/rest/v1/";

    // Root project URL (used for Storage/public URLs)
    private static final String SUPABASE_PROJECT_URL = "https://rtwrbkrroilftdhggxjc.supabase.co";

    // API key (stored securely in local.properties → BuildConfig.SUPABASE_API_KEY)
    public static final String SUPABASE_API_KEY = BuildConfig.SUPABASE_API_KEY;

    private static Retrofit retrofit;

    // 🔹 Getter for REST base URL (optional)
    public static String getRestUrl() {
        return SUPABASE_REST_URL;
    }

    // 🔹 Getter for Project root URL (useful for Storage public file URLs)
    public static String getProjectUrl() {
        return SUPABASE_PROJECT_URL;
    }

    // 🔹 Getter for API key
    public static String getApiKey() {
        return SUPABASE_API_KEY;
    }

    // 🔹 Standard Retrofit instance for REST calls
    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            Interceptor headerInterceptor = chain -> {
                Request original = chain.request();
                Request request = original.newBuilder()
                        .header("apikey", SUPABASE_API_KEY)
                        .header("Authorization", "Bearer " + SUPABASE_API_KEY)
                        .header("Content-Type", "application/json")
                        .method(original.method(), original.body())
                        .build();
                return chain.proceed(request);
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(headerInterceptor)
                    // 🔹 Increase timeout to 30s
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true) // retry automatically
                    .build();

            Gson gson = new GsonBuilder().serializeNulls().create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(SUPABASE_REST_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build();
        }
        return retrofit;
    }

}

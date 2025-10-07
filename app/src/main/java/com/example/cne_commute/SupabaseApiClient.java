package com.example.cne_commute;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;

public class SupabaseApiClient {

    private static final String SUPABASE_BASE_URL = "https://rtwrbkrroilftdhggxjc.supabase.co/rest/v1/";
    private static final String SUPABASE_API_KEY = BuildConfig.SUPABASE_API_KEY; // from gradle
    private static Retrofit retrofit;

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // add api key + authorization header automatically ♡
            Interceptor headerInterceptor = chain -> {
                Request original = chain.request();
                Request request = original.newBuilder()
                        .header("apikey", SUPABASE_API_KEY)
                        .header("Authorization", "Bearer " + SUPABASE_API_KEY)
                        .method(original.method(), original.body())
                        .build();
                return chain.proceed(request);
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(headerInterceptor)
                    .build();

            Gson gson = new GsonBuilder()
                    .serializeNulls()
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(SUPABASE_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build();
        }
        return retrofit;
    }
}

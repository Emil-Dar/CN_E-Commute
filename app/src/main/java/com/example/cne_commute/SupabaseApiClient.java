package com.example.cne_commute;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SupabaseApiClient {

    private static final String SUPABASE_BASE_URL = "https://rtwrbkrroilftdhggxjc.supabase.co/rest/v1/";
    private static Retrofit retrofit;

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            // log network requests
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            // gson with serializeNulls to ensure JsonNull is sent
            Gson gson = new GsonBuilder()
                    .serializeNulls() // <— critical!
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

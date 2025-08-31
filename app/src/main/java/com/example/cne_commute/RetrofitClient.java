package com.example.cne_commute;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import com.example.cne_commute.BuildConfig;

public class RetrofitClient {

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request request = original.newBuilder()
                                .header("apikey", BuildConfig.SUPABASE_API_KEY)
                                .header("Authorization", "Bearer " + BuildConfig.SUPABASE_API_KEY)
                                .header("Content-Type", "application/json")
                                .method(original.method(), original.body())
                                .build();
                        return chain.proceed(request);
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl("https://rtwrbkrroilftdhggxjc.supabase.co") // ← Replace with your actual Supabase REST URL
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }
}

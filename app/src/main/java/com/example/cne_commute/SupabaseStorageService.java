package com.example.cne_commute;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface SupabaseStorageService {

    @Multipart
    @POST("storage/v1/object/{bucket}/{path}")
    Call<Void> uploadFile(
            @Path("bucket") String bucket,
            @Path(value = "path", encoded = true) String path,
            @Header("Authorization") String authorization,
            @Header("apikey") String apiKey,
            @Part MultipartBody.Part file
    );
}

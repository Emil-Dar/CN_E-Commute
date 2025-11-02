package com.example.cne_commute;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

public interface AppointmentApiService {

    @GET("appointments?select=*")
    Call<List<Appointment>> getAppointmentsForUser(@QueryMap Map<String, String> filters);
}

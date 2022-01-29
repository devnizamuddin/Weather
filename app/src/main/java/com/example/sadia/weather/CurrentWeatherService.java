package com.example.sadia.weather;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface CurrentWeatherService {

    @GET()
    Call<CurrentWeatherResponse> getCurrentWeatherResponse(@Url String currentWeatherUrl);

}

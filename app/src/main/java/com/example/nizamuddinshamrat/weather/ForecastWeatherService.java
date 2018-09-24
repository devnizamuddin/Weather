package com.example.nizamuddinshamrat.weather;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface ForecastWeatherService {

    @GET()
    Call<ForecastWeatherResponse> getWeatherForecastResponse(@Url String string);

}

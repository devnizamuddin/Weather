package com.example.sadia.weather;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;

import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {


    private String api_key = "d0b941339d6f075686460c7fe0912041";
    public static final String CURRENT_BASE_URL_WEATHER = "http://api.openweathermap.org/data/2.5/";
    public static final String FORECAST_BASE_URL_WEATHER = "https://api.openweathermap.org/data/2.5/forecast/";
    private String unit = "metric";
    private int LOCATION_REQUEST_CODE = 1;

    private CurrentWeatherService currentWeatherService;
    private ForecastWeatherService forecastWeatherService;
    private Geocoder geocoder;

    private String currentWeatherUrl;
    private String forecastWeatherUrl;

    private RecyclerView forecastRV;
    private TextView locationTV, tempTV, weatherConditionTv;
    private SearchView searchView;

    private ForecastAdapter forecastAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //**********************initialize Variable Start**********************//
        forecastRV = findViewById(R.id.forecastRv);
        locationTV = findViewById(R.id.locationTv);
        tempTV = findViewById(R.id.tempTv);
        weatherConditionTv = findViewById(R.id.weatherConditionTv);
        searchView = findViewById(R.id.searchView);

        //=====================================setup forcast weather recyclerview==============================================================//

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView.ItemDecoration itemDecoration = new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);

                outRect.set(0, 0, 32, 0);
            }
        };
        forecastAdapter = new ForecastAdapter(MainActivity.this, new ForecastWeatherResponse());
        forecastRV.addItemDecoration(itemDecoration);
        forecastRV.setLayoutManager(layoutManager);
        forecastRV.setAdapter(forecastAdapter);


        if (checkPermission()) {
            getResultByLocation();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION
                    , Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);
        }


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                getSearchResult(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        //****OnCreateMethodCalling Finished*****//


    }

    private void getResultByLocation() {
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                for (Location location : locationResult.getLocations()) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    Toast.makeText(MainActivity.this, "location Call Back Called", Toast.LENGTH_SHORT).show();

                    currentWeatherUrl = String.format("weather?lat=%f&lon=%f&units=%s&appid=%s", latitude, longitude, unit, api_key);
                    gettingCurrentWeatherResponse(currentWeatherUrl);

                    forecastWeatherUrl =
                            String.format("daily?lat=%f&lon=%f&cnt=10&units=%s&appid=%s", latitude, longitude, unit, api_key);
                    gettingForecastWeatherResponse(forecastWeatherUrl);

                    geocoder = new Geocoder(MainActivity.this);

                    getLocationName(latitude, longitude);

                }
                super.onLocationResult(locationResult);
            }
        };


        LocationRequest locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_LOW_POWER)
                .setInterval(15 * 60 * 1000)
                .setFastestInterval(15 * 30 * 1000);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);

    }

    private boolean checkPermission() {
        if (ActivityCompat.checkSelfPermission(this
                , Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this
                , Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return false;
        }
        else {
            return true;
        }
    }

    private void getLocationName(double latitude, double longitude) {


        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            locationTV.setText(addresses.get(0).getLocality() + addresses.get(0).getCountryName());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void gettingForecastWeatherResponse(final String forecastWeatherUrl) {



        Retrofit forecastRetrofit = new Retrofit.Builder()
                .baseUrl(FORECAST_BASE_URL_WEATHER)
                .addConverterFactory(GsonConverterFactory.create())
                .build();


        forecastWeatherService = forecastRetrofit.create(ForecastWeatherService.class);


        Call<ForecastWeatherResponse> forecastWeatherResponseCall =
                forecastWeatherService.getWeatherForecastResponse(forecastWeatherUrl);

        forecastWeatherResponseCall.enqueue(new Callback<ForecastWeatherResponse>() {
            @Override
            public void onResponse(Call<ForecastWeatherResponse> call, Response<ForecastWeatherResponse> response) {


                if (response.code() == 200) {
                    if (response.code() == 200) {
                        ForecastWeatherResponse forecastWeatherResponse = response.body();
                        String date = forecastWeatherResponse.getList().get(0).getDt().toString();
                        //    Toast.makeText(context, date, Toast.LENGTH_SHORT).show();
                        forecastAdapter.updateForeCastResponse(forecastWeatherResponse);


                    }
                } else {
                    Toast.makeText(MainActivity.this, "No", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<ForecastWeatherResponse> call, Throwable t) {

                Toast.makeText(MainActivity.this, "ForeCast" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //************************** Get Current Weather Start**********************//
    private void gettingCurrentWeatherResponse(String currentWeatherUrl) {

        Retrofit currentRetrofit = new Retrofit.Builder()
                .baseUrl(CURRENT_BASE_URL_WEATHER)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        currentWeatherService = currentRetrofit.create(CurrentWeatherService.class);

        Call<CurrentWeatherResponse> currentWeatherResponse = currentWeatherService.getCurrentWeatherResponse(currentWeatherUrl);
        currentWeatherResponse.enqueue(new Callback<CurrentWeatherResponse>() {
            @Override
            public void onResponse(Call<CurrentWeatherResponse> call, Response<CurrentWeatherResponse> response) {
                if (response.code() == 200) {
                    CurrentWeatherResponse weatherInfo = response.body();
                    if (weatherInfo != null) {

                        tempTV.setText(String.format("%.1f", weatherInfo.getMain().getTemp()) + "°C");
                        weatherConditionTv.setText(weatherInfo.getWeather().get(0).getDescription());
                        locationTV.setText(weatherInfo.getSys().getCountry());

                    }
                }
            }

            @Override
            public void onFailure(Call<CurrentWeatherResponse> call, Throwable t) {

                Toast.makeText(MainActivity.this, "Current: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    //***Get Current Weather Finished***//


    //************************** Search Result Start**********************//
    private void getSearchResult(String query) {

            Toast.makeText(this, "" + query, Toast.LENGTH_SHORT).show();
            currentWeatherUrl = String.format("weather?q=%s&units=%s&appid=%s", query, unit, api_key);
            gettingCurrentWeatherResponse(currentWeatherUrl);
            forecastWeatherUrl = String.format("daily?q=%s&cnt=10&units=%s&appid=%s", query, unit, api_key);
            gettingForecastWeatherResponse(forecastWeatherUrl);

    }
    //**** Search Result Finish*****//



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {


        if (requestCode == LOCATION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();

            finish();
            startActivity(getIntent());
        } else {

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


}

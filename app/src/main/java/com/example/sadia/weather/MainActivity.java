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

    CurrentWeatherService currentWeatherService;
    ForecastWeatherService forecastWeatherService;


    String currentWeatherUrl;
    String forecastWeatherUrl;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;
    LocationRequest locationRequest;
    private int LOCATION_REQUEST_CODE = 1;

    private Geocoder geocoder;
    private Context context;

    RecyclerView.LayoutManager layoutManager;
    androidx.appcompat.widget.Toolbar toolbar;
    RecyclerView forecastRV;
    TextView locationTV,tempTV,weatherConditionTv;
    boolean searchWeather = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //**********************initialize Variable Start**********************//

        toolbar = findViewById(R.id.toolbar);
        forecastRV = findViewById(R.id.forecastRv);

        locationTV = findViewById(R.id.locationTv);
        tempTV = findViewById(R.id.tempTv);
        weatherConditionTv = findViewById(R.id.weatherConditionTv);

        layoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        RecyclerView.ItemDecoration itemDecoration = new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);

                outRect.set(0,0,32,0);
            }
        };
        forecastRV.addItemDecoration(itemDecoration);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        context = getApplicationContext();

        Retrofit currentRetrofit = new Retrofit.Builder()
                .baseUrl(CURRENT_BASE_URL_WEATHER)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        Retrofit forecastRetrofit = new Retrofit.Builder()
                .baseUrl(FORECAST_BASE_URL_WEATHER)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        currentWeatherService = currentRetrofit.create(CurrentWeatherService.class);
        forecastWeatherService = forecastRetrofit.create(ForecastWeatherService.class);


        Intent intent = new Intent();

        //location
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

if (!searchWeather) {


    locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {

            for (Location location : locationResult.getLocations()) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                Toast.makeText(context, "location Call Back Called", Toast.LENGTH_SHORT).show();

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

}



        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_LOW_POWER)
                .setInterval(15*60*1000)
                .setFastestInterval(15*30*1000);


//********Request Location Permission
        if (ActivityCompat.checkSelfPermission(this
                , Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this
                , Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION
                    , Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);
            return;
        }
        //Location Finished


        //****initialize Variable Finished****//

        //***********************OnCreateMethodCalling Start******************//

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);


        getSearchResult(intent);
        //****OnCreateMethodCalling Finished*****//


    }

    private void getLocationName(double latitude, double longitude) {


        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            locationTV.setText(addresses.get(0).getLocality()+addresses.get(0).getCountryName());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void gettingForecastWeatherResponse(final String forecastWeatherUrl) {

        Call<ForecastWeatherResponse> forecastWeatherResponseCall =
                forecastWeatherService.getWeatherForecastResponse(forecastWeatherUrl);

        forecastWeatherResponseCall.enqueue(new Callback<ForecastWeatherResponse>() {
            @Override
            public void onResponse(Call<ForecastWeatherResponse> call, Response<ForecastWeatherResponse> response) {


                if (response.code() == 200){
                    Toast.makeText(context, "Yes", Toast.LENGTH_SHORT).show();
                    if (response.code() == 200){
                        ForecastWeatherResponse forecastWeatherResponse = response.body();
                        String date = forecastWeatherResponse.getList().get(0).getDt().toString();
                        Toast.makeText(context, date, Toast.LENGTH_SHORT).show();
                        forecastRV.setLayoutManager(layoutManager);
                        ForecastAdapter forecastAdapter = new ForecastAdapter(MainActivity.this,forecastWeatherResponse);
                        forecastRV.setAdapter(forecastAdapter);


                    }
                }
                else {
                    Toast.makeText(context, "No", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onFailure(Call<ForecastWeatherResponse> call, Throwable t) {

                Toast.makeText(context, "ForeCast" +t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    //***OnCreateMethod Finished***//

    //************************** Get Current Weather Start**********************//
    private void gettingCurrentWeatherResponse(String currentWeatherUrl) {

        Call<CurrentWeatherResponse> currentWeatherResponse = currentWeatherService.getCurrentWeatherResponse(currentWeatherUrl);
        currentWeatherResponse.enqueue(new Callback<CurrentWeatherResponse>() {
            @Override
            public void onResponse(Call<CurrentWeatherResponse> call, Response<CurrentWeatherResponse> response) {
                if (response.code() == 200) {
                    CurrentWeatherResponse weatherInfo = response.body();
                    if (weatherInfo != null) {

                        tempTV.setText(String.format("%.1f",weatherInfo.getMain().getTemp())+"°C");
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
    private void getSearchResult(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            searchWeather = true;
            String query = intent.getStringExtra(SearchManager.QUERY);
            Toast.makeText(this, "" + query, Toast.LENGTH_SHORT).show();
            currentWeatherUrl = String.format("weather?q=%s&units=%s&appid=%s", query, unit, api_key);
            gettingCurrentWeatherResponse(currentWeatherUrl);
            forecastWeatherUrl = String.format("daily?q=%s&cnt=10&units=%s&appid=%s",query,unit,api_key);
            gettingCurrentWeatherResponse(forecastWeatherUrl);
        }

    }
    //**** Search Result Finish*****//

    //************************** Menu Item Start**********************//

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);

        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(true);


        return super.onCreateOptionsMenu(menu);
    }
    //**** Menu Item Finish*****//

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

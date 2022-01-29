package com.example.sadia.weather;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder> {

    private Context context;
    private ForecastWeatherResponse forecastWeatherResponse;

    public ForecastAdapter(Context context, ForecastWeatherResponse forecastWeatherResponse) {
        this.context = context;
        this.forecastWeatherResponse = forecastWeatherResponse;
    }

    @NonNull
    @Override
    public ForecastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.forecast_single_layout,parent,false);


        return new ForecastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ForecastViewHolder holder, int position) {

        ForecastWeatherResponse.List forecastList = forecastWeatherResponse.getList().get(position);

        Date date = new Date(forecastList.getDt());
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE");
        String day = formatter.format(date);
        holder.dayTv.setText(day);
        holder.minTempTv.setText("Min:"+forecastList.getTemp().getMin().toString());
        holder.maxTempTV.setText("Max:"+forecastList.getTemp().getMax().toString());
    }

    @Override
    public int getItemCount() {
        return forecastWeatherResponse.getList().size();
    }

    public class ForecastViewHolder extends RecyclerView.ViewHolder {

        TextView dayTv;
        TextView minTempTv;
        TextView maxTempTV;

        public ForecastViewHolder(View itemView) {
            super(itemView);

            dayTv = itemView.findViewById(R.id.dayTv);
            minTempTv = itemView.findViewById(R.id.minTempTV);
            maxTempTV = itemView.findViewById(R.id.maxTempTv);
        }
    }
}

package com.example.skb.farmingassistance;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


public class WeatherViewHolders extends RecyclerView.ViewHolder{

    //private static final String TAG = "WeatherActivity";

    public TextView dayOfWeek;

    public ImageView weatherIcon;

    public TextView weatherResult;

    public TextView weatherResultSmall;

    public WeatherViewHolders(final View itemView) {
        super(itemView);
        dayOfWeek = (TextView)itemView.findViewById(R.id.weather_daily_day_of_week);
        weatherIcon = (ImageView)itemView.findViewById(R.id.weather_daily_icon);
        weatherResult = (TextView) itemView.findViewById(R.id.weather_daily_result);
        weatherResultSmall = (TextView)itemView.findViewById(R.id.weather_daily_result_small);
    }
}

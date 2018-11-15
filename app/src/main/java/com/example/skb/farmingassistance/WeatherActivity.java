package com.example.skb.farmingassistance;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.pavlospt.CircleView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeatherActivity extends AppCompatActivity {


    private static final String TAG = "WeatherActivity";
    TextView tempTextView;
    TextView dateTextView;
    TextView weatherDescTextView;
    TextView cityTextView;
    TextView windTextView;
    TextView humidityTextView;
    ImageView weatherImageView;
    private CircleView circleView;
    TextView dailyDayTextView, dailyTemTextView, dailyMinTempTextView;
    RecyclerView dailyRecycler;
    private WeatherAdapter recyclerViewAdapter;
    private RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_activity);
        Toolbar AnnouncementToolbar = (Toolbar) findViewById(R.id.toolbar_Weather);
        setSupportActionBar(AnnouncementToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowHomeEnabled(true);
        ab.setTitle("আবহাওয়ার পূর্বাভাস");

//        tempTextView = (TextView) findViewById(R.id.tempTextView);
//        weatherDescTextView = (TextView) findViewById(R.id.weatherDesctextView);
//
        circleView = (CircleView) findViewById(R.id.weather_result_circle);
        dateTextView = (TextView) findViewById(R.id.weather_current_date);
        cityTextView = (TextView) findViewById(R.id.weather_city_country);
        windTextView = (TextView) findViewById(R.id.weather_wind_result);
        humidityTextView = (TextView) findViewById(R.id.weather_humidity_result);
        weatherImageView = (ImageView) findViewById(R.id.weather_weather_icon);


        dailyDayTextView = (TextView) findViewById(R.id.weather_daily_day_of_week);
        dailyTemTextView = (TextView) findViewById(R.id.weather_daily_result);
        dailyMinTempTextView = (TextView) findViewById(R.id.weather_daily_result_small);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(WeatherActivity.this, 5);

        dailyRecycler = (RecyclerView) findViewById(R.id.weather_daily_list_recycler);
        dailyRecycler.setLayoutManager(gridLayoutManager);
        dailyRecycler.setHasFixedSize(true);

        dateTextView.setText(getCurrentDate());



        String url = "http://api.openweathermap.org/data/2.5/weather?q=Dhaka,BGD&appid=1ddbb1a57d026001dc3fabc3c2350147&units=Metric";

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject responseObject) {
                        //tempTextView.setText("Response: " + response.toString());
                        Log.v("WEATHER", "Response: " + responseObject.toString());

                        try
                        {
                            JSONObject mainJSONObject = responseObject.getJSONObject("main");
                            JSONObject windJsonObject = responseObject.getJSONObject("wind");

                            JSONArray weatherArray = responseObject.getJSONArray("weather");
                            JSONObject firstWeatherObject = weatherArray.getJSONObject(0);


//                            JSONArray humidityArray = responseObject.getJSONArray("humidity");
//                            JSONObject firstHumidityObject = humidityArray.getJSONObject(0);
                            //String humidity = firstHumidityObject.getString("speed");

                            String city = responseObject.getString("name");
                            String wind = windJsonObject.getString("speed");
                            String humidity = mainJSONObject.getString("humidity");
                            String temp = Integer.toString((int) Math.round(mainJSONObject.getDouble("temp")));
                            String weatherDescription = firstWeatherObject.getString("description");

                            //tempTextView.setText(temp);
                            //weatherDescTextView.setText(weatherDescription);


                            cityTextView.setText(city);
                            windTextView.setText( wind + " km/h");
                            humidityTextView.setText(humidity + " %");
                            circleView.setTitleText(temp + "°c");
                            circleView.setSubtitleText(weatherDescription);


                            String icon = firstWeatherObject.getString("icon");
                            String iconUrl = "http://openweathermap.org/img/w/" + icon + ".png";

                            Picasso.get().load(iconUrl).placeholder(R.drawable.sun).into(weatherImageView);

//                            int iconResourceId = getResources().getIdentifier("icon_" + weatherDescription.replace(" ", ""), "drawable", getPackageName());
//                            weatherImageView.setImageResource(iconResourceId);
                            fiveDaysApiJsonObjectCall(city);

                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                });

        // Access the RequestQueue through your singleton class.
        queue = Volley.newRequestQueue(this);
        queue.add(jsObjRequest);

    }

    private String getCurrentDate ()
    {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM dd");
        String formattedDate = dateFormat.format(calendar.getTime());

        return formattedDate;
    }

    private void fiveDaysApiJsonObjectCall(String city){
        String apiUrl = "http://api.openweathermap.org/data/2.5/forecast?q="+city+ "&APPID=1ddbb1a57d026001dc3fabc3c2350147&units=metric";
        final List<WeatherObject> daysOfTheWeek = new ArrayList<WeatherObject>();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, apiUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Response 5 days" + response);
                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                Forecast forecast = gson.fromJson(response, Forecast.class);
                if (null == forecast) {
                    Toast.makeText(getApplicationContext(), "নেটওয়ার্ক এর সমস্যা ।", Toast.LENGTH_LONG).show();
                } else {
                   // Toast.makeText(getApplicationContext(), "Response Good", Toast.LENGTH_LONG).show();

                    int[] everyday = new int[]{0,0,0,0,0,0,0};

                    List<FiveWeathers> weatherInfo = forecast.getList();
                    if(null != weatherInfo){
                        for(int i = 0; i < weatherInfo.size(); i++){
                            String time = weatherInfo.get(i).getDt_txt();
                            String shortDay = convertTimeToDay(time);
                            String temp = weatherInfo.get(i).getMain().getTemp();
                            String tempMin = weatherInfo.get(i).getMain().getTemp_min();

                            if(convertTimeToDay(time).equals("Mon") && everyday[0] < 1){
                                daysOfTheWeek.add(new WeatherObject(shortDay, R.drawable.small_weather_icon, temp, tempMin));
                                everyday[0] = 1;
                            }
                            if(convertTimeToDay(time).equals("Tue") && everyday[1] < 1){
                                daysOfTheWeek.add(new WeatherObject(shortDay, R.drawable.small_weather_icon, temp, tempMin));
                                everyday[1] = 1;
                            }
                            if(convertTimeToDay(time).equals("Wed") && everyday[2] < 1){
                                daysOfTheWeek.add(new WeatherObject(shortDay, R.drawable.small_weather_icon, temp, tempMin));
                                everyday[2] = 1;
                            }
                            if(convertTimeToDay(time).equals("Thu") && everyday[3] < 1){
                                daysOfTheWeek.add(new WeatherObject(shortDay, R.drawable.small_weather_icon, temp, tempMin));
                                everyday[3] = 1;
                            }
                            if(convertTimeToDay(time).equals("Fri") && everyday[4] < 1){
                                daysOfTheWeek.add(new WeatherObject(shortDay, R.drawable.small_weather_icon, temp, tempMin));
                                everyday[4] = 1;
                            }
                            if(convertTimeToDay(time).equals("Sat") && everyday[5] < 1){
                                daysOfTheWeek.add(new WeatherObject(shortDay, R.drawable.small_weather_icon, temp, tempMin));
                                everyday[5] = 1;
                            }
                            if(convertTimeToDay(time).equals("Sun") && everyday[6] < 1){
                                daysOfTheWeek.add(new WeatherObject(shortDay, R.drawable.small_weather_icon, temp, tempMin));
                                everyday[6] = 1;
                            }
                            recyclerViewAdapter = new WeatherAdapter(WeatherActivity.this, daysOfTheWeek);
                            dailyRecycler.setAdapter(recyclerViewAdapter);
                        }
                    }
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error " + error.getMessage());
            }
        });
        queue.add(stringRequest);
    }


    private String convertTimeToDay(String time){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:SSSS", Locale.getDefault());
        String days = "";
        try {
            Date date = format.parse(time);
            //System.out.println("Our time " + date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            days = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault());
            //System.out.println("Our time " + days);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return days;
    }
}

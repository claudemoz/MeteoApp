package fr.claude.meteoapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import android.os.Handler;
import android.os.Looper;

public class DataActivity extends AppCompatActivity implements LocationListener {
    private ImageView btn_back;
    EditText input_search;
    TextView city, etat, temp, dg, temp_min, temp_max, speed, humidity, rain;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchAddressRunnable;
    private LocationManager locationManager;
    private LocationListener locationListener;
    ArrayList arrayList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        //btn_back = findViewById(R.id.btn_back);
        input_search = findViewById(R.id.input_search);
        city = findViewById(R.id.city);
        etat = findViewById(R.id.etat);
        temp = findViewById(R.id.temp);
        temp_min = findViewById(R.id.temp_min);
        temp_max = findViewById(R.id.temp_max);
        speed = findViewById(R.id.speed);
        humidity = findViewById(R.id.humidity);
        rain = findViewById(R.id.rain);

        //dg = findViewById(R.id.dg);

        /*btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent( DataActivity.this, MainActivity.class ));
                finish();
            }
        });*/

        if (ActivityCompat.checkSelfPermission(DataActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(DataActivity.this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            }, 100);
        }

        input_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                /*if (searchAddressRunnable != null) {
                    handler.removeCallbacks(searchAddressRunnable);
                }
                handler.postDelayed(searchAddressRunnable, 1000);
                searchAddressRunnable = new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("==================" + charSequence.toString());
                    }
                };*/

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (searchAddressRunnable != null) {
                    handler.removeCallbacks(searchAddressRunnable);
                }
                handler.postDelayed(searchAddressRunnable, 3000);
                searchAddressRunnable = new Runnable() {
                    @Override
                    public void run() {
                        fetchCityData(editable.toString());
                    }
                };
            }
        });
        //String c = "Issy-les-Moulineaux";
        //fetchCityData(c);
        getLocation();
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        try {
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, DataActivity.this);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Toast.makeText(this, "" + location.getLatitude() + " , " + location.getLongitude(), Toast.LENGTH_SHORT).show();
        fetchDataCurrentPostion((float) location.getLatitude(), (float) location.getLongitude());
        try {
            Geocoder geocoder = new Geocoder(DataActivity.this, Locale.getDefault());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(@NonNull List<Location> locations) {
        LocationListener.super.onLocationChanged(locations);
    }

    @Override
    public void onFlushComplete(int requestCode) {
        LocationListener.super.onFlushComplete(requestCode);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        LocationListener.super.onStatusChanged(provider, status, extras);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
    }

    public void fetchDataCurrentPostion(float l, float L){
        //arrayList = new ArrayList<>();
        String API;
        API = "https://api.openweathermap.org/data/2.5/weather?lat=" + l + "&lon=" + L + "&lang=fr&units=metric&appid=7b7048a95c0160c9b69a853ee70506cc";
        System.out.println("url " + API);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, API, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        /*if(response.getBoolean("success")){
                            JSONArray jsonArray = response.getJSONArray("data");


                        }*/
                        try {
                            temp.setText( response.getJSONObject("main").getString("temp") + "°C");
                            temp_min.setText("Min Temp:" + " " +response.getJSONObject("main").getString("temp_min") + "°C");
                            temp_max.setText("Max Temp:" + " " +response.getJSONObject("main").getString("temp_max") + "°C");
                            city.setText( response.getString("name"));
                            etat.setText( response.getJSONArray("weather").getJSONObject(0).getString("description"));
                            rain.setText(response.getJSONObject("main").getString("pressure") + "%");
                            speed.setText(response.getJSONObject("wind").getString("speed") + " km/h");
                            humidity.setText( response.getJSONObject("main").getString("humidity") + "%");

                            /*etat, value, dg;*/
                            /*etat, value, dg;temp_max speed, humidity*/
                            System.out.println("*******************" + response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(DataActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                        NetworkResponse response = error.networkResponse;
                        if(error instanceof ServerError && response != null){
                            try {
                                String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                            }catch(UnsupportedEncodingException je){
                                je.printStackTrace();
                            }
                        }
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type","application/json");

                return headers;
            }
        };

        int socketTime = 3000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTime,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    public void fetchCityData(String c){
        //arrayList = new ArrayList<>();
        String API;
        API = "https://api.openweathermap.org/data/2.5/weather?q=" + c + "&lang=fr&units=metric&appid=7b7048a95c0160c9b69a853ee70506cc";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, API, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        /*if(response.getBoolean("success")){
                            JSONArray jsonArray = response.getJSONArray("data");


                        }*/
                        try {
                            temp.setText( response.getJSONObject("main").getString("temp") + "°C");
                            temp_min.setText("Min Temp:" + " " +response.getJSONObject("main").getString("temp_min") + "°C");
                            temp_max.setText("Max Temp:" + " " +response.getJSONObject("main").getString("temp_max") + "°C");
                            city.setText( response.getString("name"));
                            etat.setText( response.getJSONArray("weather").getJSONObject(0).getString("description"));
                            rain.setText(response.getJSONObject("main").getString("pressure") + "%");
                            speed.setText(response.getJSONObject("wind").getString("speed") + " km/h");
                            humidity.setText( response.getJSONObject("main").getString("humidity") + "%");
                            /*etat, value, dg;*/
                            /*etat, value, dg speed, humidity;pressure*/
                            System.out.println("*******************" + response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(DataActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                        NetworkResponse response = error.networkResponse;
                        if(error instanceof ServerError && response != null){
                            try {
                                String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                            }catch(UnsupportedEncodingException je){
                                je.printStackTrace();
                            }
                        }
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type","application/json");

                return headers;
            }
        };

        int socketTime = 3000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTime,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }
}
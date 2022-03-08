package com.example.weatherapp

import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.jar.Manifest
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    lateinit var weatherModelArrayList: ArrayList<WeatherModel>
    lateinit var adapter: Adapter
    lateinit var weatherRV: RecyclerView
    lateinit var locationManager: LocationManager
    var PERMISSION_CODE: Int = 1
    lateinit var CityName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        setContentView(R.layout.activity_main)

        weatherRV = findViewById(R.id.RVTodayForecast)

        weatherModelArrayList = ArrayList<WeatherModel>()
        adapter = Adapter(this, weatherModelArrayList)
        weatherRV.adapter = adapter

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                PERMISSION_CODE
            )
        }

        var location: Location? = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        if (location != null) {
            CityName = getCityName(location.longitude, location.latitude)
        }

        getWeatherInfo(CityName)

        IVSearch.setOnClickListener {
            var city: String = TECityLocation.text.toString()

            if (city.isEmpty()) {
                Toast.makeText(this, "Please Enter City Name", Toast.LENGTH_SHORT).show()
            } else {
                getWeatherInfo(city)
            }
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

    }

        fun getCityName(longitude: Double, latitude: Double): String {
            var cityName: String = "Not Found"
            var gcd: Geocoder = Geocoder(baseContext, Locale.getDefault())
            Log.d("Latitude","$latitude")
            Log.d("longitude","$longitude")

            try {
                var address: List<Address> = gcd.getFromLocation(latitude, longitude, 1)

                for (adr in address) {
                    if (adr != null) {
                        val city = adr.locality
                        Log.d("City","$city")
                        if (city != null && !city.equals("")) {
                            cityName = city
                        } else {
                            Log.d("Location", "City Not Found")
                            Toast.makeText(this, "User City Not Found", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return cityName
        }

        fun getWeatherInfo(cityName: String) {
            var url: String =
                "http://api.weatherapi.com/v1/forecast.json?key=e66803847543443fa01142645220703&q=${cityName}&days=1&aqi=yes&alerts=yes"

            val Queue = Volley.newRequestQueue(this)

            val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
                { response ->
                    loaddata.visibility = View.GONE
                    homedata.visibility = View.VISIBLE
                    weatherModelArrayList.clear()

                    try{
                        val temperature = response.getJSONObject("current").getString("temp_c")
                        cityname.text = cityName

                        TVTemperature.text = temperature.plus("°C")

                        val isDay = response.getJSONObject("current").getInt("is_day")
                        val conditionText = response.getJSONObject("current").getJSONObject("condition").getString("text")
                        val conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon")
                        Picasso.get()
                            .load("http://".plus(conditionIcon))
                            .into(IVTemperature_Image)

                        TVCondition.text = conditionText

                        val forecastObj = response.getJSONObject("forecast")
                        val forecastDay0 = forecastObj.getJSONArray("forecastday").getJSONObject(0)
                        val hourArray = forecastDay0.getJSONArray("hour")

                        for (i in 0 until hourArray.length()){
                            var hourObj = hourArray.getJSONObject(i)
                            var time = hourObj.getString("time")
                            var temp = hourObj.getString("temp_c")
                            var forecastIcon = hourObj.getJSONObject("condition").getString("icon")
                            var windSpeed = hourObj.getString("wind_kph")
                            weatherModelArrayList.add(WeatherModel(time,temp,forecastIcon,windSpeed))
                        }

                        adapter.notifyDataSetChanged()
                    }

                    catch (e: Exception){
                        e.printStackTrace()
                    }

                },
                { error ->
                    Toast.makeText(this,"Please Enter Valid City Name",Toast.LENGTH_SHORT).show()
                }
            );

            Queue.add(jsonObjectRequest)
        }
}
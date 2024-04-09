package com.example.weather

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var tvCity: TextView
    private lateinit var tvConditions: TextView
    private lateinit var tvTemp: TextView
    private lateinit var tvWind: TextView
    private lateinit var tvCloudCoverage: TextView

    private lateinit var etCity: EditText
    private lateinit var btnCitySearch: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvCity = findViewById(R.id.tvCity)
        tvConditions = findViewById(R.id.tvConditionsData)
        tvTemp = findViewById(R.id.tvTempData)
        tvWind = findViewById(R.id.tvWindData)
        tvCloudCoverage = findViewById(R.id.tvCloudCoverageData)

        etCity = findViewById(R.id.etCity)
        btnCitySearch = findViewById(R.id.btnCity)

        var city = "Spring Grove"
        val apiKey = "6c21997a524538d106f5e4f68f326801"
        var apiURL = "https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apiKey&units=metric"

        etCity.requestFocus()

        displayWeather(city, apiURL)

        btnCitySearch.setOnClickListener {
            city = etCity.text.toString()
            apiURL = "https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apiKey&units=metric"
            displayWeather(city, apiURL)
            etCity.setText("")
        }
    }

    private fun displayWeather(city: String, apiURL: String) {
        tvCity.text = city

        Thread {
            try {
                val url = URL(apiURL)
                with(url.openConnection() as HttpURLConnection) {
                    val responseCode = responseCode
                    runOnUiThread {
                        Toast.makeText(
                            applicationContext,
                            "Response Code: $responseCode",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()

                    val jsonResponse = response.toString()
                    val jsonObject = JSONObject(jsonResponse)

                    //Condition
                    val weatherArray = jsonObject.getJSONArray("weather")
                    val weatherObject = weatherArray.getJSONObject(0)
                    val conditions = weatherObject.getString("main")
                    tvConditions.text = conditions

                    //Temp
                    val mainObject = jsonObject.getJSONObject("main")
                    val temperature = mainObject.getDouble("temp")
                    val fTemp = celsiusToFahrenheit(temperature).toDouble()
                    tvTemp.text = "$fTemp°F"

                    //Wind
                    val windObject = jsonObject.getJSONObject("wind")
                    val speed = windObject.getDouble("speed")
                    val windSpeed = metersToMiles(speed).toDouble()
                    val direction = windObject.getDouble("deg")
                    val windDirection = degreesToDirection(direction)
                    tvWind.text = "$windSpeed MPH $windDirection"

                    //Cloud Coverage
                    val cloudObject = jsonObject.getJSONObject("clouds")
                    val cloudCoverage = cloudObject.getInt("all")
                    tvCloudCoverage.text = "$cloudCoverage%"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun celsiusToFahrenheit(celsius: Double): String {
        val fahrenheit = (celsius * 9 / 5) + 32

        return String.format("%.2f", fahrenheit)
    }

    private fun degreesToDirection(degrees: Double): String {
        val directions = arrayOf("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW")
        val index = ((degrees + 11.25) / 22.5).toInt() % 16

        return directions[index]
    }

    private fun metersToMiles(metersPerSecond: Double): String {
        val milesPerHour = metersPerSecond * 2.23694

        return String.format("%.2f", milesPerHour)
    }
}
package com.example.lena.viewModels

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lena.BuildConfig
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject

class WeatherViewModel : ViewModel() {
    private val apiKey = BuildConfig.OPEN_WEATHER_MAP_API_KEY
    private val client: OkHttpClient

    init {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC)
        client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    fun fetchWeather(lat: Double, lon: Double, datetime: String?, forecastType: String, onResult: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val url = if (forecastType == "forecast") {
                "https://api.openweathermap.org/data/2.5/forecast?lat=$lat&lon=$lon&appid=$apiKey"
            } else {
                "https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&appid=$apiKey"
            }

            try {
                Log.i("weatherViewModel", "Fetching weather data from URL: $url")

                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (responseBody != null) {
                    Log.i("weatherViewModel", "Response received: $responseBody")
                    val json = JSONObject(responseBody)
                    val weatherDescription: String
                    val temperature: Double

                    if (forecastType == "forecast" && datetime != null) {
                        val forecastArray = json.optJSONArray("list")
                        if (forecastArray != null) {
                            val targetDate = datetime.split("T")[0] // Extract the date part
                            var forecast: JSONObject? = null
                            for (i in 0 until forecastArray.length()) {
                                val forecastItem = forecastArray.getJSONObject(i)
                                if (forecastItem.getString("dt_txt").startsWith(targetDate)) {
                                    forecast = forecastItem
                                    break
                                }
                            }

                            if (forecast != null) {
                                weatherDescription = forecast.getJSONArray("weather").getJSONObject(0).getString("description")
                                temperature = forecast.getJSONObject("main").getDouble("temp") - 273.15 // Convert from Kelvin to Celsius
                            } else {
                                onResult("No forecast available for the specified date")
                                Log.e("weatherViewModel", "No forecast available for the specified date")
                                return@launch
                            }
                        } else {
                            onResult("No forecast data available")
                            Log.e("weatherViewModel", "No forecast data available")
                            return@launch
                        }
                    } else {
                        val weatherArray = json.optJSONArray("weather")
                        val mainObject = json.optJSONObject("main")
                        if (weatherArray != null && mainObject != null) {
                            weatherDescription = weatherArray.getJSONObject(0).getString("description")
                            temperature = mainObject.getDouble("temp") - 273.15 // Convert from Kelvin to Celsius
                        } else {
                            onResult("Invalid weather data received")
                            Log.e("weatherViewModel", "Invalid weather data received")
                            return@launch
                        }
                    }

                    onResult("Weather: $weatherDescription, Temperature: ${"%.2f".format(temperature)}°C")
                } else {
                    onResult("Failed to retrieve weather data")
                    Log.e("weatherViewModel", "Failed to retrieve weather data")
                }
            } catch (e: Exception) {
                Log.e("weatherViewModel", "Error fetching weather data", e)
                onResult("Error fetching weather data: ${e.message}")
            }
        }
    }

    fun checkWeatherCondition(lat: Double, lon: Double, datetime: String?, forecastType: String, condition: String?, onResult: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val url = if (forecastType == "forecast") {
                "https://api.openweathermap.org/data/2.5/forecast?lat=$lat&lon=$lon&appid=$apiKey"
            } else {
                "https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&appid=$apiKey"
            }

            try {
                Log.i("weatherViewModel", "Checking weather condition with URL: $url")

                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (responseBody != null) {
                    Log.i("weatherViewModel", "Response received: $responseBody")
                    val json = JSONObject(responseBody)

                    val isConditionMet: Boolean

                    if (forecastType == "forecast" && datetime != null) {
                        val forecastArray = json.optJSONArray("list")
                        if (forecastArray != null) {
                            val targetDate = datetime.split("T")[0] // Extract the date part
                            var forecast: JSONObject? = null
                            for (i in 0 until forecastArray.length()) {
                                val forecastItem = forecastArray.getJSONObject(i)
                                if (forecastItem.getString("dt_txt").startsWith(targetDate)) {
                                    forecast = forecastItem
                                    break
                                }
                            }

                            if (forecast != null) {
                                isConditionMet = checkCondition(forecast, condition)
                            } else {
                                onResult("No forecast available for the specified date")
                                Log.e("weatherViewModel", "No forecast available for the specified date")
                                return@launch
                            }
                        } else {
                            onResult("No forecast data available")
                            Log.e("weatherViewModel", "No forecast data available")
                            return@launch
                        }
                    } else {
                        isConditionMet = checkCondition(json, condition)
                    }

                    onResult(if (isConditionMet) "Yes, the condition is met" else "No, the condition is not met")
                } else {
                    onResult("Failed to retrieve weather data")
                    Log.e("weatherViewModel", "Failed to retrieve weather data")
                }
            } catch (e: Exception) {
                Log.e("weatherViewModel", "Error checking weather condition", e)
                onResult("Error checking weather condition: ${e.message}")
            }
        }
    }

    private fun checkCondition(json: JSONObject, condition: String?): Boolean {
        return when (condition?.lowercase()) {
            "temperature" -> json.optJSONObject("main")?.has("temp") == true
            "humidity" -> json.optJSONObject("main")?.has("humidity") == true
            "wind" -> json.optJSONObject("wind")?.has("speed") == true
            "storm" -> json.optJSONArray("weather")?.getJSONObject(0)?.getString("main")?.contains("storm", true) == true
            "cloudy" -> json.optJSONArray("weather")?.getJSONObject(0)?.getString("main")?.contains("cloud", true) == true
            "snow" -> json.optJSONArray("weather")?.getJSONObject(0)?.getString("main")?.contains("snow", true) == true
            "sunny" -> json.optJSONArray("weather")?.getJSONObject(0)?.getString("main")?.contains("clear", true) == true
            "rain" -> json.optJSONArray("weather")?.getJSONObject(0)?.getString("main")?.contains("rain", true) == true
            else -> false
        }
    }

    @SuppressLint("MissingPermission")
    fun fetchCurrentLocationWeather(context: Context, onResult: (Location?) -> Unit) {
        val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            onResult(location)
        }.addOnFailureListener {
            onResult(null)
        }
    }
}
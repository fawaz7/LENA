package com.yarmouk.lena.viewModels

/**
 * WeatherViewModel.kt
 *
 * This Kotlin file defines the `WeatherViewModel` class, which is a ViewModel responsible for managing weather-related functionalities within the LENA application.
 * It integrates with the OpenWeatherMap API to fetch weather data and uses a generative AI model to generate user-friendly weather information.
 *
 * Key Components:
 * - ViewModel Initialization:
 *   - Initializes an `OkHttpClient` with logging interceptor for network requests.
 *   - Initializes a `GenerativeModel` instance with API key and system instructions for generating responses.
 *
 * - Functions:
 *   - `fetchWeather(lat: Double, lon: Double, datetime: String?, forecastType: String, onResult: (String) -> Unit)`:
 *     - Fetches weather data from OpenWeatherMap API based on latitude, longitude, and forecast type.
 *     - Processes the weather data using the generative AI model and returns the result.
 *
 *   - `checkWeatherCondition(lat: Double, lon: Double, datetime: String?, forecastType: String, condition: String?, onResult: (String) -> Unit)`:
 *     - Checks specific weather conditions from OpenWeatherMap API based on latitude, longitude, and forecast type.
 *     - Processes the weather data and evaluates the condition using the generative AI model.
 *
 *   - `fetchCurrentLocationWeather(context: Context, onResult: (Location?) -> Unit)`:
 *     - Fetches the current location using the FusedLocationProviderClient and returns the location.
 *
 * - Utility Functions:
 *   - `processWithGemini(weatherData: String): String`:
 *     - A suspend function that processes the weather data using the generative AI model and returns the generated content.
 *     - Handles exceptions and logs errors if content generation fails.
 *   - `checkCondition(json: JSONObject, condition: String?): Boolean`:
 *     - Checks if the weather data meets the specified condition (e.g., temperature, humidity, storm).
 *
 * Usage:
 * - The `WeatherViewModel` class provides comprehensive weather management, including fetching weather data, checking specific conditions, and managing current location weather.
 * - It ensures that weather data is fetched correctly and provides clear feedback to the user through AI-generated messages.
 *
 * This ViewModel enhances the LENA application's capabilities by integrating with the OpenWeatherMap API and leveraging generative AI for improved user interactions.
 */

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yarmouk.lena.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject


class WeatherViewModel : ViewModel() {
    private val apiKey = BuildConfig.OPEN_WEATHER_MAP_API_KEY
    private val client: OkHttpClient
    private val generativeModel: GenerativeModel





    init {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC)
        client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash-8b",
            apiKey = BuildConfig.GOOGLE_API_KEY,
            systemInstruction = content  {
                text("You are a helpful virtual assistant specialized in providing concise and easy-to-understand weather information.")
                text("Your task is to process the weather data response from OpenWeatherMap and present it in a user-friendly format.")
                text("You should focus on key weather information such as temperature, weather conditions (e.g., sunny, cloudy, rainy), wind speed, and humidity.")
                text("Omit less important details such as pressure, visibility, and any technical terms.")
                text("Ensure the response is brief and clear, just like how a personal virtual assistant would describe the weather.")
            }
        )

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

                    // Process data with Gemini
                    val processedData = processWithGemini(responseBody)

                    // Return processed data
                    onResult(processedData.toString())
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

    private suspend fun processWithGemini(weatherData: String): String {
        return withContext(Dispatchers.IO) {
            try {
                // Use GenerativeModel to process the weather data
                val response = generativeModel.generateContent(weatherData)
                response.text ?: "Failed to process data with Gemini"
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Error generating content with Gemini: ${e.message}")
                "Error generating content with Gemini: ${e.message}"
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
                Log.i("WeatherViewModel", "Checking weather condition with URL: $url")

                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (responseBody != null) {
                    Log.i("WeatherViewModel", "Response received: $responseBody")

                    // Process data with Gemini
                    val processedData = processWithGemini(responseBody)

                    // Send the processed data and condition to Gemini for evaluation
                    val conditionPrompt = "Check if the following weather data meets the condition: $condition\n\n$processedData"
                    val conditionResult = processWithGemini(conditionPrompt)

                    // Return the result from Gemini
                    onResult(conditionResult)
                } else {
                    onResult("Failed to retrieve weather data")
                    Log.e("WeatherViewModel", "Failed to retrieve weather data")
                }
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Error checking weather condition", e)
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
package com.example.lena.viewModels

import android.Manifest
import android.app.Application
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.core.app.ActivityCompat
import androidx.core.app.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.lena.BuildConfig
import com.example.lena.Data.LenaConstants.thinkingStrings
import com.example.lena.Models.MessageModel
import com.example.lena.utils.WitAiClient
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.launch


class ChatViewModel(application: Application) : AndroidViewModel(application) {

    val messageList by lazy {
        mutableStateListOf<MessageModel>()
    }

    val systemInstruction = content {
        text("Your name is Lena. its a short for 'Linguistic Engine Natural Assistant'.")
        text("Your logo is based on the Medusa Logo.")
        text("You're designed as a virtual assistant (Similar to Siri, Alexa, etc), but you're way more superior than all of them")
        text("You're made by 2 Computer Science Students as their final year project")
    }

    val generativeModel : GenerativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash-8b",
        apiKey = BuildConfig.GOOGLE_API_KEY,
        systemInstruction = systemInstruction
    )

    private val witAiClient = WitAiClient(BuildConfig.WIT_AI_TOKEN)
    private val weatherViewModel = WeatherViewModel()


    fun sendMessage(prompt: String) {
        viewModelScope.launch {
            try {
                val randomThinkingString = thinkingStrings.random()

                messageList.add(MessageModel(prompt.trimEnd(' '), "user"))
                messageList.add(MessageModel(randomThinkingString, "model"))

                witAiClient.sendMessage(prompt) { response ->
                    handleWitAiResponse(response, prompt)
                }
            } catch (e: Exception) {
                messageList.removeAt(messageList.lastIndex)
                messageList.add(MessageModel("Sorry, something went wrong.", "model"))
                Log.e("ChatViewModel", "Error sending message: ${e.message}")
            }
        }
    }

    private fun handleWitAiResponse(response: String, prompt: String) {
        try {
            val json = JsonParser.parseString(response).asJsonObject
            val intentsArray = json.getAsJsonArray("intents")

            if (intentsArray != null && intentsArray.size() > 0) {
                val intentName = intentsArray.get(0).asJsonObject.get("name").asString
                when (intentName) {
                    "control_device_feature" -> handleControlDeviceFeature(json)
                    "wit\$get_weather" -> handleWeatherQuery(json)
                    //"wit\$check_weather_condition" -> handleWeatherConditionQuery(json)
                    //"lena_set_reminder" -> handleSetReminder(json)
                    //"lena_set_recurring_reminder" -> handleSetRecurringReminder(json)
                    //"wit\$create_alarm" -> handleSetAlarm(json)
                    //"lena_get_directions" -> handleGetDirections(json)
                    else -> fallbackToGenerativeModel(prompt)
                }
            } else {
                fallbackToGenerativeModel(prompt)
            }
        } catch (e: Exception) {
            Log.e("ChatViewModel", "Error parsing response: ${e.message}")
            fallbackToGenerativeModel(prompt)
        }
    }
    //================================================================--> Handle Control Device Feature
    private fun handleControlDeviceFeature(json: JsonObject) {
        try {
            val traits = json.getAsJsonObject("traits")
            val entities = json.getAsJsonObject("entities")

            val action = traits?.getAsJsonArray("Action")?.get(0)?.asJsonObject?.get("value")?.asString ?: "unknown"
            val feature = entities?.getAsJsonArray("feature:feature")?.get(0)?.asJsonObject?.get("value")?.asString ?: "unknown"
            Log.i("ChatViewModel", "feature: $feature")

            val context = getApplication<Application>().applicationContext
            when (feature.lowercase()) {
                "wifi" -> toggleWiFi(context, action)
                "bluetooth" -> handleBluetoothAction(context, action)
                "location services", "location service" -> toggleLocationService(context, action)
                "airplane mode" -> toggleAirplaneMode(context, action)
                "do not disturb mode", "don't disturb mode" -> toggleDoNotDisturbMode(context, action)
                else -> messageList.add(MessageModel("Unknown feature: $feature", "model"))
            }
        } catch (e: Exception) {
            Log.e("ChatViewModel", "Error handling control device feature: ${e.message}")
            messageList.add(MessageModel("Error handling device feature", "model"))
        }
    }

    private fun toggleWiFi(context: Context, action: String) {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        when (action.lowercase()) {
            "on" -> {
                if (wifiManager.wifiState != WifiManager.WIFI_STATE_ENABLED && wifiManager.wifiState != WifiManager.WIFI_STATE_ENABLING) {
                    val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    messageList.add(MessageModel("Opening WiFi Settings to turn ON", "model"))
                } else {
                    messageList.add(MessageModel("WiFi is already ON", "model"))
                }
            }
            "off" -> {
                if (wifiManager.wifiState != WifiManager.WIFI_STATE_DISABLED && wifiManager.wifiState != WifiManager.WIFI_STATE_DISABLING) {
                    val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    messageList.add(MessageModel("Opening WiFi Settings to turn OFF", "model"))
                } else {
                    messageList.add(MessageModel("WiFi is already OFF", "model"))
                }
            }
            "check" -> {
                when (wifiManager.wifiState) {
                    WifiManager.WIFI_STATE_ENABLED -> messageList.add(MessageModel("WiFi is ON", "model"))
                    WifiManager.WIFI_STATE_DISABLED -> messageList.add(MessageModel("WiFi is OFF", "model"))
                    else -> messageList.add(MessageModel("WiFi state is UNKNOWN", "model"))
                }
            }
            else -> messageList.add(MessageModel("Unknown action for WiFi", "model"))
        }
    }

    private fun handleBluetoothAction(context: Context, action: String) {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter == null) {
            messageList.add(MessageModel("Bluetooth not supported on this device", "model"))
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
        ) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
            messageList.add(MessageModel("Please grant Bluetooth permissions in the settings", "model"))
            return
        }

        when (action.lowercase()) {
            "on" -> {
                if (!bluetoothAdapter.isEnabled) {
                    val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    messageList.add(MessageModel("Requesting to turn Bluetooth ON", "model"))
                } else {
                    messageList.add(MessageModel("Bluetooth is already ON", "model"))
                }
            }
            "off" -> {
                if (bluetoothAdapter.isEnabled) {
                    val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    messageList.add(MessageModel("Please turn off Bluetooth manually in settings", "model"))
                } else {
                    messageList.add(MessageModel("Bluetooth is already OFF", "model"))
                }
            }
            "check" -> {
                messageList.add(MessageModel(if (bluetoothAdapter.isEnabled) "Bluetooth is ON" else "Bluetooth is OFF", "model"))
            }
            else -> messageList.add(MessageModel("Unknown action for Bluetooth", "model"))
        }
    }

    private fun toggleLocationService(context: Context, action: String) {
        when (action.lowercase()) {
            "on" -> {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                messageList.add(MessageModel("Opening Location Settings to turn ON", "model"))
            }
            "off" -> {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                messageList.add(MessageModel("Opening Location Settings to turn OFF", "model"))
            }
            "check" -> {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                messageList.add(MessageModel(if (isLocationEnabled) "Location is ON" else "Location is OFF", "model"))
            }
            else -> messageList.add(MessageModel("Unknown action for Location Service", "model"))
        }
    }

    private fun toggleAirplaneMode(context: Context, action: String) {
        val isAirplaneModeOn = Settings.Global.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0

        when (action.lowercase()) {
            "on" -> {
                if (!isAirplaneModeOn) {
                    val intent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    messageList.add(MessageModel("Please enable Airplane mode manually", "model"))
                } else {
                    messageList.add(MessageModel("Airplane mode is already ON", "model"))
                }
            }
            "off" -> {
                if (isAirplaneModeOn) {
                    val intent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    messageList.add(MessageModel("Please disable Airplane mode manually", "model"))
                } else {
                    messageList.add(MessageModel("Airplane mode is already OFF", "model"))
                }
            }
            "check" -> {
                messageList.add(MessageModel(if (isAirplaneModeOn) "Airplane mode is ON" else "Airplane mode is OFF", "model"))
            }
            else -> messageList.add(MessageModel("Unknown action for Airplane mode", "model"))
        }
    }

    private fun toggleDoNotDisturbMode(context: Context, action: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (!notificationManager.isNotificationPolicyAccessGranted) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            messageList.add(MessageModel("Please grant Do Not Disturb access", "model"))
            return
        }

        when (action.lowercase()) {
            "on" -> {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
                messageList.add(MessageModel("Do Not Disturb mode turned ON", "model"))
            }
            "off" -> {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                messageList.add(MessageModel("Do Not Disturb mode turned OFF", "model"))
            }
            "check" -> {
                val isDoNotDisturbOn = notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_NONE
                messageList.add(MessageModel(if (isDoNotDisturbOn) "Do Not Disturb mode is ON" else "Do Not Disturb mode is OFF", "model"))
            }
            else -> messageList.add(MessageModel("Unknown action for Do Not Disturb mode", "model"))
        }
    }
    //================================================================
    //================================================================--> Handle Weather
    private fun handleWeatherQuery(json: JsonObject) {
        val context = getApplication<Application>().applicationContext
        try {
            val entities = json.getAsJsonObject("entities")
            val traits = json.getAsJsonObject("traits")

            val location = entities.getAsJsonArray("wit\$location:location")?.get(0)?.asJsonObject?.get("resolved")?.asJsonObject
            val datetime = entities.getAsJsonArray("wit\$datetime:datetime")?.get(0)?.asJsonObject?.get("value")?.asString
            val forecastType = traits?.getAsJsonArray("forecast_type")?.get(0)?.asJsonObject?.get("value")?.asString ?: "current"

            if (location != null) {
                val locationName = location.getAsJsonArray("values")?.get(0)?.asJsonObject?.get("name")?.asString
                val coords = location.getAsJsonArray("values")?.get(0)?.asJsonObject?.get("coords")?.asJsonObject
                val lat = coords?.get("lat")?.asDouble
                val lon = coords?.get("long")?.asDouble

                if (lat != null && lon != null) {
                    weatherViewModel.fetchWeather(lat, lon, datetime, forecastType) { weatherResult ->
                        messageList.add(MessageModel(weatherResult, "model"))
                    }
                } else {
                    messageList.add(MessageModel("Unable to determine coordinates for $locationName", "model"))
                }
            } else {
                // If no location is specified, use the current location
                fetchCurrentLocationWeather(context, datetime, forecastType)
            }
        } catch (e: Exception) {
            Log.e("ChatViewModel", "Error handling weather query: ${e.message}")
            messageList.add(MessageModel("Error handling weather query", "model"))
        }
    }

    private fun fetchCurrentLocationWeather(context: Context, datetime: String?, forecastType: String) {
        weatherViewModel.fetchCurrentLocationWeather(context) { location ->
            if (location != null) {
                weatherViewModel.fetchWeather(location.latitude, location.longitude, datetime, forecastType) { weatherResult ->
                    messageList.add(MessageModel(weatherResult, "model"))
                }
            } else {
                messageList.add(MessageModel("Unable to determine current location", "model"))
            }
        }
    }


    private fun fallbackToGenerativeModel(prompt: String) {
        viewModelScope.launch {
            try {
                val chat = generativeModel.startChat(
                    history = messageList.map {
                        content(it.role) { text(it.prompt) }
                    }.toList()
                )

                val response = chat.sendMessage(prompt)
                messageList.removeAt(messageList.lastIndex)
                messageList.add(MessageModel(response.text.toString().trimEnd('\n'), "model"))
                Log.d("ChatViewModel", "Response: ${response.text}")
            } catch (e: Exception) {
                messageList.removeAt(messageList.lastIndex)
                messageList.add(MessageModel("Sorry, something went wrong.", "model"))
                Log.e("ChatViewModel", "Error sending message: ${e.message}")
            }
        }
    }

}
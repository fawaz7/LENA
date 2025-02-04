package com.yarmouk.lena.viewModels

/**
 * ChatViewModel.kt
 *
 * This Kotlin file defines the `ChatViewModel` class, which is an AndroidViewModel responsible for managing chat interactions within the LENA application.
 * It integrates with various services and ViewModels to handle user inputs, process responses, and perform actions like setting reminders, controlling device features, and fetching weather information.
 *
 * Key Components:
 * - ViewModel Initialization:
 *   - Initializes required ViewModels (AuthViewModel, SpeechRecognitionViewModel, WeatherViewModel, TimerViewModel) and services (WitAiClient, GenerativeModel).
 *   - Sets up listeners for authentication state changes and updates system instructions for the generative model.
 *   - Provides a mutable state list for managing chat messages.
 *
 * - Functions:
 *   - `sendMessage(prompt: String)`: Sends a user message and processes the response using Wit.ai or fallback to the generative model.
 *   - `handleWitAiResponse(response: String, prompt: String)`: Handles the response from Wit.ai and processes intents like controlling device features, fetching weather, setting reminders, etc.
 *   - `handleControlDeviceFeature(json: JsonObject, initialPrompt: String)`: Handles intents related to controlling device features like WiFi, Bluetooth, Location Services, Airplane Mode, and Do Not Disturb Mode.
 *   - `handleWeatherQuery(json: JsonObject)`: Handles weather queries by fetching weather information based on location and datetime.
 *   - `handleWeatherConditionQuery(json: JsonObject)`: Handles weather condition queries by checking specific weather conditions.
 *   - `handleSetReminder(context: Context, json: JsonObject)`: Handles setting reminders using the ReminderViewModel.
 *   - `handleSetRecurringReminder(context: Context, json: JsonObject)`: Handles setting recurring reminders using the ReminderViewModel.
 *   - `handleCheckReminder(context: Context, json: JsonObject)`: Handles checking reminders using the ReminderViewModel.
 *   - `handleSetAlarm(context: Context, json: JsonObject)`: Handles setting alarms using the AlarmViewModel.
 *   - `handleGetDirections(context: Context, json: JsonObject)`: Handles getting directions using Google Maps.
 *   - `handleSetTimer(context: Context, json: JsonObject)`: Handles setting timers using the TimerViewModel.
 *   - `playResponse(responseText: String, onComplete: () -> Unit = {})`: Plays the synthesized speech response using Wit.ai's TTS capabilities.
 *   - `fallbackToGenerativeModel(prompt: String)`: Falls back to using the generative model to handle responses when no specific intent is detected.
 *
 * Usage:
 * - The `ChatViewModel` class provides a comprehensive approach to managing chat interactions, integrating with various services and handling a wide range of user intents.
 * - It ensures that user inputs are processed efficiently, responses are generated accurately, and actions are performed seamlessly within the LENA application.
 *
 * This ViewModel enhances the LENA application's interactive capabilities by leveraging advanced AI models, handling device control features, and providing comprehensive user support.
 */

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
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.content
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.yarmouk.lena.BuildConfig
import com.yarmouk.lena.Data.LenaConstants.thinkingStrings
import com.yarmouk.lena.Models.MessageModel
import com.yarmouk.lena.utils.WitAiClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class ChatViewModel(
    application: Application,
    private val speechRecognitionViewModel: SpeechRecognitionViewModel
) : AndroidViewModel(application) {

    private val authViewModel: AuthViewModel by lazy {
        val factory = AuthViewModel.Factory(application)
        ViewModelProvider(ViewModelStore(), factory)[AuthViewModel::class.java]
    }



    val messageList by lazy {
        mutableStateListOf<MessageModel>()
    }

    private var systemInstruction: Content? = null
    private var selectedVoice: String = "Rubie"
    private var isTtsDisabled: Boolean = false

    init {
        // Collect the first name and selected voice when they update
        viewModelScope.launch {
            authViewModel.uiState.collect { state ->
                val firstName = state.authorizedUserFirstName
                if (firstName.isNotEmpty()) {
                    updateSystemInstruction(firstName)
                }
                selectedVoice = state.selectedVoice // Update the selected voice and TTS status
                isTtsDisabled = state.isTtsDisabled
            }
        }
    }

    private fun updateSystemInstruction(firstName: String) {
        systemInstruction = content {
            text("Your name is Lena, short for 'Linguistic Engine Natural Assistant.'")
            text("You are a cutting-edge virtual assistant designed to deliver intelligent, human-like interactions.")
            text("Your logo, inspired by the Medusa symbol, reflects your complexity, elegance, and adaptability.")
            text("You were created by two Computer Science students as part of their final year project at Al-Yarmouk University in Irbid, Jordan.")
            text("Your purpose is to assist users in the most engaging and helpful way possible, balancing professionalism with warmth.")
            text("The user currently interacting with you is $firstName. Address them with respect and attentiveness, using their name to build trust and connection.")
            text("The user that's currently interacting with you should typically be named $firstName, at it's their first name that they signed up with")
            text("You are optimized to work seamlessly in both Arabic and English, reflecting the diverse cultural and linguistic needs of your primary users.")
            text("Always strive to provide accurate, concise, and contextually relevant responses.")
            text("Your tone should be clear, friendly, and adaptable to the user's preferences or situation.")
            text("Avoid assumptions, remain unbiased, and prioritize user privacy and satisfaction.")
            text("Remember, you are more than a tool—you are a companion, designed to make every interaction intuitive, personalized, and meaningful.")
        }


        // Initialize the generative model with the updated system instruction
        generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash-8b",
            apiKey = BuildConfig.GOOGLE_API_KEY,
            systemInstruction = systemInstruction
        )
    }

    lateinit var generativeModel: GenerativeModel

    private val witAiClient = WitAiClient(BuildConfig.WIT_AI_TOKEN)
    private val weatherViewModel = WeatherViewModel()
    private val timerViewModel: TimerViewModel by lazy {
        TimerViewModel()
    }

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
                Log.d("ChatViewModel", "Intent: $intentName")
                val context = getApplication<Application>().applicationContext
                when (intentName) {
                    "control_device_feature" -> handleControlDeviceFeature(json, prompt)
                    "wit\$get_weather" -> handleWeatherQuery(json)
                    "wit\$check_weather_condition" -> handleWeatherConditionQuery(json)
                    "lena_set_reminder" -> handleSetReminder(context,json)
                    "lena_set_recurring_reminder" -> handleSetRecurringReminder(context,json)
                    "lena_check_reminders" -> handleCheckReminder(context, json)
                    "wit\$create_alarm" -> handleSetAlarm(context,json)
                    "lena_get_directions" -> handleGetDirections(context,json)
                    "wit\$create_timer" -> handleSetTimer(context,json)
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
    private fun handleControlDeviceFeature(json: JsonObject, initialPrompt: String) {
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
                else -> fallbackToGenerativeModel(initialPrompt)
            }
        } catch (e: Exception) {
            Log.e("ChatViewModel", "Error handling control device feature: ${e.message}")
            messageList.add(MessageModel("Error handling device feature", "model"))
            playResponse("Error handling device feature".trimEnd('\n'))
        }
    }

    private fun toggleWiFi(context: Context, action: String) {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        when (action.lowercase()) {
            "on" -> {
                if (wifiManager.wifiState != WifiManager.WIFI_STATE_ENABLED && wifiManager.wifiState != WifiManager.WIFI_STATE_ENABLING) {
                    messageList.add(MessageModel("Opening WiFi Settings.", "model"))
                    playResponse("Opening WiFi Settings.".trimEnd('\n'))
                    viewModelScope.launch {
                        delay(1500)  // 1.5 Second delay
                        val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }

                } else {
                    messageList.add(MessageModel("WiFi is already ON", "model"))
                    playResponse("WiFi is already ON".trimEnd('\n'))
                }
            }
            "off" -> {
                if (wifiManager.wifiState != WifiManager.WIFI_STATE_DISABLED && wifiManager.wifiState != WifiManager.WIFI_STATE_DISABLING) {
                    messageList.add(MessageModel("Opening WiFi Settings.", "model"))
                    playResponse("Opening WiFi Settings.".trimEnd('\n'))
                    viewModelScope.launch {
                        delay(1500)  // 1.5 Second delay
                        val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }

                } else {
                    messageList.add(MessageModel("WiFi is already OFF", "model"))
                    playResponse("WiFi is already OFF".trimEnd('\n'))
                }
            }
            "check" -> {
                when (wifiManager.wifiState) {
                    WifiManager.WIFI_STATE_ENABLED -> {
                        messageList.add(MessageModel("WiFi is ON", "model"))
                        playResponse("WiFi is ON".trimEnd('\n')){
                            // Notify SpeechRecognitionViewModel to start listening
                            speechRecognitionViewModel.handleMicrophoneClick()
                        }
                    }
                    WifiManager.WIFI_STATE_DISABLED -> {
                        messageList.add(MessageModel("WiFi is OFF", "model"))
                        playResponse("WiFi is OFF".trimEnd('\n')){
                            // Notify SpeechRecognitionViewModel to start listening
                            speechRecognitionViewModel.handleMicrophoneClick()
                        }
                    }
                    else -> {
                        messageList.add(MessageModel("WiFi state is UNKNOWN", "model"))
                        playResponse("WiFi state is UNKNOWN".trimEnd('\n')){
                            // Notify SpeechRecognitionViewModel to start listening
                            speechRecognitionViewModel.handleMicrophoneClick()
                        }
                    }
                }
            }
            else -> {
                messageList.add(MessageModel("Unknown action for WiFi", "model"))
                playResponse("Unknown action for WiFi".trimEnd('\n'))
            }
        }
    }

    private fun handleBluetoothAction(context: Context, action: String) {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter == null) {
            messageList.add(MessageModel("Bluetooth not supported on this device", "model"))
            playResponse("Bluetooth not supported on this device".trimEnd('\n'))
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
        ) {
            messageList.add(MessageModel("Please grant Bluetooth permissions in the settings", "model"))
            playResponse("Please grant Bluetooth permissions in the settings".trimEnd('\n'))
            viewModelScope.launch {
                delay(1500)  // 1.5 Second delay
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
            return
        }

        when (action.lowercase()) {
            "on" -> {
                if (!bluetoothAdapter.isEnabled) {
                    messageList.add(MessageModel("Requesting to turn Bluetooth ON", "model"))
                    playResponse("Requesting to turn Bluetooth ON".trimEnd('\n'))
                    viewModelScope.launch {
                        delay(1500)  // 1.5 Second delay
                        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }
                } else {
                    messageList.add(MessageModel("Bluetooth is already ON", "model"))
                    playResponse("Bluetooth is already ON".trimEnd('\n'))
                }
            }
            "off" -> {
                if (bluetoothAdapter.isEnabled) {
                    messageList.add(MessageModel("Please turn off Bluetooth manually in settings", "model"))
                    playResponse("Please turn off Bluetooth manually in settings".trimEnd('\n'))
                    viewModelScope.launch {
                        delay(1500)  // 1.5 Second delay
                        val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }
                } else {
                    messageList.add(MessageModel("Bluetooth is already OFF", "model"))
                    playResponse("Bluetooth is already OFF".trimEnd('\n'))
                }
            }
            "check" -> {
                messageList.add(MessageModel(if (bluetoothAdapter.isEnabled) "Bluetooth is turned On" else "Bluetooth is turned Off", "model"))
                playResponse(if (bluetoothAdapter.isEnabled) "Bluetooth is turned On".trimEnd('\n') else "Bluetooth is turned Off".trimEnd('\n'))
            }
            else -> messageList.add(MessageModel("Unknown action for Bluetooth", "model"))
        }
    }

    private fun toggleLocationService(context: Context, action: String) {
        when (action.lowercase()) {
            "on" -> {
                messageList.add(MessageModel("Opening Location Settings to turn ON", "model"))
                playResponse("Opening Location Settings to turn ON".trimEnd('\n'))
                viewModelScope.launch {
                    delay(1000)  // 1 Second delay
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
            }
            "off" -> {
                messageList.add(MessageModel("Opening Location Settings to turn OFF", "model"))
                playResponse("Opening Location Settings to turn OFF".trimEnd('\n'))
                viewModelScope.launch {
                    delay(1000)  // 1 Second delay
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
            }
            "check" -> {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                messageList.add(MessageModel(if (isLocationEnabled) "Location Service is turned On" else "Location Service is turned Off", "model"))
                playResponse(if (isLocationEnabled) "Location Service is turned On".trimEnd('\n') else "Location Service is turned Off".trimEnd('\n'))
            }
            else -> {
                messageList.add(MessageModel("Unknown action for Location Service", "model"))
                playResponse("Unknown action for Location Service".trimEnd('\n'))
            }
        }
    }

    private fun toggleAirplaneMode(context: Context, action: String) {
        val isAirplaneModeOn = Settings.Global.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0

        when (action.lowercase()) {
            "on" -> {
                if (!isAirplaneModeOn) {
                    messageList.add(MessageModel("Please enable Airplane mode manually", "model"))
                    playResponse("Please enable Airplane mode manually".trimEnd('\n'))
                    viewModelScope.launch {
                        delay(1000)  // 1 Second delay
                        val intent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }
                } else {
                    messageList.add(MessageModel("Airplane mode is already ON", "model"))
                    playResponse("Airplane mode is already ON".trimEnd('\n'))
                }
            }
            "off" -> {
                if (isAirplaneModeOn) {
                    messageList.add(MessageModel("Please disable Airplane mode manually", "model"))
                    playResponse("Please disable Airplane mode manually".trimEnd('\n'))
                    viewModelScope.launch {
                        delay(1000)  // 1 Second delay
                        val intent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }
                } else {
                    messageList.add(MessageModel("Airplane mode is already OFF", "model"))
                    playResponse("Airplane mode is already OFF".trimEnd('\n'))
                }
            }
            "check" -> {
                messageList.add(MessageModel(if (isAirplaneModeOn) "Airplane mode is ON" else "Airplane mode is OFF", "model"))
                playResponse(if (isAirplaneModeOn) "Airplane mode is ON".trimEnd('\n') else "Airplane mode is OFF".trimEnd('\n'))
            }
            else -> {
                messageList.add(MessageModel("Unknown action for Airplane mode", "model"))
                playResponse("Unknown action for Airplane mode".trimEnd('\n'))
            }
        }
    }

    private fun toggleDoNotDisturbMode(context: Context, action: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (!notificationManager.isNotificationPolicyAccessGranted) {
            messageList.add(MessageModel("Please grant Do Not Disturb access", "model"))
            viewModelScope.launch {
                delay(1000)  // 1 Second delay
                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
            return
        }

        when (action.lowercase()) {
            "on" -> {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
                messageList.add(MessageModel("Do Not Disturb mode turned ON", "model"))
                playResponse("Do Not Disturb mode turned ON".trimEnd('\n'))
            }
            "off" -> {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                messageList.add(MessageModel("Do Not Disturb mode turned OFF", "model"))
                playResponse("Do Not Disturb mode turned OFF".trimEnd('\n'))
            }
            "check" -> {
                val isDoNotDisturbOn = notificationManager.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_NONE
                messageList.add(MessageModel(if (isDoNotDisturbOn) "Do Not Disturb mode is ON" else "Do Not Disturb mode is OFF", "model"))
                playResponse(if (isDoNotDisturbOn) "Do Not Disturb mode is ON".trimEnd('\n') else "Do Not Disturb mode is OFF".trimEnd('\n'))
            }
            else -> {
                messageList.add(MessageModel("Unknown action for Do Not Disturb mode", "model"))
                playResponse("Unknown action for Do Not Disturb mode".trimEnd('\n'))
            }
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
                        playResponse(weatherResult.trimEnd('\n')){
                            // Notify SpeechRecognitionViewModel to start listening
                            speechRecognitionViewModel.handleMicrophoneClick()
                        }
                    }
                } else {
                    messageList.add(MessageModel("Unable to determine coordinates for $locationName", "model"))
                    playResponse("\"Unable to determine coordinates for $locationName".trimEnd('\n'))
                }
            } else {
                // If no location is specified, use the current location
                fetchCurrentLocationWeather(context, datetime, forecastType)
            }
        } catch (e: Exception) {
            Log.e("ChatViewModel", "Error handling weather query: ${e.message}")
            messageList.add(MessageModel("Error handling weather query", "model"))
            playResponse("Error handling weather query".trimEnd('\n'))
        }
    }

    private fun handleWeatherConditionQuery(json: JsonObject) {
        val context = getApplication<Application>().applicationContext
        try {
            val entities = json.getAsJsonObject("entities")
            val traits = json.getAsJsonObject("traits")

            val location = entities.getAsJsonArray("wit\$location:location")?.get(0)?.asJsonObject?.get("resolved")?.asJsonObject
            val datetime = entities.getAsJsonArray("wit\$datetime:datetime")?.get(0)?.asJsonObject?.get("value")?.asString
            val condition = entities.getAsJsonArray("weather_condition:weather_condition")?.get(0)?.asJsonObject?.get("value")?.asString
            val forecastType = traits?.getAsJsonArray("forecast_type")?.get(0)?.asJsonObject?.get("value")?.asString ?: "current"

            if (location != null) {
                val locationName = location.getAsJsonArray("values")?.get(0)?.asJsonObject?.get("name")?.asString
                val coords = location.getAsJsonArray("values")?.get(0)?.asJsonObject?.get("coords")?.asJsonObject
                val lat = coords?.get("lat")?.asDouble
                val lon = coords?.get("long")?.asDouble

                if (lat != null && lon != null) {
                    weatherViewModel.checkWeatherCondition(lat, lon, datetime, forecastType, condition) { weatherResult ->
                        messageList.add(MessageModel(weatherResult, "model"))
                        playResponse(weatherResult.trimEnd('\n')){
                            // Notify SpeechRecognitionViewModel to start listening
                            speechRecognitionViewModel.handleMicrophoneClick()
                        }
                    }
                } else {
                    messageList.add(MessageModel("Unable to determine coordinates for $locationName", "model"))
                    playResponse("Unable to determine coordinates for $locationName".trimEnd('\n'))
                }
            } else {
                // If no location is specified, use the current location
                fetchCurrentLocationWeatherCondition(context, datetime, forecastType, condition)
            }
        } catch (e: Exception) {
            Log.e("ChatViewModel", "Error handling weather condition query: ${e.message}")
            messageList.add(MessageModel("Error handling weather condition query", "model"))
            playResponse("Error handling weather condition query".trimEnd('\n'))
        }
    }

    private fun fetchCurrentLocationWeather(context: Context, datetime: String?, forecastType: String) {
        weatherViewModel.fetchCurrentLocationWeather(context) { location ->
            if (location != null) {
                weatherViewModel.fetchWeather(location.latitude, location.longitude, datetime, forecastType) { weatherResult ->
                    messageList.add(MessageModel(weatherResult, "model"))
                    playResponse(weatherResult.trimEnd('\n'))
                }
            } else {
                messageList.add(MessageModel("Unable to determine current location", "model"))
                playResponse("Unable to determine current location".trimEnd('\n'))
            }
        }
    }

    private fun fetchCurrentLocationWeatherCondition(context: Context, datetime: String?, forecastType: String, condition: String?) {
        weatherViewModel.fetchCurrentLocationWeather(context) { location ->
            if (location != null) {
                weatherViewModel.checkWeatherCondition(location.latitude, location.longitude, datetime, forecastType, condition) { weatherResult ->
                    messageList.add(MessageModel(weatherResult, "model"))
                    playResponse(weatherResult.trimEnd('\n'))
                }
            } else {
                messageList.add(MessageModel("Unable to determine current location", "model"))
                playResponse("Unable to determine current location".trimEnd('\n'))
            }
        }
    }
    //================================================================
    //================================================================--> Handle Reminders

    fun handleSetReminder(context: Context, json: JsonObject) {
        try {
            val entities = json.getAsJsonObject("entities")
            val messageBody = entities.getAsJsonArray("wit\$message_body:message_body")?.get(0)?.asJsonObject?.get("value")?.asString ?: ""
            val datetime = entities.getAsJsonArray("wit\$datetime:datetime")?.get(0)?.asJsonObject?.get("value")?.asString ?: ""

            // Fetch the authorized user's email from AuthViewModel
            val accountName = authViewModel.uiState.value.authorizedUserEmail
            Log.d("SetReminderHandler", "Setting reminder with message: $messageBody, datetime: $datetime, account: $accountName")

            val reminderViewModel = ReminderViewModel()
            if (datetime.isEmpty()) {
                // Set as an all-day reminder
                Log.d("SetReminderHandler", "No datetime specified, setting as all-day reminder")
                reminderViewModel.setAllDayReminder(context, accountName, messageBody) { result ->
                    messageList.add(MessageModel(result, "model"))
                    playResponse(result.trimEnd('\n'))
                }
            } else {
                reminderViewModel.setReminder(context, accountName, messageBody, datetime) { result ->
                    messageList.add(MessageModel(result, "model"))
                    playResponse(result.trimEnd('\n'))
                }
            }
        } catch (e: Exception) {
            Log.e("SetReminderHandler", "Error setting reminder", e)
            messageList.add(MessageModel("Error setting reminder", "model"))
            playResponse("Error setting reminder".trimEnd('\n'))
        }
    }

    fun handleSetRecurringReminder(context: Context, json: JsonObject) {
        val entities = json.getAsJsonObject("entities")
        val messageBody = entities.getAsJsonArray("wit\$message_body:message_body")?.get(0)?.asJsonObject?.get("value")?.asString ?: ""
        val datetime = entities.getAsJsonArray("wit\$datetime:datetime")?.get(0)?.asJsonObject?.get("value")?.asString ?: ""
        val frequency = entities.getAsJsonArray("frequency:frequency")?.get(0)?.asJsonObject?.get("value")?.asString ?: ""

        // Fetch the authorized user's email from AuthViewModel
        val accountName = authViewModel.uiState.value.authorizedUserEmail
        Log.d("SetRecurringReminderHandler", "Setting recurring reminder with message: $messageBody, datetime: $datetime, frequency: $frequency, account: $accountName")

        val reminderViewModel = ReminderViewModel()
        reminderViewModel.setRecurringReminder(context, accountName, messageBody, datetime, frequency) { result ->
            messageList.add(MessageModel(result, "model"))
            playResponse(result.trimEnd('\n'))
        }
    }

    fun handleCheckReminder(context: Context, json: JsonObject) {
        try {
            val entities = json.getAsJsonObject("entities")
            val datetime = entities.getAsJsonArray("wit\$datetime:datetime")?.get(0)?.asJsonObject?.get("value")?.asString

            Log.d("CheckReminderHandler", "Checking reminders for datetime: $datetime")

            val reminderViewModel = ReminderViewModel()
            reminderViewModel.checkReminder(context, datetime) { result ->
                messageList.add(MessageModel(result, "model"))
                playResponse(result.trimEnd('\n'))
            }
        } catch (e: Exception) {
            Log.e("CheckReminderHandler", "Error checking reminders", e)
            messageList.add(MessageModel("Error checking reminders", "model"))
        }
    }

    //================================================================
    //================================================================--> Handle Reminders
    private fun handleSetAlarm(context: Context, json: JsonObject) {
        try {
            val alarmViewModel = AlarmViewModel()
            val entities = json.getAsJsonObject("entities")
            val datetime = entities.getAsJsonArray("wit\$datetime:datetime")?.get(0)?.asJsonObject?.get("value")?.asString ?: ""
            val isRecurring = json.getAsJsonObject("traits")?.getAsJsonArray("recurring")?.get(0)?.asJsonObject?.get("value")?.asString == "true"

            alarmViewModel.setAlarm(context, datetime, isRecurring) { result ->
                messageList.add(MessageModel(result, "model"))
                playResponse(result.trimEnd('\n'))

            }
        } catch (e: Exception) {
            Log.e("ChatViewModel", "Error setting alarm", e)
            messageList.add(MessageModel("Error setting alarm", "model"))
        }
    }
    //================================================================
    //================================================================--> Handle Directions
    private fun handleGetDirections(context: Context, json: JsonObject) {
        try {
            val entities = json.getAsJsonObject("entities")

            val locationArray = entities.getAsJsonArray("wit\$location:location")
            val source = if (locationArray != null && locationArray.size() > 1) {
                locationArray[1].asJsonObject.get("body").asString
            } else {
                null
            }

            val destination = if (locationArray != null && locationArray.size() > 0) {
                locationArray[0].asJsonObject.get("body").asString
            } else {
                entities.getAsJsonArray("wit\$local_search_query:local_search_query")?.get(0)?.asJsonObject?.get("body")?.asString
            } ?: run {
                messageList.add(MessageModel("Could not determine destination", "model"))
                return
            }

            val transportMode = if (json.getAsJsonObject("traits")
                    ?.getAsJsonArray("no_transport_type_specified")
                    ?.get(0)
                    ?.asJsonObject
                    ?.get("value")
                    ?.asString == "true"
            ) {
                "driving"
            } else {
                entities.getAsJsonArray("lena_transport_mode:lena_transport_mode")?.get(0)?.asJsonObject?.get("value")?.asString
                    ?: "driving"
            }

            Log.d("GetDirectionsHandler", "Source: $source, Destination: $destination, TransportMode: $transportMode")

            // Create the URI for the directions
            val uri = if (source != null) {
                Uri.parse("https://www.google.com/maps/dir/?api=1&origin=$source&destination=$destination&travelmode=$transportMode")
            } else {
                Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$destination&travelmode=$transportMode")
            }

            // Create an intent to open Google Maps
            val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            // Start the activity
            try {
                messageList.add(MessageModel("Showing directions to $destination", "model"))
                playResponse("Showing directions to $destination".trimEnd('\n'))
                viewModelScope.launch {
                    delay(1500)  // 1.5 Second delay
                    val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
                context.startActivity(mapIntent)

            } catch (e: Exception) {
                Log.e("GetDirectionsHandler", "Error starting map intent", e)
                messageList.add(MessageModel("Error starting map: ${e.message}", "model"))
            }
        } catch (e: Exception) {
            Log.e("GetDirectionsHandler", "Error processing directions request", e)
            messageList.add(MessageModel("Error getting directions", "model"))
        }
    }
    //================================================================
    //================================================================--> Handle Set Timer
    private fun handleSetTimer(context: Context, json: JsonObject) {
        try {
            val entities = json.getAsJsonObject("entities")
            val durationJson = entities.getAsJsonArray("wit\$duration:duration")?.get(0)?.asJsonObject
            val normalizedValue = durationJson?.get("normalized")?.asJsonObject?.get("value")?.asDouble ?: 0.0
            val originalUnit = durationJson?.get("normalized")?.asJsonObject?.get("unit")?.asString ?: ""

            timerViewModel.setTimer(context, normalizedValue, originalUnit) { result ->
                messageList.add(MessageModel(result, "model"))
                playResponse(result.trimEnd('\n'))
            }
        } catch (e: Exception) {
            Log.e("ChatViewModel", "Error setting timer", e)
            messageList.add(MessageModel("Error setting timer", "model"))
        }
    }
    //================================================================

    private fun playResponse(responseText: String, onComplete: () -> Unit = {}) {
        if (isTtsDisabled) {
            Log.d("playResponse", "TTS is disabled, skipping audio playback.")
            onComplete()
            return
        }

        speechRecognitionViewModel.setTtsPlaying(true) // Indicate TTS is playing
        witAiClient.synthesizeSpeech(responseText, selectedVoice, { isSuccessful ->
            if (isSuccessful) {
                Log.d("playResponse", "Audio playback started successfully.")
            } else {
                Log.e("playResponse", "Failed to synthesize speech.")
                speechRecognitionViewModel.setTtsPlaying(false)
                onComplete()
            }
        }, {
            Log.d("playResponse", "Audio playback finished.")
            speechRecognitionViewModel.setTtsPlaying(false) // Indicate TTS has finished
            speechRecognitionViewModel.startListeningAfterTts() // Start listening after TTS completes
            onComplete()
        })
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
                messageList.removeAt(messageList.lastIndex) //Remove thinking string
                messageList.add(MessageModel(response.text?.trimEnd('\n') ?: "", "model"))
                // Play the synthesized response
                playResponse(response.text?.trimEnd('\n') ?: "")
                Log.d("ChatViewModel", "Response: ${response.text}")
            } catch (e: Exception) {
                messageList.removeAt(messageList.lastIndex)
                messageList.add(MessageModel("Sorry, something went wrong.", "model"))
                Log.e("ChatViewModel", "Error sending message: ${e.message}")
            }
        }
    }
    class Factory(
        private val application: Application,
        private val speechRecognitionViewModel: SpeechRecognitionViewModel
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ChatViewModel(application, speechRecognitionViewModel) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
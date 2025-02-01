package com.yarmouk.lena.viewModels

/**
 * AlarmViewModel.kt
 *
 * This Kotlin file defines the `AlarmViewModel` class, which is a ViewModel responsible for handling alarm-related functionalities within the LENA application.
 * It leverages Android's AlarmClock API to set alarms and uses a generative AI model for generating user-friendly confirmation messages.
 *
 * Key Components:
 * - `AlarmViewModel` Class:
 *   - Inherits from `ViewModel` and manages the lifecycle-aware components.
 *   - Initializes a `GenerativeModel` instance with API key and system instructions for generating responses.
 *
 * - Functions:
 *   - `setAlarm(context: Context, datetime: String, isRecurring: Boolean, onResult: (String) -> Unit)`:
 *     - Parses the provided datetime string and sets an alarm using the AlarmClock API.
 *     - Constructs an `Intent` to set the alarm with details like hour, minute, and message.
 *     - Checks if any app can handle the alarm intent and logs the available apps.
 *     - If successful, starts the activity to set the alarm and generates a confirmation message using the AI model.
 *     - Handles exceptions and provides error messages if the alarm setting fails.
 *
 *   - `processWithGemini(prompt: String)`:
 *     - A suspend function that sends a prompt to the generative AI model and returns the generated content.
 *     - Handles exceptions and logs errors if the content generation fails.
 *
 * Usage:
 * - The `AlarmViewModel` class provides an easy way to set alarms and generate dynamic confirmation messages.
 * - It ensures that alarms are set correctly and provides clear feedback to the user through the AI-generated messages.
 *
 * This ViewModel enhances the LENA application's capabilities by integrating alarm functionalities and leveraging generative AI for improved user interactions.
 */

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.AlarmClock
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yarmouk.lena.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class AlarmViewModel : ViewModel() {
    private val generativeModel: GenerativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash-8b",
        apiKey = BuildConfig.GOOGLE_API_KEY,
        systemInstruction = content {
            text("You are a helpful virtual assistant specialized in setting alarms")
            text("Your task is to process alarm requests and generate a user-friendly confirmation message.")
            text("Ensure the response is brief, clear, and dynamic, just like how a personal virtual assistant would respond.")
        }
    )

    fun setAlarm(context: Context, datetime: String, isRecurring: Boolean, onResult: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
                val alarmTime = dateFormat.parse(datetime) ?: throw IllegalArgumentException("Invalid datetime format")

                val calendar = Calendar.getInstance().apply {
                    time = alarmTime
                }
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)

                Log.d("AlarmViewModel", "Parsed datetime: $datetime, hour: $hour, minute: $minute, isRecurring: $isRecurring")

                val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                    putExtra(AlarmClock.EXTRA_HOUR, hour)
                    putExtra(AlarmClock.EXTRA_MINUTES, minute)
                    putExtra(AlarmClock.EXTRA_MESSAGE, "Alarm set by Lena")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    if (isRecurring) {
                        putExtra(AlarmClock.EXTRA_DAYS, arrayListOf(
                            Calendar.MONDAY,
                            Calendar.TUESDAY,
                            Calendar.WEDNESDAY,
                            Calendar.THURSDAY,
                            Calendar.FRIDAY,
                            Calendar.SATURDAY,
                            Calendar.SUNDAY
                        ))
                    }
                }

                // Debug: Check for apps that can handle the intent
                val resolvedActivities = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                if (resolvedActivities.isEmpty()) {
                    Log.e("AlarmViewModel", "No app available to handle the alarm intent")
                    onResult("No clock app found to set the alarm")
                    return@launch
                } else {
                    Log.d("AlarmViewModel", "Resolved apps: ${resolvedActivities.map { it.activityInfo.packageName }}")
                }

                // Start the activity
                context.startActivity(intent)
                val responsePrompt = if (isRecurring) {
                    "Set a recurring alarm for $hour:$minute every day."
                } else {
                    "Set an alarm for $hour:$minute."
                }
                val response = processWithGemini(responsePrompt)
                withContext(Dispatchers.Main) {
                    onResult(response)
                }
            } catch (e: Exception) {
                Log.e("AlarmViewModel", "Error setting alarm", e)
                withContext(Dispatchers.Main) {
                    onResult("Error setting alarm: ${e.message}")
                }
            }
        }
    }

    private suspend fun processWithGemini(prompt: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val response = generativeModel.generateContent(prompt)
                response.text ?: "Failed to process data with Gemini"
            } catch (e: Exception) {
                Log.e("AlarmViewModel", "Error generating content with Gemini: ${e.message}")
                "Error generating content with Gemini: ${e.message}"
            }
        }
    }
}
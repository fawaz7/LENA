package com.example.lena.viewModels

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.AlarmClock
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lena.BuildConfig
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
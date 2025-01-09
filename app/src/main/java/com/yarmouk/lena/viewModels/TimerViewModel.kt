package com.yarmouk.lena.viewModels

import android.content.Context
import android.content.Intent
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

class TimerViewModel : ViewModel() {
    private val generativeModel: GenerativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash-8b",
        apiKey = BuildConfig.GOOGLE_API_KEY,
        systemInstruction = content {
            text("You are a helpful virtual assistant specialized in setting timers using the clock app.")
            text("Your task is to process timer requests and generate a user-friendly confirmation message.")
            text("Ensure the response is brief, clear, and engaging, just like how a personal virtual assistant would respond.")
        }
    )

    fun setTimer(context: Context, duration: Double, unit: String, onResult: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val durationInSeconds = when (unit) {
                    "second" -> duration.toInt()
                    "minute" -> (duration * 60).toInt()
                    "hour" -> (duration * 3600).toInt()
                    else -> duration.toInt()
                }

                val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                    putExtra(AlarmClock.EXTRA_LENGTH, durationInSeconds)
                    putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)

                val result = processWithGemini("Set a timer for $duration $unit")
                withContext(Dispatchers.Main) {
                    onResult(result)
                }
            } catch (e: Exception) {
                Log.e("TimerViewModel", "Error setting timer", e)
                withContext(Dispatchers.Main) {
                    onResult("Error setting timer: ${e.message}")
                }
            }
        }
    }

    private suspend fun processWithGemini(prompt: String): String {
        return withContext(Dispatchers.IO) {
            try {
                // Use GenerativeModel to process the prompt
                val response = generativeModel.generateContent(prompt)
                response.text ?: "Failed to process data with Gemini"
            } catch (e: Exception) {
                Log.e("TimerViewModel", "Error generating content with Gemini: ${e.message}")
                "Error generating content with Gemini: ${e.message}"
            }
        }
    }
}
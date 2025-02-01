package com.yarmouk.lena.viewModels

/**
 * TimerViewModel.kt
 *
 * This Kotlin file defines the `TimerViewModel` class, which is a ViewModel responsible for managing timer-related functionalities within the LENA application.
 * It leverages Android's AlarmClock API to set timers and uses a generative AI model to generate user-friendly confirmation messages.
 *
 * Key Components:
 * - ViewModel Initialization:
 *   - Initializes a `GenerativeModel` instance with API key and system instructions for generating responses.
 *
 * - Functions:
 *   - `setTimer(context: Context, duration: Double, unit: String, onResult: (String) -> Unit)`:
 *     - Sets a timer for the specified duration and unit using the AlarmClock API.
 *     - Converts the duration to seconds based on the unit and constructs an intent to set the timer.
 *     - Generates a user-friendly confirmation message using the generative AI model.
 *
 * - Utility Functions:
 *   - `processWithGemini(prompt: String): String`:
 *     - A suspend function that processes the prompt using the generative AI model and returns the generated content.
 *     - Handles exceptions and logs errors if content generation fails.
 *
 * Usage:
 * - The `TimerViewModel` class provides a straightforward way to set timers and generate confirmation messages.
 * - It ensures that timers are set correctly and provides clear feedback to the user through AI-generated messages.
 *
 * This ViewModel enhances the LENA application's capabilities by integrating timer functionalities and leveraging generative AI for improved user interactions.
 */

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
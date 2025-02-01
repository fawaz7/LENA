package com.yarmouk.lena.viewModels

/**
 * ReminderViewModel.kt
 *
 * This Kotlin file defines the `ReminderViewModel` class, which is a ViewModel responsible for handling reminder-related functionalities within the LENA application.
 * It integrates with the Android Calendar API to set, check, and manage reminders, and uses a generative AI model to generate user-friendly messages.
 *
 * Key Components:
 * - ViewModel Initialization:
 *   - Initializes a `GenerativeModel` instance with API key and system instructions for generating responses.
 *
 * - Functions:
 *   - `setReminder(context: Context, accountName: String, title: String, datetime: String, onResult: (String) -> Unit)`:
 *     - Sets a reminder at the specified datetime using the Calendar API.
 *     - Requires WRITE_CALENDAR and READ_CALENDAR permissions.
 *     - Generates a user-friendly message using the generative AI model.
 *
 *   - `setAllDayReminder(context: Context, accountName: String, title: String, onResult: (String) -> Unit)`:
 *     - Sets an all-day reminder for the specified title.
 *     - Requires WRITE_CALENDAR and READ_CALENDAR permissions.
 *     - Generates a user-friendly message using the generative AI model.
 *
 *   - `setRecurringReminder(context: Context, accountName: String, title: String, datetime: String, frequency: String, onResult: (String) -> Unit)`:
 *     - Sets a recurring reminder at the specified datetime with the given frequency.
 *     - Requires WRITE_CALENDAR and READ_CALENDAR permissions.
 *     - Generates a user-friendly message using the generative AI model.
 *
 *   - `checkReminder(context: Context, datetime: String?, onResult: (String) -> Unit)`:
 *     - Checks for reminders on the specified date or the current date if no date is provided.
 *     - Requires READ_CALENDAR permission.
 *     - Generates a user-friendly message using the generative AI model.
 *
 *   - `getCalendarId(context: Context, accountName: String): Long?`:
 *     - Retrieves the calendar ID associated with the given account name.
 *     - Logs the calendar information and returns the first calendar ID that matches the account name.
 *
 *   - `processWithGemini(prompt: String): String`:
 *     - A suspend function that processes the prompt using the generative AI model and returns the generated content.
 *     - Handles exceptions and logs errors if content generation fails.
 *
 * Usage:
 * - The `ReminderViewModel` class provides comprehensive management of reminders, including setting single, all-day, and recurring reminders, as well as checking for existing reminders.
 * - It ensures that reminders are set correctly and provides clear feedback to the user through AI-generated messages.
 *
 * This ViewModel enhances the LENA application's capabilities by integrating with the Android Calendar API and leveraging generative AI for improved user interactions.
 */

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import android.util.Log
import androidx.core.content.ContextCompat
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

class ReminderViewModel : ViewModel() {
    private val generativeModel: GenerativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash-8b",
        apiKey = BuildConfig.GOOGLE_API_KEY,
        systemInstruction = content {
            text("You are a helpful virtual assistant specialized in setting and checking reminders using the calender API.")
            text("Your task is to process reminder requests (Setting Reminders or Checking the calender for any reminders) and generate a user-friendly message.")
            text("Ensure the response is brief, clear, and dynamic, just like how a personal virtual assistant would respond.")
            text("If no time or date is provided, mention that it's an all-day reminder.")
        }
    )

    fun setReminder(context: Context, accountName: String, title: String, datetime: String, onResult: (String) -> Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            Log.e("ReminderViewModel", "Calendar permissions not granted")
            onResult("Calendar permissions not granted")
            return
        }

        val calendarId = getCalendarId(context, accountName) ?: run {
            Log.e("ReminderViewModel", "No calendar found for account: $accountName")
            onResult("No calendar found for account: $accountName")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val calendar = Calendar.getInstance()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
                calendar.time = dateFormat.parse(datetime) ?: Date()

                Log.d("ReminderViewModel", "Setting reminder for: $title at $datetime")

                val values = ContentValues().apply {
                    put(CalendarContract.Events.DTSTART, calendar.timeInMillis)
                    put(CalendarContract.Events.DTEND, calendar.timeInMillis + 60 * 60 * 1000) // Add 1 hour
                    put(CalendarContract.Events.TITLE, title)
                    put(CalendarContract.Events.CALENDAR_ID, calendarId)
                    put(CalendarContract.Events.EVENT_TIMEZONE, calendar.timeZone.id)
                    put(CalendarContract.Events.DESCRIPTION, "Reminder set by Lena")
                    put(CalendarContract.Events.HAS_ALARM, 1) // Ensure event has alarm
                    put(CalendarContract.Events.STATUS, CalendarContract.Events.STATUS_CONFIRMED)
                    put(CalendarContract.Events.ORGANIZER, accountName) // Set organizer
                }

                Log.d("ReminderViewModel", "ContentValues: $values")

                val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
                if (uri != null) {
                    Log.d("ReminderViewModel", "Reminder set successfully with URI: $uri")
                    val result = processWithGemini("Set a reminder for $title at $datetime")
                    withContext(Dispatchers.Main) {
                        onResult(result)
                    }
                } else {
                    Log.w("ReminderViewModel", "Failed to set reminder")
                    withContext(Dispatchers.Main) {
                        onResult("Failed to set reminder")
                    }
                }
            } catch (e: Exception) {
                Log.e("ReminderViewModel", "Error setting reminder", e)
                withContext(Dispatchers.Main) {
                    onResult("Error setting reminder: ${e.message}")
                }
            }
        }
    }

    fun setAllDayReminder(context: Context, accountName: String, title: String, onResult: (String) -> Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            Log.e("ReminderViewModel", "WRITE_CALENDAR permission not granted")
            onResult("WRITE_CALENDAR permission not granted")
            return
        }

        val calendarId = getCalendarId(context, accountName) ?: run {
            Log.e("ReminderViewModel", "No calendar found for account: $accountName")
            onResult("No calendar found for account: $accountName")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                Log.d("ReminderViewModel", "Setting all-day reminder for: $title")

                val values = ContentValues().apply {
                    put(CalendarContract.Events.DTSTART, calendar.timeInMillis)
                    put(CalendarContract.Events.DTEND, calendar.timeInMillis + 24 * 60 * 60 * 1000) // All-day event
                    put(CalendarContract.Events.TITLE, title)
                    put(CalendarContract.Events.CALENDAR_ID, calendarId)
                    put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getTimeZone("UTC").id)
                    put(CalendarContract.Events.ALL_DAY, 1)
                    put(CalendarContract.Events.DESCRIPTION, "All-day reminder set by Lena")
                    put(CalendarContract.Events.HAS_ALARM, 1) // Ensure event has alarm
                    put(CalendarContract.Events.ORGANIZER, accountName) // Set organizer
                }

                val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
                val result = if (uri != null) {
                    Log.d("ReminderViewModel", "All-day reminder set successfully with URI: $uri")
                    processWithGemini("Set an all-day reminder for $title")
                } else {
                    Log.w("ReminderViewModel", "Failed to set all-day reminder")
                    "Failed to set all-day reminder"
                }

                withContext(Dispatchers.Main) {
                    onResult(result)
                }
            } catch (e: Exception) {
                Log.e("ReminderViewModel", "Error setting all-day reminder", e)
                withContext(Dispatchers.Main) {
                    onResult("Error setting all-day reminder: ${e.message}")
                }
            }
        }
    }

    fun setRecurringReminder(context: Context, accountName: String, title: String, datetime: String, frequency: String, onResult: (String) -> Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            Log.e("ReminderViewModel", "WRITE_CALENDAR permission not granted")
            onResult("WRITE_CALENDAR permission not granted")
            return
        }

        val calendarId = getCalendarId(context, accountName) ?: run {
            Log.e("ReminderViewModel", "No calendar found for account: $accountName")
            onResult("No calendar found for account: $accountName")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val calendar = Calendar.getInstance()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
                calendar.time = dateFormat.parse(datetime) ?: Date()

                Log.d("ReminderViewModel", "Setting recurring reminder for: $title at $datetime with frequency $frequency")

                val values = ContentValues().apply {
                    put(CalendarContract.Events.DTSTART, calendar.timeInMillis)
                    put(CalendarContract.Events.DTEND, calendar.timeInMillis + 60 * 60 * 1000) // Add 1 hour
                    put(CalendarContract.Events.TITLE, title)
                    put(CalendarContract.Events.CALENDAR_ID, calendarId)
                    put(CalendarContract.Events.EVENT_TIMEZONE, calendar.timeZone.id)
                    put(CalendarContract.Events.RRULE, "FREQ=${frequency.uppercase()};WKST=SU")
                    put(CalendarContract.Events.DESCRIPTION, "Recurring reminder set by Lena")
                    put(CalendarContract.Events.HAS_ALARM, 1) // Ensure event has alarm
                    put(CalendarContract.Events.ORGANIZER, accountName) // Set organizer
                }

                val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
                val result = if (uri != null) {
                    Log.d("ReminderViewModel", "Recurring reminder set successfully with URI: $uri")
                    processWithGemini("Set a recurring reminder for $title at $datetime with frequency $frequency")
                } else {
                    Log.w("ReminderViewModel", "Failed to set recurring reminder")
                    "Failed to set recurring reminder"
                }

                withContext(Dispatchers.Main) {
                    onResult(result)
                }
            } catch (e: Exception) {
                Log.e("ReminderViewModel", "Error setting recurring reminder", e)
                withContext(Dispatchers.Main) {
                    onResult("Error setting recurring reminder: ${e.message}")
                }
            }
        }
    }

    fun checkReminder(context: Context, datetime: String?, onResult: (String) -> Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            Log.e("ReminderViewModel", "Calendar permissions not granted")
            onResult("Calendar permissions not granted")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val calendar = Calendar.getInstance()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val startDate: Date
                val endDate: Date

                if (datetime != null) {
                    startDate = dateFormat.parse(datetime) ?: Date()
                    calendar.time = startDate
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                    endDate = calendar.time
                } else {
                    startDate = dateFormat.parse(dateFormat.format(Date())) ?: Date()
                    calendar.time = startDate
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                    endDate = calendar.time
                }

                val projection = arrayOf(
                    CalendarContract.Events.TITLE,
                    CalendarContract.Events.DTSTART
                )
                val selection = "(${CalendarContract.Events.DTSTART} >= ?) AND (${CalendarContract.Events.DTSTART} < ?)"
                val selectionArgs = arrayOf(startDate.time.toString(), endDate.time.toString())
                val cursor = context.contentResolver.query(
                    CalendarContract.Events.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    null
                )

                val reminders = mutableListOf<String>()
                cursor?.use {
                    while (it.moveToNext()) {
                        val title = it.getString(it.getColumnIndexOrThrow(CalendarContract.Events.TITLE))
                        reminders.add(title)
                    }
                }

                val result = if (reminders.isNotEmpty()) {
                    val reminderList = reminders.joinToString(separator = ", ")
                    processWithGemini("You have the following reminders: $reminderList")
                } else {
                    processWithGemini("You have no reminders.")
                }

                withContext(Dispatchers.Main) {
                    onResult(result)
                }
            } catch (e: Exception) {
                Log.e("ReminderViewModel", "Error checking reminders", e)
                withContext(Dispatchers.Main) {
                    onResult("Error checking reminders: ${e.message}")
                }
            }
        }
    }

    private fun getCalendarId(context: Context, accountName: String): Long? {
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_NAME
        )
        val selection = "${CalendarContract.Calendars.ACCOUNT_NAME} = ?"
        val selectionArgs = arrayOf(accountName)

        val cursor = context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                do {
                    val id = it.getLong(it.getColumnIndexOrThrow(CalendarContract.Calendars._ID))
                    val displayName = it.getString(it.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME))
                    val accountNameCursor = it.getString(it.getColumnIndexOrThrow(CalendarContract.Calendars.ACCOUNT_NAME))
                    Log.d("CalendarInfo", "ID: $id, Display Name: $displayName, Account Name: $accountNameCursor")
                    // Return the first calendar ID that matches the account name
                    return id
                } while (it.moveToNext())
            }
        }
        Log.e("ReminderViewModel", "No calendar ID found for account: $accountName")
        return null
    }

    private suspend fun processWithGemini(prompt: String): String {
        return withContext(Dispatchers.IO) {
            try {
                // Use GenerativeModel to process the prompt
                val response = generativeModel.generateContent(prompt)
                response.text ?: "Failed to process data with Gemini"
            } catch (e: Exception) {
                Log.e("ReminderViewModel", "Error generating content with Gemini: ${e.message}")
                "Error generating content with Gemini: ${e.message}"
            }
        }
    }
}
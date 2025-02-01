package com.yarmouk.lena.viewModels

/**
 * SpeechRecognitionViewModel.kt
 *
 * This Kotlin file defines the `SpeechRecognitionViewModel` class, which is an AndroidViewModel responsible for managing speech recognition functionalities within the LENA application.
 * It leverages Android's SpeechRecognizer API to handle voice input and integrates state management with LiveData and StateFlow.
 *
 * Key Components:
 * - ViewModel Initialization:
 *   - Initializes LiveData and StateFlow for managing the state of speech recognition, errors, and permissions.
 *   - Checks if speech recognition is available on the device and sets an error message if not.
 *
 * - Functions:
 *   - `checkAndInitializeSpeechRecognizer()`: Checks for audio recording permissions and initializes the SpeechRecognizer if permissions are granted.
 *   - `handleMicrophoneClick()`: Handles microphone button clicks by starting or stopping listening based on the current state and permissions.
 *   - `setupRecognitionListener()`: Sets up the recognition listener to handle various speech recognition events such as ready for speech, end of speech, and errors.
 *   - `startListening()`: Starts listening for speech input using the SpeechRecognizer.
 *   - `startListeningAfterTts()`: Starts listening for speech input after TTS playback has finished.
 *   - `stopListening()`: Stops the SpeechRecognizer from listening.
 *   - `onCleared()`: Cleans up the SpeechRecognizer when the ViewModel is cleared.
 *   - `onPermissionHandled()`: Resets the permission required state.
 *   - `setTtsPlaying(value: Boolean)`: Sets the TTS playing state to manage when to start listening.
 *   - `clearError()`: Resets the error state after displaying it.
 *
 * Usage:
 * - The `SpeechRecognitionViewModel` class provides a comprehensive approach to managing speech recognition, handling permissions, errors, and state transitions.
 * - It ensures that the app can listen for speech input, process it, and handle various edge cases such as missing permissions and errors.
 *
 * This ViewModel enhances the LENA application's capabilities by integrating speech recognition functionalities and providing efficient state management for a seamless user experience.
 */

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.os.Handler
import android.os.Looper

class SpeechRecognitionViewModel(application: Application) : AndroidViewModel(application) {
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening

    private val _spokenText = MutableLiveData<String>()
    val spokenText: LiveData<String> = _spokenText

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _needsPermission = MutableStateFlow(false)
    val needsPermission: StateFlow<Boolean> = _needsPermission

    private val _isTtsPlaying = MutableStateFlow(false)
    val isTtsPlaying: StateFlow<Boolean> = _isTtsPlaying

    private var speechRecognizer: SpeechRecognizer? = null

    init {
        if (!SpeechRecognizer.isRecognitionAvailable(application)) {
            _error.value = "Speech recognition is not available on this device"
        }
    }

    fun checkAndInitializeSpeechRecognizer() {
        if (speechRecognizer == null &&
            ContextCompat.checkSelfPermission(
                getApplication(),
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplication())
            setupRecognitionListener()
        }
    }

    fun handleMicrophoneClick() {
        when {
            ContextCompat.checkSelfPermission(
                getApplication(),
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED -> {
                _needsPermission.value = true
            }
            isListening.value -> {
                stopListening()
            }
            else -> {
                checkAndInitializeSpeechRecognizer()
                startListening()
            }
        }
    }

    private fun setupRecognitionListener() {
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _isListening.value = true
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _isListening.value = false
            }

            override fun onError(error: Int) {
                _isListening.value = false
                _error.value = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Please grant audio recording permissions"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service is busy"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                    else -> "Unknown error occurred"
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    _spokenText.value = matches[0]
                }
                _isListening.value = false
            }

            override fun onPartialResults(partialResults: Bundle?) {}

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        speechRecognizer?.startListening(intent)
    }

    fun startListeningAfterTts() {
        // Ensure this runs on the main thread
        Handler(Looper.getMainLooper()).post {
            if (!_isTtsPlaying.value) {
                Log.d("SpeechRecognitionViewModel", "Starting listening after TTS.")
                startListening()
            } else {
                Log.d("SpeechRecognitionViewModel", "TTS is still playing, will not start listening.")
            }
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        _isListening.value = false
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    fun setTtsPlaying(value: Boolean) {
        _isTtsPlaying.value = value
    }

    // Call this to reset the error after displaying it
    fun clearError() {
        _error.value = null
    }
}
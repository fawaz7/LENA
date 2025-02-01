package com.yarmouk.lena.viewModels

/**
 * VoiceViewModel.kt
 *
 * This Kotlin file defines the `VoiceViewModel` class, which is a ViewModel responsible for managing voice-related functionalities within the LENA application.
 * It integrates with the Wit.ai client to fetch available voices and allows users to select a voice for text-to-speech.
 *
 * Key Components:
 * - ViewModel Initialization:
 *   - Initializes `mutableStateListOf` for available voices and `mutableStateOf` for the selected voice.
 *   - Fetches available voices from Wit.ai and updates the state.
 *
 * - Functions:
 *   - `fetchVoices()`: Fetches available voices from Wit.ai and updates the `availableVoices` list.
 *   - `changeSelectedVoice(voiceDisplayName: String)`: Changes the selected voice based on the display name.
 *
 * - Factory Class:
 *   - `Factory(private val witAiClient: WitAiClient)`: Provides a way to create an instance of `VoiceViewModel` with the required `witAiClient`.
 *
 * Usage:
 * - The `VoiceViewModel` class provides a way to manage voice selection and availability for text-to-speech functionalities.
 * - It ensures that available voices are fetched and updated, and allows users to change the selected voice.
 *
 * This ViewModel enhances the LENA application's voice capabilities by integrating with Wit.ai and providing efficient state management for voice selection.
 */

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yarmouk.lena.Data.Voices
import com.yarmouk.lena.utils.WitAiClient
import kotlinx.coroutines.launch

class VoiceViewModel(private val witAiClient: WitAiClient) : ViewModel() {

    val availableVoices = mutableStateListOf<String>()
    val selectedVoice = mutableStateOf<String?>(Voices.allVoices.find { it.displayName == "Rubie" }?.name)

    init {
        fetchVoices()
    }

    private fun fetchVoices() {
        viewModelScope.launch {
            witAiClient.fetchAvailableVoices { voices ->
                availableVoices.clear()
                availableVoices.addAll(Voices.allVoices.map { it.displayName })
            }
        }
    }

    fun changeSelectedVoice(voiceDisplayName: String) {
        selectedVoice.value = Voices.allVoices.find { it.displayName == voiceDisplayName }?.name
    }
}
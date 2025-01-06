package com.example.lena.viewModels

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lena.Data.Voices
import com.example.lena.utils.WitAiClient
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.IOException

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
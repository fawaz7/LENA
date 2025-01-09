package com.yarmouk.lena.viewModels

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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

    class Factory(private val witAiClient: WitAiClient) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(VoiceViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return VoiceViewModel(witAiClient) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
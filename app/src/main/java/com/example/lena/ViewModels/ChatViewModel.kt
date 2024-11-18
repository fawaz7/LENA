package com.example.lena.ViewModels

import android.util.Log
import androidx.lifecycle.ViewModel

class ChatViewModel : ViewModel() {

    fun sendMessage(message: String) {
        Log.i("ChatViewModel", "Sending message: $message")
    }

}
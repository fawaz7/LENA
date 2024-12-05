package com.example.lena.viewModels

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lena.BuildConfig
import com.example.lena.Data.LenaConstants.thinkingStrings
import com.example.lena.Models.MessageModel
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    val messageList by lazy {
        mutableStateListOf<MessageModel>()
    }

    val systemInstruction = content {
        text("Your name is Lena. its a short for 'Linguistic Engine Natural Assistant'.")
        text("Your logo is based on the Medusa Logo.")
        text("You're designed as a virtual assistant (Similar to Siri, Alexa, etc), but you're way more superior than all of them")
        text("You're made by 2 Computer Science Students as their final year project")
    }

    val generativeModel : GenerativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash-8b",
        apiKey = BuildConfig.GOOGLE_API_KEY,
        systemInstruction = systemInstruction
    )

    fun sendMessage(prompt: String) {
        viewModelScope.launch {
            try {
                val randomThinkingString = thinkingStrings.random()

                val chat = generativeModel.startChat(
                    history = messageList.map {
                        content(it.role){text(it.prompt)}
                    }.toList()
                )

                messageList.add(MessageModel(prompt.trimEnd(' '), "user"))
                messageList.add(MessageModel(randomThinkingString, "model"))

                val response = chat.sendMessage(prompt)
                messageList.removeAt(messageList.lastIndex)

                messageList.add(MessageModel(response.text.toString().trimEnd('\n'), "model"))
                Log.d("ChatViewModel", "Response: ${response.text}")
            } catch (e: Exception) {
                messageList.removeAt(messageList.lastIndex)
                messageList.add(MessageModel("Sorry, something went wrong.", "model"))
                Log.e("ChatViewModel", "Error sending message: ${e.message}")
            }
        }
    }
}
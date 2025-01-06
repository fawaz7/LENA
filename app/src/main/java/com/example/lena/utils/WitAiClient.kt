package com.example.lena.utils

import android.net.Uri
import okhttp3.*
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class WitAiClient(private val accessToken: String) {

    private val client = OkHttpClient()
    private val gson = Gson()

    fun sendMessage(message: String, callback: (String) -> Unit) {
        val url = "https://api.wit.ai/message?v=20230401&q=${Uri.encode(message)}"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                callback("Error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    callback("Error: ${response.message}")
                    return
                }

                val responseBody = response.body?.string()
                val responseJson = gson.fromJson(responseBody, Map::class.java)
                callback(gson.toJson(responseJson))
            }
        })
    }

    fun fetchAvailableVoices(callback: (List<String>) -> Unit) {
        val url = "https://api.wit.ai/voices?v=20240304"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                callback(emptyList())
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    callback(emptyList())
                    return
                }

                val responseBody = response.body?.string()
                val responseJson = gson.fromJson(responseBody, Map::class.java)
                val voices = mutableListOf<String>()
                responseJson.forEach { (_, value) ->
                    if (value is List<*>) {
                        value.forEach { voice ->
                            if (voice is Map<*, *>) {
                                voices.add(voice["name"] as String)
                            }
                        }
                    }
                }
                callback(voices)
            }
        })
    }

    // Synthesize speech
    fun synthesizeSpeech(text: String, voice: String, callback: (ByteArray?) -> Unit) {
        val url = "https://api.wit.ai/synthesize?v=20240304"
        val body = gson.toJson(mapOf(
            "q" to text,
            "voice" to voice,
            "speed" to 110,
            "pitch" to 100
        ))
        val request = Request.Builder()
            .url(url)
            .post(body.toRequestBody("application/json".toMediaTypeOrNull()))
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("Accept", "audio/wav")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    callback(null)
                    return
                }

                val audioData = response.body?.bytes()
                callback(audioData)
            }
        })
    }
}

package com.example.lena.utils

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.net.Uri
import android.util.Log
import okhttp3.*
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okio.BufferedSink
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
    fun synthesizeSpeech(text: String, voice: String, callback: (Boolean) -> Unit) {
        val url = "https://api.wit.ai/synthesize?v=20240304"
        val speed = 125
        val pitch = 130

        val jsonBody = """
        {
            "q": "$text",
            "voice": "$voice",
            "speed": $speed,
            "pitch": $pitch
        }
    """.trimIndent()

        // Create RequestBody using the current recommended approach
        val requestBody = object : RequestBody() {
            override fun contentType(): MediaType? {
                return "application/json".toMediaType() // Using Kotlin extension function
            }

            override fun writeTo(sink: BufferedSink) {
                sink.writeUtf8(jsonBody)
            }
        }

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("Accept", "audio/pcm16")
            .build()

        Log.d("synthesizeSpeech", "Request URL: $url")
        Log.d("synthesizeSpeech", "Request Body: $jsonBody")
        Log.d("synthesizeSpeech", "Request Headers: ${request.headers}")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                Log.e("synthesizeSpeech", "Request failed: ${e.message}")
                callback(false)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    Log.d("synthesizeSpeech", "Response code: ${response.code}")

                    if (!response.isSuccessful) {
                        val errorBody = response.body?.string()
                        Log.e("synthesizeSpeech", "Request unsuccessful: ${response.code}")
                        Log.e("synthesizeSpeech", "Error body: $errorBody")
                        callback(false)
                        return
                    }

                    val audioData = response.body?.bytes()
                    if (audioData == null || audioData.isEmpty()) {
                        Log.e("synthesizeSpeech", "Received empty audio data")
                        callback(false)
                        return
                    }

                    Log.d("synthesizeSpeech", "Audio data received: ${audioData.size} bytes")

                    playAudio(audioData)
                    callback(true)

                } catch (e: Exception) {
                    Log.e("synthesizeSpeech", "Error processing response: ${e.message}")
                    e.printStackTrace()
                    callback(false)
                }
            }
        })
    }

    fun playAudio(audioData: ByteArray) {
        try {
            // Specify the audio format
            val sampleRate = 22050  // Ensure this matches the sample rate used by the API
            val channelConfig = AudioFormat.CHANNEL_OUT_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT

            // Create an AudioTrack instance
            val minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            val audioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate,
                channelConfig,
                audioFormat,
                minBufferSize,
                AudioTrack.MODE_STREAM
            )

            // Play the audio data
            audioTrack.play()
            audioTrack.write(audioData, 0, audioData.size)
            audioTrack.stop()
            audioTrack.release()
        } catch (e: Exception) {
            Log.e("playAudio", "Error playing audio: ${e.message}")
            e.printStackTrace()
        }
    }


}

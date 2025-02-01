package com.yarmouk.lena.utils

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okio.BufferedSink
import java.io.IOException

/**
 * WitAiClient.kt
 *
 * This Kotlin class, `WitAiClient`, is a utility for interacting with the Wit.ai API to handle natural language processing and text-to-speech functionalities within the LENA application.
 * It uses OkHttp for network requests and Gson for JSON parsing.
 *
 * Key Components:
 * - Constructor:
 *   - `WitAiClient(accessToken: String)`: Initializes the client with the provided Wit.ai access token.
 *
 * - Functions:
 *   - `sendMessage(message: String, callback: (String) -> Unit)`:
 *     - Sends a message to the Wit.ai API and invokes the callback with the response.
 *     - Constructs the request URL and headers, and handles responses or failures.
 *
 *   - `fetchAvailableVoices(callback: (List<String>) -> Unit)`:
 *     - Fetches the available voices from the Wit.ai API and invokes the callback with a list of voice names.
 *     - Parses the JSON response to extract voice names.
 *
 *   - `synthesizeSpeech(text: String, voice: String, callback: (Boolean) -> Unit, onPlaybackComplete: () -> Unit)`:
 *     - Synthesizes speech from text using a specified voice and plays the audio.
 *     - Constructs the request body with JSON data, sends the request, and handles the response.
 *     - Uses the `playAudio` function to play the received audio data.
 *
 *   - `playAudio(audioData: ByteArray, onPlaybackComplete: () -> Unit)`:
 *     - Plays the provided audio data using `AudioTrack`.
 *     - Configures the audio format and buffer size, plays the audio, and invokes the callback upon completion.
 *
 * Usage:
 * - The `WitAiClient` class provides methods to send messages to Wit.ai for natural language understanding, fetch available voices for text-to-speech, and synthesize speech from text.
 * - The synthesized speech is played using the `AudioTrack` class, ensuring smooth playback of audio responses.
 *
 * This utility class enhances the LENA application's capabilities by integrating with the Wit.ai API for advanced natural language processing and text-to-speech functionalities.
 */

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
                Log.d("WitAiClient", "Response JSON: $responseJson")
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
    fun synthesizeSpeech(text: String, voice: String, callback: (Boolean) -> Unit, onPlaybackComplete: () -> Unit) {
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
                    if (!response.isSuccessful) {
                        callback(false)
                        return
                    }

                    val audioData = response.body?.bytes()
                    if (audioData == null || audioData.isEmpty()) {
                        callback(false)
                        return
                    }

                    playAudio(audioData) {
                        onPlaybackComplete() // Restart mic input after TTS completes
                    }
                    callback(true)

                } catch (e: Exception) {
                    Log.e("synthesizeSpeech", "Error processing response: ${e.message}")
                    e.printStackTrace()
                    callback(false)
                }
            }
        })
    }

    fun playAudio(audioData: ByteArray, onPlaybackComplete: () -> Unit) {
        try {
            val sampleRate = 22050
            val channelConfig = AudioFormat.CHANNEL_OUT_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT

            val minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            val audioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate,
                channelConfig,
                audioFormat,
                minBufferSize,
                AudioTrack.MODE_STREAM
            )

            audioTrack.play()
            audioTrack.write(audioData, 0, audioData.size)
            audioTrack.stop()
            audioTrack.release()

            Log.d("playAudio", "Playback completed.")
            onPlaybackComplete() // Invoke the callback to restart mic listening
        } catch (e: Exception) {
            Log.e("playAudio", "Error playing audio: ${e.message}")
            e.printStackTrace()
            onPlaybackComplete() // Ensure the callback is called even if there's an error
        }
    }
}

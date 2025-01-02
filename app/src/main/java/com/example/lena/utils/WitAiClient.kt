package com.example.lena.utils

import android.net.Uri
import okhttp3.*
import com.google.gson.Gson
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
}
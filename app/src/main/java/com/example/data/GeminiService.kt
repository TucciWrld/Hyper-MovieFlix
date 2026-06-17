package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com"
    private const val MODEL_NAME = "gemini-3.5-flash"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    /**
     * Sends a prompt to gemini-3.5-flash and returns the generated text response.
     */
    suspend fun getAIResponse(prompt: String, systemInstruction: String? = null): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API Key is not set or is using placeholder!")
            return@withContext "API_KEY_MISSING"
        }

        // Clean prompt for JSON formatting or escape special characters
        val escapedPrompt = prompt.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r")
        val systemPart = if (systemInstruction != null) {
            val escapedInstr = systemInstruction.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r")
            """
            "systemInstruction": {
                "parts": [{"text": "$escapedInstr"}]
            },
            """.trimIndent()
        } else ""

        val jsonRequest = """
            {
                $systemPart
                "contents": [
                    {
                        "parts": [
                            {"text": "$escapedPrompt"}
                        ]
                    }
                ]
            }
        """.trimIndent()

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonRequest.toRequestBody(mediaType)

        val url = "$BASE_URL/v1beta/models/$MODEL_NAME:generateContent?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            val response = client.newCall(request).execute()
            val bodyString = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                Log.e(TAG, "API call failed with response code ${response.code}: $bodyString")
                return@withContext "Error calling AI services (Code: ${response.code})"
            }

            // Extract the generated text from response candidate. Let's do simple manual or regex extraction for maximum reliability.
            val keyword = "\"text\":"
            val index = bodyString.indexOf(keyword)
            if (index != -1) {
                val start = bodyString.indexOf("\"", index + keyword.length) + 1
                // Find matching closing quote, respecting escaped quotes inside
                var end = start
                var found = false
                while (end < bodyString.length) {
                    if (bodyString[end] == '"' && bodyString[end - 1] != '\\') {
                        found = true
                        break
                    }
                    end++
                }
                if (found) {
                    val rawText = bodyString.substring(start, end)
                    // Unescape essential JSON sequences
                    val parsedText = rawText
                        .replace("\\n", "\n")
                        .replace("\\t", "\t")
                        .replace("\\\"", "\"")
                        .replace("\\\\", "\\")
                    return@withContext parsedText
                }
            }

            Log.w(TAG, "Failed to parse text from Gemini response: $bodyString")
            return@withContext "Hyper AI: I analyzed your request but couldn't assemble a movie recommendation right now. Please try again or select another category!"
        } catch (e: Exception) {
            Log.e(TAG, "Exception during API call", e)
            return@withContext "Error: ${e.localizedMessage ?: "Failed to reach Hyper AI servers. Verify internet connection."}"
        }
    }
}

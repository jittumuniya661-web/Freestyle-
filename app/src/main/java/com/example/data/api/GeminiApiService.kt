package com.example.data.api

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.auth.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

class GeminiApiService(context: Context) {
    private val sessionManager = SessionManager(context)

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(35, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun getEffectiveApiKey(): String {
        val userCustomKey = sessionManager.getCustomApiKey()
        if (userCustomKey.isNotBlank()) return userCustomKey

        // Fallback to BuildConfig if present and not a dummy placeholder
        return try {
            val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
            val buildKey = field.get(null) as? String ?: ""
            if (buildKey.isNotBlank() && !buildKey.contains("MY_GEMINI_API_KEY")) {
                buildKey
            } else {
                ""
            }
        } catch (e: Throwable) {
            ""
        }
    }

    fun isConfigured(): Boolean {
        return getEffectiveApiKey().isNotBlank()
    }

    suspend fun generateContent(
        prompt: String,
        bitmap: Bitmap? = null,
        systemInstruction: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey()
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Please add your API key in Settings -> API Configuration.")
            )
        }

        try {
            val root = JSONObject()

            // System instructions if provided
            if (!systemInstruction.isNullOrBlank()) {
                val sysObj = JSONObject()
                val sysParts = JSONArray()
                sysParts.put(JSONObject().put("text", systemInstruction))
                sysObj.put("parts", sysParts)
                root.put("systemInstruction", sysObj)
            }

            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            contentObj.put("role", "user")

            val partsArray = JSONArray()

            // If bitmap provided, append inlineData
            if (bitmap != null) {
                val base64 = bitmapToBase64(bitmap)
                val inlineDataObj = JSONObject()
                inlineDataObj.put("mimeType", "image/jpeg")
                inlineDataObj.put("data", base64)
                partsArray.put(JSONObject().put("inlineData", inlineDataObj))
            }

            // Append text prompt
            partsArray.put(JSONObject().put("text", prompt))
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)

            root.put("contents", contentsArray)

            val genConfig = JSONObject()
            genConfig.put("temperature", 0.7)
            genConfig.put("maxOutputTokens", 1200)
            root.put("generationConfig", genConfig)

            val requestBody = root.toString().toRequestBody(jsonMediaType)

            // Target gemini-2.5-flash or gemini-1.5-flash supported by Google Generative Language API
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    val errorMsg = try {
                        val errObj = JSONObject(bodyString).optJSONObject("error")
                        errObj?.optString("message") ?: "HTTP error ${response.code}: ${response.message}"
                    } catch (e: Exception) {
                        "HTTP error ${response.code}: $bodyString"
                    }
                    Log.e("GeminiApiService", "API Error: $errorMsg")
                    return@withContext Result.failure(IOException(errorMsg))
                }

                val responseJson = JSONObject(bodyString)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val text = parts.getJSONObject(0).optString("text", "")
                        return@withContext Result.success(text)
                    }
                }
                Result.failure(IOException("Received empty response from AI model."))
            }
        } catch (e: Exception) {
            Log.e("GeminiApiService", "Network/Parse error: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        // Compress to JPEG with quality 80 to keep under 4MB limit
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val byteArray = stream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}

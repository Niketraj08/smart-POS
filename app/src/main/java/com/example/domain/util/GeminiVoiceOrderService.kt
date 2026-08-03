package com.example.domain.util

import android.util.Log
import com.example.BuildConfig
import com.example.domain.model.MenuItemModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ParsedVoiceItem(
    val menuItem: MenuItemModel,
    val quantity: Int,
    val notes: String = ""
)

object GeminiVoiceOrderService {

    private const val TAG = "GeminiVoiceOrder"
    private const val GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun parseVoiceOrder(
        spokenTranscript: String,
        menuItems: List<MenuItemModel>
    ): List<ParsedVoiceItem> = withContext(Dispatchers.IO) {
        if (spokenTranscript.isBlank() || menuItems.isEmpty()) {
            return@withContext emptyList()
        }

        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val menuContext = menuItems.joinToString("\n") { item ->
                    "ID: ${item.id} | Name: ${item.name} | Price: ₹${item.price} | Desc: ${item.description}"
                }

                val promptText = """
                    You are an intelligent AI POS order assistant for a restaurant.
                    
                    Available Menu Items:
                    $menuContext
                    
                    Staff Voice Command: "$spokenTranscript"
                    
                    Task: Parse the staff voice command and match requested food/beverage items to the available menu items.
                    
                    Return a JSON array of objects with the following keys:
                    - "menuItemId": Int (the exact matching ID from available menu)
                    - "quantity": Int (number of items requested, default to 1)
                    - "notes": String (special preparation notes or customizations if mentioned, e.g. "extra spicy", "less ice", or empty string)
                    
                    Output MUST be strictly valid JSON array without any markdown formatting.
                """.trimIndent()

                val requestJson = JSONObject().apply {
                    val contentsArr = JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().put("text", promptText))
                            })
                        })
                    }
                    put("contents", contentsArr)
                    put("generationConfig", JSONObject().apply {
                        put("temperature", 0.1)
                        put("responseMimeType", "application/json")
                    })
                }

                val urlWithKey = "$GEMINI_URL?key=$apiKey"
                val body = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

                val request = Request.Builder()
                    .url(urlWithKey)
                    .post(body)
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val responseBodyStr = response.body?.string()

                if (response.isSuccessful && !responseBodyStr.isNullOrBlank()) {
                    val rootJson = JSONObject(responseBodyStr)
                    val candidates = rootJson.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val contentObj = candidates.getJSONObject(0).optJSONObject("content")
                        val partsArr = contentObj?.optJSONArray("parts")
                        if (partsArr != null && partsArr.length() > 0) {
                            val textResult = partsArr.getJSONObject(0).optString("text", "")
                            val parsedFromGemini = parseGeminiJsonResult(textResult, menuItems)
                            if (parsedFromGemini.isNotEmpty()) {
                                return@withContext parsedFromGemini
                            }
                        }
                    }
                } else {
                    Log.w(TAG, "Gemini API error code: ${response.code}, body: $responseBodyStr")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Gemini API exception", e)
            }
        }

        // Local Regex & Keyword Matching Fallback
        return@withContext fallbackLocalMatch(spokenTranscript, menuItems)
    }

    private fun parseGeminiJsonResult(jsonText: String, menuItems: List<MenuItemModel>): List<ParsedVoiceItem> {
        val resultList = mutableListOf<ParsedVoiceItem>()
        try {
            val cleanJson = jsonText.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val jsonArray = JSONArray(cleanJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val itemId = obj.optInt("menuItemId", -1)
                val quantity = obj.optInt("quantity", 1).coerceAtLeast(1)
                val notes = obj.optString("notes", "")

                val matchedMenuItem = menuItems.find { it.id == itemId }
                if (matchedMenuItem != null) {
                    resultList.add(ParsedVoiceItem(menuItem = matchedMenuItem, quantity = quantity, notes = notes))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Gemini JSON output: $jsonText", e)
        }
        return resultList
    }

    private fun fallbackLocalMatch(spokenTranscript: String, menuItems: List<MenuItemModel>): List<ParsedVoiceItem> {
        val matchedList = mutableListOf<ParsedVoiceItem>()
        val transcriptLower = spokenTranscript.lowercase()

        val wordToNumber = mapOf(
            "one" to 1, "a" to 1, "an" to 1, "single" to 1,
            "two" to 2, "double" to 2,
            "three" to 3, "triple" to 3,
            "four" to 4, "five" to 5, "six" to 6, "seven" to 7, "eight" to 8, "nine" to 9, "ten" to 10
        )

        for (item in menuItems) {
            val itemNameLower = item.name.lowercase()
            if (transcriptLower.contains(itemNameLower)) {
                var qty = 1
                // Look for numbers before the item name in text
                val itemIndex = transcriptLower.indexOf(itemNameLower)
                val precedingSub = transcriptLower.substring((itemIndex - 25).coerceAtLeast(0), itemIndex)

                // Check digit match (e.g., "2 paneer tikka")
                val digitMatch = Regex("\\b(\\d+)\\b").find(precedingSub)
                if (digitMatch != null) {
                    qty = digitMatch.groupValues[1].toIntOrNull() ?: 1
                } else {
                    // Check word match (e.g. "two paneer tikka")
                    for ((word, num) in wordToNumber) {
                        if (Regex("\\b$word\\b").containsMatchIn(precedingSub)) {
                            qty = num
                            break
                        }
                    }
                }

                // Check simple notes
                var notes = ""
                if (transcriptLower.contains("spicy")) notes = "Spicy"
                if (transcriptLower.contains("no onion")) notes += " No Onion"
                if (transcriptLower.contains("less ice")) notes += " Less Ice"

                matchedList.add(ParsedVoiceItem(menuItem = item, quantity = qty, notes = notes.trim()))
            }
        }
        return matchedList
    }
}

package com.mehrimaai.core

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.mehrimaai.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiAPIClient {

    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    private val systemPrompt = """
        আপনি Mehrima - একটি বন্ধুত্বপূর্ণ আনিমে গার্ল ভয়েস অ্যাসিস্ট্যান্ট।
        আপনার ভূমিকা:
        - সবসময় সহজ বাংলায় কথা বলুন
        - ব্যবহারকারীকে সাহায্য করুন এবং তাদের কাজ অটোমেট করুন
        - বন্ধুত্বপূর্ণ এবং সহায়ক থাকুন
        - শুধুমাত্র প্রোগ্রাম চালানোর নির্দেশ দিন
        
        গুরুত্বপূর্ণ: যখন অ্যাপ্লিকেশন খুলতে বলা হয়, JSON ফরম্যাটে উত্তর দিন:
        {
            "action": "open_app",
            "package_name": "com.example.app",
            "reason": "ব্যবহারকারী অনুরোধ করেছেন"
        }
    """.trimIndent()

    suspend fun sendMessage(userMessage: String): String = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = model.generateContent(
                content {
                    text("System Instruction: $systemPrompt\n\nব্যবহারকারী: $userMessage")
                }
            )
            response.text ?: "আমি আপনার প্রশ্নের উত্তর দিতে পারলাম না।"
        } catch (e: Exception) {
            "ত্রুটি: ${e.message}"
        }
    }

    fun extractActionFromResponse(response: String): Map<String, String>? {
        return try {
            val regex = """"action"\s*:\s*"([^"]+)"[^}]*"package_name"\s*:\s*"([^"]+)"""".toRegex()
            val matchResult = regex.find(response)
            
            if (matchResult != null) {
                mapOf(
                    "action" to matchResult.groupValues[1],
                    "package_name" to matchResult.groupValues[2]
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}

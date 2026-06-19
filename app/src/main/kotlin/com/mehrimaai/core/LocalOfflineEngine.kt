package com.mehrimaai.core

class LocalOfflineEngine {

    private val banglaCommands = mapOf(
        "খোলো" to "open",
        "লগইন" to "login",
        "হ্যালো" to "hello",
        "সালাম" to "hello",
        "কী খবর" to "how_are_you",
        "ধন্যবাদ" to "thank_you",
        "আমার সম্পর্কে বলো" to "tell_about_me",
        "ক্যালেন্ডার খোলো" to "open_calendar",
        "মেসেজ পাঠাও" to "send_message",
        "কল করো" to "make_call"
    )

    private val englishCommands = mapOf(
        "open" to "open",
        "login" to "login",
        "hello" to "hello",
        "how are you" to "how_are_you",
        "thank you" to "thank_you",
        "call" to "make_call",
        "send" to "send_message"
    )

    fun processOfflineCommand(input: String): String {
        val lowerInput = input.lowercase().trim()
        
        for ((bangla, action) in banglaCommands) {
            if (lowerInput.contains(bangla)) {
                return when (action) {
                    "hello" -> "সালাম! আমি Mehrima। আপনি আজ কেমন আছেন?"
                    "how_are_you" -> "আমি খুব ভালো আছি, ধন্যবাদ জিজ্ঞাসা করার জন্য!"
                    "thank_you" -> "আপনাকেও স্বাগতম! আমার সাহায্য করতে পেরে খুশি।"
                    else -> "নির্দেশ বোঝা যাচ্ছে। একটি মুহূর্ত অপেক্ষা করুন..."
                }
            }
        }

        for ((english, action) in englishCommands) {
            if (lowerInput.contains(english)) {
                return "Command recognized: $action"
            }
        }

        return "আমি এই নির্দেশ বুঝতে পারলাম না। দয়া করে পুনরায় চেষ্টা করুন।"
    }

    fun isOfflineMode(): String = "Offline: Local Engine Activated"
}

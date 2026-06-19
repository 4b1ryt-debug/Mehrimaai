# Gemini API
-keep class com.google.ai.client.generativeai.** { *; }
-keep interface com.google.ai.client.generativeai.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**

# Kotlin
-keep class kotlin.** { *; }
-keep interface kotlin.** { *; }

# Keep our app classes
-keep class com.mehrimaai.** { *; }
-keep interface com.mehrimaai.** { *; }

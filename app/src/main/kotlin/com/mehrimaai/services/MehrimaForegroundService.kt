package com.mehrimaai.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.mehrimaai.core.GeminiAPIClient
import com.mehrimaai.core.LocalOfflineEngine
import com.mehrimaai.core.NetworkMonitor
import com.mehrimaai.utils.FileManager
import com.mehrimaai.utils.MemoryManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MehrimaForegroundService : Service() {

    private lateinit var networkMonitor: NetworkMonitor
    private lateinit var geminiClient: GeminiAPIClient
    private lateinit var offlineEngine: LocalOfflineEngine
    private lateinit var vadBuffer: VADBuffer
    private lateinit var fileManager: FileManager
    private lateinit var memoryManager: MemoryManager
    
    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private val notificationId = 1001
    private val channelId = "mehrima_channel"

    override fun onCreate() {
        super.onCreate()
        setupNotificationChannel()
        setupComponents()
    }

    private fun setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Mehrima AI Service",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Mehrima AI 24/7 Voice Assistant"
                enableVibration(false)
            }
            
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun setupComponents() {
        try {
            networkMonitor = NetworkMonitor(this)
            geminiClient = GeminiAPIClient()
            offlineEngine = LocalOfflineEngine()
            vadBuffer = VADBuffer()
            fileManager = FileManager(this)
            memoryManager = MemoryManager(this)
            
            networkMonitor.startMonitoring()
            memoryManager.logActivity("Mehrima AI Service Started")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(notificationId, buildNotification())
        
        serviceScope.launch {
            try {
                vadBuffer.startListening(serviceScope)
                vadBuffer.setOnProcessingNeeded { audioData ->
                    processAudio(audioData)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        return START_STICKY
    }

    private fun processAudio(audioData: ByteArray) {
        serviceScope.launch {
            try {
                val userInput = "Mehrima, Google খোলো"
                
                if (!userInput.startsWith("Mehrima", ignoreCase = true)) {
                    memoryManager.logActivity("Ignored command (no wake-word): $userInput")
                    return@launch
                }
                
                val cleanedInput = userInput.substring(8).trim()
                
                val isOnline = networkMonitor.checkNetworkConnection()
                val response = if (isOnline) {
                    geminiClient.sendMessage(cleanedInput)
                } else {
                    offlineEngine.processOfflineCommand(cleanedInput)
                }
                
                memoryManager.logActivity(response)
                memoryManager.updateKnowledge(userInput, response)
                updateNotification(response)
                
            } catch (e: Exception) {
                memoryManager.logActivity("Error: ${e.message}")
            }
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Mehrima AI")
            .setContentText("শোনছি...")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Mehrima AI")
            .setContentText(text.take(50))
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .build()
        
        val manager = getSystemService(NotificationManager::class.java)
        manager?.notify(notificationId, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        vadBuffer.stopListening()
        networkMonitor.stopMonitoring()
        memoryManager.logActivity("Mehrima AI Service Stopped")
    }
}

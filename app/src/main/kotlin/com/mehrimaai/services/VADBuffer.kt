package com.mehrimaai.services

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sqrt

class VADBuffer(
    private val sampleRate: Int = 16000,
    private val silenceThresholdMs: Long = 1500
) {

    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    private var audioRecord: AudioRecord? = null
    private var isListening = false
    private var lastSoundTime = System.currentTimeMillis()
    private var onProcessingNeeded: ((ByteArray) -> Unit)? = null

    fun startListening(scope: CoroutineScope) {
        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )
            
            audioRecord?.startRecording()
            isListening = true
            
            scope.launch {
                processAudio()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun processAudio() {
        val buffer = ByteArray(bufferSize)
        var recordedData = mutableListOf<Byte>()

        while (isListening) {
            val readSize = audioRecord?.read(buffer, 0, buffer.size) ?: 0
            
            if (readSize > 0) {
                recordedData.addAll(buffer.take(readSize))
                
                if (detectVoiceActivity(buffer.take(readSize).toByteArray())) {
                    lastSoundTime = System.currentTimeMillis()
                }
                
                if (System.currentTimeMillis() - lastSoundTime > silenceThresholdMs && recordedData.isNotEmpty()) {
                    onProcessingNeeded?.invoke(recordedData.toByteArray())
                    recordedData.clear()
                }
            }
            
            delay(100)
        }
    }

    private fun detectVoiceActivity(audioData: ByteArray): Boolean {
        var rms = 0.0
        var count = 0
        
        for (i in audioData.indices step 2) {
            if (i + 1 < audioData.size) {
                val sample = ((audioData[i + 1].toInt() shl 8) or (audioData[i].toInt() and 0xFF)).toShort()
                rms += sample * sample
                count++
            }
        }
        
        rms = if (count > 0) sqrt(rms / count) else 0.0
        return rms > 1000
    }

    fun stopListening() {
        isListening = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }

    fun setOnProcessingNeeded(callback: (ByteArray) -> Unit) {
        onProcessingNeeded = callback
    }
}

package com.example.chikchik.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.chikchik.R
import com.example.chikchik.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

class ChikChikForegroundService : LifecycleService() {

    private lateinit var securityManager: SecurityManager
    private lateinit var chatApiService: ChatApiService

    override fun onCreate() {
        super.onCreate()
        securityManager = SecurityManager(this)
        
        val json = Json { 
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
        val contentType = "application/json".toMediaType()
        
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.groq.com/openai/v1/")
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()

        chatApiService = retrofit.create(ChatApiService::class.java)
        
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        
        if (intent?.action == ACTION_PROCESS_TEXT) {
            val text = intent.getStringExtra(EXTRA_TEXT) ?: ""
            startForeground(NOTIFICATION_ID_PROCESSING, createProcessingNotification())
            processTextWithAi(text)
        }
        
        return START_NOT_STICKY
    }

    private fun processTextWithAi(text: String) {
        val apiKey = securityManager.getApiKey()
        if (apiKey.isNullOrBlank()) {
            showResultNotification("Missing API Key. Please set it in the app.")
            stopSelf()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            var lastError: String? = null

            for (modelId in AVAILABLE_MODELS) {
                try {
                    val request = ChatRequest(
                        model = modelId,
                        messages = listOf(
                            Message(role = "system", content = SYSTEM_PROMPT),
                            Message(role = "user", content = text)
                        )
                    )
                    
                    val response = chatApiService.getCompletion("Bearer $apiKey", request)
                    val reply = response.choices.firstOrNull()?.message?.content
                    
                    if (!reply.isNullOrBlank()) {
                        withContext(Dispatchers.Main) {
                            showResultNotification(reply)
                            stopSelf()
                        }
                        return@launch
                    }
                } catch (e: Exception) {
                    lastError = e.message
                    // Log and try next model
                    Log.w("ChikChik", "Model $modelId failed: $lastError")
                }
            }
            
            withContext(Dispatchers.Main) {
                showResultNotification("Couldn't reach AI with any available model. Last error: $lastError")
                stopSelf()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "ChikChik AI Assistant",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createProcessingNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ChikChik is thinking...")
            .setContentText("Processing screen content")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use default for now
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun showResultNotification(content: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "chikchik_result_channel",
                "ChikChik Results",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, "chikchik_result_channel")
            .setContentTitle("ChikChik Reply")
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(Notification.DEFAULT_ALL)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID_RESULT, notification)
    }

    companion object {
        const val ACTION_PROCESS_TEXT = "com.example.chikchik.action.PROCESS_TEXT"
        const val EXTRA_TEXT = "extra_text"
        private const val CHANNEL_ID = "chikchik_channel"
        private const val NOTIFICATION_ID_PROCESSING = 1
        private const val NOTIFICATION_ID_RESULT = 2

        private val AVAILABLE_MODELS = listOf(
            "openai/gpt-oss-120b",
            "openai/gpt-oss-20b",
            "llama-3.3-70b-versatile",
            "mixtral-8x7b-32768",
            "qwen/qwen3.8-27b"
        )

        private const val SYSTEM_PROMPT = """
            You are a screen-reading assistant.
            TASK: Identify the primary question or content and provide the shortest direct answer.
            
            RULES:
            1. If there's a multiple-choice question, output ONLY the correct option (e.g., "D Cuba").
            2. IGNORE ALL metadata, dates, times, or "Photo taken on..." text. 
            3. If both a question and metadata are present, focus ONLY on the question.
            4. No preamble, no explanation, no sentences.
        """
    }
}

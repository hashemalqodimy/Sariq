package com.example.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.data.local.AmanPhoneDatabase
import com.example.data.model.UrgentAlert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class AmanForegroundSyncService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isListening = false

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS) // infinite timeout for stream
            .connectTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    companion object {
        private const val TAG = "AmanForegroundService"
        private const val NOTIFICATION_ID = 8888
        private const val CHANNEL_ID = "aman_foreground_channel"
        private const val NTFY_STREAM_URL = "https://ntfy.sh/aman_phone_yemen_v2/json"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isListening) {
            isListening = true
            startListeningToStream()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        isListening = false
        serviceScope.cancel()
        httpClient.dispatcher.executorService.shutdown()
        httpClient.connectionPool.evictAll()
        super.onDestroy()
    }

    private fun startListeningToStream() {
        serviceScope.launch {
            while (isActive) {
                try {
                    Log.i(TAG, "Connecting to live cloud stream...")
                    val request = Request.Builder()
                        .url(NTFY_STREAM_URL)
                        .build()

                    val response = httpClient.newCall(request).execute()
                    if (response.isSuccessful) {
                        Log.i(TAG, "Stream connected successfully.")
                        val source = response.body?.source()
                        
                        while (source != null && !source.exhausted() && isActive) {
                            val line = source.readUtf8Line()
                            if (!line.isNullOrBlank()) {
                                handleIncomingStreamData(line)
                            }
                        }
                    } else {
                        Log.w(TAG, "Stream connection failed with code: ${response.code}")
                    }
                    response.close()
                } catch (e: Exception) {
                    Log.e(TAG, "Stream error: ${e.message}")
                }

                // If stream breaks, wait 5 seconds before reconnecting
                if (isActive) {
                    delay(5000)
                }
            }
        }
    }

    private suspend fun handleIncomingStreamData(jsonLine: String) {
        try {
            val eventObj = JSONObject(jsonLine)
            if (eventObj.optString("event") == "message") {
                val messageStr = eventObj.optString("message")
                if (messageStr.startsWith("{")) {
                    val payload = JSONObject(messageStr)
                    val alertObj = payload.optJSONObject("alert")
                    if (alertObj != null) {
                        processAlert(alertObj)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing stream data: ${e.message}")
        }
    }

    private suspend fun processAlert(obj: JSONObject) {
        val title = obj.optString("title", "")
        if (title.isBlank()) return

        val urgentAlert = UrgentAlert(
            id = 0L,
            reportId = 0L,
            title = title,
            message = obj.optString("message", ""),
            governorate = obj.optString("governorate", "اليمن"),
            phoneModel = obj.optString("phoneModel", "جهاز ذكي"),
            imeiSnippet = obj.optString("imeiSnippet", ""),
            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
            isRead = false,
            severity = obj.optString("severity", "CRITICAL")
        )

        try {
            val database = AmanPhoneDatabase.getDatabase(applicationContext, serviceScope)
            val alertDao = database.alertDao()

            // Deduplicate
            val existingAlerts = alertDao.getAllAlerts().firstOrNull() ?: emptyList()
            val isDuplicate = existingAlerts.any { existing ->
                existing.phoneModel == urgentAlert.phoneModel &&
                        existing.governorate == urgentAlert.governorate &&
                        Math.abs(existing.timestamp - urgentAlert.timestamp) <= 120_000L
            }

            if (!isDuplicate) {
                Log.i(TAG, "New alert received from stream! Showing notification.")
                val alertId = alertDao.insertAlert(urgentAlert)
                
                NotificationHelper.showUrgentAlertNotification(
                    context = applicationContext,
                    id = (alertId % 10000).toInt(),
                    title = urgentAlert.title,
                    message = urgentAlert.message,
                    governorate = urgentAlert.governorate,
                    phoneModel = urgentAlert.phoneModel
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving stream alert: ${e.message}")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "خدمة المزامنة الفورية"
            val descriptionText = "تبقي التطبيق متصلاً لاستقبال البلاغات فوراً"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("أمان فون - خدمة الاستقبال الفوري")
            .setContentText("التطبيق متصل الآن ويستقبل بلاغات السرقات فورياً.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
}

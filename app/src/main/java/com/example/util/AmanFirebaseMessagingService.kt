package com.example.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.local.AmanPhoneDatabase
import com.example.data.model.UrgentAlert
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Firebase Cloud Messaging (FCM) Service for AmanPhone Yemen.
 * Handles instant push notifications for urgent stolen phone alerts across all registered devices.
 */
class AmanFirebaseMessagingService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "AmanFCMService"
        private const val PREFS_NAME = "aman_fcm_prefs"
        private const val KEY_FCM_TOKEN = "fcm_device_token"
        const val TOPIC_ALL_REPORTS = "all_yemen_reports"
        const val TOPIC_URGENT_ALERTS = "urgent_alerts"

        fun getSavedToken(context: Context): String? {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getString(KEY_FCM_TOKEN, null)
        }

        fun saveToken(context: Context, token: String) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_FCM_TOKEN, token).apply()
        }

        /**
         * Initialize FCM on app startup:
         * 1. Subscribes device to universal broadcast topics
         * 2. Retrieves and caches device token
         * 3. Syncs token with Cloud database
         */
        fun initializeFcm(context: Context, onTokenReceived: ((String) -> Unit)? = null) {
            try {
                if (FirebaseApp.getApps(context).isEmpty()) {
                    Log.w(TAG, "FirebaseApp not yet initialized, skipping FCM setup")
                    return
                }

                val fcm = FirebaseMessaging.getInstance()

                // Subscribe to universal broadcast topics
                fcm.subscribeToTopic(TOPIC_ALL_REPORTS)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.i(TAG, "Subscribed to FCM topic: $TOPIC_ALL_REPORTS")
                        } else {
                            Log.w(TAG, "Failed subscribing to topic $TOPIC_ALL_REPORTS: ${task.exception?.message}")
                        }
                    }

                fcm.subscribeToTopic(TOPIC_URGENT_ALERTS)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.i(TAG, "Subscribed to FCM topic: $TOPIC_URGENT_ALERTS")
                        }
                    }

                // Fetch current token
                fcm.token.addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result != null) {
                        val token = task.result
                        Log.i(TAG, "Current FCM Device Token retrieved: ${token.take(16)}...")
                        saveToken(context, token)
                        registerTokenToCloud(context, token)
                        onTokenReceived?.invoke(token)
                    } else {
                        Log.w(TAG, "Failed to retrieve FCM token: ${task.exception?.message}")
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error initializing FCM: ${e.message}")
            }
        }

        /**
         * Registers the device token in Firestore for targeted or device-specific push broadcasts
         */
        fun registerTokenToCloud(context: Context, token: String) {
            try {
                if (FirebaseApp.getApps(context).isNotEmpty()) {
                    val db = FirebaseFirestore.getInstance()
                    val tokenData = hashMapOf(
                        "token" to token,
                        "platform" to "android",
                        "updatedAt" to System.currentTimeMillis(),
                        "subscribedTopics" to listOf(TOPIC_ALL_REPORTS, TOPIC_URGENT_ALERTS)
                    )
                    db.collection("fcm_device_tokens")
                        .document(token.take(32))
                        .set(tokenData, SetOptions.merge())
                        .addOnSuccessListener {
                            Log.i(TAG, "FCM Device token synced to Firestore")
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Failed to sync token to Firestore: ${e.message}")
                        }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Exception registering token to cloud: ${e.message}")
            }
        }
    }

    /**
     * Called when a new FCM registration token is generated.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i(TAG, "onNewToken: Received new FCM token: ${token.take(16)}...")
        saveToken(applicationContext, token)
        registerTokenToCloud(applicationContext, token)

        // Resubscribe to default topics with new token
        try {
            FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_ALL_REPORTS)
            FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_URGENT_ALERTS)
        } catch (_: Exception) {
        }
    }

    /**
     * Called when a push message is received from Firebase Cloud Messaging while app is in foreground or background.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.i(TAG, "onMessageReceived from: ${remoteMessage.from}")

        val data = remoteMessage.data
        val notification = remoteMessage.notification

        val title = data["title"]
            ?: notification?.title
            ?: "تعميم سرقة عاجل"

        val message = data["message"]
            ?: notification?.body
            ?: "تم الإبلاغ عن هاتف مسروق جديد، يرجى التحقق من الرقم التسلسلي"

        val governorate = data["governorate"] ?: "اليمن"
        val phoneModel = data["phoneModel"] ?: "جهاز ذكي"
        val imeiSnippet = data["imeiSnippet"] ?: ""
        val severity = data["severity"] ?: "CRITICAL"
        val reportId = data["reportId"]?.toLongOrNull() ?: 0L

        // Process and persist alert into local Room database
        serviceScope.launch {
            try {
                val database = AmanPhoneDatabase.getDatabase(applicationContext, this)
                val alertDao = database.alertDao()

                val urgentAlert = UrgentAlert(
                    id = 0L,
                    reportId = reportId,
                    title = title,
                    message = message,
                    governorate = governorate,
                    phoneModel = phoneModel,
                    imeiSnippet = imeiSnippet,
                    timestamp = System.currentTimeMillis(),
                    isRead = false,
                    severity = severity
                )

                // Deduplicate check
                val existingAlerts = alertDao.getAllAlerts().firstOrNull() ?: emptyList()
                val isDuplicate = existingAlerts.any { existing ->
                    existing.phoneModel == phoneModel &&
                            existing.governorate == governorate &&
                            Math.abs(existing.timestamp - urgentAlert.timestamp) <= 120_000L
                }

                val alertId = if (!isDuplicate) {
                    alertDao.insertAlert(urgentAlert)
                } else {
                    Random.nextLong(1000, 9999)
                }

                // Trigger high-priority system notification with sound & vibration
                NotificationHelper.showUrgentAlertNotification(
                    context = applicationContext,
                    id = (alertId % 10000).toInt(),
                    title = title,
                    message = message,
                    governorate = governorate,
                    phoneModel = phoneModel
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error handling FCM message: ${e.message}", e)
                // Fallback to direct notification
                NotificationHelper.showUrgentAlertNotification(
                    context = applicationContext,
                    id = Random.nextInt(1000, 9999),
                    title = title,
                    message = message,
                    governorate = governorate,
                    phoneModel = phoneModel
                )
            }
        }
    }
}

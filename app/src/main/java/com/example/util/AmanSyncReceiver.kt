package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import com.example.data.local.AmanPhoneDatabase
import com.example.data.model.UrgentAlert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * Background BroadcastReceiver that periodically polls the Central Cloud Broadcast Hub
 * so users and phone shops receive urgent stolen phone notifications EVEN IF THE APP IS CLOSED.
 */
class AmanSyncReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AmanSyncReceiver"
        private const val REQUEST_CODE = 4421

        fun schedulePeriodicSync(context: Context) {
            try {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
                val intent = Intent(context, AmanSyncReceiver::class.java).apply {
                    action = "com.example.ACTION_SYNC_ALERTS"
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // Repeat every 15 minutes in background
                val intervalMillis = 15 * 60 * 1000L
                val triggerAtMillis = SystemClock.elapsedRealtime() + (2 * 60 * 1000L)

                alarmManager.setInexactRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerAtMillis,
                    intervalMillis,
                    pendingIntent
                )
                Log.i(TAG, "Scheduled periodic background alert sync")
            } catch (e: Exception) {
                Log.w(TAG, "Failed scheduling background sync: ${e.message}")
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val cloudSyncManager = CloudSyncManager(context)
                val database = AmanPhoneDatabase.getDatabase(context, this)
                val alertDao = database.alertDao()

                // Direct fetch from cloud ledger
                val alerts: List<UrgentAlert> = cloudSyncManager.fetchLatestAlertsDirect()
                val existingAlerts: List<UrgentAlert> = alertDao.getAllAlerts().firstOrNull() ?: emptyList()

                for (alert in alerts) {
                    val alreadyExists = existingAlerts.any { existing ->
                        existing.phoneModel == alert.phoneModel &&
                                existing.governorate == alert.governorate &&
                                Math.abs(existing.timestamp - alert.timestamp) <= 120_000L
                    }

                    if (!alreadyExists) {
                        val id = alertDao.insertAlert(alert)
                        val isFresh = (System.currentTimeMillis() - alert.timestamp) < (3 * 3600 * 1000L)
                        if (isFresh) {
                            NotificationHelper.showUrgentAlertNotification(
                                context = context,
                                id = (id % 10000).toInt(),
                                title = alert.title,
                                message = alert.message,
                                governorate = alert.governorate,
                                phoneModel = alert.phoneModel
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Background sync onReceive error: ${e.message}")
            } finally {
                pendingResult.finish()
            }
        }
    }
}

package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {
    const val CHANNEL_ID = "aman_phone_urgent_channel"
    private const val CHANNEL_NAME = "تنبيهات سرقة الهواتف العاجلة"
    private const val CHANNEL_DESC = "إشعارات فورية عند الإبلاغ عن هاتف مسروق في محافظات الجمهورية اليمنية"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showUrgentAlertNotification(
        context: Context,
        id: Int,
        title: String,
        message: String,
        governorate: String,
        phoneModel: String
    ) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🚨 $title ($governorate)")
            .setContentText("$phoneModel: $message")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("محافظة $governorate: تم التعميم الفوري عن $phoneModel.\n$message\nيرجى من أصحاب المحلات والمواطنين التحقق من رقم الـ IMEI في التطبيق.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            if (notificationManager.areNotificationsEnabled()) {
                notificationManager.notify(id, builder.build())
            }
        } catch (e: SecurityException) {
            // Permission not yet granted on Android 13+
        }
    }
}

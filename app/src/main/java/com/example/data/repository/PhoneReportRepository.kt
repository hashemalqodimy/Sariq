package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.AlertDao
import com.example.data.local.ImeiCheckDao
import com.example.data.local.ReportDao
import com.example.data.local.UserDao
import com.example.data.model.AppUser
import com.example.data.model.ImeiCheckRecord
import com.example.data.model.PhoneReport
import com.example.data.model.UrgentAlert
import com.example.util.CloudSyncManager
import com.example.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class PhoneReportRepository(
    private val reportDao: ReportDao,
    private val alertDao: AlertDao,
    private val imeiCheckDao: ImeiCheckDao,
    private val userDao: UserDao,
    private val context: Context,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    val cloudSyncManager = CloudSyncManager(context)

    val allReports: Flow<List<PhoneReport>> = reportDao.getAllReports()
    val allAlerts: Flow<List<UrgentAlert>> = alertDao.getAllAlerts()
    val unreadAlertsCount: Flow<Int> = alertDao.getUnreadCount()
    val totalReportsCount: Flow<Int> = reportDao.getTotalReportsCount()
    val recoveredReportsCount: Flow<Int> = reportDao.getRecoveredReportsCount()
    val recentImeiChecks: Flow<List<ImeiCheckRecord>> = imeiCheckDao.getRecentChecks()

    init {
        // Start real-time cloud observation and auto-sync
        startCloudSynchronization()
        com.example.util.AmanSyncReceiver.schedulePeriodicSync(context)
    }

    suspend fun syncNow(showNotificationForNewAlerts: Boolean = true): Int = withContext(Dispatchers.IO) {
        var newItemsCount = 0
        try {
            val (cloudReports, cloudAlerts) = cloudSyncManager.fetchLatestCloudData()

            for (cloudReport in cloudReports) {
                val localMatch = reportDao.findReportByExactImei(cloudReport.imei1)
                if (localMatch == null) {
                    reportDao.insertReport(cloudReport)
                    newItemsCount++
                } else if (localMatch.status != cloudReport.status) {
                    reportDao.updateStatus(localMatch.id, cloudReport.status)
                }
            }

            val existingAlerts = alertDao.getAllAlerts().firstOrNull() ?: emptyList()
            for (alert in cloudAlerts) {
                val alreadyExists = existingAlerts.any { existing ->
                    existing.phoneModel == alert.phoneModel &&
                            existing.governorate == alert.governorate &&
                            Math.abs(existing.timestamp - alert.timestamp) <= 120_000L
                }
                if (!alreadyExists) {
                    val id = alertDao.insertAlert(alert)
                    newItemsCount++
                    if (showNotificationForNewAlerts) {
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
            Log.w("PhoneReportRepository", "Sync error: ${e.message}")
        }
        newItemsCount
    }

    private fun startCloudSynchronization() {
        // 1. Initial fast sync on launch (populate existing data without loud alerts)
        externalScope.launch {
            try {
                syncNow(showNotificationForNewAlerts = false)
            } catch (_: Exception) {
            }
        }

        // 2. High-frequency live loop (every 4 seconds for instant multi-device sync)
        externalScope.launch {
            while (isActive) {
                try {
                    delay(4000)
                    syncNow(showNotificationForNewAlerts = true)
                } catch (e: Exception) {
                    Log.w("PhoneReportRepository", "Sync loop error: ${e.message}")
                    delay(5000)
                }
            }
        }
    }

    // Authentication & User profile methods
    suspend fun getUserByEmail(email: String): AppUser? = userDao.getUserByEmail(email.trim().lowercase())

    suspend fun getLastActiveUser(): AppUser? = userDao.getLastActiveUser()

    suspend fun saveUser(user: AppUser) {
        userDao.insertUser(user)
    }

    suspend fun updateUser(user: AppUser) {
        userDao.updateUser(user)
    }

    fun searchReports(query: String): Flow<List<PhoneReport>> {
        return reportDao.searchReports(query.trim())
    }

    fun getReportsByGovernorate(governorate: String): Flow<List<PhoneReport>> {
        return if (governorate == "كافة المحافظات" || governorate.isBlank()) {
            reportDao.getAllReports()
        } else {
            reportDao.getReportsByGovernorate(governorate)
        }
    }

    fun getReportsByStatus(status: String): Flow<List<PhoneReport>> {
        return if (status == "الكل" || status.isBlank()) {
            reportDao.getAllReports()
        } else {
            reportDao.getReportsByStatus(status)
        }
    }

    suspend fun checkImei(rawImei: String): Pair<PhoneReport?, Boolean> {
        val cleanImei = rawImei.trim().filter { it.isDigit() }
        var foundReport = reportDao.findReportByExactImei(cleanImei)

        // If not found in local cache, query centralized cloud directly
        if (foundReport == null && cloudSyncManager.isCloudConnected) {
            val cloudReport = cloudSyncManager.searchImeiInCloud(cleanImei)
            if (cloudReport != null) {
                reportDao.insertReport(cloudReport)
                foundReport = cloudReport
            }
        }

        val isStolen = foundReport != null && (foundReport.status == "مسروق" || foundReport.status == "مفقود")

        imeiCheckDao.insertCheck(
            ImeiCheckRecord(
                imei = cleanImei,
                isStolen = isStolen,
                phoneModel = foundReport?.let { "${it.brand} ${it.modelName}" } ?: "جهاز سليم وغير مدرج"
            )
        )

        return Pair(foundReport, isStolen)
    }

    suspend fun submitReport(report: PhoneReport): Long {
        val reportId = reportDao.insertReport(report)

        // Broadcast urgent alert to all users across the republic
        val imeiSnippet = if (report.imei1.length >= 6) {
            "${report.imei1.take(6)}...${report.imei1.takeLast(3)}"
        } else {
            report.imei1
        }

        val alertTitle = "تعميم سرقة عاجل - ${report.governorate}"
        val alertMessage = "تم الإبلاغ عن ${report.status} هاتف ${report.brand} ${report.modelName} في ${report.district} بمحافظة ${report.governorate}. مكافأة: ${if (report.rewardAmount > 0) "${report.rewardAmount} ريال" else "متاحة"}"

        val alert = UrgentAlert(
            reportId = reportId,
            title = alertTitle,
            message = alertMessage,
            governorate = report.governorate,
            phoneModel = "${report.brand} ${report.modelName}",
            imeiSnippet = imeiSnippet,
            severity = if (report.status == "مسروق") "CRITICAL" else "WARNING"
        )
        alertDao.insertAlert(alert)

        // Push to cloud immediately so ALL other users, stores, and police stations receive it instantly
        withContext(Dispatchers.IO) {
            try {
                cloudSyncManager.publishReportToCloud(report.copy(id = reportId))
            } catch (e: Exception) {
                Log.w("PhoneReportRepository", "Error publishing to cloud: ${e.message}")
            }
        }

        // Show native Android notification
        NotificationHelper.showUrgentAlertNotification(
            context = context,
            id = (reportId % 10000).toInt(),
            title = alertTitle,
            message = alertMessage,
            governorate = report.governorate,
            phoneModel = "${report.brand} ${report.modelName}"
        )

        return reportId
    }

    suspend fun updateReportStatus(reportId: Long, newStatus: String, phoneModel: String, gov: String) {
        reportDao.updateStatus(reportId, newStatus)

        // Sync update to cloud
        externalScope.launch {
            try {
                val report = reportDao.getAllReports().firstOrNull()?.find { it.id == reportId }
                if (report != null) {
                    cloudSyncManager.updateReportStatusInCloud(report.imei1, newStatus)
                }
            } catch (_: Exception) {
            }
        }

        if (newStatus == "تم الاسترجاع") {
            val alert = UrgentAlert(
                reportId = reportId,
                title = "✅ بشرى: تم استرجاع هاتف بنجاح",
                message = "بحمد الله تم استرجاع وضبط هاتف $phoneModel بمحافظة $gov وإعادته لمالكه الشرعي.",
                governorate = gov,
                phoneModel = phoneModel,
                imeiSnippet = "",
                severity = "RESOLVED"
            )
            alertDao.insertAlert(alert)

            NotificationHelper.showUrgentAlertNotification(
                context = context,
                id = Random.nextInt(20000, 30000),
                title = "تم استرجاع الهاتف بنجاح",
                message = "تم ضبط هاتف $phoneModel في محافظة $gov",
                governorate = gov,
                phoneModel = phoneModel
            )
        }
    }

    suspend fun markAlertAsRead(id: Long) {
        alertDao.markAsRead(id)
    }

    suspend fun markAllAlertsAsRead() {
        alertDao.markAllAsRead()
    }

    suspend fun deleteReport(report: PhoneReport) {
        reportDao.deleteReport(report)
    }
}

package com.example.data.repository

import android.content.Context
import com.example.data.local.AlertDao
import com.example.data.local.ImeiCheckDao
import com.example.data.local.ReportDao
import com.example.data.local.UserDao
import com.example.data.model.AppUser
import com.example.data.model.ImeiCheckRecord
import com.example.data.model.PhoneReport
import com.example.data.model.UrgentAlert
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.Flow
import kotlin.random.Random

class PhoneReportRepository(
    private val reportDao: ReportDao,
    private val alertDao: AlertDao,
    private val imeiCheckDao: ImeiCheckDao,
    private val userDao: UserDao,
    private val context: Context
) {
    val allReports: Flow<List<PhoneReport>> = reportDao.getAllReports()
    val allAlerts: Flow<List<UrgentAlert>> = alertDao.getAllAlerts()
    val unreadAlertsCount: Flow<Int> = alertDao.getUnreadCount()
    val totalReportsCount: Flow<Int> = reportDao.getTotalReportsCount()
    val recoveredReportsCount: Flow<Int> = reportDao.getRecoveredReportsCount()
    val recentImeiChecks: Flow<List<ImeiCheckRecord>> = imeiCheckDao.getRecentChecks()

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
        val foundReport = reportDao.findReportByExactImei(cleanImei)
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

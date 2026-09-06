package com.example.data.repository

import android.content.Context
import com.example.data.local.AlertDao
import com.example.data.local.ImeiCheckDao
import com.example.data.local.ReportDao
import com.example.data.local.UserDao
import com.example.data.model.AppUser
import com.example.data.model.PhoneReport
import com.example.data.model.UrgentAlert
import com.example.util.CloudSyncManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class PhoneReportRepository(
    private val dao: ReportDao,
    private val alertDao: AlertDao,
    private val imeiCheckDao: ImeiCheckDao,
    private val userDao: UserDao,
    context: Context
) {
    val cloudSyncManager = CloudSyncManager(context)

    val allReports: Flow<List<PhoneReport>> = dao.getAllReports()
    fun getMyReports(email: String): Flow<List<PhoneReport>> = dao.getReportsByUser(email)
    val stolenReports: Flow<List<PhoneReport>> = dao.getReportsByStatus("مسروق")
    val recoveredReports: Flow<List<PhoneReport>> = dao.getReportsByStatus("تم الاسترجاع")
    
    val allAlerts: Flow<List<UrgentAlert>> = alertDao.getAllAlerts()
    val unreadAlertsCount: Flow<Int> = alertDao.getUnreadCount()
    val totalReportsCount: Flow<Int> = dao.getTotalReportsCount()
    val recoveredReportsCount: Flow<Int> = dao.getRecoveredReportsCount()
    val recentImeiChecks = imeiCheckDao.getRecentChecks()

    suspend fun getLastActiveUser(): AppUser? = userDao.getLastActiveUser()
    suspend fun getUserByEmail(email: String): AppUser? = userDao.getUserByEmail(email)
    suspend fun saveUser(user: AppUser) = userDao.insertUser(user)

    suspend fun submitReport(report: PhoneReport) {
        dao.insertReport(report)
        cloudSyncManager.syncLocalReportsToCloud(listOf(report))
    }

    suspend fun updateReportStatus(reportId: Long, newStatus: String, phoneModel: String, gov: String) {
        dao.updateStatus(reportId, newStatus)
    }

    suspend fun deleteReportLocal(report: PhoneReport) {
        dao.deleteReport(report)
    }

    suspend fun checkImei(imei: String): Pair<PhoneReport?, Boolean> {
        val foundLocally = dao.findReportByExactImei(imei)
        if (foundLocally != null) {
            return Pair(foundLocally, foundLocally.status != "تم الاسترجاع")
        }
        val foundCloud = cloudSyncManager.searchImeiInCloud(imei)
        if (foundCloud != null) {
            dao.insertReport(foundCloud) // Cache locally
            return Pair(foundCloud, foundCloud.status != "تم الاسترجاع")
        }
        return Pair(null, false)
    }
    
    suspend fun syncNow(showNotificationForNewAlerts: Boolean): Int {
        var newCount = 0
        if (showNotificationForNewAlerts) {
            val cloudReports = cloudSyncManager.fetchCloudReports()
            cloudReports.forEach { 
                dao.insertReport(it)
                newCount++
            }
            val cloudAlerts = cloudSyncManager.fetchCloudAlerts()
            val existingAlerts = alertDao.getAllAlerts().first()
            cloudAlerts.forEach { alert ->
                if (existingAlerts.none { it.title == alert.title && it.timestamp == alert.timestamp }) {
                    alertDao.insertAlert(alert)
                    newCount++
                }
            }
        } else {
            val local = dao.getAllReports().first()
            cloudSyncManager.syncLocalReportsToCloud(local)
        }
        return newCount
    }
    
    suspend fun markAlertAsRead(alertId: Long) {
        alertDao.markAsRead(alertId)
    }

    suspend fun markAllAlertsAsRead() {
        alertDao.markAllAsRead()
    }
}

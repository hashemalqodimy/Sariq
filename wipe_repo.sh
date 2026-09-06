cat << 'INNER_EOF' > app/src/main/java/com/example/data/repository/PhoneReportRepository.kt
package com.example.data.repository

import com.example.data.local.AmanPhoneDatabase
import com.example.data.model.AppUser
import com.example.data.model.PhoneReport
import com.example.data.model.UrgentAlert
import com.example.util.CloudSyncManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class PhoneReportRepository(
    private val db: AmanPhoneDatabase,
    val cloudSyncManager: CloudSyncManager
) {
    private val dao = db.reportDao()
    private val alertDao = db.alertDao()
    private val userDao = db.userDao()

    val allReports: Flow<List<PhoneReport>> = dao.getAllReports()
    val myReports: Flow<List<PhoneReport>> = dao.getMyReports()
    val stolenReports: Flow<List<PhoneReport>> = dao.getStolenReports()
    val recoveredReports: Flow<List<PhoneReport>> = dao.getRecoveredReports()
    
    val allAlerts: Flow<List<UrgentAlert>> = alertDao.getAllAlerts()

    suspend fun submitReport(report: PhoneReport) {
        dao.insertReport(report)
        cloudSyncManager.syncLocalReportsToCloud(listOf(report))
    }

    suspend fun updateReportStatus(reportId: Long, newStatus: String, phoneModel: String, gov: String) {
        dao.updateReportStatus(reportId, newStatus)
        // Also update cloud if necessary
    }

    suspend fun deleteReportLocal(report: PhoneReport) {
        dao.deleteReport(report)
    }

    suspend fun checkImei(imei: String): Pair<PhoneReport?, Boolean> {
        val foundLocally = dao.searchByImeiDirect(imei)
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
    
    suspend fun syncNow(forceCloud: Boolean) {
        if (forceCloud) {
            val cloudReports = cloudSyncManager.fetchCloudReports()
            cloudReports.forEach { dao.insertReport(it) }
        } else {
            val local = dao.getAllReports().first()
            cloudSyncManager.syncLocalReportsToCloud(local)
        }
    }
    
    // Alerts
    suspend fun markAlertAsRead(alertId: Long) {
        alertDao.markAsRead(alertId)
    }

    suspend fun markAllAlertsAsRead() {
        alertDao.markAllAsRead()
    }
}
INNER_EOF

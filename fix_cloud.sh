cat << 'INNER_EOF' > app/src/main/java/com/example/util/CloudSyncManager.kt
package com.example.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.data.local.AmanPhoneDatabase
import com.example.data.model.AppUser
import com.example.data.model.PhoneReport
import com.example.data.model.UrgentAlert
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CloudSyncManager(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()
    private val TAG = "CloudSyncManager"

    suspend fun uploadProofImage(uri: Uri): String? {
        return "https://mock-image-url.com/proof.jpg"
    }

    suspend fun fetchAllUsersFromCloud(): List<AppUser> = withContext(Dispatchers.IO) {
        return@withContext emptyList()
    }

    suspend fun updateUserBanStatusInCloud(email: String, isBanned: Boolean): Boolean = withContext(Dispatchers.IO) {
        return@withContext true
    }

    suspend fun deleteReportInCloud(imei: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext true
    }

    suspend fun publishUrgentAlert(title: String, message: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext true
    }

    suspend fun searchImeiInCloud(imei: String): PhoneReport? = withContext(Dispatchers.IO) {
        try {
            val doc = db.collection("phone_reports").document(imei).get().await()
            if (doc.exists()) {
                val reward = try { doc.getLong("rewardAmount") ?: 0L } catch(e: Exception) { (doc.getString("rewardAmount")?.toLongOrNull()) ?: 0L }
                val incident = try { doc.getLong("incidentDate") ?: System.currentTimeMillis() } catch(e: Exception) { System.currentTimeMillis() }
                return@withContext PhoneReport(
                    imei1 = doc.getString("imei1") ?: "",
                    imei2 = doc.getString("imei2") ?: "",
                    brand = doc.getString("brand") ?: "",
                    modelName = doc.getString("modelName") ?: "",
                    color = doc.getString("color") ?: "",
                    status = doc.getString("status") ?: "مفقود",
                    contactPhone = doc.getString("contactPhone") ?: "",
                    whatsappNumber = doc.getString("whatsappNumber") ?: "",
                    ownerName = doc.getString("ownerName") ?: "",
                    governorate = doc.getString("governorate") ?: "",
                    district = doc.getString("district") ?: "",
                    incidentDate = incident,
                    description = doc.getString("description") ?: "",
                    rewardAmount = reward,
                    isUrgent = doc.getBoolean("isUrgent") ?: true,
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                    proofImageUrl = doc.getString("proofImageUrl") ?: ""
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error fetching report: ${e.message}")
        }
        return@withContext null
    }

    suspend fun syncLocalReportsToCloud(localReports: List<PhoneReport>) = withContext(Dispatchers.IO) {
        for (report in localReports) {
            try {
                val reportMap = hashMapOf(
                    "imei1" to report.imei1,
                    "imei2" to report.imei2,
                    "brand" to report.brand,
                    "modelName" to report.modelName,
                    "color" to report.color,
                    "status" to report.status,
                    "contactPhone" to report.contactPhone,
                    "whatsappNumber" to report.whatsappNumber,
                    "ownerName" to report.ownerName,
                    "governorate" to report.governorate,
                    "district" to report.district,
                    "incidentDate" to report.incidentDate,
                    "description" to report.description,
                    "rewardAmount" to report.rewardAmount,
                    "isUrgent" to report.isUrgent,
                    "createdAt" to report.createdAt,
                    "proofImageUrl" to report.proofImageUrl
                )
                db.collection("phone_reports").document(report.imei1).set(reportMap).await()
            } catch (e: Exception) {
                Log.w(TAG, "Error syncing report to cloud: ${e.message}")
            }
        }
    }

    suspend fun fetchCloudReports(): List<PhoneReport> = withContext(Dispatchers.IO) {
        val reports = mutableListOf<PhoneReport>()
        try {
            val snapshot = db.collection("phone_reports").get().await()
            for (doc in snapshot.documents) {
                val reward = try { doc.getLong("rewardAmount") ?: 0L } catch(e: Exception) { (doc.getString("rewardAmount")?.toLongOrNull()) ?: 0L }
                val incident = try { doc.getLong("incidentDate") ?: System.currentTimeMillis() } catch(e: Exception) { System.currentTimeMillis() }
                reports.add(
                    PhoneReport(
                        imei1 = doc.getString("imei1") ?: "",
                        imei2 = doc.getString("imei2") ?: "",
                        brand = doc.getString("brand") ?: "",
                        modelName = doc.getString("modelName") ?: "",
                        color = doc.getString("color") ?: "",
                        status = doc.getString("status") ?: "مفقود",
                        contactPhone = doc.getString("contactPhone") ?: "",
                        whatsappNumber = doc.getString("whatsappNumber") ?: "",
                        ownerName = doc.getString("ownerName") ?: "",
                        governorate = doc.getString("governorate") ?: "",
                        district = doc.getString("district") ?: "",
                        incidentDate = incident,
                        description = doc.getString("description") ?: "",
                        rewardAmount = reward,
                        isUrgent = doc.getBoolean("isUrgent") ?: true,
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                        proofImageUrl = doc.getString("proofImageUrl") ?: ""
                    )
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error fetching reports: ${e.message}")
        }
        reports
    }
}
INNER_EOF

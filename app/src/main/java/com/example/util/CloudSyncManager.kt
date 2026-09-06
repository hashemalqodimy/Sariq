package com.example.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.data.model.AppUser
import com.example.data.model.PhoneReport
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CloudSyncManager(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()
    private val TAG = "CloudSyncManager"

    suspend fun uploadProofImage(uri: Uri): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"
            val fileName = "${UUID.randomUUID()}.jpg"
            val ref = FirebaseStorage.getInstance().reference.child("proofs/$uid/$fileName")
            ref.putFile(uri).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.w(TAG, "Upload failed: ${e.message}")
            null
        }
    }

    suspend fun fetchAllUsersFromCloud(): List<AppUser> = withContext(Dispatchers.IO) {
        return@withContext emptyList()
    }

    suspend fun checkBanStatus(email: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val doc = db.collection("users").document(email).get().await()
            return@withContext doc.getBoolean("isBanned") ?: false
        } catch (e: Exception) { return@withContext false }
    }

    suspend fun updateUserBanStatusInCloud(email: String, isBanned: Boolean): Boolean = withContext(Dispatchers.IO) {
        return@withContext true
    }

    suspend fun deleteReportInCloud(imei: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext true
    }

    suspend fun publishUrgentAlert(title: String, message: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val alertData = hashMapOf(
                "title" to title,
                "message" to message,
                "governorate" to "اليمن",
                "phoneModel" to "تعميم إداري",
                "timestamp" to System.currentTimeMillis()
            )
            db.collection("urgent_alerts").add(alertData).await()
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error publishing alert", e)
            return@withContext false
        }
    }

    suspend fun searchImeiInCloud(imei: String): PhoneReport? = withContext(Dispatchers.IO) {
        try {
            val doc = db.collection("phone_reports").document(imei).get().await()
            if (doc.exists()) {
                val reward = doc.getLong("rewardAmount") ?: 0L
                val incident = doc.getString("incidentDate") ?: ""
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
                        userEmail = doc.getString("userEmail") ?: "",
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
                    "userEmail" to report.userEmail,
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
                val reward = doc.getLong("rewardAmount") ?: 0L
                val incident = doc.getString("incidentDate") ?: ""
                reports.add(
                    PhoneReport(
                        imei1 = doc.getString("imei1") ?: "",
                        imei2 = doc.getString("imei2") ?: "",
                        brand = doc.getString("brand") ?: "",
                        modelName = doc.getString("modelName") ?: "",
                        color = doc.getString("color") ?: "",
                        status = doc.getString("status") ?: "مفقود",
                        contactPhone = doc.getString("contactPhone") ?: "",
                        ownerName = doc.getString("ownerName") ?: "",
                        governorate = doc.getString("governorate") ?: "",
                        district = doc.getString("district") ?: "",
                        incidentDate = incident,
                        description = doc.getString("description") ?: "",
                        rewardAmount = reward,
                        isUrgent = doc.getBoolean("isUrgent") ?: true,
                        userEmail = doc.getString("userEmail") ?: "",
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

    suspend fun fetchCloudAlerts(): List<com.example.data.model.UrgentAlert> = withContext(Dispatchers.IO) {
        val alerts = mutableListOf<com.example.data.model.UrgentAlert>()
        try {
            val snapshot = db.collection("urgent_alerts").get().await()
            for (doc in snapshot.documents) {
                val title = doc.getString("title") ?: ""
                val message = doc.getString("message") ?: ""
                val gov = doc.getString("governorate") ?: "اليمن"
                val model = doc.getString("phoneModel") ?: "تعميم إداري"
                val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                
                alerts.add(
                    com.example.data.model.UrgentAlert(
                        id = 0L,
                        reportId = 0L,
                        title = title,
                        message = message,
                        governorate = gov,
                        phoneModel = model,
                        imeiSnippet = "",
                        timestamp = timestamp,
                        isRead = false,
                        severity = "CRITICAL"
                    )
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error fetching alerts: ${e.message}")
        }
        alerts
    }
}

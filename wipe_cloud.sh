cat << 'INNER_EOF' > app/src/main/java/com/example/util/CloudSyncManager.kt
package com.example.util

import android.content.Context
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
import org.json.JSONObject

class CloudSyncManager(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()
    private val TAG = "CloudSyncManager"

    suspend fun syncLocalReportsToCloud(localReports: List<PhoneReport>) = withContext(Dispatchers.IO) {
        for (report in localReports) {
            try {
                // Simplified payload mapping based on actual PhoneReport fields
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
                        incidentDate = doc.getLong("incidentDate") ?: System.currentTimeMillis(),
                        description = doc.getString("description") ?: "",
                        rewardAmount = doc.getLong("rewardAmount") ?: 0L,
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

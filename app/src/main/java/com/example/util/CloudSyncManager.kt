package com.example.util

import android.content.Context
import android.util.Log
import com.example.data.model.PhoneReport
import com.example.data.model.UrgentAlert
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Manages real-time cloud synchronization of phone reports and urgent alerts
 * across all Yemeni users and phone shops using Firebase Firestore.
 */
class CloudSyncManager(private val context: Context) {

    private val firestore: FirebaseFirestore? by lazy {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseFirestore.getInstance()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w("CloudSyncManager", "Firestore not available: ${e.message}")
            null
        }
    }

    val isCloudConnected: Boolean
        get() = firestore != null

    /**
     * Publishes a phone report to the centralized cloud database so all users
     * across Yemen instantly receive it.
     */
    suspend fun publishReportToCloud(report: PhoneReport): Boolean {
        val db = firestore ?: return false
        return try {
            val reportMap = hashMapOf(
                "brand" to report.brand,
                "modelName" to report.modelName,
                "imei1" to report.imei1,
                "imei2" to report.imei2,
                "serialNumber" to report.serialNumber,
                "color" to report.color,
                "storageCapacity" to report.storageCapacity,
                "governorate" to report.governorate,
                "district" to report.district,
                "incidentDate" to report.incidentDate,
                "description" to report.description,
                "distinctiveFeatures" to report.distinctiveFeatures,
                "ownerName" to report.ownerName,
                "contactPhone" to report.contactPhone,
                "whatsappNumber" to report.whatsappNumber,
                "policeStation" to report.policeStation,
                "rewardAmount" to report.rewardAmount,
                "status" to report.status,
                "createdAt" to report.createdAt,
                "isUrgent" to report.isUrgent
            )

            // Save to 'phone_reports' collection
            val docRef = db.collection("phone_reports").add(reportMap).await()

            // Also publish an urgent broadcast alert in the cloud collection
            val alertMap = hashMapOf(
                "cloudDocId" to docRef.id,
                "title" to "تعميم سرقة عاجل - ${report.governorate}",
                "message" to "تم الإبلاغ عن ${report.status} هاتف ${report.brand} ${report.modelName} في ${report.district} بمحافظة ${report.governorate}. مكافأة: ${if (report.rewardAmount > 0) "${report.rewardAmount} ريال" else "متاحة"}",
                "governorate" to report.governorate,
                "phoneModel" to "${report.brand} ${report.modelName}",
                "imeiSnippet" to if (report.imei1.length >= 6) "${report.imei1.take(6)}...${report.imei1.takeLast(3)}" else report.imei1,
                "timestamp" to System.currentTimeMillis(),
                "severity" to if (report.status == "مسروق") "CRITICAL" else "WARNING"
            )
            db.collection("urgent_broadcasts").add(alertMap).await()

            true
        } catch (e: Exception) {
            Log.w("CloudSyncManager", "Failed to push report to cloud: ${e.message}")
            false
        }
    }

    /**
     * Updates report status (e.g. 'تم الاسترجاع') in the cloud database.
     */
    suspend fun updateReportStatusInCloud(imei: String, newStatus: String): Boolean {
        val db = firestore ?: return false
        return try {
            val querySnapshot = db.collection("phone_reports")
                .whereEqualTo("imei1", imei)
                .get()
                .await()

            for (doc in querySnapshot.documents) {
                doc.reference.update("status", newStatus).await()
            }
            true
        } catch (e: Exception) {
            Log.w("CloudSyncManager", "Failed to update cloud report status: ${e.message}")
            false
        }
    }

    /**
     * Real-time listener for reports added or modified anywhere in Yemen by other users.
     */
    fun observeCloudReports(): Flow<List<PhoneReport>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        var registration: ListenerRegistration? = null
        try {
            registration = db.collection("phone_reports")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(100)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("CloudSyncManager", "Listen failed: ${error.message}")
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val reports = snapshot.documents.mapNotNull { doc ->
                            parseDocToReport(doc)
                        }
                        trySend(reports)
                    }
                }
        } catch (e: Exception) {
            Log.w("CloudSyncManager", "Error setting up cloud listener: ${e.message}")
            trySend(emptyList())
        }

        awaitClose {
            registration?.remove()
        }
    }

    /**
     * Real-time listener for urgent broadcasts to notify this device immediately
     * when a report is registered from another city or phone shop.
     */
    fun observeCloudAlerts(): Flow<List<UrgentAlert>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        var registration: ListenerRegistration? = null
        try {
            registration = db.collection("urgent_broadcasts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("CloudSyncManager", "Alerts listen failed: ${error.message}")
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val alerts = snapshot.documents.mapNotNull { doc ->
                            parseDocToAlert(doc)
                        }
                        trySend(alerts)
                    }
                }
        } catch (e: Exception) {
            Log.w("CloudSyncManager", "Error setting up cloud alerts listener: ${e.message}")
            trySend(emptyList())
        }

        awaitClose {
            registration?.remove()
        }
    }

    /**
     * Searches cloud database directly for an IMEI if not found locally.
     */
    suspend fun searchImeiInCloud(imei: String): PhoneReport? {
        val db = firestore ?: return null
        return try {
            val snapshot = db.collection("phone_reports")
                .whereEqualTo("imei1", imei)
                .limit(1)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                parseDocToReport(snapshot.documents.first())
            } else {
                val snapshot2 = db.collection("phone_reports")
                    .whereEqualTo("imei2", imei)
                    .limit(1)
                    .get()
                    .await()
                if (!snapshot2.isEmpty) {
                    parseDocToReport(snapshot2.documents.first())
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.w("CloudSyncManager", "Error checking IMEI in cloud: ${e.message}")
            null
        }
    }

    private fun parseDocToReport(doc: DocumentSnapshot): PhoneReport? {
        return try {
            PhoneReport(
                id = 0L, // Handled or assigned locally upon sync
                brand = doc.getString("brand") ?: "جهاز",
                modelName = doc.getString("modelName") ?: "",
                imei1 = doc.getString("imei1") ?: "",
                imei2 = doc.getString("imei2") ?: "",
                serialNumber = doc.getString("serialNumber") ?: "",
                color = doc.getString("color") ?: "",
                storageCapacity = doc.getString("storageCapacity") ?: "",
                governorate = doc.getString("governorate") ?: "",
                district = doc.getString("district") ?: "",
                incidentDate = doc.getString("incidentDate") ?: "",
                description = doc.getString("description") ?: "",
                distinctiveFeatures = doc.getString("distinctiveFeatures") ?: "",
                ownerName = doc.getString("ownerName") ?: "",
                contactPhone = doc.getString("contactPhone") ?: "",
                whatsappNumber = doc.getString("whatsappNumber") ?: "",
                policeStation = doc.getString("policeStation") ?: "",
                rewardAmount = doc.getLong("rewardAmount") ?: 0L,
                status = doc.getString("status") ?: "مسروق",
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                isUrgent = doc.getBoolean("isUrgent") ?: true
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parseDocToAlert(doc: DocumentSnapshot): UrgentAlert? {
        return try {
            UrgentAlert(
                id = 0L,
                reportId = 0L,
                title = doc.getString("title") ?: "تعميم عاجل",
                message = doc.getString("message") ?: "",
                governorate = doc.getString("governorate") ?: "اليمن",
                phoneModel = doc.getString("phoneModel") ?: "هاتف",
                imeiSnippet = doc.getString("imeiSnippet") ?: "",
                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                isRead = false,
                severity = doc.getString("severity") ?: "CRITICAL"
            )
        } catch (e: Exception) {
            null
        }
    }
}

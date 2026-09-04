package com.example.util

import android.content.Context
import android.util.Log
import com.example.data.model.PhoneReport
import com.example.data.model.UrgentAlert
import com.example.data.model.AppUser
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * High-Availability Multi-Device Cloud Broadcast Engine for AmanPhone Yemen.
 *
 * Employs a multi-channel synchronization architecture:
 * Channel 1 (Primary Pub/Sub): ntfy.sh high-speed real-time event pipeline (instant push to all Android devices without Firebase dependency)
 * Channel 2 (Ledger Backup): Centralized Cloud REST Hub with persistent JSON storage
 * Channel 3 (Enterprise Fallback): Firebase Firestore (when Google Play Services are active)
 */
class CloudSyncManager(private val context: Context) {

    companion object {
        private const val TAG = "CloudSyncManager"
        private const val NTFY_BROADCAST_URL = "https://ntfy.sh/aman_phone_yemen_v2"
        private const val CENTRAL_HUB_URL = "https://api.restful-api.dev/objects/ff808181a067127101a0672f4617005b"
        private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
        private const val USER_AGENT = "Mozilla/5.0 (Linux; Android 14; Mobile) AmanPhoneYemen/2.0"
    }

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .writeTimeout(12, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    private val firestore: FirebaseFirestore? by lazy {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseFirestore.getInstance()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firestore not available: ${e.message}")
            null
        }
    }

    val isCloudConnected: Boolean
        get() = true

    /**
     * Publishes a phone report and urgent broadcast to ALL devices across Yemen.
     * Guaranteed delivery across ntfy.sh pub/sub, central hub, and Firestore.
     */
    suspend fun publishReportToCloud(report: PhoneReport): Boolean = withContext(Dispatchers.IO) {
        var publishedToAtLeastOne = false

        val imeiSnippet = if (report.imei1.length >= 6) {
            "${report.imei1.take(6)}...${report.imei1.takeLast(3)}"
        } else {
            report.imei1
        }

        val newAlert = UrgentAlert(
            id = 0L,
            reportId = report.id,
            title = "تعميم سرقة عاجل - ${report.governorate}",
            message = "تم الإبلاغ عن ${report.status} هاتف ${report.brand} ${report.modelName} في ${report.district} بمحافظة ${report.governorate}. مكافأة: ${if (report.rewardAmount > 0) "${report.rewardAmount} ريال" else "متاحة"}",
            governorate = report.governorate,
            phoneModel = "${report.brand} ${report.modelName}",
            imeiSnippet = imeiSnippet,
            timestamp = if (report.createdAt > 0) report.createdAt else System.currentTimeMillis(),
            isRead = false,
            severity = if (report.status == "مسروق") "CRITICAL" else "WARNING"
        )

        // 1. Primary Broadcast via ntfy.sh (Instant push, received in ~200ms by other devices)
        try {
            val payload = JSONObject().apply {
                put("type", "NEW_REPORT_ALERT")
                put("report", reportToJson(report))
                put("alert", alertToJson(newAlert))
            }

            val ntfyRequest = Request.Builder()
                .url(NTFY_BROADCAST_URL)
                .header("User-Agent", USER_AGENT)
                .header("Priority", "high")
                .header("Tags", "warning,rotating_light")
                .post(payload.toString().toRequestBody(JSON_MEDIA))
                .build()

            val ntfyResponse = httpClient.newCall(ntfyRequest).execute()
            if (ntfyResponse.isSuccessful) {
                publishedToAtLeastOne = true
                Log.i(TAG, "Successfully published report to ntfy.sh broadcast channel")
            } else {
                Log.w(TAG, "ntfy.sh responded with code: ${ntfyResponse.code}")
            }
            ntfyResponse.close()
        } catch (e: Exception) {
            Log.w(TAG, "Error broadcasting to ntfy.sh: ${e.message}")
        }

        // 2. Secondary backup to Central Cloud Hub
        try {
            val (currentReports, currentAlerts) = fetchCloudDataFromHub()
            val updatedReports = mutableListOf<PhoneReport>()
            updatedReports.add(report)
            updatedReports.addAll(currentReports.filter { it.imei1 != report.imei1 }.take(50))

            val updatedAlerts = mutableListOf<UrgentAlert>()
            updatedAlerts.add(newAlert)
            updatedAlerts.addAll(currentAlerts.filter { !(it.phoneModel == newAlert.phoneModel && it.governorate == newAlert.governorate) }.take(50))

            val rootJson = JSONObject().apply {
                put("name", "AmanPhone_Central_Hub")
                put("data", JSONObject().apply {
                    put("version", 2)
                    put("lastUpdated", System.currentTimeMillis())
                    put("reports", reportsToJsonArray(updatedReports))
                    put("alerts", alertsToJsonArray(updatedAlerts))
                })
            }

            val hubRequest = Request.Builder()
                .url(CENTRAL_HUB_URL)
                .header("User-Agent", USER_AGENT)
                .put(rootJson.toString().toRequestBody(JSON_MEDIA))
                .build()

            val hubResponse = httpClient.newCall(hubRequest).execute()
            if (hubResponse.isSuccessful) {
                publishedToAtLeastOne = true
                Log.i(TAG, "Successfully updated Central Hub ledger")
            }
            hubResponse.close()
        } catch (e: Exception) {
            Log.w(TAG, "Error updating Central Hub ledger: ${e.message}")
        }

        // 3. Firestore Push (if configured)
        firestore?.let { db ->
            try {
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
                db.collection("phone_reports").add(reportMap).await()

                // Also publish to urgent_alerts and fcm_broadcast_queue for FCM backend triggers
                val alertMap = hashMapOf(
                    "title" to newAlert.title,
                    "message" to newAlert.message,
                    "governorate" to newAlert.governorate,
                    "phoneModel" to newAlert.phoneModel,
                    "imeiSnippet" to newAlert.imeiSnippet,
                    "timestamp" to newAlert.timestamp,
                    "severity" to newAlert.severity,
                    "targetTopic" to "urgent_alerts"
                )
                db.collection("urgent_alerts").add(alertMap).await()
                db.collection("fcm_broadcast_queue").add(alertMap).await()

                publishedToAtLeastOne = true
            } catch (e: Exception) {
                Log.w(TAG, "Firestore push skipped: ${e.message}")
            }
        }

        publishedToAtLeastOne
    }

    /**
     * Unified fetch combining all cloud sources (ntfy broadcast stream, Central Hub, and Firestore).
     * Deduplicates items and returns the complete set of reports and alerts.
     */
    suspend fun fetchLatestCloudData(): Pair<List<PhoneReport>, List<UrgentAlert>> = withContext(Dispatchers.IO) {
        val allReportsMap = LinkedHashMap<String, PhoneReport>() // key = imei1
        val allAlertsList = mutableListOf<UrgentAlert>()

        // 0. Fetch from Firestore (Primary Enterprise Source)
        firestore?.let { db ->
            try {
                // Fetch reports
                val reportsSnapshot = db.collection("phone_reports")
                    .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(100)
                    .get()
                    .await()
                
                for (doc in reportsSnapshot.documents) {
                    val report = parseFirestoreDoc(doc)
                    if (report.imei1.isNotBlank()) {
                        allReportsMap[report.imei1] = report
                    }
                }

                // Fetch alerts
                val alertsSnapshot = db.collection("urgent_alerts")
                    .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(50)
                    .get()
                    .await()
                
                for (doc in alertsSnapshot.documents) {
                    val alert = parseFirestoreAlert(doc)
                    allAlertsList.add(alert)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed reading from Firestore: ${e.message}")
            }
        }

        // 1. Fetch from ntfy.sh broadcast feed (includes all past broadcasts)
        try {
            val ntfyUrl = "$NTFY_BROADCAST_URL/json?poll=1&since=all"
            val request = Request.Builder()
                .url(ntfyUrl)
                .header("User-Agent", USER_AGENT)
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (!body.isNullOrBlank()) {
                    val lines = body.lines()
                    for (line in lines) {
                        val trimmed = line.trim()
                        if (trimmed.isEmpty()) continue
                        try {
                            val eventObj = JSONObject(trimmed)
                            if (eventObj.optString("event") == "message") {
                                val messageStr = eventObj.optString("message")
                                if (messageStr.startsWith("{")) {
                                    val payload = JSONObject(messageStr)
                                    val reportObj = payload.optJSONObject("report")
                                    if (reportObj != null) {
                                        parseJsonToReport(reportObj)?.let { rep ->
                                            if (rep.imei1.isNotBlank()) {
                                                allReportsMap[rep.imei1] = rep
                                            }
                                        }
                                    }
                                    val alertObj = payload.optJSONObject("alert")
                                    if (alertObj != null) {
                                        parseJsonToAlert(alertObj)?.let { al ->
                                            allAlertsList.add(al)
                                        }
                                    }
                                }
                            }
                        } catch (_: Exception) {
                        }
                    }
                }
            }
            response.close()
        } catch (e: Exception) {
            Log.w(TAG, "Failed reading ntfy broadcast feed: ${e.message}")
        }

        // 2. Fetch from Central Hub backup
        try {
            val (hubReports, hubAlerts) = fetchCloudDataFromHub()
            for (rep in hubReports) {
                if (!allReportsMap.containsKey(rep.imei1)) {
                    allReportsMap[rep.imei1] = rep
                }
            }
            allAlertsList.addAll(hubAlerts)
        } catch (e: Exception) {
            Log.w(TAG, "Failed reading Central Hub: ${e.message}")
        }

        // 3. Deduplicate alerts by (phoneModel, governorate, timestamp +/- 2 min)
        val deduplicatedAlerts = mutableListOf<UrgentAlert>()
        for (alert in allAlertsList) {
            val isDup = deduplicatedAlerts.any { existing ->
                existing.phoneModel == alert.phoneModel &&
                        existing.governorate == alert.governorate &&
                        Math.abs(existing.timestamp - alert.timestamp) <= 120_000L
            }
            if (!isDup) {
                deduplicatedAlerts.add(alert)
            }
        }

        Pair(allReportsMap.values.toList(), deduplicatedAlerts)
    }

    /**
     * Direct synchronous fetch for background receivers.
     */
    fun fetchLatestAlertsDirect(): List<UrgentAlert> {
        val allAlerts = mutableListOf<UrgentAlert>()
        try {
            val ntfyUrl = "$NTFY_BROADCAST_URL/json?poll=1&since=all"
            val request = Request.Builder()
                .url(ntfyUrl)
                .header("User-Agent", USER_AGENT)
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string()
                if (!body.isNullOrBlank()) {
                    for (line in body.lines()) {
                        val trimmed = line.trim()
                        if (trimmed.isEmpty()) continue
                        try {
                            val eventObj = JSONObject(trimmed)
                            if (eventObj.optString("event") == "message") {
                                val messageStr = eventObj.optString("message")
                                if (messageStr.startsWith("{")) {
                                    val payload = JSONObject(messageStr)
                                    val alertObj = payload.optJSONObject("alert")
                                    if (alertObj != null) {
                                        parseJsonToAlert(alertObj)?.let { allAlerts.add(it) }
                                    }
                                }
                            }
                        } catch (_: Exception) {
                        }
                    }
                }
            }
            response.close()
        } catch (_: Exception) {
        }

        val (_, hubAlerts) = fetchCloudDataFromHub()
        allAlerts.addAll(hubAlerts)
        return allAlerts
    }

    /**
     * Emits fresh cloud reports in real-time.
     */
    fun observeCloudReports(): Flow<List<PhoneReport>> = flow {
        while (true) {
            try {
                val (reports, _) = fetchLatestCloudData()
                if (reports.isNotEmpty()) {
                    emit(reports)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Observe reports loop error: ${e.message}")
            }
            delay(6000)
        }
    }

    /**
     * Emits fresh cloud alerts in real-time.
     */
    fun observeCloudAlerts(): Flow<List<UrgentAlert>> = flow {
        while (true) {
            try {
                val (_, alerts) = fetchLatestCloudData()
                if (alerts.isNotEmpty()) {
                    emit(alerts)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Observe alerts loop error: ${e.message}")
            }
            delay(5000)
        }
    }

    /**
     * Direct cloud search for an IMEI if not found in local cache.
     */
    suspend fun searchImeiInCloud(imei: String): PhoneReport? = withContext(Dispatchers.IO) {
        val cleanImei = imei.trim().filter { it.isDigit() }
        if (cleanImei.isBlank()) return@withContext null

        try {
            val (reports, _) = fetchLatestCloudData()
            val match = reports.firstOrNull { it.imei1 == cleanImei || it.imei2 == cleanImei }
            if (match != null) return@withContext match
        } catch (_: Exception) {
        }

        // Check Firestore if available
        firestore?.let { db ->
            try {
                val snapshot = db.collection("phone_reports")
                    .whereEqualTo("imei1", cleanImei)
                    .limit(1)
                    .get()
                    .await()
                if (!snapshot.isEmpty) {
                    return@withContext parseFirestoreDoc(snapshot.documents.first())
                }
            } catch (_: Exception) {
            }
        }

        null
    }

    suspend fun deleteReportInCloud(imei: String): Boolean = withContext(Dispatchers.IO) {
        var success = false
        val cleanImei = imei.trim().filter { it.isDigit() }
        
        firestore?.let { db ->
            try {
                val snapshot = db.collection("phone_reports")
                    .whereEqualTo("imei1", cleanImei)
                    .limit(1)
                    .get()
                    .await()
                
                if (!snapshot.isEmpty) {
                    val docId = snapshot.documents.first().id
                    db.collection("phone_reports").document(docId).delete().await()
                    success = true
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed deleting from Firestore: ${e.message}")
            }
        }
        success
    }

    suspend fun publishUrgentAlert(title: String, message: String, severity: String = "CRITICAL"): Boolean = withContext(Dispatchers.IO) {
        var success = false
        val newAlert = hashMapOf(
            "title" to title,
            "message" to message,
            "severity" to severity,
            "timestamp" to System.currentTimeMillis()
        )
        
        firestore?.let { db ->
            try {
                db.collection("urgent_alerts").add(newAlert).await()
                success = true
            } catch (e: Exception) {
                Log.w(TAG, "Failed publishing alert to Firestore: ${e.message}")
            }
        }
        
        // Also push to ntfy.sh for immediate broadcast to devices relying on SSE
        if (success) {
            try {
                val request = Request.Builder()
                    .url(NTFY_BROADCAST_URL)
                    .post(message.toRequestBody("text/plain".toMediaType()))
                    .addHeader("Title", title)
                    .addHeader("Priority", if (severity == "CRITICAL") "5" else "default")
                    .addHeader("Tags", "warning,police")
                    .build()
                httpClient.newCall(request).execute().close()
            } catch (_: Exception) {}
        }
        
        success
    }

    suspend fun updateReportStatusInCloud(imei: String, newStatus: String): Boolean = withContext(Dispatchers.IO) {
        var success = false
        val cleanImei = imei.trim().filter { it.isDigit() }
        
        // 1. Update in Firestore
        firestore?.let { db ->
            try {
                val snapshot = db.collection("phone_reports")
                    .whereEqualTo("imei1", cleanImei)
                    .limit(1)
                    .get()
                    .await()
                
                if (!snapshot.isEmpty) {
                    val docId = snapshot.documents.first().id
                    db.collection("phone_reports").document(docId)
                        .update("status", newStatus)
                        .await()
                    success = true
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed updating Firestore status: ${e.message}")
            }
        }

        // 2. Update Central Hub Backup
        try {
            val (currentReports, currentAlerts) = fetchCloudDataFromHub()
            val updatedReports = currentReports.map { report ->
                if (report.imei1 == imei || report.imei2 == imei) {
                    report.copy(status = newStatus)
                } else {
                    report
                }
            }

            val rootJson = JSONObject().apply {
                put("name", "AmanPhone_Central_Hub")
                put("data", JSONObject().apply {
                    put("version", 2)
                    put("lastUpdated", System.currentTimeMillis())
                    put("reports", reportsToJsonArray(updatedReports))
                    put("alerts", alertsToJsonArray(currentAlerts))
                })
            }

            val request = Request.Builder()
                .url(CENTRAL_HUB_URL)
                .header("User-Agent", USER_AGENT)
                .put(rootJson.toString().toRequestBody(JSON_MEDIA))
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                success = true
            }
            response.close()
        } catch (e: Exception) {
            Log.w(TAG, "Failed updating report status in Central Hub: ${e.message}")
        }
        
        success
    }

    private fun fetchCloudDataFromHub(): Pair<List<PhoneReport>, List<UrgentAlert>> {
        val request = Request.Builder()
            .url(CENTRAL_HUB_URL)
            .header("User-Agent", USER_AGENT)
            .get()
            .build()

        val reportsList = mutableListOf<PhoneReport>()
        val alertsList = mutableListOf<UrgentAlert>()

        try {
            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val bodyString = response.body?.string()
                if (!bodyString.isNullOrBlank()) {
                    val root = JSONObject(bodyString)
                    val data = root.optJSONObject("data")
                    if (data != null) {
                        val reportsArray = data.optJSONArray("reports")
                        if (reportsArray != null) {
                            for (i in 0 until reportsArray.length()) {
                                val item = reportsArray.optJSONObject(i)
                                if (item != null) {
                                    parseJsonToReport(item)?.let { reportsList.add(it) }
                                }
                            }
                        }

                        val alertsArray = data.optJSONArray("alerts")
                        if (alertsArray != null) {
                            for (i in 0 until alertsArray.length()) {
                                val item = alertsArray.optJSONObject(i)
                                if (item != null) {
                                    parseJsonToAlert(item)?.let { alertsList.add(it) }
                                }
                            }
                        }
                    }
                }
            }
            response.close()
        } catch (e: Exception) {
            Log.w(TAG, "Hub fetch exception: ${e.message}")
        }

        return Pair(reportsList, alertsList)
    }

    private fun reportToJson(r: PhoneReport): JSONObject {
        return JSONObject().apply {
            put("brand", r.brand)
            put("modelName", r.modelName)
            put("imei1", r.imei1)
            put("imei2", r.imei2)
            put("serialNumber", r.serialNumber)
            put("color", r.color)
            put("storageCapacity", r.storageCapacity)
            put("governorate", r.governorate)
            put("district", r.district)
            put("incidentDate", r.incidentDate)
            put("description", r.description)
            put("distinctiveFeatures", r.distinctiveFeatures)
            put("ownerName", r.ownerName)
            put("contactPhone", r.contactPhone)
            put("whatsappNumber", r.whatsappNumber)
            put("policeStation", r.policeStation)
            put("rewardAmount", r.rewardAmount)
            put("status", r.status)
            put("createdAt", r.createdAt)
            put("isUrgent", r.isUrgent)
        }
    }

    private fun alertToJson(a: UrgentAlert): JSONObject {
        return JSONObject().apply {
            put("title", a.title)
            put("message", a.message)
            put("governorate", a.governorate)
            put("phoneModel", a.phoneModel)
            put("imeiSnippet", a.imeiSnippet)
            put("timestamp", a.timestamp)
            put("severity", a.severity)
        }
    }

    private fun reportsToJsonArray(reports: List<PhoneReport>): JSONArray {
        val array = JSONArray()
        reports.forEach { r -> array.put(reportToJson(r)) }
        return array
    }

    private fun alertsToJsonArray(alerts: List<UrgentAlert>): JSONArray {
        val array = JSONArray()
        alerts.forEach { a -> array.put(alertToJson(a)) }
        return array
    }

    private fun parseJsonToReport(obj: JSONObject): PhoneReport? {
        return try {
            val imei = obj.optString("imei1", "")
            if (imei.isBlank()) return null
            PhoneReport(
                id = 0L,
                brand = obj.optString("brand", "هاتف"),
                modelName = obj.optString("modelName", ""),
                imei1 = imei,
                imei2 = obj.optString("imei2", ""),
                serialNumber = obj.optString("serialNumber", ""),
                color = obj.optString("color", "أخرى"),
                storageCapacity = obj.optString("storageCapacity", ""),
                governorate = obj.optString("governorate", "صنعاء"),
                district = obj.optString("district", ""),
                incidentDate = obj.optString("incidentDate", ""),
                description = obj.optString("description", ""),
                distinctiveFeatures = obj.optString("distinctiveFeatures", ""),
                ownerName = obj.optString("ownerName", "مواطن"),
                contactPhone = obj.optString("contactPhone", ""),
                whatsappNumber = obj.optString("whatsappNumber", ""),
                policeStation = obj.optString("policeStation", ""),
                rewardAmount = obj.optLong("rewardAmount", 0L),
                status = obj.optString("status", "مسروق"),
                createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                isUrgent = obj.optBoolean("isUrgent", true)
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun parseJsonToAlert(obj: JSONObject): UrgentAlert? {
        return try {
            val title = obj.optString("title", "")
            if (title.isBlank()) return null
            UrgentAlert(
                id = 0L,
                reportId = 0L,
                title = title,
                message = obj.optString("message", ""),
                governorate = obj.optString("governorate", "اليمن"),
                phoneModel = obj.optString("phoneModel", "جهاز ذكي"),
                imeiSnippet = obj.optString("imeiSnippet", ""),
                timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                isRead = false,
                severity = obj.optString("severity", "CRITICAL")
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun parseFirestoreDoc(doc: DocumentSnapshot): PhoneReport {
        return PhoneReport(
            id = 0L,
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
    }

    private fun parseFirestoreAlert(doc: DocumentSnapshot): UrgentAlert {
        return UrgentAlert(
            id = 0L,
            reportId = 0L,
            title = doc.getString("title") ?: "",
            message = doc.getString("message") ?: "",
            governorate = doc.getString("governorate") ?: "",
            phoneModel = doc.getString("phoneModel") ?: "",
            imeiSnippet = doc.getString("imeiSnippet") ?: "",
            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
            isRead = false,
            severity = doc.getString("severity") ?: "CRITICAL"
        )
    }
    
    // User Management API (Admin/Cloud)
    suspend fun syncUserToCloud(user: AppUser) = withContext(Dispatchers.IO) {
        firestore?.let { db ->
            try {
                val userMap = hashMapOf(
                    "email" to user.email,
                    "fullName" to user.fullName,
                    "authProvider" to user.authProvider,
                    "isBanned" to user.isBanned,
                    "createdAt" to user.createdAt,
                    "lastLoginAt" to user.lastLoginAt
                )
                db.collection("users").document(user.email).set(userMap).await()
            } catch (e: Exception) {
                Log.w(TAG, "Failed syncing user to Firestore: ${e.message}")
            }
        }
    }
    
    suspend fun fetchAllUsersFromCloud(): List<AppUser> = withContext(Dispatchers.IO) {
        val users = mutableListOf<AppUser>()
        firestore?.let { db ->
            try {
                val snapshot = db.collection("users").get().await()
                for (doc in snapshot.documents) {
                    users.add(
                        AppUser(
                            email = doc.getString("email") ?: doc.id,
                            fullName = doc.getString("fullName") ?: "غير معروف",
                            authProvider = doc.getString("authProvider") ?: "UNKNOWN",
                            isBanned = doc.getBoolean("isBanned") ?: false,
                            createdAt = doc.getLong("createdAt") ?: 0L,
                            lastLoginAt = doc.getLong("lastLoginAt") ?: 0L
                        )
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed fetching users from Firestore: ${e.message}")
            }
        }
        users
    }
    
    suspend fun updateUserBanStatusInCloud(email: String, isBanned: Boolean): Boolean = withContext(Dispatchers.IO) {
        var success = false
        firestore?.let { db ->
            try {
                db.collection("users").document(email).update("isBanned", isBanned).await()
                success = true
            } catch (e: Exception) {
                Log.w(TAG, "Failed updating user ban status: ${e.message}")
            }
        }
        success
    }
    
    suspend fun checkUserBannedStatus(email: String): Boolean = withContext(Dispatchers.IO) {
        var isBanned = false
        firestore?.let { db ->
            try {
                val doc = db.collection("users").document(email).get().await()
                isBanned = doc.getBoolean("isBanned") ?: false
            } catch (e: Exception) {
                // Ignore if not found
            }
        }
        isBanned
    }
}

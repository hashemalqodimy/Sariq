package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "urgent_alerts")
data class UrgentAlert(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val reportId: Long,
    val title: String,
    val message: String,
    val governorate: String,
    val phoneModel: String,
    val imeiSnippet: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val severity: String = "CRITICAL" // CRITICAL, WARNING, INFO, RESOLVED
)

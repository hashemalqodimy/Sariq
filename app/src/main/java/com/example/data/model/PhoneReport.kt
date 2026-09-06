package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "phone_reports")
data class PhoneReport(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val brand: String,
    val modelName: String,
    val imei1: String,
    val imei2: String = "",
    val serialNumber: String = "",
    val color: String,
    val storageCapacity: String = "",
    val governorate: String,
    val district: String,
    val incidentDate: String,
    val description: String,
    val distinctiveFeatures: String = "",
    val ownerName: String,
    val contactPhone: String,
    val whatsappNumber: String = "",
    val policeStation: String = "",
    val rewardAmount: Long = 0L,
    val status: String = "مسروق", // مسروق, مفقود, تم الاسترجاع, قيد التحري
    val createdAt: Long = System.currentTimeMillis(),
    val isUrgent: Boolean = true,
    val userEmail: String = "",
    val proofImageUrl: String = ""
)

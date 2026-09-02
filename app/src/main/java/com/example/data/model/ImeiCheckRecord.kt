package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "imei_checks")
data class ImeiCheckRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val imei: String,
    val isStolen: Boolean,
    val phoneModel: String = "",
    val checkedAt: Long = System.currentTimeMillis()
)

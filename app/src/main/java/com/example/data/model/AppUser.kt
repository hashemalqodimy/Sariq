package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_users")
data class AppUser(
    @PrimaryKey
    val email: String,
    val fullName: String,
    val authProvider: String = "EMAIL", // "EMAIL", "GOOGLE"
    val avatarUrl: String = "",
    val phone: String = "",
    val isBanned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis()
)

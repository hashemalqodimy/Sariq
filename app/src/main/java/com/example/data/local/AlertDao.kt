package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.UrgentAlert
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {
    @Query("SELECT * FROM urgent_alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<UrgentAlert>>

    @Query("SELECT COUNT(*) FROM urgent_alerts WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: UrgentAlert): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlerts(alerts: List<UrgentAlert>)

    @Query("UPDATE urgent_alerts SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE urgent_alerts SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM urgent_alerts WHERE id = :id")
    suspend fun deleteAlert(id: Long)
}

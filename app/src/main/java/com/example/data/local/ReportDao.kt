package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PhoneReport
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {
    @Query("SELECT * FROM phone_reports ORDER BY createdAt DESC")
    fun getAllReports(): Flow<List<PhoneReport>>

    @Query("SELECT * FROM phone_reports WHERE governorate = :gov ORDER BY createdAt DESC")
    fun getReportsByGovernorate(gov: String): Flow<List<PhoneReport>>

    @Query("SELECT * FROM phone_reports WHERE status = :status ORDER BY createdAt DESC")
    fun getReportsByStatus(status: String): Flow<List<PhoneReport>>
    @Query("SELECT * FROM phone_reports WHERE userEmail = :email ORDER BY createdAt DESC")
    fun getReportsByUser(email: String): Flow<List<PhoneReport>>

    @Query("SELECT * FROM phone_reports WHERE id = :id")
    fun getReportById(id: Long): Flow<PhoneReport?>

    @Query("""
        SELECT * FROM phone_reports 
        WHERE imei1 LIKE '%' || :query || '%' 
           OR imei2 LIKE '%' || :query || '%' 
           OR modelName LIKE '%' || :query || '%' 
           OR brand LIKE '%' || :query || '%' 
           OR serialNumber LIKE '%' || :query || '%' 
           OR governorate LIKE '%' || :query || '%' 
        ORDER BY createdAt DESC
    """)
    fun searchReports(query: String): Flow<List<PhoneReport>>

    @Query("SELECT * FROM phone_reports WHERE imei1 = :imei OR imei2 = :imei LIMIT 1")
    suspend fun findReportByExactImei(imei: String): PhoneReport?

    @Query("SELECT COUNT(*) FROM phone_reports")
    fun getTotalReportsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM phone_reports WHERE status = 'تم الاسترجاع'")
    fun getRecoveredReportsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: PhoneReport): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReports(reports: List<PhoneReport>)

    @Update
    suspend fun updateReport(report: PhoneReport)

    @Query("UPDATE phone_reports SET status = :newStatus WHERE id = :id")
    suspend fun updateStatus(id: Long, newStatus: String)

    @Delete
    suspend fun deleteReport(report: PhoneReport)
}

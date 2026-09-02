package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ImeiCheckRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface ImeiCheckDao {
    @Query("SELECT * FROM imei_checks ORDER BY checkedAt DESC LIMIT 10")
    fun getRecentChecks(): Flow<List<ImeiCheckRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheck(record: ImeiCheckRecord): Long
}

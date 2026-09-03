package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AppUser
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM app_users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): AppUser?

    @Query("SELECT * FROM app_users ORDER BY lastLoginAt DESC LIMIT 1")
    suspend fun getLastActiveUser(): AppUser?

    @Query("SELECT * FROM app_users ORDER BY lastLoginAt DESC")
    fun getAllUsers(): Flow<List<AppUser>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: AppUser)

    @Update
    suspend fun updateUser(user: AppUser)

    @Query("DELETE FROM app_users WHERE email = :email")
    suspend fun deleteUserByEmail(email: String)
}

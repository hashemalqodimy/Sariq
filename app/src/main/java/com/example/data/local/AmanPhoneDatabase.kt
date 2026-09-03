package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AppUser
import com.example.data.model.ImeiCheckRecord
import com.example.data.model.PhoneReport
import com.example.data.model.UrgentAlert
import com.example.util.YemenData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [PhoneReport::class, UrgentAlert::class, ImeiCheckRecord::class, AppUser::class],
    version = 2,
    exportSchema = false
)
abstract class AmanPhoneDatabase : RoomDatabase() {
    abstract fun reportDao(): ReportDao
    abstract fun alertDao(): AlertDao
    abstract fun imeiCheckDao(): ImeiCheckDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AmanPhoneDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AmanPhoneDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AmanPhoneDatabase::class.java,
                    "aman_phone_yemen.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.reportDao(), database.alertDao())
                    }
                }
            }

            suspend fun populateInitialData(reportDao: ReportDao, alertDao: AlertDao) {
                reportDao.insertReports(YemenData.getInitialReports())
                alertDao.insertAlerts(YemenData.getInitialAlerts())
            }
        }
    }
}

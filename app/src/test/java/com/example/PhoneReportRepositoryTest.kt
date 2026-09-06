package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.data.local.AmanPhoneDatabase
import com.example.data.local.ReportDao
import com.example.data.model.PhoneReport
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PhoneReportRepositoryTest {

    private lateinit var db: AmanPhoneDatabase
    private lateinit var reportDao: ReportDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AmanPhoneDatabase::class.java).allowMainThreadQueries().build()
        reportDao = db.reportDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun writeReportAndReadInList() = runBlocking {
        val report = PhoneReport(
            brand = "Samsung",
            modelName = "S21",
            imei1 = "123456789012345",
            color = "Black",
            governorate = "صنعاء",
            district = "السبعين",
            incidentDate = "2024-01-01",
            description = "Stolen",
            ownerName = "Hashem",
            contactPhone = "12345678",
            userEmail = "hashem714pro@gmail.com",
            status = "مسروق"
        )
        reportDao.insertReport(report)

        val myReports = reportDao.getReportsByUser("hashem714pro@gmail.com").first()
        assertEquals(1, myReports.size)
        assertEquals("Samsung", myReports[0].brand)

        val stolenReports = reportDao.getReportsByStatus("مسروق").first()
        assertEquals(1, stolenReports.size)

        val recoveredReports = reportDao.getReportsByStatus("تم الاسترجاع").first()
        assertEquals(0, recoveredReports.size)
    }
}

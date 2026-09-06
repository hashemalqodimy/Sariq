cat << 'INNER_EOF' > app/src/main/java/com/example/ui/screens/NewReportScreen.kt
package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.AlertRed
import com.example.ui.theme.PrimaryBlue
import com.example.ui.AmanPhoneViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewReportScreen(
    viewModel: AmanPhoneViewModel,
    onReportSubmitted: () -> Unit
) {
    val formImei1 by viewModel.formImei1.collectAsStateWithLifecycle()
    val formBrand by viewModel.formBrand.collectAsStateWithLifecycle()
    val formModel by viewModel.formModel.collectAsStateWithLifecycle()
    val formColor by viewModel.formColor.collectAsStateWithLifecycle()
    val formOwnerName by viewModel.formOwnerName.collectAsStateWithLifecycle()
    val formContactPhone by viewModel.formContactPhone.collectAsStateWithLifecycle()
    val formWhatsapp by viewModel.formWhatsapp.collectAsStateWithLifecycle()
    val formGovernorate by viewModel.formGovernorate.collectAsStateWithLifecycle()
    val formDistrict by viewModel.formDistrict.collectAsStateWithLifecycle()
    val formRewardAmount by viewModel.formRewardAmount.collectAsStateWithLifecycle()

    val isSubmitting by viewModel.isSubmitting.collectAsStateWithLifecycle()
    val errorMessage by viewModel.formErrorMessage.collectAsStateWithLifecycle()

    var brandExpanded by remember { mutableStateOf(false) }
    var govExpanded by remember { mutableStateOf(false) }

    val brands = listOf("Samsung", "Apple", "Xiaomi", "Huawei", "Oppo", "Vivo", "Realme", "Tecno", "Infinix", "OnePlus", "Google", "ZTE", "Nokia", "Honor", "Motorola", "أخرى")
    val governorates = listOf("صنعاء", "عدن", "تعز", "الحديدة", "إب", "ذمار", "حضرموت", "شبوة", "مأرب", "صعدة", "حجة", "المحويت", "عمران", "الضالع", "لحج", "أبين", "المهرة", "ريمة", "الجوف", "البيضاء", "سقطرى")

    Scaffold(
        containerColor = Color(0xFFF7F9FC)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp)
        ) {
            
            item {
                Text(
                    text = "تعميم البلاغ",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = "سيتم نشر هذا البلاغ بشكل فوري لجميع مستخدمي التطبيق وأصحاب محلات الجوالات في جميع المحافظات.",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )
            }

            if (errorMessage != null) {
                item {
                    Text(
                        text = errorMessage!!,
                        color = AlertRed,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .background(AlertRed.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFFFFF), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Text("1. بيانات الجهاز المفقود/المسروق", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryBlue)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = formImei1,
                        onValueChange = { viewModel.formImei1.value = it.filter { ch -> ch.isDigit() }.take(15) },
                        label = { Text("الرقم التسلسلي (IMEI 1) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("form_imei_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    ExposedDropdownMenuBox(
                        expanded = brandExpanded,
                        onExpandedChange = { brandExpanded = !brandExpanded }
                    ) {
                        OutlinedTextField(
                            value = formBrand,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("الشركة المصنعة (Brand) *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = brandExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().menuAnchor().testTag("form_brand_dropdown")
                        )
                        ExposedDropdownMenu(
                            expanded = brandExpanded,
                            onDismissRequest = { brandExpanded = false }
                        ) {
                            brands.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        viewModel.formBrand.value = selectionOption
                                        brandExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = formModel,
                            onValueChange = { viewModel.formModel.value = it },
                            label = { Text("اسم الموديل *") },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("form_model_input")
                        )
                        OutlinedTextField(
                            value = formColor,
                            onValueChange = { viewModel.formColor.value = it },
                            label = { Text("لون الجهاز") },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFFFFF), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Text("2. تفاصيل الفقدان/السرقة", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryBlue)
                    Spacer(modifier = Modifier.height(16.dp))

                    ExposedDropdownMenuBox(
                        expanded = govExpanded,
                        onExpandedChange = { govExpanded = !govExpanded }
                    ) {
                        OutlinedTextField(
                            value = formGovernorate,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("المحافظة *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = govExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = govExpanded,
                            onDismissRequest = { govExpanded = false }
                        ) {
                            governorates.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        viewModel.formGovernorate.value = selectionOption
                                        govExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = formDistrict,
                        onValueChange = { viewModel.formDistrict.value = it },
                        label = { Text("المنطقة / تفاصيل الموقع *") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFFFFF), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Text("3. بيانات التواصل والمكافأة", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryBlue)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = formOwnerName,
                        onValueChange = { viewModel.formOwnerName.value = it },
                        label = { Text("اسم صاحب البلاغ *") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = formContactPhone,
                            onValueChange = { viewModel.formContactPhone.value = it.filter { ch -> ch.isDigit() }.take(9) },
                            label = { Text("رقم الهاتف اليمني *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("form_phone_input")
                        )
                        OutlinedTextField(
                            value = formWhatsapp,
                            onValueChange = { viewModel.formWhatsapp.value = it.filter { ch -> ch.isDigit() } },
                            label = { Text("رقم الواتساب") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = formRewardAmount,
                        onValueChange = { viewModel.formRewardAmount.value = it.filter { ch -> ch.isDigit() } },
                        label = { Text("مكافأة مالية لمن يعثر عليه (اختياري)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Button(
                    onClick = { viewModel.submitReport(onSuccess = onReportSubmitted) },
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp).testTag("submit_report_button")
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("جارٍ بث التنبيه الفوري...", fontSize = 13.sp)
                    } else {
                        Icon(imageVector = Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("نشر البلاغ وتعميم التنبيه 🚨", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
INNER_EOF

cat << 'INNER_EOF' > app/src/main/java/com/example/data/repository/PhoneReportRepository.kt
package com.example.data.repository

import android.content.Context
import com.example.data.local.AlertDao
import com.example.data.local.ImeiCheckDao
import com.example.data.local.ReportDao
import com.example.data.local.UserDao
import com.example.data.model.AppUser
import com.example.data.model.PhoneReport
import com.example.data.model.UrgentAlert
import com.example.util.CloudSyncManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class PhoneReportRepository(
    private val dao: ReportDao,
    private val alertDao: AlertDao,
    private val imeiCheckDao: ImeiCheckDao,
    private val userDao: UserDao,
    context: Context
) {
    val cloudSyncManager = CloudSyncManager(context)

    val allReports: Flow<List<PhoneReport>> = dao.getAllReports()
    val myReports: Flow<List<PhoneReport>> = dao.getAllReports()
    val stolenReports: Flow<List<PhoneReport>> = dao.getAllReports()
    val recoveredReports: Flow<List<PhoneReport>> = dao.getAllReports()
    
    val allAlerts: Flow<List<UrgentAlert>> = alertDao.getAllAlerts()
    val unreadAlertsCount: Flow<Int> = alertDao.getUnreadCount()
    val totalReportsCount: Flow<Int> = dao.getTotalReportsCount()
    val recoveredReportsCount: Flow<Int> = dao.getRecoveredReportsCount()
    val recentImeiChecks = imeiCheckDao.getRecentChecks()

    suspend fun getLastActiveUser(): AppUser? = userDao.getLastActiveUser()
    suspend fun getUserByEmail(email: String): AppUser? = userDao.getUserByEmail(email)
    suspend fun saveUser(user: AppUser) = userDao.insertUser(user)

    suspend fun submitReport(report: PhoneReport) {
        dao.insertReport(report)
        cloudSyncManager.syncLocalReportsToCloud(listOf(report))
    }

    suspend fun updateReportStatus(reportId: Long, newStatus: String, phoneModel: String, gov: String) {
        dao.updateStatus(reportId, newStatus)
    }

    suspend fun deleteReportLocal(report: PhoneReport) {
        dao.deleteReport(report)
    }

    suspend fun checkImei(imei: String): Pair<PhoneReport?, Boolean> {
        val foundLocally = dao.findReportByExactImei(imei)
        if (foundLocally != null) {
            return Pair(foundLocally, foundLocally.status != "تم الاسترجاع")
        }
        val foundCloud = cloudSyncManager.searchImeiInCloud(imei)
        if (foundCloud != null) {
            dao.insertReport(foundCloud) // Cache locally
            return Pair(foundCloud, foundCloud.status != "تم الاسترجاع")
        }
        return Pair(null, false)
    }
    
    suspend fun syncNow(showNotificationForNewAlerts: Boolean): Int {
        var newCount = 0
        if (showNotificationForNewAlerts) {
            val cloudReports = cloudSyncManager.fetchCloudReports()
            cloudReports.forEach { 
                dao.insertReport(it)
                newCount++
            }
        } else {
            val local = dao.getAllReports().first()
            cloudSyncManager.syncLocalReportsToCloud(local)
        }
        return newCount
    }
    
    suspend fun markAlertAsRead(alertId: Long) {
        alertDao.markAsRead(alertId)
    }

    suspend fun markAllAlertsAsRead() {
        alertDao.markAllAsRead()
    }
}
INNER_EOF

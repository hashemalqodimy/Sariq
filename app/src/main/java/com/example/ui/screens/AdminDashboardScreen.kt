package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PhoneReport
import com.example.data.model.AppUser
import com.example.ui.AmanPhoneViewModel
import com.example.ui.theme.AlertRed
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AmanPhoneViewModel,
    onNavigateToUserApp: () -> Unit
) {
    val context = LocalContext.current
    val reports by viewModel.allReports.collectAsStateWithLifecycle()
    val users by viewModel.allAdminUsers.collectAsStateWithLifecycle()
    
    var showAddAlertDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.fetchAdminUsers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("لوحة تحكم المشرف", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = onNavigateToUserApp) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "الانتقال لواجهة المستخدمين",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = { showAddAlertDialog = true },
                    containerColor = AlertRed,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.AddAlert, contentDescription = null) },
                    text = { Text("إرسال تعميم عاجل") }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF3F4F6))
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = PrimaryBlue
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("البلاغات (${reports.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("المستخدمين (${users.size})", fontWeight = FontWeight.Bold) }
                )
            }

            if (selectedTab == 0) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(reports, key = { it.id }) { report ->
                        AdminReportCard(
                            report = report,
                            onDelete = {
                                viewModel.deleteReportByAdmin(
                                    imei = report.imei1,
                                    onSuccess = {
                                        Toast.makeText(context, "تم حذف البلاغ بنجاح", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(users) { user ->
                        AdminUserCard(
                            user = user,
                            onToggleBan = { isBanned ->
                                viewModel.updateUserBanStatus(user.email, isBanned) { success ->
                                    if (success) {
                                        Toast.makeText(context, "تم تحديث حالة الحساب", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "فشل التحديث", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
        
        if (showAddAlertDialog) {
            AdminAddAlertDialog(
                viewModel = viewModel,
                onDismiss = { showAddAlertDialog = false }
            )
        }
    }
}

@Composable
fun AdminUserCard(user: AppUser, onToggleBan: (Boolean) -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFE5E7EB), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = user.fullName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = user.email, fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(if (user.isBanned) AlertRed else SuccessGreen, RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (user.isBanned) "محظور" else "نشط",
                        fontSize = 12.sp,
                        color = if (user.isBanned) AlertRed else SuccessGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (user.isBanned) {
                IconButton(onClick = { onToggleBan(false) }) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "فك الحظر", tint = SuccessGreen)
                }
            } else {
                IconButton(onClick = { onToggleBan(true) }) {
                    Icon(Icons.Default.Block, contentDescription = "حظر", tint = AlertRed)
                }
            }
        }
    }
}

@Composable
fun AdminReportCard(report: PhoneReport, onDelete: () -> Unit) {
    var confirmDelete by remember { mutableStateOf(false) }
    
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "الموديل: ${report.brand} ${report.modelName}", fontWeight = FontWeight.Bold)
            Text(text = "IMEI: ${report.imei1}", fontSize = 13.sp, color = Color.Gray)
            Text(text = "الحالة: ${report.status}", fontSize = 13.sp, color = if (report.status == "مسروق") AlertRed else PrimaryBlue)
            
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (confirmDelete) {
                    TextButton(onClick = { confirmDelete = false }) {
                        Text("إلغاء")
                    }
                    Button(
                        onClick = {
                            confirmDelete = false
                            onDelete()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
                    ) {
                        Text("تأكيد الحذف النهائي")
                    }
                } else {
                    OutlinedButton(
                        onClick = { confirmDelete = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertRed)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("حذف البلاغ")
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAddAlertDialog(
    viewModel: AmanPhoneViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إرسال تعميم جديد للمستخدمين") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان التعميم") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("نص التعميم التفصيلي") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && message.isNotBlank()) {
                        isLoading = true
                        viewModel.publishAdminAlert(
                            title = title.trim(),
                            message = message.trim(),
                            onSuccess = {
                                isLoading = false
                                Toast.makeText(context, "تم إرسال التعميم بنجاح", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            },
                            onError = { msg ->
                                isLoading = false
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                },
                enabled = !isLoading
            ) {
                Text(if (isLoading) "جاري الإرسال..." else "إرسال وتعميم")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

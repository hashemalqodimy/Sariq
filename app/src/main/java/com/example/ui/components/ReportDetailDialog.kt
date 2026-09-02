package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.PhoneReport
import com.example.ui.theme.AlertRed
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen

@Composable
fun ReportDetailDialog(
    report: PhoneReport,
    onDismiss: () -> Unit,
    onMarkRecovered: (PhoneReport) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .testTag("report_detail_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(scrollState)
            ) {
                // Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (report.status == "مسروق") AlertRed.copy(alpha = 0.15f) else SuccessGreen.copy(alpha = 0.15f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = null,
                                tint = if (report.status == "مسروق") AlertRed else SuccessGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "${report.brand} ${report.modelName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            StatusBadge(status = report.status)
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Reward Banner if exists
                if (report.rewardAmount > 0) {
                    Surface(
                        color = Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🎁 مكافأة مالية معلنة: ",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E),
                                fontSize = 13.sp
                            )
                            Text(
                                text = "${String.format("%,d", report.rewardAmount)} ريال يمني",
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFB45309),
                                fontSize = 15.sp
                            )
                        }
                    }
                }

                // IMEI Info Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "بيانات الهوية الرقمية (IMEI)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailRow(label = "الـ IMEI الأساسي (1):", value = report.imei1, isMono = true)
                        if (report.imei2.isNotBlank()) {
                            DetailRow(label = "الـ IMEI الثانوي (2):", value = report.imei2, isMono = true)
                        }
                        if (report.serialNumber.isNotBlank()) {
                            DetailRow(label = "الرقم التسلسلي (S/N):", value = report.serialNumber, isMono = true)
                        }
                        DetailRow(label = "اللون والمواصفات:", value = "${report.color} • ${report.storageCapacity}")
                        if (report.distinctiveFeatures.isNotBlank()) {
                            DetailRow(label = "علامات فارقة بالجهاز:", value = report.distinctiveFeatures)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Location & Incident details
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = AlertRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "تفاصيل الحادثة والموقع", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailRow(label = "المحافظة:", value = report.governorate)
                        DetailRow(label = "مكان الحادثة:", value = report.district)
                        DetailRow(label = "تاريخ الواقعة:", value = report.incidentDate)
                        if (report.policeStation.isNotBlank()) {
                            DetailRow(label = "البلاغ الأمني / المركز:", value = report.policeStation)
                        }
                        if (report.description.isNotBlank()) {
                            DetailRow(label = "وصف وملابسات الحادثة:", value = report.description)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Owner contact info
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "بيانات التواصل بصاحب الهاتف", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailRow(label = "اسم صاحب البلاغ:", value = report.ownerName)
                        DetailRow(label = "رقم الهاتف المعتمد:", value = report.contactPhone)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons: Call, WhatsApp, Share
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { dialPhone(context, report.contactPhone) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dial_owner_button")
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "اتصال", fontSize = 13.sp)
                    }

                    Button(
                        onClick = { openWhatsApp(context, report.whatsappNumber.ifEmpty { report.contactPhone }, report) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("whatsapp_owner_button")
                    ) {
                        Text(text = "واتساب", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { shareReport(context, report) },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("share_report_button")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "مشاركة", modifier = Modifier.size(18.dp))
                    }
                }

                // Recovered Action
                if (report.status != "تم الاسترجاع") {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onMarkRecovered(report) },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("mark_recovered_button")
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "تم العثور على الجهاز (تغيير الحالة لمسترجع)", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, isMono: Boolean = false) {
    Column(modifier = Modifier.padding(vertical = 3.dp)) {
        Text(text = label, fontSize = 11.sp, color = Color.Gray)
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun dialPhone(context: Context, phone: String) {
    try {
        val cleanPhone = phone.filter { it.isDigit() || it == '+' }
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$cleanPhone")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback
    }
}

private fun openWhatsApp(context: Context, phone: String, report: PhoneReport) {
    try {
        val cleanPhone = phone.filter { it.isDigit() }
        val yemenPhone = if (cleanPhone.startsWith("967")) cleanPhone else "967$cleanPhone"
        val message = "السلام عليكم ورحمة الله، بخصوص بلاغ الهاتف (${report.brand} ${report.modelName}) برقم IMEI: ${report.imei1} المنشور في تطبيق أمان فون اليمن."
        val url = "https://wa.me/$yemenPhone?text=${Uri.encode(message)}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback to dialer
        dialPhone(context, phone)
    }
}

private fun shareReport(context: Context, report: PhoneReport) {
    val shareText = """
        🚨 تعميم بلاغ هاتف مسروق في الجمهورية اليمنية 🚨
        - الجهاز: ${report.brand} ${report.modelName}
        - المحافظة: ${report.governorate} (${report.district})
        - رقم الـ IMEI: ${report.imei1}
        ${if (report.rewardAmount > 0) "- مكافأة مالية: ${report.rewardAmount} ريال يمني" else ""}
        - صاحب البلاغ: ${report.ownerName}
        - للتواصل: ${report.contactPhone}
        
        يرجى من جميع محلات الهواتف والمواطنين الحذر وفحص السيريال عبر تطبيق (أمان فون اليمن).
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "بلاغ هاتف مسروق - ${report.modelName}")
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(intent, "تعميم البلاغ عبر:"))
}

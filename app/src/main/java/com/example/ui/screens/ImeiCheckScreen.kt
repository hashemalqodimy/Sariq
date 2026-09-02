package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PhoneReport
import com.example.ui.AmanPhoneViewModel
import com.example.ui.ImeiCheckState
import com.example.ui.components.ReportCard
import com.example.ui.theme.AlertRed
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen

@Composable
fun ImeiCheckScreen(
    viewModel: AmanPhoneViewModel,
    modifier: Modifier = Modifier
) {
    val imeiInput by viewModel.imeiInput.collectAsStateWithLifecycle()
    val checkState by viewModel.imeiCheckState.collectAsStateWithLifecycle()
    val recentChecks by viewModel.recentChecks.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Info Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(PrimaryBlue.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "فحص الـ IMEI والأمان",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "تحقق من سلامة أي هاتف مستعمل قبل شرائه في اليمن",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // How to find IMEI helper box
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "كيف تعرف الـ IMEI؟ اطلب الكود *#06# من لوحة الاتصال ليظهر رقم الـ 15 خانة فوراً.",
                                fontSize = 12.sp,
                                color = Color(0xFF334155),
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // IMEI Input Field
                    OutlinedTextField(
                        value = imeiInput,
                        onValueChange = { viewModel.onImeiInputChanged(it) },
                        label = { Text("أدخل رقم الـ IMEI (15 رقماً)") },
                        placeholder = { Text("مثال: 356789123456789") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.PhoneAndroid, contentDescription = null, tint = PrimaryBlue)
                        },
                        trailingIcon = {
                            if (imeiInput.isNotEmpty()) {
                                IconButton(onClick = { viewModel.resetImeiCheck() }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "مسح")
                                }
                            }
                        },
                        supportingText = {
                            Text(
                                text = "${imeiInput.length} / 15 خانة",
                                fontSize = 11.sp,
                                color = if (imeiInput.length == 15) SuccessGreen else Color.Gray,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.End
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                keyboardController?.hide()
                                viewModel.performImeiCheck()
                            }
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("imei_input_field")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action Button
                    Button(
                        onClick = {
                            keyboardController?.hide()
                            viewModel.performImeiCheck()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(12.dp),
                        enabled = imeiInput.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_imei_check_button")
                    ) {
                        if (checkState is ImeiCheckState.Loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("جارٍ الفحص في قاعدة البيانات اليمنية...", fontSize = 13.sp)
                        } else {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "فحص السجل الأمني للهاتف", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Demo Test Chips
                    Text(text = "أو اختر عينة للتجربة السريعة:", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.setQuickCheckImei("356789123456789") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("تجربة هاتف مسروق", fontSize = 11.sp, color = AlertRed, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { viewModel.setQuickCheckImei("861234567890123") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("تجربة هاتف سليم", fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Result Section
        when (val state = checkState) {
            is ImeiCheckState.Loading -> {
                // Handled in button or empty
            }

            is ImeiCheckState.Clean -> {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("imei_clean_result")
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .background(SuccessGreen, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "الهاتف سليم وغير مدرج في سجلات السرقات",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = Color(0xFF065F46)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "رقم الـ IMEI: ${state.imei}\nلم يُسجل أي بلاغ سرقة أو فقدان لهذا الرقم في كافة محافظات الجمهورية اليمنية.",
                                fontSize = 13.sp,
                                color = Color(0xFF047857),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            is ImeiCheckState.StolenFound -> {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("imei_stolen_result")
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(AlertRed, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "⚠️ تحذير أمني: هاتف مسروق ومُبلّغ عنه!",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = AlertRed
                                    )
                                    Text(
                                        text = "تطابق كامل لرقم الـ IMEI مع بلاغ رسمي",
                                        fontSize = 12.sp,
                                        color = Color(0xFF991B1B)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = AlertRed.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "بيانات البلاغ المسجل في المنصة:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF1F2937)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            ReportCard(
                                report = state.report,
                                onClick = { viewModel.selectReport(state.report) }
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "تنبيه لأصحاب المحلات: شراء هذا الهاتف يعرضك للمساءلة القانونية وشبهة شراء أموال مسروقة. يرجى التواصل فوراً مع مالكه أو أقرب مركز شرطة.",
                                fontSize = 11.sp,
                                color = Color(0xFFB91C1C),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            is ImeiCheckState.Error -> {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Error, contentDescription = null, tint = Color(0xFFD97706))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = state.message, color = Color(0xFF92400E), fontSize = 12.sp)
                        }
                    }
                }
            }

            ImeiCheckState.Idle -> {
                // Nothing
            }
        }

        // Recent Checks Section
        if (recentChecks.isNotEmpty()) {
            item {
                Text(
                    text = "سجل الفحوصات الأخيرة على هذا الجهاز",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(recentChecks, key = { it.id }) { check ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (check.isStolen) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (check.isStolen) AlertRed else SuccessGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = check.imei,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = check.phoneModel,
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Surface(
                            color = if (check.isStolen) Color(0xFFFEE2E2) else Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (check.isStolen) "مسروق" else "سليم",
                                color = if (check.isStolen) AlertRed else SuccessGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

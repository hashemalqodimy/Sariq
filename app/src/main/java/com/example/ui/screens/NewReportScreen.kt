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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AmanPhoneViewModel
import com.example.ui.theme.AlertRed
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningOrange
import com.example.util.YemenData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewReportScreen(
    viewModel: AmanPhoneViewModel,
    onReportSubmitted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formBrand by viewModel.formBrand.collectAsStateWithLifecycle()
    val formModel by viewModel.formModel.collectAsStateWithLifecycle()
    val formImei1 by viewModel.formImei1.collectAsStateWithLifecycle()
    val formImei2 by viewModel.formImei2.collectAsStateWithLifecycle()
    val formSerialNumber by viewModel.formSerialNumber.collectAsStateWithLifecycle()
    val formColor by viewModel.formColor.collectAsStateWithLifecycle()
    val formStorage by viewModel.formStorage.collectAsStateWithLifecycle()
    val formGovernorate by viewModel.formGovernorate.collectAsStateWithLifecycle()
    val formDistrict by viewModel.formDistrict.collectAsStateWithLifecycle()
    val formIncidentDate by viewModel.formIncidentDate.collectAsStateWithLifecycle()
    val formDescription by viewModel.formDescription.collectAsStateWithLifecycle()
    val formDistinctiveFeatures by viewModel.formDistinctiveFeatures.collectAsStateWithLifecycle()
    val formOwnerName by viewModel.formOwnerName.collectAsStateWithLifecycle()
    val formContactPhone by viewModel.formContactPhone.collectAsStateWithLifecycle()
    val formWhatsapp by viewModel.formWhatsapp.collectAsStateWithLifecycle()
    val formPoliceStation by viewModel.formPoliceStation.collectAsStateWithLifecycle()
    val formRewardAmount by viewModel.formRewardAmount.collectAsStateWithLifecycle()
    val formStatus by viewModel.formStatus.collectAsStateWithLifecycle()

    val isSubmitting by viewModel.isSubmitting.collectAsStateWithLifecycle()
    val errorMessage by viewModel.formErrorMessage.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    var brandExpanded by remember { mutableStateOf(false) }
    var govExpanded by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            if (formOwnerName.isBlank()) {
                viewModel.formOwnerName.value = user.fullName
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Form Title Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(AlertRed.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = AlertRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "تسجيل بلاغ سرقة جديد",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "سيتم تعميم البلاغ فوراً لجميع المستخدمين ومحلات الجوالات في اليمن",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        // Error message box if any
        if (errorMessage != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Error, contentDescription = null, tint = AlertRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = errorMessage!!, color = AlertRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Status Type Selection
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "نوع البلاغ:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = formStatus == "مسروق",
                            onClick = { viewModel.formStatus.value = "مسروق" },
                            label = { Text("هاتف مسروق 🔴", fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AlertRed,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = formStatus == "مفقود",
                            onClick = { viewModel.formStatus.value = "مفقود" },
                            label = { Text("هاتف مفقود 🟠", fontSize = 13.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = WarningOrange,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Section 1: Phone Information
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.PhoneAndroid, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "بيانات ومواصفات الهاتف", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Brand Dropdown
                    ExposedDropdownMenuBox(
                        expanded = brandExpanded,
                        onExpandedChange = { brandExpanded = !brandExpanded }
                    ) {
                        OutlinedTextField(
                            value = formBrand,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("ماركة الهاتف") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = brandExpanded) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = brandExpanded,
                            onDismissRequest = { brandExpanded = false }
                        ) {
                            YemenData.phoneBrands.forEach { brand ->
                                DropdownMenuItem(
                                    text = { Text(brand) },
                                    onClick = {
                                        viewModel.formBrand.value = brand
                                        brandExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Model name
                    OutlinedTextField(
                        value = formModel,
                        onValueChange = { viewModel.formModel.value = it },
                        label = { Text("موديل الجهاز بالكامل *") },
                        placeholder = { Text("مثال: Galaxy S24 Ultra أو iPhone 15 Pro") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_model_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // IMEI 1
                    OutlinedTextField(
                        value = formImei1,
                        onValueChange = { viewModel.formImei1.value = it.filter { ch -> ch.isDigit() }.take(15) },
                        label = { Text("رقم الـ IMEI الأساسي (15 رقماً) *") },
                        placeholder = { Text("اطلب *#06# لمعرفته أو من علبة الجهاز") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        supportingText = { Text("${formImei1.length} / 15 خانة") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_imei1_input")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // IMEI 2 & Serial Number row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = formImei2,
                            onValueChange = { viewModel.formImei2.value = it.filter { ch -> ch.isDigit() }.take(15) },
                            label = { Text("الـ IMEI الثاني (اختياري)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = formSerialNumber,
                            onValueChange = { viewModel.formSerialNumber.value = it },
                            label = { Text("الرقم التسلسلي S/N") },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Color & Storage
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = formColor,
                            onValueChange = { viewModel.formColor.value = it },
                            label = { Text("لون الجهاز") },
                            placeholder = { Text("مثال: أسود، أزرق") },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = formStorage,
                            onValueChange = { viewModel.formStorage.value = it },
                            label = { Text("سعة التخزين") },
                            placeholder = { Text("128GB, 256GB...") },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = formDistinctiveFeatures,
                        onValueChange = { viewModel.formDistinctiveFeatures.value = it },
                        label = { Text("علامات فارقة بالجهاز (خدوش، ملصقات، كفر...)") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Section 2: Location and Incident Details
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = AlertRed, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "مكان وتفاصيل الحادثة (المحافظة اليمنية)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Governorate Dropdown
                    ExposedDropdownMenuBox(
                        expanded = govExpanded,
                        onExpandedChange = { govExpanded = !govExpanded }
                    ) {
                        OutlinedTextField(
                            value = formGovernorate,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("المحافظة اليمنية *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = govExpanded) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("form_governorate_dropdown")
                        )
                        ExposedDropdownMenu(
                            expanded = govExpanded,
                            onDismissRequest = { govExpanded = false }
                        ) {
                            YemenData.governoratesOnly.forEach { gov ->
                                DropdownMenuItem(
                                    text = { Text(gov) },
                                    onClick = {
                                        viewModel.formGovernorate.value = gov
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
                        label = { Text("المديرية والشارع / مكان السرقة بالتحديد *") },
                        placeholder = { Text("مثال: شارع حدة - أمام مجمع الكميم") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_district_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = formIncidentDate,
                        onValueChange = { viewModel.formIncidentDate.value = it },
                        label = { Text("تاريخ الحادثة (سنة-شهر-يوم)") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = formPoliceStation,
                        onValueChange = { viewModel.formPoliceStation.value = it },
                        label = { Text("رقم البلاغ الأمني أو قسم الشرطة (إن وجد)") },
                        placeholder = { Text("مثال: قسم شرطة المنصورة - بلاغ رقم 123") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = formDescription,
                        onValueChange = { viewModel.formDescription.value = it },
                        label = { Text("تفاصيل وملابسات السرقة") },
                        placeholder = { Text("اشرح كيف تمت السرقة للمساعدة في التعرف عليه...") },
                        shape = RoundedCornerShape(12.dp),
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Section 3: Owner info and Reward
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "بيانات صاحب الهاتف والمكافأة المالية", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = formOwnerName,
                        onValueChange = { viewModel.formOwnerName.value = it },
                        label = { Text("اسم صاحب البلاغ بالكامل *") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_owner_name_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = formContactPhone,
                            onValueChange = { viewModel.formContactPhone.value = it.filter { ch -> ch.isDigit() }.take(9) },
                            label = { Text("رقم الهاتف اليمني *") },
                            placeholder = { Text("77xxxxxxx") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("form_phone_input")
                        )
                        OutlinedTextField(
                            value = formWhatsapp,
                            onValueChange = { viewModel.formWhatsapp.value = it.filter { ch -> ch.isDigit() } },
                            label = { Text("رقم الواتساب") },
                            placeholder = { Text("73xxxxxxx") },
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
                        label = { Text("مكافأة مالية لمن يعثر عليه (ريال يمني - اختياري)") },
                        placeholder = { Text("مثال: 50000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Submit Button with Broadcast indicator
        item {
            Button(
                onClick = {
                    viewModel.submitReport(onSuccess = onReportSubmitted)
                },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_report_button")
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("جارٍ بث التنبيه الفوري لجميع المحافظات...", fontSize = 13.sp)
                } else {
                    Icon(imageVector = Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "نشر البلاغ وتعميم التنبيه الفوري لكافة المحافظات 🚨",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Developer Credit Card
        item {
            com.example.ui.components.DeveloperCreditCard(compact = true)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

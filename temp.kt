package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import AsyncImage
import androidx.compose.material3.OutlinedButton
import AsyncImage
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.material.icons.filled.CameraAlt
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
    val formProofImageUri by viewModel.formProofImageUri.collectAsStateWithLifecycle()
    val formStatus by viewModel.formStatus.collectAsStateWithLifecycle()

    val isSubmitting by viewModel.isSubmitting.collectAsStateWithLifecycle()
    val errorMessage by viewModel.formErrorMessage.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    var brandExpanded by remember { mutableStateOf(false) }
    var govExpanded by remember { mutableStateOf(false) }
    var showBarcodeScanner by remember { mutableStateOf(false) }
    val photoPicker = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.formProofImageUri.value = uri
        }
    }
    
        androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.formProofImageUri.value = uri
        }
    }
    
    
        androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.formProofImageUri.value = uri
        }
    }
    

    if (showBarcodeScanner) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showBarcodeScanner = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            com.example.ui.screens.BarcodeScannerScreen(
                onBarcodeScanned = { barcode ->
                    viewModel.formImei1.value = barcode
                    showBarcodeScanner = false
                },
                onDismiss = { showBarcodeScanner = false }
            )
        }
    }

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
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    
                    
                        androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
                    ) { uri ->
                        if (uri != null) {
                            viewModel.formProofImageUri.value = uri
                        }
                    }

                    androidx.compose.material3.Text("إثبات الملكية (اختياري، يرفع الموثوقية)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedButton(
                        onClick = { photoPicker.launch(androidx.activity.result.PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (formProofImageUri != null) "تم اختيار الصورة (اضغط لتغييرها)" else "إرفاق صورة لكرتون الجوال أو فاتورة الشراء")
                    }
                    
                    if (formProofImageUri != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        AsyncImage(
                            model = formProofImageUri,
                            contentDescription = "صورة الفاتورة",
                            modifier = Modifier.fillMaxWidth().height(150.dp).androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        )
                    }
                    modifier = Modifier.padding(16.dp),

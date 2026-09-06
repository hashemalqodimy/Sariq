cat << 'INNER_EOF' > photo_picker.tmp
    val photoPicker = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.formProofImageUri.value = uri
        }
    }
INNER_EOF

sed -i '/var showBarcodeScanner by remember { mutableStateOf(false) }/r photo_picker.tmp' app/src/main/java/com/example/ui/screens/NewReportScreen.kt

cat << 'INNER_EOF' > ui_picker.tmp
                    Spacer(modifier = Modifier.height(16.dp))
                    androidx.compose.material3.Text("إثبات الملكية (اختياري، يرفع الموثوقية)", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    androidx.compose.material3.OutlinedButton(
                        onClick = { photoPicker.launch(androidx.activity.result.PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        androidx.compose.material3.Text(if (formProofImageUri != null) "تم اختيار الصورة (اضغط لتغييرها)" else "إرفاق صورة لكرتون الجوال أو فاتورة الشراء")
                    }
                    
                    if (formProofImageUri != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        coil.compose.AsyncImage(
                            model = formProofImageUri,
                            contentDescription = "صورة الفاتورة",
                            modifier = Modifier.fillMaxWidth().height(150.dp).androidx.compose.ui.draw.clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )
                    }
INNER_EOF

# Find the specific line to insert before the submit button safely without corrupting curly braces
sed -i '/\/\/ Submit Button with Broadcast indicator/i \
                    Spacer(modifier = Modifier.height(16.dp))\
                    androidx.compose.material3.Text("إثبات الملكية (اختياري، يرفع الموثوقية)", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 15.sp)\
                    Spacer(modifier = Modifier.height(8.dp))\
                    \
                    androidx.compose.material3.OutlinedButton(\
                        onClick = { photoPicker.launch(androidx.activity.result.PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly)) },\
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),\
                        modifier = Modifier.fillMaxWidth()\
                    ) {\
                        androidx.compose.material3.Icon(androidx.compose.material.icons.Icons.Default.CameraAlt, contentDescription = null)\
                        Spacer(modifier = Modifier.width(8.dp))\
                        androidx.compose.material3.Text(if (formProofImageUri != null) "تم اختيار الصورة (اضغط لتغييرها)" else "إرفاق صورة لكرتون الجوال أو فاتورة الشراء")\
                    }\
                    \
                    if (formProofImageUri != null) {\
                        Spacer(modifier = Modifier.height(8.dp))\
                        coil.compose.AsyncImage(\
                            model = formProofImageUri,\
                            contentDescription = "صورة الفاتورة",\
                            modifier = Modifier.fillMaxWidth().height(150.dp).androidx.compose.ui.draw.clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),\
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit\
                        )\
                    }\
                }\
            }\
        }' app/src/main/java/com/example/ui/screens/NewReportScreen.kt

# Remove the extra closing braces that were just duplicated
sed -i '/                }\n            }\n        }\n        \/\/ Submit Button with Broadcast indicator/c\
        \/\/ Submit Button with Broadcast indicator' app/src/main/java/com/example/ui/screens/NewReportScreen.kt

sed -i 's/val isUrgent = true,/val isUrgent = true/g' app/src/main/java/com/example/util/CloudSyncManager.kt
sed -i 's/put("proofImageUrl", r.proofImageUrl)//g' app/src/main/java/com/example/util/CloudSyncManager.kt
sed -i '/put("isUrgent", r.isUrgent)/a \            put("proofImageUrl", r.proofImageUrl)' app/src/main/java/com/example/util/CloudSyncManager.kt


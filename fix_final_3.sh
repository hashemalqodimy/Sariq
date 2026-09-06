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
                        Icon(androidx.compose.material.icons.Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (formProofImageUri != null) "تم اختيار الصورة (اضغط لتغييرها)" else "إرفاق صورة لكرتون الجوال أو فاتورة الشراء")
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

# Find the end of the form by matching formRewardAmount section
sed -i '/modifier = Modifier.fillMaxWidth()/ {
    /modifier = Modifier.fillMaxWidth()/!b
    :a
    n
    /)/!ba
    /}/!ba
    r ui_picker.tmp
}' app/src/main/java/com/example/ui/screens/NewReportScreen.kt

# Remove duplicate imports
sed -i 's/import coil.compose.AsyncImage//g' app/src/main/java/com/example/ui/screens/NewReportScreen.kt
sed -i 's/import androidx.compose.material3.OutlinedButton//g' app/src/main/java/com/example/ui/screens/NewReportScreen.kt

# Add imports at the top
sed -i '10a import coil.compose.AsyncImage\nimport androidx.compose.material3.OutlinedButton' app/src/main/java/com/example/ui/screens/NewReportScreen.kt


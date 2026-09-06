sed -i 's/val formRewardAmount by viewModel.formRewardAmount.collectAsStateWithLifecycle()/val formRewardAmount by viewModel.formRewardAmount.collectAsStateWithLifecycle()\n    val formProofImageUri by viewModel.formProofImageUri.collectAsStateWithLifecycle()/g' app/src/main/java/com/example/ui/screens/NewReportScreen.kt

cat << 'INNER_EOF' > photo_picker.tmp
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val photoPicker = androidx.activity.compose.rememberLauncherForActivityResult(
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
                        coil.compose.AsyncImage(
                            model = formProofImageUri,
                            contentDescription = "صورة الفاتورة",
                            modifier = Modifier.fillMaxWidth().height(150.dp).androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        )
                    }
INNER_EOF

sed -i '/modifier = Modifier.fillMaxWidth()/ {
n
n
r photo_picker.tmp
}' app/src/main/java/com/example/ui/screens/NewReportScreen.kt

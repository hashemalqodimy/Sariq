cat << 'INNER_EOF' > photo_picker.tmp
    val context = androidx.compose.ui.platform.LocalContext.current
    val photoPicker = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.formProofImageUri.value = uri
        }
    }
INNER_EOF

sed -i '/var showBarcodeScanner by remember { mutableStateOf(false) }/r photo_picker.tmp' app/src/main/java/com/example/ui/screens/NewReportScreen.kt

sed -i 's/val context = androidx.compose.ui.platform.LocalContext.current//g' app/src/main/java/com/example/ui/screens/NewReportScreen.kt
sed -i 's/val photoPicker = androidx.activity.compose.rememberLauncherForActivityResult(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()) { uri -> if (uri != null) { viewModel.formProofImageUri.value = uri } }//g' app/src/main/java/com/example/ui/screens/NewReportScreen.kt
sed -i 's/var photoPicker = androidx.activity.compose.rememberLauncherForActivityResult(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()) { uri -> if (uri != null) { viewModel.formProofImageUri.value = uri } }//g' app/src/main/java/com/example/ui/screens/NewReportScreen.kt

sed -i 's/coil.compose.AsyncImage/AsyncImage/g' app/src/main/java/com/example/ui/screens/NewReportScreen.kt
sed -i '10a import coil.compose.AsyncImage\nimport androidx.compose.material3.OutlinedButton' app/src/main/java/com/example/ui/screens/NewReportScreen.kt

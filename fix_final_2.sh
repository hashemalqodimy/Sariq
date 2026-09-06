sed -i 's/val photoPicker = .*//g' app/src/main/java/com/example/ui/screens/NewReportScreen.kt
sed -i 's/androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()//g' app/src/main/java/com/example/ui/screens/NewReportScreen.kt
sed -i 's/) { uri ->//g' app/src/main/java/com/example/ui/screens/NewReportScreen.kt
sed -i 's/if (uri != null) {//g' app/src/main/java/com/example/ui/screens/NewReportScreen.kt
sed -i 's/viewModel.formProofImageUri.value = uri//g' app/src/main/java/com/example/ui/screens/NewReportScreen.kt
sed -i 's/}//g' app/src/main/java/com/example/ui/screens/NewReportScreen.kt

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

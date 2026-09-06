sed -i 's/import AsyncImage//g' app/src/main/java/com/example/ui/screens/NewReportScreen.kt
sed -i 's/import androidx.compose.material3.OutlinedButton//g' app/src/main/java/com/example/ui/screens/NewReportScreen.kt
sed -i '10a import coil.compose.AsyncImage\nimport androidx.compose.material3.OutlinedButton' app/src/main/java/com/example/ui/screens/NewReportScreen.kt

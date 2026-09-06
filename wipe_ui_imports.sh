sed -i 's/import com.example.ui.theme.BackgroundLight/import com.example.ui.theme.SurfaceLight/g' app/src/main/java/com/example/ui/screens/NewReportScreen.kt
sed -i 's/import com.example.ui.theme.SurfaceWhite/import com.example.ui.theme.SurfaceLight/g' app/src/main/java/com/example/ui/screens/NewReportScreen.kt
sed -i 's/import com.example.ui.theme.TextPrimary/import com.example.ui.theme.TextPrimaryLight/g' app/src/main/java/com/example/ui/screens/NewReportScreen.kt
sed -i 's/import com.example.ui.theme.TextSecondary/import com.example.ui.theme.TextSecondaryLight/g' app/src/main/java/com/example/ui/screens/NewReportScreen.kt
sed -i 's/BackgroundLight/androidx.compose.ui.graphics.Color(0xFFF7F9FC)/g' app/src/main/java/com/example/ui/screens/NewReportScreen.kt
sed -i 's/SurfaceWhite/androidx.compose.ui.graphics.Color(0xFFFFFFFF)/g' app/src/main/java/com/example/ui/screens/NewReportScreen.kt
sed -i 's/TextPrimary/androidx.compose.ui.graphics.Color(0xFF1E293B)/g' app/src/main/java/com/example/ui/screens/NewReportScreen.kt
sed -i 's/TextSecondary/androidx.compose.ui.graphics.Color(0xFF64748B)/g' app/src/main/java/com/example/ui/screens/NewReportScreen.kt
sed -i 's/com.example.ui.components.AmanTopAppBar/com.example.ui.components.AmanTopAppBar/g' app/src/main/java/com/example/ui/screens/NewReportScreen.kt

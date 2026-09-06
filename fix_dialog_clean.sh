sed -i 's/import RoundedCornerShape/import androidx.compose.foundation.shape.RoundedCornerShape/g' app/src/main/java/com/example/ui/components/ReportDetailDialog.kt
sed -i '/Conflicting import/d' app/src/main/java/com/example/ui/components/ReportDetailDialog.kt

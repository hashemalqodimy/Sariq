sed -i '/modifier = Modifier.fillMaxWidth()/ {
    /modifier = Modifier.fillMaxWidth()/!b
    :a
    n
    /)/!ba
    r ui_picker.tmp
}' app/src/main/java/com/example/ui/screens/NewReportScreen.kt

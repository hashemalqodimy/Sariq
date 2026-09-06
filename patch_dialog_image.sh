cat << 'INNER_EOF' > proof_image.tmp
                    if (report.proofImageUrl.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "صورة الإثبات (الفاتورة/الكرتون)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        coil.compose.AsyncImage(
                            model = report.proofImageUrl,
                            contentDescription = "صورة الفاتورة",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(Color.Black.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                .androidx.compose.ui.draw.clip(RoundedCornerShape(8.dp)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
INNER_EOF

sed -i '/HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))/ {
r proof_image.tmp
d
}' app/src/main/java/com/example/ui/components/ReportDetailDialog.kt

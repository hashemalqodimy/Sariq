cat << 'INNER_EOF' > badge.tmp
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                StatusBadge(status = report.status)
                                if (report.proofImageUrl.isNotEmpty()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFF00C853).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00C853), modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("بلاغ مؤكد", fontSize = 11.sp, color = Color(0xFF00C853), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
INNER_EOF

sed -i '/StatusBadge(status = report.status)/ {
r badge.tmp
d
}' app/src/main/java/com/example/ui/components/ReportDetailDialog.kt

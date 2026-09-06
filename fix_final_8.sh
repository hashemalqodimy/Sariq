sed -i 's/val isUrgent = true,/val isUrgent = true/g' app/src/main/java/com/example/util/CloudSyncManager.kt
sed -i 's/put("proofImageUrl", r.proofImageUrl)//g' app/src/main/java/com/example/util/CloudSyncManager.kt
sed -i '/put("isUrgent", r.isUrgent)/a \            put("proofImageUrl", r.proofImageUrl)' app/src/main/java/com/example/util/CloudSyncManager.kt

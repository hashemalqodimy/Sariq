sed -i 's/proofImageUrl = obj.optString("proofImageUrl", ""),/proofImageUrl = obj.optString("proofImageUrl", "")/g' app/src/main/java/com/example/util/CloudSyncManager.kt
sed -i 's/proofImageUrl = doc.getString("proofImageUrl") ?: "",/proofImageUrl = doc.getString("proofImageUrl") ?: ""/g' app/src/main/java/com/example/util/CloudSyncManager.kt

sed -i 's/rewardAmount = doc.getString("rewardAmount") ?: "",/rewardAmount = doc.getString("rewardAmount")?.toLongOrNull() ?: 0L,/g' app/src/main/java/com/example/util/CloudSyncManager.kt

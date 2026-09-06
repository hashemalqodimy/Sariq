sed -i 's/"lostLocation" to report.lostLocation,/"governorate" to report.governorate,/g' app/src/main/java/com/example/util/CloudSyncManager.kt
sed -i 's/"lostGovernorate" to report.lostGovernorate,/"district" to report.district,/g' app/src/main/java/com/example/util/CloudSyncManager.kt
sed -i '/"district" to report.district,/a \                    "incidentDate" to report.incidentDate,\n                    "description" to report.description,' app/src/main/java/com/example/util/CloudSyncManager.kt
sed -i 's/lostLocation = doc.getString("lostLocation") ?: "",/governorate = doc.getString("governorate") ?: "",/g' app/src/main/java/com/example/util/CloudSyncManager.kt
sed -i 's/lostGovernorate = doc.getString("lostGovernorate") ?: "",/district = doc.getString("district") ?: "",/g' app/src/main/java/com/example/util/CloudSyncManager.kt
sed -i '/district = doc.getString("district") ?: "",/a \                        incidentDate = doc.getLong("incidentDate") ?: System.currentTimeMillis(),\n                        description = doc.getString("description") ?: "",' app/src/main/java/com/example/util/CloudSyncManager.kt

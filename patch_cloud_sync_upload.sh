cat << 'INNER_EOF' >> app/src/main/java/com/example/util/CloudSyncManager.kt

    suspend fun uploadProofImage(uri: Uri): String? = withContext(Dispatchers.IO) {
        val st = storage ?: return@withContext null
        try {
            val fileName = "proofs/${UUID.randomUUID()}.jpg"
            val ref = st.reference.child(fileName)
            ref.putFile(uri).await()
            val downloadUrl = ref.downloadUrl.await()
            return@withContext downloadUrl.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload proof image", e)
            return@withContext null
        }
    }
}
INNER_EOF
sed -i 's/^}$//g' app/src/main/java/com/example/util/CloudSyncManager.kt
echo "}" >> app/src/main/java/com/example/util/CloudSyncManager.kt

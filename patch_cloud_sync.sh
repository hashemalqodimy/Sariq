sed -i '10a import com.google.firebase.storage.FirebaseStorage\nimport android.net.Uri\nimport java.util.UUID' app/src/main/java/com/example/util/CloudSyncManager.kt

sed -i '/private val firestore: FirebaseFirestore? by lazy {/a \    private val storage: FirebaseStorage? by lazy {\n        try {\n            FirebaseStorage.getInstance()\n        } catch (e: Exception) {\n            null\n        }\n    }' app/src/main/java/com/example/util/CloudSyncManager.kt


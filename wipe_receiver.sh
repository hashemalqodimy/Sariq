cat << 'INNER_EOF' > app/src/main/java/com/example/util/AmanSyncReceiver.kt
package com.example.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AmanSyncReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AmanSyncReceiver", "Sync triggered")
    }
}
INNER_EOF

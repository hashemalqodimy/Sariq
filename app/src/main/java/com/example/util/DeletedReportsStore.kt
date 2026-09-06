package com.example.util

import android.content.Context

/**
 * Local tombstone list of IMEIs whose reports were deleted by an administrator.
 * The public ntfy/central-hub feeds cannot be purged, so without this the periodic
 * sync would immediately re-insert a report that was just deleted.
 */
object DeletedReportsStore {
    private const val PREFS = "aman_deleted_reports"
    private const val KEY = "deleted_imeis"

    fun getDeletedImeis(context: Context): Set<String> =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getStringSet(KEY, emptySet())
            ?.toSet() ?: emptySet()

    fun markDeleted(context: Context, imei: String) {
        val clean = imei.filter { it.isDigit() }
        if (clean.isBlank()) return
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val updated = (prefs.getStringSet(KEY, emptySet()) ?: emptySet()).toMutableSet()
        updated.add(clean)
        prefs.edit().putStringSet(KEY, updated).apply()
    }

    fun unmark(context: Context, imei: String) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val updated = (prefs.getStringSet(KEY, emptySet()) ?: emptySet()).toMutableSet()
        if (updated.remove(imei.filter { it.isDigit() })) {
            prefs.edit().putStringSet(KEY, updated).apply()
        }
    }
}

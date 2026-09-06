package com.example.util

import com.example.data.model.AppUser

/**
 * Single source of truth for administrator authorization inside the app.
 * Server-side enforcement lives in firestore.rules; this only gates UI access.
 */
object AdminPolicy {
    private const val ADMIN_EMAIL = "hashem714pro@gmail.com"

    /** Providers whose identity was verified by an external identity provider. */
    private val VERIFIED_PROVIDERS = setOf("GOOGLE", "FIREBASE_EMAIL")

    fun isAdminEmail(email: String): Boolean = email.trim().equals(ADMIN_EMAIL, ignoreCase = true)

    /**
     * Admin UI is shown only when the email matches AND the session was
     * authenticated by Firebase/Google – never for locally created accounts.
     */
    fun isAdmin(user: AppUser?): Boolean {
        if (user == null) return false
        return isAdminEmail(user.email) && user.authProvider in VERIFIED_PROVIDERS
    }
}

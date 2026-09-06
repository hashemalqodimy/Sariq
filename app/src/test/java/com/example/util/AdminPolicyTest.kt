package com.example.util

import com.example.data.model.AppUser
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdminPolicyTest {

    private fun user(email: String, provider: String) =
        AppUser(email = email, fullName = "t", authProvider = provider)

    @Test
    fun `admin email via Google or Firebase is admin`() {
        assertTrue(AdminPolicy.isAdmin(user("hashem714pro@gmail.com", "GOOGLE")))
        assertTrue(AdminPolicy.isAdmin(user("Hashem714Pro@Gmail.com", "FIREBASE_EMAIL")))
    }

    @Test
    fun `admin email via local offline account is NOT admin`() {
        // Regression: previously anyone could type the admin email offline and get the admin panel.
        assertFalse(AdminPolicy.isAdmin(user("hashem714pro@gmail.com", "EMAIL")))
        assertFalse(AdminPolicy.isAdmin(user("hashem714pro@gmail.com", "GUEST")))
    }

    @Test
    fun `other emails are never admin`() {
        assertFalse(AdminPolicy.isAdmin(user("someone@gmail.com", "GOOGLE")))
        assertFalse(AdminPolicy.isAdmin(null))
    }
}

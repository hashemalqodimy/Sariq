package com.example.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordHasherTest {

    @Test
    fun `hash verifies with the same password`() {
        val stored = PasswordHasher.hash("S3cret!pass")
        assertTrue(PasswordHasher.isHashed(stored))
        assertTrue(PasswordHasher.verify("S3cret!pass", stored))
    }

    @Test
    fun `hash rejects a different password`() {
        val stored = PasswordHasher.hash("correct-horse")
        assertFalse(PasswordHasher.verify("wrong-horse", stored))
        assertFalse(PasswordHasher.verify("", stored))
    }

    @Test
    fun `same password produces different hashes thanks to random salt`() {
        assertNotEquals(PasswordHasher.hash("abc123"), PasswordHasher.hash("abc123"))
    }

    @Test
    fun `plaintext is never stored`() {
        val stored = PasswordHasher.hash("VisiblePassword")
        assertFalse(stored.contains("VisiblePassword"))
    }

    @Test
    fun `legacy plaintext records still verify for migration`() {
        assertTrue(PasswordHasher.verify("legacy123", "legacy123"))
        assertFalse(PasswordHasher.verify("legacy124", "legacy123"))
        assertFalse(PasswordHasher.isHashed("legacy123"))
    }

    @Test
    fun `empty or malformed stored values never verify`() {
        assertFalse(PasswordHasher.verify("x", ""))
        assertFalse(PasswordHasher.verify("x", "pbkdf2\$bad"))
        assertFalse(PasswordHasher.verify("x", "pbkdf2\$notanumber\$zz\$zz"))
    }
}

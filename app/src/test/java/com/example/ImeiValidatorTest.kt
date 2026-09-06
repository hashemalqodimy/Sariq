package com.example

import com.example.util.ImeiValidator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImeiValidatorTest {

    @Test
    fun testValidImei_ReturnsTrue() {
        // A standard test IMEI that passes Luhn check
        val validImei = "354123114567896" 
        assertTrue(ImeiValidator.isValidImei(validImei))
    }

    @Test
    fun testInvalidImei_ReturnsFalse() {
        val invalidImei = "354123114567891" 
        assertFalse(ImeiValidator.isValidImei(invalidImei))
    }

    @Test
    fun testShortImei_ReturnsFalse() {
        val shortImei = "1234567890"
        assertFalse(ImeiValidator.isValidImei(shortImei))
    }
}

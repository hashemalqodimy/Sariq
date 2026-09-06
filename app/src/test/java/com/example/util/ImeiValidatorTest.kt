package com.example.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImeiValidatorTest {

    @Test
    fun `accepts well-known valid IMEIs`() {
        assertTrue(ImeiValidator.isValidImei("490154203237518"))
        assertTrue(ImeiValidator.isValidImei("356938035643809"))
        assertTrue(ImeiValidator.isValidImei("000000000000000"))
    }

    @Test
    fun `rejects IMEI with bad Luhn check digit`() {
        assertFalse(ImeiValidator.isValidImei("490154203237519"))
        assertFalse(ImeiValidator.isValidImei("123456789012345"))
    }

    @Test
    fun `rejects wrong length`() {
        assertFalse(ImeiValidator.isValidImei(""))
        assertFalse(ImeiValidator.isValidImei("49015420323751"))    // 14 digits
        assertFalse(ImeiValidator.isValidImei("4901542032375180"))  // 16 digits
    }

    @Test
    fun `rejects non-digit characters`() {
        assertFalse(ImeiValidator.isValidImei("49015420323751A"))
        assertFalse(ImeiValidator.isValidImei("490154-20323751"))
        assertFalse(ImeiValidator.isValidImei("٤٩٠١٥٤٢٠٣٢٣٧٥١٨")) // Arabic-Indic digits
    }
}

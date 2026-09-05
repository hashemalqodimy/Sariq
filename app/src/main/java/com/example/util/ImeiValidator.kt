package com.example.util

object ImeiValidator {
    /**
     * Checks if the given string is a valid 15-digit IMEI using the Luhn algorithm.
     */
    fun isValidImei(imei: String): Boolean {
        if (imei.length != 15 || !imei.all { it.isDigit() }) {
            return false
        }
        
        var sum = 0
        for (i in 0 until 15) {
            var digit = imei[i].toString().toInt()
            // Even positions (1-based index) are multiplied by 2
            // Since our array is 0-indexed, even positions have odd indices (1, 3, 5...)
            if (i % 2 != 0) {
                digit *= 2
                if (digit > 9) {
                    digit -= 9
                }
            }
            sum += digit
        }
        return sum % 10 == 0
    }
}

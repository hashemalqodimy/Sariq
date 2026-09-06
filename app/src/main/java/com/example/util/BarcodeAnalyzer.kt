package com.example.util

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class BarcodeAnalyzer(
    private val onBarcodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_CODE_128,
            Barcode.FORMAT_CODE_39,
            Barcode.FORMAT_CODE_93,
            Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_UPC_A,
            Barcode.FORMAT_UPC_E,
            Barcode.FORMAT_QR_CODE
        )
        .build()

    private val scanner = BarcodeScanning.getClient(options)
    @Volatile
    private var isScanning = true
    @Volatile
    private var isClosed = false

    /** Releases the ML Kit detector. Must be called when the analyzer is no longer used. */
    fun close() {
        isClosed = true
        isScanning = false
        try {
            scanner.close()
        } catch (_: Exception) {
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        if (!isScanning || isClosed) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    val rawValue = barcodes.firstOrNull()?.rawValue
                    if (rawValue != null) {
                        // Attempt to extract digits if it looks like an IMEI, or just return the raw string
                        val digits = rawValue.filter { it.isDigit() }
                        // Most IMEI barcodes start with some characters or are just the 15 digits
                        // We will return the first 15-digit sequence found, or just the raw barcode
                        val imeiRegex = Regex("\\d{15}")
                        val match = imeiRegex.find(digits)
                        if (match != null) {
                            isScanning = false
                            onBarcodeScanned(match.value)
                        } else if (digits.length >= 14) {
                            isScanning = false
                            onBarcodeScanned(digits)
                        } else if (rawValue.isNotEmpty()) {
                            // Let the caller handle it
                            isScanning = false
                            onBarcodeScanned(rawValue)
                        }
                    }
                }
                .addOnFailureListener {
                    // Handle error
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}

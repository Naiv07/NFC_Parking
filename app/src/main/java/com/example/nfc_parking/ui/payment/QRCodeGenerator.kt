package com.example.nfc_parking.ui.payment

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import java.util.UUID

object QRCodeGenerator {

    fun generateBookingId(): String {
        val id = "BID-${UUID.randomUUID().toString().takeLast(12).uppercase()}"
        Log.d("QRCodeGenerator", "Generated Booking ID: $id")
        return id
    }

    fun generateQRCode(
        content: String,
        size: Int = 512
    ): ImageBitmap? {
        return try {
            Log.d("QRCodeGenerator", "Generating QR code for: $content")

            // Generate QR code bit matrix
            val bitMatrix: BitMatrix = MultiFormatWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                size,
                size
            )

            Log.d("QRCodeGenerator", "QR encoding successful, creating bitmap...")

            // Create bitmap from bit matrix
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(
                        x,
                        y,
                        if (bitMatrix[x, y]) android.graphics.Color.BLACK
                        else android.graphics.Color.WHITE
                    )
                }
            }

            Log.d("QRCodeGenerator", "✅ QR code generated successfully!")
            bitmap.asImageBitmap()

        } catch (e: Exception) {
            Log.e("QRCodeGenerator", "❌ Error generating QR code: ${e.message}", e)
            e.printStackTrace()
            null
        }
    }
}
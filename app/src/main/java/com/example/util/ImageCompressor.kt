package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.max

data class CompressedLeafImage(
    val bitmap: Bitmap,
    val sizeKb: Int,
    val width: Int,
    val height: Int
)

object ImageCompressor {

    private const val MAX_EDGE_PX = 1024
    private const val JPEG_QUALITY = 80

    fun createTempPhotoFile(context: Context): File {
        val captureDir = File(context.cacheDir, "leaf_captures").apply { mkdirs() }
        return File(captureDir, "leaf_${System.currentTimeMillis()}.jpg")
    }

    fun createTempCameraUri(context: Context): Uri {
        val photoFile = createTempPhotoFile(context)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
    }

    suspend fun loadAndCompress(context: Context, uri: Uri): CompressedLeafImage =
        withContext(Dispatchers.IO) {
            val resolver = context.contentResolver

            // 1. Read bounds only to keep memory footprint minimal on 4GB RAM devices
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            resolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOptions)
            } ?: throw IllegalStateException("Unable to open selected image.")

            val sampleSize = calculateInSampleSize(
                boundsOptions.outWidth,
                boundsOptions.outHeight,
                MAX_EDGE_PX,
                MAX_EDGE_PX
            )

            // 2. Decode subsampled bitmap
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val decoded = resolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: throw IllegalStateException("Could not decode the selected image.")

            // 3. Apply EXIF orientation if present
            val rotationDegrees = resolver.openInputStream(uri)?.use { stream ->
                readExifRotation(stream)
            } ?: 0

            val oriented = if (rotationDegrees != 0) {
                val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                val rotated = Bitmap.createBitmap(
                    decoded,
                    0,
                    0,
                    decoded.width,
                    decoded.height,
                    matrix,
                    true
                )
                if (rotated !== decoded) decoded.recycle()
                rotated
            } else {
                decoded
            }

            // 4. Scale down strictly to <= 1024px longest edge
            val scaled = scaleDownIfNeeded(oriented, MAX_EDGE_PX)
            if (scaled !== oriented) oriented.recycle()

            // 5. Re-encode as compressed JPEG so payload is lightweight (~80-220 KB)
            val baos = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, baos)
            val jpegBytes = baos.toByteArray()
            val finalBitmap = if (jpegBytes.isNotEmpty()) {
                BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size) ?: scaled
            } else {
                scaled
            }
            if (finalBitmap !== scaled) scaled.recycle()

            val sizeKb = max(1, jpegBytes.size / 1024)
            CompressedLeafImage(
                bitmap = finalBitmap,
                sizeKb = sizeKb,
                width = finalBitmap.width,
                height = finalBitmap.height
            )
        }

    private fun readExifRotation(stream: java.io.InputStream): Int {
        return try {
            val exif = ExifInterface(stream)
            when (exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (_: Exception) {
            0
        }
    }

    private fun scaleDownIfNeeded(bitmap: Bitmap, maxEdge: Int): Bitmap {
        val longest = max(bitmap.width, bitmap.height)
        if (longest <= maxEdge) return bitmap
        val ratio = maxEdge.toFloat() / longest.toFloat()
        val targetW = max(1, (bitmap.width * ratio).toInt())
        val targetH = max(1, (bitmap.height * ratio).toInt())
        return Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
    }

    private fun calculateInSampleSize(
        width: Int,
        height: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}

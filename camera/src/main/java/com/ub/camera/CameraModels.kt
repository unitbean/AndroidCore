package com.ub.camera

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import androidx.camera.core.ImageCapture
import java.io.File
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Locale

sealed class CameraOutputVariant {
    internal abstract fun toOptions(context: Context): ImageCapture.OutputFileOptions
}

class CameraScopedStorage(
    private val filename: String = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
        .format(System.currentTimeMillis()),
    private val relativeFolder: String = "pictures",
    private val mimeType: String = "image/jpeg",
) : CameraOutputVariant() {
    override fun toOptions(context: Context): ImageCapture.OutputFileOptions {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, relativeFolder)
            }
        }

        return ImageCapture.OutputFileOptions
            .Builder(
                context.contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()
    }
}

class CameraOutputStream(
    private val outputStream: OutputStream
) : CameraOutputVariant() {
    override fun toOptions(context: Context): ImageCapture.OutputFileOptions = ImageCapture.OutputFileOptions
        .Builder(outputStream)
        .build()
}

class CameraFile(
    private val file: File
) : CameraOutputVariant() {
    override fun toOptions(context: Context): ImageCapture.OutputFileOptions = ImageCapture.OutputFileOptions
        .Builder(file)
        .build()
}
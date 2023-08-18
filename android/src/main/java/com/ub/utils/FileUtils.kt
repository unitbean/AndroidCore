package com.ub.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.annotation.WorkerThread
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.lang.NullPointerException

/**
 * Remove [file] from specified folder in defined [authority]
 *
 * @return marker, that indicates is file was deleted or not
 */
fun Context.deleteFile(
    file: File,
    authority: String
): Boolean {
    val uriToDelete = FileProvider.getUriForFile(this, authority, file)
    return contentResolver.delete(uriToDelete, null, null) == 1
}

/**
 * Creates file with name [nameWithExtension] in [folder] and write [inputContent] into this
 *
 * Content, if the file is already exists, will be rewrited
 *
 * @param folder can be default with [Context.getFilesDir]
 *
 * @return created file with [inputContent]
 */
@WorkerThread
fun createFileWithContent(
    inputContent: InputStream,
    nameWithExtension: String,
    folder: File
): File {
    val fileToSave = File(folder, nameWithExtension)
    if (!fileToSave.exists()) {
        fileToSave.createNewFile()
    }
    inputContent.use { inputStream ->
        fileToSave.outputStream().use { fileStream ->
            inputStream.copyTo(fileStream)
        }
    }
    return fileToSave
}

/**
 * Creates a new [Uri] in [folder], define in [authority], which is ready to write data into self
 *
 * **Strong note:** after call of this functions file is not will be created
 *
 * @return [Uri], which is ready to use
 */
fun Context.createUriReadyForWrite(
    nameWithExtension: String,
    authority: String,
    folder: File = filesDir,
): Uri {
    val file = File(folder, nameWithExtension)
    return FileProvider.getUriForFile(this, authority, file)
}

/**
 * Get file name by [Uri]
 *
 * @return name with extension or not
 */
fun Context.getFileNameFromUri(uri: Uri): String? {
    return contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val isFound = cursor.moveToFirst()
        if (isFound) {
            val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.getString(columnIndex)
        } else null
    }
}

/**
 * Get file size by [Uri]
 *
 * @return size in bytes or not
 */
fun Context.getFileSizeByUri(uri: Uri): Long? {
    return contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val isFound = cursor.moveToFirst()
        if (isFound) {
            val columnIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            cursor.getLong(columnIndex)
        } else null
    }
}

/**
 * Get image from [imageUri]
 *
 * If EXIF data in image contains [ExifInterface.TAG_ORIENTATION] attribute, this will be applied
 *
 * Image in [imageUri] must be placed in authority in one of [android.content.ContentProvider]'s
 * (include app's manifest). If you use picking image from external source, this will be ok,
 * this will be provide by itself. But if you try to get access to external memory without request
 * a scoped storage access or file in app inner folder and will not be specified this in your app
 * manifest, the [FileNotFoundException] will be thrown
 *
 * @throws NullPointerException is the [imageUri] contains empty content
 * @throws IOException if the content [imageUri] is not found,
 * is an unsupported format, or cannot be decoded by [ImageDecoder.decodeBitmap] for any reason
 *
 * @return rotated bitmap of image
 */
@Throws(exceptionClasses = [
    NullPointerException::class,
    FileNotFoundException::class,
    IOException::class,
    ImageDecoder.DecodeException::class
])
@WorkerThread
fun Context.getImage(imageUri: Uri): Bitmap {
    val isApiPieOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
    val bitmap = if (isApiPieOrLater) {
        ImageDecoder.createSource(contentResolver, imageUri).let { source ->
            ImageDecoder.decodeBitmap(source)
        }
    } else {
        @Suppress("DEPRECATION")
        MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
    }

    fun rotateBitmapIfNeeded(angle: Float, isNeedRotate: Boolean): Bitmap {
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, Matrix().apply {
            if (isNeedRotate) {
                postRotate(angle)
            }
        },true)
    }

    return when (
        contentResolver.openInputStream(imageUri)?.use { inputStream ->
            ExifInterface(inputStream).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )
        } ?: return bitmap
    ) {
        ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmapIfNeeded(90F, !isApiPieOrLater)
        ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmapIfNeeded(180F, !isApiPieOrLater)
        ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmapIfNeeded(270F, !isApiPieOrLater)
        else -> bitmap
    }
}
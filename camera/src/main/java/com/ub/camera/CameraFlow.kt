package com.ub.camera

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresPermission
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.suspendCoroutine

/**
 * Настраиваемый компонент камеры
 *
 * [Референс](https://developer.android.com/codelabs/camerax-getting-started)
 *
 * [Пак примеров](https://github.com/android/camera-samples)
 *
 * @param lifecycleOwner для привязки к жизненному циклу родителя
 * @param previewView представление для отображения видоискателя
 */
class CameraFlow(
    private val lifecycleOwner: LifecycleOwner,
    private val previewView: PreviewView
) {

    private var cameraExecutor: ExecutorService? = null
    private var imageCapture: ImageCapture? = null

    private val resultChannel: Channel<Result<Unit>> = Channel<Result<Unit>>(capacity = Channel.BUFFERED).apply {
        invokeOnClose {
            cameraExecutor?.shutdown()
        }
    }

    private val availableCamerasMutable = mutableListOf<CameraInfo>()
    val availableCameras: List<CameraInfo> = availableCamerasMutable

    // TODO включение/выключение фонарика
    var flashlight: Boolean
        set(value) {

        }
        get() {
            return false
        }

    // TODO переключение на конкретную камеру
    fun selectCamera(camera: CameraInfo) {

    }

    @RequiresPermission(value = Manifest.permission.CAMERA)
    fun startPreview(): Flow<Result<Unit>> {

        if (ContextCompat.checkSelfPermission(previewView.context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            resultChannel.trySend(Result.failure(SecurityException("Camera permission was not granted")))
        } else {
            cameraExecutor = Executors.newSingleThreadExecutor()

            val cameraProviderFuture = ProcessCameraProvider.getInstance(previewView.context)
            cameraProviderFuture.addListener({
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                availableCamerasMutable.clear()
                availableCamerasMutable.addAll(cameraProvider.availableCameraInfos)

                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                imageCapture = ImageCapture.Builder()
                    .build()

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner, cameraSelector, preview, imageCapture
                    )
                } catch (exc: Exception) {
                    resultChannel.trySend(Result.failure(exc))
                    return@addListener
                }

                resultChannel.trySend(Result.success(Unit))
            }, ContextCompat.getMainExecutor(previewView.context))
        }

        return resultChannel.receiveAsFlow()
    }

    /**
     * TODO разные опции сохранения
     */
    @RequiresPermission(value = Manifest.permission.WRITE_EXTERNAL_STORAGE)
    suspend fun takePhoto(
        filename: String = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
            .format(System.currentTimeMillis())
    ) : Uri = suspendCoroutine {
        imageCapture?.let { capture ->
            val outputOptions = outputOptionsScopedStorage(filename)

            capture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(previewView.context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exc: ImageCaptureException) = it.resumeWith(Result.failure(exc))

                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        output.savedUri?.let { uri ->
                            it.resumeWith(Result.success(uri))
                        } ?: it.resumeWith(Result.failure(NullPointerException("Camera result is null")))
                    }
                }
            )
        } ?: it.resumeWith(Result.failure(NullPointerException("ImageCapture instance is null")))
    }

    private fun outputOptionsScopedStorage(filename: String): ImageCapture.OutputFileOptions {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        return ImageCapture.OutputFileOptions
            .Builder(
                previewView.context.contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()
    }
}
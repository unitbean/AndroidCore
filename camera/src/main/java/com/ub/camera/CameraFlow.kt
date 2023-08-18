package com.ub.camera

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.annotation.RequiresPermission
import androidx.camera.core.Camera
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
    private var selectedCamera: CameraInfo? = null
    private var camera: Camera? = null

    private val resultChannel: Channel<Result<Unit>> = Channel<Result<Unit>>(capacity = Channel.BUFFERED).apply {
        invokeOnClose {
            cameraExecutor?.shutdown()
        }
    }

    private val availableCamerasMutable = mutableListOf<CameraInfo>()
    val availableCameras: List<CameraInfo> = availableCamerasMutable

    fun setFlashlight(isEnabled: Boolean) {
        camera?.cameraControl?.enableTorch(isEnabled)
    }

    fun selectCamera(camera: CameraInfo) {
        this.selectedCamera = camera
        initCamera()
    }

    @RequiresPermission(value = Manifest.permission.CAMERA)
    fun startPreview(): Flow<Result<Unit>> {
        initCamera()
        return resultChannel.receiveAsFlow()
    }

    @RequiresPermission(value = Manifest.permission.WRITE_EXTERNAL_STORAGE)
    suspend fun takePhoto(
        variant: CameraOutputVariant
    ) : Uri = suspendCoroutine {
        imageCapture?.let { capture ->
            val outputOptions = variant.toOptions(previewView.context)

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

    private fun initCamera() {
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

                val cameraSelector = if (selectedCamera == null) {
                    CameraSelector.DEFAULT_BACK_CAMERA
                } else {
                    CameraSelector.Builder()
                        .addCameraFilter { cameraInfos -> cameraInfos.filter { it == selectedCamera } }
                        .build()
                }

                try {
                    cameraProvider.unbindAll()
                    camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner, cameraSelector, preview, imageCapture
                    )
                } catch (exc: Exception) {
                    resultChannel.trySend(Result.failure(exc))
                    return@addListener
                }

                resultChannel.trySend(Result.success(Unit))
            }, ContextCompat.getMainExecutor(previewView.context))
        }
    }
}
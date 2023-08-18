package com.ub.camera

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
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
import kotlinx.coroutines.guava.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
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

    private var imageCapture: ImageCapture? = null
    private var selectedCamera: CameraInfo? = null
    private var camera: Camera? = null

    private val availableCamerasMutable = mutableListOf<CameraInfo>()

    /**
     * Available [Camera]'s for device
     *
     * Empty when permission [android.Manifest.permission.CAMERA] was not granted
     */
    val availableCameras: List<CameraInfo> = availableCamerasMutable

    /**
     * Enable or disable flashlight, if it presenter
     */
    suspend fun setFlashlight(isEnabled: Boolean) {
        camera?.cameraControl?.enableTorch(isEnabled)?.await()
    }

    /**
     * Pick another available [Camera]
     *
     * @param camera target camera instance
     */
    suspend fun selectCamera(camera: CameraInfo) {
        this.selectedCamera = camera
        initCamera()
    }

    /**
     * Starting camera session
     *
     * Required [android.Manifest.permission.CAMERA] for work
     *
     * @return empty result or [Exception] instance
     */
    suspend fun startPreview(): Result<Unit> = initCamera()

    /**
     * Take photo for [variant]
     *
     * Required [android.Manifest.permission.WRITE_EXTERNAL_STORAGE] for
     * [android.os.Build.VERSION.SDK_INT] <= [android.os.Build.VERSION_CODES.P] for work
     *
     * @param variant type of output data, must be one of descendants [CameraOutputVariant]
     * @return [Uri] with output data
     */
    suspend fun takePhoto(
        variant: CameraOutputVariant
    ): Uri = suspendCoroutine { continuation ->
        imageCapture?.takePicture(
            variant.toOptions(previewView.context),
            ContextCompat.getMainExecutor(previewView.context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) = continuation.resumeWithException(exc)

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    output.savedUri?.let { uri ->
                        continuation.resume(uri)
                    } ?: continuation.resumeWithException(NullPointerException("Camera result is null"))
                }
            }
        ) ?: continuation.resumeWithException(NullPointerException("ImageCapture instance is null"))
    }

    private suspend fun initCamera(): Result<Unit> {
        if (ContextCompat.checkSelfPermission(previewView.context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return Result.failure(SecurityException("Camera permission was not granted"))
        } else {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(previewView.context.applicationContext)
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.await()

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
                return Result.failure(exc)
            }

            return Result.success(Unit)
        }
    }
}
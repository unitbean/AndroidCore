package com.ub.camera

import android.net.Uri
import androidx.camera.core.Camera
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.guava.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Custom camera component
 *
 * Can be used in any types of places
 *
 * Support only taking a photo with calling [takePhoto]
 *
 * Source links: [reference](https://developer.android.com/codelabs/camerax-getting-started), [samples](https://github.com/android/camera-samples)
 *
 * @param lifecycleOwner for automatically handle parents lifecycle
 * @param previewView viewfinder presentation
 */
class CameraSession(
    private val lifecycleOwner: LifecycleOwner,
    private val previewView: PreviewView
) {

    private var imageCapture: ImageCapture? = null
    private var selectedCamera: CameraInfo? = null
    private var camera: Camera? = null

    private val _state = MutableStateFlow(CameraState())
    val state = _state.asStateFlow()

    /**
     * Enable or disable flashlight, if it present
     */
    suspend fun toggleFlashlight() {
        if (state.value.isFlashIsAvailable) {
            val torchState = camera?.cameraInfo?.torchState?.value ?: TorchState.OFF
            val newState = torchState == TorchState.OFF
            camera?.cameraControl?.enableTorch(newState)?.await()
            _state.update { state -> state.copy(isTorchEnabled = newState) }
        }
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
     * As and for all others cold flows, you must call [Flow.collect] to start camera session
     *
     * Required [android.Manifest.permission.CAMERA] for work
     *
     * @return state of camera session in [CameraState] flow
     */
    fun startSession(): Flow<CameraState> {
        return state.onStart { initCamera() }
    }

    /**
     * Updates current [CameraSession] instance
     *
     * This can be useful for, example, resolving granted permissions for update current session
     */
    suspend fun updateSession() {
        initCamera()
    }

    /**
     * Take photo for [variant]
     *
     * Required [android.Manifest.permission.WRITE_EXTERNAL_STORAGE] for
     * [android.os.Build.VERSION.SDK_INT] <= [android.os.Build.VERSION_CODES.P] for work
     *
     * @param variant type of output data, must be one of descendants [CameraOutputVariant]
     * @return [Uri] with output data. May be null if [variant] is [CameraOutputStream]
     */
    suspend fun takePhoto(
        variant: CameraOutputVariant
    ): Uri? = suspendCoroutine { continuation ->
        imageCapture?.takePicture(
            variant.toOptions(previewView.context),
            ContextCompat.getMainExecutor(previewView.context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) = continuation.resumeWithException(exc)

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    output.savedUri?.let { uri ->
                        continuation.resume(uri)
                    } ?: continuation.resume(null)
                }
            }
        ) ?: continuation.resumeWithException(NullPointerException("ImageCapture instance is null"))
    }

    private suspend fun initCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(previewView.context.applicationContext)
        val cameraProvider: ProcessCameraProvider = cameraProviderFuture.await()

        _state.update { state -> state.copy(availableCameras = cameraProvider.availableCameraInfos) }

        val preview = Preview.Builder()
            .build()

        preview.setSurfaceProvider(previewView.surfaceProvider)

        imageCapture = ImageCapture.Builder()
            .build()

        val cameraSelector = if (selectedCamera == null) {
            CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            CameraSelector.Builder()
                .addCameraFilter { cameraInfo -> cameraInfo.filter { it == selectedCamera } }
                .build()
        }

        cameraProvider.unbindAll()
        camera = cameraProvider.bindToLifecycle(
            lifecycleOwner, cameraSelector, preview, imageCapture
        )
        _state.update { state ->
            state.copy(
                isFlashIsAvailable = camera?.cameraInfo?.hasFlashUnit() ?: false,
                isTorchEnabled = false
            )
        }
    }
}
package com.ub.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.hardware.camera2.params.SessionConfiguration.SESSION_REGULAR
import android.hardware.camera2.params.StreamConfigurationMap
import android.media.Image
import android.media.ImageReader
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.core.os.ExecutorCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment

/**
 * Настраиваемый компонент камеры
 *
 * Необходимо для использования:
 * - наличие [TextureView] с идентификатором [R.id.texture_view]
 * - наличие [android.widget.View] с идентификатором [R.id.capture_button]
 */
class CameraFragment : Fragment() {

    private val cameraManager: CameraManager? by lazy {
        val context = requireContext().applicationContext
        ContextCompat.getSystemService(context, CameraManager::class.java)
    }

    private var textureView: TextureView? = null
    private var cameraDevice: CameraDevice? = null
    private var backgroundHandler: Handler? = null
    private var backgroundHandlerThread: HandlerThread? = null
    private var captureRequestBuilder: CaptureRequest.Builder? = null
    private var cameraCaptureSession: CameraCaptureSession? = null
    private var previewSize: Size? = null
    private var imageReader: ImageReader? = null
    private var cameraId: String? = null
    private var shouldProceedWithOnResume: Boolean = true
    private var orientations : SparseIntArray = SparseIntArray(4).apply {
        append(Surface.ROTATION_0, 0)
        append(Surface.ROTATION_90, 90)
        append(Surface.ROTATION_180, 180)
        append(Surface.ROTATION_270, 270)
    }

    private val onImageAvailableListener = ImageReader.OnImageAvailableListener { reader ->
        Toast.makeText(requireContext(), "Photo Taken!", Toast.LENGTH_SHORT).show()
        val image: Image = reader.acquireLatestImage()
        image.close()
    }

    private val captureStateCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigureFailed(session: CameraCaptureSession) = Unit
        override fun onConfigured(session: CameraCaptureSession) {
            cameraCaptureSession = session

            cameraCaptureSession?.setRepeatingRequest(
                captureRequestBuilder?.build()!!,
                null,
                backgroundHandler
            )
        }
    }

    private val cameraStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            val surfaceTexture : SurfaceTexture? = textureView?.surfaceTexture
            surfaceTexture?.setDefaultBufferSize(previewSize?.width!!, previewSize?.height!!)
            val previewSurface = Surface(surfaceTexture)

            captureRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)?.apply {
                addTarget(previewSurface)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                cameraDevice?.createCaptureSession(
                    SessionConfiguration(
                        SESSION_REGULAR,
                        listOf(OutputConfiguration(previewSurface), OutputConfiguration(imageReader?.surface!!)),
                        ExecutorCompat.create(Handler(Looper.getMainLooper())),
                        captureStateCallback
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                cameraDevice?.createCaptureSession(listOf(previewSurface, imageReader?.surface), captureStateCallback, null)
            }
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {

        }

        override fun onError(cameraDevice: CameraDevice, error: Int) {
            val errorMsg = when(error) {
                ERROR_CAMERA_DEVICE -> "Fatal (device)"
                ERROR_CAMERA_DISABLED -> "Device policy"
                ERROR_CAMERA_IN_USE -> "Camera in use"
                ERROR_CAMERA_SERVICE -> "Fatal (service)"
                ERROR_MAX_CAMERAS_IN_USE -> "Maximum cameras in use"
                else -> "Unknown"
            }
            Log.e("CAMERA_TAG", "Error when trying to connect camera $errorMsg")
        }
    }

    private val captureCallback = object : CameraCaptureSession.CaptureCallback() {
        override fun onCaptureStarted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            timestamp: Long,
            frameNumber: Long
        ) = Unit
        override fun onCaptureProgressed(
            session: CameraCaptureSession,
            request: CaptureRequest,
            partialResult: CaptureResult
        ) = Unit
        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) = Unit
    }

    private val surfaceTextureListener = object : TextureView.SurfaceTextureListener {
        @SuppressLint("MissingPermission")
        override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) {
            if (wasCameraPermissionWasGiven()) {
                setupCamera()
                connectCamera()
            }
        }

        override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) {

        }

        override fun onSurfaceTextureDestroyed(texture: SurfaceTexture): Boolean {
            return true
        }

        override fun onSurfaceTextureUpdated(texture: SurfaceTexture) {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layoutRes = arguments?.getInt(LAYOUT_ID) ?: throw IllegalArgumentException("LayoutId is null")
        val view = inflater.inflate(layoutRes, null, false)
        if (view.findViewById<View>(R.id.texture_view) == null) {
            throw IllegalArgumentException("TextureView is not set")
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textureView = view.findViewById(R.id.texture_view)
        view.findViewById<Button>(R.id.capture_button).setOnClickListener {
            takePhoto()
        }
    }

    override fun onResume() {
        super.onResume()
        startBackgroundThread()
        if (textureView?.isAvailable == true && shouldProceedWithOnResume) {
            setupCamera()
        } else if (textureView?.isAvailable != true) {
            textureView?.surfaceTextureListener = surfaceTextureListener
        }
        shouldProceedWithOnResume = !shouldProceedWithOnResume
    }

    private fun startBackgroundThread() {
        backgroundHandlerThread = HandlerThread("CameraVideoThread")
        backgroundHandlerThread?.start()
        backgroundHandlerThread?.looper?.let { backgroundHandler = Handler(it) }
    }

    private fun stopBackgroundThread() {
        backgroundHandlerThread?.quitSafely()
        backgroundHandlerThread?.join()
    }

    private fun setupCamera() {
        val cameraIds: Array<String> = cameraManager?.cameraIdList ?: emptyArray()

        for (id in cameraIds) {
            val cameraCharacteristics = cameraManager?.getCameraCharacteristics(id)

            //If we want to choose the rear facing camera instead of the front facing one
            if (cameraCharacteristics?.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                continue
            }

            val streamConfigurationMap : StreamConfigurationMap? = cameraCharacteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

            if (streamConfigurationMap != null) {
                previewSize = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)?.getOutputSizes(ImageFormat.JPEG)?.maxByOrNull { it.height * it.width }
                imageReader = ImageReader.newInstance(previewSize?.width!!, previewSize?.height!!, ImageFormat.JPEG, 1)
                imageReader?.setOnImageAvailableListener(onImageAvailableListener, backgroundHandler)
            }
            cameraId = id
        }
    }

    private fun takePhoto() {
        captureRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)?.also {
            it.addTarget(imageReader?.surface!!)
            val rotation = requireActivity().windowManager.defaultDisplay.rotation
            it.set(CaptureRequest.JPEG_ORIENTATION, orientations.get(rotation))
            cameraCaptureSession?.capture(it.build(), captureCallback, null)
        }
    }

    @SuppressLint("MissingPermission")
    private fun connectCamera() {
        cameraManager?.openCamera(cameraId!!, cameraStateCallback, backgroundHandler)
    }

    private fun wasCameraPermissionWasGiven() : Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    companion object {

        private const val LAYOUT_ID = "layout_id"

        /**
         * @param cameraFragmentId разметка камеры, которая должна включать в себе компоненты [R.id.capture_button], [R.id.capture_button]
         */
        fun newInstance(
            @LayoutRes cameraFragmentId: Int
        ): CameraFragment {
            return CameraFragment().apply {
                arguments = bundleOf(
                    LAYOUT_ID to cameraFragmentId
                )
            }
        }
    }
}
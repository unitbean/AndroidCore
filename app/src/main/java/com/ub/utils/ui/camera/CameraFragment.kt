package com.ub.utils.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.ub.camera.CameraFlow
import com.ub.camera.CameraOutputStream
import com.ub.camera.CameraExternalStorage
import com.ub.utils.R
import com.ub.utils.databinding.FragmentCameraBinding
import com.ub.utils.launchAndRepeatWithViewLifecycle
import dev.chrisbanes.insetter.applyInsetter
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class CameraFragment : Fragment(R.layout.fragment_camera) {

    private val permissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(requireContext(),
                    "Permission request denied",
                    Toast.LENGTH_SHORT).show()
            } else {

            }
        }

    private var binding: FragmentCameraBinding? = null

    private var isFlashlightEnabled = false
    private var cameraIndex = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentCameraBinding.bind(view)

        val photo = CameraFlow(
            lifecycleOwner = this,
            previewView = binding!!.previewView
        )

        binding?.flashlight?.applyInsetter {
            type(statusBars = true) {
                margin()
            }
        }
        binding?.switchCamera?.applyInsetter {
            type(statusBars = true) {
                margin()
            }
        }
        binding?.switchCamera?.setOnClickListener {
            if (photo.availableCameras.size.minus(1) == cameraIndex) {
                cameraIndex = 0
            } else {
                cameraIndex += 1
            }
            val camera = photo.availableCameras[cameraIndex]
            runBlocking { photo.selectCamera(camera) }
        }
        binding?.flashlight?.setOnClickListener {
            isFlashlightEnabled = !isFlashlightEnabled
            runBlocking { photo.setFlashlight(isFlashlightEnabled) }
        }

        binding?.captureExternalButton?.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val uri = photo.takePhoto(CameraExternalStorage())
                Toast.makeText(view.context, uri.path, Toast.LENGTH_LONG).show()
            }
        }

        launchAndRepeatWithViewLifecycle {
            launch {
                photo.startPreview()
            }
        }

        if (!allPermissionsGranted()) {
            requestPermissions()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun requestPermissions() {
        permissionLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}
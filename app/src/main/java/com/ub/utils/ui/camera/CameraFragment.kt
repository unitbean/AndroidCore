package com.ub.utils.ui.camera

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.ub.camera.CameraFlow
import com.ub.camera.CameraScopedStorage
import com.ub.utils.R
import com.ub.utils.databinding.FragmentCameraBinding
import com.ub.utils.launchAndRepeatWithViewLifecycle
import dev.chrisbanes.insetter.applyInsetter
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class CameraFragment : Fragment(R.layout.fragment_camera) {

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

        binding?.captureButton?.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val uri = photo.takePhoto(CameraScopedStorage())
                Toast.makeText(view.context, uri.path, Toast.LENGTH_LONG).show()
            }
        }

        launchAndRepeatWithViewLifecycle {
            launch {
                photo.startPreview()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
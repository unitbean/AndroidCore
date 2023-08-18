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
import java.text.SimpleDateFormat
import java.util.Locale

class CameraFragment : Fragment(R.layout.fragment_camera) {

    private var binding: FragmentCameraBinding? = null

    private var isFlashlightEnabled = false

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
        binding?.flashlight?.setOnClickListener {
            isFlashlightEnabled = !isFlashlightEnabled
            photo.setFlashlight(isFlashlightEnabled)
        }

        binding?.captureButton?.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val uri = photo.takePhoto(
                    CameraScopedStorage(
                        filename = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
                            .format(System.currentTimeMillis()),
                        relativeFolder =
                    )
                )
                Toast.makeText(view.context, uri.path, Toast.LENGTH_LONG).show()
            }
        }

        launchAndRepeatWithViewLifecycle {
            launch {
                photo.startPreview().collect()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
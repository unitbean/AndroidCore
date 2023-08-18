package com.ub.utils.ui.camera

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.ub.camera.CameraFlow
import com.ub.utils.R
import com.ub.utils.databinding.FragmentCameraBinding
import com.ub.utils.launchAndRepeatWithViewLifecycle
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CameraFragment : Fragment(R.layout.fragment_camera) {

    private var binding: FragmentCameraBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentCameraBinding.bind(view)

        val photo = CameraFlow(
            lifecycleOwner = this,
            previewView = binding!!.previewView
        )

        binding?.captureButton?.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val uri = photo.takePhoto()
                uri.hashCode()
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
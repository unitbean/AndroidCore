package com.ub.utils.ui.map

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ub.utils.BuildConfig
import com.ub.utils.R
import com.ub.utils.databinding.FragmentMapBinding
import com.ub.utils.launchAndRepeatWithViewLifecycle
import com.ub.yandex.YandexCameraEvent
import com.ub.yandex.YandexMapFragment
import com.ub.yandex.YandexMapReadyCallback
import com.ub.yandex.awaitMap
import com.ub.yandex.mapIdleFlow
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.Map
import dev.chrisbanes.insetter.applyInsetter
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch

class MapFragment : Fragment(R.layout.fragment_map), YandexMapReadyCallback {

    private val viewModel: MapViewModel by viewModels()

    private var binding: FragmentMapBinding? = null

    private val isMapIsHidden: Boolean
        get() = childFragmentManager.fragments.firstOrNull { it is YandexMapFragment } == null

    private val locationCollector = FlowCollector<YandexCameraEvent> {
        if (it.isFinished) {
            viewModel.geocodeLocation(it.cameraPosition)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentMapBinding.bind(view)

        if (isMapIsHidden) {
            binding?.showOrHideButton?.setText(R.string.map_show)
        } else {
            binding?.showOrHideButton?.setText(R.string.map_hide)
        }

        binding?.displayedLocation?.applyInsetter {
            type(statusBars = true) {
                margin()
            }
        }

        binding?.showOrHideButton?.setOnClickListener {
            if (isMapIsHidden) {
                binding?.showOrHideButton?.setText(R.string.map_hide)
                val fragmentToAdd = YandexMapFragment().apply {
                    this@MapFragment.launchAndRepeatWithViewLifecycle {
                        launch {
                            val map = this@apply.awaitMap()
                            map.mapIdleFlow.collect(locationCollector)
                        }
                    }
                    this.apiKeyDelegate.setApiKey(
                        BuildConfig.YANDEX_KEY
                    )
                    this.initialLocationDelegate.setInitialLocation(
                        CameraPosition(
                            Point(48.701792, 44.500053),
                            15F,
                            0F,
                            0F
                        )
                    )
                    this.setMapReady(this@MapFragment)
                }
                childFragmentManager.beginTransaction()
                    .add(R.id.map, fragmentToAdd)
                    .commit()
            } else {
                binding?.displayedLocation?.text = ""
                binding?.showOrHideButton?.setText(R.string.map_show)
                val existingFragment = binding?.map?.getFragment<YandexMapFragment>() ?: return@setOnClickListener
                childFragmentManager.beginTransaction()
                    .remove(existingFragment)
                    .commit()
            }
        }

        launchAndRepeatWithViewLifecycle {
            launch {
                val mapFragment = binding?.map?.getFragment<YandexMapFragment>()
                val map = mapFragment?.awaitMap()
                map?.mapIdleFlow?.collect(locationCollector)
            }
            launch {
                viewModel.geocodedLocation.collect { location ->
                    if (isMapIsHidden.not()) {
                        binding?.displayedLocation?.text = location
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onMapReady(yandexMap: Map) {
        Toast.makeText(
            requireContext(),
            "${yandexMap.cameraPosition.target.latitude} ${yandexMap.cameraPosition.target.longitude}",
            Toast.LENGTH_LONG
        ).show()
    }
}
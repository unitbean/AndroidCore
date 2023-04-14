package com.ub.utils.ui.map

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.ub.utils.BuildConfig
import com.ub.utils.R
import com.ub.utils.databinding.FragmentMapBinding
import com.ub.yandex.YandexMapFragment
import com.ub.yandex.YandexMapReadyDelegate
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.Map

class MapFragment : Fragment(R.layout.fragment_map), YandexMapReadyDelegate {

    private var binding: FragmentMapBinding? = null

    private val isMapIsHidden: Boolean
        get() = childFragmentManager.fragments.size == 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentMapBinding.bind(view)

        if (isMapIsHidden) {
            binding?.showOrHideButton?.setText(R.string.map_show)
        } else {
            binding?.showOrHideButton?.setText(R.string.map_hide)
        }

        binding?.showOrHideButton?.setOnClickListener {
            if (isMapIsHidden) {
                binding?.showOrHideButton?.setText(R.string.map_hide)
                val fragmentToAdd = YandexMapFragment().apply {
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
                binding?.showOrHideButton?.setText(R.string.map_show)
                val existingFragment = binding?.map?.getFragment<YandexMapFragment>() ?: return@setOnClickListener
                childFragmentManager.beginTransaction()
                    .remove(existingFragment)
                    .commit()
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
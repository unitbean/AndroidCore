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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentMapBinding.bind(view)

        val map = (childFragmentManager.findFragmentById(R.id.map)) as YandexMapFragment?

        map?.apiKeyDelegate?.setApiKey(
            BuildConfig.YANDEX_KEY
        )
        map?.initialLocationDelegate?.setInitialLocation(
            CameraPosition(
                Point(
                    48.701792,
                    44.500053
                ),
                15F,
                0F,
                0F
            )
        )
        map?.setMapReady(this)
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
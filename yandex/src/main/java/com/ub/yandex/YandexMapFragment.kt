package com.ub.yandex

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.mapview.MapView

class YandexMapFragment : Fragment() {

    val mapView: MapView?
        get() = view as? MapView

    private val viewModel: YandexViewModel by viewModels()

    private val mapInitializationDelegate by mapKitInitializer { apiKey ->
        MapKitFactory.setApiKey(apiKey)
        MapKitFactory.initialize(requireContext())
    }
    private val initialLocation by initialLocation { cameraPosition ->
        viewModel.savedCameraPosition = cameraPosition
    }

    private val apiReaderDelegate: ApiKeyReader = mapInitializationDelegate
    private var mapReadyDelegate: YandexMapReadyDelegate? = null


    val apiKeyDelegate: ApiKeyDelegate = mapInitializationDelegate
    val initialLocationDelegate: YandexInitialCameraPositionDelegate = initialLocation

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel.apiKey?.let { apiKey ->
            apiKeyDelegate.setApiKey(apiKey)
        }
        mapInitializationDelegate.initialize()
        if (viewModel.savedCameraPosition == null) {
            initialLocation.setUpInitialLocation()
        }
        return MapView(inflater.context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.savedCameraPosition?.let { position ->
            mapView?.map?.move(position)
        }
        mapView?.map?.let { mapReadyDelegate?.onMapReady(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView?.let { map ->
            viewModel.savedCameraPosition = map.map.cameraPosition
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.let { map ->
            viewModel.savedCameraPosition = map.map.cameraPosition
        }
        apiReaderDelegate.getApiKey()?.let { apiKey ->
            viewModel.apiKey = apiKey
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onMemoryWarning()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
        MapKitFactory.getInstance().onStop()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
        MapKitFactory.getInstance().onStart()
    }

    fun setMapReady(delegate: YandexMapReadyDelegate) {
        this.mapReadyDelegate = delegate
    }
}
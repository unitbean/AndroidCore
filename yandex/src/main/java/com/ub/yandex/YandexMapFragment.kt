package com.ub.yandex

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView

class YandexMapFragment : Fragment() {

    val mapView: MapView?
        get() = view as? MapView

    private var latestCameraZoom: Float? = null
    private var latestCameraAzimuth: Float? = null
    private var latestCameraTilt: Float? = null
    private var latestCameraPosition: Point? = null

    private val mapInitializationDelegate by mapInitializer { apiKey ->
        MapKitFactory.setApiKey(apiKey)
        MapKitFactory.initialize(requireContext())
    }

    val apiKeyDelegate: ApiKeyDelegate = mapInitializationDelegate
    private val apiReaderDelegate: ApiKeyReader = mapInitializationDelegate

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.let { map ->
            map.map.cameraPosition.run {
                latestCameraZoom = zoom
                latestCameraPosition = target
                latestCameraAzimuth = azimuth
                latestCameraTilt = tilt
            }
        }
        apiReaderDelegate.getApiKey()?.let { apiKey ->
            outState.putString(SAVED_API_KEY, apiKey)
        }
        latestCameraPosition?.let { position ->
            outState.putDouble(SAVED_LATITUDE, position.latitude)
            outState.putDouble(SAVED_LONGITUDE, position.longitude)
        }
        latestCameraZoom?.let { zoom ->
            outState.putFloat(SAVED_ZOOM, zoom)
        }
        latestCameraAzimuth?.let { azimuth ->
            outState.putFloat(SAVED_AZIMUTH, azimuth)
        }
        latestCameraTilt?.let { tilt ->
            outState.putFloat(SAVED_TILT, tilt)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (savedInstanceState?.containsKey(SAVED_API_KEY) == true) {
            apiKeyDelegate.setApiKey(savedInstanceState.getString(SAVED_API_KEY, null))
        }
        mapInitializationDelegate.initialize()
        if (savedInstanceState?.containsKey(SAVED_LATITUDE) == true
            && savedInstanceState.containsKey(SAVED_LONGITUDE)) {
            latestCameraPosition = Point(
                savedInstanceState.getDouble(SAVED_LATITUDE),
                savedInstanceState.getDouble(SAVED_LONGITUDE)
            )
        }
        if (savedInstanceState?.containsKey(SAVED_ZOOM) == true) {
            latestCameraZoom = savedInstanceState.getFloat(SAVED_ZOOM)
        }
        if (savedInstanceState?.containsKey(SAVED_AZIMUTH) == true) {
            latestCameraAzimuth = savedInstanceState.getFloat(SAVED_AZIMUTH)
        }
        if (savedInstanceState?.containsKey(SAVED_TILT) == true) {
            latestCameraTilt = savedInstanceState.getFloat(SAVED_TILT)
        }
        return MapView(inflater.context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        latestCameraPosition?.let { position ->
            if (latestCameraZoom == null) return
            mapView?.map?.move(
                CameraPosition(
                    Point(position.latitude, position.longitude),
                    latestCameraZoom ?: 0F,
                    latestCameraAzimuth ?: 0F,
                    latestCameraTilt ?:0F
                )
            )
            latestCameraPosition = null
            latestCameraZoom = null
            latestCameraAzimuth = null
            latestCameraTilt = null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView?.let { map ->
            map.map.cameraPosition.run {
                latestCameraZoom = zoom
                latestCameraPosition = target
                latestCameraAzimuth = azimuth
                latestCameraTilt = tilt
            }
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

    private companion object {
        private const val SAVED_LATITUDE = "saved_latitude"
        private const val SAVED_LONGITUDE = "saved_longitude"
        private const val SAVED_ZOOM = "saved_zoom"
        private const val SAVED_AZIMUTH = "saved_azimuth"
        private const val SAVED_TILT = "saved_tilt"
        private const val SAVED_API_KEY = "saved_api_key"
    }
}
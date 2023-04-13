package com.ub.yandex

import androidx.fragment.app.Fragment
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.Map
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Delegate to getting ready to work with inititalized [Map] instance
 */
@FunctionalInterface
fun interface YandexMapReadyDelegate {
    fun onMapReady(yandexMap: Map)
}

/**
 * Delegate to set up the YandexApiKey
 *
 * This must be called before using [YandexMapFragment]
 */
interface ApiKeyDelegate {
    fun setApiKey(value: String)
}

/**
 * Delegate to set up the start location on map
 *
 * [CameraPosition] from [setInitialLocation] will be used only in first open [Map] and
 * afterwards [Map] will be saving and restoring [Map.cameraPosition] automatically
 */
interface YandexInitialCameraPositionDelegate {
    fun setInitialLocation(position: CameraPosition)
}

/**
 * Map initializer delegate
 */
internal fun mapInitializer(mapInitializer: (String, CameraPosition?) -> Unit) = MapInitializerDelegate(mapInitializer)

internal class MapInitializerDelegate(mapInitializer: (String, CameraPosition?) -> Unit) : ReadOnlyProperty<Fragment, MapInitializer> {

    private val initializer = MapInitializer(mapInitializer)

    override fun getValue(thisRef: Fragment, property: KProperty<*>): MapInitializer {
        return initializer
    }
}

internal interface ApiKeyReader {
    fun getApiKey(): String?
}

internal class MapInitializer(private val mapInitializer: (String, CameraPosition?) -> Unit): ApiKeyDelegate,
    ApiKeyReader, YandexInitialCameraPositionDelegate {

    private var apiKey: String? = null
    private var cameraPosition: CameraPosition? = null

    override fun setApiKey(value: String) {
        this.apiKey = value
    }

    override fun getApiKey(): String? {
        return apiKey
    }

    override fun setInitialLocation(position: CameraPosition) {
        this.cameraPosition = position
    }

    fun initialize() {
        if (!mapStateInitialization) {
            mapInitializer.invoke(
                apiKey ?: throw IllegalArgumentException("Please set apk key first"),
                cameraPosition
            )
            mapStateInitialization = true
        }
    }

    companion object {
        private var mapStateInitialization: Boolean = false
    }
}
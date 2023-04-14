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
 * Map Kit initializer delegate
 */
internal fun mapKitInitializer(mapInitializer: (String) -> Unit) = MapKitInitializerDelegate(mapInitializer)

internal fun initialLocation(initialLocation: (CameraPosition?) -> Unit) = InitialLocationDelegate(initialLocation)

internal class MapKitInitializerDelegate(mapInitializer: (String) -> Unit) : ReadOnlyProperty<Fragment, MapKitInitializer> {

    private val initializer = MapKitInitializer(mapInitializer)

    override fun getValue(thisRef: Fragment, property: KProperty<*>): MapKitInitializer {
        return initializer
    }
}

internal class InitialLocationDelegate(initialLocation: (CameraPosition?) -> Unit) : ReadOnlyProperty<Fragment, InitialLocation> {

    private val initialLocation = InitialLocation(initialLocation)

    override fun getValue(thisRef: Fragment, property: KProperty<*>): InitialLocation {
        return initialLocation
    }
}

internal interface ApiKeyReader {
    fun getApiKey(): String?
}

internal class MapKitInitializer(private val mapInitializer: (String) -> Unit): ApiKeyDelegate,
    ApiKeyReader {

    private var apiKey: String? = null

    override fun setApiKey(value: String) {
        this.apiKey = value
    }

    override fun getApiKey(): String? {
        return apiKey
    }

    fun initialize() {
        if (!mapStateInitialization) {
            mapInitializer.invoke(
                apiKey ?: throw IllegalArgumentException("Please set apk key first")
            )
            mapStateInitialization = true
        }
    }

    companion object {
        private var mapStateInitialization: Boolean = false
    }
}

internal class InitialLocation(private val initialLocationDelegate: (CameraPosition?) -> Unit) : YandexInitialCameraPositionDelegate {

    private var cameraPosition: CameraPosition? = null

    override fun setInitialLocation(position: CameraPosition) {
        this.cameraPosition = position
    }

    fun setUpInitialLocation() {
        initialLocationDelegate.invoke(cameraPosition)
    }
}